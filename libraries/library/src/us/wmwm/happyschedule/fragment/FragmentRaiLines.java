package us.wmwm.happyschedule.fragment;

import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.AppRailLine;
import us.wmwm.happyschedule.model.RailPushMatrix;
import us.wmwm.happyschedule.service.HappyScheduleService;
import us.wmwm.happyschedule.util.Streams;
import us.wmwm.happyschedule.views.RailLineDay;
import us.wmwm.happyschedule.views.RailLineView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.flurry.android.FlurryAgent;

public class FragmentRaiLines extends HappyFragment implements ISecondary {

	ExpandableListView list;
	
	Handler handler = new Handler();
	
	RailPushMatrix railPushMatrix = new RailPushMatrix();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_rail_lines, container,false);
		return list = (ExpandableListView) root;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FlurryAgent.logEvent("FragmentRailLines");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				try {
					final AppConfig config = new AppConfig(new JSONObject(Streams.readFully(Streams.getStream("config.json"))));
					String rpm = WDb.get().getPreference("rail_push_matrix");
					if(rpm==null) {
						railPushMatrix = new RailPushMatrix();
					} else {
						JSONObject o = new JSONObject(rpm);
						railPushMatrix = new RailPushMatrix(o);
					}
					handler.post(new Runnable() {
						public void run() {
							RailLinesAdapter adapter = new RailLinesAdapter(config.getRailLines());
							adapter.setRailListener(railListener);
							list.setAdapter(adapter);
						};
					});
					
				} catch (JSONException e) {
				}
			}
		});
	}
	
	RailListener railListener = new RailListener() {
		@Override
		public void onChecked(AppRailLine appRailLine, int day, int hour, boolean isChecked) {
			railPushMatrix.update(appRailLine, day, hour, isChecked);
			ThreadHelper.getScheduler().submit(new Runnable() {
				public void run() {
					try {
						Log.d("FragmentRailLines", railPushMatrix.toJSON().toString());
						WDb.get().savePreference("rail_push_matrix", railPushMatrix.toJSON().toString());
						WDb.get().savePreference("rail_push_matrix_needs_save", String.valueOf(System.currentTimeMillis()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			});
		}
		
		@Override
		public void onDayChecked(AppRailLine arg, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void onDestroy() {
		super.onDestroy();
		Intent i = new Intent(HappyApplication.get(), HappyScheduleService.class);
		Uri u = Uri.parse("http://wmwm.us?type=push");
		i.setData(u);
		HappyApplication.get().startService(i);
	};
	
	public interface RailListener {
		void onChecked(AppRailLine appRailLine, int day, int hour, boolean isChecked);
		
		void onDayChecked(AppRailLine appRailLine, int day, boolean isChecked);
	}
	private class RailLinesAdapter extends BaseExpandableListAdapter {

		List<AppRailLine> lines;
		
		RailListener railListener;
		
		public void setRailListener(RailListener railListener) {
			this.railListener = railListener;
		}
		
		public RailLinesAdapter(List<AppRailLine> lines) {
			this.lines = lines;
		}

		@Override
		public Integer getChild(int groupPosition, int childPosition) {
			return childPosition + Calendar.SUNDAY;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition + Calendar.SUNDAY;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			RailLineDay v = (RailLineDay) convertView;
			if(v==null) {
				v = new RailLineDay(parent.getContext());
			}
			v.setRailListener(railListener);
			v.setData(getGroup(groupPosition), getChild(groupPosition,childPosition), railPushMatrix.getChecked(getGroup(groupPosition).getKey(), getChild(groupPosition, childPosition)) );
			return v;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return 7;
		}

		@Override
		public AppRailLine getGroup(int groupPosition) {
			return lines.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return lines.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			RailLineView v = (RailLineView) convertView;
			if(v==null) {
				v = new RailLineView(parent.getContext());
			}
			v.setData(getGroup(groupPosition));	
			return v;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}
		
		
				
//		
//		@Override
//		public int getCount() {
//			return lines.size();
//		}
//
//		@Override
//		public AppRailLine getItem(int arg0) {
//			return lines.get(arg0);
//		}
//
//		@Override
//		public long getItemId(int arg0) {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public View getView(int arg0, View arg1, ViewGroup arg2) {
//			return new RailLineView(arg2.getContext());
//		}
		
	}

    @Override
    public void setSecondary() {
        Intent i = new Intent(HappyApplication.get(), HappyScheduleService.class);
        Uri u = Uri.parse("http://wmwm.us?type=push");
        i.setData(u);
        HappyApplication.get().startService(i);
    }
}
