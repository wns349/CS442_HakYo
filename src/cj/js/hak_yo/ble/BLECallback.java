package cj.js.hak_yo.ble;

import java.util.Collection;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

public interface BLECallback {

	public void onBluetoothNotSupported();

	public void onBluetoothNotEnabled();

	public void onBeaconsFoundInRegion(Collection<Beacon> beacons, Region region);

}
