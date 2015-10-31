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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

//TODO: persistence of ConfigurationUpdate statevariable, version? 


/**
 *  
 * During initialization of the datamodel, EventControl is set as a SensorEventListener for all relevant DataItems
 * when a dataitem value changes it calls onSensorValueChanged in EventControl
 * 
 * onSensorValueChanged checks which events are enabled for the sensor, by checking the Sensor's SensorEventsEnable parameter
 * This parameter consists of a CSV containing EventTypes followed by a 0 or 1
 * E.g.: SensorSOAPDataAvailableEnable,1,SensorSOAPDataOverrunEnable,0, 
 * 	SensorTransportDataAvailable,0,SensorTransportDataOverrun,0,SensorTransportConnectionError,0
 * All values are set to 0 by default. I.e. by default no events are sent on Sensor value changes. 
 * Control points can write to the SensorEventsEnable parameter to enable one or more event types
 * 
 * When an event needs to be created, it is added to a list of pending events, and a timer is set.
 * The default timer is 200ms. When a new event is added to the pending events list, the timer is reset.
 * When the timer expires. the SensorEvents parameter is set.
 * 
 * The SensorEvents parameter can be read by control points, and contains an XML document describing the events 
 * When the EventOnChange attribute of the SensorEvents parameter is set, control points are notified through the evented ConfigurationUpdate Statevariable 
 * EoC is set to 1 by default.
 * 
 * SensorEvents are reported using the ConfigurationUpdate StateVariable, which is a CSV
 * The CVS consists of a version 
 * The first element of the CSV is the last value of CurrentConfigurationVersion state variable
 * The second element of the CSV is the time stamp when the CurrentConfigurationVersion changed
 * The third element is an XML document containing a parameter value list containing the sensorEvents path, and its SensorEvents XML
 *  
 * 
 * 
 */

public class ConfMgtEventControl implements SensorChangeListener, ParameterEventListener, TransportConnectionErrorListener {
	
	protected static final String TAG = "EventControl";
	protected static final boolean DEBUG = DebugDatamodel.DEBUG_EVENTING;
	
	private static final long TIMEOUT = 200; //200ms delay before sending event
	private Timer mTimer = new Timer();
	private TimerTask mTimerTask = new EventTimerTask();

	private DatamodelInterfaceImpl mDatamodelInterface;
	
	private ArrayList<SensorEventData> mPendingSensorEvents = new ArrayList<SensorEventData>();
	private ArrayList<LeafNode> mPendingParameterEvents = new ArrayList<LeafNode>();
	
	protected static final String SENSOR_EVENTS_ENABLE = "/UPnP/SensorMgt/SensorCollections/1/Sensors/1/SensorEventsEnable";
	private ArrayList<String> mEventsEnablePath = new ArrayList<String>();
	
	protected static final String SOAPDATA_AVAILABLE_ENABLE = "SOAPDataAvailableEnable";
	protected static final String SOAPDATA_OVERRUN_ENABLE = "SOAPDataOverrunEnable";
	protected static final String TRANSPORTDATA_AVAILABLE_ENABLE = "TransportDataAvailableEnable";
	protected static final String TRANSPORTDATA_OVERRUN_ENABLE = "TransportDataOverrunEnable";
	protected static final String TRANSPORTDATA_CONNECTIONERROR_ENABLE = "TransportConnectionErrorEnable";
	
	protected static final String SOAPDATA_AVAILABLE = "SOAPDataAvailable";
	protected static final String SOAPDATA_OVERRUN = "SOAPDataOverrun";
	protected static final String TRANSPORTDATA_AVAILABLE = "TransportDataAvailable";
	protected static final String TRANSPORTDATA_OVERRUN = "TransportDataOverrun";
	protected static final String TRANSPORTDATA_CONNECTIONERROR = "TransportConnectionError";
	
	protected static final String SENSOREVENTS = "/UPnP/SensorMgt/SensorEvents";
	
	private int mConfigurationVersion=0; //maintain Configuration version control
	private String mConfigurationUpdateCSV;
	private SensorChangeListener mSensorEventListener=null;
	
	
	public ConfMgtEventControl(DatamodelInterfaceImpl datamodelInterface) {
		mDatamodelInterface = datamodelInterface;
		mConfigurationUpdateCSV = mConfigurationVersion + "," + getCurrentDateTime();
		
    	mEventsEnablePath.add(SENSOR_EVENTS_ENABLE);
	}

	//TODO: modify to add
	public void setOnSensorValueChangedListener(SensorChangeListener sensorListener) {
		mSensorEventListener= sensorListener;
	}
	
	public void removeOnSensorValueChangedListener() {
		mSensorEventListener= null;
	}
	
	//sends an event to the registered listener
	private void sensorValueChanged(String collectionID, String sensorID, boolean valueRead, boolean valueTransported, DataItem dataItem) {
		if (mSensorEventListener!=null) mSensorEventListener.onSensorChange(collectionID, sensorID, valueRead, valueTransported, dataItem);
	}
	
