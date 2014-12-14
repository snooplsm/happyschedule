package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.fragment.FragmentDepartureVision.DepartureVisionListener;
import us.wmwm.happyschedule.fragment.FragmentHistory.OnHistoryListener;
import us.wmwm.happyschedule.fragment.FragmentPickStations.OnGetSchedule;
import us.wmwm.happyschedule.model.AppAd;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.TripInfo;
import us.wmwm.happyschedule.util.PremiumUserHelper;
import us.wmwm.happyschedule.util.Streams;
import us.wmwm.happyschedule.views.BackListener;
import us.wmwm.happyschedule.views.FragmentMainAdapter;
import us.wmwm.happyschedule.views.ImagePagerStrip;
import us.wmwm.happyschedule.views.OnStationSelectedListener;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.flurry.android.FlurryAgent;
import com.melnykov.fab.FloatingActionButton;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class FragmentMain extends Fragment implements BackListener {

    private static final String TAG = FragmentMain.class.getSimpleName();

    ViewPager pager;

    Handler handler = new Handler();

    BackListener currentFragment;

    OnBackStackChangedListener onBackStackListener = new OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            int count = getFragmentManager().getBackStackEntryCount();
            Log.d(FragmentMain.class.getSimpleName(), "onBackStack " + count);
            if (count == 0) {
                handler.post(new Runnable() {
                    public void run() {
                        android.support.v7.app.ActionBar a = ((ActionBarActivity) getActivity()).getSupportActionBar();
                        if (pager.getCurrentItem() == 0) {
                            a.setSubtitle("History");
                        } else if (pager.getCurrentItem() == 2) {
                            a.setSubtitle("w/ DepartureVision");
                        } else {
                            a.setSubtitle(null);
                        }

                        a.setDisplayHomeAsUpEnabled(false);
                        a.setHomeButtonEnabled(false);
                        getActivity().supportInvalidateOptionsMenu();
//						getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//						getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }

                    ;
                });
            } else {
                BackStackEntry e = getFragmentManager().getBackStackEntryAt(
                        count - 1);
                if ("schedule".equals(e.getName())) {
                    //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
                final String title;
                if (!TextUtils.isEmpty(e.getBreadCrumbTitle())) {
                    title = e.getBreadCrumbTitle().toString();
                } else {
                    title = null;
                }
                handler.post(new Runnable() {
                    public void run() {
                        ActionBar a = ((ActionBarActivity) getActivity()).getSupportActionBar();
                        getActivity().supportInvalidateOptionsMenu();
                        a.setSubtitle(title);
                    }

                    ;
                });
            }
        }
    };

    ImagePagerStrip strip;

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ad.getLayoutParams();
                lp.topMargin = view.getMeasuredHeight();//-ad.getMeasuredHeight();
                ad.setLayoutParams(lp);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        pager = (ViewPager) view.findViewById(R.id.pager);
        ad = view.findViewById(R.id.ad);
        ad.setVisibility(View.INVISIBLE);
        //indic = (TabPageIndicator) view.findViewById(R.id.indicator);
        strip = (ImagePagerStrip) view.findViewById(R.id.indicator);
        pager.setPageMargin((int) (getResources()
                .getDimension(R.dimen.activity_horizontal_margin)));
        return view;
    }

    OnGetSchedule onGetSchedule;

    View ad;

    PositionAd positionAd = new PositionAd() {

    };

    class PositionAd implements Runnable {

        float v;

        @Override
        public void run() {
            int height = getView().getMeasuredHeight();
            int vis = getVisibility();
            if(vis== View.VISIBLE) {
                Log.d(TAG, "setting visibility to visible in run()");
            }
            ad.setVisibility(vis);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ad.getLayoutParams();
            float adHeight = (float) lp.height;
            int newHeight = (int) getResources().getDimension(R.dimen.ad_height);
            lp.topMargin = height - newHeight;
            ad.setLayoutParams(lp);
        }


        protected int getVisibility() {
            return View.VISIBLE;
        }
    };

    PositionAd hide = new PositionAd() {
        @Override
        public void run() {
            super.run();
        }

        @Override
        protected int getVisibility() {
            return View.INVISIBLE;
        }
    };


    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float v, int i2) {
            if (position == 0) {
                int height = getView().getMeasuredHeight();
                Log.d(TAG,"visibility direction " + (strip.getDirection()==ImagePagerStrip.RIGHT ? "left":"right"));
                if(strip.getNextPosition()==0) {

                } else {
                    Log.d(TAG,"setting visibility to visible");
                    ad.setVisibility(View.VISIBLE);
                }
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ad.getLayoutParams();
                float adHeight = (float) lp.height;
                int newHeight = (int) (adHeight * v);
                lp.topMargin = height - newHeight;
                ad.setLayoutParams(lp);
            }
        }

        @Override
        public void onPageSelected(int i) {
            if(i==0) {
                handler.removeCallbacks(positionAd);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
            if(i== ViewPager.SCROLL_STATE_SETTLING) {
                handler.removeCallbacks(positionAd);
                handler.removeCallbacks(hide);
                if(pager.getCurrentItem()==0&&strip.getDirection()==ImagePagerStrip.RIGHT) {
                    handler.postDelayed(hide,300);
                } else
                if(strip.getDirection()==ImagePagerStrip.LEFT && strip.getNextPosition()==0) {
                    handler.postDelayed(hide,300);
                } else {
                    if(pager.getCurrentItem()==0) {

                    } else {
                        handler.postDelayed(positionAd, 500);
                    }
                }
            }
        }
    };

    AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        private int mScrollY;

        protected int getListViewScrollY(AbsListView listView) {
            View topChild = listView.getChildAt(0);
            return topChild == null ? 0 : listView.getFirstVisiblePosition() * topChild.getHeight() -
                    topChild.getTop();
        }

        @Override
        public void onScroll(AbsListView absListView, int k, int i2, int i3) {
            if (!false) {
                return;
            }
            if (absListView.getChildCount() == 0) {
                return;
            }
            Log.d(TAG,"SCROLLLEEE TOP " + absListView.getChildAt(0).getTop());
            int newScrollY = getListViewScrollY(absListView);
            if (newScrollY == mScrollY) {
                return;
            }

            if (newScrollY > mScrollY) {
                // Scrolling up
                Log.d(TAG,"scrolling up: " + newScrollY + " > " + mScrollY);
                strip.offsetTopAndBottom(mScrollY - newScrollY);
            } else if (newScrollY < mScrollY) {
                // Scrolling down
                strip.offsetTopAndBottom(mScrollY - newScrollY);
                Log.d(TAG,"scrolling down: " + newScrollY + " < " + mScrollY);
            }
            mScrollY = newScrollY;
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
        getFragmentManager().addOnBackStackChangedListener(onBackStackListener);
        AbstractAdFragment ad = new FragmentHappytapAd();

        getFragmentManager().beginTransaction().replace(R.id.ad, ad)
                .commit();
        handler.post(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getFragmentManager();
                if (fm == null) {
                    return;
                }
                final FragmentMainAdapter fma = new FragmentMainAdapter(
                        fm) {
                    @Override
                    public void setPrimaryItem(ViewGroup container, int position, Object object) {
                        super.setPrimaryItem(container, position, object);
                        if (object != null && object instanceof BackListener) {
                            currentFragment = (BackListener) object;
                        } else {
                            currentFragment = null;
                        }
                    }
                };
                pager.setOffscreenPageLimit(fma.getCount());
                pager.setAdapter(fma);
                strip.setAdapter(fma);
                fma.setOnScrollListener(onScrollListener);
                strip.setOnPageChangeListener(onPageChangeListener);
                strip.setViewPager(pager);

                fma.setOnGetScheduleListener(onGetSchedule = new OnGetSchedule() {

                    @Override
                    public void onGetSchedule(final Calendar day, final Station from, final Station to) {
                        Map<String, String> args = new HashMap<String, String>();
                        args.put("from_id", from.getId());
                        args.put("to_id", to.getId());
                        args.put("from_name", from.getName());
                        args.put("to_name", to.getName());
                        args.put("day", day.getTime().toString());
                        FlurryAgent.logEvent("OnGetSchedule", args);
                        if (getFragmentManager().getBackStackEntryCount() == 0) {
                        } else {
                            for (int i = getFragmentManager()
                                    .getBackStackEntryCount() - 1; i >= 0; i--) {
                                BackStackEntry e = getFragmentManager()
                                        .getBackStackEntryAt(i);
                                if ("schedule".equals(e.getName())) {
                                    getFragmentManager().popBackStackImmediate();
                                }
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FragmentTransaction t = getFragmentManager()
                                        .beginTransaction();
                                FragmentSchedule fs = FragmentSchedule.newInstance(
                                        day, from, to);
                                fs.setOnGetSchedule(onGetSchedule);
                                t.addToBackStack("schedule");
                                t.replace(R.id.fragment_schedule, fs)
                                        .setBreadCrumbTitle(
                                                (from.getName() + " to " + to.getName()))
                                        .commit();
//								ActionBar a = ((ActionBarActivity)getActivity()).getSupportActionBar();
//								a.setDisplayHomeAsUpEnabled(true);
//								a.setHomeButtonEnabled(true);
//								a.setDisplayUseLogoEnabled(true);
                            }
                        });

                    }
                });
                fma.setOnHistoryListener(new OnHistoryListener() {

                    @Override
                    public void onHistory(final Station from, final Station to) {
                        onGetSchedule.onGetSchedule(Calendar.getInstance(), from, to);
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    WDb.get().savePreference("lastDepartId",
                                            from.getId());
                                    WDb.get().savePreference("lastArriveId",
                                            to.getId());
                                    WDb.get().saveHistory(from, to);
                                } catch (Exception e) {

                                }
                            }
                        }.start();
                    }
                });
                fma.setDepartureVisionListener(new DepartureVisionListener() {
                    @Override
                    public void onTrip(String blockId) {
                        String tripId = ScheduleDao.get().getBlockId(blockId);
                        TripInfo tinfo = ScheduleDao.get().getStationTimesForTripId(tripId, 0, Integer.MAX_VALUE);
                        if (tinfo.stops.isEmpty()) {
                            return;
                        }
                        Station from = Db.get().getStop(tinfo.stops.get(0).id);
                        Station to = Db.get().getStop(tinfo.stops.get(tinfo.stops.size() - 1).id);
                        FragmentTrip t = FragmentTrip.newInstance(from, to,
                                tripId);


                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_schedule, t).addToBackStack(null)
                                .setBreadCrumbTitle(from.getName() + " to " + to.getName())
                                .commit();
                    }
                });
                fma.setOnStationSelectedListener(new OnStationSelectedListener() {
                    @Override
                    public void onStation(Station station, State state) {
                        int pos = pager.getCurrentItem();
                        pager.setAdapter(null);
                        pager.setAdapter(fma);
                        int newPos = pos;
                        if (state == State.ADDED) {
                            newPos++;
                        }
                        pager.setCurrentItem(newPos);
                    }
                });
                // fma.setOnStationSelectedListener(new
                // OnStationSelectedListener() {
                //
                // @Override
                // public void onStation(Station station) {
                // if(station==null) {
                // pager.setCurrentItem(pager.getCurrentItem()-1);
                // return;
                // }
                // for(int i = 1; i < fma.getCount(); i++) {
                // Station s = fma.getDepartureVision(i);
                // if(s.getId().equals(station.getId())) {
                // pager.setCurrentItem(i);
                // break;
                // }
                // }
                // //pager.invalidate();
                // }
                // });
            }
        });
        ThreadHelper.getScheduler().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!PremiumUserHelper.isPaidUser()) {
                        AppConfig config = AppConfig.get();
                        final AppAd ad = config.getBestAd(getActivity(), FragmentMain.class);
                        if (ad != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    FragmentHappyAd fad = FragmentHappyAd.newIntance(ad);
                                    FragmentManager manager = getFragmentManager();
                                    if (manager != null) {
                                        manager.beginTransaction().replace(R.id.main_fragment_ad, fad).commit();
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {

                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getFragmentManager().removeOnBackStackChangedListener(
                onBackStackListener);
    }

    @Override
    public boolean onBack() {
        if (currentFragment != null) {
            if (currentFragment.onBack()) {
                return true;
            }
        }
        if (pager.getAdapter().getCount() > 0) {
            if (pager.getCurrentItem() != 0) {
                pager.setCurrentItem(0);
                return true;
            }
        }
        return false;
    }
}
