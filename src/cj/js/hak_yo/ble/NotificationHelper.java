package cj.js.hak_yo.ble;

import java.util.Collection;
import java.util.Iterator;

import org.altbeacon.beacon.Region;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import cj.js.hak_yo.MainActivity;

public class NotificationHelper {
	private static final int NOTIFICATION_ID = 1942;
	private static final int NOTIFICATION_INTERVAL_THRESHOLD = 10000;

	private final Context context;
	private final NotificationManager notiManager;

	public NotificationHelper(Context context) {
		this.context = context;

		this.notiManager = (NotificationManager) context
				.getSystemService(context.NOTIFICATION_SERVICE);
	}

	public void sendNotification(Collection<FoundBeacon> foundBeacons,
			Region region) {
		if (!shouldNotify(foundBeacons)) {
			return;
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context).setSmallIcon(android.R.drawable.ic_btn_speak_now)
				.setContentTitle("Say YO~!")
				.setContentText("Some one is here!");
		Intent mainIntent = new Intent(context, MainActivity.class);
		PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0,
				mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(mainPendingIntent);

		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		notiManager.notify(NOTIFICATION_ID, notification);

		LastUpdated.setFoundBeacons(foundBeacons);
		LastUpdated.updateLastNotificationSentAt();
	}

	private boolean shouldNotify(Collection<FoundBeacon> foundBeacons) {

		// Avoid notifications from flooding to user with such short time
		// interval
		if ((System.currentTimeMillis() - LastUpdated
				.getLastNotificationSentAt()) <= NOTIFICATION_INTERVAL_THRESHOLD) {
			return false;
		}
		
		// If found beacons is null, ignore
		if(foundBeacons.size() <= 0){
			return false;
		}

		// Check if beacons are updated
		if (LastUpdated.getFoundBeacons() != null
				&& LastUpdated.getFoundBeacons().size() == foundBeacons.size()) {
			boolean isSameCollection = true;
			Iterator<FoundBeacon> itrLast = LastUpdated.getFoundBeacons()
					.iterator();
			while (itrLast.hasNext()) {
				boolean isMatchFound = false;
				FoundBeacon lastBeacon = itrLast.next();
				for (FoundBeacon foundBeacon : foundBeacons) {
					if (lastBeacon == foundBeacon) {
						isMatchFound = true;
						break;
					}
				}

				// fuondBeacons are different collection!
				if (!isMatchFound) {
					isSameCollection = false;
					break;
				}
			}

			if (isSameCollection) {
				return false;
			}
		}

		// Return true
		return true;
	}

	protected static class LastUpdated {
		private static long lastNotificationSentAt = 0;
		private static Collection<FoundBeacon> foundBeacons = null;

		public static long getLastNotificationSentAt() {
			return lastNotificationSentAt;
		}

		public static void updateLastNotificationSentAt() {
			lastNotificationSentAt = System.currentTimeMillis();
		}

		public static void setLastNotificationSentAt(long a) {
			lastNotificationSentAt = a;
		}

		public static Collection<FoundBeacon> getFoundBeacons() {
			return foundBeacons;
		}

		public static void setFoundBeacons(Collection<FoundBeacon> b) {
			foundBeacons = b;
		}
	}
}
