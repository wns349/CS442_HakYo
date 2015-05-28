package cj.js.hak_yo.ble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.altbeacon.beacon.Region;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.SplashActivity;
import cj.js.hak_yo.setting.SettingHelper;

public class NotificationHelper {
	private static final String TAG = "CJS_Noti";

	private final Context context;
	private final NotificationManager notiManager;

	private final Map<FoundBeacon, Integer> notifiedBeacons;

	public NotificationHelper(Context context) {
		this.context = context;

		this.notiManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		this.notifiedBeacons = new HashMap<FoundBeacon, Integer>();
	}

	public void clearNotifiedHistory() {
		this.notifiedBeacons.clear();
	}

	public void sendNotification(Collection<FoundBeacon> foundBeacons,
			Region region) {

		List<FoundBeacon> newBeacons = new ArrayList<FoundBeacon>(foundBeacons);
		Collections.sort(newBeacons);

		List<FoundBeacon> oldBeacons = new ArrayList<FoundBeacon>(
				notifiedBeacons.keySet());
		Collections.sort(oldBeacons);

		int n = 0, o = 0;
		while (n < newBeacons.size() && o < oldBeacons.size()) {
			FoundBeacon oldBeacon = oldBeacons.get(o);
			FoundBeacon newBeacon = newBeacons.get(n);
			int c = oldBeacon.compareTo(newBeacon);
			if (c == 0) {
				Log.d(TAG, "Match Found: " + oldBeacon);
				this.notifiedBeacons.put(oldBeacon, Const.NOTIFICATION_TTL);
				n++;
				o++;
			} else if (c < 0) {
				// oldBeacon not found in foundBeacons
				Log.d(TAG, "Decrement: " + oldBeacon);
				this.notifiedBeacons.put(oldBeacon,
						notifiedBeacons.get(oldBeacon) - 1);
				o++;
			} else if (c > 0) {
				// newBeacon not found in notifiedBeacons
				Log.d(TAG, "New notify: " + newBeacon);
				notifyFoundBeacon(newBeacon);
				this.notifiedBeacons.put(newBeacon, Const.NOTIFICATION_TTL);
				n++;
			}
		}

		// Notify rest
		while (n < newBeacons.size()) {
			FoundBeacon newBeacon = newBeacons.get(n);
			Log.d(TAG, "New notify: " + newBeacon);
			notifyFoundBeacon(newBeacon);
			this.notifiedBeacons.put(newBeacon, Const.NOTIFICATION_TTL);
			n++;
		}

		// Decrease TTL that are not found
		while (o < oldBeacons.size()) {
			FoundBeacon oldBeacon = oldBeacons.get(o);
			Log.d(TAG, "Decrement: " + oldBeacon);
			this.notifiedBeacons.put(oldBeacon,
					notifiedBeacons.get(oldBeacon) - 1);
			o++;
		}

		// Remove TTL expired entries
		Iterator<Entry<FoundBeacon, Integer>> itr = this.notifiedBeacons
				.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<FoundBeacon, Integer> entry = itr.next();
			if (entry.getValue() <= 0) {
				Log.d(TAG, "Remove: " + entry.getKey());
				itr.remove();
			}
		}
	}

	private void notifyFoundBeacon(FoundBeacon foundBeacon) {
		if (BLEService.getInstance().getBLECallback() != null) {
			// Do not notify when application is running
			return;
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(android.R.drawable.ic_btn_speak_now)
				.setContentTitle("Say YO~!")
				.setContentText(
						foundBeacon.getFriendInfo().getAlias() + " is nearby!");
		Intent mainIntent = new Intent(context, SplashActivity.class);
		PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0,
				mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(mainPendingIntent);

		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		notiManager.notify(Const.NOTIFICATION_ID, notification);
	}

}
