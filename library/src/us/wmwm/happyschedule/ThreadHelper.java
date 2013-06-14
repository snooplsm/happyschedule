package us.wmwm.happyschedule;
import java.util.concurrent.Executors;

import java.util.concurrent.ScheduledExecutorService;



public class ThreadHelper {


private static ScheduledExecutorService POOL = Executors.newScheduledThreadPool(2);


public static ScheduledExecutorService getScheduler() {

if(POOL.isShutdown() || POOL.isTerminated()){

POOL = Executors.newScheduledThreadPool(2);

}

return POOL;

}



public static void cleanUp(){

if(POOL!=null && !POOL.isShutdown()){

POOL.shutdownNow();

}

}

}