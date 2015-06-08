package cj.js.hak_yo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cj.js.hak_yo.ble.BLECallback;
import cj.js.hak_yo.ble.BLEService;
import cj.js.hak_yo.ble.FoundBeacon;
import cj.js.hak_yo.character.CharacterView;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.friend.AddFriendActivity;
import cj.js.hak_yo.setting.SettingActivity;
import cj.js.hak_yo.setting.SettingHelper;

public class MainActivity extends Activity implements BLECallback {
	private static final String TAG = "CJS";

	private BLEService bleService = null;

	private DBHelper dbHelper = null;

	private SettingHelper settingHelper = null;

	private Map<String, CharacterView> characters = new HashMap<String, CharacterView>();

	private FoundBeaconHistory foundBeaconHistory = new FoundBeaconHistory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbHelper = new DBHelper(this);
		settingHelper = new SettingHelper(this);

		setContentView(R.layout.activity_main);

		if (settingHelper.isFirstMainRun()) {
			displayHelp();
		}

		initializeViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_friend:
			goToAddFriendActivity();
			return true;
		case R.id.action_setting:
			goToSettingActivity();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void displayHelp() {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.TOP;
		window.setAttributes(wlp);
		window.setBackgroundDrawable(new ColorDrawable(
				android.graphics.Color.TRANSPARENT));

		dialog.setContentView(R.layout.activity_help_main);
		dialog.setCanceledOnTouchOutside(true);
		// for dismissing anywhere you touch
		View masterView = dialog.findViewById(R.id.help_main_layout);
		masterView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});
		dialog.show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startBLEService();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Only unbind, do not stop the service
		if (bleService != null && isServiceRunning(BLEService.class)) {
			bleService.setBLECallback(null);
			unbindService(bleServiceConnection);
		}
	}

	private void startBLEService() {
		Intent hakYoService = new Intent(getApplicationContext(),
				BLEService.class);
		if (!isServiceRunning(BLEService.class)) {
			startService(hakYoService);
		}

		bindService(hakYoService, bleServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	private void stopBLEService() {
		Intent hakYoService = new Intent(getApplicationContext(),
				BLEService.class);
		if (bleService != null && isServiceRunning(BLEService.class)) {
			unbindService(bleServiceConnection);
		}

		if (isServiceRunning(BLEService.class)) {
			stopService(hakYoService);
		}
	}

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void goToAddFriendActivity() {
		Intent intentAddFriend = new Intent(getApplicationContext(),
				AddFriendActivity.class);
		startActivity(intentAddFriend);
	}

	private void goToSettingActivity() {
		Intent intentSetting = new Intent(getApplicationContext(),
				SettingActivity.class);
		startActivity(intentSetting);
	}

	private void initializeViews() {
		// Hide text
		findViewById(R.id.txt_character_name).setVisibility(View.GONE);
	}

	private void showFriend(FoundBeacon friend) {
		String friendId = friend.getFriendInfo().getUUID();
		CharacterView character = characters.get(friendId);
		if (character == null) {
			Log.d(TAG, "Generating new character view: " + friendId);
			// Make new character view
			character = new CharacterView(this, friend.getFriendInfo(),
					(ViewGroup) findViewById(R.id.layout_main));
			characters.put(friendId, character);
		}

		int targetIndexToGo = getCharacterIndex(friend);
		Log.d(TAG, "ShowFriend: " + targetIndexToGo + " , " + friendId
				+ " char: " + friend.getFriendInfo().getCharacter());

		character.moveTo(targetIndexToGo, true);
	}

	private void removeFriend(String characterToRemoveId) {
		if (characterToRemoveId == null) {
			return;
		}

		CharacterView characterToRemove = characters
				.remove(characterToRemoveId);
		if (characterToRemove == null) {
			return;
		}
		
		ViewGroup parentGroup = (ViewGroup)characterToRemove.getParent();
		if(parentGroup != null){
			parentGroup.removeView(characterToRemove);
		}

		// ViewGroup mainLayout = (ViewGroup) findViewById(R.id.layout_main);
		// mainLayout.removeView(characterToRemove.getCharacterView());
		// mainLayout.removeView(characterToRemove.getCharacterBalloonView());

		// Garbage collected characterToRemove
		characterToRemove = null;
	}

	private void showNoOneCharacter(boolean isRoadEmpty) {
		ImageView noOne = (ImageView) findViewById(R.id.img_character_noone);
		ImageView noOneBalloon = (ImageView) findViewById(R.id.img_character_noone_balloon);
		TextView noOneText = (TextView) findViewById(R.id.txt_character_noone_balloon);
		noOne.setVisibility(isRoadEmpty ? View.VISIBLE : View.GONE);
		noOneBalloon.setVisibility(isRoadEmpty ? View.VISIBLE : View.GONE);
		noOneText.setVisibility(isRoadEmpty ? View.VISIBLE : View.GONE);
	}

	private ServiceConnection bleServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected called." + name);
			bleService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected called." + name + "/" + service);
			BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
			bleService = binder.getService();
			bleService.setBLECallback(MainActivity.this);
			bleService.startBLE();
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Const.REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "Bluetooth must be enabled.",
						Toast.LENGTH_LONG).show();
				stopBLEService();
				finish();
			} else if (resultCode == Activity.RESULT_OK) {
				if (bleService != null) {
					bleService.startBLE();
				}
			}
			break;
		default:
			break;
		}
	};

	@Override
	public void onBluetoothNotSupported() {
		Toast.makeText(this, "Bluetooth not found.", Toast.LENGTH_LONG).show();

		stopBLEService();
		finish();
	}

	@Override
	public void onBluetoothNotEnabled() {
		Intent enableBtIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, Const.REQUEST_ENABLE_BT);
	}

	private int getCharacterIndex(FoundBeacon foundBeacon) {
		String uuid = foundBeacon.getFriendInfo().getUUID();
		int A = foundBeacon.getFriendInfo().getRssi();
		int N = 2;

		int Rssi = foundBeacon.getBeacon().getRssi();
		if (this.foundBeaconHistory.containsPreviousRSSI(uuid)) {
			int prevRSSI = this.foundBeaconHistory.getPreviousRSSI(uuid);
			Rssi = calculateFilteredRssi(foundBeacon.getBeacon().getRssi(),
					prevRSSI);
		}

		double distance = Math.pow(10, (Rssi + A) / (double) (-10 * N));
		distance = distance / 1E6;

		Log.d(TAG, "getCharIndex: RSSI:" + Rssi + " / A: " + A
				+ " / distance: " + distance);

		this.foundBeaconHistory.addPreviousRSSI(foundBeacon.getFriendInfo()
				.getUUID(), Rssi);

		if (distance <= 2) {
			return 3;
		} else if (distance <= 5) {
			return 2;
		} else {
			return 1;
		}
	}

	private boolean shouldShowOnUI(final Collection<FoundBeacon> friends) {
		List<FoundBeacon> previouslyFoundBeacons = foundBeaconHistory
				.getPreviouslyFoundBeacons();

		if (friends.size() != previouslyFoundBeacons.size()) {
			foundBeaconHistory.setFoundBeacons(friends);
			return false;
		}
		for (FoundBeacon friend : friends) {
			if (!foundBeaconHistory.containsFriendId(friend.getFriendInfo()
					.getUUID())) {
				foundBeaconHistory.setFoundBeacons(friends);
				return false;
			}
		}

		return true;
	}

	private int calculateFilteredRssi(int rssi, int prevRssi) {
		return (int) (Const.BeaconConst.LOW_PASS_FILTER_ALPHA * prevRssi + (1 - Const.BeaconConst.LOW_PASS_FILTER_ALPHA)
				* rssi);
	}

	@Override
	public void onBeaconsFoundInRegion(final Collection<Beacon> foundBeacons,
			final Region region) {
		final Collection<FoundBeacon> friends = dbHelper
				.filterFriends(foundBeacons);

		if (!shouldShowOnUI(friends)) {
			return;
		}

		final Set<String> characterIdsToBeRemoved = new HashSet<String>(
				characters.keySet());

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Iterator<FoundBeacon> itr = friends.iterator();
				while (itr.hasNext()) {
					FoundBeacon friend = itr.next();
					showFriend(friend);
					// Remove friend's id
					characterIdsToBeRemoved.remove(friend.getFriendInfo()
							.getUUID());
				}

				Iterator<String> itrToRemove = characterIdsToBeRemoved
						.iterator();
				while (itrToRemove.hasNext()) {
					removeFriend(itrToRemove.next());
				}

				showNoOneCharacter(friends.isEmpty());
			}
		});
	}

	class FoundBeaconHistory {
		private List<FoundBeacon> previouslyFoundBeacons = new ArrayList<FoundBeacon>();
		private Map<String, Integer> rssiHistory = new HashMap<String, Integer>(); // key:
																					// UUID,
																					// val:
																					// prev
																					// RSSI

		public List<FoundBeacon> getPreviouslyFoundBeacons() {
			return previouslyFoundBeacons;
		}

		public void setFoundBeacons(Collection<FoundBeacon> friends) {
			previouslyFoundBeacons.clear();
			for (FoundBeacon beacon : friends) {
				previouslyFoundBeacons.add(beacon);
			}
		}

		public boolean containsFriendId(String friendId) {
			for (FoundBeacon foundBeacon : previouslyFoundBeacons) {
				if (foundBeacon.getFriendInfo().getUUID()
						.equalsIgnoreCase(friendId)) {
					return true;
				}
			}

			return false;
		}

		public void addPreviousRSSI(String uuid, int rssi) {
			rssiHistory.put(uuid, rssi);
		}

		public boolean containsPreviousRSSI(String uuid) {
			return uuid != null && rssiHistory.containsKey(uuid);
		}

		public int getPreviousRSSI(String uuid) {
			return rssiHistory.get(uuid);
		}

		public void setRssiHistory(Map<String, Integer> rssiHistory) {
			this.rssiHistory = rssiHistory;
		}
	}
}
