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

package com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface;

import java.util.ArrayList;
import java.util.Iterator;

import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.IUPnPDeviceListener;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPDevice;

import android.util.Log;

/**
 * Assembles a list of available devices. For each device, the UI is interested in a data structure of SensorCollections and Sensors can be built up 
 *
 */


public class UPnPDeviceList implements IUPnPDeviceListener {
	
	private static final String TAG = "UPnPDeviceList";
	private ArrayList<UPnPDevice> mDevices = new ArrayList<UPnPDevice>();
	private UPnPDeviceListListener mListener;
	
	public interface UPnPDeviceListListener {
		public void onDeviceListChanged();
	}
	
	public void setListener(UPnPDeviceListListener listener) {
		mListener = listener;
	}
	
	public void removeListener() {
		mListener = null;
	}
	
	@Override
	public void onDeviceAdded(UPnPDevice device) {
		Log.d(TAG, "onDeviceAdded "+device.getFriendlyName());

		//make very sure it is only once in the list
		UPnPDevice registeredDevice = findUPnPDevice(device.getUDN().toString());
		if (registeredDevice==null) {
			mDevices.add(device);
		} else {
			Log.e(TAG, "Discovered device "+device.getFriendlyName()+" again.");
		}

		if (mListener!=null) mListener.onDeviceListChanged();
	}

	@Override
	public void onDeviceRemoved(String udn) {
		Log.d(TAG, "onDeviceRemoved "+udn);
		
		UPnPDevice device = findUPnPDevice(udn);
		if (device!=null) {
			mDevices.remove(device);
		} else {
			Log.e(TAG, "To be removed device with udn: "+udn+" not found.");
		}
		
		if (mListener!=null) mListener.onDeviceListChanged();
	}


	public UPnPDevice findUPnPDevice(String UDN) {
		Log.d("Temp","findUPnPDevice "+UDN);
		Iterator<UPnPDevice> it = mDevices.iterator();
		boolean found = false;
		UPnPDevice device=null;
    	while (it.hasNext()&&!found) {
    		device = it.next();
    		Log.d("Temp","find "+device.getFriendlyName()+", "+device.getUDN());

    		found = (UDN!=null) && (UDN.contentEquals(device.getUDN().getIdentifierString()));
    	}
    	
    	if (found) return device;
    	else return null;
	}
	
	public ArrayList<UPnPDevice> getDevices() {
		return mDevices;
	}
		
}
