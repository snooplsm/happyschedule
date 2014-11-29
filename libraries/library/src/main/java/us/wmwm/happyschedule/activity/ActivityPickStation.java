package us.wmwm.happyschedule.activity;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentStationPicker;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.views.OnStationSelectedListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

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
			public void onStation(Station station,State state) {
				Intent i = new Intent();
				i.putExtra("station", station);
				setResult(Activity.RESULT_OK, i);
				finish();
			}
		});
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static Intent from(Context ctx, boolean departureVisionOnly) {
		Intent intent = new Intent(ctx,ActivityPickStation.class);
		intent.putExtra("departureVisionOnly", departureVisionOnly);
		return intent;
	}

}
