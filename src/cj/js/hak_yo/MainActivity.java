package cj.js.hak_yo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements BeaconConsumer {
	private static final String TAG = "CJS";

	private static final int REQUEST_ENABLE_BT = 1928;

	private static final String BEACON_UUID = "e6ed2836-e641-414b-b3ce-b200413845d3";

	private final Beacon ADVERTISING_BEACON = new Beacon.Builder()
			.setId1(BEACON_UUID).setId2("1").setId3("2")
			.setManufacturer(0x0118).setTxPower(-59)
			.setDataFields(Arrays.asList(new Long[] { 0L })).build();

	private BluetoothAdapter btAdapter = null;

	private BeaconManager beaconManager = null;
	private BeaconTransmitter beaconTransmitter = null;

	private DeviceListAdapter deviceListAdapter = null;

	private boolean isStartCalled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeViews();

		initializeBluetooth();

		initializeBeacon();

		startBeacon();
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
				startBeacon();
			}
			break;
		default:
			break;
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isAdvertisingSupportedDevice()) {
			advertiseBluetoothDevice(false);
		}
		scanBluetoothDevices(false);
	}

	CompoundButton.OnCheckedChangeListener onTbAdvertiseChanged = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isAdvertisingSupportedDevice()) {
				advertiseBluetoothDevice(isChecked);
			}
		}
	};

	CompoundButton.OnCheckedChangeListener onTbScanChanged = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			scanBluetoothDevices(isChecked);
		}
	};

	private boolean isAdvertisingSupportedDevice() {
		return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
	}

	private void initializeViews() {
		// Advertise
		ToggleButton tbAdvertise = (ToggleButton) findViewById(R.id.btn_advertise);
		tbAdvertise.setOnCheckedChangeListener(onTbAdvertiseChanged);

		// Scan
		ToggleButton tbScan = (ToggleButton) findViewById(R.id.btn_scan);
		tbScan.setOnCheckedChangeListener(onTbScanChanged);

		// Listview
		ListView listDevices = (ListView) findViewById(R.id.list_devices);
		deviceListAdapter = new DeviceListAdapter(this);
		listDevices.setAdapter(deviceListAdapter);
		deviceListAdapter.notifyDataSetChanged();
	}

	private void initializeBeacon() {
		beaconManager = BeaconManager.getInstanceForApplication(this);

		if (isAdvertisingSupportedDevice()) {
			BeaconParser beaconParser = new BeaconParser()
					.setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
			beaconTransmitter = new BeaconTransmitter(getApplicationContext(),
					beaconParser);
		}
	}

	protected void advertiseBluetoothDevice(boolean isEnabled) {
		if (isEnabled) {
			beaconTransmitter.startAdvertising(ADVERTISING_BEACON);
		} else {
			beaconTransmitter.stopAdvertising();
		}
	}

	private void startBeacon() {
		if (isStartCalled) {
			return;
		}

		ToggleButton tbAdvertise = (ToggleButton) findViewById(R.id.btn_advertise);
		if (isAdvertisingSupportedDevice()) {
			tbAdvertise.setChecked(true);
		} else {
			tbAdvertise.setChecked(false);
			tbAdvertise.setEnabled(false);
		}

		ToggleButton tbScan = (ToggleButton) findViewById(R.id.btn_scan);
		tbScan.setChecked(true);

		isStartCalled = true;
	}

	private void initializeBluetooth() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			// Bluetooth not supported by the device
			Toast.makeText(this, "Bluetooth not found.", Toast.LENGTH_LONG)
					.show();
			finish();
		}

		updateMyBluetoothAddress();

		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	private void updateMyBluetoothAddress() {
		// Update my address
		TextView txtMyAddress = (TextView) findViewById(R.id.txt_my_bluetooth_address);
		txtMyAddress.setText(btAdapter.getAddress());
	}

	private void scanBluetoothDevices(boolean isEnabled) {
		ListView listDevices = (ListView) findViewById(R.id.list_devices);
		listDevices.setEnabled(isEnabled);

		if (isEnabled) {
			beaconManager.bind(this);
		} else {
			beaconManager.unbind(this);
		}
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
