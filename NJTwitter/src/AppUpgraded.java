import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;
import us.wmwm.happytap.stream.HappyStream;
import us.wmwm.happytap.stream.Streams;


public class AppUpgraded {

	/**
	 * @param args
	 */
	
	private static Connection conn;
	private static String apiKey;
	
	public static void main(String[] args) {
		Connection connection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"+args[1]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		conn = connection;
		apiKey = args[0];
		int version = Integer.parseInt(args[2]);
		try {
			int result = 0;
			boolean hasMore = true;
			while(hasMore) {
				//System.out.println("handling " + result + " to " + (result+1000));
				int count = sendAppUpdated(version, result);
				result+=count;
				hasMore = count>0;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int sendAppUpdated(int version, int offset) throws Exception {
		HttpURLConnection conn = null;
		InputStream in = null;
		ResultSet users = null;
		try {			
			
			//users = HappyStream.findAllUsers(AppUpgraded.conn,offset,1000);
			
			
			JSONArray regs = new JSONArray();
			List<Long> userIds = new ArrayList<Long>();
			JSONObject fields = new JSONObject();
			fields.put("time_to_live", 1800);
			JSONObject data = new JSONObject();
			data.put("type","upgrade_alert");
			data.put("version", version);
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
							//HappyStream.deletePushId(AppUpgraded.conn,userIds.get(i));
						}
					}
				}
				//saveSentNotification(status, successfuls);
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

}
