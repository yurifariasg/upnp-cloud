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



public interface SensorTransportGenericCPInterface {
	
	public String connectSensor(String sensorID, String sensorClientID, String sensorURN,
								String sensorRecordInfo, Boolean sensorDataTypeEnable, String transportURL, ConnectSensor callback);

	public String disconnectSensor(String sensorID, String transportURL, String transportConnectionID, DisconnectSensor callback);
		
	public String readSensor(String sensorID, String sensorClientID, String sensorURN, String sensorRecordInfo, boolean sensorDataTypeEnable, int dataRecordCount, ReadSensor callback);
	
	public String writeSensor(String sensorID, String sensorURN, String dataRecords, WriteSensor callback);
	
	public String getSensorTransportConnections(String sensorID, GetSensorTransportConnections callback); 
	
	public interface ConnectSensor {	
		public void onConnectSensor(String transportConnectionID);
	}
	
	public interface DisconnectSensor {	
		public void onDisconnectSensor();
	}
	
	public interface ReadSensor {	
		public void onReadSensor(String DataRecordsXml);
	}
	
	public interface WriteSensor {	
		public void onWriteSensor();
	}
	
	public interface GetSensorTransportConnections {
		public void onGetSensorTransportConnections(String transportConnectionsXml);
	}
	
}