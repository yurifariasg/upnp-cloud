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

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import android.util.Log;

public class UPnPRegisteryListener extends DefaultRegistryListener {

	public interface IUPnPRegistryListener {
		public void onDeviceAdded(Device<?, ?, ?> device);

		public void onDeviceRemoved(Device<?, ?, ?> device);
	}

	private IUPnPRegistryListener mListener;

	public void setListener(IUPnPRegistryListener listener) {
		Log.d("UPnPRegistry","setListener");
		mListener = listener;
	}
	
	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
		deviceAdded(device); //TODO: check correctness
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
		deviceRemoved(device); //TODO: check correctness
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		deviceAdded(device);
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		deviceRemoved(device);
	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice device) {
		deviceAdded(device);
	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device) {
		deviceRemoved(device);
	}

	public void deviceAdded(final Device<?, ?, ?> device) {
		Log.d("UPnPRegistry","Device added");
		if (mListener!=null && device.isFullyHydrated()) mListener.onDeviceAdded(device);
		//if (mListener!=null) mListener.onDeviceAdded(device);
	}

	public void deviceRemoved(final Device<?, ?, ?> device) {
		if (mListener!=null) mListener.onDeviceRemoved(device);
	}
}
