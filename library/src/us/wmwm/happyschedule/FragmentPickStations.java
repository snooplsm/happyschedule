package us.wmwm.happyschedule;

import us.wmwm.happyschedule.views.StationButton;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragmentPickStations extends Fragment {

	StationButton departureButton;
	StationButton arrivalButton;
	View getScheduleButton;
	FragmentStationPicker picker;
	
	public static interface OnGetSchedule {
		void onGetSchedule(Station from, Station to);
	}

	OnGetSchedule onGetSchedule;
	
	public void setOnGetSchedule(OnGetSchedule onGetSchedule) {
		this.onGetSchedule = onGetSchedule;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_pick_stations,
				container, false);
		departureButton = (StationButton) root.findViewById(R.id.depart_button);
		arrivalButton = (StationButton) root.findViewById(R.id.arrive_button);
		getScheduleButton = root.findViewById(R.id.get_schedule);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

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
		departureButton.setOnClickListener(onClick);
		arrivalButton.setOnClickListener(onClick);
		
		OnClickListener onClickGetSchedule = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Station depart = departureButton.getStation();
				Station arrive = arrivalButton.getStation();
				onGetSchedule.onGetSchedule(depart, arrive);
			}
		};
		
		getScheduleButton.setOnClickListener(onClickGetSchedule);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==Activity.RESULT_OK) {
			Station station = (Station) data.getSerializableExtra("station");
			if(requestCode==200) {
				arrivalButton.setStation(station);
			} else {
				departureButton.setStation(station);
			}
		}
	}

}
