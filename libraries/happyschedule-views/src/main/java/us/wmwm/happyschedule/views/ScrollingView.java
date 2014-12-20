package us.wmwm.happyschedule.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created by gravener on 12/8/14.
 */
public class ScrollingView extends ScrollView {

    private static final String TAG = ScrollingView.class.getSimpleName();

    public ScrollingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }


    boolean intercept;

    boolean mIsScrolling;

    private float lastY;

    int touchCount;

    private float calculateDistanceY(MotionEvent e) {
        return Math.abs(e.getY() - lastY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        intercept = super.onInterceptTouchEvent(ev);
        ViewGroup parent = (ViewGroup) getParent();
        parent.getChildAt(0).dispatchTouchEvent(ev);
        getChildAt(0).dispatchTouchEvent(ev);
        return true;
    }

    private int mTouchSlop;


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean intercept = super.onTouchEvent(ev);
        ViewGroup parent = (ViewGroup) getParent();
        final int action = MotionEventCompat.getActionMasked(ev);
        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsScrolling = false;
            parent.getChildAt(0).dispatchTouchEvent(ev);
            getChildAt(0).dispatchTouchEvent(ev);
            Log.d(TAG, "intercept false");
            return true;
            //return false; // Do not intercept touch event, let the child handle it
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsScrolling = false;
                touchCount = 0;
                lastY = ev.getY();
            }

            case MotionEvent.ACTION_MOVE: {
                if (mIsScrolling) {
                    // We're currently scrolling, so yes, intercept the
                    // touch event!
                    Log.d(TAG, "intercept true");
                    parent.getChildAt(0).dispatchTouchEvent(ev);
                    getChildAt(0).dispatchTouchEvent(ev);
                    return true;
                }

                // If the user has dragged her finger horizontally more than
                // the touch slop, start the scroll

                // left as an exercise for the reader
                final float yDiff = calculateDistanceY(ev);
                Log.d(TAG, "yDiff: " + yDiff + " mTouchSLop: " + mTouchSlop);
                // Touch slop should be calculated using ViewConfiguration
                // constants.
                if (yDiff > mTouchSlop) {
                    // Start scrolling!
                    MotionEvent ev2 = MotionEvent.obtain(ev);
                    ev2.setAction(MotionEvent.ACTION_CANCEL);
                    parent.getChildAt(0).dispatchTouchEvent(ev2);
                    getChildAt(0).dispatchTouchEvent(ev2);
                    mIsScrolling = true;
                    Log.d(TAG, "intercept true");
                    return true;
                }
                break;
            }
        }
        parent.getChildAt(0).dispatchTouchEvent(ev);
        getChildAt(0).dispatchTouchEvent(ev);
        // In general, we don't want to intercept touch events. They should be
        // handled by the child view.
        return true;
    }
}
