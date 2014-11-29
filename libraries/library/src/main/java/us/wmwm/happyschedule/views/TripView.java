package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class TripView extends LinearLayout {

	public TripView(Context ctx) {
		super(ctx);
		LayoutInflater.from(ctx).inflate(R.layout.view_trip,this);
	}
	
}
