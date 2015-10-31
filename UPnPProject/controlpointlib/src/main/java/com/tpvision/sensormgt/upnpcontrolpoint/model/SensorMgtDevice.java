/* Copyright (c) 2013, TP Vision Holding B.V. 
 * All rights reserved.
 
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of TP Vision nor the  names of its contributors may
      be used to endorse or promote products derived from this software
      without specific prior written permission.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL TP VISION HOLDING B.V. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.tpvision.sensormgt.upnpcontrolpoint.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.state.StateVariableValue;

import android.util.Log;

import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPSensorMgtDevice;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorEventsSubscriptionCallback.EventCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPSensorMgtDevice.ConfigurationUpdateEventListener;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorCollection.SensorCollectionDataAvailableListener;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorCollection.SensorsChangedListener;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorInfo.SensorDataAvailableListener;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetInstances;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetSupportedDatamodels;

/** 
 * Datamodel SensorMgt Device, provides access to underlying UPnPSensorMgtDevice, and SensorCollections represented by the device
 *  
 * The UPnPSensorMgtDevice represents a discovered UPnP device on which actions can be invoked.
 * The UPnPSensorMgtDevice object provides access to the Actions of the ConfigurationMgt Service, the SensorTransport Generic Service,
 * and if present the DataStore Service
 * 
 * The SensorMgtDevice object can be used to get information about SensorCollections and Sensors from the SensorManagement device in the network.
 */


