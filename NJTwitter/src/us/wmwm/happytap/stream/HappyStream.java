package us.wmwm.happytap.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class HappyStream {

	private static DB db;

	private static String apiKey;

	private static String screenname;

	public static void main(String[] args) {
		MongoClient client;
		try {
			client = new MongoClient();
		} catch (UnknownHostException e1) {
			throw new RuntimeException(e1);
		}
		db = client.getDB("njrails");
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setGZIPEnabled(true);
		cb.setJSONStoreEnabled(true);
		TwitterStreamFactory f = new TwitterStreamFactory(cb.build());
		TwitterStream twitterStream = f.getInstance();
		twitterStream.setOAuthConsumer(args[0], args[1]);
		twitterStream.setOAuthAccessToken(new AccessToken(args[2], args[3]));
		apiKey = args[4];
		screenname = args[6];
		UserStreamListener l = new UserStreamListener() {

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {

			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {

			}

			@Override
			public void onStallWarning(StallWarning arg0) {

			}

			@Override
			public void onStatus(Status status) {
				synchronized (this) {
					System.out.println(status.getUser().getName() + " : "
							+ status.getText());
					File file = new File("njtransit.json");
					JSONArray a = null;
					if (file.exists()) {
						FileInputStream fin = null;
						try {
							fin = new FileInputStream(file);
							String data = Streams.readFully(fin);
							a = new JSONArray(data);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (fin != null) {
								try {
									fin.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					} else {
						a = new JSONArray();
					}
					if (a.length() > 3) {
						JSONArray b = new JSONArray();
						int j = 0;
						for (int i = 1; i < a.length(); i++) {
							try {
								b.put(i - 1, a.getJSONObject(i));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						a = b;
					}
					try {
						a.put(new JSONObject(DataObjectFactory
								.getRawJSON(status)));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(file);
						fos.write(a.toString().getBytes());
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							fos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					saveStatus(status);
					try {
						int result = 0;
						boolean hasMore = true;
						Long lastUserId = 0L;
						while (hasMore) {
							processStatus(status);
							hasMore = lastUserId != 0;
						}
						System.out.println("DONE");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {

			}

			@Override
			public void onException(Exception arg0) {

			}

			@Override
			public void onBlock(User arg0, User arg1) {

			}

			@Override
			public void onDeletionNotice(long arg0, long arg1) {

			}

			@Override
			public void onDirectMessage(DirectMessage arg0) {

			}

			@Override
			public void onFavorite(User arg0, User arg1, Status arg2) {

			}

			@Override
			public void onFollow(User arg0, User arg1) {

			}

			@Override
			public void onFriendList(long[] arg0) {

			}

			@Override
			public void onUnblock(User arg0, User arg1) {

			}

			@Override
			public void onUnfavorite(User arg0, User arg1, Status arg2) {

			}

			@Override
			public void onUserListCreation(User arg0, UserList arg1) {

			}

			@Override
			public void onUserListDeletion(User arg0, UserList arg1) {

			}

			@Override
			public void onUserListMemberAddition(User arg0, User arg1,
					UserList arg2) {

			}

			@Override
			public void onUserListMemberDeletion(User arg0, User arg1,
					UserList arg2) {

			}

			@Override
			public void onUserListSubscription(User arg0, User arg1,
					UserList arg2) {

			}

			@Override
			public void onUserListUnsubscription(User arg0, User arg1,
					UserList arg2) {

			}

			@Override
			public void onUserListUpdate(User arg0, UserList arg1) {

			}

			@Override
			public void onUserProfileUpdate(User arg0) {

			}

		};
		twitterStream.addListener(l);
		StatusListener s = new StatusListener() {

			@Override
			public void onException(Exception arg0) {

			}

			@Override
			public void onTrackLimitationNotice(int arg0) {

			}

			@Override
			public void onStatus(Status arg0) {

			}

			@Override
			public void onStallWarning(StallWarning arg0) {

			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {

			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {

			}
		};
		twitterStream.addListener(s);
		// sample() method internally creates a thread which manipulates
		// TwitterStream and calls these adequate listener methods continuously.
		twitterStream.user();
	}

	public static void saveStatus(Status status) {

	}

	private static boolean checkSize(JSONObject data) {
		if (data.toString().getBytes().length > 4096) {
			return false;
		}
		return true;
	}

	public static DBCursor processStatus(Status status) throws Exception {
		HttpURLConnection conn = null;
		InputStream in = null;
		DBCursor users = null;
		try {
			if (status.getUser().getScreenName().equalsIgnoreCase(screenname)) {
				users = findAllUsers(status);
			} else {
				Calendar cal = Calendar.getInstance();
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int day = cal.get(Calendar.DAY_OF_WEEK);
				users = findUsersForService(status, day, hour);
			}

			JSONArray regs = new JSONArray();
			JSONObject fields = new JSONObject();
			fields.put("time_to_live", 1800);
			JSONObject data = new JSONObject();
			data.put("title", status.getUser().getName());
			StringBuilder text = new StringBuilder(status.getText());
			if (status.getURLEntities() != null) {
				for (int i = status.getURLEntities().length - 1; i >= 0; i--) {
					URLEntity e = status.getURLEntities()[i];
					text.replace(e.getStart(), e.getEnd(), e.getDisplayURL());
				}
			}
			JSONObject tweet = new JSONObject(
					DataObjectFactory.getRawJSON(status));
			data.put("tweet", tweet);
			data.put("message", text);
			data.put("type", "alert");
			if (!checkSize(data)) {
				tweet.remove("source");
				tweet.remove("lang");
				tweet.remove("truncated");
				tweet.remove("possibly_sensitive");
				tweet.remove("favorited");
				tweet.remove("filter_level");
				if (!checkSize(data)) {
					JSONObject user = tweet.getJSONObject("user");
					user.remove("default_profile");
					user.remove("verified");
					user.remove("contributors_enabled");
					user.remove("profile_image_url_https");
					user.remove("follower_request_sent");
					user.remove("is_translator");
					if (!checkSize(data)) {
						user.remove("description");
					}

				}
			}
			if (!checkSize(data)) {
				data.remove("tweet");
			}
			fields.put("data", data);
			// fields.put("dry_run", Boolean.TRUE);
			Boolean dryRun = Boolean.getBoolean("dry_run");
			if (Boolean.FALSE.equals(dryRun)) {
				if (status.getText().startsWith("dry")
						|| status.getText().startsWith("test")) {
					dryRun = Boolean.TRUE;
				}
			}
			fields.put("dry_run", dryRun);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "key=" + apiKey);
			headers.put("Content-Type", "application/json");
			regs = new JSONArray();
			while (users.hasNext()) {
				DBObject user = users.next();
				String pushId = (String) user.get("push_id");
				regs.put(pushId);
				if (regs.length() == 0) {
					return users;
				}
				fields.put("registration_ids", regs);
				URL u = new URL("https://android.googleapis.com/gcm/send");
				conn = (HttpURLConnection) u.openConnection();
				for (Map.Entry<String, String> e : headers.entrySet()) {
					conn.setRequestProperty(e.getKey(), e.getValue());
				}
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				OutputStream out = conn.getOutputStream();
				out.write(fields.toString().getBytes());
				out.close();
				int code = conn.getResponseCode();

				if (code == 200) {
					in = conn.getInputStream();
					String response = Streams.readFully(in);
					JSONObject o = new JSONObject(response);
					JSONArray a = o.getJSONArray("results");
					List<String> successfuls = new ArrayList<String>();
					List<String> notRegistered = new ArrayList<String>();
					Map<String, String> replace = new HashMap<String, String>();
					StringBuilder sb = new StringBuilder("Successful: (");
					StringBuilder nb = new StringBuilder("Not Registered: (");
					StringBuilder replaceb = new StringBuilder(
							"Need to replace: (");
					for (int i = 0; i < a.length(); i++) {
						JSONObject ob = a.getJSONObject(i);
						// System.out.println(ob);
						boolean success = !ob.has("error");
						if (success) {
							successfuls.add(regs.getString(i));
							sb.append(regs.getString(i)).append(",");
							if (ob.has("registration_id")) {
								replaceb.append(regs.getString(i))
										.append(":")
										.append(ob.getString("registration_id"))
										.append(",");
								replace.put(regs.getString(i),
										ob.getString("registration_id"));
							}
						} else {
							if ("NotRegistered".equals(ob.opt("error"))) {
								notRegistered.add(regs.getString(i));
								nb.append(regs.getString(i)).append(",");
							}
						}
						// if(ob.has("registration_id"))
					}
					deletePushIds(HappyStream.db, notRegistered);
					if (!replace.isEmpty()) {
						fixPushIds(HappyStream.db, replace);
					}
				} else {
					in = conn.getErrorStream();
					System.err.println(Streams.readFully(in));
				}
			}
			return users;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
			if (users != null) {
				users.close();
			}
		}
		return users;
	}

	/**
	 * replace string key is to be updated with the value.
	 * 
	 * @param db
	 * @param replace
	 * @throws Exception
	 */
	private static void fixPushIds(DB db, Map<String, String> replace)
			throws Exception {
		DBCollection users = db.getCollection("users");
		for (Map.Entry<String, String> entry : replace.entrySet()) {
			DBObject original = users.findOne(new BasicDBObject("push_id",
					entry.getKey()));
			DBObject newUser = users.findOne(new BasicDBObject("push_id", entry
					.getValue()));
			if (newUser != null && original != null) {
				users.remove(original);
			} else {
				Map k = original.toMap();
				k.put("push_id", entry.getValue());
				k.put("_id", entry.getValue());
				BasicDBObject replacement = new BasicDBObject(k);
				users.save(replacement);
				users.remove(original);
			}
		}

	}

	public static void deletePushIds(DB conn, List<String> pushIds)
			throws Exception {
		BasicDBList userList = new BasicDBList();
		userList.addAll(pushIds);
		WriteResult res = conn.getCollection("users").remove(
				new BasicDBObject("push_id", userList));
	}

	public static DBCursor findUsersForService(Status status, int day, int hour)
			throws Exception {
		BasicDBObject query = new BasicDBObject("service.screenname", status
				.getUser().getScreenName()).append("service.day", day).append(
				"service.hour", hour);
		return db.getCollection("users").find(query);
	}

	public static DBCursor findAllUsers(Status status) throws Exception {
		BasicDBObject fields = new BasicDBObject("services", 0);
		return db.getCollection("users").find(new BasicDBObject(), fields);
	}

	public static ResultSet findAllUsers(Connection conn, Long afterUserId,
			int limit) throws Exception {
		PreparedStatement stat = conn
				.prepareStatement(String
						.format("select u.push_id, u.id from USER u where u.id > ? group by u.id order by u.id asc limit %s",
								limit));
		if (afterUserId == null) {
			afterUserId = 0L;
		}
		stat.setLong(1, afterUserId);
		stat.execute();
		return stat.getResultSet();
	}

}
