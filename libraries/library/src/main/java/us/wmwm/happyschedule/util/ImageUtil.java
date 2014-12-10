package us.wmwm.happyschedule.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.larvalabs.svgandroid.SVGBuilder;

/**
 * Created by gravener on 11/29/14.
 */
public class ImageUtil {

    public static Bitmap loadBitmapFromSvgWithColorOverride(Context ctx, int res, Integer color) {
        if(color==null) {
            color = Color.argb(210, 255, 255, 255);
        }
        Picture p = new SVGBuilder().readFromResource(ctx.getResources(), res).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC)).build().getPicture();
        Bitmap bb = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bb);
        c.drawPicture(p);
        return bb;
    };


}
