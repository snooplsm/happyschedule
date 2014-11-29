package us.wmwm.happyschedule.application;

import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.AlarmActivity;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.service.HappyScheduleService;
import us.wmwm.happyschedule.service.Poller;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.amazon.device.ads.AdRegistration;
import com.flurry.android.FlurryAgent;

public class HappyApplication extends Application {

	static HappyApplication INSTANCE;
	
	@Override
	public void onCreate() {
		INSTANCE = this;
		
		super.onCreate();
		FlurryAgent.setReportLocation(false);
		FlurryAgent.setCaptureUncaughtExceptions(false);
		try {
			AdRegistration.setAppKey(getString(R.string.amazon_app_key));
		} catch (Exception e) {
			
		}
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
		startService(new Intent(this,HappyScheduleService.class));
//        PendingIntent pi = PendingIntent.getActivity(this,0,new Intent(this, AlarmActivity.class),0);
//        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000,pi);
    }
	
	public static HappyApplication get() {
		return INSTANCE;
	}

}
