package us.wmwm.happyschedule;

import java.util.concurrent.Executors;

import java.util.concurrent.ScheduledExecutorService;

import android.util.Log;

public class ThreadHelper {

	private static ScheduledExecutorService POOL = Executors
			.newScheduledThreadPool(10);
	static int THREADS = 0;

	public static ScheduledExecutorService getScheduler() {

		if (POOL.isShutdown() || POOL.isTerminated()) {

			POOL = Executors.newScheduledThreadPool(10);

		}
		
		THREADS++;
		Log.d("ThreadHelper", "thread count :" + THREADS);

		return POOL;

	}

	public static void cleanUp() {

		if (POOL != null && !POOL.isShutdown()) {

			POOL.shutdownNow();

		}

	}

}