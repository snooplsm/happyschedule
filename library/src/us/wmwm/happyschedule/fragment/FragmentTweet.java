package us.wmwm.happyschedule.fragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import twitter4j.Status;
import twitter4j.URLEntity;
import us.wmwm.happyschedule.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.squareup.picasso.Picasso;

public class FragmentTweet extends HappyFragment {

	ImageView avatar;
	TextView name;
	TextView screenname;
	TextView ago;
	TextView text;
	WebView webView;
	
	Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tweet, container, false);
		avatar = (ImageView) view.findViewById(R.id.avatar);
		name = (TextView) view.findViewById(R.id.name);
		ago = (TextView) view.findViewById(R.id.ago);
		screenname = (TextView) view.findViewById(R.id.screenname);
		text = (TextView) view.findViewById(R.id.tweet_text);
		webView = (WebView) view.findViewById(R.id.webview);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();
		Status status = (Status) b.getSerializable("tweet");
		Picasso.with(getActivity())
				.load(status.getUser().getBiggerProfileImageURL()).into(avatar);
		name.setText(status.getUser().getName());
		String url = null;
		SpannableStringBuilder text = new SpannableStringBuilder(status.getText());
		if (status.getURLEntities() != null) {
			for (int i = status.getURLEntities().length - 1; i >= 0; i--) {
				final URLEntity e = status.getURLEntities()[i];
				url = e.getExpandedURL();
				ClickableSpan cs = new ClickableSpan() {
					
					@Override
					public void onClick(View widget) {
						Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(e.getExpandedURL()));
						startActivity(i);
					}
				};
				text.replace(e.getStart(), e.getEnd(), e.getDisplayURL());
				text.setSpan(cs, e.getStart(), e.getStart() + e.getDisplayURL().length(), 0);
			}
		}
		screenname.setText("@" + status.getUser().getScreenName());
		this.text.setAutoLinkMask(Linkify.WEB_URLS|Linkify.PHONE_NUMBERS);
		this.text.setText(text);
		Calendar created = Calendar.getInstance();
		created.setTime(status.getCreatedAt());
		ago.setText(FragmentAlarmPicker.buildMessage(Calendar.getInstance(), created));
		
		WebSettings s = webView.getSettings();
		s.setAppCacheEnabled(true);
		s.setAppCachePath(getActivity().getCacheDir().getAbsolutePath());
		s.setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient() {
			
		});
		webView.setWebViewClient(new WebViewClient() {
			
		});
		if(url!=null) {
			webView.loadUrl(url);
		}
		Map<String,String> k = new HashMap<String,String>();
		k.put("url", String.format("https://twitter.com/%s/status/%s",status.getUser().getScreenName(),status.getId()));
		k.put("status", text.toString());
		FlurryAgent.logEvent("TweetView", k);
		
		FragmentAmazonAd ad = new FragmentAmazonAd();
		ad.setHappyAdListener(new HappyAdListener() {
			@Override
			public void onAd() {
			}

			@Override
			public void onAdFailed(int count, boolean noFill) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							FragmentGoogleAd gad = new FragmentGoogleAd();
							if (getView() != null
									&& getView().findViewById(
											R.id.fragment_tweet_ad) != null) {
								getChildFragmentManager().beginTransaction()
										.replace(R.id.fragment_tweet_ad, gad)
										.commit();
							}
						} catch (Exception e) {

						}
					}
				});
			}
		});
		if (getView() != null
				&& getView().findViewById(R.id.fragment_tweet_ad) != null) {
			try {
				getChildFragmentManager().beginTransaction()
						.replace(R.id.fragment_tweet_ad, ad).commit();
			} catch (Exception e) {

			}
		}
	}

	public static FragmentTweet newInstance(Status status) {
		FragmentTweet t = new FragmentTweet();
		Bundle b = new Bundle();
		b.putSerializable("tweet", status);
		t.setArguments(b);
		return t;
	}

}
