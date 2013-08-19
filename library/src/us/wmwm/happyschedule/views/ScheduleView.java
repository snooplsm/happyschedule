package us.wmwm.happyschedule.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TrainStatus;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScheduleView extends RelativeLayout {

	StationToStation sts;

	TextView time;
	TextView status;
	TextView timeTillDepart;
	TextView duration;
	TextView train;
	TextView track;
	TextView track2;
	View lineIndicator;
	TextView depart;
	View alarm;
	View trainInfoContainer;
	TextView connections;
	RelativeLayout.LayoutParams trainInfoContainerParams;

	static DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
	public static DateFormat times = new SimpleDateFormat("h:mm aa");

	Drawable bg;

	public ScheduleView(Context context) {
		this(context, null, 0);
	}

	public ScheduleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.view_schedule, this);
		time = (TextView) findViewById(R.id.time);
		status = (TextView) findViewById(R.id.status);
		timeTillDepart = (TextView) findViewById(R.id.departs_in);
		duration = (TextView) findViewById(R.id.duration);
		train = (TextView) findViewById(R.id.trip_id);
		
		track = (TextView) findViewById(R.id.track);
		track2 = (TextView) findViewById(R.id.track2);
		alarm = findViewById(R.id.alarm);
		depart = (TextView) findViewById(R.id.depart);
		connections = depart;
		lineIndicator = findViewById(R.id.line_indicator);
		trainInfoContainer = findViewById(R.id.train_info_container);
		trainInfoContainerParams = (RelativeLayout.LayoutParams) trainInfoContainer.getLayoutParams();
		bg = getBackground();
	}

	public ScheduleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public void setData(StationToStation sts, Station depart, Station arrive) {
		this.sts = sts;
		train.setVisibility(View.VISIBLE);
		time.setText(shrink(sts.departTime) + " - " + shrink(sts.arriveTime));

		Calendar cal = Calendar.getInstance();
		long diff = sts.departTime.getTimeInMillis() - cal.getTimeInMillis();
		long mins = diff / 60000;
		if (mins >= 0 && mins < 100) {
			timeTillDepart.setVisibility(View.VISIBLE);
			timeTillDepart.setText("departs in " + mins + " min");
		} else {
			timeTillDepart.setVisibility(View.GONE);
			timeTillDepart.setText("");
		}

		if (!TextUtils.isEmpty(sts.tripId)) {
			train.setText("#" + sts.tripId);
		} else {
			train.setText("");
		}
		duration.setText(((sts.arriveTime.getTimeInMillis() - sts.departTime
				.getTimeInMillis()) / 60000) + " minutes");
		
		this.depart.setText(depart.getName() + " to " + arrive.getName());
		if(sts instanceof StationInterval) {
			StationInterval sts2 = (StationInterval) sts;
			train.setVisibility(View.GONE);
			duration.setText(duration(sts2));
			time.setText(shrink(sts.departTime) + " - " + shrink(sts2.getArriveTime()));
			if (sts2.schedule.transfers.length > 1) {
				populateConnections(sts2);
			} else {
				populateExtraInfo(sts2);
			}
		} else {
			time.setText(shrink(sts.departTime) + " - " + shrink(sts.arriveTime));
		}
		
	}
	
	private void populateExtraInfo(StationInterval sts) {
		StringBuilder b = new StringBuilder();
		if (sts.routeId != null) {
			b.append(sts.schedule.routeIdToName.get(sts.routeId));
			b.append(" ");
		}
		if (sts.blockId != null && sts.blockId.trim().length() > 0) {
			b.append("#").append(sts.blockId);
		}
		connections.setText(b);
	}
	
	private String duration(StationInterval sts) {
		StationInterval sts2 = sts;
		while (sts2.hasNext()) {
			// TODO: optimize by just jumping to the end...
			sts2 = sts2.next();
		}
		long diff = sts2.arriveTime.getTimeInMillis()
				- sts.departTime.getTimeInMillis();
		return "" + diff / 60000 + " minutes";
	}
	
	private void populateConnections(StationInterval sts2) {
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
				String depart = times.format(sts2.departTime.getTime())
						.toLowerCase();
				depart = depart.substring(0, depart.length() - 1).replace(" ",
						"");
				if(depart.length()<6) {
					//b.append("  ");
				}
				b.append(depart);
				b.append(" ");
				
				b.append(sts2.schedule.stopIdToName.get(sts2.departId));
				b.append(" ");
				b.append("↝\n");

				if (!(sts2.tripId != null & sts2.tripId.equals(nextTripId))) {
					String arrive = times.format(sts2.arriveTime.getTime())
							.toLowerCase();
					arrive = arrive.substring(0, arrive.length() - 1).replace(
							" ", "");					
					
					if(arrive.length()<6) {
						//b.append("  ");
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
			String depart = times.format(sts2.arriveTime.getTime())
					.toLowerCase();
			depart = depart.substring(0, depart.length() - 1).replace(" ", "");
			if(depart.length()<6) {
				//b.append(" ");
			}
			b.append(depart);			
			b.append(" ");
			b.append(sts2.schedule.stopIdToName.get(sts2.departId));
			b.append(" ");
			b.append("↝\n");
		}
		String arrive = times.format(sts2.getArriveTime().getTime())
				.toLowerCase();
		arrive = arrive.substring(0, arrive.length() - 1).replace(" ", "");
		if(arrive.length()<6) {
			//b.append(" ");
		}
		b.append(arrive);
		b.append(" ");
		b.append(sts2.schedule.stopIdToName.get(sts2.arriveId));
		if (sts2.blockId != null && sts2.blockId.trim().length() > 0) {
			b.append(" #").append(sts2.blockId);
		}
		connections.setText(b.toString());
	}

	public void setAlarm(List<Alarm> alarm) {
		if (alarm == null) {
			this.alarm.setVisibility(View.GONE);
		} else {
			this.alarm.setVisibility(View.VISIBLE);
		}
	}

	private String shrink(Calendar cal) {
		return TIME.format(cal.getTime()).toLowerCase();// .replace(" am",
														// "a").replace(" pm",
														// "p");
	}

	public void setStatus(TrainStatus trainStatus) {
		track.setVisibility(View.GONE);
		status.setVisibility(View.GONE);
		trainInfoContainerParams.addRule(RelativeLayout.BELOW, R.id.train_info);
		if (trainStatus != null) {
			if(!TextUtils.isEmpty(trainStatus.getStatus())) {
				status.setVisibility(View.VISIBLE);
				status.setText(trainStatus.getStatus());
				trainInfoContainerParams.addRule(RelativeLayout.BELOW, R.id.train_info);
			} else {
				status.setVisibility(View.GONE);
				trainInfoContainerParams.addRule(RelativeLayout.BELOW, R.id.track);
			}
			
			if(!TextUtils.isEmpty(trainStatus.getTrack()) ) {
				track.setVisibility(View.VISIBLE);
				track.setText(trainStatus.getTrack().trim());
			} else {
				track.setVisibility(View.INVISIBLE);
			}
		} else {
			status.setVisibility(View.GONE);
		}
	}
}
