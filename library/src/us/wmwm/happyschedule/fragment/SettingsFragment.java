package us.wmwm.happyschedule.fragment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.activity.RailLinesActivity;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.WDb;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.seismic.ShakeDetector;

public class SettingsFragment extends PreferenceFragment implements com.squareup.seismic.ShakeDetector.Listener {

	ListPreference refreshInterval;
	Preference railLine;
	CheckBoxPreference pushNotifications;
	PreferenceCategory debugScreen;
	Preference pushId;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
	    ShakeDetector sd = new ShakeDetector(this);
	    sd.start(sensorManager);
		refreshInterval = (ListPreference) findPreference(getString(R.string.settings_departure_vision_key_period));
		railLine = (Preference) findPreference(getString(R.string.settings_key_rail_lines));
		pushNotifications = (CheckBoxPreference) findPreference(getString(R.string.settings_key_push_on));
		Object o = findPreference(getString(R.string.settings_key_debug));
		debugScreen = (PreferenceCategory) o;
		pushId = (Preference) findPreference(getString(R.string.settings_key_debug_push));
		
		
		railLine.setIntent(new Intent(getActivity(), RailLinesActivity.class));
		refreshInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateRefreshInterval(refreshInterval.findIndexOfValue(String.valueOf(newValue)));
								
				return true;
			}
		});
		pushNotifications.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if(Boolean.TRUE.equals(newValue)) {
					boolean isPlayInstalled = checkPlayServices(true);
					if(!isPlayInstalled) {
						packageInstalledReceiver = new BroadcastReceiver() {
							@Override
							public void onReceive(Context context, Intent intent) {
								boolean installed = checkPlayServices(false);
								if(installed) {
									getActivity().unregisterReceiver(this);
									packageInstalledReceiver = null;
									pushNotifications.setChecked(true);
									//PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(pushNotifications.getKey(), Boolean.TRUE).commit();
								}
							}
						};
						IntentFilter it = new IntentFilter();
						it.addAction(Intent.ACTION_PACKAGE_ADDED);
						it.addAction(Intent.ACTION_PACKAGE_REPLACED);
						it.addAction(Intent.ACTION_PACKAGE_CHANGED);
						it.addDataScheme("package");
						getActivity().registerReceiver(packageInstalledReceiver, it);
					}
					return isPlayInstalled;
				}
				return true;
			}
		});
		int pos = refreshInterval.findIndexOfValue(refreshInterval.getValue());
		updateRefreshInterval(pos);	
		getPreferenceScreen().removePreference(debugScreen);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		pushId.setSummary(getRegistrationId());
		pushId.setIntent(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,pushId.getSummary().toString()), "Share"));
	};
	
	BroadcastReceiver packageInstalledReceiver;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(packageInstalledReceiver!=null) {
			getActivity().unregisterReceiver(packageInstalledReceiver);
		}
	}
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	private boolean checkPlayServices(boolean showDialog) {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
	    if (resultCode != ConnectionResult.SUCCESS) {
	    	if(showDialog) {
		        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
		            GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
		                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
		        } else {
		            Log.i("MainActivity", "This device is not supported.");
		        }
	    	}
	        return false;
	    }
	    return true;
	}
	
	private void updateRefreshInterval(int valuePos) {
		refreshInterval.setTitle(getString(R.string.settings_title_departure_vision_period) + " (" + refreshInterval.getEntries()[valuePos]+")");
		Map<String,String> k = new HashMap<String,String>();
		k.put("interval", String.valueOf(refreshInterval.getEntries()[valuePos]));
		FlurryAgent.logEvent("DepartureVisionRefreshInterval",k);
	}
	
	public static int getPollMilliseconds() {
		String settings = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).getString(HappyApplication.get().getString(R.string.settings_departure_vision_key_period), "10");
		return Integer.parseInt(settings)*1000;
	}
	
	public static boolean getUseDepartureVision() {
		String settings = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).getString(HappyApplication.get().getString(R.string.settings_departure_vision_key_on), Boolean.TRUE.toString());
		return Boolean.parseBoolean(settings);
	}
	
	public static String getRegistrationId() {
		String regId = WDb.get().getPreference("gcm_registration_id_"+getAppVersion());
		return regId;
	}
	
	public static int getAppVersion() {
	    try {
	    	Context context = HappyApplication.get();
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}

	public static void saveRegistrationId(String id) {
		WDb.get().savePreference("gcm_registration_id_"+getAppVersion(), id);
	}

	@Override
	public void hearShake() {
		System.out.println("shake " + new Date());
		getPreferenceScreen().addPreference(debugScreen);
	}
	
}
