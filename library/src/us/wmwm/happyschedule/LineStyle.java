package us.wmwm.happyschedule;

import org.json.JSONObject;

import android.graphics.Color;

public class LineStyle {

	public int color;
	public String acronym;
	public String key;
	
	public LineStyle(JSONObject o) {
		color = Color.parseColor(o.optString("color"));
		acronym = o.optString("acronym");
		key = o.optString("key");
	}
	
}
