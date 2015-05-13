package cj.js.hak_yo;

import java.util.Arrays;
import java.util.List;

public final class Const {

	public static final int REQUEST_ENABLE_BT = 1842;

	public static final String FOUND_BEACONS_BROADCAST_INTENT = "cj.js.hak_yo.FOUND_BEACONS";
	public static final String FOUND_BEACONS_KEY = "found.beacons";

	public static class BeaconConst {
		public static final String UUID_1 = "e6ed2836-e641-414b-b3ce-b200413845d3";
		public static final String UUID_2 = "1";
		public static final String UUID_3 = "2";
		public static final String LAYOUT_STRING = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";

		public static final int MANUFACTURER = 0x0118;
		public static final int TX_POWER = -59;
		public static final List<Long> DATA_FIELDS = Arrays
				.asList(new Long[] { 0L });
		public static final String UNIQUE_REGION_ID = "myRangingUniqueId";
	}
}
