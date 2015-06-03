package cj.js.hak_yo.setting;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import cj.js.hak_yo.R;
import cj.js.hak_yo.ble.BLEService;

public class SettingFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private static final String TAG = "CJS_Setting";

	private SettingHelper settingHelper;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settingHelper = new SettingHelper(getActivity());

		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.registerOnSharedPreferenceChangeListener(this);

		addPreferencesFromResource(R.xml.preferences);

		updatePreferences();
	}

	@Override
	public void onPause() {
		super.onPause();

		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.unregisterOnSharedPreferenceChangeListener(this);

	}

	private void updatePreferences() {
		// Scan
		CheckBoxPreference prefScan = (CheckBoxPreference) findPreference(getString(R.string.pref_scan));
		prefScan.setChecked(settingHelper.isScanEnabled());

		// Advertise
		CheckBoxPreference prefAdvertise = (CheckBoxPreference) findPreference(getString(R.string.pref_advertise));
		prefAdvertise.setChecked(settingHelper.isAdvertiseEnabled());

		// Do not disturb
		DoNotDisturbPreference prefDoNotDisturb = (DoNotDisturbPreference) findPreference(getString(R.string.pref_do_not_disturb));
		prefDoNotDisturb.updatePreferences();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		settingHelper.load(sp);

		if (key.equalsIgnoreCase(getString(R.string.pref_scan))) {
			Log.d(TAG,
					"Key: " + key + " / " + "Value: "
							+ settingHelper.isScanEnabled());
			BLEService.getInstance().scanBluetoothDevices(
					settingHelper.isScanEnabled());
		} else if (key.equalsIgnoreCase(getString(R.string.pref_advertise))) {
			Log.d(TAG,
					"Key: " + key + " / " + "Value: "
							+ settingHelper.isAdvertiseEnabled());
			BLEService.getInstance().advertiseBluetoothDevice(
					settingHelper.isAdvertiseEnabled());
		}
	}
}
