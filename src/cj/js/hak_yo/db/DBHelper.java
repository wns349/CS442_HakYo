package cj.js.hak_yo.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.altbeacon.beacon.Beacon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.ble.FoundBeacon;

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = "CJS_DBHelper";

	public DBHelper(Context context) {
		super(context, Const.DatabaseConst.DATABASE_NAME, null,
				Const.DatabaseConst.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(getSQLCreateTable());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	private String getSQLCreateTable() {
		StringBuilder sb = new StringBuilder();

		sb.append("CREATE TABLE ");
		sb.append(Const.DatabaseConst.TABLE_NAME);
		sb.append(" (");
		sb.append(Const.DatabaseConst.COLUMN_NAME_UUID).append(" ")
				.append(Const.DatabaseConst.TYPE_TEXT).append(" PRIMARY KEY,");
		sb.append(Const.DatabaseConst.COLUMN_NAME_ALIAS).append(" ")
				.append(Const.DatabaseConst.TYPE_TEXT).append(",");
		sb.append(Const.DatabaseConst.COLUMN_NAME_RSSI).append(" ")
				.append(Const.DatabaseConst.TYPE_INTEGER);
		sb.append(", UNIQUE (");
		sb.append(Const.DatabaseConst.COLUMN_NAME_UUID);
		sb.append("))");

		return sb.toString();
	}

	public long insertFriendInfo(FriendInfo friendInfo) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Const.DatabaseConst.COLUMN_NAME_ALIAS, friendInfo.getAlias());
		values.put(Const.DatabaseConst.COLUMN_NAME_UUID, friendInfo.getUUID());
		values.put(Const.DatabaseConst.COLUMN_NAME_RSSI, friendInfo.getRssi());

		long newRowId = db.insertWithOnConflict(Const.DatabaseConst.TABLE_NAME,
				null, values, SQLiteDatabase.CONFLICT_REPLACE);

		return newRowId;
	}

	public Collection<FriendInfo> selectFriendInfos() {
		SQLiteDatabase db = getReadableDatabase();

		String[] projection = { Const.DatabaseConst.COLUMN_NAME_ALIAS,
				Const.DatabaseConst.COLUMN_NAME_UUID,
				Const.DatabaseConst.COLUMN_NAME_RSSI };

		Cursor c = null;

		try {
			List<FriendInfo> fInfo = new ArrayList<FriendInfo>();
			c = db.query(Const.DatabaseConst.TABLE_NAME, projection, null,
					null, null, null, null);
			while (c.moveToNext()) {
				FriendInfo friendInfo = new FriendInfo(c.getString(0),
						c.getString(1), c.getInt(2));
				fInfo.add(friendInfo);
			}

			return fInfo;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public Collection<FoundBeacon> filterFriends(Collection<Beacon> beacons) {
		List<FoundBeacon> friendsFound = new ArrayList<FoundBeacon>();
		Collection<FriendInfo> friendInfos = this.selectFriendInfos();
		Iterator<Beacon> beaconIterator = beacons.iterator();
		while (beaconIterator.hasNext()) {
			Beacon beacon = beaconIterator.next();
			Log.d(TAG, "Beacon Found: " + beacon.getBluetoothAddress() + " / "
					+ beacon.getBluetoothName() + " / "
					+ beacon.getId1().toUuidString());
			for (FriendInfo friendInfo : friendInfos) {
				if (isFriend(beacon, friendInfo)) {
					friendsFound.add(new FoundBeacon(friendInfo, beacon));
					Log.d(TAG,
							"Friend Beacon Found: "
									+ beacon.getBluetoothAddress() + " / "
									+ beacon.getBluetoothName());
				}
			}
		}

		return friendsFound;
	}

	private boolean isFriend(Beacon beacon, FriendInfo friendInfo) {
		return friendInfo.getUUID().equalsIgnoreCase(
				beacon.getId1().toUuidString());
	}
}
