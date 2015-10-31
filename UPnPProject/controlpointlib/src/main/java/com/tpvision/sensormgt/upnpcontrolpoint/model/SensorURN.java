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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.util.Log;

import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorInfo.NodeType;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorInfo.SensorDataAvailableListener;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetInstances;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetValues;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.ReadSensor;

public class SensorURN extends SensorInfo implements GetValues, GetInstances, ReadSensor, SensorDataAvailableListener {
	
	private static final String TAG = "SensorURN";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_MODEL;
	private static final boolean DEBUG_EVENTING = DebugControlPoint.DEBUG_EVENTING;
	
	private SensorMgtDevice mSensorMgtDevice;
	private ArrayList<DataItem> mDataItems = new ArrayList<DataItem>();
	private ArrayList<SensorDataReadListener> mListeners = new ArrayList<SensorDataReadListener>();
	
	private Sensor mParent;
	private String mInstanceId;
	
	// member variables according to specs
	private String mURN="";
	private String mType="";
	
	public boolean hasData=false;
	private boolean gotInstances = false;
	
	public interface SensorDataReadListener {
		public void onSensorDataRead(SensorURN sensorURN);
	}
	
	private String mSensorID;

	public SensorURN(SensorMgtDevice device, Sensor parent, String path) {
		
		if (DEBUG) Log.d(TAG, "new SensorURN "+path);
		
		this.mSensorMgtDevice = device;
		
		this.mPath = path;
		this.mInstanceId = getInstanceIdFromPath(path);
	
		this.mParent = parent;
		this.mSensorID = getParent().getSensorId();
		
		updateBasicSensorURNInfo();
		updateDataItems();
	}
	
	
	public void addSensorDataReadListener(SensorDataReadListener listener) {
		mListeners.add(listener);
	}
	
	public void removeSensorDataReadListener(SensorDataReadListener listener) {
		mListeners.remove(listener);
	}
	
	
	private void sensorDataRead() {
		for (SensorDataReadListener listener: mListeners) {
			listener.onSensorDataRead(this);
		}
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
		if (DEBUG_EVENTING) Log.d(TAG,"onDataItemDataAvailable");
		//signal parent that all sensorURN data and all dataItems are available
		if (checkAllData()) mParent.onSensorDataAvailable(this);
	}

	private boolean checkAllData() {
		//check if we got all dataitems
		boolean all=false;
		if (gotInstances) {
			all=true;
			for (DataItem di: mDataItems) {
				all = all && di.hasData;
			}
		}
		if (DEBUG_EVENTING) Log.d(TAG,"checkAllData: "+all+" has data "+hasData);
		
		return all && hasData;
	}
	
	public void updateBasicSensorURNInfo() {
		List<String> parameters = asList(mPath + DatamodelDefinitions.SENSOR_URN);
		
		getValues(XMLUPnPCPCreateUtil.createXMLContentPathList(parameters));
		
	}
	
	private void getValues(String parametersXML) {
		if (DEBUG) Log.d(TAG, "getValues "+parametersXML);
		mSensorMgtDevice.getConfigurationManagement().getValues(parametersXML, this);
	}

	@Override
	public void onGetValues(String valuesXml) {
		if (DEBUG) Log.d(TAG, "onGetValues "+valuesXml);

		try {
			HashMap<String, String> lParameterMap = XMLUPnPCPParseUtil.parseParameterValuesList(valuesXml);
			for (Entry<String, String> entry : lParameterMap.entrySet()) {
				if (entry.getKey().startsWith(mPath + DatamodelDefinitions.SENSOR_URN)) {
					mURN = entry.getValue();
					if (DEBUG) Log.d(TAG, DatamodelDefinitions.SENSOR_URN+ ": " +mURN);
				}
			}
			this.hasData = true;
			//signal parent that all sensorURN data and all dataItems are available
			if (checkAllData()) mParent.onSensorDataAvailable(this);
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getInstances(String startingNodePath, int searchDepth) {
		if (DEBUG) Log.d(TAG,"getInstances("+startingNodePath+", "+searchDepth+")");
		gotInstances = false;
		mDataItems.clear();
		mSensorMgtDevice.getConfigurationManagement().getInstances(startingNodePath,  searchDepth, this);
	}
	
	public void updateDataItems() {
		getInstances(mPath+"DataItems/", 1);
		
	}

	public void readSensorData() {
		for (DataItem dataItem: mDataItems) {
			dataItem.readSensorData();
		}
	}
	
	public void readSensorData(ArrayList<DataItem> dataItems) {

		ArrayList<String> field = new ArrayList<String>(); 
		for (DataItem dataItem : dataItems) {
			field.add(dataItem.getName());
		}

		SensorTransportGenericCPInterface sensorTransport = mSensorMgtDevice.getSensorTransport();
		if (sensorTransport != null) {	
			//read the value of this dataItem

			String sensorRecordInfoXML = XMLUPnPCPCreateUtil.createXMLSensorRecordInfo(field);
			Log.e(TAG,"READSENSOR "+mSensorID+", "+getSensorURN()+", "+sensorRecordInfoXML);
			//TODO: get control point name from UPnPInit			
			mSensorMgtDevice.getSensorTransport().readSensor(mSensorID, "Android1", getSensorURN(), sensorRecordInfoXML, false, 1, this);
		}
	}
	
	@Override
	public void onReadSensor(String dataRecordsXml) {
		Log.d(TAG, "Read current value: "+dataRecordsXml);	
		try {
			ArrayList<DataItemInfo> diList = XMLUPnPCPParseUtil.parseDataRecords(dataRecordsXml);
			
			for (DataItemInfo dii: diList) {
				if (dii.getValue()!=null) {
					
					//find dataItem
					Iterator<DataItem> i = mDataItems.iterator();
					boolean found = false;
					while (i.hasNext()&&!found) {
						DataItem di = i.next();
						found = di.getName().equals(dii.getFieldName());
						if (found) di.setValue(dii.getValue());
					}
				}
				//signal listeners that the DataItem data changed
				sensorDataRead();
			}
		} catch (UPnPException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGetInstances(String instancesXml) {
		if (DEBUG) Log.d(TAG,"onGetInstances("+instancesXml+")");
		
		ArrayList<String> collectionsPaths;
		try {
			collectionsPaths = (ArrayList<String>) XMLUPnPCPParseUtil.parseInstancePathList(instancesXml);
			for (int i = 0; i < collectionsPaths.size(); i++) {
				mDataItems.add(new DataItem(mSensorMgtDevice, this, collectionsPaths.get(i)));
			}
			gotInstances = true;
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public String getInstanceId() {
		return mInstanceId;
	}
	
	public String getSensorURN() {
		return mURN;
	}
	
	public String getType() {
		return mType;
	}
	
	public Sensor getParent() {
		return mParent;
	}

	public ArrayList<DataItem> getDataItems() {
		return mDataItems;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.sensorURN;
	}

	@Override
	public String toString() {
		return mURN;
	}
	
}

