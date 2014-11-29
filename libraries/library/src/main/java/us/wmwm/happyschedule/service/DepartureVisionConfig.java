package us.wmwm.happyschedule.service;

import org.json.JSONObject;

public class DepartureVisionConfig {

	public String url;
	
	public DepartureVisionConfig(JSONObject o) {
		url = o.optString("url","http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=$stop_id");
	}
}
