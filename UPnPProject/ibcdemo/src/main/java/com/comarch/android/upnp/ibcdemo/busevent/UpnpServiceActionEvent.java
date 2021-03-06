/**
 *
 * Copyright 2013-2014 UPnP Forum All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE OR WARRANTIES OF 
 * NON-INFRINGEMENT, ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, by the UPnP Forum.
 *
 **/
package com.comarch.android.upnp.ibcdemo.busevent;

import java.util.HashMap;
import java.util.Map;

import com.comarch.android.upnp.ibcdemo.connectivity.common.UpnpActionCallback;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;

public class UpnpServiceActionEvent {
	
	private SourcedDeviceUpnp device;
	private String serviceName;
    private String actionName;
    private Map<String,String> args;
    private UpnpActionCallback callback;
    private boolean sentToAccompanying;
    
	public UpnpServiceActionEvent(SourcedDeviceUpnp device, String serviceName, String actionName, Map<String, String> args, UpnpActionCallback callback) {
        this.device = device;
		this.serviceName = serviceName;
		this.actionName = actionName;
        if(args==null){
            this.args = new HashMap<String,String>();
        }else{
            this.args = args;
        }
        this.callback = callback;
        this.sentToAccompanying = false;
	}

	public void setToAccompanying(boolean value) {
		this.sentToAccompanying = value;
	}
	
	public boolean getToAccompanying() {
		return this.sentToAccompanying;
	}
	
	public SourcedDeviceUpnp getDevice() {
		return device;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getActionName() {
		return actionName;
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public UpnpActionCallback getCallback() {
		return callback;
	}
	
	public void setCallback(UpnpActionCallback callback){
		this.callback = callback;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(" [");
		sb.append("deviceUuid:"+device.getUuid());
		sb.append(", serviceName:"+serviceName);
		sb.append(", actionName:"+actionName);
		sb.append(", args:"+args);
		sb.append("]");
		return sb.toString();
	}
}
