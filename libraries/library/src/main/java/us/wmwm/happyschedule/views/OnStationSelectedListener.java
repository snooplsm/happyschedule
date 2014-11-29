package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.model.Station;

public interface OnStationSelectedListener {

	public enum State {
		ADDED, CHANGED, REMOVED, NONE
	}
	void onStation(Station station, State state);
	
}
