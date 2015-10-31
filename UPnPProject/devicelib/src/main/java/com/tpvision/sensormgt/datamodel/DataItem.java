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

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/* implements the sensor dataItem which delivers the "real values". Should get values from a real sensor, or from a simulated sensor */

public class DataItem {

	private static final String TAG = "DataItem";
	private static final boolean DEBUG = DebugDatamodel.DEBUG_EVENTING;
	
	private String mDescription;
	private String mType="xsd:string";
	private String mEncoding="ascii";
	private String mNameSpace="";
	
	private String mCollectionID;
	private String mSensorID;
	private String mSensorURN;
	
	private String mValue;
	private String mName;
	
	private boolean mSOAPRead = true;
	private boolean mTransportRead = true;
	
	private ArrayList<SensorChangeListener> mSensorChangeListeners = new ArrayList<SensorChangeListener>();
	private ArrayList<SensorWriteListener> mSensorWriteListeners = new ArrayList<SensorWriteListener>();
	private ArrayList<SensorReadListener> mSensorReadListeners = new ArrayList<SensorReadListener>();
	private ArrayList<String> mClients = null;
	
	
	public DataItem(String collectionID, String sensorID, String sensorURN, String name) {
		mCollectionID = collectionID;
		mSensorID = sensorID;
		mSensorURN = sensorURN;
		mName = name;
	}

	public void addOnSensorChangeListener(SensorChangeListener sensorListener) {
		if (DEBUG) Log.d(TAG,"addOnSensorChangeListener on "+mName);
		mSensorChangeListeners.add(sensorListener);
	}
	
	public void removeOnSensorChangeListener(SensorChangeListener sensorListener) {
		mSensorChangeListeners.remove(sensorListener);
	}
	
	public void addOnSensorWriteListener(SensorWriteListener sensorListener) {
		if (DEBUG) Log.d(TAG,"addOnSensorWriteListener on "+mName);
		mSensorWriteListeners.add(sensorListener);
	}
	
	public void removeOnSensorWriteListener(SensorWriteListener sensorListener) {
		mSensorWriteListeners.remove(sensorListener);
	}
	
	/* 
	 * informs clients that this dataItem was read
	 */
	public void addOnSensorReadListener(SensorReadListener sensorListener) {
		mSensorReadListeners.add(sensorListener);
	}
	
	public void removeOnSensorReadListener(SensorReadListener sensorListener) {
		mSensorReadListeners.remove(sensorListener);
	}
	
	//sends an event to every registered listener
	private void sensorValueChanged() {
		if (DEBUG) Log.d(TAG, "sensorValueChanged "+this.mName);
		for(SensorChangeListener obs: mSensorChangeListeners) {
			obs.onSensorChange(mCollectionID, mSensorID, true, true, this);
		}
	}
	
	//sends an event to every registered listener
	private void sensorValueNotRead() {
		if (DEBUG) Log.d(TAG, "sensorValueNotRead "+this.mName);
		for(SensorChangeListener obs: mSensorChangeListeners) {
			obs.onSensorChange(mCollectionID, mSensorID, false, true, this);
		}
	}
	
	//sends an event to every registered listener
	private void sensorValueNotTransported() {
		if (DEBUG) Log.d(TAG, "sensorValueNotTransported "+this.mName);
		for(SensorChangeListener obs: mSensorChangeListeners) {
			obs.onSensorChange(mCollectionID, mSensorID, true, false, this);
		}
	}
		
	//sends an event to every registered listener
	private void sensorWritten() {
		if (DEBUG) Log.d(TAG, "sensorWritten "+this.mName);
		for(SensorWriteListener obs: mSensorWriteListeners) {
			obs.onSensorWrite(mCollectionID, mSensorID, this);
		}
	}

	//sends an event to every registered listener
	private void sensorRead() {
		if (DEBUG) Log.d(TAG, "sensorRead "+this.mName);
		for(SensorReadListener obs: mSensorReadListeners) {
			obs.onSensorRead(mCollectionID, mSensorID, this);
		}
	}
	
	public void registerClient(String clientID) {
		
		if (mClients==null) mClients = new ArrayList<String>();
		
		if (!mClients.contains(clientID)) {
			mClients.add(clientID);
			if (DEBUG) Log.d(TAG, "dataItem "+this.mName+" registered client "+ clientID);
		}
	}
	
	public void unregisterClient(String clientID) {
		
		if (mClients!=null) {
			mClients.remove(clientID);		
			if (DEBUG) Log.d(TAG, "dataItem "+this.mName+" removed client "+ clientID);
		}
	}

	public List<String> getRegisteredClients() {
		return mClients;
	}
	
	public String getDescriptionXML() {
		if (mDescription==null)
			return "";
		else
			return mDescription;
	}
	
	public String getType() {
		return mType;
	}
	