	/**
	 * Callback, dataItems call this function when a dataItem's value changed
	 */
	@Override
	public void onSensorChange(String collectionID, String sensorID, boolean valueRead, boolean valueTransported,
			DataItem dataItem) {
		checkSensorEventEnabled(collectionID, sensorID, valueRead, valueTransported, dataItem);
		
		//forward the event to registered listeners (Connection control for posting data)
		sensorValueChanged(collectionID, sensorID, valueRead, valueTransported, dataItem);
	}

	/**
	 * process the event if the parameter's EOC attribute is set
	 */
	@Override
	public void onParameterValueChanged(LeafNode parameter) {
		 //don't react to sensorevents parameter, it was already handled as a sensorEvent (to avoid 2x200ms moderation)
		if (!parameter.getPath().contentEquals(SENSOREVENTS)) {
			if (parameter.isEOC()) addPendingParameterEvent(parameter);
		}
	};	
	
	@Override
	public void onTransportConnectionError(String collectionID, String sensorID) {
		checkSensorTransportConnectionErrorEnabled(collectionID, sensorID);
	}
	
	public String getConfigurationUpdate() {
		return mConfigurationUpdateCSV;
	}
	
	public int getCurrentConfigurationVersion() {
		return mConfigurationVersion;
	}
	
	//FIXME: remove the hardcoded  collection and sensor ids
	private void checkSensorEventEnabled(String collectionID, String sensorID, boolean valueRead, boolean valueTransported, DataItem dataItem) {

		//getSelectedValues(/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorEventsEnable, 
    	//		filter /UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorID = sensorID
    	//				AND /UPnP/SensorMgt/SensorCollections/#/CollectionID = collectionID
		
		
    	HashMap<String, String> value;
    	try {
    		value = mDatamodelInterface.getValuesList(mEventsEnablePath);
    		String sensorEventsEnableCSV = value.get(SENSOR_EVENTS_ENABLE);

    		if (sensorEventsEnableCSV != null)  {
    			String[] sensorEventsEnableList = sensorEventsEnableCSV.split(",");
    			for (int i=0; (i+1) < sensorEventsEnableList.length; i+=2) {
    				//if (DEBUG) Log.d(TAG,"check: "+sensorEventsEnableList[i]+"="+sensorEventsEnableList[i+1]);
    				if (DEBUG) if (sensorEventsEnableList[i+1].contentEquals("1")) Log.d(TAG,"event enabled : "+sensorEventsEnableList[i]);
    				
    				if (sensorEventsEnableList[i].contentEquals(SOAPDATA_AVAILABLE_ENABLE) && 
    					(sensorEventsEnableList[i+1].contentEquals("1"))) {
    					//register this event, and send after 200ms
    					addPendingSensorEvent(collectionID, sensorID, SOAPDATA_AVAILABLE);
    				}	
    				
    				if (sensorEventsEnableList[i].contentEquals(TRANSPORTDATA_AVAILABLE_ENABLE) && 
        					(sensorEventsEnableList[i+1].contentEquals("1"))) {
        					//register this event, and send after 200ms
        					addPendingSensorEvent(collectionID, sensorID, TRANSPORTDATA_AVAILABLE);
        				}	
    				
    				//TODO: add checking for event duplication, multiple dataItems on same sensor
    				if ((!valueRead) && sensorEventsEnableList[i].contentEquals(SOAPDATA_OVERRUN_ENABLE) && 
        					(sensorEventsEnableList[i+1].contentEquals("1"))) {
        					//register this event, and send after 200ms
    					addPendingSensorEvent(collectionID, sensorID, SOAPDATA_OVERRUN);
        			}	
//TODO: difference between soap data read and transportdata read. (one can be  overrun while the other was read in time)   				
    				
    				if ((!valueTransported) && sensorEventsEnableList[i].contentEquals(TRANSPORTDATA_OVERRUN_ENABLE) && 
        					(sensorEventsEnableList[i+1].contentEquals("1"))) {
        					//register this event, and send after 200ms
    					addPendingSensorEvent(collectionID, sensorID, TRANSPORTDATA_OVERRUN);
        			}	
    				
    			}
    		}	

    	} catch (UPnPException e) {
    		e.printStackTrace(); //can only occur if something is wrong with the datamodel
    	}   	
	}
	
