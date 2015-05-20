package cj.js.hak_yo.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingHelper {

	private final SharedPreferences sp;

	public SettingHelper(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	
}
