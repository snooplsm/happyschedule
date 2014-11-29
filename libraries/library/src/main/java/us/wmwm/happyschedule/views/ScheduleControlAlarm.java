package us.wmwm.happyschedule.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.views.ScheduleControlsView.ScheduleControlListener;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScheduleControlAlarm extends RelativeLayout {

	TextView alarmV;
	View dismiss;
	Alarm alarm;
	ScheduleControlListener listener;
	
	public void setListener(ScheduleControlListener listener) {
		this.listener = listener;
	}
	
	public ScheduleControlAlarm(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_alarm, this);
		alarmV = (TextView) findViewById(R.id.alarm);
		dismiss = findViewById(R.id.dismiss);
		dismiss.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onTimerCancel(alarm);
			}
		});
	}
	
	public void setData(Alarm alarm) {
		this.alarm = alarm;
		StringBuilder text = new StringBuilder(alarm.getType().name().toLowerCase()).append(" alarm ");
		if(!DateUtils.isToday(alarm.getTime().getTimeInMillis())) {
			text.append(new SimpleDateFormat("MMM d").format(alarm.getTime().getTime())).append(" at ");
		}
		text.append("for ");
		text.append(DateFormat.getTimeInstance(DateFormat.SHORT).format(alarm.getTime().getTime()).toLowerCase());
		alarmV.setText(text.toString());
	}
	
	public Alarm getAlarm() {
		return alarm;
	}

}
