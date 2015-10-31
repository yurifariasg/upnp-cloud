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

package com.tpvision.sensormgt.devicelib;

import java.beans.PropertyChangeSupport;

import org.fourthline.cling.binding.annotations.*;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import com.tpvision.sensormgt.datastore.DataStoreInterfaceImpl;
import com.tpvision.sensormgt.datastore.DebugDataStore;
import com.tpvision.sensormgt.datastore.LastChangeEventListener;
import com.tpvision.sensormgt.datastore.LastChangeInfo;
import com.tpvision.sensormgt.datastore.UPnPException;
import com.tpvision.sensormgt.datastore.XMLUPnPDataStoreUtil;

import android.util.Log;

@UpnpService(
		serviceId = @UpnpServiceId("DataStore"), 
		serviceType = @UpnpServiceType(value = "DataStore", version = 1)
)

@UpnpStateVariables({ 
		@UpnpStateVariable(name = "A_ARG_TYPE_DataStoreGroups", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTableID", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTableInfo", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTableInfoElement", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTableResetReq", datatype = "boolean", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataStoreInfo", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTableInfo", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecords", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecordsStatus", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecordFilter", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecordStart", datatype = "string", defaultValue = "0", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecordIndex", datatype = "string", defaultValue = "0", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecordCount", datatype = "ui4", defaultValue = "0", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataRecordPropResolve", datatype = "boolean", defaultValue = "false", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTableKeyName", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTableKeyValue", datatype = "string", sendEvents = false),	
		@UpnpStateVariable(name = "A_ARG_TYPE_DataTransportURL", datatype = "string", sendEvents = false),
		@UpnpStateVariable(name = "LastChange", datatype = "string", sendEvents = true)
		 })

/** 
 * Implements the wrapper for all Cling UPnP Actions in the DataStore service.  
 * All actions call a corresponding implementation in the DataStoreInterfaceImpl class, which is independent of the UPnP stack that is used
 */

public class DataStore implements LastChangeEventListener {

	private static final String TAG = "DataStore";
	private static final boolean DEBUG = DebugDataStore.DEBUG_UPNP;
	private DataStoreInterfaceImpl mDataStoreInterface = null;
	private PropertyChangeSupport mPropertyChangeSupport;
	private String mDataRecords;
	private String mDataRecordIndex;
	private String lastChange;
	
//	private static final long TIMEOUT = 2000; //400ms delay before sending event
//	private Timer mTimer = new Timer();
//	private TimerTask mTimerTask = new EventTimerTask();
	
	public DataStore() {
		mPropertyChangeSupport = new PropertyChangeSupport(this);
		
		//init lastChange;
		lastChange = XMLUPnPDataStoreUtil.createXMLLastChange(new LastChangeInfo());
		
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
        return mPropertyChangeSupport;
    }
	
	public void setDataStore(DataStoreInterfaceImpl dataStoreInterfaceImpl) {
		mDataStoreInterface = dataStoreInterfaceImpl;
		mDataStoreInterface.setLastChangeEventListener(this);
	}

//	 /**
//     * Send the current configuration update as an initial event when a control point subscribes
//     */
//    public void sendInitialEvents() {
//    	Log.e("DATASTORE TIMER", "SETTING EVENT TIMER ");
//    	mTimerTask.cancel();
//		mTimerTask = new EventTimerTask();
//		mTimer.purge();
//		mTimer.schedule(mTimerTask, TIMEOUT);
//		
//		
//    }
    
    
    public void sendLastChange() {
    	if (DebugDataStore.DEBUG_EVENTS) Log.d(TAG, "send last change event");
    	getPropertyChangeSupport().firePropertyChange("LastChange", false, true); 

    }    
	
	
	@UpnpAction(out = @UpnpOutputArgument(name = "DataStoreGroupList", stateVariable = "A_ARG_TYPE_DataStoreGroups"))
	public String getDataStoreGroups() {
	
		if (DEBUG) Log.d(TAG, "getDataStoreGroups()");
		
		if (mDataStoreInterface != null) {
		
			String groups = mDataStoreInterface.getDataStoreGroups();
			return groups;
		}	
		
		return "";
	}
/*	
	@UpnpAction
	public void createDataStoreGroups(@UpnpInputArgument(name = "DataStoreGroupList", stateVariable = "A_ARG_TYPE_DataStoreGroups") String dataStoreGroupsXML) throws SensorMgtException  {

		if (DEBUG) Log.d(TAG, "createDataStoreGroups()");

		if (mDataStoreInterface != null) {
			try {

				mDataStoreInterface.createDataStoreGroups(dataStoreGroupsXML);
			}
			catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
	}
	
	
	@UpnpAction
	public void deleteDataStoreGroups(@UpnpInputArgument(name = "DataStoreGroupList", stateVariable = "A_ARG_TYPE_DataStoreGroups") String dataStoreGroupsXML) {
	
		if (DEBUG) Log.d(TAG, "deleteDataStoreGroups()");
		
		if (mDataStoreInterface != null) {
			mDataStoreInterface.deleteDataStoreGroups(dataStoreGroupsXML);
		}	
	}
	*/
	
	@UpnpAction(out = @UpnpOutputArgument(name = "DataTableID"))
	public String createDataStoreTable(@UpnpInputArgument(name = "DataTableInfo") String dataTableInfo) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "createDataStoreTable(\"" + dataTableInfo + ")");
		
		int dataTableID = -1;
		if (mDataStoreInterface != null) {
			try {
				dataTableID = mDataStoreInterface.createDataStoreTable(dataTableInfo);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
		
		return Integer.toString(dataTableID);
	}

	
	@UpnpAction
	public void deleteDataStoreTable(@UpnpInputArgument(name = "DataTableID") String dataTableID) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "deleteDataStoreTable(\"" + dataTableID + ")");
		
		if (mDataStoreInterface != null) {
			try {
			 mDataStoreInterface.deleteDataStoreTable(dataTableID);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
	}
	
	@UpnpAction
	public void resetDataStoreTable(@UpnpInputArgument(name = "DataTableID") String dataTableID, 
			@UpnpInputArgument(name = "ResetDataTableRecords", stateVariable = "A_ARG_TYPE_DataTableResetReq") boolean resetDataTableRecords, 
			@UpnpInputArgument(name = "ResetDataTableDictionary", stateVariable = "A_ARG_TYPE_DataTableResetReq") boolean resetDataTableDictionary,
			@UpnpInputArgument(name = "ResetDataTableTransport", stateVariable = "A_ARG_TYPE_DataTableResetReq") boolean resetDataTableTransport) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "resetDataStoreTable(\"" + dataTableID + ")");
		
		if (mDataStoreInterface != null) {
			try {
			 mDataStoreInterface.resetDataStoreTable(dataTableID, resetDataTableRecords, resetDataTableDictionary, resetDataTableTransport);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "DataTableID"))
	public void modifyDataStoreTable(@UpnpInputArgument(name = "DataTableID") String dataTableID,
			@UpnpInputArgument(name = "DataTableInfoElementOrig", stateVariable = "A_ARG_TYPE_DataTableInfoElement") String dataTableInfoElementOrigXML,
			@UpnpInputArgument(name = "DataTableInfoElementNew", stateVariable = "A_ARG_TYPE_DataTableInfoElement") String dataTableInfoElementNewXML ) throws SensorMgtException  {
		
		if (DEBUG) Log.d(TAG, "modifyDataStoreTable(\"" + dataTableID + "," + dataTableInfoElementOrigXML +", " +  dataTableInfoElementNewXML + ")");
		
		if (mDataStoreInterface != null) {
			try {
			 mDataStoreInterface.modifyDataStoreTable(dataTableID, dataTableInfoElementOrigXML,  dataTableInfoElementNewXML);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
	}
	
	
	@UpnpAction(out = @UpnpOutputArgument(name = "DataStoreInfo"))
	public String getDataStoreInfo() throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "getDataStoreInfo()");
		
		String dataStoreInfo = "";
		if (mDataStoreInterface != null) {
			try {
				dataStoreInfo = mDataStoreInterface.getDataStoreInfo();
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
		
		return dataStoreInfo;
	}

	
	@UpnpAction(out = @UpnpOutputArgument(name = "DataTableInfo"))
	public String getDataStoreTableInfo(@UpnpInputArgument(name = "DataTableID") String dataTableID) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "getDataStoreTableInfo(\"" + dataTableID + ")");

		String dataTableInfo = "";
		if (mDataStoreInterface != null) {
			try {
				dataTableInfo = mDataStoreInterface.getDataStoreTableInfo(dataTableID);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}

		return dataTableInfo;
	}
	
	
	@UpnpAction(out = @UpnpOutputArgument(name = "DataRecordsStatus"))
	public String writeDataStoreTableRecords(@UpnpInputArgument(name = "DataTableID") String dataTableID, 
			@UpnpInputArgument(name = "DataRecords") String dataRecordsXML) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "writeDataStoreTableRecords(\"" + dataTableID + ")");

		String dataRecordStatus = "";
		if (mDataStoreInterface != null) {
			try {
				dataRecordStatus = mDataStoreInterface.writeDataStoreTableRecords(dataTableID, dataRecordsXML);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}

		return dataRecordStatus;
	}

	

	@UpnpAction(out = {@UpnpOutputArgument(name = "DataRecords", getterName = "getDataRecords"), 
					@UpnpOutputArgument(name = "DataRecordContinue", stateVariable = "A_ARG_TYPE_DataRecordIndex", getterName = "getDataRecordIndex") })
	public void readDataStoreTableRecords(@UpnpInputArgument(name = "DataTableID") String dataTableID,
			@UpnpInputArgument(name = "DataRecordFilter") String dataRecordFilter, 
			@UpnpInputArgument(name = "DataRecordStart", stateVariable = "A_ARG_TYPE_DataRecordIndex") String dataRecordStart,
			@UpnpInputArgument(name = "DataRecordCount") UnsignedIntegerFourBytes  dataRecordCount,
			@UpnpInputArgument(name = "DataRecordPropResolve") boolean dataRecordPropResolve) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "readDataStoreTableRecords(\"" + dataTableID + ", "+ dataRecordFilter + ", " + dataRecordStart + ", "+ dataRecordCount+ ", "+ dataRecordPropResolve+")");
		
		int count = 0;
		if (dataRecordCount!=null) count = dataRecordCount.getValue().intValue();
		
		String dataRecordStatus = "";
		if (mDataStoreInterface != null) {
			try {
				dataRecordStatus = mDataStoreInterface.readDataStoreTableRecords(dataTableID, dataRecordFilter,  dataRecordStart, count, dataRecordPropResolve);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}

		//extract two values from the result
		String[] dataRecordsResults = dataRecordStatus.split(",");
		//allow getters to read out results
		mDataRecords = dataRecordsResults[0]; 
		mDataRecordIndex = dataRecordsResults[1];
		
		//return dataRecordStatus;
	}

	public String getDataRecords() {
		return mDataRecords;
	}
	
	public String getDataRecordIndex() {
		return mDataRecordIndex;
	}
	
	@UpnpAction
	public void setDataStoreTableKeyValue(@UpnpInputArgument(name = "DataTableID") String dataTableID,
			@UpnpInputArgument(name = "DataTableKeyName") String dataTableKeyName, 
			@UpnpInputArgument(name = "DataTableKeyValue") String dataTableKeyValue) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "setDataStoreTableKeyValue(\"" + dataTableID + ", "+ dataTableKeyName + ", "+ dataTableKeyValue +")");

		if (mDataStoreInterface != null) {
			try {
				mDataStoreInterface.setDataStoreTableKeyValue(dataTableID, dataTableKeyName, dataTableKeyValue);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "DataTableKeyValue"))
	public String getDataStoreTableKeyValue(@UpnpInputArgument(name = "DataTableID") String dataTableID,
			@UpnpInputArgument(name = "DataTableKeyName") String dataTableKeyName) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "getDataStoreTableKeyValue(\"" + dataTableID + ", "+ dataTableKeyName +")");

		String dataTableKeyValue = "";
		if (mDataStoreInterface != null) {
			try {
				dataTableKeyValue = mDataStoreInterface.getDataStoreTableKeyValue(dataTableID, dataTableKeyName);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
		return dataTableKeyValue;
	}

	@UpnpAction
	public void removeDataStoreTableKeyValue(@UpnpInputArgument(name = "DataTableID") String dataTableID,
			@UpnpInputArgument(name = "DataTableKeyName") String dataTableKeyName) throws SensorMgtException {
		if (DEBUG) Log.d(TAG, "removeDataStoreTableKeyValue(\"" + dataTableID + ", "+ dataTableKeyName +")");

		if (mDataStoreInterface != null) {
			try {
				mDataStoreInterface.removeDataStoreTableKeyValue(dataTableID, dataTableKeyName);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "DataTransportURL"))
	public String getDataStoreTransportURL(@UpnpInputArgument(name = "DataTableID") String dataTableID) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG, "getDataStoreTransportURL(\"" + dataTableID +")");

		String dataTransportURL = "";
		if (mDataStoreInterface != null) {
			try {
				dataTransportURL = mDataStoreInterface.getDataStoreTransportURL(dataTableID);
			} catch (UPnPException e) {
				throw new SensorMgtException(e.getErrorNum(), e.getMessage());
			}
		}
		return dataTransportURL;
	}

	public String getLastChange() {
		return lastChange;
	}
	
	public void setLastChange(String lastChange) {
		this.lastChange = lastChange;
		
	}

	@Override
	public void onLastChangeEvent(String event) {
		setLastChange(event);
		sendLastChange();
	}
	
	
//	private class EventTimerTask extends TimerTask {          
//		@Override
//		public void run() {
//
//	    	sendLastChange(getLastChange());
//			
//		}
//	}
}