package us.wmwm.happyschedule.views;

import java.util.Map;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.R.id;
import us.wmwm.happyschedule.R.layout;
import us.wmwm.happyschedule.model.LineStyle;
import us.wmwm.happyschedule.model.TrainStatus;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DepartureVisionView extends RelativeLayout {

	View lineIndicator;
	TextView destination;
	TextView acronym;
	TextView track;
	View trackContainer;
	TextView time;
	
	public DepartureVisionView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_departurevision,this);
		lineIndicator = findViewById(R.id.line_indicator);
		destination = (TextView) findViewById(R.id.destination);
		acronym = (TextView) findViewById(R.id.acronym);
		track = (TextView) findViewById(R.id.track);
		trackContainer = findViewById(R.id.track_container);
		time = (TextView) findViewById(R.id.time);
	}
	
	public void setData(TrainStatus status, Map<String,LineStyle> keyToColor) {
		LineStyle line = keyToColor.get(status.getLine().toLowerCase());
		if(line!=null) {
			lineIndicator.setBackgroundColor(line.color);
			acronym.setText(line.acronym);
		}
		
		destination.setText(status.getDest());
		if(TextUtils.isEmpty(status.getTrack())) {
			trackContainer.setVisibility(View.INVISIBLE);
		} else {
			trackContainer.setVisibility(View.VISIBLE);
			track.setText(status.getTrack());
		}
		time.setText(status.getDeparts());
		
	};

	
	
}
