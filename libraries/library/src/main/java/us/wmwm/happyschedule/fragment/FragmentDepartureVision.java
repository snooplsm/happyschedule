package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import us.wmwm.happyschedule.BuildConfig;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.ActivityPickStation;
import us.wmwm.happyschedule.adapter.DepartureVisionAdapter;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.LineStyle;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TrainStatus;
import us.wmwm.happyschedule.service.Poller;
import us.wmwm.happyschedule.util.Streams;
import us.wmwm.happyschedule.views.DepartureVisionView;
import us.wmwm.happyschedule.views.OnStationSelectedListener;
import us.wmwm.happyschedule.views.OnStationSelectedListener.State;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.FloatingActionLayout;

public class FragmentDepartureVision extends HappyFragment implements IPrimary,
		ISecondary, IScrollingFragment {

	private static final String TAG = FragmentDepartureVision.class
			.getSimpleName();
	StickyListHeadersListView list;
	TextView stationSelect;
	TextView stationArriveSelect;
    TextView stationName;
	SwipeRefreshLayout pullToRefresh;
	Future<?> poll;
	Future<?> refresh;
	DepartureVisionAdapter adapter;
	Handler handler = new Handler();
	ConnectivityManager manager;
	List<TrainStatus> lastStatuses;
	Station station;
	DepartureVision departureVision;
	int position;
	Station stationArrive;
	View empty;
	StationToStation stationToStation;

	View erroText;

	long lastStatusesReceived;

	AppConfig appConfig;

	OnStationSelectedListener onStationSelected;

	boolean canLoad;

	boolean activityCreated;
	DepartureVisionListener departureVisionListener;
    FloatingActionLayout fal;
    FloatingActionButton change;
	boolean logged = false;
	int count;

    AbsListView.OnScrollListener onScroll;

    @Override
    public void setOnScrollingListener(AbsListView.OnScrollListener onScroll) {
        this.onScroll = onScroll;
    }

    Runnable r = new Runnable() {
		@Override
		public void run() {
			try {
				final List<TrainStatus> s = BuildConfig.POLLER.getTrainStatuses(
						appConfig,
						station.getDepartureVision(),
						stationArrive != null ? stationArrive
								.getDepartureVision() : null);
				String key = getKey();
				if (s != null && !s.isEmpty()) {
					count++;
					Map<String, String> k = new HashMap<String, String>();
					k.put("station_id", station.getId());
					k.put("station_name", station.getName());
					FlurryAgent.logEvent("DepartureVision", k);
					JSONArray a = new JSONArray();
					if (lastStatuses != null) {
						for (int i = 0; i < lastStatuses.size(); i++) {
							a.put(lastStatuses.get(i).toJSON());
						}
						PreferenceManager
								.getDefaultSharedPreferences(
										HappyApplication.get())
								.edit()
								.putString("lastStation", station.getId())
								.putString(key, a.toString())
								.putString("lastStatuses",
										lastStatuses.toString())
								.putLong(key + "Time",
										System.currentTimeMillis()).commit();
					}

					lastStatuses = s;
				}
				handler.post(new Runnable() {
					public void run() {
						pullToRefresh.setRefreshing(false);
						adapter.setData(s);
						if (adapter.getCount() == 0) {
							empty.setVisibility(View.VISIBLE);
						} else {
							empty.setVisibility(View.GONE);
						}
					}
				});

			} catch (Exception e) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							pullToRefresh.setRefreshing(false);
						} catch (Exception e) {
							//this component is pretty buggy.
						}
					}
				});
				Log.e(TAG, "error with polling", e);
			}
		}
	};
	BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info == null || !info.isConnected()) {
				erroText.setVisibility(View.VISIBLE);
			}
			if (!canLoad) {
				return;
			}
			if (info != null && info.isConnected()) {
				erroText.setVisibility(View.GONE);
				if (poll == null || poll.isCancelled()) {
					poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r,
							100, SettingsFragment.getPollMilliseconds(),
							TimeUnit.MILLISECONDS);
				}
			} else {
				if (poll != null) {
					poll.cancel(true);
				}
			}

		}
	};
	SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {


        @Override
        public void onRefresh() {
            if (refresh != null) {
                refresh.cancel(true);
            }

            refresh = ThreadHelper.getScheduler().submit(r);
        }
	};
	Runnable delayed = new Runnable() {
		@Override
		public void run() {
			setPrimaryItem();
		}
	};

	public static FragmentDepartureVision newInstance(DepartureVision dvm,
			int position, Station arrival, StationToStation sts,
			boolean isOverlay) {
		FragmentDepartureVision dv = new FragmentDepartureVision();
		Bundle b = new Bundle();
		b.putSerializable("departureVision", dvm);
		b.putSerializable("stationArrival", arrival);
		if (sts != null) {
			b.putSerializable("stationToStation", sts);
		}
		b.putInt("position", position);
		b.putBoolean("isOverlay", isOverlay);
		dv.setArguments(b);
		return dv;
	}

	public FragmentDepartureVision setDepartureVisionListener(
			DepartureVisionListener departureVisionListener) {
		this.departureVisionListener = departureVisionListener;
		return this;
	}

	public FragmentDepartureVision setOnStationSelected(
			OnStationSelectedListener onStationSelected) {
		this.onStationSelected = onStationSelected;
		return this;
	}

	private String getKey() {
		return "lastStatuses" + station.getId();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_departurevision,
				container, false);
		list = (StickyListHeadersListView) root.findViewById(R.id.list);
        stationName = new TextView(getActivity());
        int padding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
        stationName.setPadding(padding,padding,padding,padding);
        stationName.setGravity(Gravity.CENTER_HORIZONTAL);
		stationSelect = (TextView) root.findViewById(R.id.departure);
        list.addHeaderView(stationName);
		stationArriveSelect = (TextView) root.findViewById(R.id.arrival);
		erroText = root.findViewById(R.id.no_internet);
		empty = root.findViewById(R.id.empty);
		pullToRefresh = (SwipeRefreshLayout)root.findViewById(R.id.pull_to_refresh);
        fal = (FloatingActionLayout) root.findViewById(R.id.fal);
        change = (FloatingActionButton) root.findViewById(R.id.button_floating_action_change);
		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		JSONArray a = new JSONArray();
		if (lastStatuses != null) {
			for (int i = 0; i < lastStatuses.size(); i++) {
				a.put(lastStatuses.get(i).toJSON());
			}
			String key = "lastStatuses" + station;
			PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
					.putString("lastStation", station.getId())
					.putString(key, a.toString())
					.putLong(key, lastStatusesReceived).commit();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		boolean showControls = true;
		if (args != null) {
			showControls = !args.containsKey("stationToStation");
		}
		setHasOptionsMenu(showControls);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FlurryAgent.logEvent(item.getTitle() + "MenuItemSelected");
		if (item.getItemId() == R.id.menu_change_station) {
			startActivityForResult(ActivityPickStation.from(getActivity(), true),
					100);
		}
		if (item.getItemId() == R.id.menu_change_arrival_station) {
			startActivityForResult(ActivityPickStation.from(getActivity(), true),
					200);
		}
		if (item.getItemId() == R.id.menu_add_station) {
			startActivityForResult(ActivityPickStation.from(getActivity(), true), 300);
		}
		if (item.getItemId() == R.id.menu_remove_station) {
			deleteCurrentStation();
		}
		if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
		}
