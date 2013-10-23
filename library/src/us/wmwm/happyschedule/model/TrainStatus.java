package us.wmwm.happyschedule.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class TrainStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	private String departs;
	private String train;
	private String dest;
	private String line;
	private String track;
	private String status;
	private String arrives;

	public TrainStatus(JSONObject o) {
		departs = o.optString("departs");
		train = o.optString("train");
		dest = o.optString("dest");
		setLine(o.optString("line"));
		track = o.optString("track");
		status = o.optString("status");
		arrives = o.optString("arrives");
	}
	
	public void setArrives(String arrives) {
		this.arrives = arrives;
	}
	
	public String getArrives() {
		return arrives;
	}

	public TrainStatus() {
	}

	public String getDeparts() {
		return departs;
	}

	public void setDeparts(String departs) {
		this.departs = departs;
	}

	public String getTrain() {
		return train;
	}

	public void setTrain(String train) {
		this.train = train;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		if (line != null) {
			this.line = line.trim();
		}
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) { 
		try {
			this.track = track.replaceAll("-", "").trim();
		} catch (Exception e) {
			this.track = track;
		}
	}

	public String getStatus() {
		if(status==null) {
			return "";
		}
		return status;
	}

	public void setStatus(String status) {
		try {
			this.status = status.replaceAll("-", "").trim();
		} catch (Exception e) {
			this.status = status;
		}
		if(TextUtils.isEmpty(this.status)) {
			this.status = null;
		}
	}

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("line", line);
			o.put("track", track);
			o.put("status", status);
			o.put("dest", dest);
			o.put("departs", departs);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	@Override
	public String toString() {
		return "TrainStatus [departs=" + departs + ", train=" + train
				+ ", dest=" + dest + ", line=" + line + ", track=" + track
				+ ", status=" + status + "]";
	}
	
	

}
