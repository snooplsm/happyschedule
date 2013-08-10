package us.wmwm.happyschedule.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;


public class Schedule implements Serializable {

	private static final long serialVersionUID = 1L;

	public Map<String, Service> services;
	public Map<String, Trip> tripIdToTrip;
	public String departId;
	public String arriveId;
	public Map<String, Integer> transferEdges;
	public String[][] transfers;
	public Map<String[], Map<String,List<ConnectionInterval>>> connections;
	public Map<String, StationToStation> tripIdToBlockId;
	public Map<String, String> stopIdToName;
	public Map<String, Set<String>> blockIdToTripId;
	public Map<String, String> routeIdToName;
	public Map<String[],Set<String>> inOrder;
	public Map<String[],Set<String>> reverseOrder;
	public Date start;
	public Date end;
	public Date userStart;
	public Date userEnd;
	public Map<String[], List<StationInterval>> stationIntervals;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	private Map<String,StationInterval> goodStations = new HashMap<String, StationInterval>();
	
	public Schedule() {}
	
	public Schedule(JSONObject o) {
		JSONArray a = o.optJSONArray("services");
		services = new HashMap<String,Service>();
		if(a!=null) {
			for(int i = 0 ; i < a.length(); i++) {
				JSONObject ob = a.optJSONObject(i);
				Service service = new Service(ob);
				services.put(service.serviceId, service);				
			}
		}
		a = o.optJSONArray("tripIdToTrip");
		tripIdToTrip = new HashMap<String,Trip>();
		if(a!=null) {
			for(int i = 0; i < a.length(); i++) {
				JSONObject ob = a.optJSONObject(i);
				Trip trip = new Trip(ob);
				tripIdToTrip.put(trip.id, trip);
			}
		}
		departId = o.optString("departId");
		arriveId = o.optString("arriveId");
	}

	void traverse(List<? extends StationToStation> stationToStations,
			ScheduleTraverser traverser) {
		int size = 0;
		if (stationToStations != null) {
			size = stationToStations.size();
		}
		int sum = 0;
		int nsize = size;
		LinkedHashSet<Integer> toRemove = new LinkedHashSet<Integer>();
		Map<StationInterval, Integer> cache = new HashMap<StationInterval,Integer>();
		for (int i = 0; i < size; i++) {
			StationInterval s = (StationInterval) stationToStations.get(i);
			Integer tot = cache.get(s);
			if(tot==null) {
				tot = s.totalMinutes();
				cache.put(s,tot);
			}
			if (tot < 0) {
				nsize--;
				toRemove.add(i);
			} else {
//				int count = 0;
//				boolean addedFirst = false;
//				Calendar now = Calendar.getInstance();
//				now.setTimeInMillis(s.getDepartTime().getTimeInMillis());
//				now.add(Calendar.MINUTE, tot);
//				for(int j = i+1; j < size; j++) {
//					StationInterval t = (StationInterval) stationToStations.get(j);
//					tot = cache.get(t);
//					if(tot==null) {
//						tot = t.totalMinutes();
//						cache.put(t,tot);
//					}
//					if(tot<0) {
//						continue;
//					}
//					Calendar later = Calendar.getInstance();
//					later.setTimeInMillis(t.getDepartTime().getTimeInMillis());
//					later.add(Calendar.MINUTE, tot);
//					//now.add(Calendar.MINUTE, tot);
//					System.out.println(now.getTime() + " vs " + later.getTime());
//					if(now.equals(later)) {
//						count++;
//						if(!addedFirst) {
//							count++;
//							toRemove.add(i);
//							addedFirst = true;
//						}
//						toRemove.add(j);
//					} else {
//						if(count>0) {
//							toRemove.remove(toRemove.size()-1);
//						}
//						if(count>1) {
//							toRemove.remove(toRemove.size()-1);
//						}
//						i = j-1;
//						break;
//					}
				}
//			}
		}
		
		Integer[] k = toRemove.toArray(new Integer[toRemove.size()]);
		
		for (int i = k.length - 1; i > -1; i--) {
//			System.out.println(stationToStations.get(k[i].intValue()));
			stationToStations.remove(k[i].intValue());
		}
		nsize = 0;
		if(stationToStations!=null) {
			nsize = stationToStations.size();
		}
		for (int i = 0; i < nsize; i++) {
			int tot = cache.get(stationToStations.get(i));
			sum+=tot;
		}
		double mean = sum / (double) size;
		double std = Math.sqrt(sum / (double) size);
		double norm = std * 6 + mean +.5*mean;
		for (int i = 0; i < nsize; i++) {
			StationInterval s = (StationInterval) stationToStations.get(i);
			int duration = cache.get(s);
			boolean canAdd = true;
			if (duration > norm) {
				canAdd = false;
			}
			if (canAdd) {
				goodStations.put(s.tripId, s);
				traverser.populateItem(i, s, size);
			}
		}
	}

