package us.wmwm.happyschedule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

public class ScheduleDao {

	private Context context;
	
	SharedPreferences preferences;

	private static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	private static ScheduleDao INSTANCE;
	
	public static ScheduleDao get() {
		if(INSTANCE==null) {
			INSTANCE = new ScheduleDao();
		}
		return INSTANCE;
	}
	
	public Date getMinDate() {
		Cursor c = Db.get().db.rawQuery("select min(date) from service", null);
		try {
			c.moveToFirst();
			String txt = c.getString(0);
			return SIMPLE.parse(txt);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return null;
	}
	
	public Date getMaxDate() {
		Cursor c = Db.get().db.rawQuery("select max(date) from service", null);
		try {
			c.moveToFirst();
			String txt = c.getString(0);
			return SIMPLE.parse(txt);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return null;
	}

	public TripInfo getStationTimesForTripId(String tripId, int departSequence, int arriveSequence) {
		Cursor c = Db.get().db.rawQuery("select stop_id,depart,arrive,route_id from nested_trip where trip_id=? and lft between ? and ? order by lft asc", new String[]{tripId, String.valueOf(departSequence), String.valueOf(arriveSequence)});
		TripInfo info = new TripInfo();
		info.stops = new ArrayList<TripInfo.Stop>(c.getCount());
		Map<String,TripInfo.Stop> whatis = new HashMap<String,TripInfo.Stop>();
		while(c.moveToNext()) {
			String stopId = c.getString(0);
			String departure = c.getString(1);
			String arrive = c.getString(2);
			String routeId = c.getString(3);
			info.routeId = routeId;
			TripInfo.Stop stop = new TripInfo.Stop();
			whatis.put(stopId, stop);		
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(timeFormat.parse(departure));
				stop.depart = cal;
				cal = Calendar.getInstance();
				cal.setTime(timeFormat.parse(arrive));
				stop.arrive = cal;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			info.stops.add(stop);
		}
		c.close();
		c = Db.get().db.rawQuery(String.format("select stop_id,name from stop where stop_id in (%s)",join(whatis.keySet(),",")),null);
		while(c.moveToNext()) {
			String id = c.getString(0);
			String name =c.getString(1);
			whatis.get(id).name = name;
		}
		c.close();
		return info;
	}
	
	private Long clearExtraFields(Date d) {
		Calendar c = Calendar.getInstance();
		c.clear();
		Calendar dc = Calendar.getInstance();
		dc.setTime(d);
		c.set(Calendar.YEAR, dc.get(Calendar.YEAR));
		c.set(Calendar.DAY_OF_YEAR, dc.get(Calendar.DAY_OF_YEAR));
		return c.getTimeInMillis();
	}

	private static final SimpleDateFormat SIMPLE = new SimpleDateFormat(
			"yyyyMMdd");

	/**
	 * 
	 * 
	 * @param d
	 * @return YYMMDD
	 */
	private String simpleDate(Date d) {
		return SIMPLE.format(d);
	}

	public Schedule getSchedule(final String departStationId,
			final String arriveStationId, Date start, Date end) {
		Cursor cur = Db.get().db
				.rawQuery(
						"select a,b from schedule_path where source=? and target=? and level=0 order by sequence asc",
						new String[] { departStationId, arriveStationId });
		String[][] pairs = new String[cur.getCount()][2];
		int i = 0;
		Set<String> p = new HashSet<String>();
		while (cur.moveToNext()) {
			pairs[i][0] = cur.getString(0);
			pairs[i][1] = cur.getString(1);
			// System.out.println(pairs[i][0] + " - " + pairs[i][1]);
			p.add(pairs[i][0]);
			p.add(pairs[i][1]);
			i++;
		}
		cur.close();
		cur = Db.get().db.rawQuery(String.format(
				"select stop_id, name from stop where stop_id in (%s)",
				join(p, ",")), null);
		Map<String, String> idToName = new HashMap<String, String>();
		while (cur.moveToNext()) {
			String id = cur.getString(0);
			String name = cur.getString(1);
			idToName.put(id, name);
		}
		cur.close();
		Date startDate = new Date(clearExtraFields(start));
		Date endDate = new Date(clearExtraFields(end));
		
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(Calendar.DAY_OF_YEAR, -1);
		String startString = simpleDate(c.getTime());
		String middle = simpleDate(startDate);
		String endString = simpleDate(endDate);
		
//		System.out.println("start : " + startString);
//		System.out.println("end   : " + endString);


		Map<String[], Map<String, List<ConnectionInterval>>> pairToTimes = new HashMap<String[], Map<String, List<ConnectionInterval>>>();
		Map<String, Integer> transferEdges = new HashMap<String, Integer>();
		Map<String, List<StopTime>> tripToResult = new HashMap<String, List<StopTime>>();
		Set<String> serviceIds = new HashSet<String>();

		Map<String, Service> services = new HashMap<String, Service>();
		Set<String> tripIds = new HashSet<String>();
		Map<String, String> routeIds = new HashMap<String, String>();
		String stationsFragment = "stop_id=" + join(p, " or stop_id=");
		String query = "select a1.depart,a1.arrive,a1.service_id,a1.trip_id,a1.block_id,a1.route_id,a1.stop_id,a1.lft from nested_trip a1 where a1.stop_id=? or a1.stop_id=? and a1.service_id in (select service_id from service where date in(:foo))";
		for (i = 0; i < pairs.length; i++) {
			Map<String, List<ConnectionInterval>> tripToConnectionIntervals = new HashMap<String, List<ConnectionInterval>>();
			Cursor rur = Db.get().db
					.rawQuery(
							"select duration from transfer_edge where source=? and target=?",
							new String[] { pairs[i][0], pairs[i][1] });
			if (rur.moveToNext()) {
				Integer duration = rur.getInt(0);
				transferEdges.put(pairs[i][0] + "-" + pairs[i][1], duration);
				rur.close();
				continue;
			}
			rur.close();
			Cursor qur = Db.get().db.rawQuery(query.replace(":foo", startString+","+middle+","+endString), new String[] { pairs[i][0],
					pairs[i][1] });
			pairToTimes.put(pairs[i], tripToConnectionIntervals);
			while (qur.moveToNext()) {
				String depart = qur.getString(0);
				String arrive = qur.getString(1);
				String serviceId = qur.getString(2);
				String tripId = qur.getString(3);
				String blockId = qur.getString(4);
				String routeId = qur.getString(5);
				String stopId = qur.getString(6);
				int seq = qur.getInt(7);
				routeIds.put(routeId, "");
				ConnectionInterval interval = new ConnectionInterval();
				interval.tripId = tripId;
				interval.sequence = seq;
				tripIds.add(tripId);
				interval.routeId = routeId;
				interval.serviceId = serviceId;
				interval.departure = depart;
				interval.arrival = arrive;
				interval.sourceId = stopId;
				interval.targetId = stopId;
				interval.blockId = blockId;
				serviceIds.add(serviceId);
				List<ConnectionInterval> cin = tripToConnectionIntervals
						.get(tripId);
				if (cin == null) {
					cin = new ArrayList<ConnectionInterval>(2);
					tripToConnectionIntervals.put(tripId, cin);
				}
				cin.add(interval);
			}
			qur.close();
		}

		cur = Db.get().db.rawQuery(
				"select date,service_id from service where date in (:foo)".replace(":foo", startString+","+middle+","+endString),
				null);
		while (cur.moveToNext()) {
			String serviceId = cur.getString(1);
			Service s = services.get(serviceId);
			if (s == null) {
				s = new Service();
				s.serviceId = serviceId;
				services.put(s.serviceId, s);
			} else {
			}
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTimeInMillis(SIMPLE.parse(cur.getString(0)).getTime());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			Date dateD = cal.getTime();
			if (dateD.getTime() > endDate.getTime()
					|| dateD.getTime() < c.getTimeInMillis()) {
			} else {
				s.dates.add(dateD);
			}
		}
		cur.close();
		cur = Db.get().db.rawQuery(String.format(
				"select route_id, name from route where route_id in (%s)",
				join(routeIds.keySet(), ",")), null);
		while (cur.moveToNext()) {
			String routeId = cur.getString(0);
			String name = cur.getString(1);
			routeIds.put(routeId, name);
		}
		Map<String[],Set<String>> regular = new HashMap<String[],Set<String>>();
		Map<String[],Set<String>> reverse = new HashMap<String[],Set<String>>();
		for (String[] pair : pairs) {
			Set<String> reg = new HashSet<String>();
			Set<String> rev = new HashSet<String>();
			regular.put(pair, reg);
			reverse.put(pair, rev);
			if(pairToTimes.get(pair)==null) {
				continue;
			}
			for (Map.Entry<String, List<ConnectionInterval>> e : pairToTimes
					.get(pair).entrySet()) {
				List<ConnectionInterval> value = e.getValue();
				if(value.size()!=2) {
					continue;
				}
				ConnectionInterval a = value.get(0);
				ConnectionInterval b = value.get(1);
				if(a.sourceId.equals(pair[0])) {
					if(a.sequence<b.sequence) {
						reg.add(e.getKey());
					} else {
						Collections.reverse(value);
						rev.add(e.getKey());
					}
				} else {
					if(a.sequence < b.sequence) {
						rev.add(e.getKey());
					} else {
						Collections.reverse(value);
						reg.add(e.getKey());
					}
				}
			}
		}
		Schedule s = new Schedule();
		s.routeIdToName = routeIds;
		s.departId = departStationId;
		s.arriveId = arriveStationId;
		s.transfers = pairs;
		s.services = services;
		s.connections = pairToTimes;
		s.transferEdges = transferEdges;
		s.stopIdToName = idToName;
		s.start = startDate;
		s.end = endDate;
		s.inOrder = regular;
		s.reverseOrder = reverse;
		s.userEnd = end;
		s.userStart = start;
		return s;
	}

	public void name(List<Favorite> favs) {
		Set<String> ids = new HashSet<String>();
		for (Favorite f : favs) {
			ids.add(f.sourceId);
			ids.add(f.targetId);
		}
		if (ids.isEmpty()) {
			return;
		}
		Cursor cursor = Db.get().db.rawQuery(String.format(
				"select stop_id,name from stop where stop_id in (%s)",
				ScheduleDao.join(ids, ",")), null);

		Map<String, String> kk = new HashMap<String, String>();
		while (cursor.moveToNext()) {
			String id = cursor.getString(0);
			String name = cursor.getString(1);
			kk.put(id, name);
		}
		for (Favorite f : favs) {
			f.sourceName = kk.get(f.sourceId);
			f.targetName = kk.get(f.targetId);
		}
	}

	public static String join(String delimiter, Object... s) {
		return join(Arrays.asList(s), delimiter);
	}

	public static String join(Collection<?> s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator<?> iter = s.iterator();
		while (iter.hasNext()) {
			Object nxt = iter.next();
			if (nxt instanceof String) {
				buffer.append('\'');
			}
			buffer.append(nxt);
			if (nxt instanceof String) {
				buffer.append('\'');
			}
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

	public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	public Double getFair(String departId, String arriveId) {
		Cursor c = Db.get().db.rawQuery(
				"select adult from fares where source=? and target=?",
				new String[] { departId, arriveId });
		Double val = null;
		while (c.moveToNext()) {
			val = c.getDouble(0);
		}
		c.close();
		return val;
	}
	
	public HashMap<String, Double> getFairs(String departId, String arriveId) {
		Cursor c = Db.get().db.rawQuery(
				"select adult,child,senior,disabled,weekly,ten_trip,monthly,student_monthly from fares where source=? and target=?",
				new String[] { departId, arriveId });
		HashMap<String, Double> fares = new HashMap<String,Double>();
		while (c.moveToNext()) {
			fares.put("Adult", c.getDouble(0));
			fares.put("Child",c.getDouble(1));
			fares.put("Senior",c.getDouble(2));
			fares.put("Disabled", c.getDouble(3));
			fares.put("Weekly" , c.getDouble(4));
			fares.put("Ten Trip" , c.getDouble(5));
			fares.put("Monthly" , c.getDouble(6));
			fares.put("Student Monthly" , c.getDouble(7));
		}
		c.close();
		return fares;
	}
}
