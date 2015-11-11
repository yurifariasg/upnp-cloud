package com.tpvision.sensormgt.datamodel;

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

/**
 * Datamodel interface implementation accesses information from the datamodel 
 * according to the UPnP ConfigurationManagement and SensorTransportGeneric actions
 * 
 */


import android.util.Log;

import com.tpvision.sensormgt.datamodel.DatamodelNode.NodeType;
import com.tpvision.sensormgt.sensormodel.DataItemInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class DatamodelInterfaceImpl implements DatamodelInterface {

	private static final String TAG = "DatamodelInterfaceImpl";
	private static final boolean DEBUG = DebugDatamodel.DEBUG_UPNP_INTERNAL;
	private static final boolean CSHACK = true;

	private DatamodelNode mDatamodelTree;
	private DatamodelNode mInstanceTree;
	private List<DataItem> mDataItemList;
	private ConfigurationUpdateListener mConfigurationUpdateListener;
	private ConfMgtEventControl mEventControl;
	private ConnectionControl mConnectionControl;
	private String mSupportedDataModelsUpdate = Datamodel.SUPPORTED_DATAMODELS_UPDATE;
	private String mSupportedParametersUpdate = Datamodel.SUPPORTED_PARAMETERS_UPDATE;
	
	
	public DatamodelInterfaceImpl(DatamodelNode instanceTree, List<DataItem> dataItemList) {
		this(Datamodel.inflateDatamodelTree(Datamodel.mDatamodelDefinition), instanceTree, dataItemList);
	}
	
	public DatamodelInterfaceImpl(DatamodelNode datamodelTree, DatamodelNode instanceTree, List<DataItem> dataItemList) {

		mDatamodelTree = datamodelTree;
		mInstanceTree = instanceTree;
		mDataItemList = dataItemList;
		
		//TODO: copy unused leaves (and nodes?) from the datamodel to the instance tree using default values 

		//FIXME:include attributes to XML
		//FIXME: for now just add the SensorEvent field
		DatamodelNode sensorMgt = findInstanceNodePath("/UPnP/SensorMgt/");
		LeafNode sensorEvents = (LeafNode) sensorMgt.addChildNode(new LeafNode(sensorMgt, "SensorEvents"));
		//LeafNode sensorEvents = (LeafNode) findInstanceNodePath("/UPnP/SensorMgt/");
		
		sensorEvents.setValueType("string");
		sensorEvents.setHasEOCAttribute(true);
		sensorEvents.setEOC(true);
		sensorEvents.setHasAOC(true);
		sensorEvents.setAOC(1);
		
	
		
		//setup eventing
		mEventControl = new ConfMgtEventControl(this);

		//register event listener with all dataitems
        Iterator<DataItem> data = dataItemList.iterator();
        while (data.hasNext()) {	
        	DataItem dataItem = data.next();
        	dataItem.addOnSensorChangeListener(mEventControl); 
        	//dataItem.addOnSensorValueChangedListener(mConnectionControl);//TODO: via eventControl 
        }
        
        //listen for parameter changes of the sensorEvents node
        sensorEvents.addOnParameterValueChangedListener(mEventControl);
		
        //setup http post system
		mConnectionControl = new ConnectionControl();
		//connection errors will be forwarded to EventControl
		mConnectionControl.addOnTransportConnectionErrorListener(mEventControl);
		//events received via the event control system will be forwarded to the ConnectionControl which can post the data to connected clients
		mEventControl.setOnSensorValueChangedListener(mConnectionControl);
        
	}

	/**
	 * Sets the listener for configurationUpdate Events
	 * 
	 */
	public void setConfigurationUpdateListener(ConfigurationUpdateListener configurationUpdateListener) {
		mConfigurationUpdateListener = configurationUpdateListener;
	}
	
	public void removeConfigurationUpdateListener() {
		mConfigurationUpdateListener = null;
	}

	
	public void configurationUpdate(String configurationUpdateCSV) {
		if (mConfigurationUpdateListener!=null) mConfigurationUpdateListener.onConfigurationUpdate(configurationUpdateCSV);
	}
	
	/**********************************************************************************************************************
	 * 
	 * UPnP Interface functions, called from device implementation
	 * 
	 **********************************************************************************************************************/

	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getSupportedDatamodels()
	 */
	public String getSupportedDataModels() {

		if (DEBUG) Log.d(TAG, "getSupportedDataModels");
		
		return XMLUPnPUtil.createXMLSupportedDatamodels(Datamodel.DATAMODEL_URI, Datamodel.DATAMODEL_LOCATION, Datamodel.DATAMODEL_URL, Datamodel.DATAMODEL_DESCRIPTION);
	}
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getSupportedDatamodelsUpdate()
	 */
	public String getSupportedDataModelsUpdate() {

		if (DEBUG) Log.d(TAG, "getSupportedDataModelsUpdate: " + mSupportedDataModelsUpdate);
		
		return mSupportedDataModelsUpdate;
	}
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getSupportedParametersUpdate()
	 */
	public String getSupportedParametersUpdate() {

		if (DEBUG) Log.d(TAG, "mSupportedParametersUpdate: " + mSupportedParametersUpdate);
		
		return mSupportedParametersUpdate;
	}
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getSupportedParameters(java.lang.String, int)
	 */
	public String getSupportedParameters(String startingNodePath, int searchDepth) throws UPnPException {

		if (DEBUG) Log.d(TAG, "getSupportedParameters(" + startingNodePath +", "+ searchDepth+")");

		List<String> parameterPathList = getSupportedParametersList(startingNodePath, searchDepth);

		return XMLUPnPUtil.createXMLStructurePathList(parameterPathList);
	}


	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getInstances(java.lang.String, int)
	 */
	public String getInstances(String startingNodePath, int searchDepth) throws UPnPException {
		List<String> instancePathList = getInstancesList(startingNodePath, searchDepth);
		
		return XMLUPnPUtil.createXMLInstancePathList(instancePathList);
	}

	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getValues(java.lang.String)
	 */
	public String getValues(String parameters) throws UPnPException {
		List<String> pathsList = XMLUPnPUtil.parseContentPathList(parameters);

		if (DEBUG) Log.d(TAG, "GetValues "+pathsList);
		
		HashMap<String, String> values = getValuesList(pathsList);
		
		String result = XMLUPnPUtil.createXMLParameterValueList(values);
		
		if (DEBUG) Log.d(TAG,"getValues result: "+result);
		
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getAttributes(java.lang.String)
	 */
	public String getAttributes(String attributePaths) throws UPnPException {
		List<String> pathsList = XMLUPnPUtil.parseAttributePathList(attributePaths);

		HashMap<String, String> values = getAttributesList(pathsList);
		return XMLUPnPUtil.createXMLNodeAttributeValueList(values);
	}
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#setAttributes(java.lang.String)
	 */
	public String setAttributes(String nodeAttrValueList) throws UPnPException {
		HashMap<String, String> attrValuesList = XMLUPnPUtil.parseNodeAttrValueList(nodeAttrValueList);

		return setNodeAttrValuesList(attrValuesList);
	}
	
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getSelectedValues(java.lang.String)
	 */
	public String getSelectedValues(String startingNodePath, String filter) throws UPnPException {
		//List<String> pathsList = XMLUPnPUtil.parseContentPathList(parameters);
		
		if (!checkStructurePathSyntax(startingNodePath)) throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_SYNTAX);
		
		
		
		
		
		HashMap<String, String> values = getSelectedValuesList(startingNodePath, filter);
		return XMLUPnPUtil.createXMLParameterValueList(values);
	}
	

	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#setValues(java.lang.String)
	 */
	public String setValues(String parameterValues) throws UPnPException {
		HashMap<String, String> pathsList= XMLUPnPUtil.parseParameterValuesList(parameterValues);
		
		String status = setValuesList(pathsList);
		return status;
	}	
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getConfigurationUpdate()
	 */
	public String getConfigurationUpdate() {
		return mEventControl.getConfigurationUpdate();
	}
	
	/* (non-Javadoc)
	 * @see gem.sensormgt.datamodel.DatamodelInterface#getCurrentConfigurationVersion()
	 */
	public int getCurrentConfigurationVersion() {
		return mEventControl.getCurrentConfigurationVersion();	
	}
	
	/**
	 * Parses the SensorRecordInfo and verifies that all fieldnames refer to available sensors/dataItems
	 * If all fieldnames or OK, the ClientId is written and a the connection is setup to send future changes to the requested URL.
	 * A connectionID is returned (for future cancellation of the connection.)
	 * 
	 */
	public String connectSensor(String sensorID, String sensorClientID, String sensorURN, String sensorRecordInfo, boolean sensorDataTypeEnable, String transportURL) throws UPnPException {
	
		//parse xml, and throw exception if syntax is wrong
		List<SensorRecordInfo> requestedRecordInfoList  = XMLUPnPUtil.parseSensorRecordInfo(sensorRecordInfo);
		
		//check if ALL fieldnames are ok, throws upnp exception if not ok
		checkReadSensorParams(sensorID, sensorURN, requestedRecordInfoList);
				
		//Set the sensorClientId of this sensor
		HashMap<String, String> clientID = new HashMap<String, String>();
		clientID.put("ClientID",sensorClientID);
		writeSensor(sensorID, sensorURN, clientID);
		
		//register transportURL, Setup connection, add event listener, send data when available. 
		return connectSensor(sensorID, sensorClientID, sensorURN, requestedRecordInfoList, sensorDataTypeEnable, transportURL);
	}
	
	public void disconnectSensor(String sensorID, String transportURL, String transportConnectionID) throws UPnPException {
		
		//test if sensorID is OK
		boolean sensorIDFound=false;
		Iterator<DataItem> it = mDataItemList.iterator();
		while (it.hasNext() && !sensorIDFound) {
			DataItem dataItem = it.next();
			sensorIDFound = dataItem.getSensorID().contentEquals(sensorID);
		}
		if (!sensorIDFound) {

			throw new UPnPException(UPnPSensorTransportErrorCode.SENSORID_NOT_FOUND);
		}
				
		mConnectionControl.disconnectSensor(sensorID, transportURL, transportConnectionID);		
	}
	
	
	//TODO: support for reading multiple sensor records
	/**
	 * locates Sensor based on SensorID. Finds all dataItems corresponding to that sensor. Reads their values, and returns a DataRecord
	 * DataItems in the DataRecord contain name (prefixed if a prefix was added in the sensorRecord, and will include dataType descriptions if the
	 * sensorDataTypeEnable flag is set.
	 * 
	 * The DataRecord count, indicates how many records can be read. Currently only one record is read from the sensor at a time.
	 */
	@Override
	public String readSensor(String sensorID, String sensorClientID, String sensorURN, String sensorRecordInfo, boolean sensorDataTypeEnable, int dataRecordCount) throws UPnPException {

		//parse xml, and throw exception if syntax is wrong
		List<SensorRecordInfo> requestedRecordInfoList = XMLUPnPUtil.parseSensorRecordInfo(sensorRecordInfo);
		
		//check if ALL fieldnames are ok, throws upnp exception if not ok
		checkReadSensorParams(sensorID, sensorURN, requestedRecordInfoList);
		
		//Set the sensorClientId of this sensor
		HashMap<String, String> clientID = new HashMap<String, String>();
		clientID.put("ClientID",sensorClientID);
		writeSensor(sensorID, sensorURN, clientID);
		
		DataRecordInfo record = readSensor(sensorID, sensorURN, requestedRecordInfoList, dataRecordCount);
		
		return XMLUPnPUtil.createXMLDataRecords(record,  sensorDataTypeEnable);
	}

//FIXME: consistent use of dataItemInfo and parseDataRecords	
	@Override
	public void writeSensor(String sensorID, String sensorURN, String dataRecords) throws UPnPException {

		//parse the datarecords xml, throw UPnPException if syntax is incorrect
	//	HashMap<String, String> fieldNames =   XMLUPnPUtil.parseDataRecords(dataRecords);
		ArrayList<DataItemInfo> items =  XMLUPnPUtil.parseDataRecords(dataRecords);
		
		
		HashMap<String, String> fieldNames = new HashMap<String, String>();
		
		for (DataItemInfo dataItem : items) {
			fieldNames.put(dataItem.getFieldName(), dataItem.getValue());
		}
		
		
		//check if ALL fieldnames are ok, thows upnp exception if not ok
		checkWriteSensorParams(sensorID, sensorURN, fieldNames);

		//no exceptions thrown so all dataItems are ok
		writeSensor(sensorID, sensorURN, fieldNames);
	}

	public String  getSensorTransportConnections(String sensorID) throws UPnPException {
		return XMLUPnPUtil.createXMLTransportConnections(mConnectionControl.getSensorTransportConnections(sensorID));
	}

	
/**********************************************************************************************************************
 * 
 * UPnP functions, internal definitions
 * 
 **********************************************************************************************************************/


	//TODO: check if startingNode needs to make distinction between leaf and single/multi instance nodes
	/**
	 * Returns supported parameters as a list of strings
	 * @throws UPnPException
	 */
	public List<String> getSupportedParametersList(String startingNodePath, int searchDepth) throws UPnPException {

		if (DEBUG) Log.d(TAG, "getSupportedParametersList("+startingNodePath+", "+ searchDepth+")");

		List<DatamodelNode> nodeList; 
		List<String> parameterPathList = new ArrayList<String>();

		if (!checkStructurePathSyntax(startingNodePath) || (searchDepth<0)) throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_SYNTAX);

		//find starting node in tree
		DatamodelNode startingNode = findNodePath(mDatamodelTree, startingNodePath);

		if (startingNode !=null) {
			nodeList = findSupportedParameters(startingNode,searchDepth);
			for (DatamodelNode node: nodeList) {
				if (CSHACK && node.getNodeName().equals("CollectionSpecific")) {
					String hack = node.getPath();
					hack = hack.substring(0,hack.length()-1);
					parameterPathList.add(hack);
					
				} 
				else
					parameterPathList.add(node.getPath());
			}
		} else {
			throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
		}

		return parameterPathList;
	}


	/**
	 * Traverse the instanceTree to find all Instance paths, returns a list of strings containing paths
	 * @param startingNodePath	
	 * @param searchDepth
	 * @return list of instance paths
	 * @throws UPnPException
	 */
	public List<String> getInstancesList(String startingNodePath, int searchDepth) throws UPnPException {

		if (DEBUG) Log.d(TAG, "Get Instances");

		List<DatamodelNode> nodeList; 
		List<String> instancePathList = new ArrayList<String>();

		if (!checkInstancePathSyntax(startingNodePath) || (searchDepth<0)) throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_SYNTAX);

		//find starting node in instance tree
		DatamodelNode startingNode = findNodePath(mInstanceTree, startingNodePath);
		if (startingNode !=null) {
			nodeList = findInstances(startingNode,searchDepth);
			for (DatamodelNode node: nodeList) {
				instancePathList.add(node.getPath());
			}			
		} else {
			throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
		}

		return instancePathList;
	}
	
	
	public HashMap<String, String> getValuesList(List<String> pathsList) throws UPnPException {

		//if (DEBUG) Log.d(TAG, "GetValuesList");

		HashMap<String, String> resultsMap = new HashMap<String, String>();

		//get the values for each path, when the path does not indicate a leaf node all values in the subtree need to be returned
		for (String path: pathsList) {
			//if (DEBUG) Log.d(TAG, "Process values, path: "+path);

			//find starting node in instance tree
			DatamodelNode tree = findNodePath(mInstanceTree, path);

			//for each path traverse the tree, and get the values for all leaves
			if (tree != null) {
				resultsMap.putAll(findValues(tree));
				
			} else {
				throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
			}
		}
		return resultsMap;
	}
	
	public HashMap<String, String> getAttributesList(List<String> nodePathsList) throws UPnPException {
		if (DEBUG) Log.d(TAG, "GetAttributesList");

		HashMap<String, String> nodeAttrValueList = new HashMap<String, String>();
		
		for (String path: nodePathsList) {
			
			checkInstancePathSyntax(path);
			
			//find starting node in instance tree
			DatamodelNode node = findNodePath(mInstanceTree, path);

			StringBuffer attrs= new StringBuffer(); 
			if (node!=null) {
				

				if ((node.getNodeType()!=NodeType.SINGLE_INSTANCE) && (!node.getNodeName().contentEquals("CollectionSpecific"))){
					attrs.append("Access,"+node.getAccess()); //access attribute for all node types
					if (node.getNodeType()==NodeType.LEAF) {
						LeafNode leafNode = (LeafNode) node;

						//FIXME:collection specific is recognised as a leafnode					
						if (CSHACK && node.getNodeName().contentEquals("CollectionSpecific")) {
							leafNode.setValueType("string");
						}

						attrs.append(",Type,"+node.getValueType());
						//if (leafNode.hasEOCAttribute()) { //ALWAYS show EOC???
						if (leafNode.isEOC()) attrs.append(",EventOnChange,1");
						else attrs.append(",EventOnChange,0");
						if(leafNode.hasAOC()) {
							attrs.append(",AlarmOnChange,"+leafNode.getAOC());
						}
						if(leafNode.hasVersion()) {
							attrs.append(",Version,"+leafNode.getVersion());
						}
						//}
					} else { //event on change for all multi_instance nodes ????
						if (node.getNodeType()==NodeType.MULTI_INSTANCE)
							attrs.append(",EventOnChange,0");
					}
					//hack to avoid instancenr nodes
					//if ((node.getValueType()!=null) && (!node.getValueType().isEmpty())) nodeAttrValueList.put(path, attrs.toString());	
				}	
				nodeAttrValueList.put(path, attrs.toString());
			} else throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
				
		}
		
		
		return nodeAttrValueList;
	}

	
	public HashMap<String, String> getSelectedValuesList(String startingNodePath, String filter) throws UPnPException {

		if (DEBUG) Log.d(TAG, "GetSelectedValuesList");

		HashMap<String, String> resultsMap = new HashMap<String, String>();

		//find starting node in instance tree
		DatamodelNode startingNode = findNodePath(mInstanceTree, startingNodePath);
		if (startingNode !=null) {
			resultsMap = findSelectedValues(startingNode, startingNodePath, filter);//FIXME: shoudl not be startingNode, but tree
		} else {
			throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
		}


		return resultsMap;
	}

	public String setValuesList(HashMap<String, String> parameterValuesList) throws UPnPException {

		if (DEBUG) Log.d(TAG, "setValuesList");
		
		//get the values path, 
		for (Map.Entry<String, String> entry : parameterValuesList.entrySet()) {
			// use "entry.getKey()" and "entry.getValue()"
			if (checkParameterPathSyntax(entry.getKey())) { 
				//find path
				DatamodelNode leaf = findNodePath(mInstanceTree, entry.getKey());

				if ((leaf != null) && (leaf.getNodeType() == NodeType.LEAF) ) { //it should be a leaf, but lets double check
					if (DEBUG) Log.d(TAG, "setValuesList: SetValue: "+entry.getValue());
					((LeafNode) leaf).setValue(entry.getValue());
				} else {
					throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
				}
			} else 
				throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME); //path syntax is not correct. But lets not report it as invalid XML
		}
		//When changes are not effected immediately it is possible to return "ChangesComitted"
		//Here we assume they are always applied immmediately
		String status = "ChangesApplied";
		return status;
	}
	

	/**
	 * only supports setting EOC
	 * @param nodeAttrValuesList
	 * @return
	 * @throws UPnPException
	 */
	public String setNodeAttrValuesList(HashMap<String, String> nodeAttrValuesList) throws UPnPException {

		//get the values path, 
		for (Map.Entry<String, String> entry : nodeAttrValuesList.entrySet()) {
			// use "entry.getKey()" and "entry.getValue()"
			if (checkParameterPathSyntax(entry.getKey())) { 
				//find path
				DatamodelNode node = findNodePath(mInstanceTree, entry.getKey());

				if ((node != null) && (node.getNodeType() == NodeType.LEAF) ) { //it should be a leaf (eoc only supported on leafnodes)
					LeafNode leafnode = (LeafNode) node; 
					if (entry.getValue()!=null) {

						if (!leafnode.hasEOCAttribute()) throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
						if (!leafnode.isWritable()) throw new UPnPException(UPnPConfMgtErrorCode.READ_ONLY_VIOLATION);
						if (entry.getValue().contentEquals("1")) {

							leafnode.setEOC(true);
						}
						else if (entry.getValue().contentEquals("0"))
							leafnode.setEOC(false);
						else throw new UPnPException(UPnPConfMgtErrorCode.INVALID_VALUE);
					} else throw new UPnPException(UPnPConfMgtErrorCode.INVALID_VALUE);
				} else {
					throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME);
				}
			} else 
				throw new UPnPException(UPnPConfMgtErrorCode.NO_SUCH_NAME); //path syntax is not correct. But lets not report it as invalid XML
		}
		//When changes are not effected immediately it is possible to return "ChangesComitted"
		//Here we assume they are always applied immmediately
		String status = "ChangesApplied";
		return status;
	}

	/**
	 * 
	 * Checks if all fieldnames correspond to Sensors in the dataItem list.
	 * If all fieldnames are found, the transportURL is stored. and a connectionID is returned
	 * If a field name is not found, an exception is thrown, and no registration is setup
	 */
	public String connectSensor(String sensorID, String clientID, String sensorURN, List<SensorRecordInfo> requestedRecordInfoList, boolean sensorDataTypeEnable, String transportURL) throws UPnPException {

		ArrayList<DataItem> dataItemRegistrationList = new ArrayList<DataItem>();
		
		for (SensorRecordInfo sensorRecord: requestedRecordInfoList)  {
			DataItem dataItem = findSensorDataItem(sensorID, sensorURN, sensorRecord.getFieldName());
			dataItemRegistrationList.add(dataItem);
			Log.d(TAG,"dataItem "+dataItem.getName()+" value: "+dataItem.getSensorValue());

		}	
		
		return mConnectionControl.connectSensor(sensorID, clientID, sensorURN, transportURL, requestedRecordInfoList, dataItemRegistrationList, sensorDataTypeEnable);
	}
	
	
	/**
	 * Writes all values to data items indicated by SensorId, SensorUrn, and field name
	 * @param sensorID
	 * @param sensorURN
	 * @param fieldValues
	 * @throws UPnPException 
	 */
	public void writeSensor(String sensorID, String sensorURN, HashMap<String, String> fieldValues) throws UPnPException {

		//now the values can be written
		for (Map.Entry<String, String> entry : fieldValues.entrySet()) {

			DataItem dataItem = findSensorDataItem(sensorID, sensorURN, entry.getKey());
			dataItem.writeSensorValue(entry.getValue());
			Log.d(TAG,"dataItem "+dataItem.getName()+" value: "+dataItem.getSensorValue());

		}	
	}

	/**
	 * Internal readSensor function, the dataItems listed in the fieldnames of the Sensor corresponding to SensorId and Sensor URN are read 
	 * A key-value pair is returned for each fieldname
	 * This implementation only supports sensors that return 1 dataItem
	 * @param sensorID
	 * @param sensorClientID
	 * @param sensorURN
	 * @param fieldNames
	 * @param dataRecordCount
	 * @return
	 * @throws UPnPException 
	 */
	public DataRecordInfo readSensor(String sensorID, String sensorURN, List<SensorRecordInfo> sensorRecordInfoList, int dataRecordCount) throws UPnPException {
		
		ArrayList<DataItemInfo> dataItems = new ArrayList<DataItemInfo>();
		DataRecordInfo record = new DataRecordInfo(dataItems);
		
//FIXME:		
		//in the current implementation a sensor always reports just 1 dataItem 
//		if (dataRecordCount<1) return fieldValues;
		
		for (SensorRecordInfo sensorRecord: sensorRecordInfoList)  {
			//get info from sensor representation, and store for information only
			DataItem di = findSensorDataItem(sensorID, sensorURN, sensorRecord.getFieldName());
			DataItemInfo dii = new DataItemInfo(di.getName(), di.getType(), di.getEncoding(), di.getNameSpace());
			dii.setValue(di.readSensorValue()); 
			di.setReadSensorProcessed(); //register that we processed the dataItem
			dii.setPrefix(sensorRecord.getPrefix());
			
			dataItems.add(dii);
			Log.d(TAG,"dataItem "+di.getName()+" value: "+di.getSensorValue());
		}
		
		return record;
		
	}
	
