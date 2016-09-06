package com.control.ws.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.control.ws.UPnPManager;
import com.control.ws.model.DeviceUpnp;
import com.control.ws.model.Sensor;
import com.control.ws.model.db.SensorValue;
import com.control.ws.xmpp.data.DeviceDescription;
import com.control.ws.xmpp.data.SensorDeviceDescription;

public class DBManager {
	
	private static DBManager instance;
	
	private Set<DeviceUpnp> mDevices;
	
	private Map<String, List<SensorValue>> mSensorValues;
	
	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}
	
	private DBManager() {
		mDevices = new HashSet<>();
		mSensorValues = new HashMap<>();
	}
	
	public void addDevice(DeviceUpnp device) {
		if (device instanceof Sensor) {
			addSensorValues((Sensor) device);
		}
		mDevices.add(device);
		UPnPManager.printDebug("AddDevice: " + mDevices.size());
	}
	
	private void addSensorValues(Sensor sensorDescription) {
		List<SensorValue> sensorValues = new ArrayList<>();
		for (String sensorUrn : sensorDescription.getSensorURNs()) {
			for (String dataItem : sensorDescription.getDataItems(sensorUrn)) {
				SensorValue value = new SensorValue();
				value.dataItem = dataItem;
				sensorValues.add(value);
			}
		}
		mSensorValues.put(sensorDescription.getExternalID(), sensorValues);
	}
	
	public void updateValue(String deviceExternalId, String dataItem, String value) {
		UPnPManager.printDebug("Updating value of: " + deviceExternalId);
		UPnPManager.printDebug(dataItem + " is now: " + value);
		List<SensorValue> values = mSensorValues.get(deviceExternalId);
		for (SensorValue sensorValue : values) {
			if (sensorValue.dataItem.equalsIgnoreCase(dataItem)) {
				sensorValue.value = value;
			}
		}
	}
	
	public List<SensorValue> getValuesFor(String deviceExternalId) {
		return mSensorValues.get(deviceExternalId);
	}
	
	public List<SensorValue> getValuesFor(DeviceUpnp device) {
		return mSensorValues.get(device.getExternalID());
	}
	
	public Set<DeviceUpnp> getDevices() {
		return mDevices;
	}
	
	public void removeDevice(DeviceDescription device) {
		UPnPManager.printDebug("Removing Device");
		if (device instanceof SensorDeviceDescription) {
			Collection<Sensor> sensors = ((SensorDeviceDescription) device).getSensors();
			for (Sensor sensor : sensors) {
				mDevices.remove(sensor);
			}
		} else {
			mDevices.remove(device.getBoundDevice());
		}
	}

}
