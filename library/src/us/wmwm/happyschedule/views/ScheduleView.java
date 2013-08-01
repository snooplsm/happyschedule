package us.wmwm.happyschedule.views;

import java.text.DateFormat;
import java.util.Calendar;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.StationToStation;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScheduleView extends RelativeLayout {

	StationToStation sts;
	
	TextView time;
	TextView status;
	TextView timeTillDepart;
	TextView duration;
	TextView train;
	
	static DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
	
	Drawable bg;
	
	public ScheduleView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_schedule,this);
		time = (TextView) findViewById(R.id.time);
		status = (TextView) findViewById(R.id.status);
		timeTillDepart = (TextView) findViewById(R.id.departs_in);
		duration = (TextView) findViewById(R.id.duration);
		train = (TextView) findViewById(R.id.trip_id);
		bg = getBackground();
	}

	public void setData(StationToStation sts) {
		this.sts = sts;
		time.setText(shrink(sts.departTime) + " - " + shrink(sts.arriveTime));
		
		Calendar cal = Calendar.getInstance();
		long diff = sts.departTime.getTimeInMillis() - cal.getTimeInMillis();
		long mins = diff / 60000;
		if(mins>=0 && mins < 100) {
			timeTillDepart.setVisibility(View.VISIBLE);
			timeTillDepart.setText("departs in " + mins + " min");
		} else {
			timeTillDepart.setVisibility(View.GONE);
			timeTillDepart.setText("");
		}
		
//		if(mins<-5) {
//			setBackgroundColor(0xFFCCCCCC);
//		} else {
//			setBackground(bg);
//		}
		if(!TextUtils.isEmpty(sts.tripId)) {
			train.setText("#"+sts.tripId);
		} else {
			train.setText("");
		}
		duration.setText(((sts.arriveTime.getTimeInMillis() - sts.departTime.getTimeInMillis()) / 60000) + " minutes");
			
	}
	
	private String shrink(Calendar cal) {
		return TIME.format(cal.getTime()).toLowerCase();//.replace(" am", "a").replace(" pm", "p");
	}
}
