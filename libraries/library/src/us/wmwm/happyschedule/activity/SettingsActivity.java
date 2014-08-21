package us.wmwm.happyschedule.activity;

import us.wmwm.happyschedule.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class SettingsActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_settings);
        ActionBar a = getSupportActionBar();
		a.setSubtitle(getString(R.string.activity_name_settings));
		a.setHomeButtonEnabled(true);
		a.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
