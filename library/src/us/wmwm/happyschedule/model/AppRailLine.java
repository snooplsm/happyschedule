package us.wmwm.happyschedule.model;

import org.json.JSONObject;

public class AppRailLine implements Comparable<AppRailLine>{

	public AppRailLine() {}
	
	private String name;
	private String key;
	
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
	}

	@Override
	public int compareTo(AppRailLine another) {
		return name.compareTo(another.name);
	}
	
}
