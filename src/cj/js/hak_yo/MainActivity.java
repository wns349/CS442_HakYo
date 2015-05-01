package cj.js.hak_yo;

import java.util.Collection;
import java.util.Iterator;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements BeaconConsumer {
	private static final String TAG = "CJS";

	private static final int REQUEST_ENABLE_BT = 1928;

	private BluetoothAdapter btAdapter = null;

	private BeaconManager beaconManager = null;

	private DeviceListAdapter deviceListAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeViews();

		initializeBluetooth();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "Bluetooth must be enabled.",
						Toast.LENGTH_LONG).show();
				finish();
			} else if (resultCode == Activity.RESULT_OK) {
				scanBluetoothDevices();
			}
			break;
		default:
			break;
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (beaconManager != null) {
			beaconManager.unbind(this);
		}
	}

	private void initializeViews() {
		ListView listDevices = (ListView) findViewById(R.id.list_devices);
		deviceListAdapter = new DeviceListAdapter(this);

		listDevices.setAdapter(deviceListAdapter);
		deviceListAdapter.notifyDataSetChanged();
	}

	private void initializeBluetooth() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			// Bluetooth not supported by the device
			Toast.makeText(this, "Bluetooth not found.", Toast.LENGTH_LONG)
					.show();
			finish();
		}

		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			scanBluetoothDevices();
		}
	}

	private void scanBluetoothDevices() {
		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager.bind(this);
	}

	@Override
	public void onBeaconServiceConnect() {
		beaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons,
					Region region) {
				if (beacons.size() > 0) {
					Iterator<Beacon> beaconItr = beacons.iterator();
					while (beaconItr.hasNext()) {
						final Beacon beacon = beaconItr.next();
						Log.d(TAG,
								"Beacon found: " + beacon.getBluetoothName()
										+ " / " + beacon.getBluetoothAddress()
										+ " / " + beacon.getDistance() + " / "
										+ beacon.getRssi());
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								deviceListAdapter.addBeacon(beacon);
								deviceListAdapter.notifyDataSetChanged();
							}
						});
					}
				}
			}
		});

		try {
			Region region = new Region("myRangingUniqueId", null, null, null);
			beaconManager.startRangingBeaconsInRegion(region);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
