package us.wmwm.happyschedule.fragment;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.util.PremiumUserHelper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class FragmentGoogleAd extends AbstractAdFragment {

    private ViewGroup adViewContainer; // View group to which the ad view will
    // be added
    private AdView currentAdView; // The ad that is currently visible to the
    // user
    private AdView nextAdView; // A placeholder for the next ad so we can keep
    // the current ad visible while the next ad
    // loads

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("failureCount", failureCount);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_amazon_ad, container,
                false);
        adViewContainer = (ViewGroup) root;
        return root;
    }

    protected void LoadAd() {
        if (nextAdView == null) { // Create and configure a new ad if the next
            // ad doesn't currently exist
            try {
                nextAdView = new AdView(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            // Note: The above implementation is for an auto-sized ad in an
            // AdLayout of width MATCH_PARENT and
            // height WRAP_CONTENT. If you instead want to give the ad a fixed
            // size, you will need to factor in
            // the phone's scale when setting up the AdLayout dimensions. See
            // the example below for 320x50 dpi:
            // nextAdView = new AdLayout(this, AdSize.SIZE_320x50);
            // float scale =
            // this.getApplicationContext().getResources().getDisplayMetrics().density;
            // LayoutParams layoutParams = new FrameLayout.LayoutParams((int)
            // (320 * scale),
            // (int) (50 * scale), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            nextAdView.setLayoutParams(layoutParams);
            // Register our ad handler that will receive call-backs for state
            // changes during the ad life cycle
            //nextAdView.setAdListener(this);
        }
        //nextAdView.l
        // Load the ad with the appropriate ad options
        AdRequest req = new AdRequest.Builder().build();
        if (nextAdView.getAdUnitId() == null) {
            nextAdView.setAdUnitId(getResources().getString(R.string.admob_app_key));
        }
        nextAdView.setAdListener(adListener);
        if (nextAdView.getAdSize() == null) {
            nextAdView.setAdSize(AdSize.SMART_BANNER);
        }
        nextAdView.loadAd(req);
    }

    AdListener adListener = new AdListener() {
        @Override
        public void onAdClosed() {
        }

        public void onAdLoaded() {
            ShowNextAd();
        }

        ;

        public void onAdFailedToLoad(int errorCode) {

        }

        ;
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentAdView != null)
            this.currentAdView.destroy();
        if (nextAdView != null)
            this.nextAdView.destroy();
        if (newAdFuture != null) {
            newAdFuture.cancel(true);
        }
    }

    Future<?> newAdFuture;

    Runnable newAdRequest = new Runnable() {
        public void run() {
            handler.post(loadAd);
        }

        ;
    };

    Runnable loadAd = new Runnable() {
        public void run() {
            LoadAd();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (newAdFuture != null) {
            newAdFuture.cancel(true);
        }
        newAdFuture = ThreadHelper.getScheduler().scheduleAtFixedRate(newAdRequest, 60000, 60000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (newAdFuture != null) {
            newAdFuture.cancel(true);
        }
    }

    private void SwapCurrentAd() {
        Activity a = getActivity();
        if (a == null) {
            return;
        }
        Animation slideDown = AnimationUtils.loadAnimation(
                a, R.anim.slide_down);
        slideDown.setAnimationListener(new AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                ShowNextAd();
            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationStart(Animation animation) {

            }

        });
        currentAdView.startAnimation(slideDown);
    }

    private void ShowNextAd() {
        adViewContainer.removeView(currentAdView);
        AdView tmp = currentAdView;
        currentAdView = nextAdView;
        nextAdView = tmp;
        ShowCurrentAd();
    }

    private void ShowCurrentAd() {
        Activity a = getActivity();
        if (a == null) {
            return;
        }
        adViewContainer.addView(currentAdView);
        Animation slideUp = AnimationUtils.loadAnimation(
                a, R.anim.slide_up);
        currentAdView.startAnimation(slideUp);
    }


}

