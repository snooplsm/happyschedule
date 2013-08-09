package us.wmwm.happyschedule.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.graphics.Color;

public class LineStyle {

	public int color;
	public String acronym;
	public Map<String, String> keys = new HashMap<String, String>();

	public LineStyle(JSONObject o) {
		color = Color.parseColor(o.optString("color"));
		acronym = o.optString("acronym");
		JSONObject kk = o.optJSONObject("keys");
		if (kk != null) {
			Iterator<String> kiter = kk.keys();

			while (kiter.hasNext()) {
				keys.put(kiter.next(), "");
			}
		}
	}

}
