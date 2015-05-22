package cj.js.hak_yo.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingHelper {

	private final SharedPreferences sp;

	protected enum Keys {
		MyUUID,

	}

	public SettingHelper(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveMyUUID(String myUUID) {
		sp.edit().putString(Keys.MyUUID.name(), myUUID).commit();
	}

	public String getMyUUID() {
		return sp.getString(Keys.MyUUID.name(), null);
	}

}
