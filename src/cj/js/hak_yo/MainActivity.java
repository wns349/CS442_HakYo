package cj.js.hak_yo;

import java.util.Collection;
import java.util.Iterator;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cj.js.hak_yo.ble.BLECallback;
import cj.js.hak_yo.ble.BLEService;
import cj.js.hak_yo.ble.FoundBeacon;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.friend.AddFriendActivity;
import cj.js.hak_yo.setting.SettingActivity;
import cj.js.hak_yo.setting.SettingHelper;
import cj.js.hak_yo.util.BLEUtil;
import cj.js.hak_yo.util.UUIDUtil;

public class MainActivity extends Activity implements BLECallback {
	private static final String TAG = "CJS";

	private BLEService bleService = null;

	private DBHelper dbHelper = null;

	private SettingHelper settingHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbHelper = new DBHelper(this);
		settingHelper = new SettingHelper(this);

		setContentView(R.layout.activity_main);

		if (settingHelper.isFirstMainRun()) {
			displayHelp();
		}

		initializeViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_friend:
			goToAddFriendActivity();
			return true;
		case R.id.action_setting:
			goToSettingActivity();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void displayHelp() {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.TOP;
		window.setAttributes(wlp);
		window.setBackgroundDrawable(new ColorDrawable(
				android.graphics.Color.TRANSPARENT));

		dialog.setContentView(R.layout.activity_help_main);
		dialog.setCanceledOnTouchOutside(true);
		// for dismissing anywhere you touch
		View masterView = dialog.findViewById(R.id.help_main_layout);
		masterView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});
		dialog.show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startBLEService();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Only unbind, do not stop the service
		if (bleService != null && isServiceRunning(BLEService.class)) {
			bleService.setBLECallback(null);
			unbindService(bleServiceConnection);
		}
	}

	private void startBLEService() {
		Intent hakYoService = new Intent(getApplicationContext(),
				BLEService.class);
		if (!isServiceRunning(BLEService.class)) {
			startService(hakYoService);
		}

		bindService(hakYoService, bleServiceConnection,
				Context.BIND_AUTO_CREATE);
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

	private void goToAddFriendActivity() {
		Intent intentAddFriend = new Intent(getApplicationContext(),
				AddFriendActivity.class);
		startActivity(intentAddFriend);
	}

	private void goToSettingActivity() {
		Intent intentSetting = new Intent(getApplicationContext(),
				SettingActivity.class);
		startActivity(intentSetting);
	}

	private void initializeViews() {

//		// Listview
//		ListView listDevices = (ListView) findViewById(R.id.list_devices);
//		deviceListAdapter = new DeviceListAdapter(this);
//		listDevices.setAdapter(deviceListAdapter);
//		deviceListAdapter.notifyDataSetChanged();
//
//		// My UUID
//		TextView txtMyUUID = (TextView) findViewById(R.id.txt_my_UUID);
//		txtMyUUID.setText(UUIDUtil.toUUID(BLEUtil.getMacAddress()).toString());
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
	public void onBeaconsFoundInRegion(final Collection<Beacon> foundBeacons,
			final Region region) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				deviceListAdapter.clear();
//
//				Collection<FoundBeacon> friends = dbHelper
//						.filterFriends(foundBeacons);
//				if (friends.size() > 0) {
//					Iterator<FoundBeacon> itr = friends.iterator();
//					while (itr.hasNext()) {
//						FoundBeacon foundBeacon = itr.next();
//						deviceListAdapter.addBeacon(foundBeacon);
//					}
//				}
//
//				deviceListAdapter.notifyDataSetChanged();
			}
		});
	}
}
