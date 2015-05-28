package cj.js.hak_yo.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import cj.js.hak_yo.R;

public class SettingHelper {

	private SharedPreferences sp;

	private final Context context;

	public SettingHelper(Context context) {
		this.context = context;
		this.sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void load(SharedPreferences sp) {
		this.sp = sp;
	}

	public boolean isScanEnabled() {
		return sp.getBoolean(context.getString(R.string.pref_scan), true);
	}

	public boolean isAdvertiseEnabled() {
		return sp.getBoolean(context.getString(R.string.pref_advertise), true);
	}

	public boolean useDoNotDisturb() {
		return sp.getBoolean(
				context.getString(R.string.pref_use_do_not_disturb), false);
	}

	public void setUseDoNotDisturb(boolean checked) {
		sp.edit()
				.putBoolean(
						context.getString(R.string.pref_use_do_not_disturb),
						checked).commit();
	}

	public void setFromTime(int hour, int minute) {
		Editor edt = sp.edit();
		edt.putInt(context.getString(R.string.pref_do_not_disturb_from_hour),
				hour);
		edt.putInt(context.getString(R.string.pref_do_not_disturb_from_minute),
				minute);
		edt.commit();
	}

	public void setToTime(int hour, int minute) {
		Editor edt = sp.edit();
		edt.putInt(context.getString(R.string.pref_do_not_disturb_to_hour),
				hour);
		edt.putInt(context.getString(R.string.pref_do_not_disturb_to_minute),
				minute);
		edt.commit();
	}

	public int getFromTimeHour() {
		return sp.getInt(
				context.getString(R.string.pref_do_not_disturb_from_hour), 0);
	}

	public int getFromTimeMinute() {
		return sp.getInt(
				context.getString(R.string.pref_do_not_disturb_from_minute), 0);
	}

	public int getToTimeHour() {
		return sp.getInt(
				context.getString(R.string.pref_do_not_disturb_to_hour), 0);
	}

	public int getToTimeMinute() {
		return sp.getInt(
				context.getString(R.string.pref_do_not_disturb_to_minute), 0);
	}

}
