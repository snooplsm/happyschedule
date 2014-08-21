package us.wmwm.happyschedule.model;


public interface ScheduleTraverser {
	void populateItem(int index, StationToStation stationToStation, int total);
}