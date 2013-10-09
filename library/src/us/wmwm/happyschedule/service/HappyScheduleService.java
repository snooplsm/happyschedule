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

import org.json.JSONObject;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.AlarmActivity;
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
	
	static SimpleDateFormat RFC;
	
	static {
		 RFC = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		 RFC.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private static final String TAG = HappyScheduleService.class.getSimpleName();
	
	@Override
	public void onCreate() {
		super.onCreate();
		notifs = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = null;
		Intent i = new Intent(this, HappyScheduleService.class);
		i.setData(Uri.parse("http://wmwm.us?type=lines"));
		pi = PendingIntent.getService(this, 0, i, 0);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+10000, 86400000,pi);
		ThreadHelper.getScheduler().submit(updateGCM);
	}
	
	Runnable updateGCM = new Runnable() {
		@Override
		public void run() {
			String version = SettingsFragment.getRegistrationId();
			if(!TextUtils.isEmpty(version)) {
				return;
			}
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(HappyScheduleService.this);
			try {
				String id = gcm.register(getString(R.string.sender_id));
				Log.d(HappyScheduleService.class.getSimpleName(), "GCM IS: " + id);
				OkHttpClient client = new OkHttpClient();
				URL u = new URL(getString(R.string.register_push_url)+"?push_id="+id+"&os=android&v="+SettingsFragment.getAppVersion()+"&m="+getString(R.string.market)+"&p="+getPackageName());
				HttpURLConnection conn = client.open(u);
				if(conn.getResponseCode()==200) {
					SettingsFragment.saveRegistrationId(id);
				}
				conn.disconnect();
				
			} catch (IOException e) {
				Log.d(TAG, "can't register", e);
			}
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	Future<?> linesFuture;
	
	Future<?> pushFuture;
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(linesFuture!=null) {
			linesFuture.cancel(true);
		}
		if(pushFuture!=null) {
			pushFuture.cancel(true);
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
								return;
							}
							String data = WDb.get().getPreference("rail_push_matrix");
							if(data==null) {
								FlurryAgent.logEvent("NoPushMatrix");
								alarmManager.cancel(pi);
								return;
							}
							String pushId = SettingsFragment.getRegistrationId();
							if(pushId==null) {
								FlurryAgent.logEvent("NoRegistrationIdOnSavePushMatrix");
								alarmManager.cancel(pi);
								return;
							}
							OkHttpClient client = new OkHttpClient();
							HttpURLConnection conn = null;
							try {
								conn = client.open(new URL(getString(R.string.register_service_url)+"?push_id="+pushId));
								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.setRequestMethod("POST");
								conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
								conn.setRequestProperty("Content-Length",String.valueOf(("services="+URLEncoder.encode(data,"utf-8")).getBytes().length));
								OutputStream out = conn.getOutputStream();
								out.write(("services="+URLEncoder.encode(data,"utf-8")).getBytes());
								out.close();
								int resp = conn.getResponseCode();
								if(resp==200) {
									WDb.get().savePreference("rail_push_matrix_needs_save", null);
									alarmManager.cancel(pi);
									FlurryAgent.logEvent("SavedPushMatrix");
								} else {
									FlurryAgent.logEvent("FailedSavePushMatrix");
								}
							} catch (Exception e) {
								
							} finally {
								if(conn!=null) {
									conn.disconnect();
								}
							}
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
							Log.d(TAG, "UpdateScheduleThread");
							OkHttpClient client = new OkHttpClient();
							HttpURLConnection conn = null;
							InputStream in = null;
							File config = getFileStreamPath("config.json");
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
								Calendar c = null;
								if(config.exists()) {
									Log.d(TAG, "UpdateScheduleThread - config exists");
									c = Calendar.getInstance();
									String lastModded = WDb.get().getPreference("config_last_modified");
									if(lastModded!=null) {
										try {											
											c.setTimeInMillis(Long.parseLong(lastModded));
											Log.d(TAG, "UpdateScheduleThread - time set to " + c.getTime());
										} catch (Exception e) {
											Log.e(TAG, "UpdateScheduleThreadException",e);
										}
									} else {
										c.setTimeInMillis(0);
										Log.d(TAG, "UpdateScheduleThread - time set to " + c.getTime());
									}
									Calendar later = (Calendar) c.clone();
									later.add(Calendar.HOUR_OF_DAY, 24);
									if(!Calendar.getInstance().after(later)) {
										Log.d(TAG, "UpdateScheduleThread - returning since " + Calendar.getInstance().getTime()+ "  ! after " + later.getTime());
										return;
									}									
								}
								
								conn = client.open(new URL(getString(R.string.config_url)+"?v="+URLEncoder.encode(version,"utf-8")));
								if(c!=null) {								
									Log.d(TAG, "UpdateScheduleThread - adding If-Modified-Since " + RFC.format(c.getTime()));
									conn.addRequestProperty("If-Modified-Since", RFC.format(c.getTime()));
								}
								if(conn.getResponseCode()==200) {
									Log.d(TAG, "UpdateScheduleThread - 200 response ");
									String txt = Streams.readFully(in = conn.getInputStream());
									JSONObject t = new JSONObject(txt);
									if(txt!=null) {
										fos = openFileOutput("config.json", Context.MODE_PRIVATE);
										fos.write(txt.getBytes());
										Log.d(TAG, "UpdateScheduleThread - saved config");
										FlurryAgent.logEvent("ConfigUpdated", Collections.singletonMap("date", new Date().toString()));
										WDb.get().savePreference("config_last_modified", String.valueOf(System.currentTimeMillis()));
									}
								} else {
									FlurryAgent.logEvent("ConfigUpToDate", Collections.singletonMap("date", new Date().toString()));
									//Streams.readFully(in = conn.getEr);
									Log.d(TAG, "UpdateScheduleThread - config up to date with response code " + conn.getResponseCode());
								}
							} catch (Exception e) {
								Log.e(TAG, "could not get config", e);
							} finally {
								if(conn!=null) {
									conn.disconnect();
								}
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
