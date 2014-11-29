package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GetScheduleButton extends RelativeLayout {

	TextView text;
	
	public GetScheduleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(R.layout.view_get_schedule, this);
		text = (TextView) findViewById(R.id.text);
	}

}
