package us.wmwm.happyschedule.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import us.wmwm.happyschedule.application.HappyApplication;

public class Streams {

	public static InputStream getStream(String resource) {
		try {
			return HappyApplication.get().openFileInput(resource);			
		} catch (Exception e) {
			int resId = HappyApplication.get().getResources().getIdentifier(resource.split("\\.")[0], "raw", HappyApplication.get().getPackageName());
			return HappyApplication.get().getResources().openRawResource(resId);
		}
	}
	
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
