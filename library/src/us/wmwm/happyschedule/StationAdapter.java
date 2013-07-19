package us.wmwm.happyschedule;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

public class StationAdapter extends CursorAdapter implements StickyListHeadersAdapter {
	
	List<String> letters = new ArrayList<String>();
	
	public StationAdapter(Context context) {
		super(context, null, true);
		char A = 'A';
		int AIND = (int)A;
		int max = ((int)A) + 26;
		for(int i = 0; i < 26; i++) {
			A = (char)(((int)AIND) + (i));
			for(int j = 0; j < 10; j++) {
				letters.add(String.valueOf(A));
			}
		}
		swapCursor(Db.get().getStops());
	}

	@Override
	public Cursor getItem(int position) {
		Cursor cursor = (Cursor) super.getItem(position);
		return cursor;
	}

	public String getId(Cursor cursor) {
		return cursor.getString(0);
	}
	
	public String getName(Cursor cursor) {
		return cursor.getString(2);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StationView t = new StationView(parent.getContext());
		t.setData(getId(getItem(position)), getName(getItem(position)));
		return t;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(parent.getContext());
		tv.setText(getName(getItem(position)).charAt(0)+"");
		tv.setBackgroundColor(Color.GRAY);
		tv.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, (int) parent.getContext().getResources().getDimension(R.dimen.header_height)));
		return tv;
	}

	@Override
	public long getHeaderId(int position) {
		return (int) getName(getItem(position)).charAt(0);
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		StationView t = (StationView) arg0;
		t.setData(getId(arg2), getName(arg2));
	}

	@Override
	public View newView(Context ctx, Cursor c, ViewGroup parent) {
		StationView t = new StationView(parent.getContext());
		return t;
	}

}
