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

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class UPnPServiceConnection implements ServiceConnection {

	public interface IServiceConnection_Callback {
		public void onServiceConnected();
	}

	private AndroidUpnpService mUPnPService = null;
	private UPnPRegisteryListener mRegistrationListener;
	private IServiceConnection_Callback mListener;

	public UPnPServiceConnection(UPnPRegisteryListener registrationListener) {
		mRegistrationListener = registrationListener;
	}
	
	public void setListener(IServiceConnection_Callback listener) {
		mListener = listener;
	}
	
	public void onServiceConnected(ComponentName className, IBinder service) {
		
		Log.d("UPnPServiceConnection","onServiceConnected");
		
		mUPnPService = (AndroidUpnpService) service;

		for (Device<?, ?, ?> device : mUPnPService.getRegistry().getDevices()) {
			mRegistrationListener.deviceAdded(device);
		}

		mUPnPService.getRegistry().addListener(mRegistrationListener);
		
		mUPnPService.getControlPoint().search();
	//	mListener.onServiceConnected();
	}

	public void onServiceDisconnected(ComponentName className) {
		if (mUPnPService != null)
			mUPnPService.getRegistry().removeListener(mRegistrationListener);

		mUPnPService = null;
	}

	public AndroidUpnpService getUPnPService() {
		return mUPnPService;
	}
}