public class SensorMgtDevice implements GetSupportedDatamodels, GetInstances, SensorsChangedListener, 
									SensorCollectionDataAvailableListener, SensorDataAvailableListener, ConfigurationUpdateEventListener {
	
	private static final String TAG = "SensorMgtDevice";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_MODEL;
	private static final boolean DEBUG_EVENTING = DebugControlPoint.DEBUG_EVENTING;
	
	private ArrayList<SensorCollection> mSensorCollections = new ArrayList<SensorCollection>();
	
	private UPnPSensorMgtDevice mUPnPDevice;
	private ConfigurationManagementCPInterface mConfMgtInterface;
	private SensorTransportGenericCPInterface mSensorTransportInterface;
	private DataStoreCPInterface mDataStoreInterface;
	
	private SensorCollectionsChangedListener mSensorCollectionListener;
	private SensorsChangedListener mSensorsListener;
	private SensorCollectionDataAvailableListener mSensorCollectionDataListener;
	private SensorDataAvailableListener mSensorDataListener;


	public interface SensorCollectionsChangedListener {
		public void onSensorCollectionsChanged(SensorMgtDevice device);
	}
	
	
	public SensorMgtDevice(UPnPSensorMgtDevice upnpDevice) {
		mUPnPDevice = upnpDevice;
		mUPnPDevice.setConfigurationUpdateEventListener(this);
		mConfMgtInterface = upnpDevice.getConfigurationManagement();
		mSensorTransportInterface = upnpDevice.getSensorTransport();
		mDataStoreInterface = upnpDevice.getDataStore();
	}
	
	/**
	 * Notify listener when a new SensorCollection Instance is available or was removed
	 */
	public void setSensorCollectionChangedListener(SensorCollectionsChangedListener listener) {
		if (DEBUG_EVENTING) Log.d(TAG,"setSensorCollectionChangedListener");
		mSensorCollectionListener = listener;
	}
	
	public void removeSensorCollectionChangedListener() {
		if (DEBUG_EVENTING) Log.d(TAG,"removeSensorCollectionChangedListener");
		mSensorCollectionListener = null;
	}
	
	/**
	 * Notify listener when a new Sensors Instance is available or was removed from a collection
	 */
	public void setSensorsChangedListener(SensorsChangedListener listener) {
		if (DEBUG_EVENTING) Log.d(TAG,"setSensorsChangedListener");
		mSensorsListener = listener;
	}
	
	public void removeSensorsChangedListener() {
		if (DEBUG_EVENTING) Log.d(TAG,"removeSensorsChangedListener");
		mSensorsListener = null;
	}
	
	/**
	 * Notify listener when a SensorCollection data is available
	 */
	public void setSensorCollectionDataAvailableListener(SensorCollectionDataAvailableListener listener) {
		if (DEBUG_EVENTING) Log.d(TAG,"setSensorCollectionDataAvailableListener");
		mSensorCollectionDataListener = listener;
	}
	
	public void removeSensorCollectionDataAvailableListener() {
		if (DEBUG_EVENTING) Log.d(TAG,"removeSensorCollectionDataAvailableListener");
		mSensorCollectionDataListener = null;
	}
	
	/**
	 * Notify listener when Sensor data is available
	 */
	public void setSensorDataAvailableListener(SensorDataAvailableListener listener) {
		if (DEBUG_EVENTING) Log.d(TAG,"setSensorDataAvailableListener");
		mSensorDataListener = listener;
	}
	
	public void removeSensorDataAvailableListener() {
		if (DEBUG_EVENTING) Log.d(TAG,"removeSensorDataAvailableListener");
		mSensorDataListener = null;
	}
	
	
	/**
	 * signals that the number of SensorCollection instances changed
	 */
	public void onSensorCollectionsChanged(SensorMgtDevice sensorMgtDevice) {
		if (DEBUG_EVENTING) Log.d(TAG,"onSensorCollectionChanged");
		if (mSensorCollectionListener != null) mSensorCollectionListener.onSensorCollectionsChanged(sensorMgtDevice);
	}
	
	/**
	 * SensorCollection signals that the number of sensor instances changed
	 */
	@Override
	public void onSensorsChanged(SensorCollection sensorCollection) {
		if (DEBUG_EVENTING) Log.d(TAG,"onSensorsChanged");
		if (mSensorsListener!=null) mSensorsListener.onSensorsChanged(sensorCollection);
	}
	
	/**
	 * SensorCollection signals that new meta-data is available
	 */
	@Override
	public void onSensorCollectionDataAvailable(SensorCollection sensorCollection) {
		if (DEBUG_EVENTING) Log.d(TAG,"onSensorCollectionDataAvailable "+sensorCollection.getPath());
		if (mSensorCollectionDataListener!=null) mSensorCollectionDataListener.onSensorCollectionDataAvailable(sensorCollection);
	}
	
	/**
	 * Sensor signals that new meta-data is available
	 */
	@Override
	public void onSensorDataAvailable(SensorInfo sensorNode) {
		if (DEBUG_EVENTING) Log.d(TAG,"onSensorCollectionDataAvailable");
		if (mSensorDataListener!=null) mSensorDataListener.onSensorDataAvailable(sensorNode);
	}

	
	
	public List<SensorCollection> getSensorCollections() {
		return mSensorCollections;
	}
	
	public ConfigurationManagementCPInterface getConfigurationManagement() {
		return mConfMgtInterface;
	}
	
	public SensorTransportGenericCPInterface getSensorTransport() {
		return mSensorTransportInterface;
	}
	
	public DataStoreCPInterface getDataStore() {
		return mDataStoreInterface;
	}
	
	
	public void getSupportedDatamodels() {
		mConfMgtInterface.getSupportedDatamodels(this);
	}
	
	private void getInstances(String startingNodePath, int searchDepth) {
		if (DEBUG) Log.d(TAG,"getInstances("+startingNodePath+", "+searchDepth+")");
		mConfMgtInterface.getInstances(startingNodePath,  searchDepth, this);
	}
	
	public void updateSensorCollections() {
		getInstances("/UPnP/SensorMgt/SensorCollections/", 1);
		
	}

	@Override
	public void onGetInstances(String instancesXml) {
		if (DEBUG) Log.d(TAG,"onGetInstances("+instancesXml+")");
		ArrayList<String> collectionsPaths;
		try {
			collectionsPaths = (ArrayList<String>) XMLUPnPCPParseUtil.parseInstancePathList(instancesXml);
			for (int i = 0; i < collectionsPaths.size(); i++) {
				mSensorCollections.add(new SensorCollection(this, collectionsPaths.get(i)));
			}
			
			onSensorCollectionsChanged(this);
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void subscribeConfigurationManagementServiceEvents() {
		if (DEBUG_EVENTING) Log.i(TAG,"subscribeEvents");
		 mUPnPDevice.subscribeConfigurationManagementServiceEvents();
	}
	
	public void subscribeDataStoreEvents() {
		if (DEBUG_EVENTING) Log.i(TAG,"subscribeEvents");
		 mUPnPDevice.subscribeDataStoreEvents();
	}
	
	
	public void unsubscribeEvents() {
		if (DEBUG_EVENTING) Log.i(TAG,"unsubscribeEvents");
		mUPnPDevice.unsubscribeEvents();
	}
	
	@Override
	public void onConfigurationEventReceived(int sequencenr, String timestamp,
			String configurationUpdateXML) {
		if (DEBUG_EVENTING) Log.i(TAG,"onConfigurationEventReceived");
		
		HashMap<String, String> parameterValues;
		try {
			parameterValues = XMLUPnPCPParseUtil.parseParameterValuesList(configurationUpdateXML);
			
			if (parameterValues.containsKey(DatamodelDefinitions.SENSOREVENTS_PATH)) {
				//It's a SensorEvent 
				ArrayList<SensorEventInfo> sensorEvents =  XMLUPnPCPParseUtil.parseSensorEvents(parameterValues.get(DatamodelDefinitions.SENSOREVENTS_PATH));
			
				for (SensorEventInfo sensorEvent: sensorEvents) {
					SensorCollection sensorCollection = findSensorCollection(sensorEvent.getCollectionId());
					if (sensorCollection!=null) {
						if (DEBUG_EVENTING) Log.d(TAG,"Event found sensorCollection "+ sensorCollection.getFriendlyName());
						Sensor sensor = sensorCollection.findSensor(sensorEvent.getSensorId());
						if (sensor!=null) {
							if (DEBUG_EVENTING) Log.d(TAG,"Event found sensor "+ sensor.getType());
							sensor.updateDataItemValues(); //since we do not know what URN and dataitem changed, update all 
						}
					}
					
					//;
					
				}
				
			}
			
			//TODO: handle other parameter values
			
			
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public SensorCollection findSensorCollection(String sensorCollectionID) {
		
		if (DEBUG_EVENTING) Log.d(TAG,"findSensorCollection: "+sensorCollectionID);
		
		int i=0;
		boolean found=false;
		SensorCollection sensorCollection=null;
		while (i<mSensorCollections.size() && !found) {
			sensorCollection = mSensorCollections.get(i);
			found = sensorCollection.getSensorCollectionId().contentEquals(sensorCollectionID);
			i++;
		}
		
		if (found) return sensorCollection;
		else return null;
		
	}
	
	
	@Override
	synchronized public void onSupportedDatamodels(String datamodelsXml) {
		// TODO Auto-generated method stub
		
	}

	
	
	
}