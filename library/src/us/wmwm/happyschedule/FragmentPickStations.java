package us.wmwm.happyschedule;

import us.wmwm.happyschedule.views.StationButton;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragmentPickStations extends Fragment {

	View departureButton;
	View arrivalButton;
	FragmentStationPicker picker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_pick_stations,
				container, false);
		departureButton = root.findViewById(R.id.depart_button);
		arrivalButton = root.findViewById(R.id.arrive_button);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		OnClickListener onClick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				final StationButton button = (StationButton) v;
				if (picker == null) {
					picker = new FragmentStationPicker();
					getFragmentManager().beginTransaction()
							.replace(R.id.secondary_view, picker).commit();
				}
				picker.setOnStationSelectedListener(new OnStationSelectedListener() {

					@Override
					public void onStation(String stationId) {
						button.setStation(stationId);
						getFragmentManager().beginTransaction().remove(picker).commit();
						picker = null;
					}
				});
			}
		};
		departureButton.setOnClickListener(onClick);
		arrivalButton.setOnClickListener(onClick);
	}

}
