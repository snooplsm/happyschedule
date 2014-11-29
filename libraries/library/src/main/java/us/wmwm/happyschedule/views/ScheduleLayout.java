package us.wmwm.happyschedule.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by gravener on 11/21/14.
 */
public class ScheduleLayout extends RelativeLayout {

    private static final String TAG = ScheduleLayout.class.getSimpleName();

    TextView depart;
    View track;

    OnHierarchyChangeListener onHierarchyChangeListener = new OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {
            if (child.getId() == us.wmwm.happyschedule.R.id.depart) {
                depart = (TextView) child;
            }
            if (child.getId() == us.wmwm.happyschedule.R.id.track2) {
                track = (TextView) child;
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {

        }
    };

    public ScheduleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnHierarchyChangeListener(onHierarchyChangeListener);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
    }

    Rect dirtyRect = new Rect();

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        int top = depart.getTop();
////        Log.d(TAG, "top is " + top);
////        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) track.getLayoutParams();
////        dirtyRect.left = track.getLeft();
////        dirtyRect.top = track.getTop();
////        dirtyRect.bottom = track.getBottom();
////        dirtyRect.right = track.getRight();
////
////        if(depart.getMeasuredHeight()>track.getMeasuredHeight()) {
////            int diff = depart.getMeasuredHeight() / 2;
////            lp.addRule(RelativeLayout.ALIGN_BASELINE,RelativeLayout.NO_ID);
////            int newTopMargin = top + diff / 2;
////            //if(newTopMargin!=lp.topMargin) {
////            lp.topMargin = top + diff / 2;
////            Log.d(TAG, "new top margin: " + lp.topMargin);
////            track.setLayoutParams(lp);
////        } else {
////            lp.addRule(RelativeLayout.ALIGN_BASELINE,depart.getId());
////            track.setLayoutParams(lp);
////        }
//
//        super.onLayout(changed, l, t, r, b);
//    }


//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        //  The layout has actually already been performed and the positions
//        //  cached.  Apply the cached values to the children.
//        int count = getChildCount();
//
//        for (int i = 0; i < count; i++) {
//            View child = getChildAt(i);
//            if (child.getVisibility() != GONE) {
//                RelativeLayout.LayoutParams st =
//                        (RelativeLayout.LayoutParams) child.getLayoutParams();
//
//                child.layout(st.leftMargin, st.topMargin, st.rightMargin, st.bottomMargin);
//
//
//            }
//        }
//    }

}
