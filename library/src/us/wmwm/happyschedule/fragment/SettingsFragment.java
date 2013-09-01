package us.wmwm.happyschedule.fragment;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.activity.RailLinesActivity;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.WDb;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {

	ListPreference refreshInterval;
	Preference railLine;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		refreshInterval = (ListPreference) findPreference(getString(R.string.settings_departure_vision_key_period));
		railLine = (Preference) findPreference(getString(R.string.settings_key_rail_lines));
		railLine.setIntent(new Intent(getActivity(), RailLinesActivity.class));
		refreshInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateRefreshInterval(refreshInterval.findIndexOfValue(String.valueOf(newValue)));
				return true;
			}
		});
		int pos = refreshInterval.findIndexOfValue(refreshInterval.getValue());
		updateRefreshInterval(pos);
	}
	
	private void updateRefreshInterval(int valuePos) {
		refreshInterval.setTitle(getString(R.string.settings_title_departure_vision_period) + " (" + refreshInterval.getEntries()[valuePos]+")");
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
	
	private static int getAppVersion() {
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
	
}
