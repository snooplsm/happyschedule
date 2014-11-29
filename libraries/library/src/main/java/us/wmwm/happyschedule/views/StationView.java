package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.TypedValue;
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
		int size = PreferenceManager.getDefaultSharedPreferences(context).getInt("textSize", 15);
		//TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics());
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
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
