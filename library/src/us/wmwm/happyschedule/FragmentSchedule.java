package us.wmwm.happyschedule;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSchedule extends Fragment {

	ViewPager pager;

	View loadingContainer;

	Station from;

	Station to;

	Handler handler = new Handler();

	private static final String TAG = FragmentSchedule.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_schedule, container,
				false);
		pager = (ViewPager) view.findViewById(R.id.pager);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();
		from = (Station) b.getSerializable("from");
		to = (Station) b.getSerializable("to");
		FragmentScheduleAdapter adapter;
		pager.setAdapter(adapter  = new FragmentScheduleAdapter(from,to,getFragmentManager()));
		pager.setCurrentItem(adapter.getTodaysPosition());
	}

	public static FragmentSchedule newInstance(Station from, Station to) {
		Bundle b = new Bundle();
		b.putSerializable("from", from);
		b.putSerializable("to", to);
		FragmentSchedule s = new FragmentSchedule();
		s.setArguments(b);
		return s;
	}

}
