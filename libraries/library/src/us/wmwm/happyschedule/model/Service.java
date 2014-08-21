package us.wmwm.happyschedule.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Service implements Serializable {

	private static final long serialVersionUID = 1L;

	public String serviceId;
	public Set<Date> dates = new HashSet<Date>();

	public Service() {
		
	}
	public Service(JSONObject o) {
		serviceId = o.optString("serviceId");
		JSONArray dates = o.optJSONArray("dates");
		if(dates!=null) {
			for(int i = 0; i < dates.length(); i++) {
				long day = dates.optLong(i);
				Date d = new Date(day);
				this.dates.add(d);
			}
		}
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("serviceId", serviceId);
			JSONArray dates = new JSONArray();
			for(Date d : this.dates) {
				dates.put(d.getTime());
			}
			o.put("dates", dates);
		} catch (Exception e) {
			
		}
		return o;
	}
	
	@Override
	public String toString() {
		return "Service [" + (dates != null ? "dates=" + dates + ", " : "")
				+ (serviceId != null ? "serviceId=" + serviceId : "") + "]";
	}
}
