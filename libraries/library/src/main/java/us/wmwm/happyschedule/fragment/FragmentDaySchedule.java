package us.wmwm.happyschedule.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;
import com.larvalabs.svgandroid.SVGBuilder;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.FloatingActionLayout;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import us.wmwm.happyschedule.Alarms;
import us.wmwm.happyschedule.BuildConfig;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.AlarmActivity;
import us.wmwm.happyschedule.activity.RailLinesActivity;
import us.wmwm.happyschedule.api.Api;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.fragment.FragmentAlarmPicker.OnTimerPicked;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.ScheduleTraverser;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TrainStatus;
import us.wmwm.happyschedule.model.Type;
import us.wmwm.happyschedule.service.FareType;
import us.wmwm.happyschedule.util.ImageUtil;
import us.wmwm.happyschedule.util.Share;
import us.wmwm.happyschedule.views.BackListener;
import us.wmwm.happyschedule.views.DepartureVisionHeader;
import us.wmwm.happyschedule.views.GraphBuilderView;
import us.wmwm.happyschedule.views.ScheduleControlsView.ScheduleControlListener;
import us.wmwm.happyschedule.views.ScheduleLayout;
import us.wmwm.happyschedule.views.ScheduleView;

public class FragmentDaySchedule extends Fragment implements IPrimary,
        ISecondary, BackListener {

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
    ScheduleAdapter adapter;
    AlarmManager alarmManger;
    boolean canLoad = false;
    DepartureVisionHeader header;

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
                    if (SettingsFragment.getUseDepartureVision() && !TextUtils.isEmpty(from.getDepartureVision())
                            && DateUtils.isToday(day.getTime()) ) {
                        poll = ThreadHelper.getScheduler().scheduleAtFixedRate(
                                departureVisionRunnable, 900, SettingsFragment.getPollMilliseconds(), TimeUnit.MILLISECONDS);
                    }
                }
            } else {
                if (poll != null) {
                    poll.cancel(true);
                }
            }

        }
    };

    ScheduleControlListener controlListener;

    ScheduleControlListener innerListener = new ScheduleControlListener() {

        @Override
        public void onDay() {

        }

        @Override
        public void onReverse() {

        }

        @Override
        public void onFavorite() {
            // TODO Auto-generated method stub

        }

        ;

        @Override
        public void onPin(StationToStation sts) {
            ArrayList<String> blocks = new ArrayList<String>();
            if (sts instanceof StationInterval) {
                StationInterval si = (StationInterval) sts;
                while (si.hasNext()) {
                    if (si.blockId != null) {
                        blocks.add(si.blockId);
                    }
                }
            } else {
                blocks.add(sts.blockId);
            }
            WDb.get().addOrDeleteNotification(true, blocks);
        }

        @Override
        public void onTimer(StationToStation sts) {
            FragmentAlarmPicker fdp = FragmentAlarmPicker
                    .newInstance(sts);
            fdp.setOnTimerPicked(onTimerPicked);
            fdp.show(getFragmentManager(), "alarmPicker");
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

        @Override
        public void onShare(Schedule schedule,
                            StationToStation stationToStation) {
            startActivity(Intent.createChooser(Share.intent(
                            getActivity(),
                            (StationInterval) stationToStation),
                    "Share"));
        }
    };

    OnTimerPicked onTimerPicked = new OnTimerPicked() {

        @Override
        public void onTimer(final Type type,
                            final Calendar cal,
                            final StationToStation stationToStation) {
            ThreadHelper.getScheduler().submit(new TimerRunnable(type,cal,stationToStation));
        }
    };

    public class TimerRunnable implements Runnable {
        StationToStation stationToStation;
        Calendar cal;
        Type type;

        public TimerRunnable(Type type, Calendar cal, StationToStation stationToStation) {
            this.type = type;
            this.cal = cal;
            this.stationToStation = stationToStation;
        }

        @Override
        public void run() {
            Intent i = AlarmActivity
                    .from(getActivity(),
                            stationToStation,
                            cal,
                            type,
                            UUID.randomUUID()
                                    .toString());
            Alarm alarm = (Alarm) i
                    .getSerializableExtra("alarm");
            addAlarm(alarm);
            try {
                Alarms.startAlarm(
                        getActivity(),
                        alarm);
            } catch (Exception e) {
                Log.e(TAG,"Error with alarm " + alarm,e);
            }
            handler.post(new Runnable() {
                public void run() {
                    adapter.notifyDataSetChanged();
                }

                ;
            });
        }
    }


    Date day;

    Station from;

    Handler handler = new Handler();

    List<StationToStation> k = null;

    static SimpleDateFormat HOURMINUTE = new SimpleDateFormat("h:mm");

    static {
        try {
            HOURMINUTE.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        } catch (Exception e) {
            // not worth it
        }
    }

    List<TrainStatus> lastStatuses;

    ExpandableStickyListHeadersListView list;

    Future<?> loadScheduleFuture;

    ConnectivityManager manager;

    NotificationManager notifs;

    List<StationToStation> o = null;

    Map<String, FareType> fareTypes = Collections.emptyMap();

    OnDateChange onDateChange;

    OnDepartureVision onDepartureVision;

    OnGetSchedule onGetSchedule;

    Future<?> poll;

    Runnable populateAdpter = new Runnable() {
        @Override
        public void run() {
            Activity activity = getActivity();
            if (activity == null) {
                Log.e(TAG, "ACTIVITY IS NULL");
                return;
            }
            adapter.notifyDataSetInvalidated();
            o = k;
            adapter.notifyDataSetChanged();
            if (DateUtils.isToday(day.getTime())) {
                Log.d(TAG, "moveToNextTrain");
                moveToNextTrain(false);
            } else {
                Log.d(TAG, "moveToNextTrainFail");

            }
            if (adapter.getGroupCount() == 0) {
                list.setVisibility(View.GONE);
//				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
//					ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimension(R.dimen.))
//					View v = new View(getActivity());
//					list.addFooterView();
//				} else {
//					
//				}

                View view = LayoutInflater.from(activity).inflate(
                        R.layout.view_help, null);
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, "feedback@wmwm.us");
                        String appName = getString(R.string.app_name);
                        intent.putExtra(Intent.EXTRA_SUBJECT, appName
                                + " Feedback");
                        StringBuilder b = new StringBuilder();
                        PackageInfo pinfo = null;
                        try {
                            pinfo = getActivity().getPackageManager()
                                    .getPackageInfo(
                                            getActivity().getPackageName(), 0);
                        } catch (NameNotFoundException e) {
                            throw new RuntimeException("no app");
                        }
                        b.append(appName).append("\n\n");
                        b.append("From: ").append(from.getName() + " (")
                                .append(from.getId()).append(")").append("\n");
                        b.append("To: ").append(to.getName() + " (")
                                .append(to.getId()).append(")").append("\n");
                        b.append("Date: " + day.toString()).append("\n");
                        b.append("Time: " + Calendar.getInstance().getTime())
                                .append("\n");
                        b.append("Version: " + pinfo.versionCode).append(" (")
                                .append(pinfo.versionName).append(")")
                                .append("\n");
                        b.append("OS Version: " + Build.VERSION.SDK_INT)
                                .append("\n");
                        b.append("Device: " + Build.DEVICE).append("\n");
                        b.append("Model: " + Build.MODEL).append("\n");
                        b.append("Manu: " + Build.MANUFACTURER).append("\n\n");
                        intent.putExtra(Intent.EXTRA_TEXT, b.toString());
                        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder
                                .from(getActivity());
                        builder.setType("text/rfc822");
                        builder.addEmailTo("feedback@wmwm.us");
                        builder.setSubject(appName + " Feedback");
                        builder.setChooserTitle("Email");
                        builder.setText(b.toString());
                        builder.startChooser();
                        // startActivity(Intent.createChooser(intent,
                        // "Send Email"));
                    }
                });
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                root.addView(view, lp);
            }
        }
    };

    Runnable hideProgress = new Runnable() {
        public void run() {
            progressBar.setVisibility(View.GONE);
        }

        ;
    };

    View progressBar;

    Runnable departureVisionRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(FragmentDaySchedule.class.getSimpleName(),
                        "getting train statuses");
                List<TrainStatus> statuses = null;
                try {
                    statuses = BuildConfig.POLLER.getTrainStatuses(AppConfig.get(),
                            from.getDepartureVision(), to.getDepartureVision());
                } catch (Exception e) {
                    statuses = Collections.emptyList();
                }
                final List<TrainStatus> s = statuses;
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
                    long time = System.currentTimeMillis();
                    if (BuildConfig.POLLER.isArrivalStationRequired()) {
                        for (TrainStatus status : s) {
                            Log.d(FragmentDaySchedule.class.getSimpleName(),
                                    status.toString());
                            for (int i = 0; i < o.size(); i++) {
                                StationToStation sts = o.get(i);

                                if (sts.blockId.endsWith(status.getTrain())) {
                                    if (HOURMINUTE.format(
                                            sts.getDepartTime().getTime())
                                            .equals(status.getDeparts())) {

                                    } else {
                                        Log.d(TAG, "not a match " + sts
                                                + " vs " + status.getDeparts());
                                    }
                                    tripIdToTrainStatus
                                            .put(sts.blockId, status);
                                }
                            }
                        }
                    } else {
                        for (TrainStatus status : s) {
                            Log.d(FragmentDaySchedule.class.getSimpleName(),
                                    status.getTrain() + " : "
                                            + status.getStatus());
                            tripIdToTrainStatus.put(status.getTrain(), status);
                        }
                    }
                    long end = System.currentTimeMillis();
                    Log.d(TAG, "took " + ((end - time) / 1000)
                            + " seconds");
                    lastStatuses = s;
                }
                handler.post(new Runnable() {
                    public void run() {
                        fal.attachToListView(list);
                        adapter.notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "error", e);
            }
        }
    };

    Schedule schedule;

    Station to;

    Map<StationToStation, List<Alarm>> tripIdToAlarm;

    Map<String, TrainStatus> tripIdToTrainStatus = new HashMap<String, TrainStatus>();

    Runnable updateEverySixtySeconds = new Runnable() {
        public void run() {
            Map<String, String> args = new HashMap<String, String>();
            args.put("day", day.toString());
            args.put("from_id", from.getId());
            args.put("from_name", from.getName());
            args.put("to_id", to.getId());
            args.put("to_name", to.getName());
            FlurryAgent.logEvent("UpdateSchedule", args);
            Log.d(FragmentDaySchedule.class.getSimpleName(),
                    "updating schedule view");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }

        ;
    };

    Future<?> updateScheduleFuture;

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

    Future<?> notificationsFuture;

    Runnable loadScheduleRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "IN LOAD");
            Map<String, String> args = new HashMap<String, String>();
            tripIdToAlarm = new HashMap<StationToStation, List<Alarm>>();
            try {
                List<Alarm> alarms = Alarms.getAlarms(getActivity());

                for (Alarm a : alarms) {
                    addAlarm(a);
                }
            } catch (Exception e) {
                Log.e(TAG,"UNABLE TO LOAD ALARMS",e);
            }
            Calendar date = Calendar.getInstance();
            date.setTime(day);
            final Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTime(day);
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            args.put("day", day.toString());
            args.put("from_id", from.getId());
            args.put("from_name", from.getName());
            args.put("to_id", to.getId());
            args.put("to_name", to.getName());
            FlurryAgent.logEvent("LoadSchedule", args, true);
            try {
                schedule = ScheduleDao.get().getSchedule(from.getId(),
                        to.getId(), day, tomorrow.getTime());
                final Calendar limit = Calendar.getInstance();
                limit.setTime(schedule.end);
                Calendar start = Calendar.getInstance();
                start.setTime(schedule.start);
                final boolean isToday = DateUtils.isToday(start
                        .getTimeInMillis());
                if (isToday) {
                    limit.set(Calendar.HOUR_OF_DAY, 5);
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
                Log.d(TAG,"populateItem start: " + day + " to " + tomorrow.getTime());
                schedule.inOrderTraversal(new ScheduleTraverser() {

                    @Override
                    public void populateItem(int index,
                                             StationToStation stationToStation, int total) {

                        // k.add(stationToStation);
                        Log.d(TAG,"populateItem depart: " + stationToStation.departTime.getTime());
                        if (!isToday) {
                            Log.d(TAG,"populateItem not today");
                            if (stationToStation.departTime.getTimeInMillis() <= tomorrow.getTimeInMillis()
                                    && stationToStation.departTime
                                    .getTimeInMillis()>=day.getTime()) {
                                Log.d(TAG,"populateItem not before " + limit.getTime() + " and not after " + tomorrow.getTime());
                                k.add(stationToStation);
                            } else {
                                Log.d(TAG,"populateItem not adding "+stationToStation.departTime.getTime()+ " not between ("+day+","+tomorrow.getTime());
                            }
                        } else {
                            Log.d(TAG,"populateItem today");
                            if (!stationToStation.departTime.before(priorLimit)
                                    && !stationToStation.departTime
                                    .after(limit)) {
                                Log.d(TAG,"populateItem not before " + priorLimit.getTime() + " and not after " + limit.getTime());
                                k.add(stationToStation);
                            }
                            Log.d(TAG,"populateItem not adding");

                        }

                    }
                });
                handler.post(populateAdpter);

                if(SettingsFragment.getUseDepartureVision()) {
                    departureVision = ThreadHelper.getScheduler().submit(departureVisionRunnable);
                }

                ThreadHelper.getScheduler().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
// TripInfo info =
// ScheduleDao.get().getStationTimesForTripId(sts.tripId,0,Integer.MAX_VALUE);
                            final Map<String, FareType> f = BuildConfig.POLLER
                                    .getFareTypes(schedule.getGoodStations());
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    fareTypes = f;
                                    adapter.notifyDataSetChanged();
                                }

                                ;
                            }, 200);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (notificationsFuture != null) {
                    notificationsFuture.cancel(true);
                }
                notificationsFuture = ThreadHelper.getScheduler().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (isToday) {
                            try {
                                api.registerForTripNotifications(AppConfig.get(), k, schedule);
                            } catch (Exception e) {
                                Log.e(TAG, "Unable to register for trip notifications", e);
                            }
                        }
                    }
                }, 3000, TimeUnit.MILLISECONDS);
                Log.i(TAG, "SUCCESSFUL SCHEDULE");
            } catch (Exception e) {
                Log.e(TAG, "UNSUCCESSFUL SCHEDULE", e);
            }
            FlurryAgent.endTimedEvent("LoadSchedule");
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

            handler.post(hideProgress);
            handler.postDelayed(new Runnable() {
                public void run() {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.supportInvalidateOptionsMenu();
                    }
                }

                ;
            }, 50);
            updateSchedulePeriodically();
        }

    };

    Future<?> departureVision;

    private void loadSchedule() {
        if (loadScheduleFuture != null) {
            if(loadScheduleFuture.isCancelled() || loadScheduleFuture.isDone()) {
                loadScheduleFuture.cancel(true);
            }
            return;
        }
        Log.d(TAG, "SCHEDULING!!");
        loadScheduleFuture = ThreadHelper.getScheduler().schedule(loadScheduleRunnable, 100,
                TimeUnit.MILLISECONDS);
    }

    Future<?> moveToNext;

    boolean moving = false;

    private void moveToNextTrain(boolean post) {
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
        final int pos = usablePosition;
        Log.d(TAG, "moveToNextTrain:" + pos);
        if (post) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    list.setSelectionFromTop(pos, 0);
                }
            });
        } else {
            list.setSelectionFromTop(pos, 0);
        }


    }

    Api api;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = new Api(getActivity());
