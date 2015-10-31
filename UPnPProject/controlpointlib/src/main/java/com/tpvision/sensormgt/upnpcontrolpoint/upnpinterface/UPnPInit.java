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

import android.content.Context;

import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ClingUPnPInit;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPDevice;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.UPnPDeviceList.UPnPDeviceListListener;

/**
 * 
 * Sets up the UPnP stack
 * Uses the cling library to setup an Android service. 
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

public class UPnPInit {

	private boolean mInitialised=false;
	ClingUPnPInit clingInit;
	private UPnPDeviceList mDevices;
	
	public boolean Init(Context context, String iPaddress, int i) {
		
		clingInit = new ClingUPnPInit();
		clingInit.InitService(context);
		mDevices = new UPnPDeviceList();
		clingInit.setUPnPDeviceListener(mDevices);
		
		mInitialised = true;
		
		return true;

	}
	
	public void stopUPnP () {
		clingInit.stopUPnP();
	}

	public ArrayList<UPnPDevice> getDevices() {
		return mDevices.getDevices();
	}
	
	public void setDeviceListListener(UPnPDeviceListListener listener) {
		mDevices.setListener(listener);
	}
	
	public void removeDeviceListListener() {
		mDevices.removeListener();
	}
	
	public void search() {
		if (clingInit!=null) clingInit.search();
	}
	
	public boolean isInitialized() {
		
		return mInitialised;
	}

	public String getHostIPaddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getHostPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void Deinit() {
		// TODO Auto-generated method stub
		
	}

	

}
