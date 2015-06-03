package cj.js.hak_yo.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import cj.js.hak_yo.R;
import cj.js.hak_yo.ble.BLEService;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.util.BLEUtil;
import cj.js.hak_yo.util.UUIDUtil;

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
		initializePreference();
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

	private void initializePreference() {
		// My UUID
		Preference prefMyId = (Preference) findPreference(getString(R.string.pref_myinfo));
		prefMyId.setSummary(UUIDUtil.toUUID(BLEUtil.getMacAddress()).toString());

		// Delete all
		Preference prefDeleteAll = (Preference) findPreference(getString(R.string.pref_deleteall));
		prefDeleteAll
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage("Are you sure to delete all your friends?");
						builder.setPositiveButton("Delete",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										DBHelper dbHelper = new DBHelper(
												getActivity());
										dbHelper.deleteAll();
									}
								});

						builder.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});

						builder.show();
						return false;
					}
				});

		// Contact
		Preference prefContact = (Preference) findPreference(getString(R.string.pref_dev_team));
		prefContact
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Toast.makeText(getActivity(),
								"Please do not try to contact us! :)",
								Toast.LENGTH_LONG).show();
						return false;
					}

				});
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
