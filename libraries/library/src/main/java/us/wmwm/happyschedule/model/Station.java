package us.wmwm.happyschedule.model;

import java.io.Serializable;

import org.json.JSONObject;

import android.database.Cursor;
import android.text.TextUtils;

public class Station implements Serializable {

	private static final long serialVersionUID = 1L;

	String id;
	
	String name;
	
	String departureVision;
	
	String alternateId;
	
	String lat;
	
	String lng;

    String shortName;
	
	public Station(Cursor c) {
		id = c.getString(0);
		name = c.getString(2);
        shortName = c.getString(3);
		departureVision = c.getString(4);
		alternateId = c.getString(5);
		lat = c.getString(6);
		lng = c.getString(7);
	}
	
	public Station(JSONObject o ) {
		id = o.optString("id");
		name = o.optString("name");
        shortName = o.optString("short_name");
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

    public String getShortName() {
        if(!TextUtils.isEmpty(shortName)) {
            return shortName;
        }
        return getName();
    }

    public String getDepartureVision() {
		return departureVision;
	}
	
	public String getAlternateId() {
		return alternateId;
	}
	
}
