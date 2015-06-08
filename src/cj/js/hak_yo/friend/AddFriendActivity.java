package cj.js.hak_yo.friend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.R;
import cj.js.hak_yo.ble.BLECallback;
import cj.js.hak_yo.ble.BLEService;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.db.FriendInfo;
import cj.js.hak_yo.util.BLEUtil;
import cj.js.hak_yo.util.UUIDUtil;

public class AddFriendActivity extends Activity implements BLECallback {
	private static final String TAG = "CJS";

	private DBHelper dbHelper = null;
	private BLEService bleService = null;

	private NearestBeacon nearestBeacon = new NearestBeacon();
	private boolean isDialogShown = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		this.dbHelper = new DBHelper(this);

		initializeView();
	}

	private void initializeView() {
		TextView txtMyInfo = (TextView) findViewById(R.id.txt_addfriend_my_info);
		txtMyInfo.setText("My ID: " + UUIDUtil.toUUID(BLEUtil.getMacAddress()));
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
			bleService.setBLECallback(AddFriendActivity.this);
			//bleService.startBLE();
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
	public void onBeaconsFoundInRegion(final Collection<Beacon> beacons,
			final Region region) {
		// Run on UI
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Find beacon with highest RSSI value
				Beacon nBeacon = null;
				if (beacons != null && !beacons.isEmpty()) {
					int t = Integer.MIN_VALUE;
					for (Beacon b : beacons) {
						if (b.getRssi() >= t) {
							nBeacon = b;
							t = nBeacon.getRssi();
						}
					}
				}

				if (isCandidateForNewFriend(nBeacon)) {
					showAddFriendDialog(nBeacon, nearestBeacon.getAverageRSSI());
				}
			}
		});
	}

	private boolean isCandidateForNewFriend(Beacon nBeacon) {
		if (nBeacon == null) {
			return false;
		}

		nearestBeacon.mark(nBeacon.getId1().toUuidString(), nBeacon.getRssi());
		return (nearestBeacon.getCount() >= Const.ADD_FRIEND_BEACON_SCAN_THRESHOLD);
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

	private void addFriend(String friendName, String macAddress, int rssi) {
		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}

		int randomCharacter = (int) (System.currentTimeMillis() % Const.Character
				.values().length);
		FriendInfo friendInfo = new FriendInfo(friendName, macAddress, rssi,
				Const.Character.values()[randomCharacter].name());
		dbHelper.insertFriendInfo(friendInfo);
	}

	private void showAddFriendDialog(final Beacon nBeacon, final double avgRssi) {
		// Do not show multiple dialogs
		if (isDialogShown) {
			return;
		}
		isDialogShown = true;

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		String existingFriendAlias = null;
		if ((existingFriendAlias = dbHelper.getFriendAlias(nBeacon)) != null) {
			// already friend
			dialogBuilder.setTitle("Existing Friend");
			dialogBuilder.setMessage("You are already friends with "
					+ existingFriendAlias + " !" + "\n"
					+ "Not a friend? Try searching again!");
			dialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finish();
						}
					});
			dialogBuilder.show();
		} else {
			// already friend
			dialogBuilder.setTitle("New Friend!");
			dialogBuilder.setMessage("New Friend ID: "
					+ nBeacon.getId1().toUuidString());
			final EditText input = new EditText(this);
			input.setHint("Enter Friend's Name");
			dialogBuilder.setView(input);

			dialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String alias = input.getText().toString();
							if (alias == null || alias.isEmpty()) {
								alias = "Unknown";
							}

							addFriend(alias, nBeacon.getId1().toUuidString(),
									(int) avgRssi);
							Toast.makeText(getApplicationContext(),
									alias + " added!", Toast.LENGTH_LONG)
									.show();
							dialog.cancel();
							finish();
						}
					});

			dialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finish();
						}
					});

			dialogBuilder.show();
		}

	}

	class NearestBeacon {
		private String uuid;
		private List<Integer> rssiVals = new ArrayList<Integer>();

		public int getCount() {
			return rssiVals.size();
		}

		public void mark(String uuid, int rssi) {
			if (this.uuid != null && this.uuid.equalsIgnoreCase(uuid)) {
				// Existing
				rssiVals.add(rssi);
			} else {
				// New
				this.uuid = uuid;
				rssiVals.clear();
				rssiVals.add(rssi);
			}
		}

		public double getAverageRSSI() {
			double t = 0.0;
			for (int i : rssiVals) {
				t += i;
			}

			return t / rssiVals.size();
		}
	}
}
