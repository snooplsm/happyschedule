package us.wmwm.happyschedule.views;

import java.text.DateFormat;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.StationToStation;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScheduleView extends RelativeLayout {

	StationToStation sts;
	
	TextView time;
	
	static DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
	
	public ScheduleView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_schedule,this);
		time = (TextView) findViewById(R.id.time);
	}

	public void setData(StationToStation sts) {
		this.sts = sts;
		time.setText(TIME.format(sts.departTime.getTime()).toLowerCase());
	}
}
