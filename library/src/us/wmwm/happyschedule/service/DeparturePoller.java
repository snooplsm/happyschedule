package us.wmwm.happyschedule.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.wmwm.happyschedule.model.TrainStatus;

/**
 * Hello world!
 * 
 */
public class DeparturePoller {
	
	private static final String TRK = "TRK";
	private static final String STATUS = "STATUS";
	private static final String TRAIN = "TRAIN";
	private static final String LINE = "LINE";
	private static final String TO = "TO";
	private static final String DEPARTS = "DEP";
	
	public List<TrainStatus> getTrainStatuses(String station) throws IOException {
		URL url = null;
		try {
			url = new URL("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+station);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(20000);
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
			br.close();
			Document document = Jsoup.parse(data.toString());
			
			Elements tables = document.getElementsByTag("table");
			
			Element table = tables.first();
			
			Elements trs = table.getElementsByTag("tr");
			
			Element third = trs.get(2);
			
			Elements tds = third.getElementsByTag("td");
			
			Map<String, Integer> typeToPosition = new HashMap<String,Integer>();
			
			for(int i = 0; i < tds.size(); i++) {
				Element td = tds.get(i);
				if(TRK.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TRK, i);
				}
				if(TRAIN.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TRAIN, i);
				}
				if(STATUS.equalsIgnoreCase(td.text())) {
					typeToPosition.put(STATUS,i);
				}
				if(LINE.equalsIgnoreCase(td.text())) {
					typeToPosition.put(LINE, i);
				}
				if(TO.equalsIgnoreCase(td.text())) {
					typeToPosition.put(TO, i);
				}
				if(DEPARTS.equalsIgnoreCase(td.text())) {
					typeToPosition.put(DEPARTS, i);
				}
			}
			
			for(int i = 3; i < trs.size(); i++) {
				tds = trs.get(i).getElementsByTag("td");
				Element train = tds.get(typeToPosition.get(TRAIN));
				Element track = tds.get(typeToPosition.get(TRK));
				Element status = tds.get(typeToPosition.get(STATUS));
				Element lline = tds.get(typeToPosition.get(LINE));
				Element to = tds.get(typeToPosition.get(TO));
				Element departs = tds.get(typeToPosition.get(DEPARTS));
					TrainStatus tstatus = new TrainStatus();
					tstatus.setStatus(status.text());
					tstatus.setTrack(track.text());
					tstatus.setTrain(train.text());
					tstatus.setLine(lline.text());
					tstatus.setDest(to.text());
					tstatus.setDeparts(departs.text());
					statuses.add(tstatus);
			}
			
			return statuses;
		} catch (IOException e) {
			throw e;
		}
	}
}