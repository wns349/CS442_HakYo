package cj.js.hak_yo.util;

import android.bluetooth.BluetoothAdapter;

public class BLEUtil {

	public static BluetoothAdapter getBluetoothAdapter() {
		return BluetoothAdapter.getDefaultAdapter();
	}

	public static String getMacAddress() {
		return BluetoothAdapter.getDefaultAdapter().getAddress();
	}

}
