package us.wmwm.happytap.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;

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
        UserStreamListener userStreamListener = new UserStreamListener() {

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
                    saveStatus(status);
                    try {
                        int count = processStatus(status,ServiceType.DYNAMIC_SERVICES);
                        System.out.println("DONE DYNAMIC " + count);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        int count = processStatus(status,ServiceType.SERVICES);
                        System.out.println("DONE SERVICES " + count);
                    } catch (Exception e) {

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
		twitterStream.addListener(userStreamListener);
		StatusListener statusListener = new StatusListener() {

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
		twitterStream.addListener(statusListener);
		// sample() method internally creates a thread which manipulates
		// TwitterStream and calls these adequate listener methods continuously.
		twitterStream.user();
		
		Server server = new Server(8080);
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		servletContextHandler.setContextPath("/rails/v1.0/");
		servletContextHandler.addServlet(new ServletHolder(new ChatServlet(client,apiKey)),"/chat/*");
        servletContextHandler.addServlet(new ServletHolder(new PushServlet(client)),"/push/*");
		server.setHandler(servletContextHandler);
		try {
			server.start();
	        server.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveStatus(Status status) {

	}

	private static boolean checkSize(JSONObject data) {
		if (data.toString().getBytes().length > 4096) {
			return false;
		}
		return true;
	}

	public static int processStatus(Status status,ServiceType serviceType) throws Exception {
		HttpURLConnection conn = null;
		InputStream in = null;
		DBCursor users = null;
        int totalCount = 0;
		try {
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int day = cal.get(Calendar.DAY_OF_WEEK);
			users = findUsersForService(serviceType, status, day, hour);

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
			while (users.hasNext()) {
				int count = 0;
				regs = new JSONArray();
				while(users.hasNext() && count<1000) {
					DBObject user = users.next();
					String pushId = (String) user.get("push_id");
					regs.put(pushId);
					count++;
				}		
				if (regs.length() == 0) {
					users.close();
					return totalCount;
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
					}
                    totalCount+=successfuls.size();
                    if(!notRegistered.isEmpty()) {
                        System.out.println("Need to delete " + notRegistered.size());
                        deletePushIds(HappyStream.db, notRegistered);
                    }
					if (!replace.isEmpty()) {
						System.out.println("Need to replace " + replace.size());
						fixPushIds(HappyStream.db, replace);
					}
				} else {
					in = conn.getErrorStream();
					System.err.println(Streams.readFully(in));
				}
			}			
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
        return totalCount;
	}

	/**
	 * replace string key is to be updated with the value.
	 * 
	 * @param db
	 * @param replace
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
				BasicDBObject replacement = new BasicDBObject(k);
				users.save(replacement);
				users.remove(original);
			}
		}

	}

	@SuppressWarnings("unused")
	public static void deletePushIds(DB conn, List<String> pushIds)
			throws Exception {
		BasicDBObject doc = new BasicDBObject();
		doc.put("push_id", new BasicDBObject("$in", pushIds));
		WriteResult res = conn.getCollection("users").remove(
				doc);
	}
	
	public enum ServiceType {
		
		SERVICES("services"), DYNAMIC_SERVICES("dynamic_services");
		
		private String key;
		
		private ServiceType(String key) {
			this.key = key;
		}
		
	}

    public static DBCursor findUsersForService(ServiceType type, String screenname, int day, int hour) {
        BasicDBObject query = new BasicDBObject(type.key+".screenname", screenname).append(type.key+".day", day).append(
                type.key+".hour", hour);
        BasicDBObject fields = new BasicDBObject("push_id",1);
        return db.getCollection("users").find(query,fields);
    }

	public static DBCursor findUsersForService(ServiceType type, Status status, int day, int hour)
			throws Exception {
		return findUsersForService(type,status.getUser().getScreenName(),day,hour);
	}

	public static DBCursor findAllUsers(Status status) throws Exception {
		BasicDBObject fields = new BasicDBObject("push_id", 1);
		return db.getCollection("users").find(new BasicDBObject(), fields);
	}

//	public static ResultSet findAllUsers(Connection conn, Long afterUserId,
//			int limit) throws Exception {
//		PreparedStatement stat = conn
//				.prepareStatement(String
//						.format("select u.push_id, u.userId from USER u where u.userId > ? group by u.userId order by u.userId asc limit %s",
//								limit));
//		if (afterUserId == null) {
//			afterUserId = 0L;
//		}
//		stat.setLong(1, afterUserId);
//		stat.execute();
//		return stat.getResultSet();
//	}

}
