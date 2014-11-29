package us.wmwm.happyschedule.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.flurry.android.FlurryAgent;

public class HappyFragment extends Fragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FlurryAgent.logEvent(getClass().getSimpleName());
	}
	
}
