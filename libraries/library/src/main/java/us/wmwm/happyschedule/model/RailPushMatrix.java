package us.wmwm.happyschedule.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RailPushMatrix {

	Map<String, Map<Integer, Set<Integer>>> railToDays = new HashMap<String, Map<Integer, Set<Integer>>>();

	public RailPushMatrix() {
	}

	public RailPushMatrix(JSONObject o) {
		Iterator<String> key = o.keys();
		while (key.hasNext()) {
			String kk = key.next();
			JSONObject d = o.optJSONObject(kk);
			Map<Integer, Set<Integer>> d2 = new HashMap<Integer, Set<Integer>>();
			railToDays.put(kk, d2);
			Iterator<String> k2 = d.keys();
			while (k2.hasNext()) {
				Set<Integer> hours = new HashSet<Integer>();
				String kk2 = k2.next();
				d2.put(Integer.parseInt(kk2), hours);
				JSONArray ints = d.optJSONArray(kk2);
				if (ints != null) {
					for (int i = 0; i < ints.length(); i++) {
						hours.add(ints.optInt(i));
					}
				}
			}
		}
	}

	public Set<Integer> getChecked(String rail, int day) {
		Map<Integer, Set<Integer>> c = railToDays.get(rail);
		if (c == null) {
			return Collections.emptySet();
		}
		Set<Integer> d = c.get(day);
		if (d == null) {
			return Collections.emptySet();
		}
		return d;
	}

	public void update(AppRailLine line, int dayOfWeek, int hour,
			boolean checked) {
		Map<Integer, Set<Integer>> rLine = railToDays.get(line.getKey());
		if (rLine == null) {
			rLine = new HashMap<Integer, Set<Integer>>();
			railToDays.put(line.getKey(), rLine);
		}
		Set<Integer> hours = rLine.get(dayOfWeek);
		if (hours == null) {
			hours = new HashSet<Integer>();
			rLine.put(dayOfWeek, hours);
		}
		if (checked) {
			hours.add(hour);
		} else {
			hours.remove(hour);

		}
	}

    public void addAllDays(String line) {
        AppRailLine l = new AppRailLine();
        l.setKey(line);
        for(int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
            for(int hour = 0; hour < 24; hour++) {
                update(l,day,hour,true);
            }
        }
    }

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		for (Map.Entry<String, Map<Integer, Set<Integer>>> ee : railToDays
				.entrySet()) {
			if(ee.getKey().length()==0) {
				continue;
			}
			JSONObject d = new JSONObject();
			for (Map.Entry<Integer, Set<Integer>> e2 : ee.getValue().entrySet()) {
				JSONArray a = new JSONArray();
				for (Integer inter : e2.getValue()) {
					a.put(inter);
				}
				try {
					d.putOpt(e2.getKey().toString(), a);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				o.putOpt(ee.getKey(), d);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return o;
	}
}