//		if (item.getItemId() == R.id.menu_refresh) {
//			// canLoad = true;
//			if (refresh != null) {
//				refresh.cancel(true);
//			}
//
//			refresh = ThreadHelper.getScheduler().submit(departureVisionRunnable);
//		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (station == null) {
			menu.removeItem(R.id.menu_remove_station);
		} else {
		}
	}
	
	private void deleteCurrentStation() {
		if(departureVision!=null) {
			DepartureVisionHelper.remove(departureVision);
			if (onStationSelected != null) {
				onStationSelected.onStation(station, State.REMOVED);
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.departurevision, menu);
		Bundle args = getArguments();
		if (!args.getBoolean("isOverlay")) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle("w/ DepartureVision");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		manager = (ConnectivityManager) getActivity().getSystemService(
				Context.CONNECTIVITY_SERVICE);
		Bundle arguments = getArguments();
        list.setOnScrollListener(onScroll);
		if (arguments != null) {
			departureVision = (DepartureVision) arguments
					.getSerializable("departureVision");
			if (departureVision == null) {
				departureVision = new DepartureVision(BuildConfig.DEFAULT_DEPARTURE_VISION_ID,null);
			}
			position = arguments.getInt("position");
			setStation(Db.get().getStop(departureVision.getFrom()));
			stationToStation = (StationToStation) arguments
					.getSerializable("stationToStation");
			stationArrive = Db.get().getStop(departureVision.getTo());
			canLoad = arguments.getBoolean("isOverlay");
		} else {
            departureVision = new DepartureVision(BuildConfig.DEFAULT_DEPARTURE_VISION_ID,null);
            setStation(Db.get().getStop(departureVision.getFrom()));
            canLoad = true;
		}

		if (stationToStation == null) {
			list.setAdapter(adapter = new DepartureVisionAdapter());
		} else {
			list.setAdapter(adapter = new DepartureVisionAdapter(station,
					stationToStation));
		}

		loadColors();
		if (!BuildConfig.POLLER.isArrivalStationRequired()) {
			stationArriveSelect.setVisibility(View.GONE);
		}
		if (station == null) {
			stationSelect.setVisibility(View.VISIBLE);
			stationSelect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivityForResult(
							ActivityPickStation.from(getActivity(), true), 100);
				}
			});
			return;
		} else {
			stationSelect.setVisibility(View.GONE);
		}
		if (BuildConfig.POLLER.isArrivalStationRequired() && departureVision.getTo()==null) {
			stationArriveSelect.setVisibility(View.VISIBLE);
			if (station != null) {
				stationSelect.setText(station.getName());
			}
			if (stationArrive != null) {
				stationArriveSelect.setText(stationArrive.getName());
			}
			stationArriveSelect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivityForResult(
							ActivityPickStation.from(getActivity(), true), 200);
				}
			});
			return;
		}
		if (station != null) {
			stationSelect.setVisibility(View.GONE);
		}
		if (stationArrive != null) {
			stationArriveSelect.setVisibility(View.GONE);
		}

		loadInitial();
		// FragmentAmazonAd ad = new FragmentAmazonAd();
		// ad.setHappyAdListener(new HappyAdListener() {
		// @Override
		// public void onAd() {
		// }
		//
		// @Override
		// public void onAdFailed(int count, boolean noFill) {
		// handler.post(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// FragmentGoogleAd gad = new FragmentGoogleAd();
		// getFragmentManager().beginTransaction()
		// .replace(R.id.fragment_ad, gad).commit();
		// } catch (Exception e) {
		//
		// }
		// }
		// });
		// }
		// });
		// getFragmentManager().beginTransaction().replace(R.id.fragment_ad, ad)
		// .commit();
		activityCreated = true;
		if (!canLoad && departureVisionListener != null) {
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					DepartureVisionView v = (DepartureVisionView) arg1;
					TrainStatus status = v.getTrainStatus();
					departureVisionListener.onTrip(status.getTrain());
				}
			});
		}
