package us.wmwm.happyschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StationView extends RelativeLayout {

	TextView text;
	
	String data;
	
	public StationView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_station, this);
		text = (TextView) findViewById(R.id.text);
	}
	
	public void setData(String k) {
		data = k;
		text.setText(data);
	}
	
	public String getData() {
		return data;
	}

}
