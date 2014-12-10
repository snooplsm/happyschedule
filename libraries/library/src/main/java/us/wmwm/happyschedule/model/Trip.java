package us.wmwm.happyschedule.model;

import java.io.Serializable;

import org.json.JSONObject;

public class Trip implements Serializable {

	private static final long serialVersionUID = 1L;

	public String blockId;
	public String id;
    public String routeId;

	public Trip(JSONObject o) {
		blockId = o.optString("blockId");
		id = o.optString("id");
        routeId = o.optString("routeId");
	}

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("blockId", blockId);
			o.put("id", id);
            o.put("routeId",routeId);
		} catch (Exception e) {

		}
		return o;
	}

	public Trip() {
	}
}