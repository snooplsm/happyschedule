package us.wmwm.happyschedule.activity;

import java.util.Map;

import us.wmwm.happyschedule.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;

import com.flurry.android.FlurryAgent;

public class HappyActivity extends ActionBarActivity {


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		Map<String,String> params = getLoggingParameters();
		if(params==null) {
			FlurryAgent.logEvent(getClass().getSimpleName());
		} else {
			FlurryAgent.logEvent(getClass().getSimpleName(),params);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		FlurryAgent.onEndSession(this);
	}
	
	protected Map<String,String> getLoggingParameters() {
		return null;
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if(item.getItemId()==android.R.id.home) {
//			onBackPressed();
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
}
