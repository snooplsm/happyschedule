package us.wmwm.happytap.stream;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class SendMessage implements Runnable {

	DBCursor cursor;
	
	String apiKey;
	
	Message message;
	
	Gson gson;
	
	DBCollection collection;
	
	public SendMessage(DBCollection collection, DBCursor cursor, Gson gson, String apiKey, Message message) {
		this.collection = collection;
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
			List<String> userId = new ArrayList<String>();
			JSONObject fields = new JSONObject();
			fields.put("time_to_live", 1800);
			data.put("type", "chat_message");
			Message m = new Message();
			m.text = message.text;
			m.id = message.id;
			m.name = message.name;
			data.put("message", gson.toJson(m).toString());
			while(cursor.hasNext()) {
				DBObject user = cursor.next();
				registrationids.put(user.get("push_id"));
				userId.add(user.get("_id").toString());
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
			JSONObject response = new JSONObject(Streams.readFully(conn.getInputStream()));
			System.out.println(response);
			List<ObjectId> deleteIds = new ArrayList<ObjectId>();
			JSONArray results = response.getJSONArray("results");
			for(int i = 0; i < results.length(); i++) {
				JSONObject item = results.getJSONObject(i);
				if(item.has("error") && item.getString("error").equals("InvalidRegistration")) {
					deleteIds.add(new ObjectId(userId.get(i)));
				} else
				if(item.has("registration_id")) {
					deleteIds.add(new ObjectId(userId.get(i)));
				}
			}
			BasicDBObject doc = new BasicDBObject();
			doc.put("_id", new BasicDBObject("$in", deleteIds));
			WriteResult res = collection.remove(
					doc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

}
