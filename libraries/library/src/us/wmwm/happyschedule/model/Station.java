package us.wmwm.happyschedule.model;

import java.io.Serializable;

import org.json.JSONObject;

import android.database.Cursor;

public class Station implements Serializable {

	private static final long serialVersionUID = 1L;

	String id;
	
	String name;
	
	String departureVision;
	
	String alternateId;
	
	String lat;
	
	String lng;
	
	
	public Station(Cursor c) {
		id = c.getString(0);
		name = c.getString(2);
		departureVision = c.getString(3);
		alternateId = c.getString(4);
		lat = c.getString(5);
		lng = c.getString(6);
	}
	
	public Station(JSONObject o ) {
		id = o.optString("id");
		name = o.optString("name");
		departureVision = o.optString("dv");
		alternateId = o.optString("alternateId");
		lat = o.optString("lat");
		lng = o.optString("lng");
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public Station() {
		
	}

	public String getId() {
		return id;
	}

	public String getLat() {
		return lat;
	}

	public String getLng() {
		return lng;
	}

	public String getName() {
		return name;
	}

	public String getDepartureVision() {
		return departureVision;
	}
	
	public String getAlternateId() {
		return alternateId;
	}
	
}