/**********************************************************************************************************************
 * 
 * Internal syntax check functions
 * 
 **********************************************************************************************************************/

	/**
	 * Checks if the nodeName consists of allowed characters
	 * (as defined in section 2.3.1.2. Definition of Grammar of ConfigurationManagement:2 Service Template Version 1.01) 
	 * 
	 * rule: no '.', '-', '/' 
	 * @param nodeName
	 * @return true if valid
	 */  
	//TODO: check rule
	public boolean checkNodeNameSyntax(String nodeName) {

		CharSequence forbiddenChar = ".-/";

		boolean valid=false;
		if ((nodeName!=null) && !nodeName.isEmpty()) {

			int i=0;
			valid = true;
			while (i<forbiddenChar.length() && valid) {
				valid =  nodeName.indexOf(forbiddenChar.charAt(i))==-1; //char not found
				i++;
			}
		}

		return valid;
	}


	/**
	 * Checks if the path complies with the structure path specification 
	 * (as defined in section 2.3.1.2. Definition of Grammar of ConfigurationManagement:2 Service Template Version 1.01)
	 * 
	 * rule:  "/"  (nodename "/" | "#/" )* nodename?
	 * @param structurePath
	 * @return true if valid
	 */
	//TODO: instance numbers are treated as node names, and will not create an error
	public boolean checkStructurePathSyntax(String structurePath) {

		boolean valid = false;
		if ((structurePath!=null) && (!structurePath.isEmpty()) && (structurePath.startsWith("/"))) {

			int pos = 1;
			int nextpos = 0;
			valid = true;
			boolean morePaths = structurePath.length()>1;
			while (morePaths && valid) {
				nextpos = structurePath.indexOf('/', pos);
				if (nextpos != -1) {
					String nodeName = structurePath.substring(pos, nextpos);
					if (!nodeName.equals("#")) 
					{  //# is ok, if its not a # check the node name
						valid = checkNodeNameSyntax(nodeName); 
					}
					pos = nextpos + 1;	
				} else { 
					morePaths=false;
				}
			}
			if (valid && (pos<structurePath.length())) { //test for leafname
				String leaf = structurePath.substring(pos);
				valid = (!leaf.equals("#")) && checkNodeNameSyntax(leaf); 
			}
		}

		if (DEBUG) Log.d(TAG,"StructurePathSyntax check: "+ valid);

		return valid;
	}


	/**
	 * Checks if the path complies with the instancePath specification 
	 * (as defined in section 2.3.1.2. Definition of Grammar of ConfigurationManagement:2 Service Template Version 1.01)
	 * 
	 * Implementation checks that the instance path does not contain a '#' node, and checks node syntax.
	 * It does not check if the path is valid, or if instance id's exist
	 * 
	 * @param structurePath
	 * @return true if valid
	 */
	public boolean checkInstancePathSyntax(String instancePath) {
		
		boolean valid = false;
		if ((instancePath!=null) && (!instancePath.isEmpty()) && (instancePath.startsWith("/"))) {
			String[] nodes = instancePath.split("/");
			int i=1; //1 because we want to ignore the first empty node, since path starts with /
			valid =true;
			while ((i < nodes.length) && valid) {
				valid = checkNodeNameSyntax(nodes[i]) && (!nodes[i].equals("#"));
				i++;
			}	
		}
		
		return valid;
	}
	
	/** 
	 * Checks if the path is a correct parameterPath, ending at a leaf node
	 * It does not check if the path is valid, or if instance id's exist
	 * @param parameterPath
	 * @return true if valid
	 */
	public boolean checkParameterPathSyntax(String parameterPath){

		return checkInstancePathSyntax(parameterPath) && !parameterPath.endsWith("/"); //check its a leaf
	}

	/**
	 * check if the Dataitem fields can be found. 
	 * Throws UPnP exceptions; SensorID not found, SensorUrn not found, DataItem not found, DataItem not readable
	 * @param sensorID 
	 * @param sensorURN
	 * @param fieldValues, list of field-names
	 * @throws UPnPException
	 */
	public void checkReadSensorParams(String sensorID, String sensorURN, List<SensorRecordInfo> sensorRecordInfoList) throws UPnPException {
		
		//check if we can find all fieldValues
		for (SensorRecordInfo sensorRecord: sensorRecordInfoList) {			
			//throws UPnP Exception if not found
			DataItem dataItem = findSensorDataItem(sensorID, sensorURN, sensorRecord.getFieldName());
			
			//TODO: 
			//check if its readable
//			if (!dataItem.isReadable()) throw new UPnPException(UPnPSensorTransportErrorCode.SENSORDATAITEM_READONLY); 	
		}
	}
	
	
	/**
	 * check if the Dataitem fields can be found. 
	 * Throws UPnP exceptions; SensorID not found, SensorUrn not found, DataItem not found, DataItem not writable
	 * @param sensorID 
	 * @param sensorURN
	 * @param fieldValues, Hashmap of key-value pairs containing field-names and values
	 * @throws UPnPException
	 */
	public void checkWriteSensorParams(String sensorID, String sensorURN, HashMap<String, String> fieldValues) throws UPnPException
	{
		//check if we can find all fieldValues
		for (Map.Entry<String, String> entry : fieldValues.entrySet()) {			
			//throws UPnP Exception if not found
			DataItem dataItem = findSensorDataItem(sensorID, sensorURN, entry.getKey());
			
			//TODO: 
			//check if its writable
//			if (!dataItem.isWritable()) throw new UPnPException(UPnPSensorTransportErrorCode.SENSORDATAITEM_READONLY); 	
		}
	}
	
	
	
