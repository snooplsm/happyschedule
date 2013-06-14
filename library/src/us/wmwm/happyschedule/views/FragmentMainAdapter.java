package us.wmwm.happyschedule.views;

import us.wmwm.happyschedule.FragmentDepartureVision;
import us.wmwm.happyschedule.FragmentPickStations;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FragmentMainAdapter extends FragmentStatePagerAdapter {

	public FragmentMainAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int pos) {
		int count = getCount();
		if(pos==0) {
			return new FragmentPickStations();
		}
		return new FragmentDepartureVision();
	}

	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		if(position==getCount()-1) {
			return "Departurevision";
		}
		return "Schedule";
	}

}
