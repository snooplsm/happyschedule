package us.wmwm.happyschedule.fragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Future;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.util.Streams;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.okhttp.OkHttpClient;

public class FragmentWebviewAd extends FragmentHappyAd {

	WebView webView;
	View discard;
	
	Future<?> loadData;
	
	Handler handler = new Handler();
	
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
	
	private void loadData() {
		if(loadData!=null) {
			loadData.cancel(true);
		}
		loadData = ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				OkHttpClient c = new OkHttpClient();				
				HttpURLConnection conn = null;
				InputStream is = null;
				try {
					conn = c.open(new URL(ad.getWebviewUrl()));
					is = conn.getInputStream();
					final String data = Streams.readFully(is);
					handler.post(new Runnable() {
						@Override
						public void run() {
							webView.loadDataWithBaseURL(ad.getWebviewUrl(), data, "text/html", "utf-8", ad.getWebviewUrl());
						}
					});
				} catch (Exception e) {
					handler.post(new Runnable() {
						public void run() {
							onAdFailed(1, false);
						};
					});
				} finally {
					if(conn!=null) {
						conn.disconnect();
					}
					if(is!=null) {
						try {
							is.close();
						} catch (Exception e) {
							
						}
					}
				}
			}
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAppCacheEnabled(true);
		webView.getSettings().setAppCachePath(getActivity().getCacheDir().getAbsolutePath());
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				String js = "javascript:" + "document.body.style.background = #" + Integer.toHexString(getResources().getColor(R.color.windowBackground))+";";
				System.out.println(js);
				webView.loadUrl(js);
				onAd();
			}						
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				if(failingUrl.equals(ad.getWebviewUrl())) {
					webView.loadData(String.format("<html><body bgcolor=\"#%s\"></body></html>", Integer.toHexString(getResources().getColor(R.color.windowBackground) - 0xFF000000)), "text/html", "utf-8");
					onAdFailed(1, false);
				}				
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
		loadData();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		webView.stopLoading();
		webView.destroy();
	}

}
