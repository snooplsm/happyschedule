package us.wmwm.happyschedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ActivityPickStation extends FragmentActivity {

	FragmentStationPicker picker;
	
	@Override
	protected void onCreate(Bundle arg0) {		
		super.onCreate(arg0);
		setContentView(R.layout.activity_pick_station);
		picker = (FragmentStationPicker) getSupportFragmentManager().findFragmentById(R.id.pickfrag);
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
	
}
