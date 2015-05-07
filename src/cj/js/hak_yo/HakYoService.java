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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class HakYoService extends Service implements BeaconConsumer {
	private static final String TAG = "CJS";

	private static final String BEACON_UUID = "e6ed2836-e641-414b-b3ce-b200413845d3";

	private static final int NOTIFICATION_ID = 1357;

	private final Beacon ADVERTISING_BEACON = new Beacon.Builder()
			.setId1(BEACON_UUID).setId2("1").setId3("2")
			.setManufacturer(0x0118).setTxPower(-59)
			.setDataFields(Arrays.asList(new Long[] { 0L })).build();

	private BluetoothAdapter btAdapter = null;

	private BeaconManager beaconManager = null;
	private BeaconTransmitter beaconTransmitter = null;

	@Override
	public void onCreate() {
		super.onCreate();

		showRunningNotification(true);

		initializeBluetooth();

		initializeBeacon();

		startBeacon();
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
	}

	private void showRunningNotification(boolean isShow) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (isShow) {
			NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(android.R.drawable.btn_star)
					.setContentTitle("Hak-Yo!")
					.setContentText("Hak-Yo is running!");
			Intent resultIntent = new Intent(this, MainActivity.class);
			// The stack builder object will contain an artificial back stack
			// for
			// the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out
			// of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			nBuilder.setContentIntent(resultPendingIntent);

			// mId allows you to update the notification later on.
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
					Iterator<Beacon> beaconItr = beacons.iterator();
					while (beaconItr.hasNext()) {
						final Beacon beacon = beaconItr.next();
						Log.d(TAG,
								"Beacon found: " + beacon.getBluetoothName()
										+ " / " + beacon.getBluetoothAddress()
										+ " / " + beacon.getDistance() + " / "
										+ beacon.getRssi());
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

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