/**********************************************************************************************************************
 * 
 * Internal functions
 * 
 **********************************************************************************************************************/

	/**
	 * Traverses datamodel tree in breath first order, to find all leaf nodes. And returns a list of found nodes.
	 * If a multiInstanceNode is the last node in the path, the '#"-node is included
	 * @param tree, tree or starting node to search 
	 * @param depth, search depth levels down the tree 
	 * @return list of found nodes
	 */	
	private List<DatamodelNode> findSupportedParameters(DatamodelNode tree, int depth)
	{

		ArrayList<DatamodelNode> result = new ArrayList<DatamodelNode>();

		if (tree == null) {
			if (DEBUG) Log.d(TAG, "findSupportedParameters on null tree");
			return result; 	
		}

		if (DEBUG) Log.d(TAG, "findSupportedParameters : from " + tree.getPath()+ ", depth " +depth);

		/* Create a queue to hold node pointers. */
		Queue<DatamodelNode> queue =new LinkedList<DatamodelNode>();
		queue.add(tree); 
		int initialLevel = tree.getLevel();

		DatamodelNode traverse;

		while (!queue.isEmpty()) {

			traverse = queue.remove();  

			//if depth is 0, add leaves
			if (depth==0) {
				if (traverse.getNumChildren()==0) {
					result.add(traverse); 
					if (DEBUG) Log.d(TAG, "leaf node :" + traverse.getPath()+ ", " +traverse.getNodeTypeString());
				}
			}
			else {
				//check if leaf found before reaching max depth
				if ((traverse.getNodeType()==NodeType.LEAF) && (depth > (traverse.getLevel() - initialLevel))) result.add(traverse);
				else { 
					//check if node is at the required depth (so we have to add it)
					if ((traverse.getLevel() - initialLevel)==depth) { 
						if ((traverse.getNodeType()==NodeType.MULTI_INSTANCE)  && !traverse.getPath().endsWith("#/")) { 
							//multi instance node, but not the #, so take the next node
							result.add(traverse.getChildNode(traverse.getPath()+"#/")); 
						}
						else
							result.add(traverse);
						if (DEBUG) Log.d(TAG, "node " + traverse.getLevel() + ", " + traverse.getPath() + ", " + traverse.getNodeTypeString());
					}
				}
			}

			queue.addAll(traverse.getChildNodes());

		}

		/* Clean up the queue. */
		queue=null;

		return result;
	}

	/**
	 * Traverses datamodel tree in breath first order, to find all instance nodes. And returns a list of found nodes.
	 * @param tree, tree or starting node to search 
	 * @param depth, search depth levels down the tree 
	 * @return list of found nodes
	 */	
	private List<DatamodelNode> findInstances(DatamodelNode tree, int depth)
	{
		ArrayList<DatamodelNode> result = new ArrayList<DatamodelNode>();

		if (tree == null) return result; 	

		/* Create a queue to hold node pointers. */
		Queue<DatamodelNode> queue =new LinkedList<DatamodelNode>();
		queue.add(tree); 
		int initialLevel = tree.getLevel();

		DatamodelNode traverse;

		while (!queue.isEmpty()) {

			traverse = queue.remove();  

			//if depth is 0, add leaves
			if (depth==0) {
				if (traverse.getNodeType()==NodeType.INSTANCE) {
					result.add(traverse); 
					if (DEBUG) Log.d(TAG, "Instance node :" + traverse.getPath()+ ", " +traverse.getNodeTypeString());
				}
			}
			else {
				//check if leaf found before reaching max depth
				if ((traverse.getNodeType()==NodeType.INSTANCE) && (depth >= (traverse.getLevel() - initialLevel))) {
					result.add(traverse);
				}
			}

			queue.addAll(traverse.getChildNodes());

		}
		/* Clean up the queue. */
		queue=null;

		return result;
	} 
	
	/**
	 * Traverses datamodel tree in breath first order, to find all leaf nodes. And returns a hashmap of found nodes and their values.
	 * @param tree, tree or starting node to search 
	 * @return list of found nodes
	 */	
	private HashMap<String, String> findValues(DatamodelNode tree)
	{

		HashMap<String, String> resultsMap = new HashMap<String, String>();

		if (tree == null) return resultsMap; 	

		/* Create a queue to hold node pointers. */
		Queue<DatamodelNode> queue =new LinkedList<DatamodelNode>();

		queue.add(tree); 

		DatamodelNode traverse;
		while (!queue.isEmpty()) {

			traverse = queue.remove();  

			if (traverse.getNodeType()==NodeType.LEAF) {
				String leafPath = traverse.getPath();
				//if (DEBUG) Log.d(TAG, "Get value for leaf :" + leafPath);
				//TODO: if getValue was not found
				resultsMap.put(leafPath, ((LeafNode) traverse).getValue());
			}

			queue.addAll(traverse.getChildNodes());
		} 

		/* Clean up the queue. */
		queue.clear();
		return resultsMap;
	} 
	
	/**
	 * Traverses datamodel tree in breath first order, to find all leaf nodes. And returns a hashmap of found nodes and their values.
	 * @param tree, tree or starting node to search 
	 * @return list of found nodes
	 */	
	private HashMap<String, String> findSelectedValues(DatamodelNode tree, String structurePath, String filter)
	{

		HashMap<String, String> resultsMap = new HashMap<String, String>();

		if (tree == null) return resultsMap; 	

		/* Create a queue to hold node pointers. */
		Queue<DatamodelNode> queue =new LinkedList<DatamodelNode>();

		queue.add(tree); 

		DatamodelNode traverse;
		while (!queue.isEmpty()) {

			traverse = queue.remove();  

			if (traverse.getNodeType()==NodeType.LEAF) {
				String leafPath = traverse.getPath();
				if (DEBUG) Log.d(TAG, "leaf :" + leafPath);
				
				//check path pattern
				if (checkPath(traverse.getPath(), structurePath)) 
				{
				//check filter
				
				//TODO: if getValue was not found
				resultsMap.put(leafPath, ((LeafNode) traverse).getValue());
				}
			}

			queue.addAll(traverse.getChildNodes());
		} 

		/* Clean up the queue. */
		queue.clear();
		return resultsMap;
	} 
	
	/**
	 * Traverses datamodel tree in breath first order, to find all nodes that correspond to the structurePath
	 * Returns a list of found nodes.
	 * Examples: /UPnP/SensorMgt/SensorCollections/#/Sensors/#/ returns all Sensors child nodes of all Sensors instances and all Collections instances
	 * Examples: /UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorEventsEnable returns all SensorEventsEnable leaf nodes of all Sensors and Collections instances
	 * @param tree, tree or starting node to search
	 * @param structurePath path including multi instance #-nodes to find all nodes in all instances
	 * @return list of found nodes
	 */	
	//
	public List<DatamodelNode> findNodes(DatamodelNode tree, String structurePath) {

		
		List<DatamodelNode> results = new ArrayList<DatamodelNode>();

		if (tree == null) return results; 	

		/* Create a queue to hold node pointers. */
		Queue<DatamodelNode> queue =new LinkedList<DatamodelNode>();

		queue.add(tree); 

		DatamodelNode traverse;
		
		while (!queue.isEmpty()) {

			traverse = queue.remove();  
			
			//check path pattern
			if (checkPath(traverse.getPath(), structurePath)) {
				//check filter
				
				//TODO: if getValue was not found
				results.add(traverse);
			}

			queue.addAll(traverse.getChildNodes());
		} 

		/* Clean up the queue. */
		queue.clear();
		return results;
	} 
	
	/** 
	 * Returns the dataItem indicated by SensorId, SensorURN and dataItem name
	 * Throws UPnPException if SensorId, SensorURN or DataItem name are not found in the set of available sensors 
	 * @param sensorID
	 * @param sensorURN
	 * @param dataItemName
	 * @throws UPnPException
	 */
	public DataItem findSensorDataItem(String sensorID, String sensorURN, String dataItemName) throws UPnPException {
		
		DataItem dataItem = null;
		boolean dataItemFound = false; //stop searching the dataitem list if the field is found
		boolean sensorIDFound = false;
		boolean sensorURNFound = false;
		Iterator<DataItem> iterator = mDataItemList.iterator();
		while (iterator.hasNext()&&!dataItemFound) {
			dataItem = (DataItem) iterator.next();

			sensorIDFound = sensorIDFound || (dataItem.getSensorID().contentEquals(sensorID)); //check if we ever saw the SensorID
			sensorURNFound = sensorURNFound || (dataItem.getSensorURN().contentEquals(sensorURN)); //check if we ever saw the SensorURN

			//check if this is the right Sensor with the correct URN and the correct dataitem name 
			dataItemFound = (dataItem.getSensorID().contentEquals(sensorID) && 
					dataItem.getSensorURN().contentEquals(sensorURN)) && 
					(dataItem.getName().contentEquals(dataItemName)); 
		}
		if (!dataItemFound) { //not found, check why and throw exception
			if (!sensorIDFound) throw new UPnPException(UPnPSensorTransportErrorCode.SENSORID_NOT_FOUND);
			else
				if (!sensorURNFound) throw new UPnPException(UPnPSensorTransportErrorCode.SENSORURN_NOT_FOUND);
				else 
					throw new UPnPException(UPnPSensorTransportErrorCode.DATAITEM_NOT_FOUND);	
		}
		
		return dataItem;
	}
	
	/** 
	 * Locates the node in the tree corresponding to the path.  
	 * Correctness of the path is not verified.
	 * If the node is not found null is returned  
	 * 
	 * @param path
	 */
	public DatamodelNode findStructurePath(String path) {
		return findNodePath(mDatamodelTree, path);
	}
	
	/** 
	 * Locates the node in the instance tree corresponding to the path.
	 * Correctness of the path is not verified.
	 * If the node is not found null is returned  
	 * @param path
	 */
	public DatamodelNode findInstanceNodePath(String path) {
		return findNodePath(mInstanceTree, path);
	}
	
	/** 
	 * Locates the node in the tree corresponding to the path.  
	 * @param path
	 */
	public DatamodelNode findNodePath(DatamodelNode tree, String path) {

		if (path.equals("/")) 
			return tree;

		DatamodelNode ret=null;
		boolean stillOk=false;
		String[] nodes = (path.split("/"));
		if ((nodes != null) && (nodes.length != 0)) {
			int i=1; //skip the / node
			boolean leaf = !path.endsWith("/");
			if (tree!=null) { //check the UPnP node
				stillOk = tree.getNodeName().equals(nodes[i]);
				i++;
			}
			DatamodelNode current = tree;
			while ((i < nodes.length)&&stillOk) { //each step along the way through the tree the path must be found
				String partialPath = DatamodelUtils.createPartialPath(nodes, i, leaf);
				current = current.getChildNode(partialPath);
				
				stillOk = (current !=null); 

				i++;
			}

			if (stillOk) ret=current;
		}	

		//if (DEBUG) Log.d(TAG,"NodePath: "+path+ "found " + stillOk);

		return ret;
	}
	
	/** 
	 * checks if the node path, matches with the pattern defined by checkPath
	 * a # in the check path will match any value in the node path. 
	 * Assumes semantically correct paths. 
	 */
	public boolean checkPath(String nodePath, String checkPath) {

		if (checkPath.equals("/")) 
			return true;

		String[] nodes = (nodePath.split("/"));
		String[] check = (checkPath.split("/"));

		if (nodes.length < check.length) 
			return false;

		boolean stillOk = true;
		int i = 0;
		while ((i < check.length) && stillOk) {
			stillOk = (check[i].contentEquals("#") || nodes[i].contentEquals(check[i]));
			i++;
		}	

		//if (DEBUG) Log.d(TAG,"checkPath: " + checkPath + " == "+ nodePath + ": " + stillOk);

		return stillOk;
	}


}