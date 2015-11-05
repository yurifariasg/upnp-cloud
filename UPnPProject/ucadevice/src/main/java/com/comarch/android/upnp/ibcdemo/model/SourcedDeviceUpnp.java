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

package com.comarch.android.upnp.ibcdemo.model;

import com.comarch.android.upnp.ibcdemo.model.interfaces.ISourcedDeviceUpnp;

import java.util.EnumSet;

public class SourcedDeviceUpnp extends DeviceUpnp implements  ISourcedDeviceUpnp{

    private EnumSet<Source> sources;

    public SourcedDeviceUpnp(SourcedDeviceUpnp device){
        super(device);
        sources = device.sources;
    }
    public SourcedDeviceUpnp(String uuid,String name) {
        super(uuid,name);
        sources = EnumSet.noneOf(Source.class);
    }

    public EnumSet<Source> getSources() {
        return sources;
    }

    public void setSources(EnumSet<Source> sources) {
        this.sources = sources;
    }

    public boolean isAvailable() {
        return !getSources().isEmpty();
    }
    
    @Override
    public String toString() {
        return "SourcedDeviceUpnp [sources=" + sources + ", uuid=" + getUuid()  + ", name=" + getName() + "]";
    }

}
