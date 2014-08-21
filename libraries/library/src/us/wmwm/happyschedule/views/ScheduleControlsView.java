
package us.wmwm.happyschedule.views;

import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.service.FareType;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScheduleControlsView extends LinearLayout {

	
	public interface ScheduleControlListener {
		void onFavorite();
		void onPin();
		void onTimer();
		void onTimerCancel(Alarm alarm);
		void onTrips(Schedule schedule, StationToStation stationToStation);
		void onShare(Schedule schedule, StationToStation stationToStation);
	}
	
	View addTimer;
	LinearLayout alarms;
	ScheduleControlListener listener;
	
	OnClickListener onClick = new OnClickListener() {
		public void onClick(View v) {
			if(v==pin) {
				listener.onPin();
			}
			if(v==trips) {
				listener.onTrips(schedule, stationToStation);
			}
			if(v==addTimer) {
				listener.onTimer();
			}
			if(v==share) {
				listener.onShare(schedule,stationToStation);
			}
		};
	};
	
	View pin;
	
	Schedule schedule;
	StationToStation stationToStation;
	
	View trips;
	
	View share;
	
	TextView fareType;
	
	public ScheduleControlsView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_schedule_controls, this);
		pin = findViewById(R.id.pin);
		trips = findViewById(R.id.trips);
		addTimer = findViewById(R.id.add_alarm);
		pin.setOnClickListener(onClick);
		trips.setOnClickListener(onClick);
		addTimer.setOnClickListener(onClick);
		share = findViewById(R.id.share);
		share.setOnClickListener(onClick);
		alarms = (LinearLayout) findViewById(R.id.alarms);
		fareType = (TextView) findViewById(R.id.peak);
	}
	
	public void setData(List<Alarm> alarms, Schedule schedule, StationToStation sts,FareType fareType) {
		this.schedule = schedule;
		this.stationToStation = sts;
		if(alarms==null) {
			this.alarms.removeAllViews();
			return;
		}
		for(Alarm alarm : alarms) {
			ScheduleControlAlarm alarmView = new ScheduleControlAlarm(getContext());
			alarmView.setListener(listener);
			this.alarms.addView(alarmView);
			alarmView.setData(alarm);
		}
//		boolean notifs = WDb.get().hasNotification(sts.blockId);
//		if(notifs) {
//			//pin.setI
//		}
		if(fareType!=null) {
			
		}
	}
	
	public void setListener(ScheduleControlListener listener) {
		this.listener = listener;
	}

	
	
}
