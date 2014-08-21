package us.wmwm.happyschedule.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class ProgressView extends Drawable {

	private Paint paint = new Paint();
	
	private int color = 0x55666666;
	
	private float percent = .10f;
	
	public void setPercent(float percent) {
		System.out.println(percent);
		this.percent = percent;
	}

	@Override
	public void draw(Canvas canvas) {
		paint.setColor(color);
		paint.setAntiAlias(true);
		if(percent>0) {
			canvas.drawRect(0, 0, canvas.getWidth()*percent, canvas.getHeight(), paint);
		}
	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paint.setColorFilter(cf);
	}
}