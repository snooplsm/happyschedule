package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.Station;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StationButton extends RelativeLayout {

	TextView text;
	
	Station station;
	
	public StationButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		try {
			LayoutInflater.from(context).inflate(R.layout.view_station_button,this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		text = (TextView) findViewById(R.id.text_id);
	}
	
	public void setHint(String txt) {
		text.setText(txt);
	}
	
	public void setStation(Station station) {
		this.station = station;
		text.setText(station.getName());
	}
	
	public Station getStation() {
		return station;
	}

}
