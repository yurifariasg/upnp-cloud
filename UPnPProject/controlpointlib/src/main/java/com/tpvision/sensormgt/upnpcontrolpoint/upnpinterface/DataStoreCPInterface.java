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

package com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface;


/**
 * Defines all asynchronous interface methods to implement a DataStore control point
 */

public interface DataStoreCPInterface {

	
/*****************************************************************************************************
 * 	DataTableGroup Management: CreateDataStoreGroups(), GetDataStoreGroups(), DeleteDataStoreGroups()
*****************************************************************************************************/	

	/**
	 * returns an XML document listing currently defined DataStore group identifiers
	 */
	public void getDataStoreGroups(GetDataStoreGroups callback);

	public interface GetDataStoreGroups {	
		public void onGetDataStoreGroups(String datastoreGroupListXML);
	}
	
	
/*****************************************************************************************************
*	DataTable Management: CreateDataStoreTable(), DeleteDataStoreTable(), ModifyDataStoreTable(), ResetDataStoreTable()	
*****************************************************************************************************/	
	
	/**
	 * creates a new DataTable
	 * Optional
	 */
	public void createDataStoreTable(String dataTableInfo, CreateDataStoreTable callback); 
	
	public interface CreateDataStoreTable {	
		public void onCreateDataStoreTable(String datatableID);
	}
	
	
	/**
	 * Deletes an existing DataTable. 
	 * Existing connections shall be closed after returning status for any pending operations
	 * Conditionally Optional
	 */
	public void deleteDataStoreTable(String dataTableID, DeleteDataStoreTable callback);
	
	public interface DeleteDataStoreTable {	
		public void onDeleteDataStoreTable();
	}
	
	/**
	 * 
	 */
	public void modifyDataStoreTable(String dataTableID, String dataTableInfoElementOrig, String dataTableInfoElementNew, ModifyDataStoreTable callback);
	
	public interface ModifyDataStoreTable {	
		public void onModifyDataStoreTable(String dataTableID);
	}
	
	
	/**
	 * Clear all DataRecord(s), DataTable Dictionary entries and Transport URLs for the DataTable
	 * Optional
	 */
	public void resetDataStoreTable(String dataTableID,
									boolean resetDataTableRecords, boolean resetDataTableDictionary, boolean resetDataTableTransport, ResetDataStoreTable callback);
	
	public interface ResetDataStoreTable {	
		public void onResetDataStoreTable();
	}
	
/*****************************************************************************************************
*	DataTable Information: GetDataStoreInfo(), GetDataStoreTableInfo(), ModifyDataStoreTable()	 
*****************************************************************************************************/	

	/**
	 * returns information about DataStore DataTable(s) currently supported by this DataStore service
	 */
	public void getDataStoreInfo(GetDataStoreInfo callback);

	public interface GetDataStoreInfo {	
		public void onGetDataStoreInfo(String datastoreInfoXML);
	}
	
	/**
	 * returns an XML document containing information about the DataTable associated  the DataTableID  argument
	 */
	public void getDataStoreTableInfo(String GUID, GetDataStoreTableInfo callback);
	
	public interface GetDataStoreTableInfo {	
		public void onGetDataStoreTableInfo(String datatableInfoXML);
	}
	

/*****************************************************************************************************
*	DataTable Read/Write: ReadDataStoreRecords(), WriteDataStoreRecords()
*****************************************************************************************************/		

	/**
	 * write DataRecord(s) to the DataTable identified by the DataTableID argument
	 */
	public void writeDataStoreTableRecords(String GUID, String dataRecordsXML, WriteDataStoreTableRecords callback);
	
	public interface WriteDataStoreTableRecords {	
		public void onWriteDataStoreTableRecords(String dataRecordsStatusXML);
	}
	
	/** 
	 * Returns DataRecord(s) from the DataTable indicated by the DataTableID argument. 
	 * The DataRecordFilter argument may specify a string to indicate which DataRecord(s) shall be returned.
	 */
	public void readDataStoreTableRecords(String dataTableId, String dataRecordFilter,
										  String dataRecordStart, int dataRecordCount, boolean dataRecordPropResolve, ReadDataStoreTableRecords callback) ;
	
	public interface ReadDataStoreTableRecords {	
		public void onReadDataStoreTableRecords(String dataRecordsXML, String dataRecordContinue);
	}
	
	
/********************************************************************************************************************
*	DataTableProperties: CreateDataStoreTableKeyValue(), GetDataStoreTableKeyValue(), RemoveDataStoreTableKeyValue()
*********************************************************************************************************************/		
	
	/**
	 * Updates a DataTable Dictionary key for the indicated DataTable. 
	 * If the Dictionary key does not exist it is created, otherwise the existing key  value is updated
	 */
	public void setDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, String dataTableKeyValue, SetDataStoreTableKeyValue callback);

	public interface SetDataStoreTableKeyValue {	
		public void onSetDataStoreTableKeyValue();
	}
	
	/**
	 *  returns the requested DataTable Dictionary key value for the indicated DataTable
	 */
	public void getDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, GetDataStoreTableKeyValue callback);

	public interface GetDataStoreTableKeyValue {	
		public void onGetDataStoreTableKeyValue(String DataTableKeyValue);
	}
	
	/**
	 * removes the indicated DataTable Dictionary Key
	 */
	public void removeDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, RemoveDataStoreTableKeyValue callback);
	
	public interface RemoveDataStoreTableKeyValue {	
		public void onRemoveDataStoreTableKeyValue();
	}
	
/********************************************************************************************************************
*	DataTable Transport Connection: GetDataStoreTransportURL()
*********************************************************************************************************************/		

	/**
	 * returns a URL which provides streaming write access to the identified DataTable
	 */
	public void getDataStoreTransportURL(String dataTableID, GetDataStoreTransportURL callback);
	
	public interface GetDataStoreTransportURL {	
		public void onGetDataStoreTransportURL(String dataTransportURL);
	}
	
}