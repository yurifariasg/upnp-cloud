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
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;



import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.ConnectSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.DisconnectSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.GetSensorTransportConnectionsCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.ReadSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.WriteSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface;


public class SensorTransportGenericInterfaceImpl implements SensorTransportGenericCPInterface {

	
	private static final String ACTION_CONNECTSENSOR = "ConnectSensor";
	private static final String ACTION_DISCONNECTSENSOR = "DisconnectSensor";
	private static final String ACTION_READSENSOR = "ReadSensor";
	private static final String ACTION_WRITESENSOR = "WriteSensor";
	private static final String ACTION_GETTRANSPCONN = "GetSensorTransportConnections";
	
	private Service<?, ?> mService;
	private AndroidUpnpService mAndroidUpnpService;
	private SensorTransportGenericCallbacks mSensorTransportGenericCallbacks;
	private SerialActionExecutor mSerialExecutor;
	
	public SensorTransportGenericInterfaceImpl(SerialActionExecutor executor, AndroidUpnpService androidUpnpService, Service<?, ?> confMgtService) {
		mAndroidUpnpService = androidUpnpService;
		mService = confMgtService;
		mSensorTransportGenericCallbacks = new SensorTransportGenericCallbacks();
		mSerialExecutor = executor;
	}
	
	@Override
	public String connectSensor(String sensorID, String sensorClientID,
			String sensorURN, String sensorRecordInfo,
			Boolean sensorDataTypeEnable, String transportURL,
			ConnectSensor callback) {
		
		Action lAction = mService.getAction(ACTION_CONNECTSENSOR);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("SensorID", sensorID);
			lInvocation.setInput("SensorClientID", sensorClientID);
			lInvocation.setInput("SensorURN", sensorURN);
			lInvocation.setInput("SensorRecordInfo", sensorRecordInfo);
			lInvocation.setInput("SensorDataTypeEnable", sensorDataTypeEnable);
			lInvocation.setInput("TransportURL", transportURL);

			if (callback!=null) {
				ConnectSensorCallback lCallback = mSensorTransportGenericCallbacks.new ConnectSensorCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			}
			else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();
				
				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp
				
				return lInvocation.getOutput()[0].getValue().toString();
			}
		}

		return null;
	}

	@Override
	public String disconnectSensor(String sensorID, String transportURL,
			String transportConnectionID, DisconnectSensor callback) {
		
		Action lAction = mService.getAction(ACTION_DISCONNECTSENSOR);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("SensorID", sensorID);
			lInvocation.setInput("TransportURL", transportURL);
			lInvocation.setInput("TransportConnectionID", transportConnectionID);

			if (callback!=null) {
				DisconnectSensorCallback lCallback = mSensorTransportGenericCallbacks.new DisconnectSensorCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();
				
				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp
				
				return lInvocation.getOutput()[0].getValue().toString();
			}
		}
		
		return null;
	}

	@Override
	public String readSensor(String sensorID, String sensorClientID,
			String sensorURN, String sensorRecordInfo, boolean dataTypeEnable, int dataRecordCount,
			ReadSensor callback) {
		
		Action lAction = mService.getAction(ACTION_READSENSOR);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("SensorID", sensorID);
			lInvocation.setInput("SensorClientID", sensorClientID);
			lInvocation.setInput("SensorURN", sensorURN);
			lInvocation.setInput("SensorRecordInfo", sensorRecordInfo);
			lInvocation.setInput("SensorDataTypeEnable", dataTypeEnable);
			lInvocation.setInput("DataRecordCount", new UnsignedIntegerFourBytes(dataRecordCount));

			if (callback!=null) {
				ReadSensorCallback lCallback = mSensorTransportGenericCallbacks.new ReadSensorCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();
				
				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp
				
				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		
		return null;
	}

	@Override
	public String writeSensor(String sensorID, String sensorURN,
			String dataRecords, WriteSensor callback) {
		
		Action lAction = mService.getAction(ACTION_WRITESENSOR);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			lInvocation.setInput("SensorID", sensorID);
			lInvocation.setInput("SensorURN", sensorURN);
			lInvocation.setInput("DataRecords", dataRecords);

			if (callback!=null) {

				WriteSensorCallback lCallback = mSensorTransportGenericCallbacks.new WriteSensorCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();
				
				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp
				
				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		
		return null;
	}
	
	@Override
	public String getSensorTransportConnections(String sensorID, GetSensorTransportConnections callback) {
		
		Action lAction = mService.getAction(ACTION_GETTRANSPCONN);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("SensorID", sensorID);

			if (callback!=null) {
				GetSensorTransportConnectionsCallback lCallback = mSensorTransportGenericCallbacks.new GetSensorTransportConnectionsCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();
				
				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp
				
				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		
		return null;
	}
	
}