package us.wmwm.happyschedule.views;

import org.json.JSONArray;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision;
import us.wmwm.happyschedule.fragment.FragmentHistory;
import us.wmwm.happyschedule.fragment.FragmentHistory.OnHistoryListener;
import us.wmwm.happyschedule.fragment.FragmentPickStations;
import us.wmwm.happyschedule.fragment.IPrimary;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.model.Station;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class FragmentMainAdapter extends FragmentStatePagerAdapter {

	private JSONArray departureVisions;

	OnStationSelectedListener onStationSelectedListener;

	OnGetSchedule onGetScheduleListener;
	
	OnHistoryListener onHistoryListener;
	
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
		public void onStation(Station station) {
			notifyDataSetChanged();
			departureVisions = null;
			if (onStationSelectedListener != null) {
				onStationSelectedListener.onStation(null);
			}
		}
	};

	OnGetSchedule onGetSchedule = new OnGetSchedule() {

		@Override
		public void onGetSchedule(Station from, Station to) {
			if (onGetScheduleListener != null) {
				onGetScheduleListener.onGetSchedule(from, to);
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
		Station station = getDepartureVision(pos);
		FragmentDepartureVision dv = FragmentDepartureVision.newInstance(
				station, null,false);
		dv.setRetainInstance(false);
		dv.setOnStationSelected(onStationSelected);
		return dv;
	}

	public Station getDepartureVision(int pos) {
		if (departureVisions.length() == 0) {
			return null;
		}
		return Db.get().getStop(departureVisions.optString(pos - 2));
	}

	@Override
	public int getCount() {
		String k = PreferenceManager.getDefaultSharedPreferences(
				HappyApplication.get()).getString("departure_visions", "[]");
		try {
			departureVisions = new JSONArray(k);
		} catch (Exception e) {
			departureVisions = new JSONArray();
		}
		int count = 2;
		if(HappyApplication.get().getString(R.string.poller).length()==0) {
			
		} else {
			count = count+1;
		}
		return count;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (position == 0) {
			return "History";
		}
		if (position == 1) {
			return "Schedule";
		}
		if (position > 1) {
			Station station = getDepartureVision(position);
			if (station == null) {
				return "Departurevision";
			}
			return station.getName();
		}
		return "Schedule";
	}

}
