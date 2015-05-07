package cj.js.hak_yo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private static final String TAG = "CJS";

	private static final int REQUEST_ENABLE_BT = 1928;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeViews();

		initializeBluetooth();
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

			}
			break;
		default:
			break;
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	CompoundButton.OnCheckedChangeListener onTbAdvertiseChanged = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {

			Intent hakYoService = new Intent(getApplicationContext(),
					HakYoService.class);
			if (isChecked) {
				startService(hakYoService);
			} else {
				stopService(hakYoService);
			}
		}
	};

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

	private boolean isAdvertisingSupportedDevice() {
		return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
	}

	private void initializeViews() {
		// Advertise
		ToggleButton tbAdvertise = (ToggleButton) findViewById(R.id.btn_advertise);
		tbAdvertise.setChecked(isMyServiceRunning(HakYoService.class));

		tbAdvertise.setOnCheckedChangeListener(onTbAdvertiseChanged);
	}

	private void initializeBluetooth() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			// Bluetooth not supported by the device
			Toast.makeText(this, "Bluetooth not found.", Toast.LENGTH_LONG)
					.show();
			finish();
		}

		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}
}
