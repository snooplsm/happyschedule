package us.wmwm.happyschedule.dao;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.fragment.FragmentLoad;
import us.wmwm.happyschedule.model.Station;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Db {

	SQLiteDatabase db;

	static Db INSTANCE;

	public static Db get() {
		if (INSTANCE == null) {
			INSTANCE = new Db();
			INSTANCE.db = SQLiteDatabase.openDatabase(
					FragmentLoad.getFile(HappyApplication.get(),"database.db")
							.getAbsolutePath(), null,
					SQLiteDatabase.OPEN_READONLY|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		}
		return INSTANCE;
	}

	private String getNameQuery() {
		String nameQuery = "name";
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get());
		if(p.getBoolean(HappyApplication.get().getString(R.string.settings_key_debug_names), false)) {
			nameQuery = "name|| ' ('||stop_id||')'";
		}
		return nameQuery;
	}

    public Cursor getStops(boolean departureVisionOnly) {
        return getStops(departureVisionOnly,null);
    }
	public Cursor getStops(boolean departureVisionOnly, String key) {
        String where = "";
        if(!TextUtils.isEmpty(key)) {
            where = "and name like '%" + key + "%'";
        }
		if(departureVisionOnly) {
			return db
					.rawQuery(
							"select stop_id as _id, stop_id, name|| ' ('||departure_vision||')', departure_vision, alternate_id, lat, lon from stop where departure_vision is not null and 1=1 " + where + " order by name asc",
							null);
		} else {
		
		return db
				.rawQuery(
						"select stop_id as _id, stop_id, " + getNameQuery() + ", departure_vision, alternate_id, lat, lon from stop where 1=1 " + where + " order by name asc",
						null);
		}
	}

	public Station getStop(String id) {
		if(id==null) {
			return null;
		}
		Cursor c = db
				.rawQuery(
						"select stop_id as _id,  stop_id, " + getNameQuery() + ", departure_vision, alternate_id, lat, lon from stop where _id=?",
						new String[] { id });
		try {
			if (c.moveToNext()) {
				Station s = new Station(c);
				return s;
			}
		} finally {
			c.close();
		}
		return null;
	}

}
