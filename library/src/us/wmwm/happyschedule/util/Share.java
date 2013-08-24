package us.wmwm.happyschedule.util;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Share {
	
	static SimpleDateFormat HOUR = new SimpleDateFormat("h");
	static SimpleDateFormat MINUTE = new SimpleDateFormat("mm");
	static SimpleDateFormat AMPM = new SimpleDateFormat("a");
	static SimpleDateFormat DATE = new SimpleDateFormat("MM/dd/yyyy");

	public static Intent intent(Context ctx, StationInterval sts) {
		Intent i = new Intent();
		i.setType("text/plain");
		StringBuilder title = new StringBuilder("#njrails ").append("#").append(sts.tripId).append(" ").append(sts.schedule.stopIdToName.get(sts.departId)).append(" to ").append(sts.schedule.stopIdToName.get(sts.arriveId));	
		i.putExtra(Intent.EXTRA_TITLE, title.toString());
		return i;
	}
	
	public static Intent intent(AppConfig config, Context ctx, Station from, Station to, Date day) {
		Intent i = new Intent();
		i.setType("text/plain");
		StringBuilder title = new StringBuilder();	
		title.append(from.getName()).append(" to ").append(to.getName()).append(" ");
		try {
			String url = null;
			if(from.getAlternateId()==null || to.getAlternateId()==null) {
				url = config.getShareTrip().replaceAll(":fromName", URLEncoder.encode(from.getName(),"utf-8")).replaceAll(":toName", URLEncoder.encode(to.getName(),"utf-8")).replaceAll(":fromLat", from.getLat()).replaceAll(":fromLng", from.getLng()).replaceAll(":toLat", from.getLat()).replaceAll(":toLng", from.getLng()).replaceAll(":hour", HOUR.format(day)).replaceAll(":minute", MINUTE.format(day)).replaceAll(":ampm", AMPM.format(day)); 
			} else { 
				url = config.getShareDay().replaceAll(":fromName", URLEncoder.encode(from.getName(),"utf-8")).replaceAll(":toName", URLEncoder.encode(to.getName(),"utf-8")).replaceAll(":from", from.getAlternateId()).replaceAll(":to", to.getAlternateId());
			}
			url = url.replace(":day", DATE.format(day));
			title.append(url);
		} catch (Exception e) {
			Log.e("Share", "can't make url", e);
		}
		title.append(" via @nj_rails");
		i.putExtra(Intent.EXTRA_SUBJECT, title.toString());
		i.putExtra(Intent.EXTRA_TEXT, title.toString());
		return i;
	}
	
}
