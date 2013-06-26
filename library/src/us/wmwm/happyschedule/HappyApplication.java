package us.wmwm.happyschedule;

import android.app.Application;

public class HappyApplication extends Application {

	static HappyApplication INSTANCE;
	
	@Override
	public void onCreate() {
		INSTANCE = this;
		super.onCreate();
	}
	
	public static HappyApplication get() {
		return INSTANCE;
	}
	
}
