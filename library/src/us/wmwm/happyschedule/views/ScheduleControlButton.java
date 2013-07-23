package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ScheduleControlButton extends RelativeLayout {

	ImageView icon;

	public ScheduleControlButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater.from(context).inflate(
				R.layout.view_schedule_control_button, this);
		icon = (ImageView) findViewById(R.id.icon);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ScheduleControlButton);
		int resId = a.getResourceId(R.styleable.ScheduleControlButton_src, 0);
		icon.setImageResource(resId);
	}

	public ScheduleControlButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScheduleControlButton(Context context) {
		this(context, null);
	}

}
