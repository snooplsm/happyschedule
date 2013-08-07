package us.wmwm.happyschedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Alarms {

	private static final String KEY_ALARMS = "alarms";

	public static void saveAlarm(Context ctx, Alarm alarm) {
		Editor e = prefs(ctx).edit();
		e.putString(alarm.getId(), alarm.toJSON())
				.commit();
	}
	
	private static SharedPreferences prefs(Context ctx) {
		return ctx.getSharedPreferences(KEY_ALARMS,
				Context.MODE_PRIVATE);
	}
	
	public static void removeAlarm(Context ctx, Alarm alarm) {
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
				
			}
		}
		return alarms;

	}

}
