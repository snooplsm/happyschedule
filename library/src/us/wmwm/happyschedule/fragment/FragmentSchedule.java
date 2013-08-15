package us.wmwm.happyschedule.fragment;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.views.ScheduleControlsView.ScheduleControlListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.timessquare.CalendarPickerView.OnDateSelectedListener;

public class FragmentSchedule extends Fragment {

	ViewPager pager;

	View loadingContainer;

	Station from;

	Station to;

	Handler handler = new Handler();
	
	FragmentScheduleAdapter adapter;
	
	ScheduleControlListener controlListener = new ScheduleControlListener() {
		
		@Override
		public void onTrips(Schedule schedule, StationToStation stationToStation) {
			FragmentTrip t = FragmentTrip.newInstance(from, to, stationToStation, schedule);
			getFragmentManager().beginTransaction().replace(R.id.fragment_date_picker, t).addToBackStack(null).commit();
		}
		
		@Override
		public void onTimerCancel(Alarm alarm) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onTimer() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onPin() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onFavorite() {
			// TODO Auto-generated method stub
			
		}
	};

	private static final String TAG = FragmentSchedule.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_schedule, container,
				false);
		pager = (ViewPager) view.findViewById(R.id.pager2);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_schedule, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.menu_change_day) {
			final FragmentDatePicker picker = FragmentDatePicker.newInstance(adapter.getCalendar(pager.getCurrentItem()).getTime());
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			picker.setOnDateSelectedListener(new OnDateSelectedListener() {
				
				@Override
				public void onDateSelected(Date date) {
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.remove(picker);
					ft.commit();
					pager.setCurrentItem(adapter.getPositionFor(date));
				}
			});
			ft.replace(R.id.fragment_date_picker, picker);
			ft.addToBackStack(null);
			ft.commit();
			//picker.show(getFragmentManager(), "datePicker");
		}
		pager.setOffscreenPageLimit(0);
		if(item.getItemId()==R.id.menu_go_to_today) {
			pager.setCurrentItem(adapter.getTodaysPosition());
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();
		from = (Station) b.getSerializable("from");
		to = (Station) b.getSerializable("to");
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				pager.setAdapter(adapter  = new FragmentScheduleAdapter(from,to,getFragmentManager()));
				adapter.setControlListener(controlListener);
				pager.setCurrentItem(adapter.getTodaysPosition());		
			}
		});
		FragmentAmazonAd ad = new FragmentAmazonAd();
		ad.setHappyAdListener(new HappyAdListener() {
			@Override
			public void onAd() {				
			}
			@Override
			public void onAdFailed(int count, boolean noFill) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						FragmentGoogleAd gad = new FragmentGoogleAd();
						getFragmentManager().beginTransaction().replace(R.id.fragment_ad, gad);
					}
				});
			}
		});
		getFragmentManager().beginTransaction().replace(R.id.fragment_ad, ad).commit();
		
	}
	
	private long started;
	
//	Future<?> fetchTweetsFuture;
//	
//	@Override
//	public void onResume() {
//		super.onResume();
//		if(started!=0) {
//			started = System.currentTimeMillis();
//		}
//		long delay = 60000;
//		if(System.currentTimeMillis()-started < delay) {
//			delay = Math.max(delay, 0);
//		}
//		fetchTweetsFuture = ThreadHelper.getScheduler().scheduleWithFixedDelay(r, delay, 60000, TimeUnit.MILLISECONDS);		
//	}
//	
//	private Runnable fetchTweets = new Runnable() {
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			
//		}
//	};
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	public static FragmentSchedule newInstance(Station from, Station to) {
		Bundle b = new Bundle();
		b.putSerializable("from", from);
		b.putSerializable("to", to);
		FragmentSchedule s = new FragmentSchedule();
		s.setArguments(b);
		return s;
	}

}
