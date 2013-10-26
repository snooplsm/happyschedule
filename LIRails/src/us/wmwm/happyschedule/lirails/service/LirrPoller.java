package us.wmwm.happyschedule.lirails.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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


public class LirrPoller implements Poller {
	
	private static final String TRK = "Track";
	private static final String STATUS = "Status";
	private static final String TRAIN = "TRAIN";
	private static final String LINE = "For";
	private static final String TO = "TO";
	private static final String ARRIVE = "Sched Arr";
	private static final String DEPARTS = "Departs";
	
	private static Set<String> NEW_YORK_IDS = new HashSet<String>();
	
	static {
		NEW_YORK_IDS.add("8");//PENN STATION
		NEW_YORK_IDS.add("9");//WOODSIDE
		NEW_YORK_IDS.add("1");//LONG ISLAND CITY
		NEW_YORK_IDS.add("2");//HUNTERSPOINT AVE
		NEW_YORK_IDS.add("17");//METS-willet
		NEW_YORK_IDS.add("10");//forrest hills
		NEW_YORK_IDS.add("11");//kew gardens
		NEW_YORK_IDS.add("15");//jamaica
		NEW_YORK_IDS.add("14");//east new york
		NEW_YORK_IDS.add("13");//nostrand ave
		NEW_YORK_IDS.add("12");//atlatnci terminal
	}	
	
	public Map<String, FareType> getFareTypes(Map<String,StationInterval> inter) {
		Map<String,FareType> tinfo = new HashMap<String,FareType>();
		for(Map.Entry<String, StationInterval> e : inter.entrySet()) {
			TripInfo info = ScheduleDao.get().getStationTimesForTripId(e.getKey(), 0, Integer.MAX_VALUE);
			if(info.stops!=null && info.stops.size()>0) {
				Stop first = info.stops.get(0);
				Stop last = info.stops.get(info.stops.size()-1);
				Calendar arrive = last.arrive;
				Calendar depart = first.depart;
				if(arrive.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||arrive.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
					if(depart.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||depart.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
						tinfo.put(e.getKey(), FareType.OFFPEAK);
						continue;
					}
				}
				if(NEW_YORK_IDS.contains(first.id)) {
					int departHour = depart.get(Calendar.HOUR_OF_DAY);
					int day = depart.get(Calendar.DAY_OF_WEEK);
					int min = depart.get(Calendar.MINUTE);
					if(departHour>=16 && departHour<=20) {
						boolean can = true;
						if(departHour==20) {
							can = min==0;
						}
						if(day!=Calendar.SUNDAY && day!=Calendar.MONDAY) {
							if(can) {
								tinfo.put(e.getKey(), FareType.PEAK);
								continue;
							}
							
						}
					} else {
						tinfo.put(e.getKey(), FareType.OFFPEAK);
						continue;
					}
				}
				Stop earliestNYC = null;
				for(int i = info.stops.size()-1; i>0; i--) {
					Stop stop = info.stops.get(i);
					if(NEW_YORK_IDS.contains(stop.id)) {
						earliestNYC = stop;
					} else {
						break;
					}
				}
				if(earliestNYC==null) {
					tinfo.put(e.getKey(), FareType.OFFPEAK);
					continue;
				}
				int arriveHour = earliestNYC.arrive.get(Calendar.HOUR_OF_DAY);
				int day = earliestNYC.arrive.get(Calendar.DAY_OF_WEEK);
				int min = earliestNYC.arrive.get(Calendar.MINUTE);
				if(arriveHour>=6 && arriveHour<=10) {
					boolean can = true;
					if(arriveHour==10) {
						can = min==0;
					}
					if(day!=Calendar.SUNDAY && day!=Calendar.MONDAY) {
						if(can) {
							tinfo.put(e.getKey(), FareType.PEAK);
							continue;
						} else {
							tinfo.put(e.getKey(), FareType.OFFPEAK);
						}
					}
				} else {
					tinfo.put(e.getKey(), FareType.OFFPEAK);
				}
				
			}
		}
		
		return tinfo;
	}
	
	@Override
	public List<TrainStatus> getTrainStatuses(AppConfig config, String station, String stationB) throws IOException {
		URL url = null;
		try {
			Log.d("DeparturePoller", config+"");
			Log.d("DeparturePoller", config.getDepartureVision());
			String u = config.getDepartureVision().replaceAll("\\$stop_id", station);
			if(stationB!=null) {
				u = u.replaceAll("\\$end_id", stationB);
			}
			Log.d("DeparturePoller", u);
			url = new URL(u);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		HttpURLConnection conn = null;
		try {
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3");
			if(conn.getResponseCode()!=200) {
				return Collections.emptyList();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder data = new StringBuilder();
			String line = null;
			List<TrainStatus> statuses = new ArrayList<TrainStatus>(10);
			while ((line = br.readLine()) != null) {
				data.append(line);
			}
//			File file = new File(HappyApplication.get().getExternalFilesDir("files"), "departure.html");
//			FileOutputStream fos = new FileOutputStream(file);
//			fos.write(data.toString().getBytes());
//			fos.close();
			br.close();
//			System.out.println(data.toString());
			Document document = Jsoup.parse(data.toString());
			
			Elements tables = document.getElementsByTag("table");
			
			Element table = tables.first();
			
			Elements trs = table.getElementsByTag("tr");
			
			Element third = trs.get(1);
			
			Elements tds = third.getElementsByTag("th");
			
			Map<String, Integer> typeToPosition = new HashMap<String,Integer>();
			
			
			for(int i = 0; i < tds.size(); i++) {
				Element td = tds.get(i);
				if(TRK.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TRK, i);
					continue;
				}
				if(TRAIN.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TRAIN, i);
					continue;
				}
				if(STATUS.equalsIgnoreCase(td.text())) {
					if(typeToPosition.containsKey(STATUS)) {
						continue;
					}
					typeToPosition.put(STATUS,i);
					continue;
				}
				if(LINE.equalsIgnoreCase(td.text())) {
					typeToPosition.put(LINE, i);
					continue;
				}
				if(TO.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TO, i);
					continue;
				}
				if(DEPARTS.equalsIgnoreCase(td.text())) {
					typeToPosition.put(DEPARTS, i);
					continue;
				}
				if(ARRIVE.equalsIgnoreCase(td.text())) {
					typeToPosition.put(ARRIVE, i);
					continue;
				}
			}
			
			for(int i = 2; i < trs.size(); i++) {
				tds = trs.get(i).getElementsByTag("td");
				String train = tds.get(typeToPosition.get(DEPARTS)).getElementsByAttribute("title").first().attr("title");
				Element track = tds.get(typeToPosition.get(TRK));
				Element status = tds.get(typeToPosition.get(STATUS));
				Element lline = tds.get(typeToPosition.get(LINE));
				//Element to = tds.get(typeToPosition.get(TO));
				Element departs = tds.get(typeToPosition.get(DEPARTS));
				Element arrives = tds.get(typeToPosition.get(ARRIVE));
					TrainStatus tstatus = new TrainStatus();
					tstatus.setStatus(status.text().trim());
					tstatus.setTrack(track.text().trim());
					tstatus.setTrain(train.trim());
					tstatus.setLine(lline.text().trim());
					tstatus.setDest(lline.text().trim());
					tstatus.setDeparts(departs.text().replaceAll("AM", "").replaceAll("PM", "").trim());
					tstatus.setArrives(arrives.text().replaceAll("AM", "").replaceAll("PM", "").trim());
					statuses.add(tstatus);
			}
			
			return statuses;
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public boolean isArrivalStationRequired() {
		return true;
	}
	
	
}