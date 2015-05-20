package cj.js.hak_yo.ble;

import org.altbeacon.beacon.Beacon;

import cj.js.hak_yo.db.FriendInfo;

public class FoundBeacon {
	private final FriendInfo friendInfo;
	private final Beacon beacon;

	public FoundBeacon(FriendInfo friendInfo, Beacon beacon) {
		this.friendInfo = friendInfo;
		this.beacon = beacon;
	}

	public FriendInfo getFriendInfo() {
		return friendInfo;
	}

	public Beacon getBeacon() {
		return beacon;
	}
}
