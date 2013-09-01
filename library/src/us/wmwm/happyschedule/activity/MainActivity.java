package us.wmwm.happyschedule.activity;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentLoad;
import us.wmwm.happyschedule.fragment.FragmentLoad.OnLoadListener;
import us.wmwm.happyschedule.fragment.FragmentMain;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends HappyActivity {

	FragmentLoad fragmentLoad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getActionBar().setDisplayShowHomeEnabled(false);
		if (!FragmentLoad.isUpdated(getApplicationContext())) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(
							R.id.fragment_load,
							fragmentLoad = new FragmentLoad()
									.setListener(new OnLoadListener() {
										@Override
										public void onLoaded() {
											getSupportFragmentManager()
													.beginTransaction()
													.remove(fragmentLoad)
													.commit();
											getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main, new FragmentMain()).commit();
										}
									})).commit();
		} else {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main, new FragmentMain()).commit();
		}
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		invalidateOptionsMenu();
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i("MainActivity", "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
