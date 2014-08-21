package us.wmwm.happyschedule.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.Type;
import us.wmwm.happyschedule.views.ScheduleView;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class AlarmActivity extends HappyActivity {

	Integer mVolumeBehavior;
	
	ScheduleView scheduleView;
	
	View dismiss;
	
	NotificationManager notifs;
	
	Vibrator vibrator;

	MediaPlayer mediaPlayer;

	AudioManager audioManager;
	
	private void cleanup() {
		if(mediaPlayer!=null) {
			mediaPlayer.stop();
		}
		vibrator.cancel();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		cleanup();
	}
	
	Alarm alarm;
	
	@Override
	protected Map<String, String> getLoggingParameters() {
		Map<String,String> params = new HashMap<String,String>();
		if(alarm!=null && alarm.getStationToStation()!=null) {
			try {
				StationToStation sts = alarm.getStationToStation();
				params.put("from_id", sts.departId);
				params.put("to_id", sts.arriveId);
				Station from = Db.get().getStop(sts.departId);
		        Station to = Db.get().getStop(sts.arriveId);
		        params.put("from_name", from.getName());
		        params.put("to_name", to.getName());
		        params.put("trip", sts.tripId);
		        params.put("time", alarm.getTime().getTime().toString());
			} catch (Exception e) {
				
			}
		}
		return params;
	}
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		notifs = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//		final String vol =
//                PreferenceManager.getDefaultSharedPreferences(this)
//                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
//                        SettingsActivity.DEFAULT_VOLUME_BEHAVIOR);
//		
//		mVolumeBehavior = Integer.parseInt(vol);

		final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_alarm);
        scheduleView = (ScheduleView) findViewById(R.id.schedule_view);
        dismiss = findViewById(R.id.dismiss);
        alarm = (Alarm) getIntent().getSerializableExtra("alarm");
        dismiss.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss.setVisibility(View.GONE);
				cleanup();
				startService(Alarms.newDismissIntent(v.getContext(), alarm));
			}
		});        
        getSupportActionBar().setTitle(getString(R.string.activity_alarm_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Alarms.removeAlarm(this, alarm);
        notifs.cancel(alarm.getId().hashCode());
        StationToStation sts = alarm.getStationToStation();
        
        Station from = Db.get().getStop(sts.departId);
        Station to = Db.get().getStop(sts.arriveId);
        scheduleView.setData(sts,from,to);
        getSupportActionBar().setSubtitle(from.getName() + " to " + to.getName());
        if(!TextUtils.isEmpty(from.getDepartureVision())) {
        	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_depareturevision, FragmentDepartureVision.newInstance(new DepartureVision(from.getId(),to==null?null:to.getId()),0,to,sts,false)).commit();
        }
        vibrator.vibrate(new long[] {0,200,500},0);
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			// alert is null, using backup
			alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) { // I can't see this ever being null (as always
									// have a default notification) but just
									// incase
				// alert backup is null, using 2nd backup
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(this, alert);

			final float vol;
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) == 0) {
				vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)/2.0f;
				System.out.println("vols: " + vol);
			} else {
				vol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
				System.out.println("vole: " + vol);
			}
			setVolumeControlStream(AudioManager.STREAM_ALARM);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.setVolume(vol, vol);
			mediaPlayer.setLooping(true);
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (Exception e) {
			//throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static Intent from(Context ctx, StationToStation stationToStation, Calendar time, Type type, String id) {
		Alarm alarm = new Alarm();
		alarm.setId(id);
		alarm.setStationToStation(stationToStation);
		alarm.setType(type);
		alarm.setTime(time);
		Intent intent = new Intent(ctx, AlarmActivity.class);
		intent.putExtra("alarm", alarm);
		return intent;
	}
	
}
