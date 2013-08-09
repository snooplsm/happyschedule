package us.wmwm.happyschedule.model;

import java.io.Serializable;

public class ConnectionInterval implements Serializable {

	public String tripId;
	public String sourceId;
	public String targetId;
	public String departure;
	public String arrival;
	public String serviceId;
	public String blockId;
	public int sequence;
	public String routeId;
}
