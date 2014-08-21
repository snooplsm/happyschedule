package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DepartureVisionHeader extends RelativeLayout {

	TextView header;
	
	public DepartureVisionHeader(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_departurevision_header, this);
		header = (TextView) findViewById(R.id.header);
	}
	
	public void setData(String txt) {
		header.setText(txt);
	}

}
