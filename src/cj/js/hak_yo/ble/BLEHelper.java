package cj.js.hak_yo.ble;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;

public class BLEHelper {
	private static BLEHelper instance = null;

	public synchronized static BLEHelper getInstance() {
		if (instance == null) {
			instance = new BLEHelper();
		}

		return instance;
	}

	private final BluetoothAdapter btAdapter;

	private BLEHelper() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return this.btAdapter;
	}

	public String getMacAddress() {
		return this.btAdapter.getAddress();
	}
//
//	public List<Long> getMacAddressAsLong() {
//		String[] nums = getMacAddress().split(":");
//
//		List<Long> rtn = new ArrayList<Long>();
//		for (String num : nums) {
//			rtn.add(Long.parseLong(num, 16));
//		}
//
//		return rtn;
//	}
//
//	public String toMacAddress(List<Long> nums) {
//		StringBuilder sb = new StringBuilder();
//
//		for (int i = 0; i < nums.size(); i++) {
//			Long n = nums.get(i);
//			sb.append(Long.toHexString(n));
//			if (i + 1 < nums.size()) {
//				sb.append(":");
//			}
//		}
//
//		return sb.toString();
//	}

}
