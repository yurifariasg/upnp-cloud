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

import org.fourthline.cling.binding.annotations.*;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import com.tpvision.sensormgt.datamodel.DatamodelInterfaceImpl;
import com.tpvision.sensormgt.datamodel.DebugDatamodel;
import com.tpvision.sensormgt.datamodel.UPnPException;

import android.util.Log;

@UpnpService(serviceId = @UpnpServiceId("SensorTransportGeneric"), serviceType = @UpnpServiceType(value = "SensorTransportGeneric", version = 1))

@UpnpStateVariables({ 
		@UpnpStateVariable(name = "A_ARG_TYPE_SensorID", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_SensorClientID", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_SensorURN", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_SensorDataTypeEnable", datatype = "boolean", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_TransportURL", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_TransportConnectionID", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_SensorRecordInfo", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecordCount", datatype = "ui4", defaultValue = "0", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecords", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_TransportConnections", datatype = "string", sendEvents = false)
		})


public class SensorTransportGeneric {

	private static final String TAG = "SensorTransportGeneric";
	private static final boolean DEBUG = DebugDatamodel.DEBUG_UPNP;
	private DatamodelInterfaceImpl mDatamodelInterface = null;

	public SensorTransportGeneric() {

	}

	public void setDatamodel(DatamodelInterfaceImpl datamodelInterfaceImpl) {
		mDatamodelInterface = datamodelInterfaceImpl;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "DataRecords"))
	public String readSensor(@UpnpInputArgument(name = "SensorID") String sensorID, 
			@UpnpInputArgument(name = "SensorClientID") String sensorClientID,
			@UpnpInputArgument(name = "SensorURN") String sensorURN, 
			@UpnpInputArgument(name = "SensorRecordInfo") String sensorRecordInfo,
			@UpnpInputArgument(name = "SensorDataTypeEnable") boolean sensorDataTypeEnable, 
			@UpnpInputArgument(name = "DataRecordCount") UnsignedIntegerFourBytes dataRecordCount) throws SensorMgtException {

		String datarecords = "";
		if (DEBUG)
			Log.d(TAG, "readSensor(\"" + sensorID + "\", \"" + sensorClientID + "\", \"" + sensorURN + "\", \"" + sensorRecordInfo + "\", \"" + dataRecordCount
					+ "\") = " + datarecords);

		try {
			datarecords = mDatamodelInterface.readSensor(sensorID, sensorClientID, sensorURN, sensorRecordInfo, sensorDataTypeEnable, dataRecordCount.getValue().intValue());
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage()); 
		}

		return datarecords;
	}

	@UpnpAction
	public void writeSensor(@UpnpInputArgument(name = "SensorID") String sensorID, @UpnpInputArgument(name = "SensorURN") String sensorURN,
			@UpnpInputArgument(name = "DataRecords") String dataRecords) throws SensorMgtException {

		if (DEBUG)
			Log.d(TAG, "writeSensor(\"" + sensorID + "\", \"" + dataRecords + "\")");
		try {
			if (mDatamodelInterface != null)
				mDatamodelInterface.writeSensor(sensorID, sensorURN, dataRecords);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage()); 
		}
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "TransportConnectionID"))
	public String connectSensor(@UpnpInputArgument(name = "SensorID") String sensorID, 
			@UpnpInputArgument(name = "SensorClientID") String sensorClientID, 
			@UpnpInputArgument(name = "SensorURN") String sensorURN, 
			@UpnpInputArgument(name = "SensorRecordInfo") String sensorRecordInfo, 
			@UpnpInputArgument(name = "SensorDataTypeEnable") boolean sensorDataTypeEnable, 
			@UpnpInputArgument(name = "TransportURL") String transportURL) throws SensorMgtException {
	
		String connID="";
		
		if (DEBUG)
			Log.d(TAG, "connectSensor(\"" + sensorID + "\", \"" + sensorClientID + "\", \"" + sensorURN + "\", \"" + 
					sensorRecordInfo + "\", \"" + sensorDataTypeEnable + "\", \"" + transportURL + "\")");
		
		try {
			connID = mDatamodelInterface.connectSensor(sensorID, sensorClientID, sensorURN, sensorRecordInfo, sensorDataTypeEnable, transportURL);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage()); 
		}
		
		return connID;
	}

	@UpnpAction
	public void disconnectSensor(@UpnpInputArgument(name = "SensorID") String sensorID, 
			@UpnpInputArgument(name = "TransportURL") String transportURL,
			@UpnpInputArgument(name = "TransportConnectionID") String transportConnectionID) throws SensorMgtException {
		
		if (DEBUG)
			Log.d(TAG, "disconnectSensor(\"" + sensorID + "\", \"" + transportURL + "\", \"" + transportConnectionID + "\")");
		
		try {
			mDatamodelInterface.disconnectSensor(sensorID, transportURL, transportConnectionID);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage()); 
		}
		
		
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "TransportConnections"))
	public String getSensorTransportConnections(@UpnpInputArgument(name = "SensorID") String sensorID) throws SensorMgtException {
		
		if (DEBUG)
			Log.d(TAG, "getSensorTransportConnections(\"" + sensorID + "\")");
		
		String connections = "";
		try {
			connections = mDatamodelInterface.getSensorTransportConnections(sensorID);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage()); 
		}
		
		return connections;
	}

	
	
}