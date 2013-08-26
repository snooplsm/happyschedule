package us.wmwm.happyschedule.fragment;

import us.wmwm.happyschedule.R;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

	ListPreference refreshInterval;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		refreshInterval = (ListPreference) findPreference(getString(R.string.settings_departure_vision_key_period));
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
	
}
