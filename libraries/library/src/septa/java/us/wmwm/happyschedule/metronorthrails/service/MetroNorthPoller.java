package us.wmwm.happyschedule.metronorthrails.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.TrainStatus;
import us.wmwm.happyschedule.model.TripInfo;
import us.wmwm.happyschedule.model.TripInfo.Stop;
import us.wmwm.happyschedule.service.FareType;
import us.wmwm.happyschedule.service.Poller;
import android.util.Log;

public class MetroNorthPoller implements Poller {

	private static final String TRK = "Track";
	private static final String STATUS = "Status";
	private static final String TRAIN = "Station Stops";
	private static final String LINE = "Destination";
	private static final String TO = "TO";
	private static final String ARRIVE = "Sched Arr";
	private static final String DEPARTS = "Scheduled Time";

	private static Set<String> NEW_YORK_IDS = new HashSet<String>();

	static {
		NEW_YORK_IDS.add("1");// Grand Central
		NEW_YORK_IDS.add("4");// Harlem

	}

	public Map<String, FareType> getFareTypes(Map<String, StationInterval> inter) {
		Map<String, FareType> tinfo = new HashMap<String, FareType>();
		for (Map.Entry<String, StationInterval> e : inter.entrySet()) {
			TripInfo info = ScheduleDao.get().getStationTimesForTripId(
					e.getKey(), 0, Integer.MAX_VALUE);
			if (info.stops != null && info.stops.size() > 0) {
				Stop first = info.stops.get(0);
				Stop last = info.stops.get(info.stops.size() - 1);
				Calendar arrive = last.arrive;
				Calendar depart = first.depart;
				if (arrive.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
						|| arrive.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					if (depart.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
							|| depart.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
						tinfo.put(e.getKey(), FareType.OFFPEAK);
						continue;
					}
				}
				if (NEW_YORK_IDS.contains(first.id)) {
					int departHour = depart.get(Calendar.HOUR_OF_DAY);
					int day = depart.get(Calendar.DAY_OF_WEEK);
					int minute = depart.get(Calendar.MINUTE);
					if (departHour >= 16 && departHour <= 20) {
						if (day != Calendar.SUNDAY && day != Calendar.MONDAY) {
							tinfo.put(e.getKey(), FareType.PEAK);
							continue;
						}
					} else if ((departHour >= 5 && minute >= 30)
							&& departHour <= 9) {
						tinfo.put(e.getKey(), FareType.PEAK);
						continue;
					} else {
						tinfo.put(e.getKey(), FareType.OFFPEAK);
						continue;
					}
				}
				Stop earliestNYC = null;
				for (int i = info.stops.size() - 1; i > 0; i--) {
					Stop stop = info.stops.get(i);
					if (NEW_YORK_IDS.contains(stop.id)) {
						earliestNYC = stop;
					} else {
						break;
					}
				}
				if (earliestNYC == null) {
					tinfo.put(e.getKey(), FareType.OFFPEAK);
					continue;
				}
				int arriveHour = earliestNYC.arrive.get(Calendar.HOUR_OF_DAY);
				int day = earliestNYC.arrive.get(Calendar.DAY_OF_WEEK);
				if (arriveHour >= 5 && arriveHour <= 10) {
					if (day != Calendar.SUNDAY && day != Calendar.MONDAY) {
						tinfo.put(e.getKey(), FareType.PEAK);
						continue;
					}
				} else {
					tinfo.put(e.getKey(), FareType.OFFPEAK);
					continue;
				}

			}
		}

		return tinfo;
	}

