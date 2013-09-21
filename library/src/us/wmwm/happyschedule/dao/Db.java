package us.wmwm.happyschedule.dao;

import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.fragment.FragmentLoad;
import us.wmwm.happyschedule.model.Station;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

	public Cursor getStops(boolean departureVisionOnly) {
		if(departureVisionOnly) {
			return db
					.rawQuery(
							"select stop_id as _id, stop_id, name|| ' ('||departure_vision||')', departure_vision, alternate_id, lat, lon from stop where departure_vision is not null order by name asc",
							null);
		} else {
		return db
				.rawQuery(
						"select stop_id as _id, stop_id, name, departure_vision, alternate_id, lat, lon from stop order by name asc",
						null);
		}
	}

	public Station getStop(String id) {
		Cursor c = db
				.rawQuery(
						"select stop_id as _id,  stop_id, name, departure_vision, alternate_id, lat, lon from stop where _id=?",
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
