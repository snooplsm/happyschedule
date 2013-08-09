package us.wmwm.happyschedule.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.R.id;
import us.wmwm.happyschedule.R.layout;
import us.wmwm.happyschedule.R.raw;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class FragmentLoad extends Fragment {

	Future<?> loadFuture;
	
	ImageView logo; 
	
	Handler handler = new Handler();
	
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
		logo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
					outputFile = getExternalFile(ctx);
					outputFile.getParentFile().mkdirs();
					isExternal = true;
				} else {
					outputFile = getInternalFile(ctx);
				}

				InputStream in = null;
				ZipInputStream zin = null;
				FileOutputStream fos = null;
				byte[] buffer = new byte[10 * 1024];
				try {
					in = ctx.getResources().openRawResource(R.raw.database_db);
					zin = new ZipInputStream(in);
					fos = new FileOutputStream(outputFile);
					int read;
					ZipEntry ent = zin.getNextEntry();
					System.out.println(ent.getSize());
					while ((read = zin.read(buffer)) != -1) {
						fos.write(buffer, 0, read);
					}					
					saveVersion(ctx, isExternal, getVersionCode(ctx));
					success = true;
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(),
							"Unable to copy database to file system", e);
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							Log.e(getClass().getSimpleName(),
									"Unable to close output file", e);
						}
					}
					if (zin != null) {
						try {
							zin.closeEntry();
							zin.close();
						} catch (IOException e) {
							Log.e(getClass().getSimpleName(),
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
			File file = getExternalFile(ctx);
			if (file.delete()) {
				removeInstalledExternalVersion(getActivity());
			}
		}
	}

	public static File getFile(Context ctx) {
		if(isExternal(ctx)) {
			return getExternalFile(ctx);
		}
		return getInternalFile(ctx);
	}
	
	private static File getInternalFile(Context ctx) {
		return new File(ctx.getFilesDir(),
				"database.db");
	}
	
	private static File getExternalFile(Context ctx) {
		return new File(Environment.getExternalStorageDirectory(),
				"Android/data/" + ctx.getPackageName()
						+ "/databases/database.db");
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
