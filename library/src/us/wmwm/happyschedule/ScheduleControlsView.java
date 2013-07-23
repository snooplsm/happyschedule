package us.wmwm.happyschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class ScheduleControlsView extends LinearLayout {

	public ScheduleControlsView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_schedule_controls, this);
	}

	
	
}
