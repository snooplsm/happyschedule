package us.wmwm.happyschedule.fragment;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.activity.RailLinesActivity;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.WDb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;

import com.android.vending.billing.IInAppBillingService;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.seismic.ShakeDetector;

public class SettingsFragment extends PreferenceFragment implements com.squareup.seismic.ShakeDetector.Listener, IPrimary {

	ListPreference refreshInterval;
	Preference railLine;
	CheckBoxPreference pushNotifications;
	PreferenceCategory debugScreen;
	Preference pushId;
	Preference version;
	Preference packageName;
    Preference subscription;

    public static interface OnPurchaseClickedListener {
        void onPurchaseClicked();
    }

    OnPurchaseClickedListener onPurchaseClickedListener;

    public void setOnPurchaseClickedListener(OnPurchaseClickedListener onPurchaseClickedListener) {
        this.onPurchaseClickedListener = onPurchaseClickedListener;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setPadding(view.getPaddingLeft(),view.getPaddingTop(),view.getPaddingRight(),(int)getResources().getDimension(R.dimen.ad_height));
    }

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
		version = (Preference) findPreference(getString(R.string.settings_key_debug_version));
		packageName = (Preference) findPreference(getString(R.string.settings_key_debug_package));
		subscription = (Preference) findPreference(getString(R.string.settings_key_purchase_subscription));

		railLine.setIntent(new Intent(getActivity(), RailLinesActivity.class));
		refreshInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateRefreshInterval(refreshInterval.findIndexOfValue(String.valueOf(newValue)));
								
				return true;
			}
		});

        subscription.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onPurchaseClickedListener.onPurchaseClicked();
                return false;
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
		//getPreferenceScreen().removePreference(debugScreen);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		pushId.setSummary(getRegistrationId());
		if(pushId.getSummary()==null || pushId.getSummary().length()==0) {
			
		} else {
			pushId.setIntent(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,pushId.getSummary().toString()), "Share"));
		}
		version.setSummary(getAppVersionName() + " / " + getAppVersion());
		packageName.setSummary(getActivity().getPackageName());
        if(WDb.get().getPreference("rails.monthly")!=null) {
            getView().setPadding(0,0,0,0);
        }
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
		Boolean settings = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).getBoolean(HappyApplication.get().getString(R.string.settings_departure_vision_key_on), Boolean.TRUE);
		return settings;
	}
	
	public static String getRegistrationId() {
		String regId = WDb.get().getPreference("gcm_registration_id_"+getAppVersion());
		return regId;
	}

    public static Map<String,String> getAllRegistrationIds() {
        Map<String,String> prefs = WDb.get().getPreferences("gcm_registration_id_*");
        return prefs;
    }

    public void deleteOldRegistrationIds() {

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
	
	public static String getAppVersionName() {
	    try {
	    	Context context = HappyApplication.get();
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionName;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}


	public static void saveRegistrationId(String id) {
		WDb.get().savePreference("gcm_registration_id_"+getAppVersion(), id);
	}

    @Override
    public void setPrimaryItem() {
        pushId.setSummary(getRegistrationId());
        String pp = WDb.get().getPreference("rails.monthly");
        if(pp!=null) {
            subscription.setSummary("Monthly subscription active.");
        } else {
            subscription.setSummary("No monthly subscription found.  Click to activate.");
        }
    }

    @Override
	public void hearShake() {
		getPreferenceScreen().addPreference(debugScreen);
	}
	
}
