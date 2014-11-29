package us.wmwm.happyschedule.model;

import java.io.Serializable;
import java.util.Calendar;

import org.json.JSONObject;


public class Alarm implements Serializable {

	private static final long serialVersionUID = 1L;

	private Type type;
	
	private Calendar time;
	
	private StationToStation stationToStation;
	
	private String id;
	
	public Alarm() {		
	}
	
	public Alarm(JSONObject o) {
		type = Type.valueOf(o.optString("type"));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(o.optLong("time"));
		id = o.optString("id");
		time = c;
		stationToStation = new StationToStation(o.optJSONObject("stationToStation"));		
	}
	
	public String toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("type", type.name());
			o.put("id", id);
			o.put("time", time.getTimeInMillis());
			o.put("stationToStation", new JSONObject(stationToStation.toJSON()));
		} catch (Exception e) {
			
		}
		return o.toString();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Calendar getTime() {
		return time;
	}

	public void setTime(Calendar time) {
		this.time = time;
	}

	public StationToStation getStationToStation() {
		return stationToStation;
	}

	public void setStationToStation(StationToStation stationToStation) {
        try {
            if(stationToStation instanceof StationInterval) {
                StationInterval si = (StationInterval)stationToStation;
                this.stationToStation = new StationInterval(new JSONObject(si.toJSON()));
            } else {
                this.stationToStation = new StationToStation(new JSONObject(stationToStation.toJSON()));
            }
        } catch (Exception e) {

        }
	}

	@Override
	public String toString() {
		return "Alarm [type=" + type + ", time=" + time + ", stationToStation="
				+ stationToStation + ", id=" + id + "]";
	}
	
}
