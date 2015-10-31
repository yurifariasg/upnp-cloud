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

import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorInfo.SensorDataAvailableListener;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetInstances;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetValues;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.SetValues;

import android.util.Log;


public class Sensor extends SensorInfo implements GetValues, SetValues, GetInstances, SensorDataAvailableListener {
	
	private static final String TAG = "Sensor";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_MODEL;
	private static final boolean DEBUG_EVENTING = DebugControlPoint.DEBUG_EVENTING;
	
	private SensorMgtDevice mSensorMgtDevice;
	private ArrayList<SensorURN> mSensorURNs = new ArrayList<SensorURN>();
	
	private SensorCollection mParent;

	private String mInstanceId;
	
	// member variables according to specs
	private String mSensorId="";
	private String mType="";
	private HashMap<String, String> mEventValues = new HashMap<String, String>();
//	private String mUniqueId;
//	private int mUpdateRequest;
//	private int mPollingInterval;
//	private int mReportChangeOnly;
	
//	private SingleInstance sensorSpecific
//	private int mRelatedNumberOfEntries;
//	private int mGroupsNumberOfEntries;
//	private int mPersmissionsNumberOfEntries;
//	private int mURNsNumberOfEntries;

	public boolean hasData=false;
	private boolean gotInstances = false;
	

	public Sensor(SensorMgtDevice device, SensorCollection parent, String path) {
		
		if (DEBUG) Log.d(TAG, "new Sensor "+path);
		
		this.mSensorMgtDevice = device;
		
		setPath(path);
		this.mInstanceId = getInstanceIdFromPath(path);
		
		this.mParent = parent;
	
		updateBasicSensorInfo();
		updateSensorURNs();
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
		if (DEBUG_EVENTING) Log.d(TAG,"onSensorURNDataAvailable");

		//signal parent that all sensorURN data and all dataItems are available
		if (checkAllData()) mParent.onSensorDataAvailable(this);
	}

	
	private boolean checkAllData() {
		//check if we got all SensorURNs
		boolean all=false;
		if (gotInstances) {
			all=true;
			for (SensorURN su: mSensorURNs) {
				all = all && su.hasData;
			}
		}
		if (DEBUG_EVENTING) Log.d(TAG,"checkAllData: "+all+" has data "+hasData);
		
		return all && hasData;
	}
	
	public void updateBasicSensorInfo() {
		List<String> parameters = asList(mPath + DatamodelDefinitions.SENSOR_ID, 
				mPath + DatamodelDefinitions.SENSOR_TYPE, 
				mPath + DatamodelDefinitions.SENSOR_EVENTS_ENABLE);
		
		getValues(XMLUPnPCPCreateUtil.createXMLContentPathList(parameters));
		
	}
	
	private void getValues(String parametersXML) {
		if (DEBUG) Log.d(TAG, "getValues "+parametersXML);
		hasData = false;
		mSensorMgtDevice.getConfigurationManagement().getValues(parametersXML, this);
	}

