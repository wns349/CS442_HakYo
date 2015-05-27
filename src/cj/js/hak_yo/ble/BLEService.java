package cj.js.hak_yo.ble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.HakYoBroadcastReceiver;
import cj.js.hak_yo.MainActivity;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.db.FriendInfo;
import cj.js.hak_yo.setting.SettingHelper;
import cj.js.hak_yo.util.BLEUtil;
import cj.js.hak_yo.util.UUIDUtil;

public class BLEService extends Service implements BeaconConsumer, Runnable {
	private static final String TAG = "CJS";

	private static final int NOTIFICATION_ID = 1357;

	private static BLEService instance = null;

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

		if (BLEUtil.isAdvertisingSupportedDevice()) {
			advertiseBluetoothDevice(false);
		}
		scanBluetoothDevices(false);

		showRunningNotification(false);

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
		showRunningNotification(true);

		if (initializeBluetooth()) {
			initializeBeacon();
			startBeacon();
			registerBroadcastReceiver(true);
		}
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

	private void showRunningNotification(boolean isShow) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (isShow) {
			NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(android.R.drawable.btn_star)
					.setContentTitle("Hak-Yo!")
					.setContentText("Hak-Yo is running!");
			Intent resultIntent = new Intent(this, MainActivity.class);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			nBuilder.setContentIntent(resultPendingIntent);

			mNotificationManager.notify(NOTIFICATION_ID, nBuilder.build());
		} else {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
	}

	private void startBeacon() {
		if (BLEUtil.isAdvertisingSupportedDevice()) {
			advertiseBluetoothDevice(true);
		}

		scanBluetoothDevices(true);
	}

	private void initializeBeacon() {
		Log.d(TAG, "Initializing BeaconManager");
		beaconManager = BeaconManager.getInstanceForApplication(this);

		if (BLEUtil.isAdvertisingSupportedDevice()) {
			Log.d(TAG, "Initializing BeaconParser - Android Lollipop detected!");

			BeaconParser beaconParser = new BeaconParser()
					.setBeaconLayout(Const.BeaconConst.LAYOUT_STRING);
			beaconTransmitter = new BeaconTransmitter(getApplicationContext(),
					beaconParser);
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
				Collection<FoundBeacon> foundBeacons = filterFriends(beacons);
				if (mBLECallback != null) {
					// Activity is running
					mBLECallback.onBeaconsFoundInRegion(foundBeacons, region);
				} else {
					// Activity is not running
					notiHelper.sendNotification(foundBeacons, region);
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

	private Collection<FoundBeacon> filterFriends(Collection<Beacon> beacons) {
		List<FoundBeacon> friendsFound = new ArrayList<FoundBeacon>();
		Collection<FriendInfo> friendInfos = dbHelper.selectFriendInfos();
		Iterator<Beacon> beaconIterator = beacons.iterator();
		while (beaconIterator.hasNext()) {
			Beacon beacon = beaconIterator.next();
			Log.d(TAG, "Beacon Found: " + beacon.getBluetoothAddress() + " / "
					+ beacon.getBluetoothName() + " / "
					+ beacon.getId1().toUuidString());
			for (FriendInfo friendInfo : friendInfos) {
				if (isFriend(beacon, friendInfo)) {
					friendsFound.add(new FoundBeacon(friendInfo, beacon));
					Log.d(TAG,
							"Friend Beacon Found: "
									+ beacon.getBluetoothAddress() + " / "
									+ beacon.getBluetoothName());
				}
			}
		}

		return friendsFound;
	}

	private boolean isFriend(Beacon beacon, FriendInfo friendInfo) {
		return friendInfo.getUUID().equalsIgnoreCase(
				beacon.getId1().toUuidString());
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
