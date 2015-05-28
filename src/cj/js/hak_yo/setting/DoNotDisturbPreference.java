package cj.js.hak_yo.setting;

import java.util.Locale;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TimePicker;
import cj.js.hak_yo.R;

public class DoNotDisturbPreference extends DialogPreference {
	private static final String TAG = "CJS_DDP";

	private CheckBox cbUseDoNotDisturb = null;
	private TimePicker tpFrom = null, tpTo = null;

	private final SettingHelper settingHelper;

	public DoNotDisturbPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		settingHelper = new SettingHelper(context);
	}

	public DoNotDisturbPreference(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		settingHelper = new SettingHelper(context);
	}

	@Override
	protected View onCreateDialogView() {
		View v = LayoutInflater.from(getContext()).inflate(
				R.layout.preference_do_not_disturb, null);

		cbUseDoNotDisturb = (CheckBox) v
				.findViewById(R.id.cb_use_do_not_disturb);
		tpFrom = (TimePicker) v.findViewById(R.id.from_time_picker);
		tpTo = (TimePicker) v.findViewById(R.id.to_time_picker);

		cbUseDoNotDisturb
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						tpFrom.setEnabled(isChecked);
						tpTo.setEnabled(isChecked);
					}
				});

		updatePreferences();

		return v;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			Log.d(TAG, "Positive Result!");
			settingHelper.setUseDoNotDisturb(cbUseDoNotDisturb.isChecked());

			settingHelper.setFromTime(tpFrom.getCurrentHour(),
					tpFrom.getCurrentMinute());

			settingHelper.setToTime(tpTo.getCurrentHour(),
					tpTo.getCurrentMinute());

			updatePreferences();
		}
	}

	public void updatePreferences() {

		// Update summary
		if (settingHelper.useDoNotDisturb()) {
			String from = String.format(Locale.getDefault(), "%02d:%02d",
					settingHelper.getFromTimeHour(),
					settingHelper.getFromTimeMinute());
			String to = String.format(Locale.getDefault(), "%02d:%02d",
					settingHelper.getToTimeHour(),
					settingHelper.getToTimeMinute());

			this.setSummary(from + "-" + to);
		} else {
			this.setSummary(getContext().getString(
					R.string.pref_use_do_not_disturb_disabled));
		}

		// Update checkbox
		if (cbUseDoNotDisturb != null) {
			cbUseDoNotDisturb.setChecked(settingHelper.useDoNotDisturb());
		}

		// Update time
		if (tpFrom != null && tpTo != null) {
			tpFrom.setCurrentHour(settingHelper.getFromTimeHour());
			tpFrom.setCurrentMinute(settingHelper.getFromTimeMinute());
			tpTo.setCurrentHour(settingHelper.getToTimeHour());
			tpTo.setCurrentMinute(settingHelper.getToTimeMinute());
		}
	}
}
