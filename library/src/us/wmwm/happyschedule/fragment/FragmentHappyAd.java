package us.wmwm.happyschedule.fragment;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.AppAd;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

public abstract class FragmentHappyAd extends HappyFragment {

	protected AppAd ad;
	protected View root;
	
	public interface DiscardListener {
		void onDiscard(AppAd ad);
	}
	
	protected DiscardListener discardListener;
	
	public void setDiscardListener(DiscardListener discardListener) {
		this.discardListener = discardListener;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();
		ad = (AppAd) args.getSerializable("ad");
		String height = ad.getHeight();
		int heightW = (int) getResources().getDimension(R.dimen.ad_height);
		if(height!=null) {
			if("match_parent".equalsIgnoreCase(height)) {
				heightW = LayoutParams.MATCH_PARENT;
			} else {
				heightW = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Integer.parseInt(height), getResources().getDisplayMetrics());
			}
		}
		root.getLayoutParams().height = heightW;
		root.getParent().requestLayout();
	}
	
	public static FragmentHappyAd newIntance(AppAd ad) {
		Bundle b = new Bundle();
		b.putSerializable("ad", ad);
		FragmentHappyAd fad = null;
		if(!TextUtils.isEmpty(ad.getWebviewUrl())) {
			fad = new FragmentWebviewAd();
		}
		fad.setArguments(b);
		return fad;
	}
	
}
