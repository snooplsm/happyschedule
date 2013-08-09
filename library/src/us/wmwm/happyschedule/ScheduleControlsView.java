
package us.wmwm.happyschedule;

import java.util.List;

import us.wmwm.happyschedule.views.ScheduleControlAlarm;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class ScheduleControlsView extends LinearLayout {

	
	public interface ScheduleControlListener {
		void onTimer();
		void onTimerCancel(Alarm alarm);
		void onTrips();
		void onPin();
		void onFavorite();
	}
	
	View pin;
	View trips;
	View addTimer;
	
	LinearLayout alarms;
	
	ScheduleControlListener listener;
	
	public void setListener(ScheduleControlListener listener) {
		this.listener = listener;
	}
	
	OnClickListener onClick = new OnClickListener() {
		public void onClick(View v) {
			if(v==pin) {
				listener.onPin();
			}
			if(v==trips) {
				listener.onTrips();
			}
			if(v==addTimer) {
				listener.onTimer();
			}
		};
	};
	
	public ScheduleControlsView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_schedule_controls, this);
		pin = findViewById(R.id.pin);
		trips = findViewById(R.id.trips);
		addTimer = findViewById(R.id.add_alarm);
		pin.setOnClickListener(onClick);
		trips.setOnClickListener(onClick);
		addTimer.setOnClickListener(onClick);
		alarms = (LinearLayout) findViewById(R.id.alarms);
	}
	
	public void setData(List<Alarm> alarms) {
		if(alarms==null) {
			this.alarms.removeAllViews();
			return;
		}
		for(Alarm alarm : alarms) {
			ScheduleControlAlarm alarmView = new ScheduleControlAlarm(getContext());
			this.alarms.addView(alarmView);
			alarmView.setData(alarm);
		}
	}

	
	
}
