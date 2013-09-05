package us.wmwm.happyschedule.util;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.views.ScheduleView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Share {

	static SimpleDateFormat HOUR = new SimpleDateFormat("h");
	static SimpleDateFormat MINUTE = new SimpleDateFormat("mm");
	static SimpleDateFormat MONTH = new SimpleDateFormat("MM");
	static SimpleDateFormat DAY = new SimpleDateFormat("dd");
	static SimpleDateFormat AMPM = new SimpleDateFormat("a");
	static SimpleDateFormat YEAR = new SimpleDateFormat("yyyy");
	static SimpleDateFormat DATE = new SimpleDateFormat("MM/dd/yyyy");

	public static Intent intent(Context ctx, StationInterval sts2) {

		Intent i = new Intent(Intent.ACTION_SEND);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		StringBuilder b = new StringBuilder();
		boolean added = false;
		String lastTripId = null;
		String nextTripId = null;
		while (sts2.hasNext()) {
			boolean isTransfer = sts2.isTransfer();
			nextTripId = sts2.next().tripId;
			if (isTransfer
					|| (sts2.tripId != null && sts2.tripId.equals(lastTripId))) {
				added = false;

			} else {
				added = true;
				String depart = ScheduleView.times.format(
						sts2.departTime.getTime()).toLowerCase();
				depart = depart.substring(0, depart.length() - 1).replace(" ",
						"");
				if (depart.length() < 6) {
					// b.append("  ");
				}
				b.append(depart);
				b.append(" ");

				b.append(sts2.schedule.stopIdToName.get(sts2.departId));
				b.append(" ");
				b.append("↝\n");

				if (!(sts2.tripId != null & sts2.tripId.equals(nextTripId))) {
					String arrive = ScheduleView.times.format(
							sts2.arriveTime.getTime()).toLowerCase();
					arrive = arrive.substring(0, arrive.length() - 1).replace(
							" ", "");

					if (arrive.length() < 6) {
						// b.append("  ");
					}
					b.append(arrive);
					b.append(" ");
					b.append(sts2.schedule.stopIdToName.get(sts2.arriveId));

					if (sts2.blockId != null
							&& sts2.blockId.trim().length() > 0) {
						b.append(" #");
						b.append(sts2.blockId);
					}

				}
			}

			if (sts2.hasNext()) {
				lastTripId = sts2.tripId;
				sts2 = sts2.next();
				if (added) {
					b.append(" ");
					if (sts2.tripId != null && sts2.tripId.equals(lastTripId)) {

					} else {
						if (sts2.isTransfer()) {
							b.append("↻\n");
						} else {
							b.append("↝\n");
						}
					}

				}

			} else {
				break;
			}
		}

		if (sts2.tripId != null && !sts2.tripId.equals(lastTripId)) {
			String depart = ScheduleView.times
					.format(sts2.departTime.getTime()).toLowerCase();
			depart = depart.substring(0, depart.length() - 1).replace(" ", "");
			if (depart.length() < 6) {
				// b.append(" ");
			}
			b.append(depart);
			b.append(" ");
			b.append(sts2.schedule.stopIdToName.get(sts2.departId));
			b.append(" ");
			b.append("↝\n");
		}
		String arrive = ScheduleView.times.format(
				sts2.getArriveTime().getTime()).toLowerCase();
		arrive = arrive.substring(0, arrive.length() - 1).replace(" ", "");
		if (arrive.length() < 6) {
			// b.append(" ");
		}
		b.append(arrive);
		b.append(" ");
		b.append(sts2.schedule.stopIdToName.get(sts2.arriveId));
		if (sts2.blockId != null && sts2.blockId.trim().length() > 0) {
			b.append(" #").append(sts2.blockId);
		}
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, "#NJRails Schedule");
		i.putExtra(Intent.EXTRA_TEXT, b.toString());	
		return i;
	}

	public static Intent intent(AppConfig config, Context ctx, Station from,
			Station to, Date day) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		StringBuilder title = new StringBuilder();
		title.append(from.getName()).append(" to ").append(to.getName())
				.append(" ");
		try {
			String url = null;
			if (from.getAlternateId() == null || to.getAlternateId() == null) {
				url = config
						.getShareTrip()
						.replaceAll(":fromName",
								URLEncoder.encode(from.getName(), "utf-8"))
						.replaceAll(":toName",
								URLEncoder.encode(to.getName(), "utf-8"))
						.replaceAll(":fromLat", from.getLat())
						.replaceAll(":fromLng", from.getLng())
						.replaceAll(":toLat", from.getLat())
						.replaceAll(":toLng", from.getLng())
						.replaceAll(":hour", HOUR.format(day))
						.replaceAll(":minute", MINUTE.format(day))
						.replaceAll(":ampm", AMPM.format(day))
						.replaceAll(":month", MONTH.format(day))
						.replaceAll(":day", DAY.format(day))
						.replaceAll(":year", YEAR.format(day))
						.replaceAll(":datepicker", DATE.format(day));
			} else {
				url = config
						.getShareDay()
						.replaceAll(":fromName",
								URLEncoder.encode(from.getName(), "utf-8"))
						.replaceAll(":toName",
								URLEncoder.encode(to.getName(), "utf-8"))
						.replaceAll(":from", from.getAlternateId())
						.replaceAll(":to", to.getAlternateId());
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
	
	public static Intent intent(Activity ctx) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, (ctx.getString(R.string.app_name)));
		i.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id="+ctx.getPackageName());
		return i;
	}

}
