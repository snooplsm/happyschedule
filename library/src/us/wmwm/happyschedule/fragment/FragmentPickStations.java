package us.wmwm.happyschedule.fragment;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.activity.ActivityPickStation;
import us.wmwm.happyschedule.activity.SettingsActivity;
import us.wmwm.happyschedule.dao.Db;
import us.wmwm.happyschedule.dao.ScheduleDao;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.util.Share;
import us.wmwm.happyschedule.views.ClipDrawListener;
import us.wmwm.happyschedule.views.HappyShadowBuilder;
import us.wmwm.happyschedule.views.StationButton;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class FragmentPickStations extends Fragment implements IPrimary {

	StationButton departureButton;
	StationButton arrivalButton;
	View getScheduleButton;
	FragmentStationPicker picker;
	View reverseButton;
	View reverseButtonContainer;
	View reverseHolder;
	TextView fare;

	Handler handler = new Handler();

	public static interface OnGetSchedule {
		void onGetSchedule(Station from, Station to);
	}

	OnGetSchedule onGetSchedule;

	public void setOnGetSchedule(OnGetSchedule onGetSchedule) {
		this.onGetSchedule = onGetSchedule;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	View root;
	
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
		reverseButton = root.findViewById(R.id.reverse);
		reverseButtonContainer = root.findViewById(R.id.reverse_container);
		reverseHolder = root.findViewById(R.id.reverse_holder);
		fare = (TextView) root.findViewById(R.id.fare);
		return root;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_pick_stations, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (reverseButton.getVisibility() != View.VISIBLE) {
			menu.removeItem(R.id.menu_reverse);
		}
		//ShareCompat.configureMenuItem(menu, R.id.menu_share, Share.intent(getActivity()));
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_reverse) {
			FlurryAgent.logEvent("RevsereMenuOptionClicked",Collections.singletonMap("time", new Date().toString()));
			reverse();
			return true;
		}
		if(item.getItemId()==R.id.menu_settings) {
			startActivity(new Intent(getActivity(), SettingsActivity.class));
		}
		if(item.getItemId()==R.id.menu_share_pick) {
			FlurryAgent.logEvent("ShareApp", Collections.singletonMap("time", new Date().toString()));
			startActivity(Intent.createChooser(Share.intent(getActivity()),"Share"));
		}
		return super.onOptionsItemSelected(item);
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
				Map<String, Double> fares = ScheduleDao.get().getFairs(
						departureButton.getStation().getId(),
						arrivalButton.getStation().getId());
				Map<String,String> k = new HashMap<String,String>();
				k.put("from_id", departureButton.getStation().getId());
				k.put("to_id", arrivalButton.getStation().getId());
				k.put("from_name", departureButton.getStation().getName());
				k.put("to_name", arrivalButton.getStation().getName());
				if (fares == null) {
					FlurryAgent.logEvent("FareNotFound",k);
					return;
				}
				final String adult = DecimalFormat.getCurrencyInstance()
						.format(fares.get("Adult"));
				FlurryAgent.logEvent("FareFound",k);
				handler.post(new Runnable() {
					@Override
					public void run() {
						fare.setText("Fare: " + adult);
						fare.setVisibility(View.VISIBLE);
					}
				});
			}
		});
	}

	private void setupReverseButton() {
		boolean canShowReverse = arrivalButton.getStation() != null
				&& departureButton.getStation() != null;
		if (!canShowReverse) {
			reverseButton.setVisibility(View.GONE);
		} else {
			reverseButton.setVisibility(View.VISIBLE);			
		}
		ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				String percentL = WDb.get().getPreference(WDb.REVERSE_BUTTON_MARGIN_LEFT_PERCENTAGE);
				if(percentL==null) {
					percentL = ".05";
				}
				if(!".05".equals(percentL)) {
					FlurryAgent.logEvent("ReverseButtonPosition", Collections.singletonMap("percent", percentL));
				}
				final Float percentLeft = Float.parseFloat(percentL);
				handler.post(new Runnable() {
					@Override
					public void run() {
						//MarginLayoutParams lp = (MarginLayoutParams) reverseButtonContainer.getLayoutParams();
						//lp.leftMargin = (int)(reverseButtonContainer.getWidth()*left) - reverseButton.getWidth();
						MarginLayoutParams lp = (MarginLayoutParams) reverseHolder.getLayoutParams();
						lp.leftMargin = (int) (percentLeft * reverseButtonContainer.getWidth());
						reverseHolder.setLayoutParams(lp);
						//reverseButtonContainer.setLayoutParams(lp);	
					}
				});
				
			}
		});
		getActivity().invalidateOptionsMenu();
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
				// picker = new FragmentStationPicker();
				// getFragmentManager().beginTransaction()
				// .replace(R.id.secondary_view, picker)
				// .addToBackStack(null).commit();
				// picker.setOnStationSelectedListener(new
				// OnStationSelectedListener() {
				//
				// @Override
				// public void onStation(String stationId) {
				// button.setStation(stationId);
				// getFragmentManager().beginTransaction().remove(picker)
				// .commit();
				// picker = null;
				// }
				// });
			}
		};

		OnClickListener reverse = new OnClickListener() {

			@Override
			public void onClick(View v) {
				FlurryAgent.logEvent("RevserButtonClicked");
				reverse();
			}
		};

		reverseButton.setOnClickListener(reverse);

		// reverseButton.setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// if(event.getAction()==MotionEvent.ACTION_DOWN) {
		//
		// }
		// if(event.getAction()==MotionEvent.ACTION_MOVE) {
		// }
		// return false;
		// }
		// });
		reverseButton.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				FlurryAgent.logEvent("ReverseButtonLongClick");
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new HappyShadowBuilder(view);
				
				
				ClipDrawListener c = new ClipDrawListener(view) {
					
					protected void onStart(float xStart, View view, DragEvent event) {
						FlurryAgent.logEvent("ReverseButtonDrag",true);
						reverseButton.setVisibility(View.INVISIBLE);
					}
					
					@Override
					protected void onDrop(float xStart, View view, DragEvent event) {
						FlurryAgent.endTimedEvent("ReverseButtonDrag");
						reverseButton.setVisibility(View.VISIBLE);
						int left = (int)event.getX() - reverseButton.getWidth()/2;
						final Float percentLeft = left / (float)reverseButtonContainer.getWidth();
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
				//view.setOnDragListener(c);
				view.startDrag(data, shadowBuilder, view, 0);				
				//view.setVisibility(View.INVISIBLE);
				return true;
			}
		});
		
		departureButton.setOnClickListener(onClick);
		arrivalButton.setOnClickListener(onClick);

		OnClickListener onClickGetSchedule = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!canGetSchedule()) {
					return;
				}
				final Station depart = departureButton.getStation();
				final Station arrive = arrivalButton.getStation();
				onGetSchedule.onGetSchedule(depart, arrive);
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
		};

		getScheduleButton.setOnClickListener(onClickGetSchedule);
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
		if(!canGet) {
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
		getActivity().getActionBar().setSubtitle(null);
	}

}
