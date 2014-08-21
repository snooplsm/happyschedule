package us.wmwm.happyschedule.model;

import java.io.Serializable;

import org.json.JSONObject;

public class DepartureVision implements Serializable {

	private static final long serialVersionUID = 1L;

	String from;
	String to;

	JSONObject object;
	
	long updated;

	public DepartureVision(JSONObject o) {
		from = o.optString("from");
		to = o.optString("to");
		updated = o.optLong("updated",System.currentTimeMillis());
	}

	public DepartureVision() {
	}

	public DepartureVision(String from, String to) {
		this.from = from;
		this.to = to;
		updated = System.currentTimeMillis();
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public JSONObject getObject() {	
		JSONObject o = new JSONObject();
		try {
			o.put("from", from);
			o.put("to", to);
			o.put("updated", updated);
		} catch (Exception e) {
		}
		return o;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + (int) (updated ^ (updated >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DepartureVision other = (DepartureVision) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		if (updated != other.updated)
			return false;
		return true;
	}


	
}
