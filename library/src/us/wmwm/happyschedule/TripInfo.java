package us.wmwm.happyschedule;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public class TripInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public List<Stop> stops;	
	public String routeId;
	
	public static class Stop {
		public String name;
		public String id;
		public Calendar arrive;
		public Calendar depart;
		
		@Override
		public String toString() {
			return name;
		}
	}
	
}


