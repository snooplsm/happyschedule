package us.wmwm.happytap.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.internal.org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
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
		String facebookJson = req.getParameter("facebook");
		if(facebookJson!=null) {
			try {
				Map<String,Object> facebookMap = gson.fromJson(facebookJson, new TypeToken<HashMap<String, Object>>() {}.getType());
				user.put("facebook", facebookMap);
			} catch (Exception e) {
				error.message = e.getMessage();
				sendError(resp,error);
				return;
			}
		}
		user.put("updated", new Date());
		db.getCollection("chat_users").save(user);
		
		new SendGcm(db, apiKey, message, gson).send();
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
