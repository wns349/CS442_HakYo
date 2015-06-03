package cj.js.hak_yo.friend;

import java.util.ArrayList;
import java.util.List;

import org.altbeacon.beacon.Beacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cj.js.hak_yo.R;

public class DeviceBeaconListAdapter extends BaseAdapter {
	private final Context context;
	private final List<Beacon> beacons;

	public DeviceBeaconListAdapter(Context context) {
		this.context = context;
		this.beacons = new ArrayList<Beacon>();
	}

	@Override
	public int getCount() {
		return beacons.size();
	}

	@Override
	public Beacon getItem(int position) {
		return beacons.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = li.inflate(R.layout.device_listitem, null);

		Beacon beacon = this.beacons.get(position);
		if (beacon == null) {
			return null;
		}

		TextView txtName = (TextView) view.findViewById(R.id.device_item_name);
		TextView txtAddress = (TextView) view
				.findViewById(R.id.device_item_address);
		TextView txtDistance = (TextView) view
				.findViewById(R.id.device_item_distance);
		TextView txtRssi = (TextView) view.findViewById(R.id.device_item_rssi);

		txtName.setText(beacon.getBluetoothName());
		txtAddress.setText(beacon.getBluetoothAddress());
		txtDistance.setText(String.valueOf(beacon.getDistance()));
		txtRssi.setText(String.valueOf(beacon.getRssi()));

		return view;
	}

	public void addBeacon(Beacon beacon) {
		int i = 0;
		boolean exist = false;
		for (i = 0; i < this.beacons.size(); i++) {
			if (this.beacons.get(i).getBluetoothAddress()
					.equals(beacon.getBluetoothAddress())) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			this.beacons.add(beacon);
		} else {
			this.beacons.remove(i);
			this.beacons.add(i, beacon);
		}
	}

	public void clear() {
		this.beacons.clear();
	}
}
