package us.wmwm.happyschedule;

import java.util.ArrayList;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StationAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	
	List<String> letters = new ArrayList<String>();
	
	public StationAdapter() {
		char A = 'A';
		int AIND = (int)A;
		int max = ((int)A) + 26;
		for(int i = 0; i < 26; i++) {
			A = (char)(((int)AIND) + (i));
			for(int j = 0; j < 10; j++) {
				letters.add(String.valueOf(A));
			}
		}
	}
	
	@Override
	public int getCount() {
		return letters.size();
	}

	@Override
	public String getItem(int position) {
		return letters.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StationView t = new StationView(parent.getContext());
		t.setData(getItem(position));
		return t;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(parent.getContext());
		tv.setText(getItem(position));
		tv.setBackgroundColor(Color.GRAY);
		tv.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		return tv;
	}

	@Override
	public long getHeaderId(int position) {
		return (int) getItem(position).charAt(0);
	}

}
