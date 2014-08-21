package us.wmwm.happyschedule.api;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import us.wmwm.happyschedule.util.Streams;

/**
* Created by gravener on 6/15/14.
*/
public class BaseApi {

    Context ctx;

    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    BroadcastReceiver mExternalStorageReceiver;

    protected BaseApiDbHelper helper;

    public BaseApi(Context ctx) {
        this.ctx = ctx;

        updateExternalStorageState();
        helper = new BaseApiDbHelper(ctx);
    }

    void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

    class BaseApiDbHelper extends SQLiteOpenHelper {
        public BaseApiDbHelper(Context context) {
            super(context, new File(getStorageDir(),"api_storage.db").getAbsolutePath(), null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table files(key text unique, value text, created integer)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public void set(String key, Object value) {
            ContentValues cv = new ContentValues();
            cv.put("key",key);
            cv.put("created", System.currentTimeMillis());
            cv.put("value",value.toString());
            getWritableDatabase().insertWithOnConflict("files",null,cv,SQLiteDatabase.CONFLICT_REPLACE);
        }


        public Calendar getCreated(String key) {
            Calendar cal = Calendar.getInstance();
            cal.clear();
            Cursor c = null;
            try {
                //getWritableDatabase().q
                c= getWritableDatabase().rawQuery("select created from files where key=?", new String[]{key});
                if(c.moveToNext()) {
                    cal.setTimeInMillis(c.getLong(0));
                    return cal;
                }
            } catch (Exception e) {

            } finally {
                if(c!=null) {
                    c.close();;
                }
            }
            return cal;
        };

        public String get(String key) {
            Cursor c = null;
            try {
                //getWritableDatabase().q
                c= getWritableDatabase().rawQuery("select value from files where key=?", new String[]{key});
                if(c.moveToNext()) {
                    return c.getString(0);
                }
            } catch (Exception e) {

            } finally {
                if(c!=null) {
                    c.close();;
                }
            }
            return null;
        }
    }


    void startWatchingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("test", "Storage: " + intent.getData());
                updateExternalStorageState();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        ctx.registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();
    }

    protected Map<String, String> map(Object... os) {
        Map<String, String> d = new HashMap<String, String>();
        for (int i = 0, j = 1; i < os.length; i += 2, j += 2) {
            if (os[i] == null || os[j] == null) {
                continue;
            }
            d.put(os[i].toString(), os[j].toString());
        }
        return d;
    };

    void stopWatchingExternalStorage() {
        ctx.unregisterReceiver(mExternalStorageReceiver);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopWatchingExternalStorage();
    }

    protected File getStorageDir() {
        if(mExternalStorageAvailable&&mExternalStorageWriteable) {
            File path = Environment.getExternalStorageDirectory();
            path = new File(path,String.format("Android/data/%s",ctx.getPackageName()));
            Log.d("BaseApi","getStorageDir() = " + path.getAbsolutePath());
            return path;
        }
        return ctx.getFilesDir();
    }

    protected HttpURLConnection post(Map<String, String> postContent,
                           Map<String, String> headers, String url) {
        return req("POST", postContent, headers, url);
    }

    protected HttpURLConnection get(Map<String, String> map,
                                  Map<String, String> map2, String format) {
        return req("GET", map, map2, format);
    }


    protected JSONObject consumeJsonObject(HttpURLConnection conn) {
        try {
            int status = conn.getResponseCode();
            if (status >= 200 && status <= 200) {
                JSONObject data = new JSONObject(Streams.readFully(conn
                        .getInputStream()));
                return data;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected HttpURLConnection req(String method,
                                  Map<String, String> postContent, Map<String, String> headers,
                                  String url) {
        URL u = null;
        try {
            if("GET".equals(method)) {
                Uri.Builder uri = Uri.parse(url).buildUpon();
                for(Map.Entry<String, String> entry : postContent.entrySet()) {
                    uri.appendQueryParameter(entry.getKey(), entry.getValue());
                }
                u = new URL(uri.build().toString());
            } else {
                u = new URL(url);
            }
        } catch (Exception e) {
            throw new RuntimeException("invalid url", e);
        }
        HttpURLConnection conn = null;
        OutputStream out = null;
        try {
            conn = (HttpURLConnection) u.openConnection();
        } catch (Exception e) {
            throw new RuntimeException("can't open connection, e");
        }

        try {
            conn.setRequestMethod(method);

            for (Iterator<Map.Entry<String, String>> iter = headers.entrySet()
                    .iterator(); iter.hasNext();) {
                Map.Entry<String, String> e = iter.next();
                try {
                    conn.setRequestProperty(
                            URLEncoder.encode(e.getKey(), "utf-8"),
                            URLEncoder.encode(e.getValue(), "utf-8"));
                } catch (UnsupportedEncodingException e1) {
                    throw new RuntimeException("utf-8 encoding exception", e1);
                }
            }
            conn.setDoInput(true);
            if ("POST".equals(method)) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                StringBuilder b = new StringBuilder();
                for (Iterator<Map.Entry<String, String>> iter = postContent
                        .entrySet().iterator(); iter.hasNext();) {
                    Map.Entry<String, String> e = iter.next();
                    try {
                        b.append(URLEncoder.encode(e.getKey(), "utf-8"))
                                .append("=")
                                .append(URLEncoder.encode(e.getValue(), "utf-8"));
                        if (iter.hasNext()) {
                            b.append("&");
                        }
                    } catch (UnsupportedEncodingException e1) {
                        throw new RuntimeException("utf-8 encoding exception",
                                e1);
                    }
                }
                byte[] data = b.toString().getBytes("utf-8");
                out = conn.getOutputStream();
                out.write(data, 0, data.length);
                out.close();
            }


        } catch (IOException e2) {
            throw new RuntimeException("Can't write data", e2);
        } finally {
            try {
                if(out!=null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }
}
