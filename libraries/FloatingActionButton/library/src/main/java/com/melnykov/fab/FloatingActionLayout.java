package com.melnykov.fab;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by gravener on 10/5/14.
 */
public class FloatingActionLayout extends LinearLayout implements ViewGroup.OnHierarchyChangeListener {

    protected AbsListView mListView;

    private int mScrollY;

    private final AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int newScrollY = getListViewScrollY();
            if (newScrollY == mScrollY) {
                return;
            }

            if (newScrollY > mScrollY) {
                // Scrolling up
                for(int i = 0; i < getChildCount(); i++) {
                    View v = getChildAt(i);
                    if(v instanceof FloatingActionButton) {
                        FloatingActionButton fab = (FloatingActionButton)v;
                        fab.hide();
                    }
                }
            } else if (newScrollY < mScrollY) {
                // Scrolling down
                for(int i = 0; i < getChildCount(); i++) {
                    View v = getChildAt(i);
                    if(v instanceof FloatingActionButton) {
                        FloatingActionButton fab = (FloatingActionButton)v;
                        fab.show();
                    }
                }
            }
            mScrollY = newScrollY;
        }
    };


    protected int getListViewScrollY() {
        View topChild = mListView.getChildAt(0);
        return topChild == null ? 0 : mListView.getFirstVisiblePosition() * topChild.getHeight() -
                topChild.getTop();
    }

    public void attachToListView(@NonNull AbsListView listView) {
        if (listView == null) {
            throw new NullPointerException("AbsListView cannot be null.");
        }
        mListView = listView;
        mListView.setOnScrollListener(mOnScrollListener);
    }

    public void attachToListView(@NonNull StickyListHeadersListView listView) {
        if (listView == null) {
            throw new NullPointerException("AbsListView cannot be null.");
        }
        mListView = listView.getWrappedList();
        listView.setOnScrollListener(mOnScrollListener);
    }

    public FloatingActionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        setOnHierarchyChangeListener(this);
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        if(orientation==LinearLayout.VERTICAL) {
            throw new IllegalStateException("Orientation is not allowed to be set");
        }
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        doWork();
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        doWork();
    }

    FloatingActionButton overflow;

    private void doWork() {
        int count = getChildCount();
        if(count>1) {
            if(overflow!=null) {
                count--;
            }
        }
        if(count>1) {
            if(overflow==null) {
                overflow = new FloatingActionButton(getContext());
                int res = getResources().getIdentifier("ic_action_overflow","drawable",getContext().getPackageName());
                overflow.setImageResource(res);
                FloatingActionButton next = ((FloatingActionButton)getChildAt(1));
                overflow.setColorNormal(next.getColorNormal());
                overflow.setColorPressed(next.getColorPressed());
                overflow.setType(FloatingActionButton.TYPE_MINI);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getChildAt(0).getLayoutParams();
                LinearLayout.LayoutParams lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                lp2.bottomMargin = lp.bottomMargin;
                lp2.topMargin = lp.topMargin;
                addView(overflow,0,lp2);
                overflow.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int nextVis = View.GONE;
                        if(getChildAt(1).getVisibility()==View.GONE) {
                            nextVis = View.VISIBLE;
                        }

                        for(int i = 1; i < getChildCount(); i++) {
                            View view = getChildAt(i);
                            view.setVisibility(nextVis);
                            overflow.setType(FloatingActionButton.TYPE_MINI);
                        }
                    }
                });
            }
        }

        for(int i = 1; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setVisibility(View.GONE);
            if(view instanceof FloatingActionButton) {
                FloatingActionButton b = (FloatingActionButton)view;
                b.setType(FloatingActionButton.TYPE_MINI);
            }
        }
    }
}