	//FIXME: remove the hardcoded  collection and sensor ids
		private void checkSensorTransportConnectionErrorEnabled(String collectionID, String sensorID) {

			//getSelectedValues(/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorEventsEnable, 
	    	//		filter /UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorID = sensorID
	    	//				AND /UPnP/SensorMgt/SensorCollections/#/CollectionID = collectionID
			
			
	    	HashMap<String, String> value;
	    	try {
	    		value = mDatamodelInterface.getValuesList(mEventsEnablePath);
	    		String sensorEventsEnableCSV = value.get(SENSOR_EVENTS_ENABLE);

	    		if (sensorEventsEnableCSV != null)  {
	    			String[] sensorEventsEnableList = sensorEventsEnableCSV.split(",");
	    			for (int i=0; (i+1) < sensorEventsEnableList.length; i+=2) {
	    				//if (DEBUG) Log.d(TAG,"check: "+sensorEventsEnableList[i]+"="+sensorEventsEnableList[i+1]);
	    				if (DEBUG) if (sensorEventsEnableList[i+1].contentEquals("1")) Log.d(TAG,"event enabled : "+sensorEventsEnableList[i]);
	    				
	    				if (sensorEventsEnableList[i].contentEquals(TRANSPORTDATA_CONNECTIONERROR_ENABLE) && 
	    					(sensorEventsEnableList[i+1].contentEquals("1"))) {
	    					//register this event, and send after 200ms
	    					addPendingSensorEvent(collectionID, sensorID, TRANSPORTDATA_CONNECTIONERROR);
	    				}		
	    			}
	    		}	

	    	} catch (UPnPException e) {
	    		e.printStackTrace(); //can only occur if something is wrong with the datamodel
	    	}   	
		}
	
	
	
	/**
	 * Add the current event to the list of pending sensor events, and set the timer.
	 * It will be sent if within 200ms no new event is added
	 * 
	 * @param collectionID
	 * @param sensorID
	 * @param eventType
	 */
	private void addPendingSensorEvent(String collectionID, String sensorID, String eventType) {

		if (DEBUG) Log.i(TAG,"add Pending Event: "+collectionID+" ,"+sensorID+", "+eventType);

		mPendingSensorEvents.add(new SensorEventData(collectionID, sensorID, eventType));
		
		mTimerTask.cancel();
		mTimerTask = new EventTimerTask();
		mTimer.purge();
		mTimer.schedule(mTimerTask, TIMEOUT);
	}
	

	/**
	 * Add the current event to the list of pending parameter events, and set the timer.
	 * It will be sent if within 200ms no new event is added
	 * 
	 * @param collectionID
	 * @param sensorID
	 * @param eventType
	 */
	private void addPendingParameterEvent(LeafNode parameter) {

		if (DEBUG) Log.i(TAG,"add Pending parameter Event:" + parameter.getNodeName());

		mPendingParameterEvents.add(parameter);
		mConfigurationVersion++; //update the version
		parameter.setVersion(mConfigurationVersion);
		
		mTimerTask.cancel();
		mTimerTask = new EventTimerTask();
		mTimer.purge();
		mTimer.schedule(mTimerTask, TIMEOUT);
	}
	
	
	/**
	 * Processes the pending sensor events, when the 200ms timer expires
	 * Assigns the pending events to the SensorEvents parameter node in XML SensorEvents format, and clears the pending event
	 * 
	 * @param pendingSensorEvents list of registered sensor events
	 * @return SensorEvent LeafNode
	 */
	private LeafNode setSensorEvent(ArrayList<SensorEventData> pendingSensorEvents) {
		
		mConfigurationVersion++; //update the version
		
		LeafNode sensorEventsNode = (LeafNode) mDatamodelInterface.findInstanceNodePath(SENSOREVENTS);
		sensorEventsNode.setValue(XMLUPnPUtil.createXMLSensorEvents(pendingSensorEvents));
		sensorEventsNode.setVersion(mConfigurationVersion);
		pendingSensorEvents.clear(); //event was processed, remove it
		
		return sensorEventsNode;
	}
	
	/**
	 * Create configurationUpdateCSV consisting of version, timestamp and ParameterValueList
	 * @param pendingParameterEvents
	 * @return
	 */
	private void configurationUpdate(ArrayList<LeafNode> pendingParameterEvents) {

		if (!pendingParameterEvents.isEmpty()) {
			mConfigurationUpdateCSV = Integer.toString(mConfigurationVersion);
			mConfigurationUpdateCSV += "," + getCurrentDateTime();
			HashMap<String, String> parameterValues = new HashMap<String, String>();

			if (DEBUG) Log.d(TAG,"Configuration update");

			for (LeafNode leafnode : pendingParameterEvents) {
				if (DEBUG) Log.d(TAG,"pendingParameterEvents: "+leafnode.getPath()+", " + leafnode.getValue());
				parameterValues.put(leafnode.getPath(), leafnode.getValue());
			}
			mConfigurationUpdateCSV += "," + XMLUPnPUtil.createXMLParameterValueList(parameterValues);
			mDatamodelInterface.configurationUpdate(mConfigurationUpdateCSV);
		}
	}
	
	/**
	 * Gets the current date-time as ISO8601 formatted string
	 * @return date-time string, ISO8601 formatted
	 */
	private String getCurrentDateTime() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	    df.setTimeZone(tz);
	    return df.format(new Date());
	}

	
	private class EventTimerTask extends TimerTask {          
		@Override
		public void run() {
			
			LeafNode sensorEventsNode = setSensorEvent(mPendingSensorEvents); //changing the SensorEvents parameter is done here, for 200ms moderation
			if (sensorEventsNode.isEOC()) mPendingParameterEvents.add(sensorEventsNode); 

			//now process all parameter events
			configurationUpdate(mPendingParameterEvents);
			
			mPendingParameterEvents.clear(); //events processed, clear the events
		}
	}
		
}

