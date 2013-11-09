package us.wmwm.happytap.stream;

public class Service {

	String screenname;
	int day;
	int hour;
	long created;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + day;
		result = prime * result + hour;
		result = prime * result
				+ ((screenname == null) ? 0 : screenname.hashCode());
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
		Service other = (Service) obj;
		if (day != other.day)
			return false;
		if (hour != other.hour)
			return false;
		if (screenname == null) {
			if (other.screenname != null)
				return false;
		} else if (!screenname.equals(other.screenname))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Service [screenname=" + screenname + ", day=" + day + ", hour="
				+ hour + "]";
	}
	
}
