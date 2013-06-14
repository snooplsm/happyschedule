package us.wmwm.happyschedule;

import us.wmwm.happyschedule.views.FragmentMainAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentMain extends Fragment {

	ViewPager pager;
	
	Handler handler = new Handler();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_main, container, false);
		pager = (ViewPager) view.findViewById(R.id.pager);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				pager.setAdapter(new FragmentMainAdapter(getFragmentManager()));
			}
		});
		
	}

}
