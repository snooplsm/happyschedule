package us.wmwm.happyschedule.fragment;

import android.os.Bundle;
import android.os.Handler;

import us.wmwm.happyschedule.util.PremiumUserHelper;

/**
 * Created by gravener on 11/14/14.
 */
public abstract class AbstractAdFragment extends HappyFragment {

    protected HappyAdListener happyAdListener;

    public void setHappyAdListener(HappyAdListener happyAdListener) {
        this.happyAdListener = happyAdListener;
    }

    Handler handler = new Handler();


    protected int failureCount;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("failureCount", failureCount);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null) {
            failureCount = savedInstanceState.getInt("failureCount");
        }
        if(!PremiumUserHelper.isPaidUser()) {
            LoadAd();
        }
    }

    abstract protected void LoadAd();



}
