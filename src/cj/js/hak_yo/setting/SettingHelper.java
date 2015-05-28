package cj.js.hak_yo.setting;

import java.util.Calendar;
import java.util.Date;

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

	public boolean canSendNotification(int fromHour, int fromMinute,
			int toHour, int toMinute, int nowHour, int nowMinute) {

		Calendar calc = Calendar.getInstance();
		calc.set(Calendar.HOUR_OF_DAY, fromHour);
		calc.set(Calendar.MINUTE, fromMinute);
		Date from = calc.getTime();

		calc.set(Calendar.HOUR_OF_DAY, toHour);
		calc.set(Calendar.MINUTE, toMinute);
		Date to = calc.getTime();

		calc.set(Calendar.HOUR_OF_DAY, nowHour);
		calc.set(Calendar.MINUTE, nowMinute);
		Date now = calc.getTime();

		// THREE cases: 1) within a day interval 2) covering upto tomorrow or
		// 3) equal date time
		int dateDiff = from.compareTo(to);
		if (dateDiff == 0) {
			// case 3
			return true;
		} else if (dateDiff < 0) {
			// case 1
			return !(now.compareTo(from) > 0 && to.compareTo(now) > 0);
		} else {
			// case 2
			return !(to.compareTo(now) > 0 || now.compareTo(from) > 0);
		}
	}

	public boolean canSendNotification() {
		if (!useDoNotDisturb()) {
			return true;
		}

		int fromHour = getFromTimeHour();
		int fromMinute = getFromTimeMinute();
		int toHour = getToTimeHour();
		int toMinute = getToTimeMinute();
		int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int m = Calendar.getInstance().get(Calendar.MINUTE);

		return canSendNotification(fromHour, fromMinute, toHour, toMinute, h, m);
	}

}
