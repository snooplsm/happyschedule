package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GetScheduleButton extends RelativeLayout {

	TextView text;

    private static final String TAG = GetScheduleButton.class.getSimpleName();
	
	public GetScheduleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(R.layout.view_get_schedule, this);
		text = (TextView) findViewById(R.id.text);
	}

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        boolean intercept = super.onInterceptTouchEvent(ev);
//        Log.d(TAG,"intercept? " + intercept);
//        return intercept;
//    }
}
