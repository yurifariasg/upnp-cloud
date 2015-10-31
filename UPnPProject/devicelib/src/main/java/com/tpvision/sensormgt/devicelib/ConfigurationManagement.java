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
import java.util.Timer;
import java.util.TimerTask;


import org.fourthline.cling.binding.annotations.*;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import com.tpvision.sensormgt.datamodel.DatamodelInterfaceImpl;
import com.tpvision.sensormgt.datamodel.DebugDatamodel;
import com.tpvision.sensormgt.datamodel.UPnPException;

import android.util.Log;


@UpnpService(
        serviceId = @UpnpServiceId("ConfigurationManagement"),
        serviceType = @UpnpServiceType(value = "ConfigurationManagement", version = 2)
)

@UpnpStateVariables(
		{
			@UpnpStateVariable(name = "A_ARG_TYPE_SupportedDataModels", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_StructurePath", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_PartialPath", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_SearchDepth", datatype = "ui4", defaultValue = "0", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_StructurePathList", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_InstancePathList", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_ContentPathList", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_ParameterValueList", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_NodeAttributePathList", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_NodeAttributeValueList", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_ChangeStatus", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_Filter", datatype = "string", sendEvents = false),
			
			//TODO: implement createInstance
			@UpnpStateVariable(name = "A_ARG_TYPE_InstancePath", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_MultiInstancePath", datatype = "string", sendEvents = false),
			@UpnpStateVariable(name = "A_ARG_TYPE_ParameterInitialValueList", datatype = "string", sendEvents = false),
			
			@UpnpStateVariable(name = "ConfigurationUpdate", datatype = "string", sendEvents = true),
			@UpnpStateVariable(name = "CurrentConfigurationVersion", datatype = "ui4", sendEvents = true),
			@UpnpStateVariable(name = "SupportedDataModelsUpdate", datatype = "string", sendEvents = true),
			@UpnpStateVariable(name = "SupportedParametersUpdate", datatype = "string", sendEvents = true)
			//@UpnpStateVariable(name = "AlarmsEnabled", datatype = "boolean", sendEvents = true)
		}
)

public class ConfigurationManagement {


    private static final String TAG = "ConfigurationManagement";
    private static final boolean DEBUG = DebugDatamodel.DEBUG_UPNP;
	private static final boolean DEBUG_EVENTING = DebugDatamodel.DEBUG_EVENTING;
	private DatamodelInterfaceImpl mDatamodelInterface=null;
	private PropertyChangeSupport propertyChangeSupport;
	
	private static final long TIMEOUT = 2000; //400ms delay before sending event
	private Timer mTimer = new Timer();
	private TimerTask mTimerTask = new EventTimerTask();
	
	public ConfigurationManagement() {
    	this.propertyChangeSupport = new PropertyChangeSupport(this);
    }
  
    
    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
    
  
    public void setDatamodel(DatamodelInterfaceImpl datamodelInterfaceImpl) {
    	mDatamodelInterface =  datamodelInterfaceImpl;
    }
    
    /**
     * Send the current configuration update as an initial event when a control point subscribes
     */
    public void sendInitialEvents() {
    	
    	Log.i(TAG, "Sending initial events");
    	
    	mTimerTask.cancel();
		mTimerTask = new EventTimerTask();
		mTimer.purge();
		mTimer.schedule(mTimerTask, TIMEOUT);
    	
    }
    
    
    /**
     * Send ConfigurationUpdate Event
     */
    public void sendConfigurationUpdate() {
    
    	if (DEBUG_EVENTING) Log.d(TAG, "sendConfigurationUpdate event:"+getConfigurationUpdate());
    	getPropertyChangeSupport().firePropertyChange("ConfigurationUpdate", false, true); 

    }    
    
    /**
     * Send ConfigurationUpdate Event
     */
    public void sendCurrentConfigurationVersion() {
    
    	if (DEBUG_EVENTING) Log.d(TAG, "sendCurrentConfigurationVersion event:"+getCurrentConfigurationVersion());
    	getPropertyChangeSupport().firePropertyChange("CurrentConfigurationVersion", false, true); 

    }    
    
    /**
     * Send SupportedDataModelsUpdate Event
     */
    public void sendSupportedDataModelsUpdate() {
    
    	if (DEBUG_EVENTING) Log.d(TAG, "sendSupportedDataModelsUpdate event:"+getSupportedDataModelsUpdate());
    	getPropertyChangeSupport().firePropertyChange("SupportedDataModelsUpdate", false, true); 

    }   
    
    /**
     * Send SupportedDataModelsUpdate Event
     */
    public void sendSupportedParametersUpdate() {
    
    	if (DEBUG_EVENTING) Log.d(TAG, "sendSupportedParametersUpdate event:"+getSupportedParametersUpdate());
    	getPropertyChangeSupport().firePropertyChange("SupportedParametersUpdate", false, true); 

    }   
    
    /**
     * Send AlarmsEnabled Event
     */
    public void sendAlarmsEnabled() {
    
    	if (DEBUG_EVENTING) Log.d(TAG, "sendAlarmsEnabled event:");
    	getPropertyChangeSupport().firePropertyChange("sendAlarmsEnabled", false, true); 

    }   
    
    
    
    @UpnpAction(out = @UpnpOutputArgument(name = "SupportedDataModels"))
    public String getSupportedDataModels() {
    	
    	if (DEBUG) Log.d(TAG,"getSupportedDataModels()");
    	
    	return mDatamodelInterface.getSupportedDataModels();
    }
    
    @UpnpAction(out = @UpnpOutputArgument(name = "StateVariableValue", stateVariable = "SupportedDataModelsUpdate"))
    public String getSupportedDataModelsUpdate() {
    	
    	if (DEBUG) Log.d(TAG,"getSupportedDataModelsUpdate()");
    	
    	return mDatamodelInterface.getSupportedDataModelsUpdate();
    }
    
    @UpnpAction(out = @UpnpOutputArgument(name = "StateVariableValue", stateVariable = "SupportedParametersUpdate"))
    public String getSupportedParametersUpdate() {
    	
    	if (DEBUG) Log.d(TAG,"getSupportedParametersUpdate()");
    	
    	return mDatamodelInterface.getSupportedParametersUpdate();
    }
    
    
	@UpnpAction(out = @UpnpOutputArgument(name = "Result", stateVariable = "A_ARG_TYPE_StructurePathList"))
    public String getSupportedParameters(@UpnpInputArgument(name = "StartingNode", stateVariable = "A_ARG_TYPE_StructurePath") String structurePath,
    		@UpnpInputArgument(name = "SearchDepth") UnsignedIntegerFourBytes searchDepth) throws SensorMgtException {
     
        if (DEBUG) Log.d(TAG,"getSupportedParameters(\""+structurePath+"\", "+searchDepth+")");
        
        String parameters="";
        try {
        	if (mDatamodelInterface!=null)
        		parameters = mDatamodelInterface.getSupportedParameters(structurePath, searchDepth.getValue().intValue());
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage());
		}
        
        return parameters;
    }
	
	
	@UpnpAction(out = @UpnpOutputArgument(name = "Result", stateVariable = "A_ARG_TYPE_InstancePathList"))
    public String getInstances(@UpnpInputArgument(name = "StartingNode", stateVariable = "A_ARG_TYPE_PartialPath") String partialPath,
    		@UpnpInputArgument(name = "SearchDepth") UnsignedIntegerFourBytes searchDepth) throws SensorMgtException {
     
		if (DEBUG) Log.d(TAG,"getSupportedInstances(\""+partialPath+"\", "+searchDepth+")");
     
		String instances="";
		
        try {
        	if (mDatamodelInterface!=null)
        		instances = mDatamodelInterface.getInstances(partialPath, searchDepth.getValue().intValue());
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage());
		}
   
        return instances;
	}
	
	
	@UpnpAction(out = @UpnpOutputArgument(name = "ParameterValueList"))
    public String getValues(@UpnpInputArgument(name = "Parameters" , stateVariable = "A_ARG_TYPE_ContentPathList") String contentPath) throws SensorMgtException {
     
		if (DEBUG) Log.d(TAG,"getValues(\""+contentPath+"\")");
     
		String valuesXML;
		
        try {
			valuesXML = mDatamodelInterface.getValues(contentPath);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage());
		}
   
        return valuesXML;
    }
	
	@UpnpAction(out = @UpnpOutputArgument(name = "Status", stateVariable = "A_ARG_TYPE_ChangeStatus"))
	public String setValues(@UpnpInputArgument(name = "ParameterValueList") String parameterValueList) throws SensorMgtException {
		
		if (DEBUG) Log.d(TAG,"setValues(\""+parameterValueList+"\")");
		
		String changeStatus;
		
        try {
        	changeStatus = mDatamodelInterface.setValues(parameterValueList);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage());
		}
   
        return changeStatus;
	
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "NodeAttributeValueList"))
	public String getAttributes(@UpnpInputArgument(name = "Parameters" , stateVariable = "A_ARG_TYPE_NodeAttributePathList") String nodeAttrPathList) throws SensorMgtException  {
		
		if (DEBUG) Log.d(TAG,"getAttributes(\""+nodeAttrPathList+"\")");
		
		String valuesXML;
		
        try {
			valuesXML = mDatamodelInterface.getAttributes(nodeAttrPathList);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage());
		}
   
        return valuesXML;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "Status", stateVariable = "A_ARG_TYPE_ChangeStatus"))
	public String setAttributes(@UpnpInputArgument(name = "NodeAttributeValueList") String nodeAttrValueList)  throws SensorMgtException {
		if (DEBUG) Log.d(TAG,"setAttributes(\""+nodeAttrValueList+"\")");

		String changeStatus;

		try {
			changeStatus = mDatamodelInterface.setAttributes(nodeAttrValueList);
		} catch (UPnPException e) {
			throw new SensorMgtException(e.getErrorNum(), e.getMessage());
		}

		return changeStatus;
	}
	
	
	@UpnpAction(out = @UpnpOutputArgument(name = "StateVariableValue", stateVariable = "ConfigurationUpdate"))
	public String getConfigurationUpdate() {
		
		if (DEBUG) Log.d(TAG,"getConfigurationUpdate()");
		
		return mDatamodelInterface.getConfigurationUpdate();
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "StateVariableValue", stateVariable = "CurrentConfigurationVersion"))
	public UnsignedIntegerFourBytes getCurrentConfigurationVersion() {
		return new UnsignedIntegerFourBytes(mDatamodelInterface.getCurrentConfigurationVersion());
	}
	
	private class EventTimerTask extends TimerTask {          
		@Override
		public void run() {

			sendConfigurationUpdate();
	    	sendCurrentConfigurationVersion();
	    	sendSupportedDataModelsUpdate(); 
	    	sendSupportedParametersUpdate();
	    	//sendAlarmsEnabled();
			
		}
	}
	
}