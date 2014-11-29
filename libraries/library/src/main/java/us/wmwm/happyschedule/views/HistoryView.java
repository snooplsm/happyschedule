package us.wmwm.happyschedule.views;

import java.text.DateFormat;
import java.util.Calendar;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Station;
import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HistoryView extends RelativeLayout {

	TextView from;
	TextView to;
	DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
	TextView time;
	
	Station fromStation;
	Station toStation;
	long timetime;
	ImageView bookmark;
	
	public HistoryView(Context ctx) {
		super(ctx);
		LayoutInflater.from(ctx).inflate(R.layout.view_history, this);
		from = (TextView) findViewById(R.id.from);
		to = (TextView) findViewById(R.id.to);
		time = (TextView) findViewById(R.id.time);
		bookmark = (ImageView) findViewById(R.id.bookmark);
		int size = PreferenceManager.getDefaultSharedPreferences(ctx).getInt("textSize", 17);
		//TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics());
		from.setTextSize(TypedValue.COMPLEX_UNIT_SP, size+2);
		to.setTextSize(TypedValue.COMPLEX_UNIT_SP, size+2);
		time.setTextSize(TypedValue.COMPLEX_UNIT_SP, size+2);
	}
	
	public void setData(Cursor cursor) {		
		bookmark.setVisibility(View.GONE);
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
		timetime = cursor.getLong(2);
		cal.setTimeInMillis(timetime);		
		time.setText(TIME.format(cal.getTime()).toLowerCase());
		time.setVisibility(View.VISIBLE);
	}
	
	public void setData(DepartureVision dv) {
		bookmark.setVisibility(View.VISIBLE);
		fromStation = Db.get().getStop(dv.getFrom());
		toStation = Db.get().getStop(dv.getTo());
		from.setText(fromStation.getName());
		to.setText(toStation.getName());
		time.setVisibility(View.GONE);
	}
	
	public Station getFromStation() {
		return fromStation;
	}
	
	public long getTimetime() {
		return timetime;
	}
	
	public Station getToStation() {
		return toStation;
	}
	
}
