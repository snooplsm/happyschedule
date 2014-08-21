package us.wmwm.happyschedule.views;

import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.fragment.DepartureVisionHelper;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision.DepartureVisionListener;
import us.wmwm.happyschedule.fragment.FragmentHistory;
import us.wmwm.happyschedule.fragment.FragmentHistory.OnHistoryListener;
import us.wmwm.happyschedule.fragment.FragmentPickStations;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.fragment.IPrimary;
import us.wmwm.happyschedule.fragment.ISecondary;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Station;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class FragmentMainAdapter extends FragmentStatePagerAdapter {

	OnStationSelectedListener onStationSelectedListener;

	OnGetSchedule onGetScheduleListener;
	
	OnHistoryListener onHistoryListener;
	
	DepartureVisionListener departureVisionListener;
	
	public void setDepartureVisionListener(
			DepartureVisionListener departureVisionListener) {
		this.departureVisionListener = departureVisionListener;
	}
	
	public void setOnHistoryListener(OnHistoryListener onHistoryListener) {
		this.onHistoryListener = onHistoryListener;
	}

	public void setOnGetScheduleListener(OnGetSchedule onGetScheduleListener) {
		this.onGetScheduleListener = onGetScheduleListener;
	};

	public void setOnStationSelectedListener(
			OnStationSelectedListener onStationSelectedListener) {
		this.onStationSelectedListener = onStationSelectedListener;
	};

	OnStationSelectedListener onStationSelected = new OnStationSelectedListener() {

		@Override
		public void onStation(Station station, State state) {
			notifyDataSetChanged();
			if (onStationSelectedListener != null) {
				onStationSelectedListener.onStation(station, state);
			}
		}
	};

	OnGetSchedule onGetSchedule = new OnGetSchedule() {

		@Override
		public void onGetSchedule(Calendar cal, Station from, Station to) {
			if (onGetScheduleListener != null) {
				onGetScheduleListener.onGetSchedule(cal, from, to);
			}
		}
	};

	public FragmentMainAdapter(FragmentManager fm) {
		super(fm);
	}

	Object last;

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		if (object != last) {
			if(last!=null && last instanceof ISecondary) {
				((ISecondary)last).setSecondary();
			}
			last = object;
			if (object instanceof IPrimary) {
				((IPrimary) object).setPrimaryItem();
			}
		}
	}

	@Override
	public Fragment getItem(int pos) {
		int count = getCount();
		if (pos == 0) {
			FragmentHistory history = new FragmentHistory();
			history.setRetainInstance(false);
			history.setOnHistoryListener(onHistoryListener);
			return history;
		}
		if (pos == 1) {
			FragmentPickStations pick = new FragmentPickStations();
			pick.setRetainInstance(false);
			pick.setOnGetSchedule(onGetSchedule);
			return pick;
		}
		DepartureVision station = getDepartureVision(pos);
		FragmentDepartureVision dv = FragmentDepartureVision.newInstance(
				station, pos-2, getDepartureVisionArrival(),null,false);
		dv.setDepartureVisionListener(departureVisionListener);
		dv.setRetainInstance(false);
		dv.setOnStationSelected(onStationSelected);
		return dv;
	}

	public DepartureVision getDepartureVision(int pos) {
		List<DepartureVision> v = DepartureVisionHelper.getDepartureVisions();
		if (v.size() == 0) {
			return null;
		}
		return v.get(pos - 2);
	}
	
	public Station getDepartureVisionArrival() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get());
		String id = prefs.getString("departureVisionArrivalId", null);
		if(id==null) {
			return null;
		}
		return Db.get().getStop(id);
	}

	@Override
	public int getCount() {
		int count = 2;
		if(HappyApplication.get().getString(R.string.poller).length()==0) {
			
		} else {
			count = count+Math.max(1,DepartureVisionHelper.getDepartureVisions().size());
		}
		return count;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (position == 0) {
			return " History & Favorites ";
		}
		if (position == 1) {
			return " Schedule ";
		}
		if (position > 1) {
			DepartureVision dv = getDepartureVision(position);
			if (dv == null) {
				return " Departurevision ";
			}
			Station station = Db.get().getStop(dv.getFrom());
			return " " + station.getName() + " ";
		}
		return " Schedule ";
	}

}
