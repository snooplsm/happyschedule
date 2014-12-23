package us.wmwm.happyschedule.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.model.GraphUser;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.fragment.ChatFragment;
import us.wmwm.happyschedule.fragment.SettingsFragment;
import us.wmwm.happyschedule.model.AppConfig;
import us.wmwm.happyschedule.model.AppRailLine;
import us.wmwm.happyschedule.model.RailPushMatrix;
import us.wmwm.happyschedule.model.Schedule;
import us.wmwm.happyschedule.model.StationInterval;
import us.wmwm.happyschedule.model.StationToStation;
import us.wmwm.happyschedule.util.Streams;

/**
 * Created by gravener on 6/15/14.
 */
public class Api extends BaseApi {

    private static final String TAG = Api.class.getSimpleName();

    public Api(Context ctx) {
        super(ctx);
    }

    public void updateGcm() {
        String version = SettingsFragment.getRegistrationId();
        if (!TextUtils.isEmpty(version)) {
            return;
        }
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
        try {
            String id = gcm.register(ctx.getString(R.string.sender_id));
            Log.d(Api.class.getSimpleName(), "GCM IS: " + id);
            OkHttpClient client = new OkHttpClient();
            URL u = new URL(ctx.getString(R.string.register_push_url) + "?push_id=" + id + "&os=android&v=" + SettingsFragment.getAppVersion() + "&m=" + ctx.getString(R.string.market) + "&p=" + ctx.getPackageName());
            HttpURLConnection conn = client.open(u);
            if (conn.getResponseCode() == 200) {
                SettingsFragment.saveRegistrationId(id);
            }
            conn.disconnect();

        } catch (Exception e) {
            Log.d(TAG, "can't register", e);
        }
    }

