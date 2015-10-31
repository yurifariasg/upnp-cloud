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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.util.Log;

import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorMgtDevice;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorInfo.SensorDataAvailableListener;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetInstances;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetValues;

/**
 * Represents a sensor collection as described by the datamodel
 * 
 */

public class SensorCollection extends SensorInfo implements GetValues, GetInstances, SensorDataAvailableListener {

	private static final String TAG = "SensorCollection";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_MODEL;
	private static final boolean DEBUG_EVENTING = DebugControlPoint.DEBUG_EVENTING;
	
	private SensorMgtDevice mSensorMgtDevice;
	private ArrayList<Sensor> mSensors = new ArrayList<Sensor>();

	
	
	private String mInstanceId;
	
	// member variables according to specs
	private String mSensorCollectionId="";
	private String mType="";
	private String mFriendlyName="";
	private String mInformation="";
	private String mUniqueId="";

//	private int mSensorNumberOfEntries;
//
	public boolean hasData=false;
	private boolean gotInstances = false;
	private SensorMgtDevice mSensorListener;

	public interface SensorCollectionDataAvailableListener {
		public void onSensorCollectionDataAvailable(SensorCollection sensorCollection);
	}
	
	public interface SensorsChangedListener {
		public void onSensorsChanged(SensorCollection sensorCollection);
	}

	public SensorCollection(SensorMgtDevice device, String path) {
		
		if (DEBUG) Log.d(TAG, "new SensorCollection "+path);
		
		this.mSensorMgtDevice = device;
		setPath(path);
		this.mInstanceId = getInstanceIdFromPath(path);
	
		updateBasicSensorCollectionInfo();
		updateSensors();
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

	@Override
	public void onSensorDataAvailable(SensorInfo sensorNode) {
		if (DEBUG_EVENTING) Log.d(TAG,"onSensorDataAvailable");

		//signal parent that all sensor data, URNs and dataItems are available
		if (checkAllData()) mSensorMgtDevice.onSensorCollectionDataAvailable(this);
	}
	
	private boolean checkAllData() {
		//check if we got all SensorURNs
		boolean all=false;
		if (gotInstances) {
			all=true;
			for (Sensor s: mSensors) {
				all = all && s.hasData;
			}
		}
		
		if (DEBUG_EVENTING) Log.d(TAG,"checkAllData: "+all+" has data "+hasData);
		
		return all && hasData; 
	}
	
	public void updateBasicSensorCollectionInfo() {
		List<String> parameters = asList(mPath + DatamodelDefinitions.COLLECTION_ID, 
				mPath + DatamodelDefinitions.COLLECTION_TYPE, 
				mPath + DatamodelDefinitions.COLLECTION_FRIENDLYNAME, 
				mPath + DatamodelDefinitions.COLLECTION_INFORMATION,
				mPath + DatamodelDefinitions.COLLECTION_UNIQUEIDENTIFIER);
		
		getValues(XMLUPnPCPCreateUtil.createXMLContentPathList(parameters));
		
	}
	
	private void getValues(String parametersXML) {
		if (DEBUG) Log.d(TAG,"getValues("+parametersXML+")");
		hasData = false;
		mSensorMgtDevice.getConfigurationManagement().getValues(parametersXML, this);
	}

	@Override
	public void onGetValues(String valuesXml) {
		if (DEBUG) Log.d(TAG, "onGetValues "+valuesXml);

		try {
			HashMap<String, String> lParameterMap = XMLUPnPCPParseUtil.parseParameterValuesList(valuesXml);
			for (Entry<String, String> entry : lParameterMap.entrySet()) {
				if (entry.getKey().startsWith(mPath + DatamodelDefinitions.COLLECTION_ID)) {
					mSensorCollectionId = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.COLLECTION_ID+ ": " +mSensorCollectionId);
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.COLLECTION_TYPE)) {
					mType = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.COLLECTION_TYPE+ ": " +mType);
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.COLLECTION_FRIENDLYNAME)) {
					mFriendlyName = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.COLLECTION_FRIENDLYNAME+ ": " +mFriendlyName);
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.COLLECTION_INFORMATION)) {
					mInformation = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.COLLECTION_INFORMATION+ ": " +mInformation);
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.COLLECTION_UNIQUEIDENTIFIER)) {
					mUniqueId = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.COLLECTION_UNIQUEIDENTIFIER+ ": " +mUniqueId);
				}
			}
			hasData = true;
			//signal parent that all data for SensorCollection, Sensor, sensorURN and dataItems are available
			if (checkAllData()) mSensorMgtDevice.onSensorCollectionDataAvailable(this);
			
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getInstances(String startingNodePath, int searchDepth) {
		if (DEBUG) Log.d(TAG,"getInstances("+startingNodePath+", "+searchDepth+")");
		gotInstances = false;
		mSensors.clear();
		mSensorMgtDevice.getConfigurationManagement().getInstances(startingNodePath,  searchDepth, this);
	}
	
	public void updateSensors() {
		getInstances(mPath+"Sensors/", 1);
	}

	@Override
	public void onGetInstances(String instancesXml) {
		if (DEBUG) Log.d(TAG,"onGetInstances("+instancesXml+")");
		
		ArrayList<String> collectionsPaths;
		try {
			collectionsPaths = (ArrayList<String>) XMLUPnPCPParseUtil.parseInstancePathList(instancesXml);
			for (int i = 0; i < collectionsPaths.size(); i++) {
				mSensors.add(new Sensor(mSensorMgtDevice, this, collectionsPaths.get(i)));
			}
			gotInstances = true;
			if (mSensorListener != null) mSensorListener.onSensorsChanged(this);
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public Sensor findSensor(String sensorID) {
		
		if (DEBUG_EVENTING) Log.d(TAG,"findSensor: "+sensorID);
		
		int i=0;
		boolean found=false;
		Sensor sensor=null;
		while (i<mSensors.size() && !found) {
			sensor = mSensors.get(i);
			found = sensor.getSensorId().contentEquals(sensorID);
			i++;
		}
		
		if (found) return sensor;
		else return null;
	}
	
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o)
//			return true;
//		if (o == null || getClass() != o.getClass())
//			return false;
//		SensorCollection that = (SensorCollection) o;
//		return mId.equals(that.mId) && mType.equals(that.mType);
//	}
//

	public SensorMgtDevice getSensorMgtDevice() {
		return mSensorMgtDevice;
	}
	
	public ArrayList<Sensor> getSensors() {
		return mSensors;
	}

	public String getFriendlyName() {
		return mFriendlyName;
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

	@Override
	public NodeType getNodeType() {
		return NodeType.sensorCollection;
	}
	
	@Override
	public String toString() {
		
		return mFriendlyName;
	}

	

	
}
