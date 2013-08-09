package us.wmwm.happyschedule.service;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.activity.AlarmActivity;
import us.wmwm.happyschedule.model.Alarm;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class HappyScheduleService extends Service {

	NotificationManager notifs;
	AlarmManager alarmManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		notifs = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent!=null) {
			Uri data = intent.getData();
			if(data!=null) {
				String type = data.getQueryParameter("type");
				if("alarm".equals(type)) {
					String id = data.getQueryParameter("id");
					Alarm alarm = Alarms.getAlarm(this,id);
					String action = data.getQueryParameter("action");
					if("dismiss".equals(action)) {
						notifs.cancel(id.hashCode());
						if(alarm!=null) {
							Intent i = AlarmActivity.from(this, alarm.getStationToStation(), alarm.getTime(), alarm.getType());
							PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
							alarmManager.cancel(pi);
							Alarms.removeAlarm(this, alarm);
						}
						System.out.println("notif cancel: " + id);
						
					}
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

}
