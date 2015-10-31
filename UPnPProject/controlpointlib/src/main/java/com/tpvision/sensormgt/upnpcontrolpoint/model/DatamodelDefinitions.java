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

package com.tpvision.sensormgt.upnpcontrolpoint.model;

//TODO: check if we need to read a datamodel definition

public class DatamodelDefinitions {
	
	public static final String COLLECTION_ID = "CollectionID";
	public static final String COLLECTION_TYPE = "CollectionType";
	public static final String COLLECTION_FRIENDLYNAME = "CollectionFriendlyName";
	public static final String COLLECTION_INFORMATION = "CollectionInformation";
	public static final String COLLECTION_UNIQUEIDENTIFIER = "CollectionUniqueIdentifier";
	
	public static final String SENSOR_ID = "SensorID";
	public static final String SENSOR_TYPE = "SensorType";
	public static final String SENSOR_UPDATEREQUEST = "SensorUpdateRequest";
	public static final String SENSOR_POLLINGINTERVAl = "SensorPollingInterval";
	public static final String SENSOR_REPORTCHANGEONLY = "SensorReportChangeOnly";
	public static final String SENSOR_EVENTS_ENABLE = "SensorEventsEnable";
	
	public static final String SENSOR_URN = "SensorURN";
	
	public static final String DATAITEM_NAME = "Name";
	public static final String DATAITEM_TYPE = "Type";
	public static final String DATAITEM_ENCODING = "Encoding";
	public static final String DATAITEM_DESCRIPTION = "Description";

	public static final String SENSOREVENTS = "SensorEvents";
	public static final String SENSOREVENTS_PATH = "/UPnP/SensorMgt/SensorEvents";
	public static final String CONFIGURATION_UPDATE = "ConfigurationUpdate";
	
	public static final String EVENT_SOAPDATA = "SOAPDataAvailableEnable";
	
}