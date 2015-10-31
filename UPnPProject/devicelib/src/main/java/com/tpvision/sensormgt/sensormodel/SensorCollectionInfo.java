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


package com.tpvision.sensormgt.sensormodel;

import java.util.HashMap;
import android.util.Log;

/**
 * Holds the data for a sensor collection 
 * Used to create SensorCollections dynamically and to read from/write to xml
 * 
 */

public class SensorCollectionInfo  {

	private static final String TAG = "SensorCollectionInfo";
	private static final boolean DEBUG = false;
	
//	private ArrayList<Sensor> mSensors = new ArrayList<Sensor>();
	private HashMap<String, String> fields = new HashMap<String, String>();

	private String mPath;
	
	private String mInstanceId;
	
	// member variables according to specs
	private String mSensorCollectionId="";
	private String mType="";
	private String mFriendlyName="";
	private String mInformation="";
	private String mUniqueId="";

	public interface SensorCollectionDataAvailableListener {
		public void onSensorCollectionDataAvailable(SensorCollectionInfo sensorCollection);
	}
	
	public interface SensorsChangedListener {
		public void onSensorsChanged(SensorCollectionInfo sensorCollection);
	}

	public SensorCollectionInfo(String path) {
		
		if (DEBUG) Log.d(TAG, "new SensorCollection "+path);
		
		this.mPath = path;
		this.mInstanceId = getInstanceIdFromPath(path);
	
	}
	
	/**
	 * Gets the instanceID, assumes path is correct and last segment is instanceId
	 */
	private String getInstanceIdFromPath(String path) {
		
		String[] segments = path.split("/");
		
		if (segments.length > 1)
			return segments[segments.length-1];
		else
			return "";
	}
	
	
	
//	public ArrayList<Sensor> getSensors() {
//		return mSensors;
//	}

	public String getPath() {
		return mPath;
	}
	
	public String getInstanceId() {
		return mInstanceId;
	}

	public String getSensorCollectionId() {
		return mSensorCollectionId;
	}

	public String getType() {
		return mType;
	}

	public String getFriendlyName() {
		return mFriendlyName;
	}

	public String getInformation() {
		return mInformation;
	}

	public String getUniqueId() {
		return mUniqueId;
	}
//
//	public int getSensorNumberOfEntries() {
//		return mSensorNumberOfEntries;
//	}
//

	
}
