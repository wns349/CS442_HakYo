package cj.js.hak_yo.ble;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import android.test.InstrumentationTestCase;
import cj.js.hak_yo.db.FriendInfo;
import cj.js.hak_yo.setting.SettingHelper;

public class NotificationHelperTest extends InstrumentationTestCase {

	public void testDoNotDisturb() {
		SettingHelper settingHelper = new SettingHelper(getInstrumentation()
				.getContext());

		Assert.assertFalse(settingHelper.canSendNotification(9, 0, 18, 0, 16,
				35));

		Assert.assertFalse(settingHelper.canSendNotification(18, 0, 9, 0, 22,
				35));

		Assert.assertTrue(settingHelper
				.canSendNotification(9, 0, 18, 0, 21, 33));

		Assert.assertTrue(settingHelper
				.canSendNotification(18, 0, 9, 0, 16, 33));
	}

	public void testNotificationHelper() throws InterruptedException {
		NotificationHelper notiHelper = new NotificationHelper(
				getInstrumentation().getContext());

		List<FoundBeacon> foundBeacons = new ArrayList<FoundBeacon>();
		foundBeacons.add(new FoundBeacon(new FriendInfo("A", "A", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("C", "C", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("D", "D", 1, "c1"),
				null));

		notiHelper.sendNotification(foundBeacons, null);

		Thread.sleep(500);
		foundBeacons.clear();
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("C", "C", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("D", "D", 1, "c1"),
				null));

		Thread.sleep(500);

		foundBeacons.clear();
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("E", "E", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("D", "D", 1, "c1"),
				null));

		Thread.sleep(500);
		foundBeacons.clear();
		foundBeacons.add(new FoundBeacon(new FriendInfo("F", "F", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("C", "C", 1, "c1"),
				null));
		foundBeacons.add(new FoundBeacon(new FriendInfo("B", "B", 1, "c1"),
				null));

		Thread.sleep(500);

		notiHelper.sendNotification(foundBeacons, null);
	}
}
