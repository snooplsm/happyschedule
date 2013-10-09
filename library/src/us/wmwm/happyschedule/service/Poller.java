package us.wmwm.happyschedule.service;

import java.io.IOException;
import java.util.List;

import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.TrainStatus;

public interface Poller {

	List<TrainStatus> getTrainStatuses(AppConfig config, String station, String stationB) throws IOException;
	
	boolean isArrivalStationRequired();
}
