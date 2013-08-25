package us.wmwm.happyschedule.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

public class AppAd implements Serializable {

	private static final long serialVersionUID = 1L;
	
	String url;
	String imageUrl;
	String webviewUrl;
	String text;
	String target="*";
	Calendar start;
	Calendar end;
	String alignment;
	String discardKey;
	String height;
	boolean closeable;
	boolean enabled;
	Integer beforeVersion;
	Integer afterVersion;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z");
	
	public AppAd() {}
	
	public String getUrl() {
		return url;
	}
	
	public String getHeight() {
		return height;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getWebviewUrl() {
		return webviewUrl;
	}

	public void setWebviewUrl(String webviewUrl) {
		this.webviewUrl = webviewUrl;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Calendar getStart() {
		return start;
	}

	public void setStart(Calendar start) {
		this.start = start;
	}

	public Calendar getEnd() {
		return end;
	}

	public void setEnd(Calendar end) {
		this.end = end;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public String getDiscardKey() {
		return discardKey;
	}

	public void setDiscardKey(String discardKey) {
		this.discardKey = discardKey;
	}
	
	public Integer getBeforeVersion() {
		return beforeVersion;
	}
	
	public void setBeforeVersion(Integer beforeVersion) {
		this.beforeVersion = beforeVersion;
	}

	public AppAd(JSONObject o) {
		url = o.optString("url");
		imageUrl = o.optString("imageUrl");
		webviewUrl = o.optString("webviewUrl");
		text = o.optString("text");
		target = o.optString("target","*");
		alignment = o.optString("alignment","top");
		discardKey = o.optString("discardKey");
		height = o.optString("height");
		closeable = o.optBoolean("closeable");
		enabled = o.optBoolean("enabled",true);
		if(o.has("beforeVersion")) {
			beforeVersion = o.optInt("beforeVersion");
		}
		if(o.has("afterVersion")) {
			afterVersion = o.optInt("afterVersion");
		}
		if(o.has("start")) {			
			Calendar start = Calendar.getInstance();
			try {
			if(o.get("start") instanceof String) {
				Date d = SDF.parse(o.getString("start"));
				start.setTime(d);
			} else {
				long time = o.getLong("start");
				start.setTimeInMillis(time);
			}
			} catch (Exception e) {
				
			}
			this.start = start;
		}
		if(o.has("end")) {
			Calendar end = Calendar.getInstance();
			try {
			if(o.get("end") instanceof String) {
				Date d = SDF.parse(o.getString("end"));
				start.setTime(d);
			} else {
				long time = o.getLong("end");
				end.setTimeInMillis(time);
			}
			} catch (Exception e) {
				
			}
			this.end = end;
		}
		
	}
	
	public Integer getAfterVersion() {
		return afterVersion;
	}
	
}
