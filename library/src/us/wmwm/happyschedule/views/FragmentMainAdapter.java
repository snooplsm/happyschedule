package us.wmwm.happyschedule.views;

import org.json.JSONArray;

import us.wmwm.happyschedule.Db;
import us.wmwm.happyschedule.FragmentDepartureVision;
import us.wmwm.happyschedule.FragmentPickStations;
import us.wmwm.happyschedule.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.HappyApplication;
import us.wmwm.happyschedule.IPrimary;
import us.wmwm.happyschedule.OnStationSelectedListener;
import us.wmwm.happyschedule.Station;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class FragmentMainAdapter extends FragmentStatePagerAdapter {

	private JSONArray departureVisions;

	OnStationSelectedListener onStationSelectedListener;

	OnGetSchedule onGetScheduleListener;

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
		if(object!=last) {
			last = object;
			if(object instanceof IPrimary) {
				((IPrimary)object).setPrimaryItem();
			}
		}
	}
	
	@Override
	public Fragment getItem(int pos) {
		int count = getCount();
		if (pos == 0) {
			FragmentPickStations pick = new FragmentPickStations();
			pick.setOnGetSchedule(onGetSchedule);
			return pick;
		}
		Station station = getDepartureVision(pos);
		FragmentDepartureVision dv = FragmentDepartureVision
				.newInstance(station,null);
		dv.setOnStationSelected(onStationSelected);
		return dv;
	}

	public Station getDepartureVision(int pos) {
		if (departureVisions.length() == 0) {
			return null;
		}
		return Db.get().getStop(departureVisions.optString(pos - 1));
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
		return 1 + (departureVisions.length() == 0 ? 1 : departureVisions
				.length());
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (position > 0) {
			Station station = getDepartureVision(position);
			if (station == null) {
				return "Departurevision";
			}
			return station.getName();
		}
		return "Schedule";
	}

}
