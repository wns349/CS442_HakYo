package cj.js.hak_yo.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cj.js.hak_yo.Const;

public class DBHelper extends SQLiteOpenHelper {

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
		sb.append(Const.DatabaseConst.COLUMN_NAME_MAC_ADDR).append(" ")
				.append(Const.DatabaseConst.TYPE_TEXT).append(",");
		sb.append(Const.DatabaseConst.COLUMN_NAME_ALIAS).append(" ")
				.append(Const.DatabaseConst.TYPE_TEXT).append(",");
		sb.append(Const.DatabaseConst.COLUMN_NAME_RSSI).append(" ")
				.append(Const.DatabaseConst.TYPE_INTEGER);
		sb.append(", UNIQUE (");
		sb.append(Const.DatabaseConst.COLUMN_NAME_MAC_ADDR);
		sb.append("))");

		return sb.toString();
	}

	private String getSQLInsert(FriendInfo friendInfo) {
		StringBuilder sb = new StringBuilder();

		sb.append("INSERT OR IGNORE INTO ");
		sb.append(Const.DatabaseConst.TABLE_NAME);
		sb.append("(");
		sb.append(Const.DatabaseConst.COLUMN_NAME_ALIAS).append(",");
		sb.append(Const.DatabaseConst.COLUMN_NAME_MAC_ADDR).append(",");
		sb.append(Const.DatabaseConst.COLUMN_NAME_RSSI);
		sb.append(") VALUES (");
		sb.append(friendInfo.getAlias()).append(",");
		sb.append(friendInfo.getMacAddress()).append(",");
		sb.append(friendInfo.getRssi());
		sb.append(")");

		return sb.toString();
	}

	public long insertFriendInfo(FriendInfo friendInfo) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Const.DatabaseConst.COLUMN_NAME_ALIAS, friendInfo.getAlias());
		values.put(Const.DatabaseConst.COLUMN_NAME_MAC_ADDR,
				friendInfo.getMacAddress());
		values.put(Const.DatabaseConst.COLUMN_NAME_RSSI, friendInfo.getRssi());

		long newRowId = db.insert(Const.DatabaseConst.TABLE_NAME, null, values);

		return newRowId;
	}

	public Collection<FriendInfo> selectFriendInfos() {
		SQLiteDatabase db = getReadableDatabase();

		String[] projection = { Const.DatabaseConst.COLUMN_NAME_ALIAS,
				Const.DatabaseConst.COLUMN_NAME_MAC_ADDR,
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

}
