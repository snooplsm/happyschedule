package us.wmwm.happyschedule.fragment;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.R.id;
import us.wmwm.happyschedule.R.layout;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentHistory extends Fragment {

	StickyListHeadersListView list;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pick_stations, container,false);
		list = (StickyListHeadersListView) view.findViewById(R.id.list);
		return view;
	}
	
}
