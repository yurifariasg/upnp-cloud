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

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;

import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPRegisteryListener.IUPnPRegistryListener;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * Sets up the UPnP stack; Uses the cling library to setup an Android service. 
 * 
 * upnp interface receives OnDevice added and onDevice removed calls to maintain a list of UPnP devices (sensormgt, and basic)
 * 
 * UI setup determines which devices the user is interested in.
 * For each device, the number of sensorsCollections is requested. (by invoking get instances)
 * ..needs sensorcollections changed call.  
 *
 * UI is shown. 
 * (On demand?) additional info per sensorCollection is requested (num sensors, urns, data items)
 *
 */

public class ClingUPnPInit implements IUPnPRegistryListener {

	
	private static final String TAG = "ClingUPnPInit";
	private static UPnPServiceConnection mServiceConnection;
	private static IUPnPDeviceListener mUPnPDeviceListener;
	private Context mContext;	

	public void InitService(Context context) {
		
		Log.i("ClingUPnPInit","Initialising ClingUPnP Service");
		///UPnPServiceConnection is called when the UPnP service connects or disconnects
		//and sets up a registry listener to be notified when new upnp devices are discovered or upnp devices disappear from the network
		UPnPRegisteryListener registrationListener = new UPnPRegisteryListener();
		registrationListener.setListener(this);
		mServiceConnection = new UPnPServiceConnection(registrationListener);
		
		//Start the Android Service that implements the Cling UPnP stack and bind to it 
		//Service_UPnPBrowser class checks which android service we are interested in
		
		Intent intent = new Intent(); //new Intent(context, Service_UPnPBrowser.class)
		intent.setComponent(new ComponentName(context.getPackageName(),"com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.AndroidUPnPService"));
		context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
		mContext = context;
	}
		
	public void stopUPnP() {
		if (mServiceConnection!=null) {
			mContext.unbindService(mServiceConnection);
		}
	}
	
	public void setUPnPDeviceListener(IUPnPDeviceListener listener) {
		mUPnPDeviceListener = listener;
	}
	
	public void search() {
		Log.i(TAG, "Sending UPnP M-Search to discover devices");
		if (mServiceConnection!=null) 
			if (mServiceConnection.getUPnPService()!=null) 
					mServiceConnection.getUPnPService().getControlPoint().search();
	}
	
	

	//IUPnPDeviceListener
	public void onDeviceAdded(Device<?, ?, ?> device) {
		Log.i(TAG,"UPnP Device discovered: "+device.getDisplayString());
		if (device.isFullyHydrated()) {
			AndroidUpnpService upnpService = mServiceConnection.getUPnPService();
			UPnPDevice upnpDevice = UPnPSensorMgtDevice.getInstance(device, upnpService);
			if (upnpDevice == null) {
				Log.e(TAG, "Ignored upnp device, don't know how to hanlde this type");
			}
			else
				if (mUPnPDeviceListener!=null) mUPnPDeviceListener.onDeviceAdded(upnpDevice);
		}
	}

	//IUPnPDeviceListener
	public void onDeviceRemoved(Device<?, ?, ?> device) {
		if (mUPnPDeviceListener!=null) mUPnPDeviceListener.onDeviceRemoved(device.getIdentity().getUdn().getIdentifierString());
	}

}
