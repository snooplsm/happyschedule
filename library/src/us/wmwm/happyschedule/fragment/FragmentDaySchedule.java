package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.AlarmActivity;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.fragment.FragmentAlarmPicker.OnTimerPicked;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.ScheduleTraverser;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TrainStatus;
import us.wmwm.happyschedule.model.Type;
import us.wmwm.happyschedule.service.DeparturePoller;
import us.wmwm.happyschedule.views.ScheduleControlsView;
import us.wmwm.happyschedule.views.ScheduleControlsView.ScheduleControlListener;
import us.wmwm.happyschedule.views.ScheduleView;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

public class FragmentDaySchedule extends Fragment implements IPrimary,
		ISecondary {

	public interface OnDateChange {
		void onDateChange(Calendar cal);
	}

	private static final String TAG = FragmentDaySchedule.class.getSimpleName();

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

	boolean activityCreated = false;
	BaseExpandableListAdapter adapter;
	AlarmManager alarmManger;
	boolean canLoad = false;

	OnDepartureVision onDepartureVision;

	public void setOnDepartureVision(OnDepartureVision onDepartureVision) {
		this.onDepartureVision = onDepartureVision;
	}

	BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info == null || !info.isConnected()) {
				// erroText.setVisibility(View.VISIBLE);
			}
			if (info != null && info.isConnected()) {
				// erroText.setVisibility(View.GONE);
				if (poll == null || poll.isCancelled()) {
					poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r,
							100, 10000, TimeUnit.MILLISECONDS);
				}
			} else {
				if (poll != null) {
					poll.cancel(true);
				}
			}

		}
	};

	Date day;

	Station from;

	Handler handler = new Handler();

	Schedule schedule;

	List<StationToStation> k = null;

	List<TrainStatus> lastStatuses;

	Map<String, TrainStatus> tripIdToTrainStatus = new HashMap<String, TrainStatus>();

	ExpandableListView list;

	Future<?> loadScheduleFuture;

	ConnectivityManager manager;

	NotificationManager notifs;

	List<StationToStation> o = null;

	OnDateChange onDateChange;

	OnGetSchedule onGetSchedule;

	public void setOnGetSchedule(OnGetSchedule onGetSchedule) {
		this.onGetSchedule = onGetSchedule;
	}

	Future<?> poll;

	DeparturePoller poller;

	Runnable populateAdpter = new Runnable() {
		@Override
		public void run() {
			Activity activity = getActivity();
			if (activity == null) {
				Log.e(TAG, "ACTIVITY IS NULL");
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

	Runnable updateEverySixtySeconds = new Runnable() {
		public void run() {
			Log.d(FragmentDaySchedule.class.getSimpleName(),
					"updating schedule view");
			handler.post(populateAdpter);
		};
	};

	View progressBar;

	Runnable r = new Runnable() {
		@Override
		public void run() {
			try {
				Log.d(FragmentDaySchedule.class.getSimpleName(),
						"getting train statuses");
				final List<TrainStatus> s = poller.getTrainStatuses(from
						.getDepartureVision());
				Log.d(FragmentDaySchedule.class.getSimpleName(),
						"got train statuses: " + s.size());
				String key = getKey();
				if (s != null && !s.isEmpty()) {
					JSONArray a = new JSONArray();
					if (lastStatuses != null) {
						for (int i = 0; i < lastStatuses.size(); i++) {
							a.put(lastStatuses.get(i).toJSON());
						}
						PreferenceManager
								.getDefaultSharedPreferences(getActivity())
								.edit()
								.putString("lastStation", from.getId())
								.putString(key, a.toString())
								.putString("lastStatuses",
										lastStatuses.toString())
								.putLong(key + "Time",
										System.currentTimeMillis()).commit();
					}
					for (TrainStatus status : s) {
						Log.d(FragmentDaySchedule.class.getSimpleName(),
								status.getTrain() + " : " + status.getStatus());
						tripIdToTrainStatus.put(status.getTrain(), status);
					}
					lastStatuses = s;
				}
				handler.post(new Runnable() {
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	Station to;

	Map<StationToStation, List<Alarm>> tripIdToAlarm;

	private void addAlarm(Alarm alarm) {
		List<Alarm> alarms = tripIdToAlarm.get(alarm.getStationToStation());
		if (alarms == null) {
			alarms = new ArrayList<Alarm>();
			tripIdToAlarm.put(alarm.getStationToStation(), alarms);
		}
		alarms.add(alarm);
	}

	private String getKey() {
		return "lastStatuses" + from.getId();
	}

	private void loadSchedule() {
		if (loadScheduleFuture != null) {
			loadScheduleFuture.cancel(true);
		}
		Runnable load = new Runnable() {
			@Override
			public void run() {
				List<Alarm> alarms = Alarms.getAlarms(getActivity());
				tripIdToAlarm = new HashMap<StationToStation, List<Alarm>>();
				for (Alarm a : alarms) {
					addAlarm(a);
				}
				Calendar date = Calendar.getInstance();
				date.setTime(day);
				final Calendar tomorrow = Calendar.getInstance();
				tomorrow.setTime(day);
				tomorrow.add(Calendar.DAY_OF_YEAR, 1);
				try {
					schedule = ScheduleDao.get().getSchedule(from.getId(),
							to.getId(), day, day);
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
							// k.add(stationToStation);
							if (!isToday) {
								if (!stationToStation.departTime.before(limit)
										&& !stationToStation.departTime
												.after(tomorrow)) {
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

				Iterator<Map.Entry<StationToStation, List<Alarm>>> ak = tripIdToAlarm
						.entrySet().iterator();
				while (ak.hasNext()) {
					Map.Entry<StationToStation, List<Alarm>> entry = ak.next();
					Activity activity = getActivity();
					if (activity != null) {
						Iterator<Alarm> aks = entry.getValue().iterator();
						while (aks.hasNext()) {
							Alarm alarm = aks.next();
							if (alarm.getTime().before(Calendar.getInstance())) {
								activity.startService(Alarms.newDismissIntent(
										activity, alarm));
								aks.remove();
							}
							if (entry.getValue().isEmpty()) {
								ak.remove();
							}
						}

					}
				}
				updateSchedulePeriodically();
			}

		};
		loadScheduleFuture = ThreadHelper.getScheduler().submit(load);
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		poller = new DeparturePoller();
		alarmManger = (AlarmManager) getActivity().getSystemService(
				Context.ALARM_SERVICE);
		notifs = (NotificationManager) getActivity().getSystemService(
				Context.NOTIFICATION_SERVICE);
		manager = (ConnectivityManager) getActivity().getSystemService(
				Context.CONNECTIVITY_SERVICE);
		Bundle b = getArguments();
		from = (Station) b.getSerializable("from");
		to = (Station) b.getSerializable("to");
		day = (Date) b.getSerializable("date");
		adapter = new BaseExpandableListAdapter() {

			int TYPE_CONTROLS = 0;

			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return 1;
			}

			@Override
			public int getChildType(int groupPosition, int childPosition) {
				return childPosition;
			}

			@Override
			public int getChildTypeCount() {
				return 1;
			}

			@Override
			public View getChildView(final int groupPosition,
					int childPosition, boolean isLastChild, View convertView,
					ViewGroup parent) {
				if (childPosition == TYPE_CONTROLS) {
					ScheduleControlsView v = new ScheduleControlsView(
							parent.getContext());
					final StationToStation sts = getItem(groupPosition);

					v.setListener(new ScheduleControlListener() {

						@Override
						public void onFavorite() {
							// TODO Auto-generated method stub

						};

						@Override
						public void onPin() {

						}

						@Override
						public void onTimer() {
							FragmentAlarmPicker fdp = FragmentAlarmPicker
									.newInstance(sts);
							fdp.setOnTimerPicked(new OnTimerPicked() {

								@Override
								public void onTimer(final Type type,
										final Calendar cal,
										final StationToStation stationToStation) {
									ThreadHelper.getScheduler().submit(
											new Runnable() {
												@Override
												public void run() {
													Intent i = AlarmActivity
															.from(getActivity(),
																	stationToStation,
																	cal, type);
													Alarm alarm = (Alarm) i
															.getSerializableExtra("alarm");
													addAlarm(alarm);
													Alarms.startAlarm(
															getActivity(),
															alarm);
													handler.post(new Runnable() {
														public void run() {
															adapter.notifyDataSetChanged();
														};
													});
												}
											});

								}
							});
							fdp.show(getFragmentManager(), "alarmPicker");
							// FragmentTransaction ft =
							// getActivity().getSupportFragmentManager().beginTransaction();
							// ft.replace(R.id.fragment_alarm_picker, fdp);
							// ft.commit();
						}

						public void onTimerCancel(Alarm alarm) {
							List<Alarm> alarms = tripIdToAlarm.get(alarm
									.getStationToStation());
							if (alarms != null) {
								alarms.remove(alarm);
							}
							getActivity().startService(
									Alarms.newDismissIntent(getActivity(),
											alarm));
							adapter.notifyDataSetChanged();
						}

						@Override
						public void onTrips(Schedule schedule,
								StationToStation stationToStation) {
							controlListener.onTrips(schedule, stationToStation);
						}

					});
					v.setData(tripIdToAlarm.get(sts), schedule, sts);
					return v;
				}
				return null;
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

			// @Override
			// public long getItemId(int position) {
			// // TODO Auto-generated method stub
			// return 0;
			// }

			// @Override
			public int getCount() {
				if (o == null) {
					return 0;
				}
				return o.size();
			}

			@Override
			public StationToStation getGroup(int groupPosition) {
				return getItem(groupPosition);
			}

			@Override
			public int getGroupCount() {
				return getCount();
			}

			@Override
			public long getGroupId(int groupPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				return getView(groupPosition, convertView, parent);
			}

			// @Override
			public StationToStation getItem(int position) {
				return o.get(position);
			}

			// @Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ScheduleView view = (ScheduleView) convertView;
				if (view == null) {
					view = new ScheduleView(parent.getContext());
				}
				StationToStation sts = getItem(position);
				view.setData(sts, from, to);
				view.setAlarm(tripIdToAlarm.get(sts));
				view.setStatus(tripIdToTrainStatus.get(sts.blockId));
				return view;
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
		};
		list.setAdapter(adapter);
		activityCreated = true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (!isVisible() || !isAdded()) {
			return;
		}
		menu.clear();
		inflater.inflate(R.menu.menu_schedule_day, menu);
	};

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
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(populateAdpter);
		if (loadScheduleFuture != null) {
			loadScheduleFuture.cancel(true);
		}
		if (poll != null) {
			poll.cancel(true);
		}
		if (updateScheduleFuture != null) {
			updateScheduleFuture.cancel(true);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_go_to_next_train) {
			moveToNextTrain();
		}
		if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
			return true;
		}
		if (item.getItemId() == R.id.menu_departurevision) {
			onDepartureVision.onDepartureVision(from);
		}
		if (item.getItemId() == R.id.menu_reverse) {
			onGetSchedule.onGetSchedule(to, from);
		}
		return super.onOptionsItemSelected(item);
	}

	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(connectionReceiver);
		if (poll != null) {
			poll.cancel(true);
		}
		if (updateScheduleFuture != null) {
			updateScheduleFuture.cancel(true);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (DateUtils.isToday(day.getTime())) {
			menu.removeItem(R.id.menu_go_to_today);
		} else {
			menu.removeItem(R.id.menu_go_to_next_train);
			menu.removeItem(R.id.menu_departurevision);
		}
	}

	Future<?> updateScheduleFuture;

	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		getActivity().registerReceiver(
				connectionReceiver,
				new IntentFilter(
						android.net.ConnectivityManager.CONNECTIVITY_ACTION));
		if (!DateUtils.isToday(day.getTime())) {
			return;
		} else {
			updateSchedulePeriodically();
		}
		NetworkInfo i = manager.getActiveNetworkInfo();
		if (i != null && i.isConnected() && canLoad) {
			poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r, 100,
					10000, TimeUnit.MILLISECONDS);
		}
	}

	private void updateSchedulePeriodically() {
		if (updateScheduleFuture != null) {
			updateScheduleFuture.cancel(true);
		}
		if (o == null) {
			return;
		}
		Calendar now = Calendar.getInstance();
		Calendar later = Calendar.getInstance();
		later.add(Calendar.MINUTE, 1);
		later.set(Calendar.SECOND, 0);
		later.set(Calendar.MILLISECOND, 0);
		Log.d(FragmentDaySchedule.class.getSimpleName(),
				"updateScheduleFuture delay : "
						+ (later.getTimeInMillis() - now.getTimeInMillis()));
		updateScheduleFuture = ThreadHelper.getScheduler().scheduleAtFixedRate(
				updateEverySixtySeconds,
				later.getTimeInMillis() - now.getTimeInMillis(), 60000,
				TimeUnit.MILLISECONDS);
	}

	public void setOnDateChange(OnDateChange onDateChange) {
		this.onDateChange = onDateChange;
	}

	@Override
	public void setPrimaryItem() {
		if (activityCreated && o == null) {
			loadSchedule();
		}
	}

	@Override
	public void setSecondary() {
		if (loadScheduleFuture != null) {
			loadScheduleFuture.cancel(true);
		}
		if (poll != null) {
			poll.cancel(true);
		}
		if (updateScheduleFuture != null) {
			updateScheduleFuture.cancel(true);
		}
	}

	ScheduleControlListener controlListener;

	public void setScheduleControlListener(
			ScheduleControlListener controlListener) {
		this.controlListener = controlListener;

	}

}
