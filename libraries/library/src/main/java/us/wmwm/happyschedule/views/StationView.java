package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
        disableTouchTheft(this);
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

    public static void disableTouchTheft(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        super.onInterceptTouchEvent(ev);
//        return false;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        super.onTouchEvent(event);
//        return false;
//    }
}
