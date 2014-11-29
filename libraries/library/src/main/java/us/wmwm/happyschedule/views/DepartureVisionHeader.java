package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DepartureVisionHeader extends RelativeLayout {

    TextView header;

    ShapeDrawable shape;

    public DepartureVisionHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("DepartureVisionHeader", "constructor");
        LayoutInflater.from(context).inflate(R.layout.view_departurevision_header, this);
        header = (TextView) findViewById(R.id.header);
        float[] roundedCorner = new float[]{0, 0, 8, 8, 8, 8, 0, 0};
        RoundRectShape s = new RoundRectShape(roundedCorner, null, null);
        ShapeDrawable d = new ShapeDrawable(s);
        d.getPaint().setColor(getResources().getColor(R.color.get_schedule_11));
        shape = d;
        header.setBackgroundDrawable(d);
    }

    public void setShapeColor(int color) {
        shape.getPaint().setColor(color);
    }

    public void setData(String txt) {
        header.setText(txt);
    }

}
