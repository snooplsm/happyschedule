package us.wmwm.happyschedule.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class AppRailLine implements Comparable<AppRailLine>{

	public AppRailLine() {}
	
	private String name;
	private String key;
    private Set<String> routeIds = new HashSet<String>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public AppRailLine(JSONObject o) {
		name = o.optString("name");
		key = o.optString("screenname");
        JSONArray routeIds = o.optJSONArray("route_ids");
        if(routeIds!=null) {
            this.routeIds = new HashSet<String>();
            for(int i = 0; i < routeIds.length(); i++) {
                this.routeIds.add(routeIds.optString(i));
            }
        }
	}

	@Override
	public int compareTo(AppRailLine another) {
		return name.compareTo(another.name);
	}

    public Set<String> getRouteIds() {
        return routeIds;
    }
}
