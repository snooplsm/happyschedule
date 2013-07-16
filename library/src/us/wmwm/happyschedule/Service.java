package us.wmwm.happyschedule;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Service implements Serializable {

	private static final long serialVersionUID = 1L;

	public String serviceId;
	public Set<Date> dates = new HashSet<Date>();

	@Override
	public String toString() {
		return "Service [" + (dates != null ? "dates=" + dates + ", " : "")
				+ (serviceId != null ? "serviceId=" + serviceId : "") + "]";
	}
}
