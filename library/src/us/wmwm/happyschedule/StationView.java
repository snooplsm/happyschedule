package us.wmwm.happyschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StationView extends RelativeLayout {

	TextView text;
	
	String name;
	String id;
	
	public StationView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_station, this);
		text = (TextView) findViewById(R.id.text);
	}
	
	public void setData(String id, String name) {
		this.name = name;
		this.id = id;
		text.setText(name);
	}
	
	public String getStationId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

}
