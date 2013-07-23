package us.wmwm.happyschedule;

import us.wmwm.happyschedule.views.StationButton;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragmentPickStations extends Fragment {

	StationButton departureButton;
	StationButton arrivalButton;
	View getScheduleButton;
	FragmentStationPicker picker;
	View reverseButton;

	public static interface OnGetSchedule {
		void onGetSchedule(Station from, Station to);
	}

	OnGetSchedule onGetSchedule;

	public void setOnGetSchedule(OnGetSchedule onGetSchedule) {
		this.onGetSchedule = onGetSchedule;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_pick_stations,
				container, false);
		departureButton = (StationButton) root.findViewById(R.id.depart_button);
		arrivalButton = (StationButton) root.findViewById(R.id.arrive_button);
		departureButton.setHint("Departure Station");
		arrivalButton.setHint("Arrival Station");
		getScheduleButton = root.findViewById(R.id.get_schedule);
		reverseButton = root.findViewById(R.id.reverse);
		return root;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_pick_stations, menu);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String lastDepartId = manager.getString("lastDepartId", null);
		String lastArriveId = manager.getString("lastArriveId", null);
		
		if(lastDepartId!=null) {
			Station station = Db.get().getStop(lastDepartId);
			if(station!=null) {
				departureButton.setStation(station);
			}
		}
		
		if(lastArriveId!=null) {
			Station station = Db.get().getStop(lastArriveId);
			if(station!=null) {
				arrivalButton.setStation(station);
			}
		}
		
		OnClickListener onClick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				final StationButton button = (StationButton) v;
				Intent i = new Intent(getActivity(), ActivityPickStation.class);
				final int code;
				if(button==arrivalButton) {
					code = 200;
				} else {
					code = 100;
				}
				startActivityForResult(i,code);
//				picker = new FragmentStationPicker();
//				getFragmentManager().beginTransaction()
//						.replace(R.id.secondary_view, picker)
//						.addToBackStack(null).commit();
//				picker.setOnStationSelectedListener(new OnStationSelectedListener() {
//
//					@Override
//					public void onStation(String stationId) {
//						button.setStation(stationId);
//						getFragmentManager().beginTransaction().remove(picker)
//								.commit();
//						picker = null;
//					}
//				});
			}
		};
		
		OnClickListener reverse = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Station tmp = departureButton.getStation();
				departureButton.setStation(arrivalButton.getStation());
				arrivalButton.setStation(tmp);
			}
		};
		
		reverseButton.setOnClickListener(reverse);
		departureButton.setOnClickListener(onClick);
		arrivalButton.setOnClickListener(onClick);
		
		OnClickListener onClickGetSchedule = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Station depart = departureButton.getStation();
				Station arrive = arrivalButton.getStation();
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().
				putString("lastDepartId", depart.id).
				putString("lastArriveId", arrive.id).commit();
				onGetSchedule.onGetSchedule(depart, arrive);
			}
		};
		
		getScheduleButton.setOnClickListener(onClickGetSchedule);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Station station = (Station) data.getSerializableExtra("station");
			if (requestCode == 200) {
				arrivalButton.setStation(station);
			} else {
				departureButton.setStation(station);
			}
		}
	}

}
