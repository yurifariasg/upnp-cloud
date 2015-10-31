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

package upnp.controlpoint.fridge;

import android.util.Log;

import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPDevice;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPSensorMgtDevice;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorCollection.SensorCollectionDataAvailableListener;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorMgtDevice;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorMgtDevice.SensorCollectionsChangedListener;


/** stores UPnP device info for display in  the user interface,
 *  for devices for which the user indicated that it will be used, the a UPnPDevice is created in the Datamodel, and its Sensor information is retrieved
 * 
 */

public class UPnPDeviceInfo {
	
	private static final String TAG = "UPnPDeviceInfo";
	private String friendlyName;
	private String UDN;
	private boolean use=false;
	private boolean found=false;
	private UPnPDevice mUPnPDevice;
	private SensorMgtDevice mSensorMgtDevice;
	private SensorCollectionsChangedListener mSensorCollectionChangedListener;
	private SensorCollectionDataAvailableListener mSensorCollectionDataAvailable;

//FIXME: deal with found & not found	
	
	public UPnPDeviceInfo(String UDN, String friendlyName, boolean found, boolean use) {
		this.friendlyName = friendlyName;
		this.setUDN(UDN);
		this.found = found;
		setUse(use);
	}
	
	public UPnPDeviceInfo(String UDN, String friendlyName) {
		this(UDN, friendlyName, true, false);
	}
	
	public UPnPDeviceInfo(UPnPDevice upnpDevice) {
		if (upnpDevice!=null) {
			this.friendlyName = upnpDevice.getFriendlyName();
			this.UDN = upnpDevice.getUDN().getIdentifierString();
			found=true;
			mUPnPDevice = upnpDevice;
		}
	}
	
	public void setUPnPDevice(UPnPDevice upnpDevice) {
		mUPnPDevice = upnpDevice;
	}

	public void setSensorCollectionChangedListener(SensorCollectionsChangedListener listener) {
		mSensorCollectionChangedListener = listener;
		enableSensorCollectionChangedListener(use);
	}
	
	public void setSensorCollectionDataAvailableListener(SensorCollectionDataAvailableListener listener) {
		mSensorCollectionDataAvailable = listener;
		enableSensorCollectionDataAvailableListener(use);
	}
	
	//TODO: event listener
	
	/**
	 * Build the datamodel for this device by requesting sensorcollections, sensors, sensorURNs, and dataItems information from the upnp device
	 */
	public void populateDatamodel() {
		if (isUsed() && isFound()) {
			if (mUPnPDevice!=null) {
				Log.d(TAG,"found and used, populate the data model: ");
				if (mSensorMgtDevice==null) { //do not recreate, e.g. when user toggles use
					mSensorMgtDevice = new SensorMgtDevice((UPnPSensorMgtDevice) mUPnPDevice);
					enableSensorCollectionChangedListener(true);
					enableSensorCollectionDataAvailableListener(true);
					mSensorMgtDevice.updateSensorCollections();
				}
			}
		}
	}
	
	private void enableSensorCollectionChangedListener(boolean enable) {
		if (mSensorMgtDevice!=null) {
			if (enable) {
				mSensorMgtDevice.setSensorCollectionChangedListener(mSensorCollectionChangedListener);
			} else {
				mSensorMgtDevice.removeSensorCollectionChangedListener();
			}
				
				
		}
	}
	
	private void enableSensorCollectionDataAvailableListener(boolean enable) {
		if (mSensorMgtDevice!=null) {
			if (enable) {
				mSensorMgtDevice.setSensorCollectionDataAvailableListener(mSensorCollectionDataAvailable);
			} else {
				mSensorMgtDevice.removeSensorCollectionDataAvailableListener();
			}
				
				
		}
	}
	
	//TODO: change to private
	public void enableEventSubscription(boolean enable) {
		Log.e(TAG,"enableEventSubscription");
		if (mSensorMgtDevice!=null) {
			if (enable) {
				Log.e(TAG,"enableEventSubscription");
				mSensorMgtDevice.subscribeConfigurationManagementServiceEvents();
			} else {
				mSensorMgtDevice.unsubscribeEvents();
			}
		}
	}
	
	/**
	 * used to access the root of the datamodel, if the sensorManagement device is not set it is created
	 * @return sensormgt device representation
	 */
	public SensorMgtDevice getsensorMgtDevice() {
		
		if (mSensorMgtDevice==null) { //do not recreate, e.g. when user toggles use
			mSensorMgtDevice = new SensorMgtDevice((UPnPSensorMgtDevice) mUPnPDevice);
		}
		
		return mSensorMgtDevice;
	}
	
	public void setUse(boolean use) {
		this.use = use;
	
		enableSensorCollectionChangedListener(use); 
		enableSensorCollectionDataAvailableListener(use);
		enableEventSubscription(use);
	//	populateDatamodel();
	}
	
	public boolean isUsed() {
		return use;
	}
	
	public void toggleUse() {
		use = !use;
		enableSensorCollectionChangedListener(use); 
		enableSensorCollectionDataAvailableListener(use);
	//	populateDatamodel();
		enableEventSubscription(use);
	}
	
	public void setfound(boolean found) {
		this.found = found;
		enableSensorCollectionChangedListener(use); 
		enableSensorCollectionDataAvailableListener(use);
	//	populateDatamodel();
		enableEventSubscription(use);
	}
	
	public boolean isFound() {
		return found;
	}
	
	public String getUDN() {
		return UDN;
	}

	public void setUDN(String uDN) {
		UDN = uDN;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String toString() {
		return friendlyName;
	}
	
}