    public int registerService(String data, String pushId) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = post(map("services", data), map(), ctx.getString(R.string.register_service_url) + "?push_id=" + pushId);
            int code = conn.getResponseCode();
            Streams.readFully(conn.getInputStream());
            return code;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public int registerFacebookUser(GraphUser u, String pushId) {
        HttpURLConnection conn = null;
        try {
            conn = post(map("user", u.getInnerJSONObject().toString()), map(), ctx.getString(R.string.register_facebook_url) + "?push_id=" + pushId + "&id=" + u.getId());
            int code = conn.getResponseCode();
            Streams.readFully(conn.getInputStream());
            return code;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    static SimpleDateFormat RFC;

    static {
        RFC = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        RFC.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void updateLines() {
        Log.d(TAG, "UpdateScheduleThread");
        HttpURLConnection conn = null;
        File config = ctx.getFileStreamPath("config.json");
        FileOutputStream fos = null;
        try {
            String version = "";
            try {
                version = String.valueOf(SettingsFragment.getAppVersion());
            } catch (Exception e) {

            }
            Calendar c = null;
            if (config.exists()) {
                Log.d(TAG, "UpdateScheduleThread - config exists");
                c = Calendar.getInstance();
                String lastModded = WDb.get().getPreference("config_last_modified");
                if (lastModded != null) {
                    try {
                        c.setTimeInMillis(Long.parseLong(lastModded));
                        Log.d(TAG, "UpdateScheduleThread - time set to " + c.getTime());
                    } catch (Exception e) {
                        Log.e(TAG, "UpdateScheduleThreadException", e);
                    }
                } else {
                    c.setTimeInMillis(0);
                    Log.d(TAG, "UpdateScheduleThread - time set to " + c.getTime());
                }
                Calendar later = (Calendar) c.clone();
                later.add(Calendar.HOUR_OF_DAY, 24);
                if (!Calendar.getInstance().after(later)) {
                    Log.d(TAG, "UpdateScheduleThread - returning since " + Calendar.getInstance().getTime() + "  ! after " + later.getTime());
                    return;
                }
            }
            Map<String, String> headers = new HashMap<String, String>();
            if (c != null) {
                Log.d(TAG, "UpdateScheduleThread - adding If-Modified-Since " + RFC.format(c.getTime()));
                headers.put("If-Modified-Since", RFC.format(c.getTime()));
            }
            conn = get(map("v", version), headers, ctx.getString(R.string.config_url));


            if (conn.getResponseCode() == 200) {
                Log.d(TAG, "UpdateScheduleThread - 200 response ");
                String txt = Streams.readFully(conn.getInputStream());
                JSONObject t = new JSONObject(txt);
                if (txt != null) {
                    fos = ctx.openFileOutput("config.json", Context.MODE_PRIVATE);
                    fos.write(txt.getBytes());
                    Log.d(TAG, "UpdateScheduleThread - saved config");
                    FlurryAgent.logEvent("ConfigUpdated", Collections.singletonMap("date", new Date().toString()));
                    WDb.get().savePreference("config_last_modified", String.valueOf(System.currentTimeMillis()));
                }
            } else {
                FlurryAgent.logEvent("ConfigUpToDate", Collections.singletonMap("date", new Date().toString()));
                //Streams.readFully(in = conn.getEr);
                Log.d(TAG, "UpdateScheduleThread - config up to date with response code " + conn.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "could not get config", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {

                }
            }
        }
    }

    Gson gson = new Gson();

//    public void leaveChat(GraphUser user) {
//        String pushId = SettingsFragment.getRegistrationId();
//        if (TextUtils.isEmpty(pushId)) {
//            Log.d(TAG, "no pushid trip notifications");
//            throw new ApiException(500);
//        }
//        Map<String, String> data = new HashMap<String, String>();
//        JSONObject text = new JSONObject();
//        try {
//            text.put("type", "leave");
//            text.put("name", user.getName());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        data.put("text", text.toString());
//        data.put("push_id",pushId);
//        HttpURLConnection conn = null;
//        try {
//            conn = post(data,map(),"http://192.168.0.12:8080/rails/chat");
//            int respCode = conn.getResponseCode();
//            if(respCode!=200) {
//                throw new ApiException(respCode);
//            }
//            JoinResponse resp = gson.fromJson(new InputStreamReader(conn.getInputStream()),JoinResponse.class);
//            return resp;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        finally {
//            if(conn!=null) {
//                conn.disconnect();
//            }
//        }
//
//    }

    public JoinResponse joinChat(GraphUser user) {
        String pushId = SettingsFragment.getRegistrationId();
        if (TextUtils.isEmpty(pushId)) {
            Log.d(TAG, "no pushid trip notifications");
            throw new ApiException(500);
        }
        Map<String, String> data = new HashMap<String, String>();
        JSONObject message = new JSONObject();
        try {
            message.put("type", "join");
            message.put("name", user.getName());
            message.put("facebookId", user.getId());
            message.put("facebook", user.getInnerJSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        data.put("message", message.toString());
        data.put("push_id",pushId);
        Collection<String> all = SettingsFragment.getAllRegistrationIds().values();
        JSONArray allIds = new JSONArray();
        for(String s : all) {
            if(s.equals(pushId)) {
                continue;
            }
            allIds.put(s);
        }
        data.put("all_push_ids",allIds.toString());
        HttpURLConnection conn = null;
        try {
            conn = post(data,map(),ctx.getString(R.string.chat_url));
            int respCode = conn.getResponseCode();
            if(respCode!=200) {
                throw new ApiException(respCode);
            }
            JoinResponse resp = gson.fromJson(new InputStreamReader(conn.getInputStream()),JoinResponse.class);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if(conn!=null) {
                conn.disconnect();
            }
        }

        //post(text)
    }

    public MessageResponse sendMessage(ChatFragment.Message m) {
        String pushId = SettingsFragment.getRegistrationId();
        if (TextUtils.isEmpty(pushId)) {
            Log.d(TAG, "no pushid trip notifications");
            throw new ApiException(500);
        }
        Map<String, String> data = new HashMap<String, String>();
        JSONObject message = new JSONObject();
        try {
            message.put("type", "message");
            message.put("text",m.text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        data.put("message", message.toString());
        data.put("push_id",pushId);
        HttpURLConnection conn = null;
        try {
            conn = post(data,map(),ctx.getString(R.string.chat_url));
            int respCode = conn.getResponseCode();
            if(respCode!=200) {
                throw new ApiException(respCode);
            }
            MessageResponse resp = gson.fromJson(new InputStreamReader(conn.getInputStream()),MessageResponse.class);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }

    public int registerForTripNotifications(AppConfig appConfig, List<StationToStation> stationToStations, Schedule schedule) throws Exception {
        Log.d(TAG, "registering for trip notifications");
        String pushId = SettingsFragment.getRegistrationId();
        if (TextUtils.isEmpty(pushId)) {
            Log.d(TAG, "no pushid trip notifications");
            return 500;
        }
        RailPushMatrix m = new RailPushMatrix();

        Map<String, AppRailLine> lines = new HashMap<String, AppRailLine>();
        for (AppRailLine line : appConfig.getRailLines()) {
            for (String s : line.getRouteIds()) {
                lines.put(s, line);
            }
        }
        Calendar cal = Calendar.getInstance();
        Set<String> routeIds = new HashSet<String>();
        for (StationToStation sts : stationToStations) {
            routeIds.add(schedule.tripIdToRouteId.get(sts.tripId));
            if (sts instanceof StationInterval) {
                StationInterval si = (StationInterval) sts;
                while (si.hasNext()) {
                    StationInterval next = si.next();
                    routeIds.add(schedule.tripIdToRouteId.get(next.tripId));
                }
            }
        }
        for (String routeId : routeIds) {
            AppRailLine line = lines.get(routeId);
            if (line != null) {
                m.update(line, cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.HOUR_OF_DAY), true);
                cal.add(Calendar.HOUR_OF_DAY, 1);
                m.update(line, cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.HOUR_OF_DAY), true);
                cal.add(Calendar.HOUR_OF_DAY, -1);
            }

        }
        AppRailLine line = new AppRailLine();
        line.setKey(ctx.getResources().getString(R.string.promotional_account));
        m.update(line, cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.HOUR_OF_DAY), true);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        m.update(line, cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.HOUR_OF_DAY), true);
        cal.add(Calendar.HOUR_OF_DAY, -1);
        HttpURLConnection conn = null;
        try {
            conn = post(map("dynamic_services", m.toJSON().toString()), map(), ctx.getString(R.string.register_dynamic_service_url) + "?push_id=" + pushId);
            int status = conn.getResponseCode();
            Streams.readFully(conn.getInputStream());
            Log.d(TAG, "registered for trip notifications " + m.toJSON().toString());
            return status;
        } catch (Exception e) {
            Log.e(TAG, "error registering for trip notifications", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }



}
