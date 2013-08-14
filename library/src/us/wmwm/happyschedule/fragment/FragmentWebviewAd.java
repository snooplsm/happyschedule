package us.wmwm.happyschedule.fragment;

import us.wmwm.happyschedule.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class FragmentWebviewAd extends FragmentHappyAd {

	WebView webView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(
				R.layout.fragment_webview_ad, container, false);
		webView = (WebView) root.findViewById(R.id.webview);
		this.root = root;
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		webView.loadUrl(ad.getWebviewUrl());
	}

}
