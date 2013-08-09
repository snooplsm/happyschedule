package us.wmwm.happyschedule.fragment;

import java.util.Date;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.R.id;
import us.wmwm.happyschedule.R.layout;
import us.wmwm.happyschedule.dao.ScheduleDao;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarPickerView.OnDateSelectedListener;

public class FragmentDatePicker extends Fragment {

	CalendarPickerView calendar;
	
	Date day;

	OnDateSelectedListener onDateSelectedListener;
	
	public void setOnDateSelectedListener(
			OnDateSelectedListener onDateSelectedListener) {
		this.onDateSelectedListener = onDateSelectedListener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_date_picker, container,false);
		calendar = (CalendarPickerView) view.findViewById(R.id.calendar_view);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
	}
	
	public static FragmentDatePicker newInstance(Date date) {
		Bundle b = new Bundle();
		b.putSerializable("day", date);
		FragmentDatePicker fdp = new FragmentDatePicker();
		fdp.setArguments(b);
		return fdp;
	}
	
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		day = (Date) getArguments().getSerializable("day");
		Date max = ScheduleDao.get().getMaxDate();
		Date min = ScheduleDao.get().getMinDate();
		calendar.init(min, max)
		    .withSelectedDate(day);
		calendar.setOnDateSelectedListener(onDateSelectedListener);
	}
	
}
