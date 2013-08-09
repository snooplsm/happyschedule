package us.wmwm.happyschedule.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.R.id;
import us.wmwm.happyschedule.R.layout;
import us.wmwm.happyschedule.R.string;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.Type;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

public class FragmentAlarmPicker extends DialogFragment {
		
	Calendar cal;
	StationToStation sts;
	TextView date;
	TextView hour;
	TextView minute;
	TextView second;
	TextView ampm;
	View departButton;
	View arriveButton;
	TextView alarmText;
	View timePicker;
	View chooseType;
	TextView type;
	Type typeType;
	View okButton;
	View cancelButton;
	
	Handler handler = new Handler();
	
	public interface OnTimerPicked {
		void onTimer(Type type, Calendar cal, StationToStation stationToStation);
	}
	
	OnTimerPicked onTimerPicked;
	
	public void setOnTimerPicked(OnTimerPicked onTimerPicked) {
		this.onTimerPicked = onTimerPicked;
	}
	
	SimpleDateFormat DATE = new SimpleDateFormat("MMM d");
	
	SimpleDateFormat HOUR = new SimpleDateFormat("hh");
	
	SimpleDateFormat MIN = new SimpleDateFormat("mm");
	
	SimpleDateFormat SEC = new SimpleDateFormat("ss");
	
	SimpleDateFormat AM = new SimpleDateFormat("a");
	
	View root;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_alarm_picker, container,false);
		date = (TextView) view.findViewById(R.id.date);
		hour = (TextView) view.findViewById(R.id.hour);
		minute = (TextView) view.findViewById(R.id.minute);
		second = (TextView) view.findViewById(R.id.second);
		ampm = (TextView) view.findViewById(R.id.ampm);
		departButton = view.findViewById(R.id.depart_button);
		arriveButton = view.findViewById(R.id.arrive_button);
		chooseType = view.findViewById(R.id.choose_type);
		timePicker = view.findViewById(R.id.time_picker);
		okButton = view.findViewById(R.id.set_button);
		cancelButton = view.findViewById(R.id.cancel_button);
		type = (TextView) view.findViewById(R.id.type);
		root = view;
		alarmText = (TextView) view.findViewById(R.id.alarm_text);
		return view;		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog d = super.onCreateDialog(savedInstanceState);
		d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return d;
	}
	
	OnClickListener onClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int val = 0; 
			if(v.getId()==R.id.pos_button) {
				val = 1;
			} else {
				val = -1;
			}
			Object tag = v.getTag();
			int field = 0;
			if("hour".equals(tag)) {
				field = Calendar.HOUR_OF_DAY;
			}else
			if("minute".equals(tag)) {
				field = Calendar.MINUTE;
			}else
			if("second".equals(tag)) {
				field = Calendar.SECOND;
			}
			cal.add(field, val);
			populateValues();
		}
	};
	
	OnClickListener okCancel = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v==okButton) {
				onTimerPicked.onTimer(typeType, (Calendar) cal.clone(), sts);
			}
			if(v==cancelButton) {
				
			}
			dismiss();
		}
	};
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();

		sts = (StationToStation)b.getSerializable("sts");
		Stack<ViewGroup> views = new Stack<ViewGroup>();
		views.add((ViewGroup)root);
		while(!views.isEmpty()) {
			ViewGroup vg = views.pop();
			for(int i = 0; i < vg.getChildCount(); i++) {
				View child = vg.getChildAt(i);
				if(child.getId()==R.id.pos_button || child.getId()==R.id.neg_button) {
					child.setOnClickListener(onClick);
				}
				if(child instanceof ViewGroup) {
					views.add((ViewGroup)child);
				}
			}
		}
		
		OnClickListener onButton = new OnClickListener() {
			@Override
			public void onClick(View v) {			
				final int resourceId;
				if(v.getId()==R.id.depart_button) {
					typeType = Type.DEPARTURE;
					resourceId = R.string.fragment_alarm_type_depart;
				} else {
					resourceId = R.string.fragment_alarm_type_arrive;
					typeType = Type.ARRIVAL;
				}
				chooseType.setVisibility(View.GONE);
				type.setText(resourceId);
				cal = Calendar.getInstance();
				cal.setTime(typeType.equals(Type.DEPARTURE) ? sts.departTime.getTime() : sts.arriveTime.getTime());
				populateValues();
				timePicker.setVisibility(View.VISIBLE);
			}
		};
		departButton.setOnClickListener(onButton);
		arriveButton.setOnClickListener(onButton);
		okButton.setOnClickListener(okCancel);
		cancelButton.setOnClickListener(okCancel);
//		Date d = (Date)b.getSerializable("date");
//		cal = Calendar.getInstance();
//		cal.setTime(d);
//		populateValues();
		//picker.set
//		picker.setDateTime(cal);
//		picker.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
			
	}

	Future<?> timerFuture;
	
	private void scheduleTimer() {
		if(timerFuture!=null) {
			timerFuture.cancel(true);
		}
		timerFuture = ThreadHelper.getScheduler().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				handler.post(popRuner);
			}
			
		},1500,1000,TimeUnit.MILLISECONDS);
	}
	
	public void onPause() {
		super.onPause();
		if(timerFuture!=null) {
			timerFuture.cancel(true);
		}
	};
	
	public void onResume() {
		super.onResume();
		scheduleTimer();
	};
	
	Runnable popRuner = new Runnable() {
		@Override
		public void run() {
			populateTime();
		}
	};
	
	private void populateTime() {
		if(cal==null) {
			return;
		}
		
		alarmText.setText(buildMessage(cal).toString());
	}
	
	public static StringBuilder buildMessage(Calendar cal) {
		long diff = cal.getTimeInMillis() - System.currentTimeMillis();
		long hours = diff / 3600000;
		diff = diff % 3600000;
		long mins = diff / 60000;
		diff = diff % 60000;
		long seconds = diff / 1000;
		StringBuilder b = new StringBuilder();
		if(hours!=0) {
			b.append(hours).append("h");
		}
		if(mins!=0) {
			b.append(mins).append("m");
		}
		if(seconds!=0) {
			if(seconds/10.0 < 1) {
				b.append("0");
			}
			b.append(seconds).append("s");
		}
		return b;
	}

	private void populateValues() {
		Date d = cal.getTime();
		date.setText(DATE.format(d));
		hour.setText(HOUR.format(d));
		minute.setText(MIN.format(d));
		second.setText(SEC.format(d));
		ampm.setText(AM.format(d).toLowerCase());	
		populateTime();
	}
	
	public static FragmentAlarmPicker newInstance(StationToStation date) {
		FragmentAlarmPicker p = new FragmentAlarmPicker();
		Bundle b = new Bundle();
		b.putSerializable("sts", date);
		p.setArguments(b);
		return p;
	}

}
