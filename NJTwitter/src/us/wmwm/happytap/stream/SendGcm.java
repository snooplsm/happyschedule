package us.wmwm.happytap.stream;

import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.DBCursor;

public class SendGcm {

	private DB db;
	private Message message;
	private Gson gson;
	private String apiKey;
	
	
	public SendGcm(DB db, String apiKey, Message message, Gson gson) {
		this.db = db;
		this.apiKey = apiKey;
		this.message = message;
		this.gson = gson;						
	}
	
	public int send() {
		int count = (int) db.getCollection("chat_users").count();
		for(int i = 0; i < count; i+=1000) {
			DBCursor cursor = db.getCollection("chat_users").find().skip(i).limit(1000);
			ThreadHelper.getScheduler().submit(new SendMessage(db.getCollection("chat_users"),cursor,gson, apiKey,message));
		}
		return count;
	}
	
}
