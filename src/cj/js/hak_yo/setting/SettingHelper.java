package cj.js.hak_yo.setting;

import cj.js.hak_yo.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingHelper {

	private SharedPreferences sp;

	private final Context context;

	public SettingHelper(Context context) {
		this.context = context;
		this.sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isScanEnabled() {
		return sp.getBoolean(context.getString(R.string.pref_scan), true);
	}

	public boolean isAdvertiseEnabled() {
		return sp.getBoolean(context.getString(R.string.pref_scan), true);
	}

	public void load(SharedPreferences sp) {
		this.sp = sp;
	}
}