	public void setType(String mType) {
		this.mType = mType;
	}
	
	public String getSensorID() {
		return mSensorID;
	}

	public String getSensorURN() {
		return mSensorURN;
	}

	/**
	 * Returns the sensor value.
	 * Triggers notification of onSensorAccessed() listeners.
	 * @return the sensor value
	 */
	public String readSensorValue() {
		sensorRead();
		Log.d(TAG,"Read Sensor Value "+ mName + ": " + mValue +"dataItem "+this);
		return mValue;
	}

	/**
	 * Returns the sensor value as string.
	 * Does NOT trigger notification of onSensorAccessed() listeners.
	 * @return the sensor value
	 */
	public String getSensorValue() {
		Log.d(TAG,"getSensorValue "+ mName + ": " + mValue +"dataItem "+this);
		return mValue;
	}

	/**
	 * Returns the sensor value as boolean.
	 * Does NOT trigger notification of onSensorAccessed() listeners.
	 * @return the sensor value as boolean
	 */
	public Boolean getSensorValueAsBoolean() {
		Boolean value = (mValue.equals("1") ? true : false);
		Log.d(TAG,"getSensorValueAsBoolean "+ mName + ": " + value +"dataItem "+this);
		return value;
	}

	/**
	 * Returns the sensor value as integer.
	 * Does NOT trigger notification of onSensorAccessed() listeners.
	 * @return the sensor value as integer
	 */
	public Integer getSensorValueAsInt() {
		Integer value = Integer.parseInt(mValue);
		Log.d(TAG,"getSensorValueAsInt "+ mName + ": " + value +"dataItem "+this);
		return value;
	}

	/* when the sensor value is read using the GetValues SOAP action, this method must be called
	 * If the value has not been processed before new values arrive, an overrun event may be generated
	 */
	public void setReadSensorProcessed() {
		setSOAPRead(true);
	}
	
	/* when the sensor value is posted to a URL, this method must be called
	 * If the value has not been processed before new values arrive, an overrun event may be generated
	 */
	public void setTransportProcessed() {
		setTransportRead(true);
	}

	synchronized private void setSOAPRead(boolean read) {
		mSOAPRead = read;
	}
	
	synchronized private void setTransportRead(boolean read) {
		mTransportRead = read;
	}
	
	/**
	 * Writes the provided sensor value.
	 * Triggers notification of onSensorChanged() and onWrite() listeners.
	 * @param value the value to be written 
	 */
	public void writeSensorValue(String value) {
		if (value!=null) {
			if ((mValue==null) || !value.contentEquals(mValue))
			{
				Log.i(TAG,"Write Sensor Value " + mName + ": " + value + " on "+ mName);
				mValue = value;
				if (!mSOAPRead) sensorValueNotRead();
				if (!mTransportRead) sensorValueNotTransported();
				
				setSOAPRead(false);
				setTransportRead(false);
				sensorValueChanged();
				sensorWritten();
			}
		} else {
			Log.i(TAG, "Write Sensor null value, on "+mName);
		}
	}

	/**
	 * Sets the provided string sensor value.
	 * Triggers notification of onSensorChanged() listeners.
	 * Does NOT trigger notification of onWrite() listeners.
	 * @param value the string value to be written 
	 */
	public void setSensorValue(String value) {
		if (value!=null) {
			if ((mValue==null) || !value.contentEquals(mValue))
			{
				Log.i(TAG,"Set Value " + mName + ": " + value + " on "+ mName);
				mValue = value;
				if (!mSOAPRead) sensorValueNotRead();
				if (!mTransportRead) sensorValueNotTransported();
				
				setSOAPRead(false);
				setTransportRead(false);
				sensorValueChanged();
			}
		} else {
			Log.i(TAG, "Set null value, on "+mName);
		}
	}

	/**
	 * Sets the provided sensor boolean value.
	 * Triggers notification of onSensorChanged() listeners.
	 * Does NOT trigger notification of onWrite() listeners.
	 * @param value the boolean value to be written 
	 */
	public void setSensorValue(boolean value) {
		setSensorValue(value ? "1" : "0");
	}

	/**
	 * Sets the provided sensor integer value.
	 * Triggers notification of onSensorChanged() listeners.
	 * Does NOT trigger notification of onWrite() listeners.
	 * @param value the integer value to be written 
	 */
	public void setSensorValue(Integer value) {
		setSensorValue(value.toString());
	}

	public String getName() {
		return mName;
	}
	
	public String getEncoding() {
		return mEncoding;
	}

	public void setEncoding(String mEncoding) {
		this.mEncoding = mEncoding;
	}

	public String getNameSpace() {
		return mNameSpace;
	}

	public void setNameSpace(String mNameSpace) {
		this.mNameSpace = mNameSpace;
	}

	public String toString() {
		return "DataItem: type "+mType;
	}

}