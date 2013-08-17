package us.wmwm.happyschedule.fragment;

import us.wmwm.happyschedule.R;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentWebviewAd extends FragmentHappyAd {

	WebView webView;
	View discard;
	


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(
				R.layout.fragment_webview_ad,null);
		webView = (WebView) root.findViewById(R.id.webview);
		discard = root.findViewById(R.id.discard);
		this.root = root;
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		webView.loadUrl(ad.getWebviewUrl());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				String js = "javascript:" + "document.body.style.background = #" + Integer.toHexString(getResources().getColor(R.color.windowBackground))+";";
				System.out.println(js);
				webView.loadUrl(js);
			}
		});
		if(!TextUtils.isEmpty(ad.getDiscardKey())) {
			discard.setVisibility(View.VISIBLE);
			discard.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					discardListener.onDiscard(ad);
				}
			});
		} else {
			discard.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		webView.stopLoading();
		webView.destroy();
	}

}
