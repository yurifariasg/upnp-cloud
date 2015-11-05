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

package com.comarch.android.upnp.ibcdemo.connectivity.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.model.Source;
import com.comarch.android.upnp.ibcdemo.model.SourcedDeviceUpnp;
import com.google.common.base.Function;

public class SourcedDeviceUpnpConverterFunction implements Function<SourcedDeviceUpnp, SourcedDeviceUpnp> {

    private final String TAG = getClass().getName();
    private final EnumSet<Source> sources;
    
    public SourcedDeviceUpnpConverterFunction(EnumSet<Source> sources) {
        this.sources = sources;
    }
    
    @Override
    public SourcedDeviceUpnp apply(SourcedDeviceUpnp baseLight) {
        Class<? extends SourcedDeviceUpnp> clazz = baseLight.getClass();
        try {
            Constructor<?extends SourcedDeviceUpnp> constr = clazz.getConstructor(clazz);
            SourcedDeviceUpnp copy = constr.newInstance(baseLight);
            copy.setSources(sources);
            return copy;
        } catch (InstantiationException e) {
            Log.e(TAG,"Exception",e);
        } catch (IllegalAccessException e) {
            Log.e(TAG,"Exception",e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"Exception",e);
        } catch (InvocationTargetException e) {
            Log.e(TAG,"Exception",e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG,"Exception",e);
        }
        return null;

    }
}
