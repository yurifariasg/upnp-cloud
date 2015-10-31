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

package com.tpvision.sensormgt.upnpcontrolpoint.clinginterface;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UDAServiceType;

import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorEventsSubscriptionCallback.EventCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DatamodelDefinitions;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DebugControlPoint;
import com.tpvision.sensormgt.upnpcontrolpoint.model.UPnPException;
import com.tpvision.sensormgt.upnpcontrolpoint.model.XMLUPnPCPParseUtil;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface;

import android.util.Log;

public class UPnPSensorMgtDevice extends UPnPDevice implements EventCallback {
	
	private static final String TAG = "UPnPSensorMgtDevice";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_UPNP;
	private static final boolean DEBUG_EVENTING = DebugControlPoint.DEBUG_EVENTING;

	public final static String ACTION_INSTANCES = "GetInstances";
	public final static String ACTION_VALUES = "GetValues";
	public final static String ACTION_READSENSOR = "ReadSensor";
	public final static String ACTION_WRITESENSOR = "WriteSensor";
	
	private Device<?, ?, ?> mDevice;
	private AndroidUpnpService mAndroidUPnPService;
	ConfigurationManagementInterfaceImpl mConfMgtInterface;
	SensorTransportGenericInterfaceImpl mSensorTransportGenericInterface;
	private ConfigurationUpdateEventListener mConfigurationUpdateEventListener;
	private DataStoreInterfaceImpl mDataStoreInterface;
	private static UPnPSensorMgtDevice mInstance;

	public interface ConfigurationUpdateEventListener {
		public void onConfigurationEventReceived(int sequencenr, String timestamp, String configurationUpdateXML);
	}

	/**
	 * Represents the UPnP Sensor Management device that was discovered on the network
	 * @param device
	 * @param androidUpnpService
	 */
	private UPnPSensorMgtDevice(Device<?, ?, ?> device, AndroidUpnpService androidUpnpService) {
		super(device);

		if (DEBUG) Log.d(TAG,"new UPnP device:" +device.getDetails().getFriendlyName());
		
		this.mDevice = device;
		this.mAndroidUPnPService = androidUpnpService; 
		
		SerialActionExecutor serialExecutor = new SerialActionExecutor();
		
		Service<?, ?> configurationMgtService = mDevice.findService(new UDAServiceType(AndroidUPnPService.TYPE_CONFIGURATIONMANAGEMENT));
		Service<?, ?> sensorTransportService = mDevice.findService(new UDAServiceType(AndroidUPnPService.TYPE_SENSORTRANSPORTGENERIC));
		Service<?, ?> dataStoreService = mDevice.findService(new UDAServiceType(AndroidUPnPService.TYPE_DATASTORE));
		
		mConfMgtInterface = new ConfigurationManagementInterfaceImpl(serialExecutor, mAndroidUPnPService, configurationMgtService);
		mSensorTransportGenericInterface = new SensorTransportGenericInterfaceImpl(serialExecutor, mAndroidUPnPService, sensorTransportService);
		if (dataStoreService!=null)
			mDataStoreInterface = new DataStoreInterfaceImpl(serialExecutor, mAndroidUPnPService, dataStoreService);
	}
	
	public static UPnPSensorMgtDevice getInstance(Device<?, ?, ?> device, AndroidUpnpService androidUpnpService) {
		if (mInstance!=null)
			return mInstance;
		else {
			if ((device!=null) && (androidUpnpService!=null)) {
				String type = device.getType().getType();
				if (DEBUG) Log.d(TAG,"UPnP device type:" +type);
				if ((type!=null) && (type.contentEquals("SensorManagement"))) {
					return new UPnPSensorMgtDevice(device, androidUpnpService);
				}
			}
		}
			
		return null;
	}
	
	public void setConfigurationUpdateEventListener(ConfigurationUpdateEventListener listener) {
		mConfigurationUpdateEventListener = listener;
	}
	
	public void removeConfigurationUpdateEventListener() {
		mConfigurationUpdateEventListener = null;
	}
	
	public void subscribeConfigurationManagementServiceEvents() {
		Log.v(TAG,"SubscribeConfigurationManagementServiceEvents()");
		mAndroidUPnPService.getControlPoint()
					.execute(new SensorEventsSubscriptionCallback(mDevice.findService(new UDAServiceId(AndroidUPnPService.TYPE_CONFIGURATIONMANAGEMENT)), this));

	}
	
	public void subscribeDataStoreEvents() {
		Log.v(TAG,"subscribeDataStoreEvents()");
		
		Service<?, ?> dataStoreService = mDevice.findService(new UDAServiceId(AndroidUPnPService.TYPE_DATASTORE));
		
		if (dataStoreService!=null) {
			mAndroidUPnPService.getControlPoint()
					.execute(new SensorEventsSubscriptionCallback(dataStoreService, this));
		}
		else
			Log.e(TAG,"DataStore service to subscribe to not found");
	}
	
	@Override
	public void onEventReceived(GENASubscription sub) {
		if (DEBUG_EVENTING) Log.d(TAG,"Event: " + sub.getCurrentSequence().getValue());		

        Map<String, StateVariableValue> values = sub.getCurrentValues();
        StateVariableValue confUpdate = values.get(DatamodelDefinitions.CONFIGURATION_UPDATE);

        if (confUpdate!=null) {
        	String[] csv = confUpdate.toString().split(",");
        	if (csv.length==3) {
        		try {
        			int sequenceNr = Integer.parseInt(csv[0]);
        			String timestamp = csv[1];
        			String configurationUpdateXML = csv[2];
        		
        			if (mConfigurationUpdateEventListener!=null) { 
        				mConfigurationUpdateEventListener.onConfigurationEventReceived(sequenceNr, timestamp, configurationUpdateXML);
        		
        			}
        		} catch (NumberFormatException nfe) {
        			Log.e(TAG,"Event format incorrect");
        		}
        	} else Log.e(TAG, "Number of CVS values: "+csv.length +" expected 3");
        }
	}
	
	//TODO: implement
	public void unsubscribeEvents() {
		
	}

	public ConfigurationManagementCPInterface getConfigurationManagement() {
		return mConfMgtInterface;
	}
	
	public SensorTransportGenericCPInterface getSensorTransport() {
		return mSensorTransportGenericInterface;
	}
	
	public DataStoreCPInterface getDataStore() {
		return mDataStoreInterface;
	}
	
	//TODO: check if we need this
	public AndroidUpnpService getAndroidUPnPService() {
		return mAndroidUPnPService;
	}


//
//	// ISensorCollection_Callback
//	@Override
//	public void onSensorCollectionChanged(SensorCollection sensorCollection) {
//		if (mSensorCollections.contains(sensorCollection)) {
//			mSensorCollections.remove(sensorCollection);
//		}
//		mSensorCollections.add(sensorCollection);
//		mCallback.onUPnPDeviceChanged(this);
//	}
//
//	public ArrayList<SensorCollection> getSensorCollections() {
//		return mSensorCollections;
//	}
//
//	public Device<?, ?, ?> getDevice() {
//		return mDevice;
//	}
	
}
