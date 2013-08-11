package us.wmwm.happyschedule.fragment;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdProperties;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class FragmentGoogleAd extends Fragment implements AdListener {

	private ViewGroup adViewContainer; // View group to which the ad view will
										// be added
	private AdView currentAdView; // The ad that is currently visible to the
									// user
	private AdView nextAdView; // A placeholder for the next ad so we can keep
									// the current ad visible while the next ad
									// loads

	Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_amazon_ad, container,
				false);
		adViewContainer = (ViewGroup) root;
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LoadAd();
	}

	private void LoadAd() {
		if (nextAdView == null) { // Create and configure a new ad if the next
									// ad doesn't currently exist
			try {
				nextAdView = new AdView(getActivity(),AdSize.SMART_BANNER,getString(R.string.admob_app_key));
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
			nextAdView.setAdListener(this);
		}

		// Load the ad with the appropriate ad options.
		nextAdView.loadAd(new AdRequest());
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
		newAdFuture = ThreadHelper.getScheduler().scheduleAtFixedRate(newAdRequest, 10000, 10000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onPause() {
		super.onPause();
		if(newAdFuture!=null) {
			newAdFuture.cancel(true);
		}
	}

	@Override
	public void onReceiveAd(Ad arg0) {
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
		Log.d(getClass().getSimpleName(), "onAdLoaded ");
	}

	private void SwapCurrentAd() {
		Activity a = getActivity();
		if(a==null) {
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
		if(a==null) {
			return;
		}
		adViewContainer.addView(currentAdView);
		Animation slideUp = AnimationUtils.loadAnimation(
				a, R.anim.slide_up);
		currentAdView.startAnimation(slideUp);
	}

	@Override
	public void onDismissScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}
}

