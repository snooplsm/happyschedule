package us.wmwm.happyschedule.fragment;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.util.PremiumUserHelper;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdError.ErrorCode;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdTargetingOptions;

public class FragmentAmazonAd extends HappyFragment implements AdListener {

	private ViewGroup adViewContainer; // View group to which the ad view will
										// be added
	private AdLayout currentAdView; // The ad that is currently visible to the
									// user
	private AdLayout nextAdView; // A placeholder for the next ad so we can keep
									// the current ad visible while the next ad
									// loads

	Handler handler = new Handler();
	
	private HappyAdListener happyAdListener;
	
	private static final String TAG = FragmentAmazonAd.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_amazon_ad, container,
				false);
		adViewContainer = (ViewGroup) root;
		return root;
	}
	
	private int failureCount;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("failureCount", failureCount);
	}
	
	public void setHappyAdListener(HappyAdListener happyAdListener) {
		this.happyAdListener = happyAdListener;
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

	private void LoadAd() {
		if (nextAdView == null) { // Create and configure a new ad if the next
									// ad doesn't currently exist
			nextAdView = new AdLayout(getActivity());
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
			nextAdView.setListener(this);
		}

		// Load the ad with the appropriate ad options.
		AdTargetingOptions adOptions = new AdTargetingOptions();
		nextAdView.loadAd(adOptions);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (currentAdView != null)
			this.currentAdView.destroy();
		if (nextAdView != null)
			this.nextAdView.destroy();
		if(newAdFuture!=null) {
			newAdFuture.cancel(true);
		}
	}

	Future<?> newAdFuture;

	Runnable newAdRequest = new Runnable() {
		public void run() {
			handler.post(loadAd);
		};
	};

	Runnable loadAd = new Runnable() {
		public void run() {
			LoadAd();
		};
	};

	@Override
	public void onResume() {
		super.onResume();
		if(newAdFuture!=null) {
			newAdFuture.cancel(true);
		}
		newAdFuture = ThreadHelper.getScheduler().scheduleAtFixedRate(loadAd, 60000, 60000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onPause() {
		super.onPause();
		if(newAdFuture!=null) {
			newAdFuture.cancel(true);
		}
	}

	@Override
	public void onAdCollapsed(AdLayout arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAdExpanded(AdLayout arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAdFailedToLoad(AdLayout arg0, final AdError arg) {
		// TODO Auto-generated method stub
		Log.d(getClass().getSimpleName(),
				"onAdFailedToLoad " + arg.getMessage());
		failureCount++;
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(happyAdListener!=null) {
					happyAdListener.onAdFailed(failureCount, arg.getCode()==ErrorCode.NO_FILL);
				}
			}
		});
		
	}

	@Override
	public void onAdLoaded(AdLayout ad, AdProperties arg1) {
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
				if (currentAdView != null) {
					SwapCurrentAd();
				} else {
					// This is the first time we're loading an ad, so set the
					// current ad view to the ad we just loaded and set the next to null
					// so that we can load a new ad in the background.
					currentAdView = nextAdView;
					nextAdView = null;
					ShowCurrentAd();
				}
				failureCount = 0;
				if(happyAdListener!=null) {
					happyAdListener.onAd();
				}
				Log.d(getClass().getSimpleName(), "onAdLoaded ");
			}
		});
		
	}

	private void SwapCurrentAd() {
		Log.d(TAG,"SWAP CURRENT AD");
		Activity a = getActivity();
		if(a==null) {
			Log.d(getClass().getSimpleName(), "activity is null, returning");
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
		adViewContainer.removeAllViews();
		adViewContainer.removeView(currentAdView);
		AdLayout tmp = currentAdView;
		currentAdView = nextAdView;
		nextAdView = tmp;
		ShowCurrentAd();
	}

	private void ShowCurrentAd() {
		Activity a = getActivity();
		if(a==null) {
			Log.d(getClass().getSimpleName(), "activity is null, returning");
			return;
		}
		adViewContainer.addView(currentAdView);
		Animation slideUp = AnimationUtils.loadAnimation(
				a, R.anim.slide_up);
		currentAdView.startAnimation(slideUp);
	}
}
