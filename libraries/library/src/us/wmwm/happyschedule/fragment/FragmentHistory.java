package us.wmwm.happyschedule.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.model.DepartureVision;
import us.wmwm.happyschedule.model.Station;
import us.wmwm.happyschedule.views.DepartureVisionHeader;
import us.wmwm.happyschedule.views.HistoryView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.flurry.android.FlurryAgent;
import com.melnykov.fab.FloatingActionButton;

public class FragmentHistory extends HappyFragment implements IPrimary {

	StickyListHeadersListView list;

	HistoryAdapter adapter;

	View empty;

	Handler handler = new Handler();

    FloatingActionButton fab;

	public interface OnHistoryListener {
		void onHistory(Station from, Station to);
	}

	OnHistoryListener onHistoryListener;
	
	boolean enabled = true;

	public void setOnHistoryListener(OnHistoryListener onHistoryListener) {
		this.onHistoryListener = onHistoryListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.fragment_history, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FlurryAgent.logEvent(item.getTitle() + "MenuItemSelected");
		if (item.getItemId() == R.id.menu_delete_all) {
			showDiscard();
		}
		return super.onOptionsItemSelected(item);
	}

    private void showDiscard() {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle("Delete All History");
        b.setMessage("Are you sure?");
        b.setPositiveButton("Discard", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FlurryAgent.logEvent("AllHistoryDeleted");
                ThreadHelper.getScheduler().submit(new Runnable() {
                    @Override
                    public void run() {
                        WDb.get().deleteAllHistory();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.swap();
                                fab.hide();
                                empty.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                dialog.dismiss();
            }
        }).setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, container,
				false);
		list = (StickyListHeadersListView) view.findViewById(R.id.list);
        fab = (FloatingActionButton) view.findViewById(R.id.button_floating_action_discard);
        fab.attachToListView(list);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscard();
            }
        });
		list.setAdapter(adapter = new HistoryAdapter(getActivity()));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(!enabled) {
					return;
				}
				HistoryView v = (HistoryView) arg1;
				onHistoryListener.onHistory(v.getFromStation(),
						v.getToStation());
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {			
				enabled = false;
				final HistoryView v = new HistoryView(arg1.getContext());
				if(position<adapter.favorites.size()) {
					v.setData(adapter.favorites.get(position));
				} else {
					v.setData(adapter.getItem(position));
				}
				AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setTitle("Delete History Item");
				b.setMessage("Are you sure?");
				b.setPositiveButton("Discard", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final Station from = v.getFromStation();
						final Station to = v.getToStation();
						final long time = v.getTimetime();
						ThreadHelper.getScheduler().submit(new Runnable() {
							@Override
							public void run() {
								if(position<adapter.favorites.size()) {
									FavoriteHelper.remove(adapter.favorites.get(position));
									handler.post(new Runnable() {
										@Override
										public void run() {
											adapter.swap();
										}
									});
								} else {
									WDb.get().delete(from, to, time);
									handler.post(new Runnable() {
										@Override
										public void run() {
											adapter.swap();
										}
									});
								}
							}
						});
						dialog.dismiss();
						enabled = true;
					}
				}).setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						enabled = true;
					}
				}).setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						enabled = true;
					}
				}).
				create().show();
				return false;
			}

		});
		empty = view.findViewById(R.id.empty);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		// FragmentAmazonAd ad = new FragmentAmazonAd();
		// ad.setHappyAdListener(new HappyAdListener() {
		// @Override
		// public void onAd() {
		//
		// }
		// @Override
		// public void onAdFailed(int count, boolean noFill) {
		// handler.post(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// FragmentGoogleAd gad = new FragmentGoogleAd();
		// getFragmentManager().beginTransaction().replace(R.id.fragment_ad,
		// gad).commit();
		// } catch (Exception e) {
		//
		// }
		// }
		// });
		// }
		// });
		// getFragmentManager().beginTransaction().replace(R.id.fragment_ad,
		// ad).commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
        sync();
	}

    private void sync() {
        if (adapter.getCount() == 0) {
            empty.setVisibility(View.VISIBLE);
            fab.hide();
        } else {
            fab.show();
            empty.setVisibility(View.GONE);
        }
    }

	private static class HistoryAdapter extends CursorAdapter implements
            StickyListHeadersAdapter {

		SimpleDateFormat DATE;

		public HistoryAdapter(Context ctx) {
			super(ctx, null, true);
			DATE = new SimpleDateFormat("MMMM dd");
			swapCursor(WDb.get().getHistory());
			favorites = FavoriteHelper.getFavoritesVisions();
		}

		public void swap() {
			favorites = FavoriteHelper.getFavoritesVisions();
			swapCursor(WDb.get().getHistory());
		}

		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			DepartureVisionHeader h = new DepartureVisionHeader(
					parent.getContext());
			if (position < favorites.size()) {
				h.setData("BOOKMARKS");
			} else {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(getItem(position-favorites.size()).getLong(2));
				h.setData(DATE.format(cal.getTime()).toUpperCase());
			}
			h.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			return h;
		}

		List<DepartureVision> favorites;

		@Override
		public Cursor getItem(int position) {
			return (Cursor) super.getItem(position);
		}

		@Override
		public long getHeaderId(int position) {
			if (position < favorites.size()) {
				return 0l;
			}
			Cursor cursor = getItem(position-favorites.size());
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(cursor.getLong(2));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis();
		}

		@Override
		public void bindView(View view, Context arg1, Cursor cursor) {
			HistoryView hv = (HistoryView) view;
			hv.setData(cursor);
		}

		@Override
		public int getCount() {
			return favorites.size() + super.getCount();
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (position < favorites.size()) {
				return getFavoriteView(position, view, parent);
			}
			return super.getView(position - favorites.size(), view, parent);
		}

		private View getFavoriteView(int position, View view, ViewGroup parent) {
			HistoryView hview = (HistoryView) view;
			if (hview == null) {
				hview = (HistoryView) newView(parent.getContext(), null, parent);
			}
			hview.setData(favorites.get(position));
			return hview;
		}

		@Override
		public View newView(Context ctx, Cursor arg1, ViewGroup arg2) {
			return new HistoryView(ctx);
		}

	}

	@Override
	public void setPrimaryItem() {
		// TODO Auto-generated method stub
        ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle("History");
		getActivity().supportInvalidateOptionsMenu();
		FragmentAmazonAd ad = new FragmentAmazonAd();
		ad.setHappyAdListener(new HappyAdListener() {
			@Override
			public void onAd() {
			}

			@Override
			public void onAdFailed(int count, boolean noFill) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							FragmentGoogleAd gad = new FragmentGoogleAd();
							if (getView() != null
									&& getView().findViewById(
											R.id.fragment_history_ad) != null) {
								getChildFragmentManager().beginTransaction()
										.replace(R.id.fragment_history_ad, gad)
										.commit();
							}
						} catch (Exception e) {

						}
					}
				});
			}
		});
		if (getView() != null
				&& getView().findViewById(R.id.fragment_history_ad) != null) {
			try {
				getChildFragmentManager().beginTransaction()
						.replace(R.id.fragment_history_ad, ad).commit();
			} catch (Exception e) {

			}
		}
		if(adapter!=null) {
			adapter.swap();
            sync();
		}

	}

}
