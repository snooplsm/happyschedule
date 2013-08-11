package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.Db;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HistoryView extends RelativeLayout {

	TextView from;
	TextView to;
	
	public HistoryView(Context ctx) {
		super(ctx);
		LayoutInflater.from(ctx).inflate(R.layout.view_history, this);
		from = (TextView) findViewById(R.id.from);
		to = (TextView) findViewById(R.id.to);
	}
	
	public void setData(Cursor cursor) {
		
		try {
			from.setText(Db.get().getStop(cursor.getString(0)).getName());
		} catch (Exception e) {
			
		}
		try {
			to.setText(Db.get().getStop(cursor.getString(1)).getName());
		} catch (Exception e) {
			
		}
	}
	
}
