package cj.js.hak_yo.ble;

import java.util.Collection;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.HakYoBroadcastReceiver;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.setting.SettingHelper;
import cj.js.hak_yo.util.BLEUtil;
import cj.js.hak_yo.util.UUIDUtil;

public class BLEService extends Service implements BeaconConsumer, Runnable {
	private static final String TAG = "CJS";

	private static BLEService instance = null;
	private static boolean isInitialized = false;

	private BluetoothAdapter btAdapter = null;
	private BeaconManager beaconManager = null;
	private BeaconTransmitter beaconTransmitter = null;

	private final HakYoBroadcastReceiver hakYoBroadcastReceiver = new HakYoBroadcastReceiver();

	private final LocalBinder mBinder = new LocalBinder();
	private BLECallback mBLECallback = null;

	private NotificationHelper notiHelper = null;
	private DBHelper dbHelper = null;
	private SettingHelper settingHelper = null;

	private Thread selfThread = null;
	

	@Override
	public void onCreate() {
		super.onCreate();

		instance = this;

		// Initialize DB Helper
		dbHelper = new DBHelper(getApplicationContext());

		// Initialize notification helper
		notiHelper = new NotificationHelper(getApplicationContext());

		// Initialize setting helper
		settingHelper = new SettingHelper(getApplicationContext());

		// Run thread
		selfThread = new Thread(this);
		selfThread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {

		if (BLEUtil.isAdvertisingSupportedDevice(this)) {
			advertiseBluetoothDevice(false);
		}
		scanBluetoothDevices(false);

		registerBroadcastReceiver(false);

		// Stop thread
		if (selfThread != null && selfThread.isAlive()) {
			selfThread.interrupt();
		}

		super.onDestroy();
	}

	public static BLEService getInstance() {
		return instance;
	}

	private Beacon createAdvertisingBeacon() {
		String uuid = UUIDUtil.toUUID(BLEUtil.getMacAddress()).toString();

		return new Beacon.Builder().setId1(uuid)
				.setId2(Const.BeaconConst.UUID_2)
				.setId3(Const.BeaconConst.UUID_3)
				.setManufacturer(Const.BeaconConst.MANUFACTURER)
				.setTxPower(Const.BeaconConst.TX_POWER)
				.setDataFields(Const.BeaconConst.DATA_FIELDS).build();
	}

	public void startBLE() {
		if (initializeBluetooth() && !isInitialized()) {
			isInitialized = true;
			initializeBeacon();
			startBeacon();
			registerBroadcastReceiver(true);
		}
	}

	private boolean isInitialized() {
		return isInitialized;
	}

	private void registerBroadcastReceiver(boolean register) {
		if (register) {
			registerReceiver(hakYoBroadcastReceiver, new IntentFilter(
					BluetoothAdapter.ACTION_STATE_CHANGED));
		} else {
			try {
				unregisterReceiver(hakYoBroadcastReceiver);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "Failed to unregister receiver", e);
			}
		}
	}

	private void startBeacon() {
		if (BLEUtil.isAdvertisingSupportedDevice(this)) {
			advertiseBluetoothDevice(true);
		}

		scanBluetoothDevices(true);
	}

	private void initializeBeacon() {
		Log.d(TAG, "Initializing BeaconManager");
		BeaconParser beaconParser = new BeaconParser()
				.setBeaconLayout(Const.BeaconConst.LAYOUT_STRING);

		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager.getBeaconParsers().add(beaconParser);
		// beaconManager.setForegroundScanPeriod(500L);
		// beaconManager.setBackgroundScanPeriod(500L);

		if (BLEUtil.isAdvertisingSupportedDevice(this)) {
			Log.d(TAG, "Initializing BeaconParser - Android Lollipop detected!");

			beaconTransmitter = new BeaconTransmitter(getApplicationContext(),
					beaconParser);
			// beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
			// beaconTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
		}
	}

	public void advertiseBluetoothDevice(boolean isEnabled) {
		if (beaconTransmitter == null) {
			return;
		}

		if (isEnabled && settingHelper.isAdvertiseEnabled()) {
			beaconTransmitter.startAdvertising(createAdvertisingBeacon());
		} else {
			beaconTransmitter.stopAdvertising();
		}
	}

	private boolean initializeBluetooth() {
		btAdapter = BLEUtil.getBluetoothAdapter();
		if (btAdapter == null) {
			// Bluetooth not supported by the device
			if (mBLECallback != null) {
				mBLECallback.onBluetoothNotSupported();
			}
			return false;
		}

		if (!btAdapter.isEnabled()) {
			if (mBLECallback != null) {
				mBLECallback.onBluetoothNotEnabled();
			}
			return false;
		}

		return true;
	}

	public void scanBluetoothDevices(boolean isEnabled) {
		if (beaconManager == null) {
			return;
		}

		if (isEnabled && settingHelper.isScanEnabled()) {
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
				if (mBLECallback != null) {
					// Activity is running
					mBLECallback.onBeaconsFoundInRegion(beacons, region);
				} else {
					// Activity is not running
					if (settingHelper.canSendNotification()) {
						Collection<FoundBeacon> friends = dbHelper
								.filterFriends(beacons);
						notiHelper.sendNotification(friends, region);
					}
				}
			}
		});

		try {
			Region region = new Region(Const.BeaconConst.UNIQUE_REGION_ID,
					null, null, null);
			beaconManager.startRangingBeaconsInRegion(region);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void setBLECallback(BLECallback callback) {
		this.mBLECallback = callback;
	}

	public BLECallback getBLECallback() {
		return this.mBLECallback;
	}

	public class LocalBinder extends Binder {

		public BLEService getService() {
			return BLEService.this;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Log.d(TAG, "BLEService is alive");
				Thread.sleep(Const.BLE_SERVICE_SLEEP_TIME);
			} catch (Exception e) {
				Log.e(TAG, "Error ", e);
			}
		}

	}

}
