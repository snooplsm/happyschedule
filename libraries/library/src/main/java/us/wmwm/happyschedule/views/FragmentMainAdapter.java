package us.wmwm.happyschedule.views;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.fragment.ChatFragment;
import us.wmwm.happyschedule.fragment.DepartureVisionHelper;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision.DepartureVisionListener;
import us.wmwm.happyschedule.fragment.FragmentHistory;
import us.wmwm.happyschedule.fragment.FragmentHistory.OnHistoryListener;
import us.wmwm.happyschedule.fragment.FragmentPickStations;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.fragment.IPrimary;
import us.wmwm.happyschedule.fragment.IScrollingFragment;
import us.wmwm.happyschedule.fragment.ISecondary;
import us.wmwm.happyschedule.fragment.SettingsFragment;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Station;

public class FragmentMainAdapter extends FragmentStatePagerAdapter implements ImagePagerStrip.ImagePagerStripAdapter {

    OnStationSelectedListener onStationSelectedListener;
    OnStationSelectedListener onStationSelected = new OnStationSelectedListener() {

        @Override
        public void onStation(Station station, State state) {
            notifyDataSetChanged();
            if (onStationSelectedListener != null) {
                onStationSelectedListener.onStation(station, state);
            }
        }
    };
    OnGetSchedule onGetScheduleListener;
    OnGetSchedule onGetSchedule = new OnGetSchedule() {

        @Override
        public void onGetSchedule(Calendar cal, Station from, Station to) {
            if (onGetScheduleListener != null) {
                onGetScheduleListener.onGetSchedule(cal, from, to);
            }
        }
    };

    SettingsFragment.OnPurchaseClickedListener onPurchaseClickedListener;
    OnHistoryListener onHistoryListener;
    DepartureVisionListener departureVisionListener;
    ViewPager.OnPageChangeListener onPageChangeListener;
    Object last;
    List<Option> options;
    ;
    AbsListView.OnScrollListener onScrollListener;
    ;

    public FragmentMainAdapter(FragmentManager fm) {

        super(fm);

    }

    public List<Option> getOptions() {

        List<Option> options = new ArrayList<Option>();
//        history = new FragmentHistory();
//        history.setRetainInstance(false);
//        history.setOnHistoryListener(onHistoryListener);
//        bookmark = new Option("BOOKMARKS",R.raw.bookmark,history);
//
//        options.add(bookmark);

        FragmentPickStations pickStations = new FragmentPickStations();
        pickStations.setRetainInstance(false);
        pickStations.setOnGetSchedule(onGetSchedule);
        Option pick = new Option("SCHEDULE", R.raw.schedule, pickStations);
        options.add(pick);

        FragmentHistory history = new FragmentHistory();
        history.setRetainInstance(false);
        history.setOnHistoryListener(onHistoryListener);
        Option bookmark = new Option("BOOKMARKS", R.raw.bookmark, history);
        options.add(bookmark);

        if (HappyApplication.get().getString(R.string.poller).length() == 0) {

        } else {
            int depts = Math.max(1, DepartureVisionHelper.getDepartureVisions().size());
            if (depts > 0) {
                DepartureVision station = getDepartureVision(0);
                FragmentDepartureVision dv = FragmentDepartureVision.newInstance(
                        station, 0, getDepartureVisionArrival(), null, false);
                dv.setDepartureVisionListener(departureVisionListener);
                dv.setRetainInstance(false);
                //dv.setOnStationSelected(onStationSelected);

                Option departureVision = new Option("DEPARTUREVISION", R.raw.television1, dv);
                options.add(departureVision);
            }
        }

//        ChatFragment r = new ChatFragment();
//
//        Option railLines = new Option("Chat", R.raw.chat, r);
//        options.add(railLines);

        SettingsFragment f = new SettingsFragment();
        f.setOnPurchaseClickedListener(onPurchaseClickedListener);
        Option settings = new Option("SETTINGS", R.raw.globe2, f);
        options.add(settings);
        return options;
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setOnPurchaseClickedListener(SettingsFragment.OnPurchaseClickedListener onPurchaseClickedListener) {
        this.onPurchaseClickedListener = onPurchaseClickedListener;
    }

    public void setDepartureVisionListener(
            DepartureVisionListener departureVisionListener) {
        this.departureVisionListener = departureVisionListener;
    }

    public void setOnHistoryListener(OnHistoryListener onHistoryListener) {
        this.onHistoryListener = onHistoryListener;
    }

    public void setOnGetScheduleListener(OnGetSchedule onGetScheduleListener) {
        this.onGetScheduleListener = onGetScheduleListener;
    }

    public void setOnStationSelectedListener(
            OnStationSelectedListener onStationSelectedListener) {
        this.onStationSelectedListener = onStationSelectedListener;
    }

    public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    @Override
    public void onBack() {
        if (last != null && BackListener.class.isAssignableFrom(last.getClass())) {
            ((BackListener) last).onBack();
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (object != last) {
            if (last != null && last instanceof ISecondary) {
                ((ISecondary) last).setSecondary();
            }
            last = object;
            if (object instanceof IPrimary) {
                ((IPrimary) object).setPrimaryItem();
            }

        }
    }


    @Override
    public Fragment getItem(int pos) {
        Fragment f = options.get(pos).fragment;
        if (f instanceof IScrollingFragment) {
            ((IScrollingFragment) f).setOnScrollingListener(onScrollListener);
        }
        return f;
    }

    public DepartureVision getDepartureVision(int pos) {
        List<DepartureVision> v = DepartureVisionHelper.getDepartureVisions();
        if (v.size() == 0) {
            return null;
        }
        return v.get(pos);
    }

    public Station getDepartureVisionArrival() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get());
        String id = prefs.getString("departureVisionArrivalId", null);
        if (id == null) {
            return null;
        }
        return Db.get().getStop(id);
    }

    @Override
    public int getCount() {
        if(options==null) {
            options = getOptions();
        }
        return options.size();
//		int count = 2;
//		if(HappyApplication.get().getString(R.string.poller).length()==0) {
//
//		} else {
//			count = count+Math.max(1,DepartureVisionHelper.getDepartureVisions().size());
//		}
//		return count;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return options.get(position).title;
//		if (position == 0) {
//			return " History & Favorites ";
//		}
//		if (position == 1) {
//			return " Schedule ";
//		}
//		if (position > 1) {
//			DepartureVision dv = getDepartureVision(position);
//			if (dv == null) {
//				return " Departurevision ";
//			}
//			Station station = Db.get().getStop(dv.getFrom());
//			return " " + station.getName() + " ";
//		}
//		return " Schedule ";
    }

    @Override
    public int getSVGResourceId(int position) {
        return options.get(position).rawResource;
    }

    private class Option {

        String title;
        int rawResource;
        Fragment fragment;

        public Option(String title, int rawResource, Fragment fragment) {
            this.title = title;
            this.rawResource = rawResource;
            this.fragment = fragment;
        }
    }

}
