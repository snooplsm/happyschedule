package us.wmwm.happytap.stream;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

import com.google.gson.Gson;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SendMessage implements Runnable {

	DBCursor cursor;
	
	String apiKey;
	
	Message message;
	
	Gson gson;
	
	public SendMessage(DBCursor cursor, Gson gson, String apiKey, Message message) {
		this.cursor = cursor;
		this.apiKey = apiKey;
		this.message = message;
		this.gson = gson;
	}
	
	@Override
	public void run() {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "key=" + apiKey);
			headers.put("Content-Type", "application/json");
			URL u = new URL("https://android.googleapis.com/gcm/send");
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			for (Map.Entry<String, String> e : headers.entrySet()) {
				conn.setRequestProperty(e.getKey(), e.getValue());
			}
			JSONObject data = new JSONObject();
			JSONArray registrationids = new JSONArray();
			JSONObject fields = new JSONObject();
			fields.put("time_to_live", 1800);
			data.put("type", "chat_message");
			Message m = new Message();
			m.text = message.text;
			m.facebookId = message.facebookId;
			data.put("message", gson.toJson(m));
			while(cursor.hasNext()) {
				DBObject user = cursor.next();
				registrationids.put(user.get("push_id"));
			}
			fields.put("registration_ids", registrationids);
			fields.put("data", data);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			OutputStream out = conn.getOutputStream();
			out.write(fields.toString().getBytes());
			out.close();
			int code = conn.getResponseCode();
			if(code==401) {
				System.out.println(Streams.readFully(conn.getErrorStream()));
			}
			System.out.println(code);
			System.out.println(Streams.readFully(conn.getInputStream()));
		} catch (Exception e) {
			
		}		
	}

}
