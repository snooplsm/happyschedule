package us.wmwm.happyschedule.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import us.wmwm.happyschedule.BuildConfig;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class FragmentLoad extends HappyFragment {

	Future<?> loadFuture;
	
	ImageView logo; 
	
	Handler handler = new Handler();

    private static final String TAG = FragmentLoad.class.getSimpleName();
	
	public static interface OnLoadListener {
		void onLoaded();		
	}
	
	OnLoadListener listener;
	
	public FragmentLoad setListener(OnLoadListener listener) {
		this.listener = listener;
		return this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_load,container,false);
		logo = (ImageView) v.findViewById(R.id.logo);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
            logo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
		return v;
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (!isUpdated(getActivity())) {
			removeDatabase(getActivity());
			createDatabase();
		}
		
		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.logo);
		logo.setImageDrawable(svg.createPictureDrawable());
        ((ActionBarActivity)getActivity()).getSupportActionBar().hide();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
        ((ActionBarActivity)getActivity()).getSupportActionBar().show();
	}

	private void createDatabase() {
		loadFuture = ThreadHelper.getScheduler().submit(new Runnable() {
			@Override
			public void run() {
				Context ctx = getActivity();
				boolean success = false;
				if(ctx==null) {
					return;
				}
				File outputFile = null;
				boolean isExternal = false;
				if (isMediaMounted()) {
					outputFile = getExternalFile(ctx,"database.db");
					outputFile.getParentFile().mkdirs();
					isExternal = true;
				} else {
					outputFile = getInternalFile(ctx, "database.db");
				}

				InputStream in = null;
				ZipInputStream zin = null;
				FileOutputStream fos = null;
				byte[] buffer = new byte[10 * 1024];
				try {
                    fos = new FileOutputStream(outputFile);
                    if(Build.VERSION.SDK_INT<Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        Field[] fields = Class.forName(getActivity().getPackageName()+".R$raw").getDeclaredFields();
                        System.out.println("has " + fields.length + " fields for copying");
                        List<String> names = new ArrayList<String>();
                        for(Field f : fields) {
                            if(f.getName().startsWith("database_")) {
                                System.out.println(f.getName());
                                names.add(f.getName());
                            }
                        }

                        for(String s : names) {
                            int res = getResources().getIdentifier(s,"raw",getActivity().getPackageName());
                            System.out.println("res id i s " + res);
                            in = ctx.getResources().openRawResource(res);
                            int read;
                            while((read = in.read(buffer))!=-1) {
                                fos.write(buffer,0,read);
                                System.out.println("writing ");
                            }
                            in.close();;
                        }
                    } else {
                        in = ctx.getResources().openRawResource(ctx.getResources().getIdentifier("database_db","raw",ctx.getPackageName()));
                        zin = new ZipInputStream(in);
                        int read;
                        ZipEntry ent = zin.getNextEntry();
                        System.out.println(ent.getSize());
                        while ((read = zin.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    }
					saveVersion(ctx, isExternal, getVersionCode(ctx));
					success = true;
				} catch (Exception e) {
                    Log.e(TAG,
                            "Unable to copy database to file system", e);
                } finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							Log.e(TAG,
									"Unable to close output file", e);
						}
					}
					if (zin != null) {
						try {
							zin.closeEntry();
							zin.close();
						} catch (IOException e) {
							Log.e(TAG,
									"Unable to close input file", e);
						}
					}
				}
				if(success) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							listener.onLoaded();	
						}
					});
				}
			}
		});

	}

	private void removeDatabase(Context ctx) {
		if (getActivity().deleteFile("database.db")) {
			removeInstalledVersion(getActivity());
		}
		if (isMediaMounted()) {
			File file = getExternalFile(ctx,"database.db");
			if (file.delete()) {
				removeInstalledExternalVersion(getActivity());
			}
		}
	}

	public static File getFile(Context ctx, String name) {
		if(isExternal(ctx)) {
			return getExternalFile(ctx,name);
		}
		return getInternalFile(ctx,name);
	}
	
	private static File getInternalFile(Context ctx, String name) {
		return new File(ctx.getFilesDir(),
				"database.db");
	}
	
	private static File getExternalFile(Context ctx, String name) {
		return new File(Environment.getExternalStorageDirectory(),
				"Android/data/" + ctx.getPackageName()
						+ "/databases/" + name);
	}

	private boolean isMediaMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	public static boolean isUpdated(Context ctx) {
		int versionCode = getVersionCode(ctx);

		return versionCode == getInstalledVersion(ctx);
	}

	private static int getVersionCode(Context ctx) {
		PackageManager pm = ctx.getPackageManager();

		PackageInfo info = null;

		try {
			info = pm.getPackageInfo(ctx.getPackageName(), 0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		int versionCode = info.versionCode;
		return versionCode;
	}

	private static void removeInstalledVersion(Context ctx) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit()
				.remove("database_internal_version").commit();
	}

	private static void removeInstalledExternalVersion(Context ctx) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit()
				.remove("database_external_version").commit();
	}

	private static boolean isExternal(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
				"database_external", false);
	}

	private static void saveVersion(Context ctx, boolean external,
			int versionCode) {
		final String key;
		if (external) {
			key = "database_external_version";
		} else {
			key = "database_version";
		}
		PreferenceManager.getDefaultSharedPreferences(ctx).edit()
				.putBoolean("database_external", external)
				.putInt(key, versionCode).commit();
	}

	private static int getInstalledVersion(Context ctx) {
		final String key;
		if (isExternal(ctx)) {
			key = "database_external_version";
		} else {
			key = "database_version";
		}
		return PreferenceManager.getDefaultSharedPreferences(ctx)
				.getInt(key, 0);
	}

}
