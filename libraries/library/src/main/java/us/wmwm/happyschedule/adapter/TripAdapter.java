package us.wmwm.happyschedule.adapter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.drawable.ProgressView;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.TripInfo;
import us.wmwm.happyschedule.model.TripInfo.Stop;
import us.wmwm.happyschedule.views.TripView;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TripAdapter extends BaseAdapter {

    private static final String TAG = TripAdapter.class.getSimpleName();

	private Schedule schedule;

	private Calendar startCal;

	List<TripInfo.Stop> stops;

	DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);

	public TripAdapter(Context context) {
		super();
	}

	public TripAdapter(Context context, List<TripInfo.Stop> stops,
			Schedule schedule, Calendar startCal) {
		super();
		Log.d(TAG, startCal.getTime().toString());
		this.stops = stops;
		this.schedule = schedule;
		this.startCal = startCal;
		this.startCal.set(Calendar.HOUR_OF_DAY, 0);
		this.startCal.set(Calendar.MINUTE, 0);
		this.startCal.set(Calendar.SECOND, 0);
		this.startCal.set(Calendar.MILLISECOND, 0);
	}

	@Override
	public TripInfo.Stop getItem(int position) {
		return stops.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TripView v = (TripView) convertView;
		ProgressView progress = null;
		if (v == null) {
			v = new TripView(parent.getContext());
			v.setBackgroundDrawable(new ProgressView());
		}
		progress = (ProgressView) v.getBackground();

		Stop sts = getItem(position);
		Calendar departCalendar = Calendar.getInstance();
		Calendar arriveCalendar = Calendar.getInstance();
		departCalendar.setTimeInMillis(sts.depart.getTimeInMillis());

		if (position < getCount() - 1) {
			Stop next = getItem(position + 1);
			arriveCalendar.setTimeInMillis(next.arrive.getTimeInMillis());
		} else {
			arriveCalendar.setTimeInMillis(sts.arrive.getTimeInMillis());
		}

		Calendar dCal = Calendar.getInstance();
		dCal.setTimeInMillis(startCal.getTimeInMillis());
		dCal.add(Calendar.DAY_OF_YEAR,
				departCalendar.get(Calendar.DAY_OF_YEAR) - 1);
		dCal.add(Calendar.HOUR_OF_DAY, departCalendar.get(Calendar.HOUR_OF_DAY));
		dCal.add(Calendar.MINUTE, departCalendar.get(Calendar.MINUTE));
		Calendar aCal = Calendar.getInstance();
		aCal.setTimeInMillis(startCal.getTimeInMillis());
		aCal.add(Calendar.DAY_OF_YEAR,
				arriveCalendar.get(Calendar.DAY_OF_YEAR) - 1);
		aCal.add(Calendar.HOUR_OF_DAY, arriveCalendar.get(Calendar.HOUR_OF_DAY));
		aCal.add(Calendar.MINUTE, arriveCalendar.get(Calendar.MINUTE));
		// long arrive = sts.arrive.getTimeInMillis();
		if (dCal.getTimeInMillis() > System.currentTimeMillis()) {
			progress.setPercent(0);
			Log.d(TAG,"0 percent");
		} else {
			long max = aCal.getTimeInMillis() - dCal.getTimeInMillis();
			long curr = aCal.getTimeInMillis() - System.currentTimeMillis();
			float percent = 1;
			if (curr <= 0) {
				percent = 1;
			} else {
				percent = 1.0f - (curr / (float) max);
			}
			Log.d(TAG,Math.min((float) 1, percent) + " percent " + curr);
			progress.setPercent(Math.min((float) 1, percent));
		}
		TextView time = (TextView) v.findViewById(R.id.time);
		time.setText(sts.name);
		String dpt = TIME.format(sts.depart.getTime()).toLowerCase();
		TextView timeDescriptor = (TextView) v
				.findViewById(R.id.time_descriptor);
		timeDescriptor.setText(dpt);
		return v;
	}

	@Override
	public int getCount() {
		return stops.size();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

    public List<Stop> getStops() {
        return this.stops;
    }
}
