package us.wmwm.happytap.stream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Streams {
	
	public static String readFully(InputStream in) {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;
		StringBuilder b = new StringBuilder();
		try {
			while ((line = r.readLine()) != null) {
				b.append(line);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return b.toString();
	}
}
