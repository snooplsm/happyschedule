package us.wmwm.happyschedule.fragment;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.adapter.StationAdapter;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.views.OnStationSelectedListener;
import us.wmwm.happyschedule.views.OnStationSelectedListener.State;
import us.wmwm.happyschedule.views.StationView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.happytap.jumper.JumpListener;
import com.happytap.jumper.JumpView;

public class FragmentStationPicker extends HappyFragment {
	
	StickyListHeadersListView list;
	
	OnStationSelectedListener onStationSelectedListener;
	
	JumpView jumper;
	
	StationAdapter adapter;
	
	public void setOnStationSelectedListener(
			OnStationSelectedListener onStationSelectedListener) {
		this.onStationSelectedListener = onStationSelectedListener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_station_picker, menu);
		if(jumper==null || jumper.getVisibility()==View.VISIBLE) {
			menu.findItem(R.id.menu_jumper).setVisible(false);
		} else {
			menu.findItem(R.id.menu_jumper).setVisible(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.menu_jumper) {
			jumper.setVisibility(View.VISIBLE);
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_station_picker, container, false);
		list = (StickyListHeadersListView) root.findViewById(R.id.list);
		jumper = (JumpView ) root.findViewById(R.id.jumper);
		jumper.setColorStateList(inflater.getContext().getResources().getColorStateList(R.drawable.selector_jumper));
		jumper.initializeViews();
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();
		boolean departureVisionOnly = false;
		if(args!=null) {
			departureVisionOnly = args.getBoolean("departureVisionOnly", false);
		}
		list.setAdapter(adapter = new StationAdapter(getActivity(),departureVisionOnly));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long idss) {
				StationView v = (StationView)view;
				if(onStationSelectedListener!=null) {
					onStationSelectedListener.onStation(Db.get().getStop(v.getStationId()),State.NONE);
				}
				
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				StationView v = (StationView)arg1;
				Toast.makeText(arg1.getContext(), v.getStationId(), Toast.LENGTH_SHORT).show();
				return false;
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
				getActivity().supportInvalidateOptionsMenu();
			}
		});
		jumper.setVisibility(View.VISIBLE);
	}

	public static FragmentStationPicker newInstance(boolean departureVisionOnly) {
		Bundle b= new Bundle();
		b.putBoolean("departureVisionOnly", departureVisionOnly);
		FragmentStationPicker p = new FragmentStationPicker();
		p.setArguments(b);
		return p;
	}
}
