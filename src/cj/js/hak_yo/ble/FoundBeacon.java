package cj.js.hak_yo.ble;

import java.util.Comparator;

import org.altbeacon.beacon.Beacon;

import cj.js.hak_yo.db.FriendInfo;

public class FoundBeacon implements Comparable<FoundBeacon>,
		Comparator<FoundBeacon> {
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

	@Override
	public int compareTo(FoundBeacon another) {
		return this.friendInfo.getUUID()
				.compareTo(another.friendInfo.getUUID());
	}

	@Override
	public int hashCode() {
		return this.friendInfo.getUUID().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof FoundBeacon) {
			return this.friendInfo.getUUID().equals(
					((FoundBeacon) o).getFriendInfo().getUUID());
		} else {
			return false;
		}
	}

	@Override
	public int compare(FoundBeacon lhs, FoundBeacon rhs) {
		return lhs.getFriendInfo().getUUID()
				.compareTo(rhs.getFriendInfo().getUUID());
	}

	@Override
	public String toString() {
		return "[" + this.friendInfo + ", " + this.beacon
				+ "]";
	}
}
