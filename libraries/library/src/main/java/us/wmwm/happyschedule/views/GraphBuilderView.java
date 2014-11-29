package us.wmwm.happyschedule.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.adapter.StationAdapter;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.model.Station;

/**
 * Created by gravener on 10/5/14.
 */
public class GraphBuilderView extends LinearLayout {

    private Station start;
    private Station end;
    private List<Station> connections = new ArrayList<Station>();

    public GraphBuilderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
    }

    public GraphBuilderView init(Station start, Station end) {
        this.start = start;
        this.end = end;
        setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        removeAllViews();
        if(connections.isEmpty()) {
            connections.add(0, start);
            connections.add(end);
        }
        for(int i = 0; i < connections.size();i++) {
            Station station = connections.get(i);
            TextView t = new TextView(getContext());
            LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            t.setText(station.getName());
            addView(t, lp);
            if(i!=connections.size()-1) {
                final ImageButton b = new ImageButton(getContext());
                b.setColorFilter(Color.BLACK);
                b.setImageResource(R.drawable.ic_action_new);
                b.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        b.setVisibility(View.GONE);
                        final Pair<Integer,Integer> p = (Pair<Integer,Integer>)v.getTag();
                        Integer pos = p.first;
                        AutoCompleteTextView t = new AutoCompleteTextView(getContext());
                        t.setHint("Type Connection Station Here");
                        t.setGravity(Gravity.CENTER_HORIZONTAL);
                        t.setThreshold(1);
                        final StationAdapter adapter = new StationAdapter(getContext(),false);
                        t.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                adapter.filter(s.toString());
                            }
                        });
                        t.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                StationView stationView = (StationView) view;
                                Station stop = Db.get().getStop(stationView.getStationId());
                                GraphBuilderView.this.connections.add(p.second + 1, stop);
                                init(GraphBuilderView.this.start,GraphBuilderView.this.end);
                            }
                        });

                        t.setAdapter(adapter);
                        ViewGroup.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                        addView(t,pos+1,lp);
                    }
                });
                b.setTag(new Pair<Integer,Integer>(getChildCount(),i));
                lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                addView(b,lp);
            }
        }
        if(connections.size()>2) {
            Button save = new Button(getContext());
            save.setGravity(Gravity.CENTER_HORIZONTAL);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            save.setText("Save");
            save.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    WDb.get().addGraph(connections);
                    List<Station> reversed = new ArrayList<Station>(connections);
                    Collections.reverse(reversed);
                    WDb.get().addGraph(reversed);
                }
            });
            addView(save, lp);
        }
        return this;
    }
}
