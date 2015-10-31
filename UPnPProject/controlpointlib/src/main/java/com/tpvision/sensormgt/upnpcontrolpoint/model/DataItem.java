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

import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorInfo.NodeType;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorURN.SensorDataReadListener;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetValues;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.ReadSensor;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.WriteSensor;

import android.util.Log;

public class DataItem extends SensorInfo implements GetValues, ReadSensor, WriteSensor {

	private static final String TAG = "DataItem";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_MODEL;
	
	private SensorMgtDevice mSensorMgtDevice;
	
	private SensorURN mParent;
	
	private String mInstanceId;
	
	// member variables according to specs
	private String mName="";
	private String mType="";
	private String mEncoding="";
	private String mDescription="";
	
	public boolean hasData=false;
	private String mReadBasicOnOffXML;
	private String mValue;
	private String mSensorURN;
	private String mSensorID;
	HashMap<String,String> mDataRecord = new HashMap<String, String>();
	private ArrayList<SensorDataItemReadListener> mListeners = new ArrayList<SensorDataItemReadListener>();
	
	public interface SensorDataItemReadListener {
		public void onSensorDataItemRead(DataItem dataItem);
	}
	
	public DataItem(SensorMgtDevice device, SensorURN parent, String path) {
		
		if (DEBUG) Log.d(TAG, "new SensorURN "+path);
		
		this.mSensorMgtDevice = device;
		
		this.mPath = path;
		this.mInstanceId = getInstanceIdFromPath(path);
	
		this.mParent = parent;
		
		//check which URN and Sensor we belong to
		this.mSensorURN = parent.getSensorURN();
		this.mSensorID = parent.getParent().getSensorId();

		updateBasicDataItemInfo();
		
	}
	
	public void addSensorDataItemReadListener(SensorDataItemReadListener listener) {
		mListeners.add(listener);
	}
	
	public void removeSensorDataItemReadListener(SensorDataItemReadListener listener) {
		mListeners.remove(listener);
	}
	
	
	private void sensorDataRead() {
		for (SensorDataItemReadListener listener: mListeners) {
			listener.onSensorDataItemRead(this);
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
	
	public void updateBasicDataItemInfo() {
		List<String> parameters = asList(mPath + DatamodelDefinitions.DATAITEM_NAME, 
				mPath + DatamodelDefinitions.DATAITEM_TYPE, 
				mPath + DatamodelDefinitions.DATAITEM_ENCODING,
				mPath + DatamodelDefinitions.DATAITEM_DESCRIPTION);
		
		getValues(XMLUPnPCPCreateUtil.createXMLContentPathList(parameters));
		
	}
	
	private void getValues(String parametersXML) {
		if (DEBUG) Log.d(TAG, "getValues "+parametersXML);
		hasData=false;
		mSensorMgtDevice.getConfigurationManagement().getValues(parametersXML, this);
	}

	@Override
	public void onGetValues(String valuesXml) {
		if (DEBUG) Log.d(TAG, "onGetValues "+valuesXml);

		try {
			HashMap<String, String> lParameterMap = XMLUPnPCPParseUtil.parseParameterValuesList(valuesXml);
			for (Entry<String, String> entry : lParameterMap.entrySet()) {
				if (entry.getKey().startsWith(mPath + DatamodelDefinitions.DATAITEM_NAME)) {
					this.mName = entry.getValue();
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.DATAITEM_TYPE)) {
					this.mType = entry.getValue();
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.DATAITEM_ENCODING)) {
					this.mEncoding = entry.getValue();
				} else if (entry.getKey().startsWith(mPath + DatamodelDefinitions.DATAITEM_DESCRIPTION)) {
					//HashMap<String, String> dataItemDescriptionMap = XMLUPnPUtil.parseDataItemDescription(StringEscapeUtils.unescapeXml(entry.getValue()));
					//mDescription = dataItemDescriptionMap.get(DatamodelDefinitions.DATAITEM_DESCRIPTION_DESCR);	
					this.mDescription = entry.getValue();
				}
			}
			hasData = true;
			mParent.onSensorDataAvailable(this);
		} catch (UPnPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void readSensorData() {
		SensorTransportGenericCPInterface sensorTransport = mSensorMgtDevice.getSensorTransport();
		if (sensorTransport != null) {	
			//read the value of this dataItem
			List<String> field = asList(mName);
			mReadBasicOnOffXML = XMLUPnPCPCreateUtil.createXMLSensorRecordInfo(field);
			if (DEBUG) Log.d(TAG,"READSENSOR "+mSensorID+", "+mSensorURN+", "+mReadBasicOnOffXML);
			mSensorMgtDevice.getSensorTransport().readSensor(mSensorID, "Android1", mSensorURN, mReadBasicOnOffXML, false, 1, this);
		}
	}
	
	/** 
	 * Writes the value to the Sensor
	 * @return
	 */
	public void writeSensorData(String value)
	{

		if (DEBUG) Log.d(TAG, "setValue: "+mSensorID+", "+mSensorURN);	
		SensorTransportGenericCPInterface sensorTransport = mSensorMgtDevice.getSensorTransport();
		if (sensorTransport != null) {
			mDataRecord.clear();
			mDataRecord.put(mName, value);
			String datarecordXML = XMLUPnPCPCreateUtil.createXMLDataRecords(mDataRecord);
			sensorTransport.writeSensor(mSensorID, mSensorURN, datarecordXML, this);
		} 		
	}

	@Override
	public void onReadSensor(String dataRecordsXml) {
		if (DEBUG) Log.d(TAG, "Read current value: "+dataRecordsXml);	
		try {
			ArrayList<DataItemInfo> diList = XMLUPnPCPParseUtil.parseDataRecords(dataRecordsXml);
			if (diList.size()>0) {
				DataItemInfo dii = diList.get(0);
				if ((dii!=null) && (dii.getValue()!=null)) {
					mValue = dii.getValue();

				}
				//signal listeners that the DataItem data changed
				sensorDataRead();
			}
		} catch (UPnPException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onWriteSensor() {
		if (DEBUG) Log.d(TAG, "onWriteSensor");
		
	}

	public String getInstanceId() {
		return mInstanceId;
	}
	
	public String getName() {
		return mName;
	}

	public String getType() {
		return mType;
	}

	public String getEncoding() {
		return mEncoding;
	}

	public String getDescription() {
		return mDescription;
	}
	
	public SensorURN getParent() {
		return mParent;
	}
	
	/** returns the last read value of the sensor 
	 * (The get the current Value call updateValue() instead)
	 */
	public String getValue() {
		return mValue;
	}
	
	/** 
	 * Sets the value of the dataItem. Used when the dataitem value is obtained by another ReadSensor Call
	 * Does NOT write to the sensor.
	 * @param value
	 */
	public void setValue(String value) {
		mValue = value;
	}
	
	@Override
	public NodeType getNodeType() {
		return NodeType.dataItem;
	}
	
	@Override
	public String toString() {
		
		return mName;
	}
	
	
}
