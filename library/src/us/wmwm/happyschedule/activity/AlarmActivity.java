package us.wmwm.happyschedule.activity;

import java.util.Calendar;
import java.util.UUID;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.Type;
import us.wmwm.happyschedule.views.ScheduleView;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		notifs = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        dismiss.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss.setVisibility(View.GONE);
			}
		});        
        getActionBar().setTitle(getString(R.string.activity_alarm_title));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Alarm alarm = (Alarm) getIntent().getSerializableExtra("alarm");
        Alarms.removeAlarm(this, alarm);
        notifs.cancel(alarm.getId().hashCode());
        StationToStation sts = alarm.getStationToStation();
        
        Station from = Db.get().getStop(sts.departId);
        Station to = Db.get().getStop(sts.arriveId);
        scheduleView.setData(sts,from,to);
        getActionBar().setSubtitle(from.getName() + " to " + to.getName());
        if(!TextUtils.isEmpty(from.getDepartureVision())) {
        	getSupportFragmentManager().beginTransaction().replace(R.id.fragment_depareturevision, FragmentDepartureVision.newInstance(from,sts,false)).commit();
        }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static Intent from(Context ctx, StationToStation stationToStation, Calendar time, Type type) {
		Alarm alarm = new Alarm();
		alarm.setId(UUID.randomUUID().toString());
		alarm.setStationToStation(stationToStation);
		alarm.setType(type);
		alarm.setTime(time);
		Intent intent = new Intent(ctx, AlarmActivity.class);
		intent.putExtra("alarm", alarm);
		return intent;
	}
	
}
