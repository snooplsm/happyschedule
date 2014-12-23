package us.wmwm.happyschedule.fragment;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdTargetingOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.BuildConfig;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.MainActivity;

/**
 * Created by gravener on 11/14/14.
 */
public class FragmentHappytapAd extends AbstractAdFragment {

    private static final String TAG = FragmentHappytapAd.class.getSimpleName();

    private ViewGroup adViewContainer; // View group to which the ad view will
    // be added
    private AdView currentAdView; // The ad that is currently visible to the
    // user
    private AdView nextAdView;

    private AdLayout amazonCurrentAdView; // The ad that is currently visible to the
    // user
    private AdLayout amazonNextAdView; // A placeholder for the next ad so we can keep

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_happytap_ad,container,false);
        return adViewContainer;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle buyIntentBundle = MainActivity.BILLING_SERVICE.getBuyIntent(3, getActivity().getPackageName(),
                            "rails.monthly", "subs", new Date().toString());
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                            Integer.valueOf(0));
                } catch (Exception e) {

                }
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"onActivityCreated");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(BuildConfig.IS_GOOGLE_ADS_ENABLED) {
                    LoadAd();
                } else
                if(BuildConfig.IS_AMAZON_ADS_ENABLED) {
                    LoadAmazonAd();
                }
            }
        },3000);
    }

    AdListener adListener = new AdListener() {
        @Override
        public void onAdClosed() {
        }

        public void onAdLoaded() {
            showNextAd();
        }

        ;

        public void onAdFailedToLoad(int errorCode) {
            Log.d(TAG,"ad failed to loadScheduleRunnable " + errorCode);
        }

        ;
    };

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
        if(newAdFuture!=null) {
            newAdFuture.cancel(true);
        }
    }

    private void showNextAd() {
        Log.d(TAG,"showing next ad");
        adViewContainer.removeView(currentAdView);
        AdView tmp = currentAdView;
        currentAdView = nextAdView;
        nextAdView = tmp;
        showCurrentAd();
    }

    private void showCurrentAd() {
        Activity a = getActivity();
        if (a == null) {
            return;
        }
        adViewContainer.addView(currentAdView);
        Animation slideUp = AnimationUtils.loadAnimation(
                a, R.anim.slide_up);
        currentAdView.startAnimation(slideUp);
    }

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

    @Override
    protected void LoadAd() {
        Log.d(TAG,"google ads enabled");
        if (nextAdView == null) { // Create and configure a new ad if the next
            // ad doesn't currently exist
            nextAdView = new AdView(getActivity());
            ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            nextAdView.setLayoutParams(layoutParams);
        }
        AdRequest req = new AdRequest.Builder().build();
        if (nextAdView.getAdUnitId() == null) {
            nextAdView.setAdUnitId(getResources().getString(R.string.admob_app_key));
        }
        Log.d(TAG,"setting ad listener");
        nextAdView.setAdListener(adListener);
        if (nextAdView.getAdSize() == null) {
            Log.d(TAG,"setting ad size");
            nextAdView.setAdSize(AdSize.SMART_BANNER);
        }
        Log.d(TAG,"loading ad");
        nextAdView.loadAd(req);
    }

    com.amazon.device.ads.AdListener amazonAdListener = new com.amazon.device.ads.AdListener() {

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
                        happyAdListener.onAdFailed(failureCount, adError.getCode() == AdError.ErrorCode.NO_FILL);
                    }
                }
            });
        }
    };

    private void SwapAmazonCurrentAd() {
        Log.d(TAG, "SWAP CURRENT AD");
        Activity a = getActivity();
        if (a == null) {
            Log.d(getClass().getSimpleName(), "activity is null, returning");
            return;
        }
        Animation slideDown = AnimationUtils.loadAnimation(
                a, R.anim.slide_down);
        slideDown.setAnimationListener(new Animation.AnimationListener() {

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


    protected void LoadAmazonAd() {
        if (amazonNextAdView == null) { // Create and configure a new ad if the next
            // ad doesn't currently exist
            amazonNextAdView = new AdLayout(getActivity());
            ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            amazonNextAdView.setLayoutParams(layoutParams);
            // Register our ad handler that will receive call-backs for state
            // changes during the ad life cycle
            amazonNextAdView.setListener(amazonAdListener);
        }

        // Load the ad with the appropriate ad options.
        AdTargetingOptions adOptions = new AdTargetingOptions();
        amazonNextAdView.loadAd(adOptions);
    }
}
