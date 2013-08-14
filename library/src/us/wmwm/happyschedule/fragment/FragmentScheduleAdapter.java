package us.wmwm.happyschedule.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.views.ScheduleControlsView.ScheduleControlListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.format.DateUtils;
import android.view.ViewGroup;

public class FragmentScheduleAdapter extends FragmentStatePagerAdapter {

	public FragmentScheduleAdapter(Station from, Station to, FragmentManager fm) {
		super(fm);
		this.from = from;
		this.to = to;		
	}

	List<Date> day;
	Date min;
	Date max;
	int days;
	Station from;
	Station to;
	DateFormat SHORT = new SimpleDateFormat("E M/d");
	
	Object last;

	@Override
	public int getCount() {
		if(days!=0) {
			return days;
		}
		min = ScheduleDao.get().getMinDate();
		max = ScheduleDao.get().getMaxDate();
		days = (int) ((max.getTime() - min.getTime()) / 86400000);
		return days;
	}
	
	public Calendar getCalendar(int pos) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(min);
		cal.add(Calendar.DAY_OF_YEAR, pos);
		return cal;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		if(object == last) {
			if(object instanceof IPrimary) {
				((IPrimary)object).setPrimaryItem();
			}
		} else {
			if(last!=null && last instanceof ISecondary) {
				((ISecondary)last).setSecondary();
			}
			last = object;
		}
	}
	
	@Override
	public Fragment getItem(int pos) {
		Calendar cal = getCalendar(pos);
		FragmentDaySchedule fds = 
		 FragmentDaySchedule.newInstance(from, to, cal.getTime());
		fds.setScheduleControlListener(controlListener);
		return fds;
	}
	
	public CharSequence getPageTitle(int position) {
		return "  " + getPageTitle2(position)+"  ";
	}
	
	private CharSequence getPageTitle2(int position) {
		Calendar cal = getCalendar(position);
		if(DateUtils.isToday(cal.getTimeInMillis())) {
			return "Today";
		}
//		
//		Calendar tom = Calendar.getInstance();
//		tom.add(Calendar.DAY_OF_YEAR, 1);
//		clear(tom);
//		Calendar tomafter = Calendar.getInstance();
//		tomafter.add(Calendar.DAY_OF_YEAR, 2);
//		clear(tomafter);
//		Calendar today = Calendar.getInstance();
//		clear(today);
//		if(cal.before(tomafter) && cal.after(today)) {
//			return "Tomorrow";
//		}
//		Calendar yesterday = Calendar.getInstance();
//		yesterday.add(Calendar.DAY_OF_YEAR, -1);
//		Calendar yesterbefore = Calendar.getInstance();
//		yesterbefore.add(Calendar.DAY_OF_YEAR, -2);
//		clear(yesterday);
//		clear(yesterbefore);
//		if(cal.after(yesterbefore) && cal.before(today)) {
//			return "Yesterday";
//		}
		return SHORT.format(cal.getTime());
	};

	public static void clear(Calendar c) {
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	}
	
	public int getPositionFor(Date date) {
		Calendar k = Calendar.getInstance();
		k.setTime(date);
		Calendar start = getCalendar(0);
		long diff = k.getTimeInMillis()-start.getTimeInMillis();
		long days = diff / 86400000;
		return (int) days;
	}
	
	public int getTodaysPosition() {
		Calendar cal = Calendar.getInstance();
		int days = (int) ((cal.getTimeInMillis() - min.getTime()) / 86400000);
		return days;
	}

	ScheduleControlListener controlListener;
	
	public void setControlListener(ScheduleControlListener controlListener) {
		this.controlListener = controlListener;
	}
}
