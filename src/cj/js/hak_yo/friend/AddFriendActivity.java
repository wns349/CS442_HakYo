package cj.js.hak_yo.friend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import cj.js.hak_yo.R;
import cj.js.hak_yo.db.DBHelper;
import cj.js.hak_yo.db.FriendInfo;
import cj.js.hak_yo.util.BLEUtil;

public class AddFriendActivity extends Activity {

	/*****
	 * SAMPLE !!!!! SAMPLE !!!!! SAMPLE !!!!! SAMPLE !!!!! SAMPLE !!!!!
	 */

	private DBHelper dbHelper = null;
	private ArrayAdapter<String> listAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);

		initializeViews();
	}

	private void addFriend(String friendName, String macAddress, int rssi) {
		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}

		FriendInfo friendInfo = new FriendInfo(friendName, macAddress, rssi);
		dbHelper.insertFriendInfo(friendInfo);
	}

	private Collection<FriendInfo> getFriends() {
		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}

		return dbHelper.selectFriendInfos();
	}

	private void refreshList() {
		listAdapter.clear();

		Collection<FriendInfo> friends = getFriends();
		Iterator<FriendInfo> itr = friends.iterator();
		while (itr.hasNext()) {
			FriendInfo friendInfo = itr.next();
			listAdapter.add(friendInfo.toString());
		}

		listAdapter.notifyDataSetChanged();
	}

	private void initializeViews() {
		// Add button
		Button btnAddFriend = (Button) findViewById(R.id.btn_add_friend);
		btnAddFriend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edtMacAddr = (EditText) findViewById(R.id.edt_mac_address);
				String macAddrToAdd = edtMacAddr.getText().toString().trim();

				addFriend(macAddrToAdd, macAddrToAdd, 10);
				refreshList();
			}
		});

		Button btnRefresh = (Button) findViewById(R.id.btn_refresh_friends_list);
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshList();
			}
		});

		// Textview
		TextView txtMyMacAddr = (TextView) findViewById(R.id.txt_my_mac_addr);
		txtMyMacAddr.setText(BLEUtil.getMacAddress());

		// List View
		ListView listFriends = (ListView) findViewById(R.id.list_friends);
		listAdapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, new ArrayList<String>()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text = (TextView) view
						.findViewById(android.R.id.text1);
				text.setTextColor(Color.BLACK);
				return view;
			}
		};
		listFriends.setAdapter(listAdapter);
		refreshList();
	}
}
