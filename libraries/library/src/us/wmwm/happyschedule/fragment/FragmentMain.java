package us.wmwm.happyschedule.fragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision.DepartureVisionListener;
import us.wmwm.happyschedule.fragment.FragmentHistory.OnHistoryListener;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.model.AppAd;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.TripInfo;
import us.wmwm.happyschedule.util.PremiumUserHelper;
import us.wmwm.happyschedule.util.Streams;
import us.wmwm.happyschedule.views.FragmentMainAdapter;
import us.wmwm.happyschedule.views.OnStationSelectedListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.flurry.android.FlurryAgent;

public class FragmentMain extends Fragment {

	ViewPager pager;

	Handler handler = new Handler();

	Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			
		}
	};
	
	OnBackStackChangedListener onBackStackListener = new OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			int count = getFragmentManager().getBackStackEntryCount();
			Log.d(FragmentMain.class.getSimpleName(), "onBackStack " + count);
			if (count == 0) {
				handler.post(new Runnable() {
					public void run() {
						android.support.v7.app.ActionBar a = ((ActionBarActivity)getActivity()).getSupportActionBar();
						if (pager.getCurrentItem() == 0) {
							a.setSubtitle("History");
						} else if (pager.getCurrentItem() == 2) {
							a.setSubtitle("w/ DepartureVision");
						} else {
							a.setSubtitle(null);
						}

						a.setDisplayHomeAsUpEnabled(false);
						a.setHomeButtonEnabled(false);
						getActivity().supportInvalidateOptionsMenu();
//						getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//						getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
					};
				});
			} else {
				BackStackEntry e = getFragmentManager().getBackStackEntryAt(
						count - 1);
				if("schedule".equals(e.getName())) {
					//getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
					//getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				}
				final String title;
				if (!TextUtils.isEmpty(e.getBreadCrumbTitle())) {
					title = e.getBreadCrumbTitle().toString();
				} else {
					title = null;
				}
				handler.post(new Runnable() {
					public void run() {
                        ActionBar a = ((ActionBarActivity)getActivity()).getSupportActionBar();
						getActivity().supportInvalidateOptionsMenu();
						a.setSubtitle(title);
					};
				});
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		pager = (ViewPager) view.findViewById(R.id.pager);
		pager.setPageMargin((int) (getResources()
				.getDimension(R.dimen.activity_horizontal_margin)));
		Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }
		return view;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	OnGetSchedule onGetSchedule;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getFragmentManager().addOnBackStackChangedListener(onBackStackListener);
		handler.post(new Runnable() {
			@Override
			public void run() {
				FragmentManager fm = getFragmentManager();
				if(fm==null) {
					return;
				}
				final FragmentMainAdapter fma = new FragmentMainAdapter(
						fm);
				pager.setAdapter(fma);
				pager.setCurrentItem(1);
				fma.setOnGetScheduleListener(onGetSchedule = new OnGetSchedule() {

					@Override
					public void onGetSchedule(final Calendar day, final Station from, final Station to) {
						Map<String, String> args = new HashMap<String, String>();
						args.put("from_id", from.getId());
						args.put("to_id", to.getId());
						args.put("from_name", from.getName());
						args.put("to_name", to.getName());						
						args.put("day", day.getTime().toString());
						FlurryAgent.logEvent("OnGetSchedule", args);
						if (getFragmentManager().getBackStackEntryCount() == 0) {
						} else {
							for (int i = getFragmentManager()
									.getBackStackEntryCount() - 1; i >= 0; i--) {
								BackStackEntry e = getFragmentManager()
										.getBackStackEntryAt(i);
								if ("schedule".equals(e.getName())) {
									getFragmentManager().popBackStackImmediate();
								}
							}
						}
						handler.post(new Runnable() {
							@Override
							public void run() {
								FragmentTransaction t = getFragmentManager()
										.beginTransaction();
								FragmentSchedule fs = FragmentSchedule.newInstance(
										day, from, to);
								fs.setOnGetSchedule(onGetSchedule);
								t.addToBackStack("schedule");
								t.replace(R.id.fragment_schedule, fs)
										.setBreadCrumbTitle(
												(from.getName() + " to " + to.getName()))
										.commit();
								ActionBar a = ((ActionBarActivity)getActivity()).getSupportActionBar();
								a.setDisplayHomeAsUpEnabled(true);
								a.setHomeButtonEnabled(true);
								a.setDisplayUseLogoEnabled(true);
							}
						});
						
					}
				});
				fma.setOnHistoryListener(new OnHistoryListener() {

					@Override
					public void onHistory(final Station from, final Station to) {
						onGetSchedule.onGetSchedule(Calendar.getInstance(),from, to);
						new Thread() {
							@Override
							public void run() {
								try {
									WDb.get().savePreference("lastDepartId",
											from.getId());
									WDb.get().savePreference("lastArriveId",
											to.getId());
									WDb.get().saveHistory(from, to);
								} catch (Exception e) {

								}
							}
						}.start();
					}
				});
				fma.setDepartureVisionListener(new DepartureVisionListener() {
					@Override
					public void onTrip(String blockId) {
						String tripId = ScheduleDao.get().getBlockId(blockId);
						TripInfo tinfo = ScheduleDao.get().getStationTimesForTripId(tripId, 0, Integer.MAX_VALUE);
						if(tinfo.stops.isEmpty()) {
							return;
						}
						Station from = Db.get().getStop(tinfo.stops.get(0).id);
						Station to = Db.get().getStop(tinfo.stops.get(tinfo.stops.size()-1).id);
						FragmentTrip t = FragmentTrip.newInstance(from, to,
								tripId);
						
						
						getFragmentManager().beginTransaction()
						.replace(R.id.fragment_schedule, t).addToBackStack(null)
						.setBreadCrumbTitle(from.getName() + " to " + to.getName())
						.commit();
					}
				});
				fma.setOnStationSelectedListener(new OnStationSelectedListener() {
					@Override
					public void onStation(Station station,State state) {
						int pos = pager.getCurrentItem();
						pager.setAdapter(null);
						pager.setAdapter(fma);
						int newPos = pos;
						if(state==State.ADDED) {
							newPos++;
						}
						pager.setCurrentItem(newPos);
					}
				});
				// fma.setOnStationSelectedListener(new
				// OnStationSelectedListener() {
				//
				// @Override
				// public void onStation(Station station) {
				// if(station==null) {
				// pager.setCurrentItem(pager.getCurrentItem()-1);
				// return;
				// }
				// for(int i = 1; i < fma.getCount(); i++) {
				// Station s = fma.getDepartureVision(i);
				// if(s.getId().equals(station.getId())) {
				// pager.setCurrentItem(i);
				// break;
				// }
				// }
				// //pager.invalidate();
				// }
				// });
			}
		});
		ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				try {
					if(!PremiumUserHelper.isPaidUser()) {
						AppConfig config = new AppConfig(new JSONObject(Streams.readFully(Streams.getStream("config.json"))));
						final AppAd ad = config.getBestAd(getActivity(), FragmentMain.class);
						if(ad!=null) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									FragmentHappyAd fad = FragmentHappyAd.newIntance(ad);
									FragmentManager manager = getFragmentManager();
									if(manager!=null) {
										manager.beginTransaction().replace(R.id.main_fragment_ad, fad).commit();
									}
								}
							});
						} else {
							handler.post(new Runnable() {
								@Override
								public void run() {
									FragmentGoogleAd gad = new FragmentGoogleAd();
									FragmentManager manager = getFragmentManager();
									if(manager!=null) {
										manager.beginTransaction().replace(R.id.main_fragment_ad, gad).commit();
									}
								}
							});
						}
					}					
				} catch (Exception e) {
					
				}
			}
		});

	}
	
	@Override
	public void onStart() {
		super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
	}
	
	@Override
	public void onStop() {
		super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getFragmentManager().removeOnBackStackChangedListener(
				onBackStackListener);
	}

}
