package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.concurrent.Future;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.MainActivity;
import us.wmwm.happyschedule.util.PremiumUserHelper;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;

import com.facebook.widget.LoginButton;


public class FacebookLoginFragment extends Fragment {

	LoginButton button;
	
	Button purchaseButton;
	
	Future<?> queryPurchase;

	Handler handler = new Handler();
	
	View ignoreButtonContainer;
	Button ignoreButton;
	
	
	private static final String SKU = "rails.monthly";
	
	public interface PurchaseListener {
		void onPurchased();
	}
	
	PurchaseListener purchaseListener;
	
	public FacebookLoginFragment setPurchaseListener(PurchaseListener listener) {
		this.purchaseListener = listener;
		return this;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_facebook_login, container,false);
		button = (LoginButton) root.findViewById(R.id.login_button);
		purchaseButton = (Button) root.findViewById(R.id.purchase_button);
		ignoreButtonContainer = root.findViewById(R.id.ignore_button_container);
		ignoreButton = (Button) root.findViewById(R.id.ignore_button);
		return root;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		button.setReadPermissions("user_friends","user_about_me");
		purchaseButton.setEnabled(true);
		purchaseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Bundle bundle = MainActivity.BILLING_SERVICE.getBuyIntent(3, getActivity().getPackageName(),
							   SKU, "subs", null);

							PendingIntent pendingIntent = bundle.getParcelable("BUY_INTENT");
							if (bundle.getInt("RESPONSE_CODE") == 0) {
							   // Start purchase flow (this brings up the Google Play UI).
							   // Result will be delivered through onActivityResult().														
							   getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(), Integer.valueOf(100), new Intent(),
							       Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
							}
				} catch (Exception e) {
					
				}
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(queryPurchase!=null) {
			queryPurchase.cancel(true);
		}
		
		queryPurchase = ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				try {
					Bundle ownedItems = MainActivity.BILLING_SERVICE.getPurchases(3, getActivity().getPackageName(), "subs", null);
					int response = ownedItems.getInt("RESPONSE_CODE");
					if (response == 0) {
					   ArrayList<String> ownedSkus =
					      ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
					   ArrayList<String>  purchaseDataList =
					      ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
					   ArrayList<String>  signatureList =
					      ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
					   String continuationToken = 
					      ownedItems.getString("INAPP_CONTINUATION_TOKEN");
					   PremiumUserHelper.setPaidUser(false);
					   for (int i = 0; i < purchaseDataList.size(); ++i) {
					      String purchaseData = purchaseDataList.get(i);
					      String signature = signatureList.get(i);
					      String sku = ownedSkus.get(i);
					      if(SKU.equals(sku)) {
					    	  PremiumUserHelper.setPaidUser(true);
					      }
					      // do something with this purchase information
					      // e.g. display the updated list of products owned by user
					   } 
					   
					   if(PremiumUserHelper.isPaidUser()) {
						   handler.post(new Runnable() {
							  public void run() {
								  purchaseListener.onPurchased();
							  }; 
						   });
						   return;
					   } else {
						   handler.post(new Runnable() {
							  @Override
							public void run() {
	
							} 
						   });
					   }

					   // if continuationToken != null, call getPurchases again 
					   // and pass in the token to retrieve more items
					}
				} catch (RemoteException e) {
				}
			}
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				AlphaAnimation a = new AlphaAnimation(0, 1);
				a.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						ignoreButtonContainer.setVisibility(View.VISIBLE);
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						
					}
				});
				ignoreButtonContainer.startAnimation(a);
			}
		}, 3000);
	}

}
