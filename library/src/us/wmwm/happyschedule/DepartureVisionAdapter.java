package us.wmwm.happyschedule;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DepartureVisionAdapter extends BaseAdapter {

	List<TrainStatus> statuses;
	Map<String, LineStyle> keyToColor = Collections.emptyMap();
	
	@Override
	public int getCount() {
		if(statuses==null) {
			return 0;
		}
		return statuses.size();
	}

	@Override
	public TrainStatus getItem(int arg0) {
		return statuses.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		DepartureVisionView de;
		if(view==null) {			
			de = new DepartureVisionView(parent.getContext());
		} else {
			de = (DepartureVisionView)view;
		}
		de.setData(getItem(pos),keyToColor);
		return de;
	}
	
	public void setData(List<TrainStatus> stats) {
		this.statuses = stats;
		notifyDataSetChanged();
	}
	
	public void setKeyToColor(Map<String, LineStyle> keyToColor) {
		this.keyToColor = keyToColor;
		notifyDataSetChanged();
	}

}
