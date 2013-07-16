package us.wmwm.happyschedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FragmentDaySchedule extends Fragment {

	ListView list;
	
	Future<?> loadScheduleFuture;
	
	Station from;
	Station to;
	Handler handler = new Handler();
	
	private static final String TAG = FragmentDaySchedule.class.getSimpleName();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_day_schedule, container,false);
		list = (ListView) view.findViewById(R.id.list);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle b = getArguments();
		from = (Station) b.getSerializable("from");
		to = (Station) b.getSerializable("to");
		loadSchedule();
	}
	
	List<Object> o = new ArrayList<Object>();
	
	private void loadSchedule() {
		if (loadScheduleFuture != null) {
			loadScheduleFuture.cancel(true);
		}
		Runnable load = new Runnable() {
			@Override
			public void run() {
				Calendar oneDayPrior = Calendar.getInstance();
				oneDayPrior.add(Calendar.DAY_OF_YEAR, -1);
				Calendar tomorrow = Calendar.getInstance();
				tomorrow.add(Calendar.DAY_OF_YEAR, 1);
				Schedule schedule = null;
				try {
					schedule = ScheduleDao.get().getSchedule(from.id, to.id,
							oneDayPrior.getTime(), tomorrow.getTime());
					o.clear();
					schedule.inOrderTraversal(new ScheduleTraverser() {
						
						@Override
						public void populateItem(int index, StationToStation stationToStation,
								int total) {
							o.add(stationToStation.departTime.getTime());
						}
					});
					handler.post(populateAdpter);
					Log.i(TAG, "SUCCESSFUL SCHEDULE");
				} catch (Exception e) {
					Log.e(TAG, "UNSUCCESSFUL SCHEDULE", e);
				}

			}
		};
		loadScheduleFuture = ThreadHelper.getScheduler().submit(load);
	}
	
	Runnable populateAdpter = new Runnable() {
		@Override
		public void run() {			
			Activity activity = getActivity();
			if(activity==null) {
				return;
			}
			list.setAdapter(new ArrayAdapter(activity,android.R.layout.simple_list_item_1,o));
		}
	};

	@Override
	public void onDestroy() {
		handler.removeCallbacks(populateAdpter);
		if(loadScheduleFuture!=null) {
			loadScheduleFuture.cancel(true);
		}
		super.onDestroy();
		
		
	}
	
	public static FragmentDaySchedule newInstance(Station from, Station to, Date date) {
		FragmentDaySchedule f = new FragmentDaySchedule();
		Bundle b = new Bundle();
		b.putSerializable("from", from);
		b.putSerializable("to", to);
		b.putSerializable("date", date);
		f.setArguments(b);
		return f;
	}
	
}
