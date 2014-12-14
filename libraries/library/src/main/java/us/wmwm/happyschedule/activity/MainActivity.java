package us.wmwm.happyschedule.activity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.json.JSONObject;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.fragment.FacebookLoginFragment;
import us.wmwm.happyschedule.fragment.FacebookLoginFragment.PurchaseListener;
import us.wmwm.happyschedule.fragment.FragmentLoad;
import us.wmwm.happyschedule.fragment.FragmentLoad.OnLoadListener;
import us.wmwm.happyschedule.fragment.FragmentMain;
import us.wmwm.happyschedule.util.PremiumUserHelper;
import us.wmwm.happyschedule.views.BackListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;

import com.android.vending.billing.IInAppBillingService;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

public class MainActivity extends HappyActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

	FragmentLoad fragmentLoad;

	UiLifecycleHelper helper;

	Future<?> loadFacebookUser;

	StatusCallback statusCallback = new StatusCallback() {
		@Override
		public void call(final Session session, SessionState state,
				Exception exception) {
//			if (state != null && state == SessionState.OPENED) {
//				showMain();
//				Calendar cal = Calendar.getInstance();
//				cal.setTimeInMillis(PremiumUserHelper.getFacebookUserSaved());
//				Calendar oneMonthPrior = Calendar.getInstance();
//				oneMonthPrior.add(Calendar.MONTH, -1);
//				if (cal.before(oneMonthPrior)) {
//					loadFacebookUser = ThreadHelper.getScheduler().submit(
//							new Runnable() {
//								@Override
//								public void run() {
//
//									Request meRequest = Request.newMeRequest(
//											session,
//											new Request.GraphUserCallback() {
//
//												@Override
//												public void onCompleted(
//														final GraphUser user,
//														Response response) {
//													if (user != null) {
//														PremiumUserHelper
//																.setFacebookUser(
//																		user,
//																		session);
//													}
//												}
//											});
//									Request friendsRequest = Request
//											.newMyFriendsRequest(
//													session,
//													new Request.GraphUserListCallback() {
//
//														@Override
//														public void onCompleted(
//																List<GraphUser> users,
//																Response response) {
//															if (users != null) {
//																PremiumUserHelper
//																		.setFacebookUserFriends(
//																				users,
//																				session);
//															}
//														}
//													});
//									Bundle b = new Bundle();
//									b.putString("fields", "id,name,picture");
//									friendsRequest.setParameters(b);
//									Bundle email = new Bundle();
//									email.putString(
//											"fields",
//											"id,about,age_range,bio,birthday,context,cover,currency,education,email,favorite_athletes,favorite_teams,gender,hometown,is_verified,languages,last_name,link,location,middle_name,name,name_format,significant_other,verified,website,work,education");
//									meRequest.setParameters(email);
//									Request.executeBatchAndWait(meRequest,
//											friendsRequest);
//								}
//							});
//				}
//			}
		}
	};

	public static IInAppBillingService BILLING_SERVICE;

	ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			BILLING_SERVICE = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			BILLING_SERVICE = IInAppBillingService.Stub.asInterface(service);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		bindService(new Intent(
//				"com.android.vending.billing.InAppBillingService"),
//				mServiceConn, Context.BIND_AUTO_CREATE);
		setContentView(R.layout.activity_main);
		helper = new UiLifecycleHelper(this, statusCallback);
		helper.onCreate(savedInstanceState);
		// getActionBar().setDisplayShowHomeEnabled(false);
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
											showMain();
										}
									})).commit();
		} else {
			showMain();
		}
	}

	private void showMain() {
		if (PremiumUserHelper.isPremiumUser()) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_main, new FragmentMain()).commit();
		} else {
			showFacebook();
		}
	}

	private void showFacebook() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_main, new FacebookLoginFragment().setPurchaseListener(purchaseListener))
				.commit();
	}

	PurchaseListener purchaseListener = new PurchaseListener() {
		@Override
		public void onPurchased() {
			showMain();
		}
	};
	
	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		supportInvalidateOptionsMenu();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// checkPlayServices();
		helper.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		helper.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		helper.onStop();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		helper.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 100) {
			int responseCode = arg2.getIntExtra("RESPONSE_CODE", 0);
			String purchaseData = arg2.getStringExtra("INAPP_PURCHASE_DATA");
			String dataSignature = arg2.getStringExtra("INAPP_DATA_SIGNATURE");

			if (arg1 == RESULT_OK) {
				try {
					JSONObject jo = new JSONObject(purchaseData);
					String sku = jo.getString("productId");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		helper.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		helper.onDestroy();
		if (BILLING_SERVICE != null) {
			unbindService(mServiceConn);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if(fragments!=null) {
            for(Fragment fragment :fragments) {
                if(fragment==null) {
                    continue;
                }
                Log.d(TAG, fragment.getClass().getSimpleName());
                if(BackListener.class.isAssignableFrom(fragment.getClass())) {
                    BackListener b = (BackListener)fragment;
                    if(b.onBack()) {
                        return;
                    }
                }
            }
        }
        super.onBackPressed();
    }
}
