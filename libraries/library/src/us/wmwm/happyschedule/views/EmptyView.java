package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class EmptyView extends RelativeLayout {

	public EmptyView(Context ctx) {
		this(ctx,null,0);
		
	}

	public EmptyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.view_sad, this);
	}

	public EmptyView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}
	
	
	
}
