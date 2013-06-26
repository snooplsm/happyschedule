package us.wmwm.happyschedule.views;

import org.json.JSONArray;

import us.wmwm.happyschedule.Db;
import us.wmwm.happyschedule.FragmentDepartureVision;
import us.wmwm.happyschedule.FragmentPickStations;
import us.wmwm.happyschedule.HappyApplication;
import us.wmwm.happyschedule.OnStationSelectedListener;
import us.wmwm.happyschedule.Station;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragmentMainAdapter extends FragmentPagerAdapter {

	private JSONArray departureVisions;
	
	OnStationSelectedListener onStationSelectedListener;
	
	public void setOnStationSelectedListener(
			OnStationSelectedListener onStationSelectedListener) {
		this.onStationSelectedListener = onStationSelectedListener;
	};
	
	OnStationSelectedListener onStationSelected = new OnStationSelectedListener() {
		
		@Override
		public void onStation(Station station) {
			notifyDataSetChanged();
			departureVisions = null;
			if(onStationSelectedListener!=null) {
				onStationSelectedListener.onStation(null);
			}
		}
	};

	public FragmentMainAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int pos) {
		int count = getCount();
		if (pos == 0) {
			return new FragmentPickStations();
		}
		Station station = getDepartureVision(pos);
		FragmentDepartureVision dv = FragmentDepartureVision.newInstance(station);
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
			if(station==null) {
				return "Departurevision";
			}
			return station.getName();
		}		
		return "Schedule";
	}

}