	void traversal(ScheduleTraverser traversal,Map<String[],Set<String>> tripIds) {
		int first = 0;
		int ignore = 0;
		Set<Integer> deleteMe = new HashSet<Integer>();
		for (int i = 0; i < transfers.length; i++) {
			String[] pair = transfers[i];
			if (pair[0].equals(pair[1])) {
				deleteMe.add(i);
				ignore++;
				continue;
			} else {
				first = i;
				break;
			}
		}
		ArrayList<String[]> d = new ArrayList<String[]>(transfers.length
				- deleteMe.size());
		for (int i = 0; i < transfers.length; i++) {
			if (!deleteMe.contains(i)) {
				d.add(transfers[i]);
			}
		}
		transfers = new String[d.size()][2];
		d.toArray(transfers);

		int goback = 0;
		for (int i = 0; i < transfers.length; i++) {
			String[] pair = transfers[i];
			Integer transferDuration = transferEdges.get(pair[0] + "-"
					+ pair[1]);
			if (transferDuration != null) {
				goback++;
			} else {
				break;
			}
		}

		stationIntervals = new HashMap<String[], List<StationInterval>>();

		for (int i = goback; i < transfers.length; i++) {
			String[] pair = transfers[i];
			Integer transferDuration = transferEdges.get(pair[0] + "-"
					+ pair[1]);
			if (transferDuration != null) {
//				System.out.println(transferDuration);
			}
			Map<String,List<ConnectionInterval>> k = connections.get(pair);
			// if k is null, this is a transfer edge.
			if (k == null || k.isEmpty()) {
				continue;
			}
			//for(String tripId : tr)
			
			Set<StationInterval> intervals = new HashSet<StationInterval>();

			for(String tripId : tripIds.get(pair)) {
				List<ConnectionInterval> ok = k.get(tripId);
				ConnectionInterval a = ok.get(0);
				ConnectionInterval b = ok.get(1);
				Service service = services.get(a.serviceId);
				if (service == null || service.dates == null) {
					continue;
				}
				Calendar before = Calendar.getInstance();
				before.setTimeInMillis(start.getTime());
				before.add(Calendar.DAY_OF_YEAR, -1);
				for (Date date : service.dates) {
					if (date.getTime() >= before.getTimeInMillis()
							&& date.getTime() <= end.getTime()) {
						try {
							Date arrive = sdf.parse(b.arrival);
							Date depart = sdf.parse(a.departure);
							// System.out.println(depart + " - " + arrive);
							Calendar arriveTime = convert(date, arrive);
							Calendar departTime = convert(date, depart);
							// System.out.println(depart + " - " + arrive);
							StationInterval si = new StationInterval();
							si.departSequence = a.sequence;
							si.arriveSequence = b.sequence;
							si.departId = pair[0];
							si.arriveId = pair[1];
							si.arriveTime = arriveTime;
							si.departTime = departTime;
							si.blockId = a.blockId;
							si.tripId = tripId;
							si.routeId = a.routeId;
							si.level = i;
							si.schedule = this;
							intervals.add(si);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
			
//			for (ConnectionInterval interval : k.g) {
//				Service service = services.get(interval.serviceId);
//				if (service == null || service.dates == null) {
//					continue;
//				}
//				for (Date date : service.dates) {
//					if (date.getTime() >= start.getTime()
//							&& date.getTime() <= end.getTime()) {
//						try {
//							Date arrive = sdf.parse(interval.arrival);
//							Date depart = sdf.parse(interval.departure);
//							// System.out.println(depart + " - " + arrive);
//							Calendar arriveTime = convert(date, arrive);
//							Calendar departTime = convert(date, depart);
//							// System.out.println(depart + " - " + arrive);
//							StationInterval si = new StationInterval();
//							si.departId = pair[0];
//							si.arriveId = pair[1];
//							si.arriveTime = arriveTime;
//							si.departTime = departTime;
//							si.blockId = interval.blockId;
//							si.tripId = interval.tripId;
//							si.routeId = interval.routeId;
//							si.level = i;
//							si.schedule = this;
//							intervals.add(si);
//						} catch (Exception e) {
//							throw new RuntimeException(e);
//						}
//					}
//				}
//			}
			List<StationInterval> ints = new ArrayList<StationInterval>(
					intervals);
			stationIntervals.put(pair, ints);
			Collections.sort(ints, new Comparator<StationInterval>() {

				@Override
				public int compare(StationInterval arg0, StationInterval arg1) {
					return arg0.departTime.compareTo(arg1.departTime);
				}

			});
		}
		if (transfers.length == 0) {
			traverse((List<StationInterval>) Collections.EMPTY_LIST, traversal);
		} else {
			Collections.sort(stationIntervals.get(transfers[goback]), new Comparator<StationInterval>() {
				@Override
				public int compare(StationInterval lhs, StationInterval rhs) {
					return lhs.departTime.compareTo(rhs.departTime);
				}
			});
			List<StationInterval> intervals = stationIntervals.get(transfers[goback]);
			traverse(intervals, traversal);
		}
	}

	public StationInterval getStationIntervalForTripId(String tripId) {
		return goodStations.get(tripId);
	}
	
	private Calendar convert(Date date, Date dateTime) {
		Calendar caldate = Calendar.getInstance();
		caldate.setTime(date);
		Calendar caldatetime = Calendar.getInstance();
		caldatetime.setTime(dateTime);
		caldate.add(Calendar.DAY_OF_YEAR,
				caldatetime.get(Calendar.DAY_OF_YEAR) - 1);
		caldate.set(Calendar.HOUR_OF_DAY, caldatetime.get(Calendar.HOUR_OF_DAY));
		caldate.set(Calendar.MINUTE, caldatetime.get(Calendar.MINUTE));
		caldate.set(Calendar.SECOND, caldatetime.get(Calendar.SECOND));
		return caldate;
	}

	void traversal(Set<String> orderedTripIds, ScheduleTraverser traversal) {
		TreeMap<Date, Set<String>> dateToTripIds = new TreeMap<Date, Set<String>>();
		for (String tripId : orderedTripIds) {
			// List<StopTime> stopTimes = tripIdToStopTimes.get(tripId);
			// StopTime depart = stopTimes.get(0);
			// Service service = services.get(depart.serviceId);
			// if(service.dates==null) {
			// continue;
			// }
			// for (Date date : service.dates) {
			// if (date.getTime() >= start.getTime()
			// && date.getTime() <= end.getTime()) {
			// Set<String> tripIds = dateToTripIds.get(date);
			// if (tripIds == null) {
			// tripIds = new HashSet<String>();
			// dateToTripIds.put(date, tripIds);
			// }
			// tripIds.add(tripId);
			// }
			// }
		}
		TreeMap<Date, List<StationToStation>> stationToStation = new TreeMap<Date, List<StationToStation>>();
		for (Map.Entry<Date, Set<String>> entry : dateToTripIds.entrySet()) {
			Date date = entry.getKey();
			Calendar dateCal = Calendar.getInstance();
			dateCal.setTime(date);
			List<StationToStation> _stationToStations = stationToStation
					.get(date);
			if (_stationToStations == null) {
				_stationToStations = new ArrayList<StationToStation>();
				stationToStation.put(date, _stationToStations);
			}
			blockIdToTripId = new HashMap<String, Set<String>>();
			Collections.sort(_stationToStations,
					new Comparator<StationToStation>() {

						@Override
						public int compare(StationToStation a,
								StationToStation b) {
							return a.departTime.compareTo(b.departTime);
						}

					});
			traverse(_stationToStations, traversal);
		}
	}

	public void inOrderTraversal(ScheduleTraverser t) {
		this.traversal(t,inOrder);
	}

	public void inReverseOrderTraversal(ScheduleTraverser t) {
		this.traversal(t,reverseOrder);
	}

	private Calendar populate(Date day, Date time) {
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(day);
		Calendar departCal = Calendar.getInstance();
		departCal.setTime(time);
		Calendar departTime = Calendar.getInstance();
		departTime.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
		departTime.set(Calendar.DAY_OF_YEAR, dateCal.get(Calendar.DAY_OF_YEAR));
		departTime.set(Calendar.HOUR_OF_DAY,
				departCal.get(Calendar.HOUR_OF_DAY));
		departTime.set(Calendar.MINUTE, departCal.get(Calendar.MINUTE));
		departTime.set(Calendar.SECOND, departCal.get(Calendar.SECOND));
		return departTime;
	}

	public String tripIdToBlockId(String tripId) {
		if (tripIdToTrip == null || tripId == null) {
			return null;
		}
		Trip trip = tripIdToTrip.get(tripId);
		if (trip == null) {
			return null;
		}
		return trip.blockId;
	}

	public Set<String> blockidToTripId(String blockId) {
		Set<String> s = blockIdToTripId.get(blockId);
		if (s == null)
			return Collections.emptySet();
		return s;
	}
}
