package us.wmwm.happyschedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import us.wmwm.happyschedule.activity.AlarmActivity;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.fragment.FragmentAlarmPicker;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.service.HappyScheduleService;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

public class Alarms {

	private static final String KEY_ALARMS = "alarms";

	public static void saveAlarm(Context ctx, Alarm alarm) {
		System.out.println("save alarm " + alarm.getId());
		Editor e = prefs(ctx).edit();
		e.putString(alarm.getId(), alarm.toJSON())
				.commit();
	}
	
	private static SharedPreferences prefs(Context ctx) {
		return ctx.getSharedPreferences(KEY_ALARMS,
				Context.MODE_PRIVATE);
	}
	
	public static void removeAlarm(Context ctx, Alarm alarm) {
		System.out.println("remove alarm " + alarm.getId());
		prefs(ctx).edit().remove(alarm.getId()).commit();
	}
	
	public static Alarm getAlarm(Context ctx, String id) {
		String a = prefs(ctx).getString(id, null);
		if(a==null) {
			return null;
		} else {
			try {
				return new Alarm(new JSONObject(a));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static List<Alarm> getAlarms(Context ctx) {
		List<Alarm> alarms = new ArrayList<Alarm>();
		for(Map.Entry<String, ?> e : prefs(ctx).getAll().entrySet()) {
			try {
				Alarm alarm = new Alarm(new JSONObject((String)e.getValue()));
				alarms.add(alarm);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return alarms;
	}
	
	public static Intent newDismissIntent(Context ctx, Alarm alarm) {
		return new Intent(ctx, HappyScheduleService.class).putExtra("alarm", alarm).setData(Uri.parse("http://wmwm.us?type=alarm&action=dismiss&id="+alarm.getId()));
	}
	
	public static void startAlarm(Context ctx, Alarm alarm) {		
		AlarmManager alarmManger = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
		NotificationManager notifs = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		Alarms.saveAlarm(ctx, alarm);
		PendingIntent pi = PendingIntent.getActivity(ctx, 0, AlarmActivity.from(ctx, alarm.getStationToStation(), alarm.getTime(), alarm.getType(), alarm.getId()) , 0);
		PendingIntent dismiss = PendingIntent.getService(ctx, 0, Alarms.newDismissIntent(ctx, alarm), 0);
		NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);
		NotificationCompat.BigTextStyle bs = new NotificationCompat.BigTextStyle(b);
		
		bs.setBigContentTitle(ctx.getString(R.string.app_name) + " " + alarm.getType().name().toLowerCase() + " alarm");
		StringBuilder text = new StringBuilder(alarm.getType().name().toLowerCase());
		text.replace(0, 1, text.substring(0,1).toUpperCase());
		String typet = text.toString();
		text.append(" alarm set for ");
		if(!DateUtils.isToday(alarm.getTime().getTimeInMillis())) {
			text.append(new SimpleDateFormat("MMM d").format(alarm.getTime().getTime())).append(" at ");
		}
		StationToStation stationToStation = alarm.getStationToStation();
		text.append(DateFormat.getTimeInstance(DateFormat.SHORT).format(alarm.getTime().getTime()).toLowerCase());
		text.append(".  For train #" + stationToStation.blockId + " departing from " + Db.get().getStop(stationToStation.departId).getName() + " arriving at " + Db.get().getStop(stationToStation.arriveId).getName()+".");
		bs.bigText(text.toString());
		b.addAction(R.drawable.ic_action_cancel, ctx.getString(R.string.notif_alarm_dismiss), dismiss);
		b.setContentText(text.toString());
		b.setContentTitle(ctx.getString(R.string.app_name) + " " + alarm.getType().name().toLowerCase() + " alarm");
		//bs.setSummaryText(text.toString());
		b.setOngoing(true);
		b.setSmallIcon(R.drawable.stat_notify_alarm);
		Notification notif = b.build();
		notif.tickerText = typet + " alarm to go off in " + FragmentAlarmPicker.buildMessage(alarm.getTime(),Calendar.getInstance()).toString();
		notifs.notify(alarm.getId().hashCode(), notif);
		alarmManger.set(AlarmManager.RTC_WAKEUP, alarm.getTime().getTimeInMillis(), pi);
	}

}
