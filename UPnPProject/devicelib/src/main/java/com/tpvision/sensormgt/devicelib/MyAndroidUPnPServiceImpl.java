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

package com.tpvision.sensormgt.devicelib;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidRouter;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;

import android.net.wifi.WifiManager;

/** 
 * Created to override the default UPnPServiceImpl, and alter AndroidUpnpServiceConfiguration
 * The MyUPnPServiceImpl implements GENA subscription callbacks to be notified when a control point subscribes to our device
 *
 */

public class MyAndroidUPnPServiceImpl extends AndroidUpnpServiceImpl {

	/**
     * Starts the UPnP service.
     */
    @Override
    public void onCreate() {

        upnpService = new MyUPnPServiceImpl(createConfiguration()) {

            @Override
            protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
                return MyAndroidUPnPServiceImpl.this.createRouter(
                		getConfiguration(),
                    protocolFactory,
                    MyAndroidUPnPServiceImpl.this
                );
            }

            @Override
            public synchronized void shutdown() {
                // First have to remove the receiver, so Android won't complain about it leaking
                // when the main UI thread exits.
                ((AndroidRouter)getRouter()).unregisterBroadcastReceiver();

                // Now we can concurrently run the Cling shutdown code, without occupying the
                // Android main UI thread. This will complete probably after the main UI thread
                // is done.
                super.shutdown(true);
            }
        };
    }
    
    @Override
    protected AndroidUpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration() {

            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return 7000;
            }

            /**
		     * @return Defaults to zero, disabling ALIVE flooding.
		     */
		    public int getAliveIntervalMillis() {
		    	return 30000; //let's do 30 seconds instead of 30 Minutes
		    }
		    
        };
    }
    
    
    
}