	@Override
	public List<TrainStatus> getTrainStatuses(AppConfig config, String station,
			String stationB) throws IOException {
		URL url = null;
		try {
			Log.d("DeparturePoller", config + "");
			Log.d("DeparturePoller", config.getDepartureVision());
			String u = config.getDepartureVision();
			Log.d("DeparturePoller", u);
			url = new URL(u);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		HttpURLConnection conn = null;
		try {

			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			Map<String,String> parms = new HashMap<String,String>();
			parms.put("P_AVIS_ID", station);
			parms.put("Get Train Status", "Get Train Status");
			parms.put("refered","ault.cfm");
			StringBuilder b = new StringBuilder();
			Iterator<Map.Entry<String,String>> it = parms.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, String> e = it.next();				
				b.append(URLEncoder.encode(e.getKey(),"UTF-8"));
				b.append("=");
				b.append(URLEncoder.encode(e.getValue(),"UTF-8"));
			}
			String dat = b.toString();
			conn.setRequestProperty("Content-Length", String.valueOf(dat.getBytes().length));
			OutputStream os = conn.getOutputStream();
			os.write(dat.getBytes());
			os.close();
			
			if (conn.getResponseCode() != 200) {
				return Collections.emptyList();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder data = new StringBuilder();
			String line = null;
			List<TrainStatus> statuses = new ArrayList<TrainStatus>(10);
			while ((line = br.readLine()) != null) {
				data.append(line);
			}
			// File file = new
			// File(HappyApplication.get().getExternalFilesDir("files"),
			// "departure.html");
			// FileOutputStream fos = new FileOutputStream(file);
			// fos.write(data.toString().getBytes());
			// fos.close();
			br.close();
			Document document = Jsoup.parse(data.toString());

			Elements tables = document.getElementsByTag("table");

			Element table = tables.first();

			Elements trs = table.getElementsByTag("tr");

			Element third = trs.get(0);

			Elements tds = third.getElementsByTag("td");

			Map<String, Integer> typeToPosition = new HashMap<String, Integer>();

			for (int i = 0; i < tds.size(); i++) {
				Element td = tds.get(i);
				if (TRK.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TRK, i);
					continue;
				}
				if (TRAIN.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TRAIN, i);
					continue;
				}
				if (STATUS.equalsIgnoreCase(td.text())) {					
					typeToPosition.put(STATUS, i);
					continue;
				}
				if (LINE.equalsIgnoreCase(td.text())) {
					typeToPosition.put(LINE, i);
					continue;
				}
				if (TO.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TO, i);
					continue;
				}
				if (DEPARTS.equalsIgnoreCase(td.text())) {
					typeToPosition.put(DEPARTS, i);
					continue;
				}
				if (ARRIVE.equalsIgnoreCase(td.text())) {
					typeToPosition.put(ARRIVE, i);
					continue;
				}
			}

			for (int i = 1; i < trs.size(); i++) {
				tds = trs.get(i).getElementsByTag("td");
				Element trainTd = tds.get(typeToPosition.get(TRAIN));
				String train = trainTd.getElementsByAttributeValue("name", "train_name").first().attr("value");
				Element track = tds.get(typeToPosition.get(TRK));
				Element status = tds.get(typeToPosition.get(STATUS));
				Element lline = tds.get(typeToPosition.get(LINE));
				// Element to = tds.get(typeToPosition.get(TO));
				Element departs = tds.get(typeToPosition.get(DEPARTS));
				//Element arrives = tds.get(typeToPosition.get(ARRIVE));
				TrainStatus tstatus = new TrainStatus();
				tstatus.setStatus(status.text().trim());
				tstatus.setTrack(track.text().trim());
				tstatus.setTrain(train.trim());				
				tstatus.setLine(lline.text().trim());
				tstatus.setDest(lline.text().trim());
				// tstatus.setDest(to.text());
				tstatus.setDeparts(departs.text().replaceAll("AM", "")
						.replaceAll("PM", "").trim());
				//tstatus.setArrives(arrives.text().replaceAll("AM", "")
						//.replaceAll("PM", "").trim());
				statuses.add(tstatus);
			}

			return statuses;
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public boolean isArrivalStationRequired() {
		return false;
	}

}