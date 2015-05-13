package cj.js.hak_yo;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cj.js.hak_yo.ble.HakYoBLEService;

public class HakYoBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		// When bluetooth status is changed
		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
				Intent hakYoServiceIntent = new Intent(context,
						HakYoBLEService.class);
				context.stopService(hakYoServiceIntent);
			}
		}
	}
}
