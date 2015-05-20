package cj.js.hak_yo.db;

public class FriendInfo {
	private String macAddress;
	private String alias;
	private int rssi;

	public FriendInfo() {

	}

	public FriendInfo(String alias, String macAddress, int rssi) {
		setAlias(alias);
		setMacAddress(macAddress);
		setRssi(rssi);

	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
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
		return alias + "/" + macAddress + "/" + rssi;
	}
}
