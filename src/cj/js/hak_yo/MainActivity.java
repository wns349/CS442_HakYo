package cj.js.hak_yo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import cj.js.hak_yo.ble.HakYoBLEService;
import cj.js.hak_yo.friend.AddFriendActivity;

public class MainActivity extends Activity {
	private static final String TAG = "CJS";

	private DeviceListAdapter deviceListAdapter = null;

	private HakYoBLEService bleService = null;

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

		if (bleService != null) {
			unbindService(bleServiceConnection);
		}
	}

	private void startBLEService() {
		Intent hakYoService = new Intent(getApplicationContext(),
				HakYoBLEService.class);
		bindService(hakYoService, bleServiceConnection,
				Context.BIND_AUTO_CREATE);
		startService(hakYoService);
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
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
				Toast.makeText(getApplicationContext(), "TODO",
						Toast.LENGTH_SHORT).show();
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
			bleService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			HakYoBLEService.LocalBinder binder = (HakYoBLEService.LocalBinder) service;
			bleService = binder.getService();
		}
	};
}
