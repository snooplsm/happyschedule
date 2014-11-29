package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.AppRailLine;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RailLineView extends RelativeLayout {

	TextView text;
	
	public RailLineView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_rail_line, this);
		text = (TextView) findViewById(R.id.name);
	}
	
	public void setData(AppRailLine r) {
		text.setText(r.getName());
	}
	
	

}
