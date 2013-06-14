package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StationButton extends RelativeLayout {

	TextView text;
	
	public StationButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_station_button,this);
		text = (TextView) findViewById(R.id.departure_id);
	}
	
	

}
