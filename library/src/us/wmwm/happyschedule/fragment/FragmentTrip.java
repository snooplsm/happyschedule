package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.adapter.TripAdapter;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TripInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FragmentTrip extends HappyFragment {

	private String[] tripIds;

	private Station depart;
	private Station arrive;

	private Schedule schedule;

	private long start;

	private ListView list;

	TripAdapter adapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();
		depart = (Station) b.getSerializable("depart");
		arrive = (Station) b.getSerializable("arrive");
		String id = b.getString("tripId");
		// loadSchedule();
		// getSupportActionBar().setTitle("Trip #" + id);
		// getSupportActionBar().setSubtitle(
		// schedule.stopIdToName.get(schedule.departId) + " to "
		// + schedule.stopIdToName.get(schedule.arriveId));

		StationInterval interval = schedule.getStationIntervalForTripId(id);
		Stack<StationInterval> intervals = new Stack<StationInterval>();
		List<TripInfo.Stop> stops = new ArrayList<TripInfo.Stop>();
		intervals.push(interval);
		TripInfo last;
		StationInterval lastInterval = null;
		while (!intervals.isEmpty()) {
			interval = intervals.pop();
			if (interval.tripId != null) {
				TripInfo tripInfo = ScheduleDao.get().getStationTimesForTripId(
						interval.tripId, interval.departSequence,
						interval.arriveSequence);
				last = tripInfo;
				stops.addAll(tripInfo.stops);
				lastInterval = interval;
			}
			if (interval.hasNext()) {
				intervals.push(interval.next());
			} else {
			}
			// System.out.println(stops);
			last = ScheduleDao.get().getStationTimesForTripId(lastInterval.tripId,
					interval.arriveSequence - 1, Integer.MAX_VALUE);
			// View finalStop = LayoutInflater.from(this).inflate(
			// R.layout.final_stop, null);
			// TextView t = (TextView) finalStop.findViewById(R.id.text);
			// Object c = last.stops.get(last.stops.size() - 1);
			// t.setText("last stop " + c.toString());
			// listView.addHeaderView(finalStop, null, false);

			start = b.getLong("start", 0);
			Calendar startCal = Calendar.getInstance();
			startCal.setTimeInMillis(start);
			adapter = new TripAdapter(getActivity(), stops, schedule, startCal);
			list.setAdapter(adapter);
		}

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_trips, container,false);
		list = (ListView) view.findViewById(R.id.list);
		return view;
	}

	public static FragmentTrip newInstance(Station depart, Station arrive,
			StationToStation sts, Schedule schedule) {
		Bundle b = new Bundle();
		b.putSerializable("depart", depart);
		b.putSerializable("arrive", arrive);
		b.putString("tripId", sts.tripId);
		FragmentTrip t = new FragmentTrip();
		t.setArguments(b);
		t.schedule = schedule;
		return t;
	}

}
