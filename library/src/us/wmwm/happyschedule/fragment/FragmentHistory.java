package us.wmwm.happyschedule.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.views.DepartureVisionHeader;
import us.wmwm.happyschedule.views.HistoryView;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

public class FragmentHistory extends HappyFragment {

	StickyListHeadersListView list;
	
	HistoryAdapter adapter;
	
	View empty;
	
	public interface OnHistoryListener {
		void onHistory(Station from, Station to);
	}
	
	OnHistoryListener onHistoryListener;
	
	public void setOnHistoryListener(OnHistoryListener onHistoryListener) {
		this.onHistoryListener = onHistoryListener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_station_picker, container,false);
		list = (StickyListHeadersListView) view.findViewById(R.id.list);
		list.setAdapter(adapter = new HistoryAdapter(getActivity()));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				HistoryView v = (HistoryView) arg1;
				onHistoryListener.onHistory(v.getFromStation(), v.getToStation());
			}
		});
		empty = view.findViewById(R.id.empty);
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
		if(adapter.getCount()==0) {
			empty.setVisibility(View.VISIBLE);
		} else {
			empty.setVisibility(View.GONE);
		}
		
	}
	
	private static class HistoryAdapter extends CursorAdapter implements StickyListHeadersAdapter {

		SimpleDateFormat DATE;
		
		public HistoryAdapter(Context ctx) {
			super(ctx, null,true);
			DATE = new SimpleDateFormat("MMMM dd");
			swapCursor(WDb.get().getHistory());
		}
		
		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			DepartureVisionHeader h = new DepartureVisionHeader(parent.getContext());
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(getItem(position).getLong(2));
			h.setData(DATE.format(cal.getTime()));
			h.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			return h;
		}
		
		@Override
		public Cursor getItem(int position) {
			return (Cursor) super.getItem(position);
		}

		@Override
		public long getHeaderId(int position) {
			Cursor cursor = getItem(position);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(cursor.getLong(2));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis();
		}

		@Override
		public void bindView(View view, Context arg1, Cursor cursor) {
			HistoryView hv = (HistoryView)view;
			hv.setData(cursor);
		}

		@Override
		public View newView(Context ctx, Cursor arg1, ViewGroup arg2) {
			return new HistoryView(ctx);
		}
		
	}
	
}
