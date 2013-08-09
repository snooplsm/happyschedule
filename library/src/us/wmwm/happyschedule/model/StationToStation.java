package us.wmwm.happyschedule.model;

import java.io.Serializable;
import java.util.Calendar;

import org.json.JSONObject;

public class StationToStation implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public String departId;
		public String arriveId;
		public Calendar departTime;
		public Calendar arriveTime;		
		public String blockId;
		public String tripId;
		public String routeId;
		
		public StationToStation(){}
		
		public StationToStation(JSONObject o) {
			departId = o.optString("departId");
			arriveId = o.optString("arriveId");
			Calendar d = Calendar.getInstance();
			d.setTimeInMillis(o.optLong("departTime"));
			departTime = d;
			Calendar a = Calendar.getInstance();
			a.setTimeInMillis(o.optLong("arriveTime"));
			arriveTime = a;
			blockId = o.optString("blockId");
			tripId = o.optString("tripId");
			routeId = o.optString("routeId");
		}
		
		public String toJSON() {
			JSONObject o  = new JSONObject();
			try {
				o.put("departId", departId);
				o.put("arriveId", arriveId);
				o.put("departTime", departTime.getTimeInMillis());
				o.put("arriveTime", arriveTime.getTimeInMillis());
				o.put("blockId", blockId);
				o.put("tripId", tripId);
				o.put("routeId", routeId);
			} catch (Exception e) {}
			return o.toString();
		}
		
		public Calendar getDepartTime() {
			return departTime;
		}
		
		public Calendar getArriveTime() {
			return arriveTime;
		}
		
		public int getDuration() {
			if(arriveTime==null || departTime==null) {
				return 0;
			}
			return (int)(arriveTime.getTimeInMillis()-departTime.getTimeInMillis())/60000;
		}


		@Override
		public String toString() {
			return "StationToStation [departId=" + departId + ", arriveId="
					+ arriveId + ", departTime=" + departTime + ", arriveTime="
					+ arriveTime + ", blockId=" + blockId + ", tripId="
					+ tripId + ", routeId=" + routeId + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((arriveId == null) ? 0 : arriveId.hashCode());
			result = prime * result
					+ ((arriveTime == null) ? 0 : arriveTime.hashCode());
			result = prime * result
					+ ((blockId == null) ? 0 : blockId.hashCode());
			result = prime * result
					+ ((departId == null) ? 0 : departId.hashCode());
			result = prime * result
					+ ((departTime == null) ? 0 : departTime.hashCode());
			result = prime * result
					+ ((routeId == null) ? 0 : routeId.hashCode());
			result = prime * result
					+ ((tripId == null) ? 0 : tripId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			StationToStation other = (StationToStation) obj;
			if (arriveId == null) {
				if (other.arriveId != null) {
					return false;
				}
			} else if (!arriveId.equals(other.arriveId)) {
				return false;
			}
			if (arriveTime == null) {
				if (other.arriveTime != null) {
					return false;
				}
			} else if (!arriveTime.equals(other.arriveTime)) {
				return false;
			}
			if (blockId == null) {
				if (other.blockId != null) {
					return false;
				}
			} else if (!blockId.equals(other.blockId)) {
				return false;
			}
			if (departId == null) {
				if (other.departId != null) {
					return false;
				}
			} else if (!departId.equals(other.departId)) {
				return false;
			}
			if (departTime == null) {
				if (other.departTime != null) {
					return false;
				}
			} else if (!departTime.equals(other.departTime)) {
				return false;
			}
			if (routeId == null) {
				if (other.routeId != null) {
					return false;
				}
			} else if (!routeId.equals(other.routeId)) {
				return false;
			}
			if (tripId == null) {
				if (other.tripId != null) {
					return false;
				}
			} else if (!tripId.equals(other.tripId)) {
				return false;
			}
			return true;
		}

		
		
	}