package us.wmwm.happyschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

public class FragmentStationPicker extends Fragment {
	
	StickyListHeadersListView list;
	
	OnStationSelectedListener onStationSelectedListener;
	
	public void setOnStationSelectedListener(
			OnStationSelectedListener onStationSelectedListener) {
		this.onStationSelectedListener = onStationSelectedListener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_station_picker, container, false);
		list = (StickyListHeadersListView) root.findViewById(R.id.list);
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		list.setAdapter(new StationAdapter());
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long idss) {
				StationView v = (StationView)view;
				if(onStationSelectedListener!=null) {
					onStationSelectedListener.onStation(v.getData());
				}
				
			}
		});
	}

}
