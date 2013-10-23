package us.wmwm.happyschedule.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.TrainStatus;

public interface Poller {

	List<TrainStatus> getTrainStatuses(AppConfig config, String station, String stationB) throws IOException;
	
	boolean isArrivalStationRequired();
	
	Map<String, FareType> getFareTypes(Map<String,StationInterval> inter);
	

}
