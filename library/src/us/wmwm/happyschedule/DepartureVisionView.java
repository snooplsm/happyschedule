package us.wmwm.happyschedule;

import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DepartureVisionView extends RelativeLayout {

	View lineIndicator;
	TextView destination;
	TextView acronym;
	
	public DepartureVisionView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_departurevision,this);
		lineIndicator = findViewById(R.id.line_indicator);
		destination = (TextView) findViewById(R.id.destination);
		acronym = (TextView) findViewById(R.id.acronym);
	}
	
	public void setData(TrainStatus status, Map<String,LineStyle> keyToColor) {
		LineStyle line = keyToColor.get(status.getLine().toLowerCase());
		if(line!=null) {
			lineIndicator.setBackgroundColor(line.color);
			acronym.setText(line.acronym);
		}
		
		destination.setText(status.getDest());
		
		
	};

	
	
}
