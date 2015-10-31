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

package com.tpvision.sensormgt.datamodel;

import java.net.URL;
import java.util.List;

public class ClientConnectionInfo {
	
	private String mClientID;
	private int mConnectionID;
	private String mSensorID;
	private String mSensorURN;
	private URL mTransportURL;
	private boolean mSensorDatatypeEnable;
	private List<DataItem> mDataItemList;
	private List<SensorRecordInfo> mRequestedRecordInfoList;
	
	
	public ClientConnectionInfo(String clientID, int connectionID, String sensorID, String sensorURN, URL transportURL, 
			List<SensorRecordInfo> requestedRecordInfoList, List<DataItem> dataItemList, boolean sensorDatatypeEnable) {
		mClientID = clientID;
		mConnectionID = connectionID;
		mSensorID = sensorID;
		mSensorURN = sensorURN;
		mTransportURL = transportURL;
		mRequestedRecordInfoList = requestedRecordInfoList;
		mDataItemList = dataItemList;
		mSensorDatatypeEnable = sensorDatatypeEnable;
	}
	
	public String getClientID() {
		return mClientID;
	}
	
	public int getConnectionID() {
		return mConnectionID;
	}
	
	public String getSensorID() {
		return mSensorID;
	}


	public String getSensorURN() {
		return mSensorURN;
	}


	public URL getTransportURL() {
		return mTransportURL;
	}


	public boolean getSensorDatatypeEnable() {
		return mSensorDatatypeEnable;
	}


	public List<DataItem> getDataItems() {
		return mDataItemList;
	}
	
	public List<SensorRecordInfo> getRequestedDataItemInfo() {
		return mRequestedRecordInfoList;
	}

	public String getNamespace() {
		// TODO Auto-generated method stub
		
		return "urn:schemas-upnp-org:smgt:tspc";
	}
}