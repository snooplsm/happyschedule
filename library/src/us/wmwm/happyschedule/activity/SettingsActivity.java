package us.wmwm.happyschedule.activity;

import us.wmwm.happyschedule.R;
import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_settings);
		getActionBar().setSubtitle(getString(R.string.activity_name_settings));
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
}
