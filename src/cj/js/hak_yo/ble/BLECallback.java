package cj.js.hak_yo.ble;

import java.util.Collection;

import org.altbeacon.beacon.Region;

public interface BLECallback {

	public void onBluetoothNotSupported();

	public void onBluetoothNotEnabled();

	public void onBeaconsFoundInRegion(Collection<FoundBeacon> foundBeacons, Region region);

}
