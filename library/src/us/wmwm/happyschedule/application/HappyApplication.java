package us.wmwm.happyschedule.application;

import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.model.Alarm;

import android.app.Application;

public class HappyApplication extends Application {

	static HappyApplication INSTANCE;
	
	@Override
	public void onCreate() {
		INSTANCE = this;
		super.onCreate();
		ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				List<Alarm> alarms = Alarms.getAlarms(INSTANCE);
				for(Alarm alarm : alarms) {
					if(Calendar.getInstance().before(alarm.getTime())) {
						Alarms.startAlarm(INSTANCE, alarm);
					} else {
						Alarms.removeAlarm(INSTANCE, alarm);
					}
				}
			}
		});
	}
	
	public static HappyApplication get() {
		return INSTANCE;
	}
	
}
