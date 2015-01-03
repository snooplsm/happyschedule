package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.adapter.TripAdapter;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TripInfo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class FragmentTrip extends HappyFragment implements OnMapReadyCallback {

	private String[] tripIds;

	private Station depart;
	private Station arrive;

	private Schedule schedule;

	private long start;

	private ListView list;

	TripAdapter adapter;
	
	Handler handler = new Handler();

    GoogleMap map;
	
	Runnable update = new Runnable() {
		public void run() {
			adapter.notifyDataSetChanged();
            updateMap();
			handler.postDelayed(update, 1000);
		};
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);
		Bundle b = getArguments();
		depart = (Station) b.getSerializable("depart");
		arrive = (Station) b.getSerializable("arrive");
		String id = b.getString("tripId");
		// loadSchedule();
		// getSupportActionBar().setTitle("Trip #" + id);
		// getSupportActionBar().setSubtitle(
		// schedule.stopIdToName.get(schedule.departId) + " to "
		// + schedule.stopIdToName.get(schedule.arriveId));

		StationInterval interval = null;

		Stack<StationInterval> intervals = new Stack<StationInterval>();
		List<TripInfo.Stop> stops = new ArrayList<TripInfo.Stop>();
		Map<String, String> k = new HashMap<String, String>();
		if (schedule != null) {
			interval = schedule.getStationIntervalForTripId(id);
			intervals.push(interval);
			TripInfo last;
			StationInterval lastInterval = null;
			
			k.put("from_id", depart.getId());
			k.put("to_id", arrive.getId());
			k.put("from_name", depart.getName());
			k.put("to_name", arrive.getName());
			k.put("trip", id);
			while (!intervals.isEmpty()) {
				interval = intervals.pop();
				if (interval.tripId != null) {
					TripInfo tripInfo = ScheduleDao.get()
							.getStationTimesForTripId(interval.tripId,
									interval.departSequence,
									interval.arriveSequence);
					last = tripInfo;
					stops.addAll(tripInfo.stops);
					lastInterval = interval;
				}
				if (interval.hasNext()) {
					intervals.push(interval.next());
				} else {
				}
				last = ScheduleDao.get().getStationTimesForTripId(
						lastInterval.tripId, interval.arriveSequence - 1,
						Integer.MAX_VALUE);

				start = b.getLong("start", 0);
				Calendar startCal = Calendar.getInstance();
				startCal.setTimeInMillis(start);
				adapter = new TripAdapter(getActivity(), stops, schedule,
						startCal);
				list.setAdapter(adapter);
			}
		} else {
			TripInfo tripInfo = ScheduleDao.get()
					.getStationTimesForTripId(id,
							0,
							Integer.MAX_VALUE);
			stops.addAll(tripInfo.stops);
			Calendar startCal = Calendar.getInstance();
			startCal.setTimeInMillis(start);
			adapter = new TripAdapter(getActivity(), stops, schedule,
					startCal);
			list.setAdapter(adapter);
		}

		FlurryAgent.logEvent("ViewTrip", k);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_trips, container, false);
		list = (ListView) view.findViewById(R.id.list);
		return view;
	}

	public static FragmentTrip newInstance(Station depart, Station arrive,
			StationToStation sts, Schedule schedule) {
		Bundle b = new Bundle();
		b.putSerializable("depart", depart);
		b.putSerializable("arrive", arrive);
		b.putLong("start", sts.getDepartTime().getTimeInMillis());
		b.putString("tripId", sts.tripId);
		FragmentTrip t = new FragmentTrip();
		t.setArguments(b);
		t.schedule = schedule;
		return t;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		handler.post(update);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(update);
	}

	public static FragmentTrip newInstance(Station depart, Station arrive,
			String tripId) {
		Bundle b = new Bundle();
		b.putSerializable("depart", depart);
		b.putSerializable("arrive", arrive);
		b.putString("tripId", tripId);
		FragmentTrip t = new FragmentTrip();
		t.setArguments(b);
		return t;
	}

    private static final String TAG = FragmentTrip.class.getSimpleName();

    Map<TripInfo.Stop,Marker> markers = new HashMap<TripInfo.Stop,Marker>();
    Polyline polyline;

    private void updateMap() {
        if(map==null) {
            return;
        }
        Station station = Db.get().getStop(adapter.getStops().get(0).id);
        LatLng ll = new LatLng(Double.parseDouble(station.getLat()),Double.parseDouble(station.getLng()));
        PolylineOptions poly = new PolylineOptions();
        LatLngBounds llb = new LatLngBounds(ll,ll);
        //map.setMapType(GoogleMap.MAP_TYPE_NONE);
        int dimen = (int) getResources().getDimension(R.dimen.fab_size_mini)/3;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int color = Color.RED;
        Bitmap bitmap = Bitmap.createBitmap(dimen,dimen, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (TripInfo.Stop stop : adapter.getStops()) {
            canvas.clipRect(0,0,canvas.getWidth(),canvas.getHeight());
            if(stop.arrive.before(Calendar.getInstance())) {
                Log.d(TAG,"arrive before");
                paint.setColor(color);
            } else {
                paint.setColor(Color.argb((int)(255*.25f),Color.red(color),Color.green(color),Color.blue(color)));
            }
            canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,bitmap.getWidth()/2,paint);
            station = Db.get().getStop(stop.id);
            LatLng latLng = new LatLng(Double.parseDouble(station.getLat()),Double.parseDouble(station.getLng()));
            llb = llb.including(latLng);

            if(markers.get(stop)!=null) {
                markers.get(stop).remove();
            }
            Marker hamburg = map.addMarker(new MarkerOptions().position(latLng).anchor(0.5f,0.5f).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            markers.put(stop,hamburg);
            poly.add(latLng);
        }

        if(polyline!=null) {
            polyline.remove();
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(llb,dimen));
        }
        //poly.
        polyline = map.addPolyline(poly);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }
}
