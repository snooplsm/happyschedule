package us.wmwm.happyschedule.activity;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentStationPicker;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.views.OnStationSelectedListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityPickStation extends HappyActivity {

	FragmentStationPicker picker;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_pick_station);
		picker = FragmentStationPicker.newInstance(getIntent().getBooleanExtra(
				"departureVisionOnly", false));
		getSupportFragmentManager().beginTransaction().replace(R.id.pickfrag,
				picker).commit();
		picker.setOnStationSelectedListener(new OnStationSelectedListener() {

			@Override
			public void onStation(Station station) {
				Intent i = new Intent();
				i.putExtra("station", station);
				setResult(Activity.RESULT_OK, i);
				finish();
			}
		});
	}
	
	public static Intent from(Context ctx, boolean departureVisionOnly) {
		Intent intent = new Intent(ctx,ActivityPickStation.class);
		intent.putExtra("departureVisionOnly", departureVisionOnly);
		return intent;
	}

}
