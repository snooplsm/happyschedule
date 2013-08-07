package us.wmwm.happyschedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import us.wmwm.happyschedule.FragmentAlarmPicker.OnTimerPicked;
import us.wmwm.happyschedule.ScheduleControlsView.ScheduleControlListener;
import us.wmwm.happyschedule.views.ScheduleView;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

public class FragmentDaySchedule extends Fragment {

	ExpandableListView list;

	Future<?> loadScheduleFuture;
	
	View progressBar;

	Station from;
	Station to;
	Date day;
	Handler handler = new Handler();

	BaseExpandableListAdapter adapter;
	
	AlarmManager alarmManger;

	NotificationManager notifs;
	
	public interface OnDateChange {
		void onDateChange(Calendar cal);
	}

	OnDateChange onDateChange;

	public void setOnDateChange(OnDateChange onDateChange) {
		this.onDateChange = onDateChange;
	}

	private static final String TAG = FragmentDaySchedule.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(!isVisible()||!isAdded()) {
			return;
		}
		inflater.inflate(R.menu.menu_schedule_day, menu);		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		if (item.getItemId() == R.id.menu_go_to_next_train) {
			moveToNextTrain();
		}
		if(item.getItemId()== android.R.id.home) {
			getActivity().onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	private void moveToNextTrain() {
		Calendar now = Calendar.getInstance();
		int usablePosition = 0;
		for (int i = 0; i < adapter.getGroupCount(); i++) {
			StationToStation s = (StationToStation) adapter.getGroup(i);
			if (s.departTime.after(now)) {
				usablePosition = i;
				break;
			}
		}
		if (usablePosition > 1) {
			usablePosition--;
		}
		list.setSelectionFromTop(usablePosition, 0);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (DateUtils.isToday(day.getTime())) {
			menu.removeItem(R.id.menu_go_to_today);
		} else {
			menu.removeItem(R.id.menu_go_to_next_train);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_day_schedule, container,
				false);
		list = (ExpandableListView) view.findViewById(R.id.list2);
		progressBar = view.findViewById(R.id.progress);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		alarmManger = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		notifs = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		Bundle b = getArguments();
		from = (Station) b.getSerializable("from");
		to = (Station) b.getSerializable("to");
		day = (Date) b.getSerializable("date");
		adapter = new BaseExpandableListAdapter() {

			@Override
			public int getGroupCount() {
				return getCount();
			}

			// @Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ScheduleView view = (ScheduleView) convertView;
				if (view == null) {
					view = new ScheduleView(parent.getContext());
				}
				view.setData(getItem(position));
				return view;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				return getView(groupPosition, convertView, parent);
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return null;
			}
			
			int TYPE_CONTROLS = 0;

			@Override
			public int getChildType(int groupPosition, int childPosition) {
				return childPosition;
			}

			@Override
			public int getChildTypeCount() {
				return 1;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return 1;
			}

			@Override
			public View getChildView(final int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {
				if (childPosition == TYPE_CONTROLS) {
					ScheduleControlsView v = new ScheduleControlsView(parent.getContext());
					final StationToStation sts = getItem(groupPosition);
					v.setListener(new ScheduleControlListener() {

						@Override
						public void onTimer() {
							FragmentAlarmPicker fdp = FragmentAlarmPicker.newInstance(sts);
							fdp.setOnTimerPicked(new OnTimerPicked() {
								
								@Override
								public void onTimer(Type type, Calendar cal, StationToStation stationToStation) {
									Intent i = AlarmActivity.from(getActivity(), stationToStation, cal, type);
									Alarm alarm = (Alarm) i.getSerializableExtra("alarm");
									Alarms.saveAlarm(getActivity(), alarm);
									PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, i , 0);
									PendingIntent dismiss = PendingIntent.getService(getActivity(), 0, new Intent(getActivity(), HappyScheduleService.class).putExtra("alarm", alarm).setData(Uri.parse("http://wmwm.us?type=alarm&action=dismiss&id="+alarm.getId())), 0);
									NotificationCompat.Builder b = new NotificationCompat.Builder(getActivity());
									NotificationCompat.BigTextStyle bs = new NotificationCompat.BigTextStyle(b);
									
									bs.setBigContentTitle(getString(R.string.app_name) + " " + alarm.getType().name().toLowerCase() + " alarm");
									StringBuilder text = new StringBuilder(alarm.getType().name().toLowerCase());
									text.replace(0, 1, text.substring(0,1).toUpperCase());
									String typet = text.toString();
									text.append(" alarm set for ");
									if(!DateUtils.isToday(alarm.getTime().getTimeInMillis())) {
										text.append(new SimpleDateFormat("MMM d").format(alarm.getTime().getTime())).append(" at ");
									}
									text.append(DateFormat.getTimeInstance(DateFormat.SHORT).format(alarm.getTime().getTime()).toLowerCase());
									text.append(".  For train #" + stationToStation.blockId + " departing from " + Db.get().getStop(stationToStation.departId).getName() + " arriving at " + Db.get().getStop(stationToStation.arriveId).getName()+".");
									bs.bigText(text.toString());
									b.addAction(R.drawable.ic_action_cancel, getString(R.string.notif_alarm_dismiss), dismiss);
									b.setContentText(text.toString());
									b.setContentTitle(getString(R.string.app_name) + " " + alarm.getType().name().toLowerCase() + " alarm");
									//bs.setSummaryText(text.toString());
									b.setOngoing(true);
									b.setSmallIcon(R.drawable.stat_notify_alarm);
									Notification notif = b.build();
									notif.tickerText = typet + " alarm to go off in " + FragmentAlarmPicker.buildMessage(alarm.getTime()).toString();
									System.out.println("notif : " + alarm.getId());
									notifs.notify(alarm.getId().hashCode(), notif);
									alarmManger.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
								}
							});
							fdp.show(getFragmentManager(), "alarmPicker");
							//FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
							//ft.replace(R.id.fragment_alarm_picker, fdp);
							//ft.commit();							
						}

						@Override
						public void onTrips() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onPin() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onFavorite() {
							// TODO Auto-generated method stub
							
						}
						
					});
					return v;
				}
				return null;
			}

			@Override
			public StationToStation getGroup(int groupPosition) {
				return getItem(groupPosition);
			}

			// @Override
			// public long getItemId(int position) {
			// // TODO Auto-generated method stub
			// return 0;
			// }

			@Override
			public long getGroupId(int groupPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			// @Override
			public StationToStation getItem(int position) {
				return o.get(position);
			}

			// @Override
			public int getCount() {
				if (o == null) {
					return 0;
				}
				System.out.println(o.size());
				return o.size();
			}

			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public long getCombinedChildId(long groupId, long childId) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public long getCombinedGroupId(long groupId) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		list.setAdapter(adapter);
		loadSchedule();
	}

	List<StationToStation> o = null;
	
	List<StationToStation> k = null;

	private void loadSchedule() {
		if (loadScheduleFuture != null) {
			loadScheduleFuture.cancel(true);
		}
		Runnable load = new Runnable() {
			@Override
			public void run() {
				Calendar date = Calendar.getInstance();
				date.setTime(day);
				final Calendar tomorrow = Calendar.getInstance();
				tomorrow.setTime(day);
				tomorrow.add(Calendar.DAY_OF_YEAR, 1);
				Schedule schedule = null;
				try {
					schedule = ScheduleDao.get().getSchedule(from.id, to.id,
							day, day);
					final Calendar limit = Calendar.getInstance();
					limit.setTime(schedule.end);
					Calendar start = Calendar.getInstance();
					start.setTime(schedule.start);
					final boolean isToday = DateUtils.isToday(start
							.getTimeInMillis());
					if (isToday) {
						limit.add(Calendar.DAY_OF_YEAR, 1);
					} else {
						limit.add(Calendar.DAY_OF_YEAR, 0);
						limit.set(Calendar.HOUR_OF_DAY, 0);
						limit.set(Calendar.MINUTE, 0);
						limit.set(Calendar.SECOND, 0);
						limit.set(Calendar.MILLISECOND, 0);
					}

					final Calendar priorLimit = Calendar.getInstance();
					priorLimit.setTime(day);
					// priorLimit.add(Calendar.HOUR_OF_DAY, -2);
					k = new ArrayList<StationToStation>();
					schedule.inOrderTraversal(new ScheduleTraverser() {

						@Override
						public void populateItem(int index,
								StationToStation stationToStation, int total) {
							//k.add(stationToStation);
							if (!isToday) {
								if (!stationToStation.departTime.before(limit) && !stationToStation.departTime.after(tomorrow)) {
									// System.out.println(stationToStation.departTime.getTime());
									k.add(stationToStation);
								}
							} else {
								if (!stationToStation.departTime
										.before(priorLimit)
										&& !stationToStation.departTime
												.after(limit))
									k.add(stationToStation);

							}

						}
					});
					handler.post(populateAdpter);
					Log.i(TAG, "SUCCESSFUL SCHEDULE");
				} catch (Exception e) {
					Log.e(TAG, "UNSUCCESSFUL SCHEDULE", e);
				}

			}
		};
		loadScheduleFuture = ThreadHelper.getScheduler().submit(load);
	}

	Runnable populateAdpter = new Runnable() {
		@Override
		public void run() {
			Activity activity = getActivity();
			if (activity == null) {
				Log.e(TAG,"ACTIVITY IS NULL");
				return;
			}
			o = k;
			adapter.notifyDataSetChanged();
			if (DateUtils.isToday(day.getTime())) {
				moveToNextTrain();
			}
			progressBar.setVisibility(View.GONE);
		}
	};

	@Override
	public void onDestroy() {
		handler.removeCallbacks(populateAdpter);
		if (loadScheduleFuture != null) {
			loadScheduleFuture.cancel(true);
		}
		super.onDestroy();

	}

	public static FragmentDaySchedule newInstance(Station from, Station to,
			Date date) {
		FragmentDaySchedule f = new FragmentDaySchedule();
		Bundle b = new Bundle();
		b.putSerializable("from", from);
		b.putSerializable("to", to);
		b.putSerializable("date", date);
		f.setArguments(b);
		return f;
	}

}
