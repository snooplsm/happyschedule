package us.wmwm.happyschedule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

public class FragmentDepartureVision extends Fragment {

	StickyListHeadersListView list;

	Future<?> poll;

	DeparturePoller poller;

	DepartureVisionAdapter adapter;

	Handler handler = new Handler();

	ConnectivityManager manager;
	
	List<TrainStatus> lastStatuses;
	
	String station = "NY";
	
	long lastStatusesReceived;

	BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
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

	private String getKey() {
		return "lastStatuses" + station;
	}
	
	Runnable r = new Runnable() {
		@Override
		public void run() {
			try {
				final List<TrainStatus> s = poller.getTrainStatuses(station);
				String key = getKey();				
				if(s!=null && !s.isEmpty()) {
					JSONArray a = new JSONArray();
					if(lastStatuses!=null) {
						for(int i = 0; i < lastStatuses.size(); i++) {
							a.put(lastStatuses.get(i).toJSON());
						}
						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("lastStation",station).putString(key , a.toString()).putLong(key+"Time", System.currentTimeMillis()).commit();
					}
				}
				handler.post(new Runnable() {
					public void run() {
						adapter.setData(s);
					}
				});
				System.out.println(s.size());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_departurevision,
				container, false);
		list = (StickyListHeadersListView) root.findViewById(R.id.list);
		return root;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		JSONArray a = new JSONArray();
		if(lastStatuses!=null) {
			for(int i = 0; i < lastStatuses.size(); i++) {
				a.put(lastStatuses.get(i).toJSON());
			}
			String key = "lastStatuses" + station;
			PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("lastStation",station).putString(key , a.toString()).putLong(key, lastStatusesReceived).commit();
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.departurevision, menu);
		super.onCreateOptionsMenu(menu, inflater);		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		poller = new DeparturePoller();
		list.setAdapter(adapter = new DepartureVisionAdapter());
		manager = (ConnectivityManager) getActivity().getSystemService(
				Context.CONNECTIVITY_SERVICE);
		loadInitial();
		loadColors();
		super.onActivityCreated(savedInstanceState);
	}

	private void loadInitial() {
		Long time = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(getKey()+"Time", 0);
		if(System.currentTimeMillis() - time > 40000) {
			return;
		}
		String data = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getKey(), null);
		if(data==null) {
			return;
		}
		try {
			JSONArray a = new JSONArray(data);
			List<TrainStatus> statuses = new ArrayList<TrainStatus>(a.length());
			for(int i = 0; i < a.length(); i++) {
				statuses.add(new TrainStatus(a.optJSONObject(i)));
			}
			adapter.setData(statuses);
			
		} catch (Exception e) {
			
		}
		
	}

	private void loadColors() {
		ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				Activity a = getActivity();
				if (a == null) {
					return;
				}
				InputStream is = a.getResources().openRawResource(R.raw.lines);
				BufferedReader r = new BufferedReader(new InputStreamReader(is));
				String line = null;
				StringBuilder b = new StringBuilder();
				try {
					while ((line = r.readLine()) != null) {
						b.append(line);
					}
					JSONObject o = new JSONObject(b.toString());
					JSONArray k = o.optJSONArray("lines");
					final Map<String, LineStyle> keyToColor = new HashMap<String, LineStyle>();
					for (int i = 0; i < k.length(); i++) {
						JSONObject li = k.optJSONObject(i);
						LineStyle l = new LineStyle(li);
						keyToColor.put(l.key, l);
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

		NetworkInfo i = manager.getActiveNetworkInfo();
		if (i != null && i.isConnected()) {
			poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r, 100,
					10000, TimeUnit.MILLISECONDS);
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

}
