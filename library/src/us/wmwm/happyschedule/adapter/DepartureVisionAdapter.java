package us.wmwm.happyschedule.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import us.wmwm.happyschedule.model.LineStyle;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TrainStatus;
import us.wmwm.happyschedule.views.DepartureVisionHeader;
import us.wmwm.happyschedule.views.DepartureVisionView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

public class DepartureVisionAdapter extends BaseAdapter implements
		StickyListHeadersAdapter {

	List<TrainStatus> statuses;
	Map<String, LineStyle> keyToColor = Collections.emptyMap();
	Map<String, Long> headerToPos = new HashMap<String, Long>();
	StationToStation stationToStation;
	Station station;

	public DepartureVisionAdapter() {
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	public DepartureVisionAdapter(Station station, StationToStation sts) {
		this.stationToStation = sts;
		this.station = station;
	}

	@Override
	public int getCount() {
		if (statuses == null) {
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
		if (view == null) {
			de = new DepartureVisionView(parent.getContext());
		} else {
			de = (DepartureVisionView) view;
		}
		de.setData(getItem(pos), keyToColor);
		return de;
	}

	public void setData(List<TrainStatus> stats) {
		notifyDataSetInvalidated();
		this.statuses = stats;
		headerToPos.clear();
		Iterator<TrainStatus> iter = statuses.iterator();
		String status = "";
		long lastPos = 1;
		while (iter.hasNext()) {
			TrainStatus ts = iter.next();
			boolean canContinue = true;
			if (stationToStation != null) {
				if (stationToStation.blockId.equals(ts.getTrain())) {
					canContinue = true;
				} else {
					canContinue = false;
				}
			}
			if(!canContinue) {
				iter.remove();
				continue;
			}
			if (ts.getStatus().equals(status)) {

			} else {
				status = ts.getStatus();
				lastPos++;
			}
			if (canContinue && !headerToPos.containsKey(ts.getStatus())) {
				headerToPos.put(ts.getStatus(), (long) headerToPos.size() + 1);
			}
		}
		notifyDataSetChanged();
	}

	public void setKeyToColor(Map<String, LineStyle> keyToColor) {
		this.keyToColor = keyToColor;
		notifyDataSetChanged();
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		DepartureVisionHeader h;
		if (convertView == null) {
			h = new DepartureVisionHeader(parent.getContext());
			h.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		} else {
			h = (DepartureVisionHeader) convertView;
		}
		String status = getItem(position).getStatus();
		if (TextUtils.isEmpty(status)) {
			status = "SCHEDULED";
		}
		h.setData(status);
		return h;
	}

	@Override
	public long getHeaderId(int position) {
		return headerToPos.get(getItem(position).getStatus());
	}

}
