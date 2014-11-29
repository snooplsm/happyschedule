package us.wmwm.happyschedule.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by gravener on 10/25/14.
 */
public class TrackView extends TextView {

    Path path = new Path();

    Paint p = new Paint();

    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP,24);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        path.reset();
//        float min = Math.min(canvas.getWidth(),canvas.getHeight());
//        p.setColor(Color.argb(37,0,0,0));
//        p.setAntiAlias(true);
//        p.setStyle(Paint.Style.FILL_AND_STROKE);
//        canvas.drawCircle(canvas.getWidth()/2f,canvas.getHeight()/2f,min/2f,p);
//        min*=.65f;
//        path.addCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f, min / 2f, Path.Direction.CW);
//        p.setColor(getPaint().getColor());
//        p.setAntiAlias(true);
//        p.setStyle(Paint.Style.FILL_AND_STROKE);
//        p.setTextSize(25);
//
//        canvas.rotate(-140,canvas.getWidth()/2f,canvas.getHeight()/2f);
//        //canvas.rotate(-90);
//        canvas.drawTextOnPath("TRACK", path,0,0,p);

    }
}
