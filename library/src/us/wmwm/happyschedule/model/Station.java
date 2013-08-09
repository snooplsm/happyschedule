package us.wmwm.happyschedule.model;

import java.io.Serializable;

import org.json.JSONObject;

import android.database.Cursor;

public class Station implements Serializable {

	private static final long serialVersionUID = 1L;

	String id;
	
	String name;
	
	String departureVision;
	
	public Station(Cursor c) {
		id = c.getString(0);
		name = c.getString(2);
		departureVision = c.getString(3);
	}
	
	public Station(JSONObject o ) {
		id = o.optString("id");
		name = o.optString("name");
		departureVision = o.optString("dv");
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

	public String getName() {
		return name;
	}

	public String getDepartureVision() {
		return departureVision;
	}
	
	
	
}
