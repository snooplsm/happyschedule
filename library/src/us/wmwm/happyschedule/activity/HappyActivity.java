package us.wmwm.happyschedule.activity;

import us.wmwm.happyschedule.R;

import com.flurry.android.FlurryAgent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class HappyActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		FlurryAgent.logEvent(getClass().getSimpleName());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		FlurryAgent.onEndSession(this);
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if(item.getItemId()==android.R.id.home) {
//			onBackPressed();
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
}