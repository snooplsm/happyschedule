package us.wmwm.happyschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class ScheduleControlsView extends LinearLayout {

	
	public interface ScheduleControlListener {
		void onTimer();
		void onTrips();
		void onPin();
		void onFavorite();
	}
	
	View pin;
	View trips;
	View addTimer;
	
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
	}

	
	
}
