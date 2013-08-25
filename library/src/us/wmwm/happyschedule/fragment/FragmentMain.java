package us.wmwm.happyschedule.fragment;

import java.util.HashMap;
import java.util.Map;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.fragment.FragmentHistory.OnHistoryListener;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.views.FragmentMainAdapter;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.flurry.android.FlurryAgent;

public class FragmentMain extends Fragment {

	ViewPager pager;

	Handler handler = new Handler();

	OnBackStackChangedListener onBackStackListener = new OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			int count = getFragmentManager().getBackStackEntryCount();
			Log.d(FragmentMain.class.getSimpleName(), "onBackStack " + count);
			if (count == 0) {
				handler.post(new Runnable() {
					public void run() {
						ActionBar a = getActivity().getActionBar();
						a.setSubtitle(null);
						a.setDisplayHomeAsUpEnabled(false);
						a.setHomeButtonEnabled(false);
						getActivity().invalidateOptionsMenu();
					};
				});
			} else {
				BackStackEntry e = getFragmentManager().getBackStackEntryAt(count-1);
				final String title;
				if(!TextUtils.isEmpty(e.getBreadCrumbTitle())) {
					 title = e.getBreadCrumbTitle().toString();
				} else {
					title = null;
				}
				handler.post(new Runnable() {
					public void run() {
						ActionBar a = getActivity().getActionBar();
						getActivity().invalidateOptionsMenu();
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
				final FragmentMainAdapter fma = new FragmentMainAdapter(
						getFragmentManager());
				pager.setAdapter(fma);				
				pager.setCurrentItem(1);
				fma.setOnGetScheduleListener(onGetSchedule = new OnGetSchedule() {

					@Override
					public void onGetSchedule(Station from, Station to) {
						Map<String, String> args = new HashMap<String, String>();
						args.put("from_id", from.getId());
						args.put("to_id", to.getId());
						args.put("from_name", from.getName());
						args.put("to_name", to.getName());
						FlurryAgent.logEvent("OnGetSchedule", args);
						FragmentTransaction t = getFragmentManager()
								.beginTransaction();
						FragmentSchedule fs = FragmentSchedule.newInstance(
								from, to);
						fs.setOnGetSchedule(onGetSchedule);
						if(getFragmentManager().getBackStackEntryCount()==0) {
						} else {
							for(int i = getFragmentManager().getBackStackEntryCount()-1; i>=0; i--) {
								BackStackEntry e = getFragmentManager().getBackStackEntryAt(i);
								if("schedule".equals(e.getName())) {
									getFragmentManager().popBackStackImmediate(i, i);
								}
							}							
						}
						t.addToBackStack("schedule");
						t.replace(R.id.fragment_schedule, fs).setBreadCrumbTitle((from.getName() + " to " + to.getName())).commit();
						ActionBar a = getActivity().getActionBar();
						a.setDisplayHomeAsUpEnabled(true);
						a.setHomeButtonEnabled(true);
						a.setDisplayUseLogoEnabled(true);
					}
				});
				fma.setOnHistoryListener(new OnHistoryListener() {
					
					@Override
					public void onHistory(final Station from, final Station to) {						
						onGetSchedule.onGetSchedule(from, to);
						new Thread() {
							@Override
							public void run() {
								try {
									WDb.get().savePreference("lastDepartId", from.getId());
									WDb.get().savePreference("lastArriveId", to.getId());
									WDb.get().saveHistory(from, to);
								} catch (Exception e) {
									
								}
							}
						}.start();
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

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getFragmentManager().removeOnBackStackChangedListener(
				onBackStackListener);
	}

}
