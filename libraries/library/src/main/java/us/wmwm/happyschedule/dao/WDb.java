package us.wmwm.happyschedule.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Status;
import twitter4j.json.DataObjectFactory;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.StationToStation;

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

    public Map<String,String> getPreferences(String wildcard) {
        Cursor c = db.rawQuery("select key,value from preference where key like ?", new String[] {wildcard.replaceAll("\\*","%")});
        Map<String,String> prefs = new HashMap<String,String>();
        while(c.moveToNext()) {
            prefs.put(c.getString(0),c.getString(1));
        }
        c.close();
        return prefs;
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
						"select depart_id,  arrive_id, time, time as _id from history group by depart_id,arrive_id, datetime(time,'unixepoch') order by time desc",
						null);
	}

    public int getHistorySize() {
        Cursor c = db.rawQuery("select count(*) from history",null);
        try {
            c.moveToNext();
            return c.getInt(0);
        } catch (Exception e) {

        } finally {
            c.close();
        }
        return 0;


    }

    public void save(Status tweet, String tweetJson) {
        ContentValues cv = new ContentValues();
        cv.put("screenName",tweet.getUser().getScreenName());
        cv.put("created", tweet.getCreatedAt().getTime());
        cv.put("original",tweetJson);
        db.insert("status",null,cv);
    }

    public List<Status> getStatuses() {
        Cursor c = db.rawQuery("select original from status order by created desc limit 30", null);
        List<Status> statuses = new ArrayList<Status>(30);
        try {
            while (c.moveToNext()) {
                String orig = c.getString(0);
                try {
                    Status status = DataObjectFactory.createStatus(orig);
                    statuses.add(status);
                } catch (Exception e) {
                    // can't do shit here
                }
            }
        } finally {
            if(c!=null) {
                c.close();
            }
        }
        return statuses;
    }



	private static class OpenHelper extends SQLiteOpenHelper {
		public OpenHelper(String name) {
			super(HappyApplication.get(), name, null, 8);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists history(depart_id varchar(255), arrive_id varchar(255), time integer)");
			db.execSQL("create table if not exists preference(key varchar(255) unique, value text)");
			db.execSQL("create table if not exists notification(block_id varchar(100) unique, created integer)");
			db.execSQL("create table if not exists push_notification(created_at integer, created_str varchar(100), id integer, text text, source varchar(100), user_id integer, user_name varchar(100), user_screen_name varchar(100), json text)");
            db.execSQL("CREATE TABLE if not exists schedule_path (source VARCHAR(20) NOT NULL,target VARCHAR(20) NOT NULL,sequence INTEGER,level INTEGER,a VARCHAR(20) NOT NULL,b VARCHAR(20) NOT NULL)");
            db.execSQL("create table if not exists status(id integer, screenName text, created integer, original text, unique(id))");
            db.execSQL("create table if not exists favorite(trip_id text, block_id text, days text)");
			db.execSQL("create table if not exists favorite_trip(depart_id text, arrive_id text, trip_id text, block_id text, unique(depart_id,arrive_id,trip_id))");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("create table if not exists notification(block_id varchar(100) unique, created integer)");
            db.execSQL("CREATE TABLE if not exists schedule_path (source VARCHAR(20) NOT NULL,target VARCHAR(20) NOT NULL,sequence INTEGER,level INTEGER,a VARCHAR(20) NOT NULL,b VARCHAR(20) NOT NULL)");
			db.execSQL("create table if not exists push_notification(created_at integer, created_str varchar(100), id integer, text text, source varchar(100), user_id integer, user_name varchar(100), user_screen_name varchar(100), json text)");
            if(oldVersion<6) {
                db.execSQL("drop table if exists status");
            }
            db.execSQL("create table if not exists status(id integer, screenName text, created integer, original text, unique(id))");
            db.execSQL("create table if not exists favorite(trip_id text, block_id text, days text)");
			db.execSQL("create table if not exists favorite_trip(depart_id text, arrive_id text, trip_id text, block_id text, unique(depart_id,arrive_id,trip_id))");
		}
	}

    public void addGraph(List<Station> stations) {
        Station first = stations.get(0);
        Station last = stations.get(stations.size()-1);
        Cursor c = db.rawQuery("select max(level) from schedule_path where source=? and target=?", new String[]{first.getId(),last.getId()});
        int level = 0;
        if(c.moveToNext()) {
            level = c.getInt(0)+1;
        }
        c.close();
        int sequence = 0;
        for(int i = 1; i < stations.size();i++) {
            Station a = stations.get(i-1);
            Station b = stations.get(i);
            ContentValues cv = new ContentValues();
            cv.put("a",a.getId());
            cv.put("b",b.getId());
            cv.put("source",first.getId());
            cv.put("target",last.getId());
            cv.put("sequence",sequence++);
            cv.put("level",level);
            db.insert("schedule_path",null,cv);
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

	public boolean isFavorite(StationToStation sts) {
		boolean fav = true;
		if (sts instanceof StationInterval) {
			StationInterval si = (StationInterval) sts;
			if (si.hasNext()) {
				while (si.hasNext()) {
					Cursor c = db.rawQuery("select count(*) from favorite_trip where depart_id=? and arrive_id=? and trip_id=?", new String[]{si.departId,si.arriveId,si.tripId});
					c.moveToNext();
					fav = c.getInt(0)==1;
					c.close();
					if(!fav) {
						return false;
					}
				}
			} else {
				Cursor c = db.rawQuery("select count(*) from favorite_trip where depart_id=? and arrive_id=? and trip_id=?", new String[]{si.departId,si.arriveId,si.tripId});
				c.moveToNext();
				fav = c.getInt(0)==1;
				c.close();
				if(!fav) {
					return false;
				}
			}
		} else {
			Cursor c = db.rawQuery("select count(*) from favorite_trip where depart_id=? and arrive_id=? and trip_id=?", new String[]{sts.departId,sts.arriveId,sts.tripId});
			c.moveToNext();
			fav = c.getInt(0)==1;
			c.close();
		}
		return fav;
	}

	public void deleteFavorite(StationToStation sts) {
		ContentValues cv = new ContentValues();
		if (sts instanceof StationInterval) {
			StationInterval si = (StationInterval) sts;
			if (si.hasNext()) {
				while (si.hasNext()) {
					db.delete("favorite_trip", "depart_id=? and arrive_id=? and trip_id=?", new String[]{si.departId, si.arriveId, si.tripId});
				}
			} else {
				db.delete("favorite_trip", "depart_id=? and arrive_id=? and trip_id=?", new String[]{si.departId, si.arriveId, si.tripId});
			}
		} else {
			db.delete("favorite_trip", "depart_id=? and arrive_id=? and trip_id=?", new String[]{sts.departId, sts.arriveId, sts.tripId});
		}
	}



	public void saveFavorite(StationToStation sts) {
		ContentValues cv = new ContentValues();
		if (sts instanceof StationInterval) {
			StationInterval si = (StationInterval) sts;
			cv.put("depart_id",si.departId);
			cv.put("arrive_id",si.arriveId);
			if(si.hasNext()) {
				while (si.hasNext()) {
					if (si.blockId != null) {
						cv.put("trip_id", si.tripId);
						cv.put("block_id", si.blockId);
					}
					db.insertWithOnConflict("favorite_trip", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
				}
			} else {
				cv.put("trip_id", si.tripId);
				cv.put("block_id", si.blockId);
				db.insertWithOnConflict("favorite_trip", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
			}
		} else {
			cv.put("depart_id",sts.departId);
			cv.put("arrive_id", sts.arriveId);
			cv.put("trip_id",sts.tripId);
			cv.put("arrive_id",sts.arriveId);
			db.insertWithOnConflict("favorite_trip", null,cv,SQLiteDatabase.CONFLICT_REPLACE);
		}

		//db.delete("favorite_trip","depart_id=? and arrive_id=?",new String[]{});
	}

}
