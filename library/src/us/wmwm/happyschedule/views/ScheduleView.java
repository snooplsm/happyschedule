package us.wmwm.happyschedule.views;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.Station;
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
	RelativeLayout.LayoutParams trainInfoContainerParams;

	static DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);

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

		// if(mins<-5) {
		// setBackgroundColor(0xFFCCCCCC);
		// } else {
		// setBackground(bg);
		// }
		if (!TextUtils.isEmpty(sts.tripId)) {
			train.setText("#" + sts.tripId);
		} else {
			train.setText("");
		}
		duration.setText(((sts.arriveTime.getTimeInMillis() - sts.departTime
				.getTimeInMillis()) / 60000) + " minutes");
		
		this.depart.setText(depart.getName() + " to " + arrive.getName());
		
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
