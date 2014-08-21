package us.wmwm.happyschedule.fragment;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.fragment.FragmentHappyAd.DiscardListener;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.AppAd;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.util.PremiumUserHelper;
import us.wmwm.happyschedule.util.Streams;
import us.wmwm.happyschedule.views.ScheduleControlsView.ScheduleControlListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.flurry.android.FlurryAgent;
import com.squareup.timessquare.CalendarPickerView.OnDateSelectedListener;

public class FragmentSchedule extends Fragment {

	ViewPager pager;

	View loadingContainer;

	Station from;

	Station to;

	Handler handler = new Handler();

	FragmentScheduleAdapter adapter;
	
	int adId;

	OnDepartureVision onDepartureVision = new OnDepartureVision() {

		@Override
		public void onDepartureVision(Station station, Station arrival) {
			try {
				getFragmentManager()
						.beginTransaction()
						.replace(
								R.id.fragment_date_picker,
								FragmentDepartureVision.newInstance(new DepartureVision(station.getId(),
										arrival==null?null:arrival.getId()),0,arrival, null, true))
						.addToBackStack(null)
						.setBreadCrumbTitle(
								"DepartureVision @ " + station.getName())
						.commit();
			} catch (Exception e) {

			}
		}
	};

	OnGetSchedule onGetSchedule;

	public void setOnGetSchedule(OnGetSchedule onGetSchedule) {
		this.onGetSchedule = onGetSchedule;
	}

	ScheduleControlListener controlListener = new ScheduleControlListener() {

		@Override
		public void onTrips(Schedule schedule, StationToStation stationToStation) {
			FragmentTrip t = FragmentTrip.newInstance(from, to,
					stationToStation, schedule);
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment_date_picker, t).addToBackStack(null)
					.setBreadCrumbTitle(from.getName() + " to " + to.getName())
					.commit();
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

		public void onShare(Schedule schedule, StationToStation stationToStation) {
		};
	};

	private static final String TAG = FragmentSchedule.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_schedule, container,
				false);
		pager = (ViewPager) view.findViewById(R.id.pager2);
		setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FlurryAgent.logEvent(item.getTitle() + "MenuItemSelected");
		if (item.getItemId() == R.id.menu_change_day) {
			final FragmentDatePicker picker = FragmentDatePicker
					.newInstance(adapter.getCalendar(pager.getCurrentItem())
							.getTime());
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			picker.setOnDateSelectedListener(new OnDateSelectedListener() {

				@Override
				public void onDateSelected(Date date) {
					getFragmentManager().popBackStack();
					// FragmentTransaction ft = getFragmentManager()
					// .beginTransaction();
					// ft.remove(picker);
					// ft.commit();
					pager.setCurrentItem(adapter.getPositionFor(date));
				}
			});
			ft.replace(R.id.fragment_date_picker, picker);
			ft.addToBackStack(null);
			ft.commit();
			// picker.show(getFragmentManager(), "datePicker");
		}
		pager.setOffscreenPageLimit(0);
		if (item.getItemId() == R.id.menu_go_to_today || item.getItemId()==R.id.menu_go_to_next_train) {
			pager.setCurrentItem(adapter.getTodaysPosition());
		}
		if (item.getItemId() == R.id.menu_reverse_in) {
			onGetSchedule.onGetSchedule(adapter.getCalendar(pager.getCurrentItem()),to, from);
		}
		if(item.getItemId()==R.id.menu_save_as_favorite) {
			DepartureVision dv = new DepartureVision(FragmentSchedule.this.from.getId(),FragmentSchedule.this.to.getId());
			FavoriteHelper.add(dv);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
//			adId = R.id.fragment_schedule_ad_top;
//		} else {
//			adId = R.id.fragment_schedule_ad;
//		}
		adId = R.id.fragment_schedule_ad;
		Bundle b = getArguments();
		from = (Station) b.getSerializable("from");
		to = (Station) b.getSerializable("to");
		handler.post(new Runnable() {
			@Override
			public void run() {
				pager.setAdapter(adapter = new FragmentScheduleAdapter(from,
						to, getFragmentManager()).setControlListener(controlListener));
				adapter.setOnDepartureVision(onDepartureVision);
				adapter.setOnGetSchedule(onGetSchedule);
				pager.setCurrentItem(adapter.getTodaysPosition());
			}
		});

		ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				try {
					AppConfig config = new AppConfig(new JSONObject(Streams
							.readFully(Streams.getStream("config.json"))));
					final AppAd ad = config.getBestAd(getActivity(),
							FragmentSchedule.class);
					if (ad != null) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								final FragmentHappyAd fad = FragmentHappyAd
										.newIntance(ad);
								fad.setDiscardListener(new DiscardListener() {
									@Override
									public void onDiscard(AppAd ad) {
										getFragmentManager().beginTransaction()
												.remove(fad).commit();
										WDb.get()
												.savePreference(
														"discard_"
																+ ad.getDiscardKey(),
														System.currentTimeMillis()
																+ "");
									}
								});
								getFragmentManager().beginTransaction()
										.replace(R.id.top_ad, fad).commit();
							}
						});
					}
				} catch (Exception e) {

				}
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
						try {
							if (getActivity()!=null && getView() != null
									&& getView().findViewById(adId) != null) {
								FragmentGoogleAd gad = new FragmentGoogleAd();
								getChildFragmentManager().beginTransaction()
										.replace(adId, gad)
										.commit();
							}
							
						} catch (Exception e) {

						}
					}
				});
			}
		});

		
		if (getActivity()!=null && getView() != null
				&& getView().findViewById(R.id.fragment_schedule_ad) != null) {
			try {
				getChildFragmentManager().beginTransaction()
						.replace(adId, ad).commit();
			} catch (Exception e) {

			}
		}

	}

	private long started;

	// Future<?> fetchTweetsFuture;
	//
	// @Override
	// public void onResume() {
	// super.onResume();
	// if(started!=0) {
	// started = System.currentTimeMillis();
	// }
	// long delay = 60000;
	// if(System.currentTimeMillis()-started < delay) {
	// delay = Math.max(delay, 0);
	// }
	// fetchTweetsFuture = ThreadHelper.getScheduler().scheduleWithFixedDelay(r,
	// delay, 60000, TimeUnit.MILLISECONDS);
	// }
	//
	// private Runnable fetchTweets = new Runnable() {
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	//
	// }
	// };

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	public static FragmentSchedule newInstance(Calendar day, Station from, Station to) {
		Bundle b = new Bundle();
		b.putSerializable("from", from);
		b.putSerializable("to", to);
		b.putSerializable("day", day);
		FragmentSchedule s = new FragmentSchedule();
		s.setArguments(b);
		return s;
	}

}