//		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
//			SystemBarTintManager manager = new SystemBarTintManager(getActivity());
//			SystemBarConfig config = manager.getConfig();
//			list.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(), config.getPixelInsetBottom());
//		}
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
        adapter = new ScheduleAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof ScheduleLayout) {
                    ScheduleView sv = (ScheduleView) view.getTag();
                    sv.toggleControls();
                }
            }
        });
        setHasOptionsMenu(true);
        activityCreated = true;
        if (DateUtils.isToday(day.getTime())) {
            header.setShapeColor(getResources().getColor(R.color.get_schedule_11));
            header.setData(from.getShortName() + " \u2192 " + to.getShortName() + "\nToday • " + DAY.format(day));
        } else {
            header.setShapeColor(Color.BLACK);
            header.setData(from.getShortName() + " \u2192 " + to.getShortName() + "\n" + DATE.format(day));
        }
        setFabBookmarkImage();
    }

    private static final SimpleDateFormat DATE = new SimpleDateFormat("EEEE MMMM dd");
    private static final SimpleDateFormat DAY = new SimpleDateFormat("EEEE");

    private class ScheduleAdapter extends BaseExpandableListAdapter implements StickyListHeadersAdapter {

        int TYPE_CONTROLS = 0;

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

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
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            DepartureVisionHeader h = new DepartureVisionHeader(
                    parent.getContext(),null);
            Calendar cal = Calendar.getInstance();
            StationToStation sts = getItem(position);
            cal.setTimeInMillis(getItem(position).getDepartTime().getTimeInMillis());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(day);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);
            if (DateUtils.isToday(cal.getTimeInMillis())) {
                h.setShapeColor(getResources().getColor(R.color.get_schedule_11));
                h.setData(from.getShortName() + " \u2192 " + to.getShortName() + "\nToday • " + DAY.format(cal.getTime()));
            } else {
                h.setShapeColor(Color.BLACK);
                h.setData(from.getShortName() + " \u2192 " + to.getShortName() + "\n" + DATE.format(cal.getTime()));
            }
            h.setLayoutParams(new ViewGroup.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return h;
        }

        @Override
        public long getHeaderId(int position) {
            StationToStation sts = getItem(position);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(sts.getDepartTime().getTimeInMillis());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }

        @Override
        public View getChildView(final int groupPosition,
                                 int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
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
            ScheduleView view;
            if (convertView == null) {
                view = new ScheduleView(parent.getContext(), null);
            } else {
                view = (ScheduleView) convertView.getTag();
            }
            StationToStation sts = getItem(position);
            view.setData(sts, from, to, innerListener, tripIdToAlarm.get(sts.tripId));
            view.setAlarm(tripIdToAlarm.get(sts));
            view.setStatus(tripIdToTrainStatus.get(sts.blockId));
            return view.getView();
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!isVisible() || !isAdded()) {
            return;
        }
        menu.clear();
        inflater.inflate(R.menu.menu_schedule_day, menu);
    }

    ;

    RelativeLayout root;

    FloatingActionButton fabReverse;
    FloatingActionButton fabDate;
    FloatingActionButton fabBookmark;
    FloatingActionButton fabGraph;
    FloatingActionLayout fal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = (RelativeLayout) inflater.inflate(
                R.layout.fragment_day_schedule, container, false);
        list = (ExpandableStickyListHeadersListView) root.findViewById(R.id.list2);
        progressBar = root.findViewById(R.id.progress);
        fabReverse = (FloatingActionButton) root.findViewById(R.id.button_floating_action_reverse);
        fabDate = (FloatingActionButton) root.findViewById(R.id.button_floating_action_change_date);
        fabBookmark = (FloatingActionButton) root.findViewById(R.id.button_floating_action_bookmark);
        fabGraph = (FloatingActionButton) root.findViewById(R.id.button_floating_action_graph);
        header = (DepartureVisionHeader) root.findViewById(R.id.dvheader);
        int color = Color.argb(210, 255, 255, 255);

        fabDate.setImageBitmap(ImageUtil.loadBitmapFromSvgWithColorOverride(getActivity(),R.raw.schedule,color));
        fabBookmark.setImageBitmap(ImageUtil.loadBitmapFromSvgWithColorOverride(getActivity(),R.raw.bookmark,color));
        fabGraph.setImageBitmap(ImageUtil.loadBitmapFromSvgWithColorOverride(getActivity(),R.raw.graph,color));
        fabGraph.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                root.addView(graphBuilder = new GraphBuilderView(getActivity(), null).init(from, to), lp);
            }
        });
        fal = (FloatingActionLayout) root.findViewById(R.id.fal);
        fabBookmark.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                controlListener.onFavorite();
                setFabBookmarkImage();
            }
        });
        fabReverse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                controlListener.onReverse();
            }
        });
        fabDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                controlListener.onDay();
            }
        });
        return root;
    }

    private void setFabBookmarkImage() {
        int color = Color.argb(210, 255, 255, 255);
        int cc = getResources().getColor(R.color.get_schedule_11);
        int bg = cc;
        if (FavoriteHelper.hasFavorite(new DepartureVision(from.getId(), to.getId()))) {
            bg = Color.WHITE;
            color = Color.argb(210, Color.red(cc), Color.green(cc), Color.blue(cc));
        }
        fabBookmark.setImageBitmap(ImageUtil.loadBitmapFromSvgWithColorOverride(getActivity(),R.raw.bookmark,color));
        fabBookmark.setColorNormal(bg);
    }

    GraphBuilderView graphBuilder;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fal.attachToListView(list);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        try {
            getActivity().unregisterReceiver(connectionReceiver);
        } catch (Exception e) {

        }
        if (loadScheduleFuture != null) {
            loadScheduleFuture.cancel(true);
        }
        loadScheduleFuture = null;
        if (poll != null) {
            poll.cancel(true);
        }
        poll = null;
        if (updateScheduleFuture != null) {
            Log.d(TAG,
                    "cancel updateSched: " + updateScheduleFuture.cancel(true));
        }
        updateScheduleFuture = null;
        moveToNext = null;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FlurryAgent.logEvent(item.getTitle() + "MenuItemSelected");
        if (item.getItemId() == R.id.menu_go_to_next_train) {
            moveToNextTrain(true);
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_departurevision) {
            onDepartureVision.onDepartureVision(from, to);
            return true;
        }
        if (item.getItemId() == R.id.menu_rate) {
            FlurryAgent.logEvent("Rate",
                    Collections.singletonMap("time", new Date().toString()));
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getActivity().getString(R.string.share)
                    + getActivity().getPackageName()));
            startActivity(i);
            return true;
        }
        if (item.getItemId() == R.id.menu_share) {
            Map<String, String> k = new HashMap<String, String>();
            k.put("from_id", from.getId());
            k.put("to_id", to.getId());
            k.put("from_name", from.getName());
            k.put("to_name", to.getName());
            k.put("date", new Date().toString());
            FlurryAgent.logEvent("ShareSchedule", k);
            startActivity(Intent.createChooser(
                    Share.intent(AppConfig.get(), this.getActivity(), from, to, day),
                    "Share"));
            return true;
        }
        if (item.getItemId() == R.id.menu_day_push) {
            Intent i = new Intent(getActivity(), RailLinesActivity.class);
            startActivity(i);
        }
        return false;
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(connectionReceiver);
        if (poll != null) {
            if (poll != null) {
                Log.d(TAG, "cancel poll: " + poll.cancel(true));
            }
            poll = null;
        }
        if (updateScheduleFuture != null) {
            if (updateScheduleFuture != null) {
                Log.d(TAG,
                        "cancel updateSched: "
                                + updateScheduleFuture.cancel(true));
            }
            updateScheduleFuture = null;
        }
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (DateUtils.isToday(day.getTime())) {
            menu.removeItem(R.id.menu_go_to_today);
        } else {
            menu.removeItem(R.id.menu_go_to_next_train);
            menu.removeItem(R.id.menu_departurevision);
        }
        final MenuItem i = menu.findItem(R.id.menu_day_push);
        if (i != null) {
            i.setVisible(false);
            ThreadHelper.getScheduler().submit(new Runnable() {
                @Override
                public void run() {
                    if (SettingsFragment.getRegistrationId() == null
                            || WDb.get().getPreference("rail_push_matrix") != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                i.setVisible(false);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                i.setVisible(true);
                            }
                        });

                    }
                }
            });
        }
    }

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
        if (i != null && i.isConnected() && canLoad && SettingsFragment.getUseDepartureVision()
                && !TextUtils.isEmpty(from.getDepartureVision())) {
            poll = ThreadHelper.getScheduler().scheduleAtFixedRate(departureVisionRunnable, 100,
                    SettingsFragment.getPollMilliseconds(), TimeUnit.MILLISECONDS);
        }
    }

    public void setOnDateChange(OnDateChange onDateChange) {
        this.onDateChange = onDateChange;
    }

    public void setOnDepartureVision(OnDepartureVision onDepartureVision) {
        this.onDepartureVision = onDepartureVision;
    }

    public void setOnGetSchedule(OnGetSchedule onGetSchedule) {
        this.onGetSchedule = onGetSchedule;
    }

    int loadingAttempt = 0;

    @Override
    public void setPrimaryItem() {
        if (activityCreated && o == null) {
            loadingAttempt++;
            Log.d(TAG, "LoadingSchedule attepmt " + loadingAttempt);
            loadSchedule();
        }
    }

    public void setScheduleControlListener(
            ScheduleControlListener controlListener) {
        this.controlListener = controlListener;

    }

    @Override
    public void setSecondary() {
        Log.d(TAG, "SETTING SECONDARY");
        if (loadScheduleFuture != null) {
            loadScheduleFuture.cancel(true);
        }
        loadScheduleFuture = null;
        if (poll != null) {
            poll.cancel(true);
        }
        poll = null;
        if (updateScheduleFuture != null) {
            updateScheduleFuture.cancel(true);
        }
        updateScheduleFuture = null;
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

    @Override
    public boolean onBack() {
        System.out.println("onback");
        if (graphBuilder != null) {
            root.removeView(graphBuilder);
            graphBuilder = null;
            return true;
        }
        return false;
    }
}
