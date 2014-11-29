package us.wmwm.happyschedule.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.FloatingActionLayout;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import twitter4j.Status;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.RailLinesActivity;
import us.wmwm.happyschedule.adapter.StatusAdapter;
import us.wmwm.happyschedule.dao.WDb;

/**
 * Created by gravener on 10/12/14.
 */
public class FragmentStatuses extends Fragment implements IPrimary, ISecondary {

    StickyListHeadersListView list;

    StatusAdapter adapter;

    Handler handler = new Handler();

    FloatingActionLayout fal;
    FloatingActionButton edit;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_alerts,container,false);
        list = (StickyListHeadersListView) root.findViewById(R.id.list);
        fal = (FloatingActionLayout) root.findViewById(R.id.fal);
        edit = (FloatingActionButton) root.findViewById(R.id.edit_alerts);
        return root;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ThreadHelper.getScheduler().submit(new Runnable() {
                @Override
                public void run() {
                    final List<Status> status = WDb.get().getStatuses();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                            adapter.add(status);
                        }
                    });
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(receiver,new IntentFilter("data_inserted"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list.setAdapter(adapter = new StatusAdapter());
        fal.attachToListView(list.getWrappedList());
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), RailLinesActivity.class));
            }
        });
        ThreadHelper.getScheduler().submit(new Runnable() {
            @Override
            public void run() {
                final List<Status> status = WDb.get().getStatuses();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(status);
                    }
                });
            }
        });
    }

    Runnable refresh = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
            handler.postDelayed(this,10000);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(refresh,10000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refresh);
    }

    @Override
    public void setPrimaryItem() {
        handler.removeCallbacks(refresh);
        handler.post(refresh);
    }

    @Override
    public void setSecondary() {
        handler.removeCallbacks(refresh);
    }
}
