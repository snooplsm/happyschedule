package us.wmwm.happyschedule.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import us.wmwm.happyschedule.application.HappyApplication;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.Session;
import com.facebook.model.GraphUser;

public class PremiumUserHelper {

	public static boolean isPremiumUser() {
        //return true;
		return isPaidUser() || isFacebookUser() || isDontCareUser();
	}

	public static boolean isFacebookUser() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(HappyApplication.get());
		return prefs.contains("facebookUser");
	}

	public static boolean isPaidUser() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(HappyApplication.get());
		return prefs.getBoolean("isPaidUser", false);
	}

	public static boolean isDontCareUser() {
        return true;
//		SharedPreferences prefs = PreferenceManager
//				.getDefaultSharedPreferences(HappyApplication.get());
//		return prefs.getBoolean("isDontCareUser", false);
	}

	public static void setFacebookUser(GraphUser graphUser, Session session) {
		if (session != null && session.getAccessToken() != null) {
			JSONObject o = graphUser.getInnerJSONObject();
			try {
				o.put("accessToken", session.getAccessToken());
				o.put("accessTokenExpiration", session.getExpirationDate()
						.getTime());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(HappyApplication.get());
			prefs.edit().putString("facebookUser", o.toString())
			.putLong("facebookUserSaved", System.currentTimeMillis())
			.commit();
		}
	}

	public static long getFacebookUserSaved() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(HappyApplication.get());
		return prefs.getLong("facebookUserSaved", -1);
	}
	
	public static void setPaidUser(boolean b) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(HappyApplication.get());
		prefs.edit().putBoolean("isPaidUser", b).commit();
	}

	public static void setFacebookUserFriends(List<GraphUser> users,
			Session session) {
		// TODO Auto-generated method stub
		JSONArray friends =new JSONArray();
		for(GraphUser user : users) {
			friends.put(user.getInnerJSONObject());
		}
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(HappyApplication.get());
		try {
			JSONObject fbUser = new JSONObject(prefs.getString("facebookUser", "{}"));
			fbUser.put("friends", friends);
			prefs.edit().putString("facebookUser", friends.toString()).commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
	}

}
