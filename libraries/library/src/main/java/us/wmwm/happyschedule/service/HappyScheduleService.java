package us.wmwm.happyschedule.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.AlarmActivity;
import us.wmwm.happyschedule.api.Api;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.fragment.SettingsFragment;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.util.Streams;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.OkHttpClient;

public class HappyScheduleService extends Service {

	NotificationManager notifs;
	AlarmManager alarmManager;
	
	private static final String TAG = HappyScheduleService.class.getSimpleName();

    Api api;

	@Override
	public void onCreate() {
		super.onCreate();
        api = new Api(this);
		notifs = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = null;
		Intent i = new Intent(this, HappyScheduleService.class);
		i.setData(Uri.parse("http://wmwm.us?type=lines"));
		pi = PendingIntent.getService(this, 0, i, 0);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+10000, 86400000,pi);

		updateGCMFuture = ThreadHelper.getScheduler().schedule(updateGCM,10000, TimeUnit.MILLISECONDS);
	}
	
	Runnable updateGCM = new Runnable() {
		@Override
		public void run() {
            api.updateGcm();;
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	Future<?> linesFuture;
	
	Future<?> pushFuture;

    Future<?> updateGCMFuture;
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(linesFuture!=null) {
			linesFuture.cancel(true);
		}
		if(pushFuture!=null) {
			pushFuture.cancel(true);
		}
        if(updateGCMFuture!=null) {
            updateGCMFuture.cancel(true);
        }
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
							Intent i = AlarmActivity.from(this, alarm.getStationToStation(), alarm.getTime(), alarm.getType(), alarm.getId());
							PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
							alarmManager.cancel(pi);
							Alarms.removeAlarm(this, alarm);
						}						
					}
					stopSelf();
				}
				if("push".equals(type)) {
					if(pushFuture!=null) {
						pushFuture.cancel(true);						
					}
					Intent i = new Intent(getApplicationContext(), HappyScheduleService.class);
					i.setData(Uri.parse("http://wmwm.us?type=push"));
					final PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);
					alarmManager.cancel(pi);
					alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+18000000, pi);
					pushFuture = ThreadHelper.getScheduler().submit(new Runnable() {
						@Override
						public void run() {
							String needsSave = WDb.get().getPreference("rail_push_matrix_needs_save");
							if(needsSave==null) {
								alarmManager.cancel(pi);
								stopSelf();
								return;
							}
							String data = WDb.get().getPreference("rail_push_matrix");
							if(data==null) {
								FlurryAgent.logEvent("NoPushMatrix");
								alarmManager.cancel(pi);
								stopSelf();
								return;
							}
							String pushId = SettingsFragment.getRegistrationId();
							if(pushId==null) {
								FlurryAgent.logEvent("NoRegistrationIdOnSavePushMatrix");
								alarmManager.cancel(pi);
								stopSelf();
								return;
							}


							try {
                                int resp = api.registerService(data,pushId);
								if(resp==200) {
									WDb.get().savePreference("rail_push_matrix_needs_save", null);
									alarmManager.cancel(pi);
									FlurryAgent.logEvent("SavedPushMatrix");
								} else {
									FlurryAgent.logEvent("FailedSavePushMatrix");
								}
							} catch (Exception e) {
                                Log.e(TAG,"Error doing somethign",e);
                                FlurryAgent.logEvent("FailedSavePushMatrix");
							}
							stopSelf();
						}
					});
				}
				if("lines".equals(type)) {
					if(linesFuture!=null) {
						linesFuture.cancel(true);
					}
					linesFuture = ThreadHelper.getScheduler().submit(new Runnable() {
						@Override
						public void run() {
							api.updateLines();
							stopSelf();
						}
					});
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

}
