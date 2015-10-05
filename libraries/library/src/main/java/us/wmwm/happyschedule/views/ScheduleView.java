package us.wmwm.happyschedule.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.model.Alarm;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.model.TrainStatus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScheduleView {

    StationToStation sts;

    TextView time;
    TextView status;
    TextView timeTillDepart;
    TextView duration;
    TextView train;
    TextView track2;
    View lineIndicator;
    TextView depart;
    View alarm;
    View trainInfoContainer;
    TextView connections;
    RelativeLayout.LayoutParams trainInfoContainerParams;
    float one, two, three, four;
    TextView peakOffpeak;


    static DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
    public static DateFormat times = new SimpleDateFormat("h:mm aa");

    Drawable bg;

    View view;

    ScheduleControlsView controls;

    ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {


            if (track2.getVisibility() == View.VISIBLE) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) track2.getLayoutParams();

                //if(connections.getMeas)
                if (connections.getMeasuredHeight() >= track2.getMeasuredHeight()) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int height = track2.getMeasuredHeight();
                    int connectionsHeight = connections.getMeasuredHeight();
                    int offset = connectionsHeight - height;
                    lp.topMargin = offset / 2 + connections.getTop();
                    track2.setLayoutParams(lp);
                } else {
                    connections.setGravity(Gravity.CENTER_VERTICAL);
                    connections.setMinimumHeight(track2.getMeasuredHeight());
                    int height = track2.getMeasuredHeight();
                    int connectionsHeight = connections.getMeasuredHeight();
                    int offset = height - connectionsHeight;
                    lp.topMargin = offset / 2 + connections.getTop();
                    track2.setLayoutParams(lp);

                }
                // Log.d("ScheduleView",sts.blockId + "Connections height is: " + connections.getMeasuredHeight() + " vs " + track2.getMeasuredHeight());
            } else {
                connections.setGravity(Gravity.NO_GRAVITY);
                connections.setMinimumHeight(1);
            }

        }
    };

    public ScheduleView(Context context, View view) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.view_schedule, null);
        }
        this.view = view;
        view.setTag(this);
        time = (TextView) view.findViewById(R.id.time);
        status = (TextView) view.findViewById(R.id.status);
        timeTillDepart = (TextView) view.findViewById(R.id.departs_in);
        duration = (TextView) view.findViewById(R.id.duration);
        train = (TextView) view.findViewById(R.id.trip_id);

        track2 = (TextView) view.findViewById(R.id.track2);
        alarm = view.findViewById(R.id.alarm);
        depart = (TextView) view.findViewById(R.id.depart);
        connections = depart;
        lineIndicator = view.findViewById(R.id.line_indicator);
        trainInfoContainer = view.findViewById(R.id.train_info_container);
        trainInfoContainerParams = (RelativeLayout.LayoutParams) trainInfoContainer.getLayoutParams();
        bg = view.getBackground();
        one = view.getResources().getInteger(R.integer.departure_vision_one);
        two = view.getResources().getInteger(R.integer.departure_vision_two);
        three = view.getResources().getInteger(R.integer.departure_vision_three);
        four = view.getResources().getInteger(R.integer.departure_vision_four);
        peakOffpeak = (TextView) view.findViewById(R.id.peak);
        int size = PreferenceManager.getDefaultSharedPreferences(context).getInt("textSize", 15);
        time.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 1);
        timeTillDepart.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        depart.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - 1);
        track2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size + 9);
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - 2);
        duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - 2);
        peakOffpeak.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - 2);
        train.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - 3);
        controls = (ScheduleControlsView) view.findViewById(R.id.controls);


        //view.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    public View getView() {
        return view;
    }

    public void setData(StationToStation sts, Station depart, Station arrive, ScheduleControlsView.ScheduleControlListener listener,List<Alarm> alarms) {
        if(this.sts!=null && sts!=null) {
            if(!this.sts.tripId.equals(sts.tripId)) {
                controls.setVisibility(View.GONE);
            }
        }
        this.sts = sts;
        this.controls.setListener(listener);
        train.setVisibility(View.VISIBLE);
        time.setText(shrink(sts.departTime) + " - " + shrink(sts.arriveTime));

        Calendar cal = Calendar.getInstance();
        long diff = sts.departTime.getTimeInMillis() - cal.getTimeInMillis();
        long mins = diff / 60000;
        if (mins >= 0 && mins < 100) {
            timeTillDepart.setVisibility(View.VISIBLE);
            timeTillDepart.setText("departs in " + mins + " min");
        } else {
            timeTillDepart.setVisibility(View.GONE);
            timeTillDepart.setText("");
        }

        if (!TextUtils.isEmpty(sts.tripId)) {
            train.setText("#" + sts.tripId);
        } else {
            train.setText("");
        }
        duration.setText(((sts.arriveTime.getTimeInMillis() - sts.departTime
                .getTimeInMillis()) / 60000) + " minutes");

        this.depart.setText(depart.getShortName() + " to " + arrive.getShortName());
        if (sts instanceof StationInterval) {
            StationInterval sts2 = (StationInterval) sts;
            train.setVisibility(View.GONE);
            duration.setText(duration(sts2));
            time.setText(shrink(sts.departTime) + " - " + shrink(sts2.getArriveTime()));
            if (sts2.schedule.transfers.get(sts2.level).length > 1) {
                populateConnections(sts2);
            } else {
                populateExtraInfo(sts2);
            }
        } else {
            time.setText(shrink(sts.departTime) + " - " + shrink(sts.arriveTime));
        }

        if (!TextUtils.isEmpty(sts.fareType)) {
            this.peakOffpeak.setText(sts.fareType);
            this.peakOffpeak.setVisibility(View.VISIBLE);
        } else {
            this.peakOffpeak.setVisibility(View.GONE);
        }
        controls.setData(alarms,((StationInterval) sts).schedule,sts,null);
    }

    private void populateExtraInfo(StationInterval sts) {
        StringBuilder b = new StringBuilder();
        if (sts.routeId != null) {
            b.append(sts.schedule.routeIdToName.get(sts.routeId));
            b.append(" ");
        }
        if (sts.blockId != null && sts.blockId.trim().length() > 0) {
            b.append("#").append(sts.blockId);
        }
        connections.setText(b);
    }

    private String duration(StationInterval sts) {
        StationInterval sts2 = sts;
        while (sts2.hasNext()) {
            // TODO: optimize by just jumping to the end...
            sts2 = sts2.next();
        }
        long diff = sts2.arriveTime.getTimeInMillis()
                - sts.departTime.getTimeInMillis();
        return "" + diff / 60000 + " minutes";
    }

    private void populateConnections(StationInterval sts2) {
        StringBuilder b = new StringBuilder();
        boolean added = false;
        String lastTripId = null;
        String nextTripId = null;
        while (sts2.hasNext()) {
            boolean isTransfer = sts2.isTransfer();
            nextTripId = sts2.next().tripId;
            if (isTransfer
                    || (sts2.tripId != null && sts2.tripId.equals(lastTripId))) {
                added = false;

            } else {
                added = true;
                String depart = shrink2(sts2.departTime);
                depart = depart.substring(0, depart.length() - 1).replace(" ",
                        "");
//				if (sts2.routeId != null) {
//					b.append(sts2.schedule.routeIdToName.get(sts2.routeId));
//					b.append("\n");
//				}
                if (depart.length() < 6) {
                    //b.append("  ");
                }
                b.append(depart);
                b.append(" ");

                b.append(sts2.schedule.stopIdToName.get(sts2.departId));
                b.append(" ");
                b.append("↝\n");

                if (!(sts2.tripId != null & sts2.tripId.equals(nextTripId))) {
                    String arrive = shrink2(sts2.arriveTime);
                    arrive = arrive.substring(0, arrive.length() - 1).replace(
                            " ", "");

                    if (arrive.length() < 6) {
                        //b.append("  ");
                    }
                    b.append(arrive);
                    b.append(" ");
                    b.append(sts2.schedule.stopIdToName.get(sts2.arriveId));

                    if (sts2.blockId != null
                            && sts2.blockId.trim().length() > 0) {
                        b.append(" #");
                        b.append(sts2.blockId);
                    }

                }
            }

            if (sts2.hasNext()) {
                lastTripId = sts2.tripId;
                sts2 = sts2.next();
                if (added) {
                    b.append(" ");
                    if (sts2.tripId != null && sts2.tripId.equals(lastTripId)) {

                    } else {
                        if (sts2.isTransfer()) {
                            b.append("↻\n");
                        } else {
                            b.append("↝\n");
                        }
                    }
//					if (sts2.routeId != null) {
//						b.append(sts2.schedule.routeIdToName.get(sts2.routeId));
//						b.append("\n");
//					}
                }

            } else {
                break;
            }
        }

        if (sts2.tripId != null && !sts2.tripId.equals(lastTripId)) {
            if (sts2.routeId != null) {
                b.append(sts2.schedule.routeIdToName.get(sts2.routeId));
                b.append("\n");
            }
            String depart = shrink2(sts2.departTime);
            depart = depart.substring(0, depart.length() - 1).replace(" ", "");
            if (depart.length() < 6) {
                //b.append(" ");
            }
            b.append(depart);
            b.append(" ");
            b.append(sts2.schedule.stopIdToName.get(sts2.departId));
            b.append(" ");
            b.append("↝\n");
        }
        String arrive = shrink2(sts2.getArriveTime());
        arrive = arrive.substring(0, arrive.length() - 1).replace(" ", "");
        if (arrive.length() < 6) {
            //b.append(" ");
        }
        b.append(arrive);
        b.append(" ");
        b.append(sts2.schedule.stopIdToName.get(sts2.arriveId));
        if (sts2.blockId != null && sts2.blockId.trim().length() > 0) {
            b.append(" #").append(sts2.blockId);
        }
        connections.setText(b.toString());
    }

    public void setAlarm(List<Alarm> alarm) {
        if (alarm == null) {
            this.alarm.setVisibility(View.GONE);
        } else {
            this.alarm.setVisibility(View.VISIBLE);
        }
    }

    private String shrink(Calendar cal) {
        if (cal == null) {
            return "error";
        }
        return TIME.format(cal.getTime()).toLowerCase();// .replace(" am",														// "a").replace(" pm",														// "p");
    }

    private String shrink2(Calendar cal) {
        if (cal == null) {
            return "error2";
        }
        return times.format(cal.getTime()).toLowerCase();
    }

    public void setStatus(TrainStatus trainStatus) {
        //view.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        track2.setVisibility(View.INVISIBLE);
        status.setVisibility(View.GONE);
        if (trainStatus != null) {
            if (!TextUtils.isEmpty(trainStatus.getStatus())) {
                status.setVisibility(View.VISIBLE);
                status.setText(trainStatus.getStatus());
            } else {
                status.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(trainStatus.getTrack())) {
                if (!TextUtils.isEmpty(trainStatus.getStatus())) {
                    track2.setVisibility(View.VISIBLE);
                } else {
                    track2.setVisibility(View.VISIBLE);
                }
                int len = trainStatus.getTrack().trim().length();
                float size;
                if (len == 1) {
                    size = one;
                } else if (len == 2) {
                    size = two;
                } else if (len == 2) {
                    size = three;
                } else {
                    size = four;
                }
                track2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                track2.setText(trainStatus.getTrack().trim());
            } else {
            }
        } else {
            status.setVisibility(View.GONE);
        }
    }

    public void toggleControls() {
        if (controls.getVisibility() == View.GONE) {
            controls.setVisibility(View.VISIBLE);
        } else {
            controls.setVisibility(View.GONE);
        }

    }

    public void setFavorite(Boolean b) {
        if(b) {
            view.setBackgroundColor(view.getResources().getColor(R.color.favorite_backgorund));
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
    }
}
