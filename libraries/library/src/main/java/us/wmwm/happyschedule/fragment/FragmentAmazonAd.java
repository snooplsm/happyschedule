package us.wmwm.happyschedule.fragment;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdError.ErrorCode;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdTargetingOptions;

public class FragmentAmazonAd extends AbstractAdFragment implements AdListener {

    private static final String TAG = FragmentAmazonAd.class.getSimpleName();
    Future<?> newAdFuture;
    // the current ad visible while the next ad
    // loads
    Runnable loadAd = new Runnable() {
        public void run() {
            LoadAd();
        }

        ;
    };
    Runnable newAdRequest = new Runnable() {
        public void run() {
            handler.post(loadAd);
        }

        ;
    };
    private ViewGroup adViewContainer; // View group to which the ad view will
    // be added
    private AdLayout amazonCurrentAdView; // The ad that is currently visible to the
    // user
    private AdLayout amazonNextAdView; // A placeholder for the next ad so we can keep
    private int lastAd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_amazon_ad, container,
                false);
        adViewContainer = (ViewGroup) root;
        return root;
    }

    protected void LoadAd() {
        if (amazonNextAdView == null) { // Create and configure a new ad if the next
            // ad doesn't currently exist
            amazonNextAdView = new AdLayout(getActivity());
            LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            amazonNextAdView.setLayoutParams(layoutParams);
            // Register our ad handler that will receive call-backs for state
            // changes during the ad life cycle
            amazonNextAdView.setListener(this);
        }

        // Load the ad with the appropriate ad options.
        AdTargetingOptions adOptions = new AdTargetingOptions();
        amazonNextAdView.loadAd(adOptions);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (amazonCurrentAdView != null)
            this.amazonCurrentAdView.destroy();
        if (amazonNextAdView != null)
            this.amazonNextAdView.destroy();
        if (newAdFuture != null) {
            newAdFuture.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (newAdFuture != null) {
            newAdFuture.cancel(true);
        }
        newAdFuture = ThreadHelper.getScheduler().scheduleAtFixedRate(loadAd, 60000, 60000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (newAdFuture != null) {
            newAdFuture.cancel(true);
        }
    }

    @Override
    public void onAdLoaded(Ad ad, AdProperties adProperties) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Activity a = getActivity();
                if (a == null) {
                    return;
                }

                // If there is an ad currently being displayed, swap the ad that just
                // loaded
                // with ad currently being displayed, else display the ad that just
                // loaded.
                if (amazonCurrentAdView != null) {
                    SwapAmazonCurrentAd();
                } else {
                    // This is the first time we're loading an ad, so set the
                    // current ad view to the ad we just loaded and set the next to null
                    // so that we can loadScheduleRunnable a new ad in the background.
                    amazonCurrentAdView = amazonNextAdView;
                    amazonNextAdView = null;
                    ShowCurrentAmazonAd();
                }
                failureCount = 0;
                if (happyAdListener != null) {
                    happyAdListener.onAd();
                }
                Log.d(getClass().getSimpleName(), "onAdLoaded ");
            }
        });
    }


    private void SwapAmazonCurrentAd() {
        Log.d(TAG, "SWAP CURRENT AD");
        Activity a = getActivity();
        if (a == null) {
            Log.d(getClass().getSimpleName(), "activity is null, returning");
            return;
        }
        Animation slideDown = AnimationUtils.loadAnimation(
                a, R.anim.slide_down);
        slideDown.setAnimationListener(new AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                ShowNextAmazonAd();
            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationStart(Animation animation) {

            }

        });
        amazonCurrentAdView.startAnimation(slideDown);
    }

    private void ShowNextAmazonAd() {
        adViewContainer.removeAllViews();
        adViewContainer.removeView(amazonCurrentAdView);
        AdLayout tmp = amazonCurrentAdView;
        amazonCurrentAdView = amazonNextAdView;
        amazonNextAdView = tmp;
        ShowCurrentAmazonAd();
    }

    private void ShowCurrentAmazonAd() {
        Activity a = getActivity();
        if (a == null) {
            Log.d(getClass().getSimpleName(), "activity is null, returning");
            return;
        }
        adViewContainer.addView(amazonCurrentAdView);
        Animation slideUp = AnimationUtils.loadAnimation(
                a, R.anim.slide_up);
        amazonCurrentAdView.startAnimation(slideUp);
    }

    @Override
    public void onAdDismissed(Ad ad) {

    }

    @Override
    public void onAdCollapsed(Ad ad) {

    }

    @Override
    public void onAdExpanded(Ad ad) {

    }

    @Override
    public void onAdFailedToLoad(Ad ad, final AdError adError) {
        // TODO Auto-generated method stub
        Log.d(getClass().getSimpleName(),
                "onAdFailedToLoad " + adError.getMessage());
        failureCount++;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (happyAdListener != null) {
                    happyAdListener.onAdFailed(failureCount, adError.getCode() == ErrorCode.NO_FILL);
                }
            }
        });
    }
}
