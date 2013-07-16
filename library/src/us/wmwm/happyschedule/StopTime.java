package us.wmwm.happyschedule;

import java.io.Serializable;
import java.util.Date;

public class StopTime implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String tripId;
	public String sourceId;
	public String targetId;
	public Date departure;
	public Date arrival;
	public String serviceId;
	
}