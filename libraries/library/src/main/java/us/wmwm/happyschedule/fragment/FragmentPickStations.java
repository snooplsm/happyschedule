package us.wmwm.happyschedule.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.larvalabs.svgandroid.SVGBuilder;
import com.melnykov.fab.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.ActivityPickStation;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.util.ImageUtil;
import us.wmwm.happyschedule.views.BackListener;
import us.wmwm.happyschedule.views.ClipDrawListener;
import us.wmwm.happyschedule.views.HappyShadowBuilder;
import us.wmwm.happyschedule.views.ScrollingView;
import us.wmwm.happyschedule.views.StationButton;

public class FragmentPickStations extends Fragment implements IPrimary, ISecondary, BackListener {

    private static final String TAG = FragmentPickStations.class.getSimpleName();

    StationButton departureButton;
    StationButton arrivalButton;
    View getScheduleButton;
    FragmentStationPicker picker;
    FloatingActionButton reverseButton;
    ImageView bookmarkButton;
    View reverseButtonContainer;
    View reverseHolder;
    TextView fare;
    LinearLayout scrollingContent;
    ScrollingView scrollView;

    Handler handler = new Handler();

    boolean canEatBack;
    OnGetSchedule onGetSchedule;
    View root;
    FragmentSchedule fragmentSchedule;

