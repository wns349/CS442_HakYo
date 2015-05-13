package cj.js.hak_yo.ble;

import java.util.Collection;

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
import android.widget.Toast;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.HakYoBroadcastReceiver;
import cj.js.hak_yo.MainActivity;

public class HakYoBLEService extends Service implements BeaconConsumer {
	private static final String TAG = "CJS";

	private static final int NOTIFICATION_ID = 1357;

	private final Beacon ADVERTISING_BEACON = new Beacon.Builder()
			.setId1(Const.BeaconConst.UUID_1).setId2(Const.BeaconConst.UUID_2)
			.setId3(Const.BeaconConst.UUID_3)
			.setManufacturer(Const.BeaconConst.MANUFACTURER)
			.setTxPower(Const.BeaconConst.TX_POWER)
			.setDataFields(Const.BeaconConst.DATA_FIELDS).build();

	private BluetoothAdapter btAdapter = null;

	private BeaconManager beaconManager = null;
	private BeaconTransmitter beaconTransmitter = null;

	private final HakYoBroadcastReceiver hakYoBroadcastReceiver = new HakYoBroadcastReceiver();

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();

		showRunningNotification(true);

		initializeBluetooth();

		initializeBeacon();

		startBeacon();

		registerBroadcastReceiver(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (isAdvertisingSupportedDevice()) {
			advertiseBluetoothDevice(false);
		}
		scanBluetoothDevices(false);

		showRunningNotification(false);

		showRunningNotification(false);

		registerBroadcastReceiver(false);
	}

	private void registerBroadcastReceiver(boolean register) {
		if (register) {
			registerReceiver(hakYoBroadcastReceiver, new IntentFilter(
					BluetoothAdapter.ACTION_STATE_CHANGED));
		} else {
			unregisterReceiver(hakYoBroadcastReceiver);
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
		if (isAdvertisingSupportedDevice()) {
			advertiseBluetoothDevice(true);
		}

		scanBluetoothDevices(true);
	}

	private boolean isAdvertisingSupportedDevice() {
		return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
	}

	private void initializeBeacon() {
		beaconManager = BeaconManager.getInstanceForApplication(this);

		if (isAdvertisingSupportedDevice()) {
			BeaconParser beaconParser = new BeaconParser()
					.setBeaconLayout(Const.BeaconConst.LAYOUT_STRING);
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

	private void initializeBluetooth() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			// Bluetooth not supported by the device
			Toast.makeText(this, "Bluetooth not found.", Toast.LENGTH_LONG)
					.show();
			stopSelf();
			return;
		}

		if (!btAdapter.isEnabled()) {
			Toast.makeText(this, "Bluetooth is not enabled.", Toast.LENGTH_LONG)
					.show();
			stopSelf();
			return;
		}
	}

	private void scanBluetoothDevices(boolean isEnabled) {
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

					// Iterator<Beacon> beaconItr = beacons.iterator();
					// while (beaconItr.hasNext()) {
					// final Beacon beacon = beaconItr.next();
					// Log.d(TAG,
					// "Beacon found: " + beacon.getBluetoothName()
					// + " / " + beacon.getBluetoothAddress()
					// + " / " + beacon.getDistance() + " / "
					// + beacon.getRssi());
					// }
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

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {

		public HakYoBLEService getService() {
			return HakYoBLEService.this;
		}
	}
}
