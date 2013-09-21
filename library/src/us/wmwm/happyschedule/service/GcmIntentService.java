package us.wmwm.happyschedule.service;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.activity.MainActivity;
import us.wmwm.happyschedule.fragment.SettingsFragment;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	private static final String TAG = GcmIntentService.class.getSimpleName();

	NotificationManager mNotificationManager;

	public GcmIntentService(String name) {
		super(name);
	}

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				// sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				// sendNotification("Deleted messages on server: "
				// + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// This loop represents the service doing some work.
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				// Post notification of received message.
				if("alert".equals(extras.getString("type"))) {
					System.out.println(extras.get("tweet"));
					sendNotification(extras.getString("title"),
							extras.getString("message"));
					Log.i(TAG, "Received: " + extras.toString());
				} else
				if("upgrade_alert".equals(extras.getString("type"))) {
					int newVersion = extras.getInt("version");
					int currVersion = SettingsFragment.getAppVersion();
					if(currVersion<newVersion) {
						showUpgradeNotification();
					}
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void showUpgradeNotification() {
		boolean on = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.settings_key_push_on), false);
		if (!on) {
			return;
		}
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName())), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_stat_512)
				.setContentTitle(getString(R.string.app_name) + " update available.")
				.setContentText("Upgrade now to ensure correct schedules.")
				.setLargeIcon(
						((BitmapDrawable)getResources().getDrawable(R.drawable.ic_launcher)).getBitmap());
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setPriority(Notification.PRIORITY_HIGH);
		mBuilder.setLights(0xFFFF5555, 3000, 3000);
		boolean vibrate = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.settings_key_push_vibrate),
						false);
		if (vibrate) {
			mBuilder.setVibrate(new long[] { 0, 200, 500 });
		}
		boolean sound = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.settings_key_push_audio), false);
		if (sound) {
			Uri alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert != null) {
				mBuilder.setSound(alert);
			}
		}
		Notification notif = mBuilder.build();
		//notif.tickerText = msg;
		mNotificationManager.notify("update_available".hashCode(), notif);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String title, String msg) {
		boolean on = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.settings_key_push_on), false);
		if (!on) {
			return;
		}
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_stat_512)
				.setContentTitle(title)
				.setLargeIcon(
						((BitmapDrawable)getResources().getDrawable(R.drawable.ic_512)).getBitmap())
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setPriority(Notification.PRIORITY_HIGH);
		mBuilder.setLights(0xFFFF5555, 3000, 3000);
		boolean vibrate = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.settings_key_push_vibrate),
						false);
		if (vibrate) {
			mBuilder.setVibrate(new long[] { 0, 200, 500 });
		}
		boolean sound = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getString(R.string.settings_key_push_audio), false);
		if (sound) {
			Uri alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert != null) {
				mBuilder.setSound(alert);
			}
		}
		Notification notif = mBuilder.build();
		notif.tickerText = msg;
		mNotificationManager.notify(msg.hashCode(), notif);
	}

}