    public void setOnGetSchedule(OnGetSchedule onGetSchedule) {
        this.onGetSchedule = onGetSchedule;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_pick_stations,
                container, false);
        departureButton = (StationButton) root.findViewById(R.id.depart_button);
        arrivalButton = (StationButton) root.findViewById(R.id.arrive_button);
        departureButton.setHint("Departure Station");
        arrivalButton.setHint("Arrival Station");
        getScheduleButton = root.findViewById(R.id.get_schedule);
        reverseButton = (FloatingActionButton) root.findViewById(R.id.reverse);
        bookmarkButton = (ImageView) root.findViewById(R.id.bookmarked);
        reverseButtonContainer = root.findViewById(R.id.reverse_container);
        reverseHolder = root.findViewById(R.id.reverse_holder);
        fare = (TextView) root.findViewById(R.id.fare);
        scrollingContent = (LinearLayout) root.findViewById(R.id.scrolling_content);
        scrollView = (ScrollingView) root.findViewById(R.id.scroll_container);
        return root;
    }

    private void findFare() {
        fare.setVisibility(View.GONE);
        ThreadHelper.getScheduler().submit(new Runnable() {
            @Override
            public void run() {
                if (departureButton.getStation() != null
                        && arrivalButton.getStation() != null) {

                } else {
                    return;
                }
                final Map<String, Double> fares = ScheduleDao.get().getFairs(
                        departureButton.getStation().getId(),
                        arrivalButton.getStation().getId());
                Map<String, String> k = new HashMap<String, String>();
                k.put("from_id", departureButton.getStation().getId());
                k.put("to_id", arrivalButton.getStation().getId());
                k.put("from_name", departureButton.getStation().getName());
                k.put("to_name", arrivalButton.getStation().getName());
                if (fares == null) {
                    FlurryAgent.logEvent("FareNotFound", k);
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int start = scrollingContent.getChildAt(0) instanceof RelativeLayout ? 1 : 2;
                        while (scrollingContent.getChildCount() > start) {
                            scrollingContent.removeViewAt(scrollingContent.getChildCount() - 1);
                        }
                    }
                });
                final String adult = DecimalFormat.getCurrencyInstance()
                        .format(fares.get("Adult"));
                FlurryAgent.logEvent("FareFound", k);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        fare.setText("Fare: " + adult);
                        fare.setVisibility(View.VISIBLE);
                        for (Map.Entry<String, Double> fare : fares.entrySet()) {
                            TextView t = new TextView(getActivity());
                            t.setText(fare.getKey() + " : " + DecimalFormat.getCurrencyInstance()
                                    .format(fare.getValue()));
                            scrollingContent.addView(t);
                        }
                    }
                });
            }
        });
    }

    private void setupReverseButton() {
        boolean canShowReverse = arrivalButton.getStation() != null
                && departureButton.getStation() != null;
        if (!canShowReverse) {
            reverseButton.setVisibility(View.INVISIBLE);
        } else {
            reverseButton.setVisibility(View.VISIBLE);
        }
        ThreadHelper.getScheduler().submit(new Runnable() {
            @Override
            public void run() {
                String percentL = WDb.get().getPreference(WDb.REVERSE_BUTTON_MARGIN_LEFT_PERCENTAGE);
                if (percentL == null) {
                    percentL = ".05";
                }
                if (!".05".equals(percentL)) {
                    FlurryAgent.logEvent("ReverseButtonPosition", Collections.singletonMap("percent", percentL));
                }
                final Float percentLeft = Float.parseFloat(percentL);
                boolean hasFavorite;
                try {
                    hasFavorite = FavoriteHelper.hasFavorite(new DepartureVision(departureButton.getStation().getId(), arrivalButton.getStation().getId()));
                } catch (Exception e) {
                    hasFavorite = false;
                }
                final boolean bookmarked = hasFavorite;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MarginLayoutParams lp = (MarginLayoutParams) reverseHolder.getLayoutParams();
                        lp.leftMargin = (int) (percentLeft * reverseButtonContainer.getWidth());
                        reverseHolder.setLayoutParams(lp);
                        if (bookmarked) {
                            bookmarkButton.setVisibility(View.VISIBLE);
                        } else {
                            bookmarkButton.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) view;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int height = view.getMeasuredHeight();
                int getSchedHeight = getScheduleButton.getMeasuredHeight();
                View v = new View(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - getSchedHeight);
                v.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                scrollingContent.addView(v, 0, lp);
            }
        });

        disallowTouches(viewGroup);

    }

    private void disallowTouches(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                disallowTouches(vg);
                vg.requestDisallowInterceptTouchEvent(false);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String lastDepartId = WDb.get().getPreference("lastDepartId");
        String lastArriveId = WDb.get().getPreference("lastArriveId");

        if (lastDepartId != null) {
            Station station = Db.get().getStop(lastDepartId);
            if (station != null) {
                departureButton.setStation(station);
            }
        }

        if (lastArriveId != null) {
            Station station = Db.get().getStop(lastArriveId);
            if (station != null) {
                arrivalButton.setStation(station);
            }
        }

        setupReverseButton();

        findFare();
        OnClickListener onClick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                final StationButton button = (StationButton) v;
                Intent i = ActivityPickStation.from(getActivity(), false);
                final int code;
                if (button == arrivalButton) {
                    code = 200;
                } else {
                    code = 100;
                }
                startActivityForResult(i, code);

            }
        };

        OnClickListener reverse = new OnClickListener() {

            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("RevserButtonClicked");
                reverse();
            }
        };

        int color = Color.argb(210, 255, 255, 255);

        Bitmap bitmap = ImageUtil.loadBitmapFromSvgWithColorOverride(getActivity(), R.raw.retweet, color);
        int wihi = (int) (getResources().getDimension(R.dimen.fab_size_normal) *.75f);
        reverseButton.setImageBitmap(Bitmap.createScaledBitmap(bitmap,wihi,wihi,false));
        reverseButton.setOnClickListener(reverse);

        reverseButton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                FlurryAgent.logEvent("ReverseButtonLongClick");
                ClipData data = ClipData.newPlainText("", "");
                DragShadowBuilder shadowBuilder = new HappyShadowBuilder(view);


                ClipDrawListener c = new ClipDrawListener(view) {

                    protected void onStart(float xStart, View view, DragEvent event) {
                        FlurryAgent.logEvent("ReverseButtonDrag", true);
                        reverseButton.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    protected void onDrop(float xStart, View view, DragEvent event) {
                        FlurryAgent.endTimedEvent("ReverseButtonDrag");
                        reverseButton.setVisibility(View.VISIBLE);
                        int left = (int) event.getX() - reverseButton.getWidth() / 2;
                        final Float percentLeft = left / (float) reverseButtonContainer.getWidth();
                        MarginLayoutParams lp = (MarginLayoutParams) reverseHolder.getLayoutParams();
                        lp.leftMargin = (int) (percentLeft * reverseButtonContainer.getWidth());
                        reverseHolder.setLayoutParams(lp);
                        ThreadHelper.getScheduler().submit(new Runnable() {
                            @Override
                            public void run() {
                                WDb.get().savePreference("reverseButtonMarginLeftPercentage", percentLeft.toString());
                            }
                        });
                    }
                };
                root.setOnDragListener(c);
                view.startDrag(data, shadowBuilder, view, 0);
                return true;
            }
        });

        departureButton.setOnClickListener(onClick);
        arrivalButton.setOnClickListener(onClick);

        OnClickListener onClickGetSchedule = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Station depart = departureButton.getStation();
                Station arrive = arrivalButton.getStation();
                getSchedule(depart, arrive, Calendar.getInstance());
            }
        };

        getScheduleButton.setOnClickListener(onClickGetSchedule);


        Fragment frag = getChildFragmentManager().findFragmentById(R.id.secondary_view);
        if (frag != null) {
            if (frag instanceof FragmentSchedule) {
                this.fragmentSchedule = (FragmentSchedule) frag;
            }
        }

        SVGBuilder b = new SVGBuilder().readFromResource(getResources(), R.raw.bookmark_alt).setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.get_schedule_11), PorterDuff.Mode.SRC_ATOP));
        int twentySix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        Picture p = b.build().getPicture();
        Bitmap bb = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bb);
        c.drawPicture(p);
        bookmarkButton.setImageBitmap(bb);
    }

    private void getSchedule(final Station depart, final Station arrive, Calendar calendar) {
        if (!canGetSchedule()) {
            return;
        }

        fragmentSchedule = FragmentSchedule.newInstance(calendar, depart, arrive, true);
        fragmentSchedule.setOnGetSchedule(onGetSchedule = new OnGetSchedule() {
            @Override
            public void onGetSchedule(Calendar day, Station from, Station to) {
                getSchedule(from, to, day);
            }
        });
        getChildFragmentManager().beginTransaction().replace(R.id.secondary_view, fragmentSchedule).commit();

        ThreadHelper.getScheduler().submit(new Runnable() {
            @Override
            public void run() {
                WDb.get().savePreference("lastDepartId", depart.getId());
                WDb.get().savePreference("lastArriveId", arrive.getId());
                WDb.get().saveHistory(depart, arrive);
                Log.d(getClass().getSimpleName(), "saving history "
                        + depart + " to " + arrive);
            }
        });
    }

    protected boolean canGetSchedule() {
        Station depart = departureButton.getStation();
        Station arrive = arrivalButton.getStation();
        boolean canGet = true;
        if (depart == null) {
            animate(departureButton);
            canGet = false;
        }
        if (arrive == null) {
            animate(arrivalButton);
            canGet = false;
        }
        if (!canGet) {
            FlurryAgent.logEvent("CanNotGetSchedule");
        }
        return canGet;
    }

    private void animate(View view) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(),
                R.anim.scale_up);
        view.startAnimation(anim);
    }

    private void reverse() {
        Station tmp = departureButton.getStation();
        departureButton.setStation(arrivalButton.getStation());
        arrivalButton.setStation(tmp);
        setupReverseButton();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Station station = (Station) data.getSerializableExtra("station");
            if (requestCode == 200) {
                arrivalButton.setStation(station);
            } else {
                departureButton.setStation(station);
            }
            findFare();
            setupReverseButton();
        }
    }

    @Override
    public void setPrimaryItem() {
        Log.d(TAG, "setPrimaryItem");
        canEatBack = true;
    }

    @Override
    public void setSecondary() {
        canEatBack = false;
    }

    @Override
    public boolean onBack() {
        FragmentBackListener back = new FragmentBackListener(this);
        if (back.onBack()) {
            return true;
        }
        Log.d(TAG, "fragment pick stations back");
        if (fragmentSchedule != null) {
            getChildFragmentManager().beginTransaction().remove(fragmentSchedule).commit();
            Log.d(TAG, "fragment pick stations back remove frag");
            fragmentSchedule = null;
            return true;
        }
        return false;
    }

    public static interface OnGetSchedule {
        void onGetSchedule(Calendar day, Station from, Station to);
    }
}
