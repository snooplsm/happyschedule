package us.wmwm.happyschedule.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import us.wmwm.happyschedule.dao.WDb;
import android.content.Context;
import android.text.TextUtils;

public class AppConfig {

	List<AppAd> ads;
	List<LineStyle> lines;
	String departureVision;

	String shareDay;
	String shareTrip;

	List<AppRailLine> railLines;

	public AppConfig(JSONObject o) {
		ads = new ArrayList<AppAd>();
		if (o.has("ads")) {
			JSONArray ads = o.optJSONArray("ads");
			for (int i = 0; i < ads.length(); i++) {
				this.ads.add(new AppAd(ads.optJSONObject(i)));
			}
		}
		lines = new ArrayList<LineStyle>();
		if (o.has("lines")) {
			JSONArray lines = o.optJSONArray("lines");
			for (int i = 0; i < lines.length(); i++) {
				this.lines.add(new LineStyle(lines.optJSONObject(i)));
			}
		}
		JSONObject share = o.optJSONObject("share");
		if (share != null) {
			shareDay = share.optString("day");
			shareTrip = share.optString("trip");
		}
		JSONObject departureVision = o.optJSONObject("departure_vision");
		if (departureVision != null) {
			this.departureVision = departureVision.optString("url");
		}
		railLines = new ArrayList<AppRailLine>();
		if (o.has("rail_lines")) {
			JSONArray railLines = o.optJSONArray("rail_lines");
			for (int i = 0; i < railLines.length(); i++) {
				AppRailLine r = new AppRailLine(railLines.optJSONObject(i));
				this.railLines.add(r);
			}
			Collections.sort(this.railLines);

		}
	}

	public AppConfig() {
		ads = Collections.emptyList();
		lines = Collections.emptyList();
		departureVision = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=$stop_id";
		shareDay = "http://www.njtransit.com/sf/sf_servlet.srv?hdnPageAction=TrainSchedulesFrom&selOrigin=:from&selDestination=:to&OriginDescription=:fromName&DestDescription=:toName&datepicker=:day";
		shareTrip = "http://www.njtransit.com/sf/sf_servlet.srv?hdnPageAction=TripPlannerItineraryResultsEmailTo&StartAddress=:fromName&EndAddress=:toName&TravelFromLatLong=:fromLat,:fromLng&TravelToLatLong=:toLat,:toLng&Date=:day&ArrDep=D&Hour=:hour&Minute=:minute&AmPm=:ampm&Mode=BCTLXR&Minimize=T&WalkDistance=1.00&Atr=N&KeepThis=true&TB_iframe=true&height=125&width=300";
	}

	public static final AppConfig DEFAULT;

	static {
		DEFAULT = new AppConfig();

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
		if (ads == null) {
			return null;
		}
		for (AppAd ad : ads) {
			if (!ad.isEnabled()) {
				continue;
			}
			Calendar start = ad.getStart();
			if (start == null || start.before(Calendar.getInstance())) {

			} else {
				continue;
			}
			Calendar end = ad.getEnd();
			if (end == null || end.after(Calendar.getInstance())) {

			} else {
				continue;
			}
			int appVersion = 0;
			try {
				appVersion = ctx.getPackageManager().getPackageInfo(
						ctx.getPackageName(), 0).versionCode;
			} catch (Exception e) {

			}
			if (ad.getBeforeVersion() != null) {
				int version = ad.getBeforeVersion();
				if (appVersion >= version) {
					continue;
				}
			}
			if (ad.getAfterVersion() != null) {
				int version = ad.getAfterVersion();
				if (appVersion <= version) {
					continue;
				}
			}
			if (!TextUtils.isEmpty(ad.getDiscardKey())) {
				if (WDb.get().getPreference("discard_" + ad.getDiscardKey()) != null) {
					continue;
				}
			}
			return ad;
		}
		return null;
	}
	
	public List<AppRailLine> getRailLines() {
		return railLines;
	}

}
