package us.wmwm.happytap.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.internal.org.json.JSONObject;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class ChatServlet extends HttpServlet {
	
	MongoClient client;
	DB db;
	Gson gson;
	String apiKey;
	
	public ChatServlet(MongoClient client, String apiKey) {
		this.client = client;
		this.apiKey = apiKey;
		db = client.getDB("rails");
		gson = new Gson();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req,resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String push_id = req.getParameter("push_id");
		Error error = new Error();		
		String messageJson = req.getParameter("message");
		Message message = null;
		try {
			if(messageJson==null) {
				error.missingFields.add("message");				
			}
			message = gson.fromJson(messageJson, Message.class);
			if(message.type==null) {
				error.missingFields.add("message.type");
			}
			if(message.name==null) {
				error.missingFields.add("message.name");
			}
			if(!error.missingFields.isEmpty()) {
				message = null;
			}
		} catch (Exception e) {
			error.message = e.getMessage();
		}
		
		if(push_id == null || message==null) {
			error.missingFields.add("push_id");
			error.code = HttpServletResponse.SC_BAD_REQUEST;
			sendError(resp, error);
			return;
		}		
		DBObject user = db.getCollection("chat_users").findOne(new BasicDBObject("push_id", req.getParameter("push_id")));
		if(user==null) {
			user = new BasicDBObject();
			user.put("push_id", push_id);
			user.put("created", new Date());	
		}		
		if(message.facebook!=null) {
			user.put("facebook", message.facebook);
		}

		user.put("updated", new Date());
		db.getCollection("chat_users").save(user);		
		if("join".equals(message.type)) {
			List<User> users = getLatestUsers();
			Response response = new Response();
			response.self = getUser(user);
			response.data = users;
			response.code = 200;
			resp.setStatus(200);
			resp.getWriter().write(gson.toJson(response));
		} if("message".equals(message.type)) {
			int count = new SendGcm(db, apiKey, message, gson).send();
			Response response = new Response();
			response.data = count;
			response.code = 200;
			resp.setStatus(200);
			resp.getWriter().write(gson.toJson(response));
		}
		else {
			
		}
	}
	
	private List<User> getLatestUsers() {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR_OF_DAY, -1);
		BasicDBObject o = new BasicDBObject();
//		BasicDBObject o = new BasicDBObject("updated", new BasicDBObject("$gte", now.getTime()));
		//BasicDBObject projection = new BasicDBObject("facebook.first_name",true).append("name", true);
		DBCursor cursor = db.getCollection("chat_users").find();
		List<User> users = new ArrayList<User>();
		while(cursor.hasNext()) {
			DBObject ob = cursor.next();
			User user = getUser(ob);
			users.add(user);
		}
		cursor.close();
		return users;
	}
	
	private User getUser(DBObject ob) {
		User user = new User();
		user.id = ob.get("_id").toString();
		DBObject facebook = (DBObject) ob.get("facebook");
		if(facebook!=null) {
			user.name = facebook.get("first_name") + " " + facebook.get("last_name");
			user.facebookId = (String)facebook.get("id");
		} else {
			user.name = (String) ob.get("name");
		}
		return user;
	}

	private void sendError(HttpServletResponse resp, Error error) throws IOException {
		resp.setStatus(error.code);
		resp.getWriter().write(error.toJSON());
	}

	public class Error {
		String message;
		List<String> missingFields = new ArrayList<String>();
		int code;
		
		public String toJSON() {
			try {
				JSONObject o = new JSONObject();
				o.put("message", message);
				if(missingFields.size()>0) {
					o.put("missing_fields", missingFields);
				}
				o.put("code", code);
				return o.toString();
			} catch (Exception e) {
				
			}
			return null;
		}
	}
	
}
