package us.wmwm.happyschedule.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.model.DepartureVision;
import android.preference.PreferenceManager;

public class FavoriteHelper {

	
	public static List<DepartureVision> getFavoritesVisions() {
		String visioned = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).getString("favorites", "[]");
		try {
			JSONArray o = new JSONArray(visioned);
			List<DepartureVision> stations = new ArrayList<DepartureVision>(o.length());
			for(int i = 0; i < o.length(); i++) {
				DepartureVision v = new DepartureVision(o.optJSONObject(i));
				stations.add(v);
			}
			return stations;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void add(DepartureVision dv) {
		String visioned = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).getString("favorites", "[]");
		try {
			JSONArray o = new JSONArray(visioned);
			o.put(dv.getObject());
			PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).edit().putString("favorites", o.toString()).commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void set(int pos, DepartureVision dv) {
		try {
			List<DepartureVision> v = getFavoritesVisions();
			if(v.size()-1<pos) {
				v.add(dv);
			} else {
				v.set(pos, dv);
			}
			JSONArray a = new JSONArray();
			for(DepartureVision vis : v ) {
				a.put(vis.getObject());
			}
			PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).edit().putString("favorites", a.toString()).commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void add(int pos, DepartureVision dv) {
		try {
			List<DepartureVision> v = getFavoritesVisions();
			if(pos>=v.size()) {
				pos = v.size();
			} else {
				pos = pos+1;
			}
			v.add(pos, dv);
			JSONArray a = new JSONArray();
			for(DepartureVision vis : v ) {
				a.put(vis.getObject());
			}
			PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).edit().putString("favorites", a.toString()).commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void remove(DepartureVision dv) {
		String visioned = PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).getString("favorites", "[]");
		try {
			JSONArray o = new JSONArray(visioned);
			JSONArray nn = new JSONArray();
			boolean found = false;
			for(int i = 0; i < o.length(); i++) {
				DepartureVision v = new DepartureVision(o.optJSONObject(i));
				if(!found && v.equals(dv)) {
					found = true;
				} else {
					nn.put(o.optJSONObject(i));
				}
			}
			PreferenceManager.getDefaultSharedPreferences(HappyApplication.get()).edit().putString("favorites", nn.toString()).commit();
		}catch (Exception e) {
			
		}
	}

    public static boolean hasFavorite(DepartureVision departure) {
        List<DepartureVision> favs = getFavoritesVisions();
        for(DepartureVision dv : favs) {
            if(dv.getFrom().equals(departure.getFrom()) && dv.getTo().equals(departure.getTo())) {
                return true;
            }
        }
        return false;
    }
}
