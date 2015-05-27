package cj.js.hak_yo.setting;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import cj.js.hak_yo.R;
import cj.js.hak_yo.ble.BLEService;

public class SettingFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

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
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		settingHelper.load(sp);

		if (key.equalsIgnoreCase(getString(R.string.pref_scan))) {
			BLEService.getInstance().scanBluetoothDevices(
					settingHelper.isScanEnabled());
		} else if (key.equalsIgnoreCase(getString(R.string.pref_advertise))) {
			BLEService.getInstance().advertiseBluetoothDevice(
					settingHelper.isAdvertiseEnabled());
		}
	}

}
