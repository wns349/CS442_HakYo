package cj.js.hak_yo.ble;

import java.io.Serializable;
import java.util.Collection;

import org.altbeacon.beacon.Beacon;

public class HakYoBeaconsWrapper implements Serializable {

	private static final long serialVersionUID = 5506959981344682710L;

	private Collection<Beacon> foundBeacons;

	public HakYoBeaconsWrapper(Collection<Beacon> beacons) {
		this.foundBeacons = beacons;
	}

	public Collection<Beacon> getFoundBeacons() {
		return foundBeacons;
	}
}
