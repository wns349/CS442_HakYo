package cj.js.hak_yo.ble;

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
}
