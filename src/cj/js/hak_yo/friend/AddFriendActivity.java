package cj.js.hak_yo.friend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.DeviceListAdapter;
import cj.js.hak_yo.MainActivity;
import cj.js.hak_yo.R;
import cj.js.hak_yo.ble.BLECallback;
import cj.js.hak_yo.ble.BLEService;
import cj.js.hak_yo.ble.FoundBeacon;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.db.FriendInfo;
import cj.js.hak_yo.util.BLEUtil;
import cj.js.hak_yo.util.UUIDUtil;

public class AddFriendActivity extends Activity implements BLECallback {
	private static final String TAG = "CJS";

	private DBHelper dbHelper = null;
	private ArrayAdapter<String> listFriendAdapter = null;
	private DeviceBeaconListAdapter deviceListAdapter = null;

	
	private BLEService bleService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		initializeViews();
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
			bleService.startBLE();
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
			final Collection<Beacon> beacons, final Region region) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//deviceListAdapter.clear();

				if (beacons.size() > 0) {
					Iterator<Beacon> itr = beacons.iterator();
					while (itr.hasNext()) {
						Beacon beacon = itr.next();
						deviceListAdapter.addBeacon(beacon);
					}
				}

				deviceListAdapter.notifyDataSetChanged();
			}
		});
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

		FriendInfo friendInfo = new FriendInfo(friendName, macAddress, rssi);
		dbHelper.insertFriendInfo(friendInfo);
		
		refreshList();
	}

	private Collection<FriendInfo> getFriends() {
		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}

		return dbHelper.selectFriendInfos();
	}
	
	private void refreshList() {
		listFriendAdapter.clear();

		Collection<FriendInfo> friends = getFriends();
		Iterator<FriendInfo> itr = friends.iterator();
		while (itr.hasNext()) {
			FriendInfo friendInfo = itr.next();
			listFriendAdapter.add(friendInfo.toString());
		}

		listFriendAdapter.notifyDataSetChanged();
	}

	private void initializeViews() {		
		// Add button
		Button btnAddDevice = (Button) findViewById(R.id.btn_add_device);
		btnAddDevice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edtMacAddr = (EditText) findViewById(R.id.edt_mac_address);
				String uuid = UUIDUtil.toUUID(edtMacAddr.getText().toString().trim()).toString();
				
				Beacon beacon = new Beacon.Builder().setId1(uuid)
						.setId2(Const.BeaconConst.UUID_2)
						.setId3(Const.BeaconConst.UUID_3)
						.setManufacturer(Const.BeaconConst.MANUFACTURER)
						.setTxPower(Const.BeaconConst.TX_POWER)
						.setBluetoothAddress(uuid)
						.setBluetoothName(uuid)
						.setDataFields(Const.BeaconConst.DATA_FIELDS).build();
				
				deviceListAdapter.addBeacon(beacon);
				deviceListAdapter.notifyDataSetChanged();
			}
		});

		Button btnRefresh = (Button) findViewById(R.id.btn_refresh_friends_list);
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshList();
			}
		});
		
		// List View - Devices
		ListView listDevices = (ListView) findViewById(R.id.list_devices);
		deviceListAdapter = new DeviceBeaconListAdapter(this);
		listDevices.setAdapter(deviceListAdapter);
		deviceListAdapter.notifyDataSetChanged();
		listDevices.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView arg0, View view, int position, long id) {
				Beacon beacon = deviceListAdapter.getItem(position);
				Log.d(TAG, "selected ble addr: " + beacon.getBluetoothAddress());
				showAddFriendDialog(position);
			}
		});

		// Textview
		TextView txtMyMacAddr = (TextView) findViewById(R.id.txt_my_mac_addr);
		txtMyMacAddr.setText(BLEUtil.getMacAddress());

		// List View
		ListView listFriends = (ListView) findViewById(R.id.list_friends);
		listFriendAdapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, new ArrayList<String>()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text = (TextView) view
						.findViewById(android.R.id.text1);
				text.setTextColor(Color.BLACK);
				return view;
			}
		};
		listFriends.setAdapter(listFriendAdapter);
		refreshList();
	}
	
	private void showAddFriendDialog(int position) {
		final int pos = position;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Add Friend");
		alert.setMessage("Do you want to add " + deviceListAdapter.getItem(position).getBluetoothName() + "as your friend?");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setHint("Enter friend's nickname");
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String nickname = input.getText().toString();
				addFriend(nickname, deviceListAdapter.getItem(pos).getBluetoothAddress(), deviceListAdapter.getItem(0).getRssi());
		  	}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled. Do nothing.
			}
		});

		alert.show();
	}
}


