package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
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
	private View close,mapContainer;

	private Schedule schedule;

	private long start;

	private ListView list;

	TripAdapter adapter;
	
	Handler handler = new Handler();

    GoogleMap map;

    private View holder;
	
	Runnable update = new Runnable() {
		public void run() {
			adapter.notifyDataSetChanged();
            updateMap();
			handler.postDelayed(update, 10000);
		};
	};

    Date date;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);
		Bundle b = getArguments();
		depart = (Station) b.getSerializable("depart");
		arrive = (Station) b.getSerializable("arrive");
        date = (Date) b.getSerializable("date");
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
							.getStationTimesForTripId(date, interval.tripId,
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
				last = ScheduleDao.get().getStationTimesForTripId(date,
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
					.getStationTimesForTripId(date, id,
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
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslateAnimation a = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF,0,TranslateAnimation.RELATIVE_TO_SELF,0,TranslateAnimation.RELATIVE_TO_PARENT,0,TranslateAnimation.RELATIVE_TO_PARENT,-1);
                a.setDuration(2500);

                mapContainer.setVisibility(View.GONE);
                holder.setVisibility(View.GONE);

            }
        });
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_trips, container, false);
		list = (ListView) view.findViewById(R.id.list);
        close = view.findViewById(R.id.close);
        holder = view.findViewById(R.id.holder);
        mapContainer = view.findViewById(R.id.map_container);
		return view;
	}

	public static FragmentTrip newInstance(Date date, Station depart, Station arrive,
			StationToStation sts, Schedule schedule) {
		Bundle b = new Bundle();
        b.putSerializable("date",date);
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

	public static FragmentTrip newInstance(Date date, Station depart, Station arrive,
			String tripId) {
		Bundle b = new Bundle();
        b.putSerializable("date",date);
		b.putSerializable("depart", depart);
		b.putSerializable("arrive", arrive);
		b.putString("tripId", tripId);
		FragmentTrip t = new FragmentTrip();
		t.setArguments(b);
		return t;
	}

    private static final String TAG = FragmentTrip.class.getSimpleName();

    Map<TripInfo.Stop,Marker> markers = new HashMap<TripInfo.Stop,Marker>();
    Marker me;
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
        Station stationA = null,stationB = null;
        Calendar now = Calendar.getInstance();

        TripInfo.Stop previous = null;
        float percent = 0;
        for (TripInfo.Stop stop : adapter.getStops()) {

            canvas.clipRect(0,0,canvas.getWidth(),canvas.getHeight());
            station = Db.get().getStop(stop.id);
            Log.d(TAG,station.getName()+ " " + station.getLat()+","+station.getLng() + " " + stop.depart.getTime());
            paint.setColor(color);

            if(now.after(stop.depart)) {
                stationA = station;
                previous = stop;
            } else {
                if(stationB==null && stationA!=station) {
                    stationB = station;
                    if(previous==null) {
                        percent = 0;
                    } else
                    if(previous.depart==null || stop.arrive==null) {

                    } else {
                        long max = stop.arrive.getTimeInMillis() - previous.depart.getTimeInMillis();
                        long curr = stop.arrive.getTimeInMillis() - System.currentTimeMillis();

                        if (curr <= 0) {
                            percent = 0;
                        } else {
                            percent = 1 - (curr / (float) max);
                        }
                    }
                }
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

        if(stationA==null) {
            return;
        }
        if(stationB==null) {
            stationB = stationA;
        }

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(Double.parseDouble(stationA.getLat()));
        location.setLongitude(Double.parseDouble(stationA.getLng()));

        Location location2 = new Location(LocationManager.GPS_PROVIDER);
        location2.setLatitude(Double.parseDouble(stationB.getLat()));
        location2.setLongitude(Double.parseDouble(stationB.getLng()));
//        var Î¸ = Number(brng).toRadians();
//        var Î´ = Number(dist) / this.radius; // angular distance in radians
//
//        var Ï†1 = this.lat.toRadians();
//        var Î»1 = this.lon.toRadians();
//
//        var Ï†2 = Math.asin( Math.sin(Ï†1)*Math.cos(Î´) +
//                Math.cos(Ï†1)*Math.sin(Î´)*Math.cos(Î¸) );
//        var Î»2 = Î»1 + Math.atan2(Math.sin(Î¸)*Math.sin(Î´)*Math.cos(Ï†1),
//                Math.cos(Î´)-Math.sin(Ï†1)*Math.sin(Ï†2));
//        Î»2 = (Î»2+3*Math.PI) % (2*Math.PI) - Math.PI; // normalise to -180..+180Â°

        double totalDist = location.distanceTo(location2);

        Log.d(TAG,"distance is " + totalDist + " percentage is " + percent);

        double dist = (percent * totalDist) / (6371.0*1000.0);


        double brng = Math.toRadians(location.bearingTo(location2));
        double lat1 = Math.toRadians(location.getLatitude());
        double lon1 = Math.toRadians(location.getLongitude());

        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
        double a = Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
        System.out.println("a = " + a);
        double lon2 = lon1 + a;

        lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;

        System.out.println("Latitude = "+Math.toDegrees(lat2)+"\nLongitude = "+Math.toDegrees(lon2));
        MarkerOptions o = new MarkerOptions().anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.new_blue_dot)).position(new LatLng(Math.toDegrees(lat2),Math.toDegrees(lon2)));
        if(me!=null) {
            me.remove();
        }
        if(stationA!=stationB) {
            me = map.addMarker(o);
        }
        Log.d(TAG,"new coordinates are " + lat2+","+lon2);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }
}
