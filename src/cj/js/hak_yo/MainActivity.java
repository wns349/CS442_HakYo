package cj.js.hak_yo;

import java.util.Collection;
import java.util.Iterator;

import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import cj.js.hak_yo.ble.BLECallback;
import cj.js.hak_yo.ble.BLEService;
import cj.js.hak_yo.ble.FoundBeacon;
import cj.js.hak_yo.friend.AddFriendActivity;
import cj.js.hak_yo.setting.SettingActivity;

public class MainActivity extends Activity implements BLECallback {
	private static final String TAG = "CJS";

	private DeviceListAdapter deviceListAdapter = null;

	private BLEService bleService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeViews();

		startBLEService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Only unbind, do not stop the service
		if (bleService != null && isServiceRunning(BLEService.class)) {
			unbindService(bleServiceConnection);
		}
	}

	private void startBLEService() {
		Intent hakYoService = new Intent(getApplicationContext(),
				BLEService.class);
		if (!isServiceRunning(BLEService.class)) {
			startService(hakYoService);
		}

		if (bleService == null) {
			bindService(hakYoService, bleServiceConnection,
					Context.BIND_AUTO_CREATE);
		}
	}

	private void stopBLEService() {
		Intent hakYoService = new Intent(getApplicationContext(),
				BLEService.class);
		if (bleService != null && isServiceRunning(BLEService.class)) {
			unbindService(bleServiceConnection);
		}

		if (isServiceRunning(BLEService.class)) {
			stopService(hakYoService);
		}
	}

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void initializeViews() {
		// Add Friend
		Button btnAddFriend = (Button) findViewById(R.id.btn_add_friend);
		btnAddFriend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentAddFriend = new Intent(getApplicationContext(),
						AddFriendActivity.class);
				startActivity(intentAddFriend);
			}
		});

		// Settings
		Button btnSettings = (Button) findViewById(R.id.btn_settings);
		btnSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentSetting = new Intent(getApplicationContext(),
						SettingActivity.class);
				startActivity(intentSetting);
			}
		});

		// Listview
		ListView listDevices = (ListView) findViewById(R.id.list_devices);
		deviceListAdapter = new DeviceListAdapter(this);
		listDevices.setAdapter(deviceListAdapter);
		deviceListAdapter.notifyDataSetChanged();
	}

	private ServiceConnection bleServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected called." + name);
			bleService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected called." + name + "/" + service);
			BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
			bleService = binder.getService();
			bleService.setBLECallback(MainActivity.this);
			bleService.startBLE();
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Const.REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "Bluetooth must be enabled.",
						Toast.LENGTH_LONG).show();
				stopBLEService();
				finish();
			} else if (resultCode == Activity.RESULT_OK) {
				if (bleService != null) {
					bleService.startBLE();
				}
			}
			break;
		default:
			break;
		}
	};

	@Override
	public void onBluetoothNotSupported() {
		Toast.makeText(this, "Bluetooth not found.", Toast.LENGTH_LONG).show();

		stopBLEService();
		finish();
	}

	@Override
	public void onBluetoothNotEnabled() {
		Intent enableBtIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, Const.REQUEST_ENABLE_BT);
	}

	@Override
	public void onBeaconsFoundInRegion(
			final Collection<FoundBeacon> foundBeacons, final Region region) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				deviceListAdapter.clear();

				if (foundBeacons.size() > 0) {
					Iterator<FoundBeacon> itr = foundBeacons.iterator();
					while (itr.hasNext()) {
						FoundBeacon foundBeacon = itr.next();
						deviceListAdapter.addBeacon(foundBeacon);
					}
				}

				deviceListAdapter.notifyDataSetChanged();
			}
		});
	}
}
