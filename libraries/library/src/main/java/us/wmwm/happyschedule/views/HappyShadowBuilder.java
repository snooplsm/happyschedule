package us.wmwm.happyschedule.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.DragShadowBuilder;

public class HappyShadowBuilder extends DragShadowBuilder {

    Drawable bitmapDrawable;

    public HappyShadowBuilder(View view) {
        super(view);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int prev = view.getVisibility();
        view.setVisibility(View.VISIBLE);
        view.draw(canvas);
        view.setVisibility(prev);

        bitmapDrawable = new BitmapDrawable(view.getResources(), bitmap);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        bitmapDrawable.draw(canvas);
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        shadowSize.x = bitmapDrawable.getIntrinsicWidth() * 2;
        shadowSize.y = bitmapDrawable.getIntrinsicHeight() * 2;
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth() * 2, bitmapDrawable.getIntrinsicHeight() * 2);
        shadowTouchPoint.x = (int) (shadowSize.x / 2);
        shadowTouchPoint.y = (int) (shadowSize.y / 2);
    }

}
