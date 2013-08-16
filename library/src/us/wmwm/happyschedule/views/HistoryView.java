package us.wmwm.happyschedule.views;

import java.text.DateFormat;
import java.util.Calendar;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.model.Station;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HistoryView extends RelativeLayout {

	TextView from;
	TextView to;
	DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
	TextView time;
	
	Station fromStation;
	Station toStation;
	
	public HistoryView(Context ctx) {
		super(ctx);
		LayoutInflater.from(ctx).inflate(R.layout.view_history, this);
		from = (TextView) findViewById(R.id.from);
		to = (TextView) findViewById(R.id.to);
		time = (TextView) findViewById(R.id.time);
	}
	
	public void setData(Cursor cursor) {
		
		try {fromStation = Db.get().getStop(cursor.getString(0));
			from.setText(fromStation.getName());
		} catch (Exception e) {
			
		}
		try {
			toStation = Db.get().getStop(cursor.getString(1));
			to.setText(toStation.getName());
		} catch (Exception e) {
			
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cursor.getLong(2));
		time.setText(TIME.format(cal.getTime()));
		
	}
	
	public Station getFromStation() {
		return fromStation;
	}
	
	public Station getToStation() {
		return toStation;
	}
	
}