	@Override
	public void onGetValues(String valuesXml) {
		if (DEBUG) Log.d(TAG, "onGetValues "+valuesXml);

		try {
			HashMap<String, String> lParameterMap = XMLUPnPCPParseUtil.parseParameterValuesList(valuesXml);
			for (Entry<String, String> entry : lParameterMap.entrySet()) {
				if (entry.getKey().startsWith(mPath + DatamodelDefinitions.SENSOR_ID)) {
					mSensorId = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.SENSOR_ID+ ": " +mSensorId);
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.SENSOR_TYPE)) {
					mType = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.SENSOR_TYPE+ ": " +mType);
				} 
				else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.SENSOR_EVENTS_ENABLE)) {
					//Store events map
					String eventsCSV = entry.getValue();
					String[] events = eventsCSV.split(",");
					mEventValues.clear();
					for (int i=0; (i+1) < events.length; i+=2) {
						mEventValues.put(events[i], events[i+1]);
					}
					
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.SENSOR_EVENTS_ENABLE+ ": " +eventsCSV);
				} 
			}
			hasData = true;
			enableEvents();
			//signal parent that all data for Sensor, sensorURN and dataItems are available
			if (checkAllData()) {
				//signal parent that all data for Sensor, sensorURN and dataItems are available
				mParent.onSensorDataAvailable(this);
				//also signal the SensorMgt Device
				mSensorMgtDevice.onSensorDataAvailable(this);
			}
			
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getInstances(String startingNodePath, int searchDepth) {
		if (DEBUG) Log.d(TAG,"getInstances("+startingNodePath+", "+searchDepth+")");
		gotInstances = false;
		mSensorURNs.clear();
		mSensorMgtDevice.getConfigurationManagement().getInstances(startingNodePath,  searchDepth, this);
	}
	
	public void updateSensorURNs() {
		getInstances(mPath+"SensorURNs/", 1);
		
	}

	@Override
	public void onGetInstances(String instancesXml) {
		if (DEBUG) Log.d(TAG,"onGetInstances("+instancesXml+")");
		
		ArrayList<String> collectionsPaths;
		try {
			collectionsPaths = (ArrayList<String>) XMLUPnPCPParseUtil.parseInstancePathList(instancesXml);
			for (int i = 0; i < collectionsPaths.size(); i++) {
				mSensorURNs.add(new SensorURN(mSensorMgtDevice, this, collectionsPaths.get(i)));
			}
			gotInstances = true;
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void setValues(String parametersValuesXML) {
		if (DEBUG) Log.d(TAG, "setValues "+parametersValuesXML);
		 Log.d(TAG, "setValues "+parametersValuesXML);
		 
		mSensorMgtDevice.getConfigurationManagement().setValues(parametersValuesXML, this);
	}
	
	@Override
	public void onSetValues(String status) {
		// TODO Auto-generated method stub
		
	}
	
	/** update all DataItems of all SensorURNs by calling readSensor */
	public void updateDataItemValues() {
		for (SensorURN sensorURN: mSensorURNs) {
			sensorURN.readSensorData();
		}
	}

	public void enableEvents() {
		
		mEventValues.put(DatamodelDefinitions.EVENT_SOAPDATA,"1");
		
		String eventCSV = hashMapToCSV(mEventValues);
		HashMap<String, String> parVal = new HashMap<String, String>();
		parVal.put(mPath+DatamodelDefinitions.SENSOR_EVENTS_ENABLE, eventCSV);
		setValues(XMLUPnPCPCreateUtil.createXMLParameterValueList(parVal));
		
	}
	
	private String hashMapToCSV(HashMap<String,String> map) {

		StringBuffer eventCSV = new StringBuffer();
		boolean first = true;
		for(Entry<String, String> event : map.entrySet()) {
			if (!first) {
				eventCSV.append(',');
			} else { 
				first = false; 
			};
			eventCSV.append(event.getKey()+","+event.getValue());
		}
		
		return eventCSV.toString();
	}
		
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o)
//			return true;
//		if (o == null || getClass() != o.getClass())
//			return false;
//		Sensor that = (Sensor) o;
//		return mId.equals(that.mId) && mType.equals(that.mType);
//	}
//
//	public UPnPSensorMgtDevice getUPnPDevice() {
//	return mUPnPDevice;
//}
//
	public ArrayList<SensorURN> getSensorURNs() {
		return mSensorURNs;
	}
	
	public String getInstanceId() {
		return mInstanceId;
	}
	
	public String getSensorId() {
		return mSensorId;
	}


	public String getType() {
		return mType;
	}

	@Override
	public String toString() {
		return mSensorId+" "+mType;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.sensor;
	}

	
	

//	public String getUniqueId() {
//		return mUniqueId;
//	}
//
//	public int getUpdateRequest() {
//		return mUpdateRequest;
//	}
//
//	public int getPollingInterval() {
//		return mPollingInterval;
//	}
//
//	public int getReportChangeOnly() {
//		return mReportChangeOnly;
//	}
//
//	public String getEventsEnabled() {
//		return mEventsEnabled;
//	}
//
//	public int getRelatedNumberOfEntries() {
//		return mRelatedNumberOfEntries;
//	}
//
//	public int getGroupsNumberOfEntries() {
//		return mGroupsNumberOfEntries;
//	}
//
//	public int getPersmissionsNumberOfEntries() {
//		return mPersmissionsNumberOfEntries;
//	}
//
//	public int getURNsNumberOfEntries() {
//		return mURNsNumberOfEntries;
//	}
//
//	public SensorCollection getSensorCollection() {
//		return mCollection;
//	}
//
//	public String getPathId() {
//		return mPathId;
//	}
//

}
