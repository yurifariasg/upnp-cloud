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

package com.tpvision.sensormgt.datastore;

/**
 * Defines all methods that are accessed from UPnP to implement the DataStore Services
 */

public interface DataStoreInterface {

	
/*****************************************************************************************************
 * 	DataTableGroup Management: CreateDataStoreGroups(), GetDataStoreGroups(), DeleteDataStoreGroups()
*****************************************************************************************************/	

	/*
	 * returns an XML document listing currently defined DataStore group identifiers
	 */
	public String getDataStoreGroups();
	
	/* declares new DataStore group name(s) */
	public void createDataStoreGroups(String dataStoreGroupsXML) throws UPnPException;
	
	/* declares new DataStore group name(s) */
	public void deleteDataStoreGroups(String dataStoreGroupsXML);
	
	
/*****************************************************************************************************
*	DataTable Management: CreateDataStoreTable(), DeleteDataStoreTable(), ResetDataStoreTable()	
*****************************************************************************************************/	
	
	/**
	 * creates a new DataTable
	 * Optional
	 */
	public int createDataStoreTable(String dataTableInfo) throws UPnPException; 
	
	/**
	 * Deletes an existing DataTable. 
	 * Existing connections shall be closed after returning status for any pending operations
	 * Conditionally Optional
	 */
	public void deleteDataStoreTable(String dataTableID) throws UPnPException;
	
	/**
	 * Clear all DataRecord(s), DataTable Dictionary entries and Transport URLs for the DataTable as indicated by each boolean 
	 * Optional
	 */
	public void resetDataStoreTable(String dataTableID,
									boolean resetDataTableRecords, boolean resetDataTableDictionary, boolean resetDataTableTransport) throws UPnPException;
	
/*****************************************************************************************************
*	DataTable Information: GetDataStoreInfo(), GetDataStoreTableInfo(), ModifyDataStoreTable()	 
*****************************************************************************************************/	

	/**
	 * returns information about DataStore DataTable(s) currently supported by this DataStore service
	 */
	public String getDataStoreInfo() throws UPnPException;

	/**
	 * returns an XML document containing information about the DataTable associated  the DataTableID  argument
	 */
	public String getDataStoreTableInfo(String GUID) throws UPnPException;
	
	/**
	 * modification of selected elements of an existing DataTable indicated by the DataTableID argument
	 */
	public void modifyDataStoreTable(String dataTableID, String dataTableInfoElementOrigXML, String dataTableInfoElementNewXML) throws UPnPException;
	

/*****************************************************************************************************
*	DataTable Read/Write: ReadDataStoreTableRecords(), WriteDataStoreTableRecords()
*****************************************************************************************************/		

	/**
	 * write DataRecord(s) to the DataTable identified by the DataTableID argument
	 */
	public String writeDataStoreTableRecords(String GUID, String dataRecordsXML) throws UPnPException;
	
	/** 
	 * Returns DataRecord(s) from the DataTable indicated by the DataTableID argument. 
	 * The DataRecordFilter argument may specify a string to indicate which DataRecord(s) shall be returned.
	 */
	public String readDataStoreTableRecords(String dataTableId, String dataRecordFilter,
											String dataRecordStart, int dataRecordCount, boolean dataRecordPropResolve) throws UPnPException;
	
	
/********************************************************************************************************************
*	DataTableProperties: CreateDataStoreTableKeyValue(), GetDataStoreTableKeyValue(), RemoveDataStoreTableKeyValue()
*********************************************************************************************************************/		
	
	/**
	 * Updates a DataTable Dictionary key for the indicated DataTable. 
	 * If the Dictionary key does not exist it is created, otherwise the existing key  value is updated
	 */
	public void setDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, String dataTableKeyValue) throws UPnPException;

	/**
	 *  returns the requested DataTable Dictionary key value for the indicated DataTable
	 */
	public String getDataStoreTableKeyValue(String dataTableID, String dataTableKeyName) throws UPnPException;

	/**
	 * removes the indicated DataTable Dictionary Key
	 */
	public void removeDataStoreTableKeyValue(String dataTableID, String dataTableKeyName) throws UPnPException;
	
	
/********************************************************************************************************************
*	DataTable Transport Connection: GetDataStoreTransportURL()
*********************************************************************************************************************/		

	/**
	 * returns a URL which provides streaming write access to the identified DataTable
	 */
	public String getDataStoreTransportURL(String dataTableID) throws UPnPException;
	

}