package cj.js.hak_yo;

import java.util.Arrays;
import java.util.List;

public final class Const {

	public static final int SPLASH_TIME_OUT = 1500;

	public static final int REQUEST_ENABLE_BT = 1842;
	public static final int NOTIFICATION_ID = 1353;
	public static final int NOTIFICATION_TTL = 5;

	public static final String FOUND_BEACONS_BROADCAST_INTENT = "cj.js.hak_yo.FOUND_BEACONS";
	public static final String FOUND_BEACONS_KEY = "found.beacons";
	public static final long BLE_SERVICE_SLEEP_TIME = 500L;

	public static final int ADD_FRIEND_BEACON_SCAN_THRESHOLD = 3;

	public static final long ANIMATION_TIME = 1200L;

	public static class BeaconConst {
		public static final String UUID_2 = "1";
		public static final String UUID_3 = "2";
		//public static final String LAYOUT_STRING = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
		public static final String LAYOUT_STRING = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

		public static final int MANUFACTURER = 0x0118;
		public static final int TX_POWER = -59;
		public static final List<Long> DATA_FIELDS = Arrays
				.asList(new Long[] { 0L });
		public static final String UNIQUE_REGION_ID = "myRangingUniqueId";
		
		public static final double LOW_PASS_FILTER_ALPHA = 0.75;
	}

	public static class DatabaseConst {
		public static final String TABLE_NAME = "FriendInfo";
		public static final String COLUMN_NAME_UUID = "UUID";
		public static final String COLUMN_NAME_ALIAS = "alias";
		public static final String COLUMN_NAME_RSSI = "rssi";
		public static final String COLUMN_NAME_CHARACTER = "character";

		public static final int DATABASE_VERSION = 1;
		public static final String DATABASE_NAME = "HakYo.db";

		public static final String TYPE_TEXT = "TEXT";
		public static final String TYPE_INTEGER = "INTEGER";
	}

	public static enum Character {
		c1, c2, c3, c4, c0;
	}
}
