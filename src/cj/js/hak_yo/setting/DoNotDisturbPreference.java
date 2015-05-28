package cj.js.hak_yo.setting;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import cj.js.hak_yo.R;

public class DoNotDisturbPreference extends DialogPreference {
	private static final String TAG = "CJS_DDP";

	public DoNotDisturbPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DoNotDisturbPreference(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected View onCreateDialogView() {
		View v = LayoutInflater.from(getContext()).inflate(
				R.layout.preference_do_not_disturb, null);

		return v;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			Log.d(TAG, "Positive Result!");
		}
	}
}
