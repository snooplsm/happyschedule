package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.DragShadowBuilder;

public class HappyShadowBuilder extends DragShadowBuilder {

	Drawable bitmapDrawable;
	
	public HappyShadowBuilder(View view) {
		super(view);
				
		bitmapDrawable = view.getResources().getDrawable(R.drawable.reload_light);
		bitmapDrawable.setBounds(0, 0, bitmapDrawable.getMinimumWidth()*2, bitmapDrawable.getMinimumHeight()*2);
				
	}
	
	@Override
	public void onDrawShadow(Canvas canvas) {
		bitmapDrawable.draw(canvas);
	}
	
    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        shadowSize.x = bitmapDrawable.getMinimumWidth()*2;
        shadowSize.y = bitmapDrawable.getMinimumHeight()*2;

        shadowTouchPoint.x = (int)(shadowSize.x / 2);
        shadowTouchPoint.y = (int)(shadowSize.y / 2);
    }
	

}
