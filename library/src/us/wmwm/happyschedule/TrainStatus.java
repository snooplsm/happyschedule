package us.wmwm.happyschedule;

import java.io.Serializable;

public class TrainStatus implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String departs;
	private String train;
	private String dest;
	private String line;
	private String track;
	private String status;
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
		if(line!=null) {
			this.line = line.trim();
		}
	}
	public String getTrack() {
		return track;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
}
