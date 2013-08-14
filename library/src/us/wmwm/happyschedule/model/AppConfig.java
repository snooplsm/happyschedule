package us.wmwm.happyschedule.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class AppConfig {
	
	List<AppAd> ads;
	List<LineStyle> lines;

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
	
}
