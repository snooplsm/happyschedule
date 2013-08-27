package us.wmwm.happyschedule.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Future;

import org.json.JSONObject;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.AlarmActivity;
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

import com.squareup.okhttp.OkHttpClient;

public class HappyScheduleService extends Service {

	NotificationManager notifs;
	AlarmManager alarmManager;
	
	SimpleDateFormat RFC = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	
	@Override
	public void onCreate() {
		super.onCreate();
		notifs = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = null;
		Intent i = new Intent(this, HappyScheduleService.class);
		i.setData(Uri.parse("http://wmwm.us?type=lines"));
		pi = PendingIntent.getService(this, 0, i, 0);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+10000, 43200000,pi);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	Future<?> linesFuture;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		linesFuture.cancel(true);
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
					}
				}
				if("lines".equals(type)) {
					if(linesFuture!=null) {
						linesFuture.cancel(true);
					}
					linesFuture = ThreadHelper.getScheduler().submit(new Runnable() {
						@Override
						public void run() {
							OkHttpClient client = new OkHttpClient();
							HttpURLConnection conn = null;
							InputStream in = null;
							File config = new File(getApplication().getFilesDir(),"config.json");
							FileOutputStream fos = null;
							try {
								String version = "";
								try {
									PackageManager pm = getPackageManager();
									PackageInfo pinfo = pm.getPackageInfo(getPackageName(), 0);
									version = pinfo.versionName;
									version = URLEncoder.encode(version,"utf-8");
								} catch (Exception e) {
									
								}
								conn = client.open(new URL("http://ryangravener.com/njrails/config.json?appVersion="+version));
								if(config.exists()) {
									Calendar c = Calendar.getInstance();
									c.setTimeInMillis(config.lastModified());
									Calendar later = (Calendar) c.clone();
									later.add(Calendar.HOUR_OF_DAY, 3);
									if(!Calendar.getInstance().after(later)) {
										return;
									}
									conn.addRequestProperty("If-Modified-Since", RFC.format(c.getTime()));
								}
								if(conn.getResponseCode()==200) {
									String txt = Streams.readFully(in = conn.getInputStream());
									JSONObject t = new JSONObject(txt);
									if(txt!=null) {
										fos = openFileOutput("config.json", Context.MODE_PRIVATE);
										fos.write(txt.getBytes());									
									}
								} else {
									Streams.readFully(in = conn.getInputStream());
								}
							} catch (Exception e) {
								
							} finally {
								conn.disconnect();
								if(in!=null) {
									try {
										in.close();
									} catch (Exception ex) {}
								}
								if(fos!=null) {
									try {
										fos.close();
									} catch (Exception e) {
										
									}
								}
							}
						}
					});
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

}
