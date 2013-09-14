package us.wmwm.happytap.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class HappyStream {

	/**
	 * @param args
	 */

	private static Connection conn;

	private static String apiKey;

	public static void main(String[] args) {
		Connection connection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"+args[5]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		conn = connection;
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setGZIPEnabled(true);
		cb.setJSONStoreEnabled(true);
		TwitterStreamFactory f = new TwitterStreamFactory(cb.build());
		TwitterStream twitterStream = f.getInstance();
		twitterStream.setOAuthConsumer(args[0], args[1]);
		twitterStream.setOAuthAccessToken(new AccessToken(args[2], args[3]));
		apiKey = args[4];
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
					a.put(new JSONObject(DataObjectFactory.getRawJSON(status)));
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
					while(hasMore) {
						//System.out.println("handling " + result + " to " + (result+1000));
						int count = processStatus(status,result);
						result+=count;
						hasMore = count>0;
						
					}
				} catch (Exception e) {
					e.printStackTrace();
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

	public static int processStatus(Status status, int offset) throws Exception {
		HttpURLConnection conn = null;
		InputStream in = null;
		ResultSet users = null;
		try {			
			if (status.getUser().getScreenName().equalsIgnoreCase("nj_rails")) {
				users = findAllUsers(status,offset,1000);
			} else {
				Calendar cal = Calendar.getInstance();
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int day = cal.get(Calendar.DAY_OF_WEEK);
				users = findUsersForService(status, day, hour,offset,1000);
			}
			
			JSONArray regs = new JSONArray();
			List<Long> userIds = new ArrayList<Long>();
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
			JSONObject tweet = new JSONObject(DataObjectFactory.getRawJSON(status));
			data.put("tweet", tweet);
			data.put("message", text);
			data.put("type","alert");
			if(!checkSize(data)) {
				tweet.remove("source");
				tweet.remove("lang");
				tweet.remove("truncated");
				tweet.remove("possibly_sensitive");
				tweet.remove("favorited");
				tweet.remove("filter_level");
				if(!checkSize(data)) {
					JSONObject user = tweet.getJSONObject("user");				
					user.remove("default_profile");
					user.remove("verified");
					user.remove("contributors_enabled");
					user.remove("profile_image_url_https");
					user.remove("follower_request_sent");
					user.remove("is_translator");
					if(!checkSize(data)) {
						user.remove("description");				
					}
					
				}
			}
			if(!checkSize(data)) {
				data.remove("tweet");
			}
			fields.put("data", data);
			//fields.put("dry_run", Boolean.TRUE);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "key=" + apiKey);
			headers.put("Content-Type", "application/json");
			while (users.next()) {
				String pushId = users.getString(1);
				long userId = users.getLong(2);
				regs.put(pushId);
				userIds.add(userId);
			}
			if (regs.length() == 0) {
				return 0;
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
				List<Long> successfuls = new ArrayList<Long>();
				for (int i = 0; i < a.length(); i++) {
					JSONObject ob = a.getJSONObject(i);
					System.out.println(ob);
					boolean success = !ob.has("error");
					if (success) {
						successfuls.add(userIds.get(i));						
					} else {
						if("NotRegistered".equals(ob.opt("error"))) {
							deletePushId(userIds.get(i));
						}
					}
				}
				saveSentNotification(status, successfuls);
			} else {
				in = conn.getErrorStream();
				System.err.println(Streams.readFully(in));
			}	
			return regs.length();
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
		return 0;
	}

	private static void deletePushId(Long long1) throws Exception {
		PreparedStatement stat = conn.prepareStatement("delete from services where user_id=?");
		stat.setLong(1, long1);
		stat.execute();
		stat.close();
		stat = conn.prepareStatement("delete from user where id=?");
		stat.setLong(1, long1);
		stat.execute();
		stat.close();
	}

	private static void saveSentNotification(Status status, List<Long> userIds)
			throws Exception {
		PreparedStatement stat = conn
				.prepareStatement("insert into SENT(user_id,status_id) values(?,?)");
		boolean before = conn.getAutoCommit();
		conn.setAutoCommit(false);
		try {
			for(Long userId : userIds) {
				
				stat.setLong(1, userId);
				stat.setLong(2, status.getId());
				stat.addBatch();
				System.out.println("saving " + status.getText() + " for " + userId);
			}
			stat.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("rolling back");
			conn.rollback();
		} finally {
			conn.commit();
			conn.setAutoCommit(before);
			stat.close();
		}
	}

	public static ResultSet findUsersForService(Status status, int day, int hour, int offset, int limit)
			throws Exception {
		PreparedStatement stat = conn
				.prepareStatement(String.format("select u.push_id, u.id from USER u where u.id not in (select sb.user_id from SENT sb where sb.user_id=u.id and sb.status_id=?) and u.id in (select sv.user_id from SERVICES sv where sv.screenname=? and sv.day=? and sv.hour=?) group by u.id limit %s, %s",offset,limit));
		stat.setLong(1, status.getId());
		stat.setString(2, status.getUser().getScreenName());
		stat.setInt(3, day);
		stat.setInt(4, hour);
		stat.execute();
		return stat.getResultSet();
	}

	public static ResultSet findAllUsers(Status status,int offset, int limit) throws Exception {
		PreparedStatement stat = conn
				.prepareStatement(String.format("select u.push_id, u.id from USER u where u.id not in (select sb.user_id from SENT sb where sb.user_id=u.id and sb.status_id=:status_id) group by u.id limit %s, %s",offset,limit));
		stat.setLong(1, status.getId());
		stat.execute();
		return stat.getResultSet();

	}

}
