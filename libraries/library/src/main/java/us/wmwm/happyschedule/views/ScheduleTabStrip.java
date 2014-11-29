package us.wmwm.happyschedule.views;

import android.content.Context;
import android.support.v4.view.PagerTabStrip;
import android.util.AttributeSet;

public class ScheduleTabStrip extends PagerTabStrip {

	Context ctx;
	
	public ScheduleTabStrip(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public ScheduleTabStrip(Context context) {
		super(context);
		this.ctx = context;
	}

}
