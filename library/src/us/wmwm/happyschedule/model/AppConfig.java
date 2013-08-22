package us.wmwm.happyschedule.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import us.wmwm.happyschedule.dao.WDb;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.text.TextUtils;

public class AppConfig {
	
	List<AppAd> ads;
	List<LineStyle> lines;
	String departureVision;
	
	String shareDay;
	String shareTrip;

	public AppConfig(JSONObject o) {
		ads = new ArrayList<AppAd>();
		if(o.has("ads")) {
			JSONArray ads = o.optJSONArray("ads");
			for(int i = 0; i < ads.length(); i++) {
				this.ads.add(new AppAd(ads.optJSONObject(i)));
			}
		}
		lines = new ArrayList<LineStyle>();
		if(o.has("lines")) {
			JSONArray lines = o.optJSONArray("lines");
			for(int i = 0; i < lines.length(); i++) {
				this.lines.add(new LineStyle(lines.optJSONObject(i)));
			}
		}
		JSONObject share = o.optJSONObject("share");
		if(share!=null) {
			shareDay = share.optString("day");
			shareTrip = share.optString("trip");
		}
		departureVision = o.optString("departureVision");
	}
	
	public String getShareDay() {
		return shareDay;
	}
	
	public String getShareTrip() {
		return shareTrip;
	}
	
	public String getDepartureVision() {
		return departureVision;
	}
	
	public AppConfig() {}
	
	public List<LineStyle> getLines() {
		return lines;
	}

	public void setLines(List<LineStyle> lines) {
		this.lines = lines;
	}

	public void setAds(List<AppAd> ads) {
		this.ads = ads;
	}

	public List<AppAd> getAds() {
		return ads;
	}

	public AppAd getBestAd(Context ctx) {
		List<AppAd> ads = getAds();
		if(ads==null) {
			return null;
		}
		for(AppAd ad : ads) {
			if(!ad.isEnabled()) {
				continue;
			}
			Calendar start = ad.getStart();
			if(start==null || start.before(Calendar.getInstance())) {
				
			} else {
				continue;
			}
			Calendar end = ad.getEnd();
			if(end==null || end.after(Calendar.getInstance())) {
				
			} else {
				continue;
			}
			if(ad.getBeforeVersion()!=null) {
				int version = ad.getBeforeVersion();
				try {
					int appVersion = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
					if(appVersion >= version) {
						continue;
					}
				} catch (Exception e) {}
			}
			if(!TextUtils.isEmpty(ad.getDiscardKey())) {
				if(WDb.get().getPreference("discard_"+ad.getDiscardKey())!=null) {
					continue;
				}
			}
			return ad;
		}
		return null;
	}
	
}