//		ActionBarPullToRefresh.from(getActivity())
//        // Mark All Children as pullable
//        .allChildrenArePullable()
//        // Set the OnRefreshListener
//        .listener(onRefreshListener)
//        // Finally commit the setup to our PullToRefreshLayout
//        .setup(pullToRefresh);
        pullToRefresh.setOnRefreshListener(onRefreshListener);
        change.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ActivityPickStation.from(getActivity(), true),
                        100);
            }
        });
        //fal.attachToListView(list);
	}

	private void loadInitial() {
		Long time = PreferenceManager
				.getDefaultSharedPreferences(getActivity()).getLong(
						getKey() + "Time", 0);
		if (System.currentTimeMillis() - time > 50000) {
			return;
		}
		String data = PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getString(getKey(), null);
		if (data == null) {
			return;
		}
		try {
			JSONArray a = new JSONArray(data);
			List<TrainStatus> statuses = new ArrayList<TrainStatus>(a.length());
			for (int i = 0; i < a.length(); i++) {
				statuses.add(new TrainStatus(a.optJSONObject(i)));
			}
			adapter.setData(statuses);

		} catch (Exception e) {

		}
		activityCreated = true;
	}

	private void loadColors() {
		ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				Activity a = getActivity();
				if (a == null) {
					return;
				}
				try {
					appConfig = AppConfig.get();
					final Map<String, LineStyle> keyToColor = new HashMap<String, LineStyle>();
					for (LineStyle l : appConfig.getLines()) {
						Iterator<String> keys = l.keys.keySet().iterator();
						while (keys.hasNext()) {
							keyToColor.put(keys.next(), l);
						}
					}
					handler.post(new Runnable() {
						@Override
						public void run() {
							adapter.setKeyToColor(keyToColor);
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(
				connectionReceiver,
				new IntentFilter(
						android.net.ConnectivityManager.CONNECTIVITY_ACTION));
		if (station == null) {
			return;
		}
		NetworkInfo i = manager.getActiveNetworkInfo();
		if (i != null && i.isConnected() && canLoad) {
			poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r, 100,
					SettingsFragment.getPollMilliseconds(),
					TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(connectionReceiver);
		if (poll != null) {
			poll.cancel(true);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		State state = null;
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 100 || requestCode == 300) {
                setStation((Station) data.getSerializableExtra("station"));
			} else {
				stationArrive = (Station) data.getSerializableExtra("station");
			}
			canLoad = true;
			if (requestCode == 100 || requestCode == 200 || requestCode == 300) {
				if (requestCode == 100) {
					departureVision = new DepartureVision(station.getId(),
							stationArrive == null ? null
									: stationArrive.getId());
					DepartureVisionHelper.set(position, departureVision);
				} else if (requestCode == 300) {
					departureVision = new DepartureVision(station.getId(),
							stationArrive == null ? null
									: stationArrive.getId());
					DepartureVisionHelper.add(position,departureVision);
				} else {
					DepartureVisionHelper.set(position, new DepartureVision(station!=null?station.getId():null,stationArrive!=null?stationArrive.getId():null));
				}
				adapter.notifyDataSetInvalidated();
				if (requestCode == 100) {
					this.stationSelect.setVisibility(View.GONE);
				}
				if (requestCode == 200) {
					this.stationArriveSelect.setVisibility(View.GONE);
				}
				if (requestCode == 300) {

				}
				if (poll != null) {
					poll.cancel(true);
				}
				poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r, 100,
						SettingsFragment.getPollMilliseconds(),
						TimeUnit.MILLISECONDS);
				List<TrainStatus> ks = new ArrayList<TrainStatus>();
				adapter.setData(ks);
			}
			if (requestCode == 100 || requestCode == 200) {
				state = State.CHANGED;
			}
			if (requestCode == 300) {
				state = State.ADDED;
			}
			if (onStationSelected != null) {
				onStationSelected.onStation(station, state);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

    private void setStation(Station station) {
        this.station = station;
        if(station!=null) {
            this.stationName.setText(station.getName());
        }
    }

public void onDestroy() {
		super.onDestroy();
		if(handler!=null) {
			handler.removeCallbacks(delayed);
		}
	}
	
		@Override
	public void setPrimaryItem() {
		canLoad = true;
		if(!activityCreated) {
			if(handler!=null) {
				handler.postDelayed(delayed, 1000);
			}
			return;
		}
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			erroText.setVisibility(View.VISIBLE);
		}
		if (info != null && info.isConnected()) {
			erroText.setVisibility(View.GONE);
		} else {

		}
		if (poll != null) {
			poll.cancel(true);
		}
		if ((poll == null || poll.isCancelled()) && station != null) {
			Log.d(TAG, "Polling!");
			poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r, 100,
					SettingsFragment.getPollMilliseconds(),
					TimeUnit.MILLISECONDS);
		} else {
			Log.d(TAG, "Not Polling!");
		}
		if (!logged) {
			Map<String, String> k = new HashMap<String, String>();
			if (station != null) {
				k.put("station_id", station.getId());
				k.put("station_name", station.getName());
			}
			FlurryAgent.logEvent("FragmentDepartureVision", k);
		}
	};
	
	@Override
	public void setSecondary() {
		canLoad = false;
		if (poll != null) {
			poll.cancel(true);
		}
	}

	public interface DepartureVisionListener {
		void onTrip(String tripId);
	}

}
