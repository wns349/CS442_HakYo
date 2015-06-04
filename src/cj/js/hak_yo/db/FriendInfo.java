package cj.js.hak_yo.db;

public class FriendInfo {
	private String uuid;
	private String alias;
	private String character;
	private int rssi;

	public FriendInfo() {

	}

	public FriendInfo(String alias, String uuid, int rssi, String character) {
		setAlias(alias);
		setUUID(uuid);
		setRssi(rssi);
		setCharacter(character);
	}

	private void setCharacter(String character) {
		this.character = character;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String macAddress) {
		this.uuid = macAddress;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	@Override
	public String toString() {
		return alias + "/" + uuid + "/" + rssi;
	}

	public String getCharacter() {
		return character;
	}
}
