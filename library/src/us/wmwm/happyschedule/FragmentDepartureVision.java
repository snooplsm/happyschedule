package us.wmwm.happyschedule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

public class FragmentDepartureVision extends Fragment {

	StickyListHeadersListView list;

	Future<?> poll;

	DeparturePoller poller;

	DepartureVisionAdapter adapter;

	Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_departurevision,
				container, false);
		list = (StickyListHeadersListView) root.findViewById(R.id.list);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		poller = new DeparturePoller();
		list.setAdapter(adapter = new DepartureVisionAdapter());
		loadColors();
		super.onActivityCreated(savedInstanceState);
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

		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					final List<TrainStatus> s = poller.getTrainStatuses("TR");
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
		poll = ThreadHelper.getScheduler().scheduleAtFixedRate(r, 100, 10000,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (poll != null) {
			poll.cancel(true);
		}
	}

}
