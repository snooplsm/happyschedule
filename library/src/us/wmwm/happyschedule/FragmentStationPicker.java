package us.wmwm.happyschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.happytap.jumper.JumpListener;
import com.happytap.jumper.JumpView;

public class FragmentStationPicker extends Fragment {
	
	StickyListHeadersListView list;
	
	OnStationSelectedListener onStationSelectedListener;
	
	JumpView jumper;
	
	StationAdapter adapter;
	
	public void setOnStationSelectedListener(
			OnStationSelectedListener onStationSelectedListener) {
		this.onStationSelectedListener = onStationSelectedListener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_station_picker, container, false);
		list = (StickyListHeadersListView) root.findViewById(R.id.list);
		jumper = (JumpView ) root.findViewById(R.id.jumper);
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		list.setAdapter(adapter = new StationAdapter(getActivity()));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long idss) {
				StationView v = (StationView)view;
				if(onStationSelectedListener!=null) {
					onStationSelectedListener.onStation(Db.get().getStop(v.getStationId()));
				}
				
			}
		});
		jumper.setJumpListener(new JumpListener() {
			@Override
			public void onJump(Character c) {
				String letter = Character.toString(c);
				for(int i = 0; i < adapter.getCount(); i++) {
					String name = adapter.getName(adapter.getItem(i));
					if(name.startsWith(letter)) {
						list.setSelectionFromTop(i,(int)-getResources().getDimension(R.dimen.header_height));
						break;
					}
				}
				jumper.setVisibility(View.GONE);
			}
		});
		jumper.setVisibility(View.VISIBLE);
	}

}
