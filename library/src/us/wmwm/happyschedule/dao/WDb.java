package us.wmwm.happyschedule.dao;

import java.util.List;

import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.model.Station;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WDb {

	public static final String REVERSE_BUTTON_MARGIN_LEFT_PERCENTAGE = "reverseButtonMarginLeftPercentage";

	SQLiteDatabase db;

	static WDb INSTANCE;

	public static WDb get() {
		if (INSTANCE == null) {
			INSTANCE = new WDb();
			INSTANCE.db = new OpenHelper("njrails.db").getWritableDatabase();
		}
		return INSTANCE;
	}

	public void saveHistory(Station from, Station to) {
		ContentValues cv = new ContentValues(3);
		cv.put("depart_id", from.getId());
		cv.put("arrive_id", to.getId());
		cv.put("time", System.currentTimeMillis());
		db.insert("history", null, cv);
	}

	public void savePreference(String key, String value) {
		ContentValues cv = new ContentValues(2);
		cv.put("key", key);
		cv.put("value", value);
		db.insertWithOnConflict("preference", null, cv,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	public String getPreference(String key) {
		Cursor c = db.rawQuery("select value from preference where key = ?",
				new String[] { key });
		try {
			if (c.moveToNext()) {
				return c.getString(0);
			}
		} finally {
			c.close();
		}
		return null;
	}

	public Cursor getHistory() {
		return db
				.rawQuery(
						"select depart_id,  arrive_id, time, time as _id from history order by time desc",
						null);
	}

	private static class OpenHelper extends SQLiteOpenHelper {
		public OpenHelper(String name) {
			super(HappyApplication.get(), name, null, 2);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists history(depart_id varchar(255), arrive_id varchar(255), time integer)");
			db.execSQL("create table if not exists preference(key varchar(255) unique, value text)");
			db.execSQL("create table if not exists notification(block_id varchar(100) unique, created integer)");
			db.execSQL("create table if not exists push_notification(created_at integer, created_str varchar(100), id integer, text text, source varchar(100), user_id integer, user_name varchar(100), user_screen_name varchar(100), json text)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("create table if not exists notification(block_id varchar(100) unique, created integer)");
			db.execSQL("create table if not exists push_notification(created_at integer, created_str varchar(100), id integer, text text, source varchar(100), user_id integer, user_name varchar(100), user_screen_name varchar(100), json text)");
		}
	}
	
	public boolean hasNotification(String block) {
		Cursor c = db.rawQuery("select count(*) from notification where block_id=?", new String[]{block});
		c.moveToNext();
		int count = c.getInt(0);
		c.close();
		return count!=0;
	}

	public boolean addOrDeleteNotification(boolean enable, List<String> blocks) {
		boolean has;
//		Cursor c = db.rawQuery(
//				"select count(*) from notification where block_id=?",
//				new String[] { blockid });
//		has = c.getInt(0) != 0;
//		c.close();
		for (String blockid : blocks) {
			if (!enable) {
				db.delete("notification", "block_id=?",
						new String[] { blockid });
			} else {
				ContentValues cv = new ContentValues();
				cv.put("block_id", blockid);
				cv.put("created", System.currentTimeMillis());
				db.insert("notification", null, cv);
			}
		}
		return !enable;
	}

	public void delete(Station from, Station to, long time) {
		db.delete("history", "depart_id=? and arrive_id=? and time=?",
				new String[] { from.getId(), to.getId(), String.valueOf(time) });
	}

	public void deleteAllHistory() {
		db.delete("history", null, null);
	}

}
