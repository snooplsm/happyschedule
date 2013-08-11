package us.wmwm.happyschedule.dao;

import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.model.Station;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WDb {

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
		cv.put("time",  System.currentTimeMillis());
		db.insert("history", null, cv);
	}
	
	public Cursor getHistory() {
		return db.rawQuery("select depart_id,  arrive_id, time, time as _id from history order by time desc", null);
	}

	private static class OpenHelper extends SQLiteOpenHelper {
		public OpenHelper(String name) {
			super(HappyApplication.get(),name,null,1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists history(depart_id varchar(255), arrive_id varchar(255), time integer)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}

}

