package us.wmwm.happyschedule.views;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentRaiLines.RailListener;
import us.wmwm.happyschedule.model.AppRailLine;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RailLineDay extends LinearLayout {

    private static final String TAG = RailLineDay.class.getSimpleName();

	TextView text;
	
	SimpleDateFormat DF = new SimpleDateFormat("EEEE");
	
	RailListener railListener;
	
	int day;
	
	CheckBox all;
	
	AppRailLine appRailLine;
	
	public void setRailListener(RailListener railListener) {
		this.railListener = railListener;
	}
	
	public RailLineDay(Context ctx) {
		super(ctx);
		LayoutInflater.from(ctx).inflate(R.layout.view_day,this);
		text = (TextView) findViewById(R.id.day);
		all = (CheckBox) findViewById(R.id.all);
//		for(int i = 0; i < 24; i++) {
//			int resId = getResources().getIdentifier("h"+i, "id", ctx.getPackageName());
//			CheckBox b = (CheckBox) findViewById(resId);
//			b.setTag(i);
//			//b.setOnCheckedChangeListener(onCheckChanged);
//		}
		all.setOnCheckedChangeListener(onDayCheckChanged);
	}
	
	private OnCheckedChangeListener onCheckChanged = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Log.d(TAG, buttonView.getTag() + " " + isChecked);
			int hour = (Integer)buttonView.getTag();
			railListener.onChecked(appRailLine, day, hour, isChecked);
		}
	};

	private OnCheckedChangeListener onDayCheckChanged = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			for(int i = 0; i < 24; i++) {
				int resId = getResources().getIdentifier("h"+i, "id", getContext().getPackageName());
				CheckBox b = (CheckBox) findViewById(resId);
				//b.setOnCheckedChangeListener(null);				
				b.setTag(i);
				b.setChecked(isChecked);							
				//b.setOnCheckedChangeListener(onCheckChanged);
			}
		}
	};
		
	public void setData(AppRailLine appRailLine, int day, Set<Integer> checked) {
		this.day = day;
		this.appRailLine = appRailLine;
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_WEEK, day);
		text.setText(DF.format(c.getTime()));
		for(int i = 0; i < 24; i++) {
			int resId = getResources().getIdentifier("h"+i, "id", getContext().getPackageName());
			CheckBox b = (CheckBox) findViewById(resId);
			b.setOnCheckedChangeListener(null);
			b.setChecked(checked.contains(i));
			b.setTag(i);
			b.setOnCheckedChangeListener(onCheckChanged);
		}
		if(checked.size()==24) {
			all.setChecked(true);
		} else {
			all.setChecked(false);
		}
		
	}
}
