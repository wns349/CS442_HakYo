package cj.js.hak_yo.ble;

import java.util.ArrayList;
import java.util.List;

import android.test.InstrumentationTestCase;
import cj.js.hak_yo.db.FriendInfo;

public class NotificationHelperTest extends InstrumentationTestCase {
	public void testNotificationHelper() throws InterruptedException {
		NotificationHelper notiHelper = new NotificationHelper(
				getInstrumentation().getContext());

		List<FoundBeacon> foundBeacons = new ArrayList<FoundBeacon>();
		foundBeacons.add(new FoundBeacon(new FriendInfo("A", "A", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("C", "C", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("D", "D", 1), null));

		notiHelper.sendNotification(foundBeacons, null);

		Thread.sleep(500);
		foundBeacons.clear();
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("C", "C", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("D", "D", 1), null));

		Thread.sleep(500);

		foundBeacons.clear();
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("E", "E", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("D", "D", 1), null));

		Thread.sleep(500);
		foundBeacons.clear();
		foundBeacons.add(new FoundBeacon(new FriendInfo("F", "F", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("C", "C", 1), null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1), null));

		Thread.sleep(500);

		notiHelper.sendNotification(foundBeacons, null);
	}
}
