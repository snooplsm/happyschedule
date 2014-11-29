package us.wmwm.happyschedule.adapter;

import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import twitter4j.Status;
import us.wmwm.happyschedule.views.DepartureVisionHeader;
import us.wmwm.happyschedule.views.TweetView;

/**
 * Created by gravener on 10/12/14.
 */
public class StatusAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    List<Status> statuses = new ArrayList<Status>(30);

    SimpleDateFormat DATE = new SimpleDateFormat("MMMM dd");

    @Override
    public int getCount() {
        return statuses.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Status status = statuses.get(position);
        TweetView v = (TweetView)convertView;
        if(v==null) {
            v = new TweetView(parent.getContext());
        }
        v.setData(status);
        return v;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        DepartureVisionHeader h = new DepartureVisionHeader(
                parent.getContext(),null);
        Status status = statuses.get(position);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(status.getCreatedAt().getTime());
        h.setData(DATE.format(cal.getTime()).toUpperCase());
        if(DateUtils.isToday(cal.getTimeInMillis())) {
            h.setData("TODAY");
        }
        h.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return h;
    }

    public void add(List<Status> status) {
        this.statuses.addAll(status);
        notifyDataSetChanged();
    }

    public void clear() {
        this.statuses.clear();
        notifyDataSetChanged();
    }

    @Override
    public long getHeaderId(int position) {
        if (position < statuses.size()) {
            return 0l;
        }
        Status status = statuses.get(position);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(status.getCreatedAt().getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
