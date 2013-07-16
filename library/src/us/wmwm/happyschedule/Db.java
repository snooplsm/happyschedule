package us.wmwm.happyschedule;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Db {

	SQLiteDatabase db;

	static Db INSTANCE;

	public static Db get() {
		if (INSTANCE == null) {
			INSTANCE = new Db();
			INSTANCE.db = SQLiteDatabase.openDatabase(
					FragmentLoad.getFile(HappyApplication.get())
							.getAbsolutePath(), null,
					SQLiteDatabase.OPEN_READONLY|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		}
		return INSTANCE;
	}

	public Cursor getStops() {
		return db
				.rawQuery(
						"select stop_id as _id, stop_id, name, departure_vision from stop order by name asc",
						null);
	}

	public Station getStop(String id) {
		Cursor c = db
				.rawQuery(
						"select stop_id as _id,  stop_id, name, departure_vision from stop where _id=?",
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
