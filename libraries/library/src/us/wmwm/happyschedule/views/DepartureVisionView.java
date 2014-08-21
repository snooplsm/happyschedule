package us.wmwm.happyschedule.views;

import java.util.Map;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.LineStyle;
import us.wmwm.happyschedule.model.TrainStatus;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
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
	float one,two,three,four;
	TrainStatus status;
	
	public TrainStatus getTrainStatus() {
		return status;
	}

	public DepartureVisionView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_departurevision,
				this);
		lineIndicator = findViewById(R.id.line_indicator);
		destination = (TextView) findViewById(R.id.destination);
		acronym = (TextView) findViewById(R.id.acronym);
		track = (TextView) findViewById(R.id.track);
		trackContainer = findViewById(R.id.track_container);
		time = (TextView) findViewById(R.id.time);
		one = getResources().getInteger(R.integer.departure_vision_one);
		two = getResources().getInteger(R.integer.departure_vision_two);
		three = getResources().getInteger(R.integer.departure_vision_three);
		four = getResources().getInteger(R.integer.departure_vision_four);
	}

	public void setData(TrainStatus status, Map<String,LineStyle> keyToColor) {
		this.status = status;
		LineStyle line = keyToColor.get(status.getLine().toLowerCase());
		if(line!=null) {
			lineIndicator.setBackgroundColor(line.color);
			acronym.setText(line.acronym);
		} else {
			try {
			acronym.setText(status.getLine().substring(0, Math.min(status.getLine().length(), 3)));
			} catch (Exception e) {
				
			}
		}
		
		destination.setText(status.getDest());
		if(TextUtils.isEmpty(status.getTrack())) {
			track.setVisibility(View.INVISIBLE);
		} else {
			track.setVisibility(View.VISIBLE);
			track.setText(status.getTrack());
			int len = status.getTrack().length();
			float size;
			if(len==1) {
				size = one;
			}else
			if(len==2) {
				size = two;
			} else if (len==3){
				size = three;
			} else {
				size = four;
			}
			track.setTextSize(TypedValue.COMPLEX_UNIT_DIP,size);
		}
		time.setText(status.getDeparts());
		
	};

}
