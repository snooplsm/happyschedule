package us.wmwm.happyschedule.service;

import org.json.JSONObject;

public class DepartureVision {

	public String url;
	
	public DepartureVision(JSONObject o) {
		url = o.optString("url","http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=$stop_id");
	}
}
