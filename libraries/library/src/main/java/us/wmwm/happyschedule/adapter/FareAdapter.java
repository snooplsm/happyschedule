package us.wmwm.happyschedule.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FareAdapter extends BaseAdapter {

	Map<String,Double> fares;
	List<String> keys;
	
	@Override
	public int getCount() {
		return fares.size();
	}
	
	public void setData(Map<String,Double> fares) {
		keys = new ArrayList<String>(fares.keySet());
		this.fares = fares;
	}

	@Override
	public Object getItem(int arg) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		TextView t = (TextView) view;
		if(view==null) {
			t = new TextView(parent.getContext());
		}
		String key = keys.get(pos);
		String fare = DecimalFormat.getCurrencyInstance()
				.format(fares.get(key));
		t.setText(key + ": " + fare);
		return t;
	}

}
