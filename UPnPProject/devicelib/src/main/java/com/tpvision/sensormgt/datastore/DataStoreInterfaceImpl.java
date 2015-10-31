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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import com.tpvision.sensormgt.datastore.DataTableInfo.ModificationType;
import com.tpvision.sensormgt.datastore.database.DataStoreContract;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Implements all functions to implement the UPnP DataStore
 * 
 * Tables, TableInfo, Dictionary, DataItem Descriptions, and DataItems are stored and access using a content provider
 * The content provider is implemented in the com.tpvision.sensormgt.datastore.database package
 *
 */

public class DataStoreInterfaceImpl implements DataStoreInterface {

	private static final String TAG = "DataStoreInterfaceImpl";
	private static final boolean DEBUG=DebugDataStore.DEBUG_INTERFACE;
	
	private ContentResolver mContentResolver;
	private DataStoreEventControl mDataStoreEventControl;

	public DataStoreInterfaceImpl(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
		mDataStoreEventControl = new DataStoreEventControl();
	}
	
	public void setLastChangeEventListener(LastChangeEventListener lastChangeListener) {
		mDataStoreEventControl.setLastChangeEventListener(lastChangeListener);
	}
	
	
	/*
	*********************************************************************************************************************
	* 
	* UPnP Interface functions, called from device implementation
	* 
	**********************************************************************************************************************/
	
	
	/*
	*****************************************************************************************************
	* 	DataTableGroup Management: CreateDataStoreGroups(), GetDataStoreGroups(), DeleteDataStoreGroups()
	*****************************************************************************************************/	
	@Override
	public String getDataStoreGroups() {
		
		String groups[] = {"Unimatrix01"};
		return XMLUPnPDataStoreUtil.createXMLDataStoreGroups(groups);
	}
	
	@Override
	public void createDataStoreGroups(String dataStoreGroupsXML) throws UPnPException {
		
		if (DEBUG) Log.d(TAG, "createDataStoreGroups");
		ArrayList<String> dataStoreGroups = XMLUPnPDataStoreUtil.parseDataStoreGroups(dataStoreGroupsXML);
//TODO: to be completed		
		
		//di.registerModification(ModificationType.groups);
		//mDataStoreEventControl.dataTableModified(di);
	}
	
	
	@Override
	public void deleteDataStoreGroups(String dataStoreGroupsXML) {
		
		//TODO: implement deleteDataStoreGroups
	}
	
	/* 
	*****************************************************************************************************
	*	DataTable Management: CreateDataStoreTable(), DeleteDataStoreTable(), ResetDataStoreTable()	
	*****************************************************************************************************/	
	
	/* Creates a new data table according to dataTableInfo Xml description. Inserts into DataTableInfo table, and DataItemDescription table
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#createDataStoreTable(java.lang.String)
	 */
	@Override
	public int createDataStoreTable(String dataTableInfoXml) throws UPnPException {
		if (DEBUG) Log.d(TAG, "createDataStoreTable");
		DataTableInfo dataTableInfo = XMLUPnPDataStoreUtil.parseDataTableInfo(dataTableInfoXml);

		int dataTableID = createDataStoreTable(dataTableInfo.getURN(), 0, dataTableInfo.getDataItemInfoList().length); //urn, initial update ID, numColumns
		createDataTableRecord(dataTableID, dataTableInfo.getDataItemInfoList());
		
		//signal event
		dataTableInfo.setTableID(dataTableID);
		mDataStoreEventControl.dataTableCreated(dataTableInfo);
		
		return dataTableID;
	}
	
	/* Completely deletes data table, deletes DataTableInfo table, Dictionary, DataItemDescriptions, and DataItems entries
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#deleteDataStoreTable(java.lang.String)
	 */
	@Override
	public void deleteDataStoreTable(String dataTableID) throws UPnPException {
		if (DEBUG) Log.d(TAG, "deleteDataStoreTable: "+dataTableID);
		
		DataTableInfo di = getTableInfo(dataTableID);
		if (di==null) {
			throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND);
		}
			
		int tableID = di.getTableID();
		
		//delete the entry from the dataTable
		if (deleteDataStoreTable(tableID) < 1) new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND);

		//delete Dictionary items related to the table
		deleteDictionaryEntries(tableID);
		
		//delete dataItem descriptions related to table 
		deleteDataItemDescriptions(tableID);
		
		//delete dataItems related to table
		deleteDataItems(tableID);
		//deleteRecords(tableID);
		
		//signal event
		mDataStoreEventControl.dataTableDeleted(di);
	}
	
	/* deletes all entries in the data table, the DataTable Dictionary entries and Transport URLs for the DataTable as indicated by each boolean
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#resetDataStoreTable(java.lang.String)
	 */
	@Override
	public void resetDataStoreTable(String dataTableID, 
			boolean resetDataTableRecords, boolean resetDataTableDictionary, boolean resetDataTableTransport) throws UPnPException {
		
		if (DEBUG) Log.d(TAG, "resetDataStoreTable: "+dataTableID);
		
		DataTableInfo di = getTableInfo(dataTableID);
		if (di==null) {
			throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND);
		}

		int tableID = di.getTableID();
		
		//delete Dictionary items related to the table
		if (resetDataTableDictionary) deleteDictionaryEntries(tableID);
		
		//delete dataItems related to table
		if (resetDataTableRecords) { 
			deleteDataItems(tableID);
			deleteRecords(tableID);
		}
		
		di.incUpdateID();
		updateTableUpdateId(di);
		
		di.registerModification(ModificationType.reset);
		mDataStoreEventControl.dataTableModified(di);
		
		//TODO: transport
		
	}
	
	/*
	*****************************************************************************************************
	*	DataTable Information: GetDataStoreInfo(), GetDataStoreTableInfo(), ModifyDataStoreTable()	 
	*****************************************************************************************************/	

	/* Gets DataStoreInfo from DataTableInfo table, returns available DataTables 
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#getDataStoreInfo()
	 */
	@Override
	public String getDataStoreInfo() throws UPnPException {
		if (DEBUG) Log.d(TAG, "getDataStoreInfo");
		
		DataTableInfo[] datatable = getDataStoreInfoArray();
	
		return XMLUPnPDataStoreUtil.createXMLDataStoreInfo(datatable);
	}
	
	/* Gets DataStoreInfo from DataItemDescriptions table
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#GetDataStoreTableInfo(java.lang.String)
	 */
	@Override
	public String getDataStoreTableInfo(String dataTableID) throws UPnPException {
		if (DEBUG) Log.d(TAG, "GetDataStoreTableInfo: "+dataTableID);
		
		DataTableInfo dataTableInfo = getTableInfo(dataTableID);
		
		if (dataTableInfo == null) {
			throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND);
		}
	
		int tableID = dataTableInfo.getTableID();
		DataItemInfo[] dataItemInfo = getDataStoreTableInfoArray(tableID);
		dataTableInfo.setDataItemInfoList(dataItemInfo);
		
		return XMLUPnPDataStoreUtil.createXMLDataStoreTableInfo(dataTableInfo);
	}
	
	public void modifyDataStoreTable(String dataTableID, String dataTableInfoElementOrigXML, String dataTableInfoElementNewXML) throws UPnPException {
		if (DEBUG) Log.d(TAG, "modifyDataStoreTable: "+dataTableID);
		
		//TODO: modify modifyDataStoreTable
	}
	
	
	
	/*
	*****************************************************************************************************
	*	DataTable Read/Write: ReadDataStoreTableRecords(), WriteDataStoreTableRecords()
	*****************************************************************************************************/		

	/* Reads dataItems and dataItemDescriptions to create dataItem Records 
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#ReadDataStoreRecords(java.lang.String, java.lang.String, java.lang.int, java.lang.int, java.lang.boolean)
	 */
	@Override
	public String readDataStoreTableRecords(String dataTableID, String dataRecordFilter, 
			String dataRecordStart, int dataRecordCount, boolean dataRecordPropResolve) throws UPnPException {
		if (DEBUG) Log.d(TAG, "ReadDataStoreTableRecords: "+dataTableID);

		if (dataRecordFilter!=null) {
			if (DEBUG) Log.d(TAG, "Filter: "+dataRecordFilter);
			String filter = XMLUPnPDataStoreUtil.parseDataRecordFilter(dataRecordFilter);
			if (DEBUG) Log.d(TAG, "Parsed Filter: "+filter);
		}
		
		ArrayList<DataRecordInfo> dataRecords = readDataStoreRecordsArray(dataTableID, dataRecordFilter, dataRecordStart, dataRecordCount, dataRecordPropResolve);
		
		int dataRecordStartInt = 0;
		try {
			dataRecordStartInt = Integer.parseInt(dataRecordStart);
		} catch (NumberFormatException e) {
			//if we can't parse it, we start from 0
		}
		
		int cont = dataRecordStartInt + dataRecords.size();
		
		if (DEBUG) Log.d(TAG, "Continue: "+cont);
		//return a CSV
		return XMLUPnPDataStoreUtil.createXMLDataRecords(dataRecords)+","+cont;
		
	}
	
	/* Writes datarecords into dataItems table
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#WriteDataStoreRecords(java.lang.String, java.lang.String)
	 */
	@Override
	public String writeDataStoreTableRecords(String dataTableID, String dataRecordsXML) throws UPnPException {
		if (DEBUG) Log.d(TAG, "WriteDataStoreTableRecords: "+dataTableID);
		
		DataTableInfo di = getTableInfo(dataTableID);
		if (di==null) throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND);
		
		ArrayList<DataRecordInfo> dataRecords = XMLUPnPDataStoreUtil.parseDataRecords(dataRecordsXML);
		
		boolean[] recordsAccepted = new boolean[dataRecords.size()];
		
		int tableId = di.getTableID();
		
		//multiple records
		int i=0;
		boolean allAccepted = true;
		for (DataRecordInfo dii: dataRecords) {
			recordsAccepted[i] = writeDataStoreRecordsArray(tableId, dii.dataRecord);
			allAccepted = allAccepted && recordsAccepted[i];
			i++;
		}
		
		di.registerModification(ModificationType.records);
		mDataStoreEventControl.dataTableModified(di);
		
		di.incUpdateID();
		updateTableUpdateId(di);
		
		if (allAccepted) 
			return "";
		else 
			return XMLUPnPDataStoreUtil.createXMLDataRecordsStatus(recordsAccepted);
	}
	
	
	/*
	*********************************************************************************************************************
	*	DataTableProperties: CreateDataStoreTableKeyValue(), GetDataStoreTableKeyValue(), RemoveDataStoreTableKeyValue() 
	*********************************************************************************************************************/	
	
	/* Writes key-value into Dictionary table
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#setDataStoreTableKeyValue(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, String dataTableKeyValue ) throws UPnPException {
		if (DEBUG) Log.d(TAG, "setDataStoreTableKeyValue: "+dataTableID+", "+dataTableKeyName+"-"+dataTableKeyValue);
		
		DataTableInfo di = getTableInfo(dataTableID);
		if (di==null) throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND); 
		
		int tableId = di.getTableID();
		
	//	Only allow storing key values for which a dataItemDescription is found	
	//	DataItemInfo dataItemInfo = getDataItemInfo(tableID, dataTableKeyName);
	//	if (dataItemInfo==null) throw new UPnPException(UPnPDataStoreErrorCode.KEYNAME_INVALID);
		
		if ((dataTableKeyName==null) || dataTableKeyName.isEmpty()) throw new UPnPException(UPnPDataStoreErrorCode.KEYNAME_INVALID);
		
		if ((dataTableKeyValue==null)) setDataTableKeyValue(tableId, dataTableKeyName, "");
		else
			setDataTableKeyValue(tableId, dataTableKeyName, dataTableKeyValue);
		
		di.incUpdateID();
		updateTableUpdateId(di);
		
		di.registerModification(ModificationType.properties);
		mDataStoreEventControl.dataTableModified(di);
		
	}
	
	/* reads value associated to key from Dictionary table
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#getDataStoreTableKeyValue(java.lang.String, java.lang.String)
	 */
	@Override
	public String getDataStoreTableKeyValue(String dataTableID, String dataTableKeyName) throws UPnPException {
		if (DEBUG) Log.d(TAG, "getDataStoreTableKeyValue: "+dataTableID+", "+dataTableKeyName);
		
		int tableID = findTable(dataTableID);
		if (tableID == -1) throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND); 
		
		if ((dataTableKeyName==null) || dataTableKeyName.isEmpty()) throw new UPnPException(UPnPDataStoreErrorCode.KEYNAME_INVALID);
		
		//		Only allow storing key values for which a dataItemDescription is found	
		//DataItemInfo dataItemInfo = getDataItemInfo(tableID, dataTableKeyName);
		//if (dataItemInfo==null) throw new UPnPException(UPnPDataStoreErrorCode.KEYNAME_NOT_FOUND);
		
		String value = getDataStoreTableKeyValue(tableID, dataTableKeyName);
		if (value==null) throw new UPnPException(UPnPDataStoreErrorCode.KEYNAME_NOT_FOUND);
		
		return value;
	}
	
	/* Deletes value associated to key from Dictionary table
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#removeDataStoreTableKeyValue(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeDataStoreTableKeyValue(String dataTableID, String dataTableKeyName) throws UPnPException {
		if (DEBUG) Log.d(TAG, "removeDataStoreTableKeyValue"+dataTableID+", "+dataTableKeyName);

		DataTableInfo di = getTableInfo(dataTableID);
		if (di==null)  throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND); 
		int tableId = di.getTableID();
		
		if ((dataTableKeyName==null) || dataTableKeyName.isEmpty()) throw new UPnPException(UPnPDataStoreErrorCode.KEYNAME_INVALID);
		
		Uri uri = DataStoreContract.Dictionary.getContentUri(tableId, dataTableKeyName);
			
		int numDeleted = mContentResolver.delete(uri, null, null);
		if (numDeleted==0) throw new UPnPException(UPnPDataStoreErrorCode.KEYNAME_NOT_FOUND);
		
		di.incUpdateID();
		updateTableUpdateId(di);
		
		di.registerModification(ModificationType.properties);
		mDataStoreEventControl.dataTableModified(di);
	}

	
	/*
	*********************************************************************************************************************
	*	DataTable Transport Connection: GetDataStoreTransportURL() 
	*********************************************************************************************************************/	
	
	/* returns a URL which supports HTTP-POST requests to the DataTable identified by the DataTableID argument
	 * (non-Javadoc)
	 * @see gem.datastore.DataStoreInterface#getDataStoreTransportURL(java.lang.String)
	 */
	@Override
	public String getDataStoreTransportURL(String dataTableID) throws UPnPException {
		if (DEBUG) Log.d(TAG, "getDataStoreTransportURL"+dataTableID);
		
		DataTableInfo di = getTableInfo(dataTableID);
		if (di==null) throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND); 
		int tableID = di.getTableID();
		
		try {
			URL url = new URL("http", NetworkInfo.getLocalIpAddress(), NetworkInfo.getPort(), NetworkInfo.getDatatablePath()+tableID);
			if (DEBUG) Log.d(TAG, "getDataStoreTransportURL"+url.toString());
			return url.toString();
			
		} catch (MalformedURLException e) {
			//wrong dataTableID
		}
		
//		di.registerModification(ModificationType.other);
//		mDataStoreEventControl.dataTableModified(di);
		
//		di.incUpdateID();
//		updateTableUpdateId(di);
		
		return "";
	}
	

	/* *********************************************************************************************************************
	 * 
	 * UPnP functions, internal definitions
	 * 
	 **********************************************************************************************************************/
	
	/**
	 * Internal function to create DataTable using content provider
	 * No maximum on the amount of tables that can be created (for now)
	 * @param tableURN the URN for this table
	 * @param updateID the updateID, 1 for new table
	 * @return id of the newly created table
	 */
	public int createDataStoreTable(String tableURN, int updateID, int numColumns) {
		if (DEBUG) Log.d(TAG, "createDataStoreTable: " + ", " + tableURN+", "+updateID);
		
		Uri uri = DataStoreContract.DataTableInfo.CONTENT_URI;
        ContentValues values = new ContentValues();
       
        values.put(DataStoreContract.DataTableInfoColumns.DATATABLE_URN, tableURN);
        values.put(DataStoreContract.DataTableInfoColumns.DATATABLE_UPDATEID, updateID);
        values.put(DataStoreContract.DataTableInfoColumns.DATATABLE_NUMCOLUMNS, numColumns);
        
		Uri newUri = mContentResolver.insert(uri, values);
		
		//now get the id for the new dataTable entry from the Uri
		String idString = newUri.getLastPathSegment();
		
		return Integer.parseInt(idString);
	}

	/**
	 * Inserts datatable information into the dataItemDescription table
	 * @param dataTableID identifies the datatable
	 * @param dataItemInfoList all dataItems to insert
	 */
	public void createDataTableRecord(int dataTableID, DataItemInfo[] dataItemInfoList) {
		if (DEBUG) Log.d(TAG, "createDataTableRecord: tableId"+ dataTableID + ", items:" + dataItemInfoList.length);

		Uri uri = DataStoreContract.DataItemDescr.getContentUri(dataTableID);
		if (dataItemInfoList != null) {
			for (int i=0; i < dataItemInfoList.length; i++) {
				DataItemInfo dataItemInfo = dataItemInfoList[i];
				ContentValues values = new ContentValues();
				values.put(DataStoreContract.DataItemDescrColumns.DII_DATATABLE_ID, dataTableID);
				values.put(DataStoreContract.DataItemDescrColumns.DII_FIELDNAME, dataItemInfo.getFieldName());
				values.put(DataStoreContract.DataItemDescrColumns.DII_FIELDTYPE, dataItemInfo.getFieldType());
				values.put(DataStoreContract.DataItemDescrColumns.DII_ENCODING, dataItemInfo.getEncoding());
				values.put(DataStoreContract.DataItemDescrColumns.DII_REQUIRED, dataItemInfo.isRequired());
				values.put(DataStoreContract.DataItemDescrColumns.DII_NAMESPACE, dataItemInfo.getNamespace());
				values.put(DataStoreContract.DataItemDescrColumns.DII_TABLEPROP, dataItemInfo.isTableProp());

				mContentResolver.insert(uri, values);
			}	
		}
	}
	

	/**
	 * gets URN and UpdateID info for a Table 
	 * @param dataTableID identifies the datatable
	 * @return DataTableInfo or null if no table with the given dataTableID is found
	 * @throws UPnPException 
	 */
	public DataTableInfo getTableInfo(String dataTableID) throws UPnPException {
		if (DEBUG) Log.d(TAG, "getTableInfo: "+dataTableID);
		
		if (dataTableID==null)
			return null;
		
		DataTableInfo datatableInfo=null;
		
		Uri uri;
		try {
			int id = Integer.valueOf(dataTableID);
			uri = DataStoreContract.DataTableInfo.getContentUri(id);
		} catch (Exception e) {
			throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND); 
		}
		
		final String[] projection = new String[] {
				DataStoreContract.DataTableInfoColumns._ID,	
				DataStoreContract.DataTableInfoColumns.DATATABLE_URN, 
				DataStoreContract.DataTableInfoColumns.DATATABLE_UPDATEID
		};
		
		Cursor cursor = mContentResolver.query(uri, projection, null , null, null);
		
		if (cursor !=null && (cursor.getCount()!=0 )) {
			cursor.moveToFirst();
			
			int id = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DataTableInfoColumns._ID));
			String urn = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataTableInfoColumns.DATATABLE_URN));
			int updateid = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DataTableInfoColumns.DATATABLE_UPDATEID));

			datatableInfo = new DataTableInfo(id, urn, updateid);
			
			cursor.close();
		} else {
			throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND); 
		}
		
		return datatableInfo;
	}
 	
	
	
	/**
	 * Reads URN and updateID from the DataTableInfo table
	 * @return Array of DataTableInfo objects
	 */
	public DataTableInfo[]  getDataStoreInfoArray() {
		
		Uri uri = DataStoreContract.DataTableInfo.CONTENT_URI;

		if (DEBUG) Log.d(TAG, "getDataStoreInfoArray: "+uri.toString());
		
		final String[] projection = new String[] {
				DataStoreContract.DataTableInfoColumns._ID,	
				DataStoreContract.DataTableInfoColumns.DATATABLE_URN,
				DataStoreContract.DataTableInfoColumns.DATATABLE_UPDATEID
		};
		Cursor cursor = mContentResolver.query(uri, projection, null, null, null);
		DataTableInfo[] datatable = null;
		if (cursor!=null) {
			datatable = new DataTableInfo[cursor.getCount()];
			int i=0;
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DataTableInfoColumns._ID));
				String urn = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataTableInfoColumns.DATATABLE_URN));
				int updateid = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DataTableInfoColumns.DATATABLE_UPDATEID));

				datatable[i] = new DataTableInfo(id, urn, updateid);
				i++;
				
				if(DEBUG) {
					Log.d(TAG,"id "+id);
					if (urn!=null) Log.d(TAG,"id "+urn);
					Log.d(TAG,"id "+updateid);
				}
			}	
			cursor.close();
		}

		return datatable;
	}
	

	/**
	 * Gets the dataItem descriptions for the current dataTable from the DataItemsDescription table
	 * @param dataTableID identifies the datatable
	 * @return Array of DataItemInfo objects
	 */
	public DataItemInfo[] getDataStoreTableInfoArray(int dataTableID) {

		Uri uri = DataStoreContract.DataItemDescr.getContentUri(dataTableID);
		
		if (DEBUG) Log.d(TAG, "GetDataStoreTableInfoArray: "+ uri.toString());
		

		final String[] projection = new String[] {
				DataStoreContract.DataItemDescrColumns._ID,	
				DataStoreContract.DataItemDescrColumns.DII_FIELDNAME,
				DataStoreContract.DataItemDescrColumns.DII_FIELDTYPE,
				DataStoreContract.DataItemDescrColumns.DII_ENCODING, 
				DataStoreContract.DataItemDescrColumns.DII_REQUIRED, 
				DataStoreContract.DataItemDescrColumns.DII_NAMESPACE, 
				DataStoreContract.DataItemDescrColumns.DII_TABLEPROP, 
		};

		Cursor cursor = mContentResolver.query(uri, projection, null, null, null);

		if (cursor!=null) {
			DataItemInfo[] dataItemInfo = new DataItemInfo[cursor.getCount()];
			int i=0;
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns._ID));
				String fieldname = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_FIELDNAME));
				String fieldtype = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_FIELDTYPE));
				String encoding = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_ENCODING));
				String required = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_REQUIRED));
				String namespace = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_NAMESPACE));
				String tableprop = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_TABLEPROP));

				boolean frequiredBool = (required.contentEquals("1")) ? true : false;
				boolean ftablepropBool = (tableprop.contentEquals("1")) ? true : false;	

				dataItemInfo[i++] = new DataItemInfo(id, dataTableID, fieldname, fieldtype, encoding, frequiredBool, namespace, ftablepropBool);

			}	
			
			cursor.close();
			return dataItemInfo;
		}

		return null;
	}
	
	/**
	 * Reads dataItem values and time stamps from the DataItem and gets corresponding info from the DataItemDescription table
	 * constructs a query according to the filter, datarecordStart arguments
	 * @param dataTableID identifies the datatable
	 * 
	 * @return Array of DataItemInfo objects
	 * @throws UPnPException
	 */
	//TODO: implement filter
	public ArrayList<DataRecordInfo> readDataStoreRecordsArray(String dataTableID, String dataRecordFilter, 
			String dataRecordStart, int dataRecordCount, boolean dataRecordPropResolve) throws UPnPException {

		int tableID = findTable(dataTableID);
		if (tableID == -1) throw new UPnPException(UPnPDataStoreErrorCode.DATATABLE_NOT_FOUND);

		int dataRecordStartInt = 0;
		try {
			dataRecordStartInt = Integer.parseInt(dataRecordStart);
		} catch (NumberFormatException e) {
			//if we can't parse it, we start from 0
		}
		
		ArrayList<DataRecordInfo> dataRecords = new ArrayList<DataRecordInfo>(dataRecordCount);

		//
		Uri uri2 = DataStoreContract.Records.getContentUri(tableID);
		if (dataRecordStartInt!=0)	uri2 = uri2.buildUpon().appendQueryParameter("start", Integer.toString(dataRecordStartInt)).build();
		if (dataRecordCount!=0)	uri2 = uri2.buildUpon().appendQueryParameter("limit", Integer.toString(dataRecordCount)).build();

		DataItemInfo[] dataItemDescriptions = getDataItemInfoSelection(tableID ,"");

		//add all columns to projection
		String[] projection2 = new String[dataItemDescriptions.length+1];
		projection2[0] = DataStoreContract.DataItemColumns._ID; //change to Record
		for (int i=0; i < dataItemDescriptions.length; i++) {
			projection2[i+1] = "di_" + dataItemDescriptions[i].getIndexId();
			//Log.d(TAG,"projection "+projection2[i+1]);
		}

		Cursor cursor2 = mContentResolver.query(uri2, projection2, "" , null, null);

		if (cursor2!=null) {
			while (cursor2.moveToNext()) {
				ArrayList<DataItemInfo> dataItems = new ArrayList<DataItemInfo>();

				int id = cursor2.getInt(cursor2.getColumnIndex(DataStoreContract.DataItemColumns._ID));
				int skipFirstColumns=1;
				for (int i=skipFirstColumns; i < cursor2.getColumnCount(); i++) {
					
					DataItemInfo dii = dataItemDescriptions[i-skipFirstColumns].copy();
					
//					if (DEBUG) { Log.d(TAG,"get item "+ dii.getFieldName());
//					Log.d(TAG,"colname "+"di_"+dii.getIndexId());
//					Log.d(TAG,"val"+ cursor2.getString(cursor2.getColumnIndex("di_"+dii.getIndexId())));
//					}
					String value = cursor2.getString(cursor2.getColumnIndex("di_"+dii.getIndexId()));
					dii.setValue(value);

					//check if we need to resolve the value through the dictionary
					if (dataRecordPropResolve && (dii.isTableProp())) {
						
						if (!value.isEmpty()) {
							String propValue = getDataStoreTableKeyValue(tableID, value);
							if ((propValue!=null) && !propValue.isEmpty()) {
								dii.setValue(propValue);
							} else {	
								//if the dictionary key is missing, the dataItem shall be empty
								dii.setValue("");
							}
						} else {	
							//dictionary key is empty, set the dataItem to empty
							dii.setValue("");
						}
					}	

					dataItems.add(dii);
				}
				dataRecords.add(new DataRecordInfo(dataItems));
			}
		
			cursor2.close();
			return dataRecords;
		}

		return null;
	}
	
	/**
	 * Searches Keyname in the Dictionary table and returns associated value
	 * @param dataTableID identifies the datatable
	 * @param dataTableKeyName the key in the Dictionary table
	 * @return the value corresponding to the key
	 */
	public String getDataStoreTableKeyValue(int dataTableID, String dataTableKeyName) {
		if (DEBUG) Log.d(TAG, "getDataStoreTableKeyValue: "+ dataTableID + ", " + dataTableKeyName);
		
		Uri uri = DataStoreContract.Dictionary.getContentUri(dataTableID, dataTableKeyName);

		final String[] projection = new String[] {
				DataStoreContract.DictionaryColumns._ID,	
				DataStoreContract.DictionaryColumns.DICT_KEY,
				DataStoreContract.DictionaryColumns.DICT_VALUE
		};
		Cursor cursor = mContentResolver.query(uri, projection, null, null, null);
		String value = null;
		if ((cursor!=null) && (cursor.getCount()!=0)) {
			cursor.moveToFirst();
			int id = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DictionaryColumns._ID));
			String key = cursor.getString(cursor.getColumnIndex(DataStoreContract.DictionaryColumns.DICT_KEY));
			value = cursor.getString(cursor.getColumnIndex(DataStoreContract.DictionaryColumns.DICT_VALUE));

			if (DEBUG) {
				Log.v(TAG,"id "+id);
				if (key!=null) Log.v(TAG,"key "+key);
				if (value!=null) Log.v(TAG,"id "+value);
			}
			
			cursor.close();
		}	
		
		return value;
	}
	
	/**
	 * Writes the key value pair into the Dictionary table
	 * @param dataTableID identifies the datatable
	 * @param dataTableKeyName the key in the Dictionary table
	 * @param dataTableKeyValue the value corresponding to the key
	 */
	public void setDataTableKeyValue(int dataTableID, String dataTableKeyName, String dataTableKeyValue ) {
		if (DEBUG) Log.d(TAG, "setDataTableKeyValue: "+ dataTableID + ", " + dataTableKeyName + " ," + dataTableKeyValue);

		Uri uri = DataStoreContract.Dictionary.getContentUri(dataTableID);
		ContentValues values = new ContentValues();
        values.put(DataStoreContract.Dictionary.DICT_DATATABLE_ID, dataTableID);
        values.put(DataStoreContract.Dictionary.DICT_KEY, dataTableKeyName);
        values.put(DataStoreContract.Dictionary.DICT_VALUE, dataTableKeyValue);
		
        mContentResolver.insert(uri, values);
	}
	
	/**
	 * Writes dataItem values from the DataItemInfo array to the DataItem table 
	 * Timestamps are automatically creates
	 * @param dataTableID identifies the datatable
	 * @param dataRecord Array of records
	 * @throws UPnPException
	 */
	private boolean writeDataStoreRecordsArray(int dataTableID, ArrayList<DataItemInfo> dataRecord) throws UPnPException {
		if (DEBUG) Log.d(TAG, "WriteDataStoreRecords: "+ dataTableID);

		ContentValues values = new ContentValues();
		//mandatory columns
		values.put(DataStoreContract.DataItemColumns.DI_DATATABLE_ID, dataTableID); 

		//Prepare list for checking acceptance of datarecords
		boolean itemsAccepted = true; 

		for (DataItemInfo dataItem : dataRecord) {

			//check if the dataItem is allowed in this table
			DataItemInfo dataItemInfo = getDataItemInfo(dataTableID, dataItem.getFieldName());
			if (dataItemInfo==null) throw new UPnPException(UPnPDataStoreErrorCode.DATAITEM_NOT_FOUND);

			//check if all required items are found
			DataItemInfo[] requiredDataItems =  getRequiredDataItems(dataTableID);
			int i=0;

			while (i < requiredDataItems.length) {
				//check if this requiredItem is part of the dataRecords
				if (!containsDataItemName(requiredDataItems[i].getFieldName(), dataRecord)) {
					//throw new UPnPException(UPnPDataStoreErrorCode.DATAITEM_MISSING);
					Log.d(TAG,"Not accepted item "+ requiredDataItems[i].getFieldName());
					itemsAccepted = false;
				}
				i++;
			}

			//put the column value
			values.put("di_"+dataItemInfo.getIndexId(), dataItem.getValue());
		}

		//write the values to the record
		if (itemsAccepted) {
			Uri uri = DataStoreContract.Records.getContentUri(dataTableID);
			mContentResolver.insert(uri, values);
		} else {
			Log.d(TAG,"Record not accepted, missing required items");
		}
			

		return itemsAccepted;
	}

	/**
	 * Check if a dataItem with the required name is found in the list of dataItems
	 * @param requiredName the Name to look for
	 * @param dataItems list of dataItems
	 * @return true if data item with the required name was found
	 */
	private boolean containsDataItemName(String requiredName, ArrayList<DataItemInfo> dataItems) {
		
		boolean found = false;
		Iterator<DataItemInfo> di = dataItems.iterator();
		while(di.hasNext()&&!found) {
			DataItemInfo dataItem = di.next();
			found = dataItem.getFieldName().contentEquals(requiredName);
		}
		
		return found;
	}
	
	/* 
	 **********************************************************************************************************************
	 * 
	 * Support functions
	 * 
	 **********************************************************************************************************************/

	/**
	 * Check if the datatable exists (GUID is used equal to tableID), returns -1 if not found
	 * @param dataTableID identifies the table
	 * @return id if found
	 */
	public int findTable(String dataTableID) {
		if (DEBUG) Log.d(TAG, "findTableGUID: "+dataTableID);
		
		if (dataTableID==null||dataTableID.isEmpty())
			return -1;
		
		DataTableInfo datatableInfo=null;
		try { 
			datatableInfo = getTableInfo(dataTableID);
			
		} catch (Exception e) {
			return -1;
		}
		
		if (datatableInfo==null)
			return -1;
		
		
		return datatableInfo.getTableID();
	}

	public int updateTableUpdateId(DataTableInfo dataTableInfo) throws UPnPException {
		if (DEBUG) Log.d(TAG, "getTableInfo: "+dataTableInfo);
		
		if (dataTableInfo==null)
			return 0;
		
		Uri uri = DataStoreContract.DataTableInfo.getContentUri(dataTableInfo.getTableID());
		
		ContentValues values = new ContentValues();
		values.put(DataStoreContract.DataTableInfoColumns.DATATABLE_UPDATEID, dataTableInfo.getUpdateID()); 
		
		int numUpdated = mContentResolver.update(uri, values , null, null);
		
		return numUpdated;
	}
		
	/**
	 * Inserts one dataItemInfo record into the datItemDescription table
	 * @param dataTableID identifies the table
	 * @param di the DataItemInfo object
	 */
	public void insertDataItemInfo(int dataTableID, DataItemInfo di) {
		if (DEBUG) Log.d(TAG, "insertRecord: into "+ dataTableID);
		
		Uri uri = DataStoreContract.DataItemDescr.getContentUri(dataTableID);
		
        ContentValues values = new ContentValues();
    
        values.put(DataStoreContract.DataItemDescrColumns.DII_FIELDNAME, di.getFieldName()); 
        values.put(DataStoreContract.DataItemDescrColumns.DII_FIELDTYPE, di.getFieldType());
        values.put(DataStoreContract.DataItemDescrColumns.DII_ENCODING, di.getEncoding());
        values.put(DataStoreContract.DataItemDescrColumns.DII_REQUIRED, di.isRequired());
        values.put(DataStoreContract.DataItemDescrColumns.DII_NAMESPACE, di.getNamespace());
        values.put(DataStoreContract.DataItemDescrColumns.DII_TABLEPROP, di.isTableProp());
        
		mContentResolver.insert(uri, values);	
	}
	
	/** 
	 * Gets the dataItemInfo for a given dataTableId and dataItem name  
	 * @param dataTableID identifies the table
	 * @param fieldname fieldname for which to get dataItemInfo
	 * @return DataItemInfo record describing all properties of the data item, returns null if not found
	 */
	public DataItemInfo[] getRequiredDataItems(int dataTableID) {
		if (DEBUG) Log.d(TAG, "getDataItemInfo: "+ dataTableID);
		
		String where = " (" + DataStoreContract.DataItemDescrColumns.DII_REQUIRED + " = '1' ) ";		
		return getDataItemInfoSelection(dataTableID, where);
	}
	
	/** 
	 * Gets the dataItemInfo for a given dataTableId and dataItem name  
	 * @param dataTableID identifies the table
	 * @param fieldname fieldname for which to get dataItemInfo
	 * @return DataItemInfo record describing all properties of the data item, returns null if not found
	 */
	public DataItemInfo getDataItemInfo(int dataTableID, String fieldname) {
		if (DEBUG) Log.d(TAG, "getDataItemInfo: "+ dataTableID+", "+fieldname);
		
		String where = " (" + DataStoreContract.DataItemDescrColumns.DII_FIELDNAME + " = '" + fieldname + "') ";		
		DataItemInfo[] dataItemInfo = getDataItemInfoSelection(dataTableID, where);
		
		if ((dataItemInfo==null) || (dataItemInfo.length==0)) return null;
		if (DEBUG) if (dataItemInfo.length>1) Log.d(TAG, "getDataItemInfo returned more than one result");
		
		return dataItemInfo[0];
	}
	
	
	/** 
	 * Gets the dataItemInfo for a given dataTableId and dataItem name  
	 * @param dataTableID identifies the table
	 * @param descrID index in the description table
	 * @return DataItemInfo record describing all properties of the data item, returns null if not found
	 */
	public DataItemInfo getDataItemInfo(int dataTableID, int descrID) {
		if (DEBUG) Log.d(TAG, "getDataItemInfo: "+ dataTableID+", "+descrID);
		
		String where = " (" + DataStoreContract.DataItemDescrColumns._ID + " = '" + descrID + "') ";	
		DataItemInfo[] dataItemInfo = getDataItemInfoSelection(dataTableID, where);
		
		if (dataItemInfo.length==0) return null;
		if (DEBUG) if (dataItemInfo.length>1) Log.d(TAG, "getDataItemInfo returned more than one result");
		
		return dataItemInfo[0];
	}
	
	
	/** Gets the dataItemInfo for a given dataTableId filters based on "where"-clause and returns the first dataItemFound
	 * Internally used to retrieve dataItem based on dataItemTableId and dataItemName
	 * @param dataTableID identifies the table
	 * @param where defines the where clause
	 * @return DataItemInfo record describing all properties of the data item, returns null if not found
	 */
	private DataItemInfo[] getDataItemInfoSelection(int dataTableID, String where) {
		if (DEBUG) Log.d(TAG, "getDataTableRecord: tableId"+ dataTableID + ", where:" + where);

		Uri uri = DataStoreContract.DataItemDescr.getContentUri(dataTableID);

		final String[] projection = new String[] {
				DataStoreContract.DataItemDescrColumns._ID,
				DataStoreContract.DataItemDescrColumns.DII_DATATABLE_ID,	
				DataStoreContract.DataItemDescrColumns.DII_FIELDNAME,
				DataStoreContract.DataItemDescrColumns.DII_FIELDTYPE,
				DataStoreContract.DataItemDescrColumns.DII_ENCODING,
				DataStoreContract.DataItemDescrColumns.DII_REQUIRED,
				DataStoreContract.DataItemDescrColumns.DII_NAMESPACE,
				DataStoreContract.DataItemDescrColumns.DII_TABLEPROP
		};

		Cursor cursor = mContentResolver.query(uri, projection, where , null, null);

		DataItemInfo[] dataItemInfo=null;
		if (cursor !=null && (cursor.getCount()!=0 )) {
			dataItemInfo = new DataItemInfo[cursor.getCount()];

			int i=0;
			while (cursor.moveToNext()) {

				int id = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns._ID));
				int tableID = cursor.getInt(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_DATATABLE_ID));
				String fieldname = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_FIELDNAME));
				String fieldtype = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_FIELDTYPE));
				String encoding = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_ENCODING));
				String required = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_REQUIRED));
				String namespace = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_NAMESPACE));
				String tableprop = cursor.getString(cursor.getColumnIndex(DataStoreContract.DataItemDescrColumns.DII_TABLEPROP));

				boolean frequiredBool = false;
				boolean ftablepropBool = false;
				if ((required!=null) && (required.contentEquals("1"))) frequiredBool = true;
				if ((tableprop!=null) && (tableprop.contentEquals("1"))) ftablepropBool = true;

				dataItemInfo[i] = new DataItemInfo(id, tableID, fieldname, fieldtype, encoding, frequiredBool, namespace, ftablepropBool);
				i++;
			}
			cursor.close();
		}
		return dataItemInfo;
	}
	

	/**
	 * Inserts a single dataitem consisting of datatableId, reference to dataitem description table, and value
	 * Automatically creates timestamps
	 * @param dataTableID identifies the table
	 * @param descriptionID index into the dataItemDescription table
	 * @param value the dataItem value to write
	 */
	public void insertDataItem(int dataTableID, int descriptionID, String value) {
		
		if (DEBUG) Log.d(TAG, "insertDataItem: into "+ dataTableID);
		
		Uri uri = DataStoreContract.DataItem.getContentUri(dataTableID);
		
        ContentValues values = new ContentValues();
    
        values.put(DataStoreContract.DataItemColumns.DI_DATATABLE_ID, dataTableID); 
        values.put(DataStoreContract.DataItemColumns.DI_DESCR_ID, descriptionID);
        values.put(DataStoreContract.DataItemColumns.DI_VALUE, value);
        
		mContentResolver.insert(uri, values);
	}

	
	/**
	 * Delete all dataTableID entries from dataTable (should be just one)
	 * @param dataTableID identifies the table
	 * @return number of deleted items
	 */
	private int deleteDataStoreTable(int dataTableID) {
		Uri uri = DataStoreContract.DataTableInfo.getContentUri(dataTableID);
		int numDeleted = mContentResolver.delete(uri, null, null);
		return numDeleted;
	}
	
	
	/**
	 * Delete all dictionary entries related to the table
	 * @param dataTableID identifies the table
	 * @return number of deleted items
	 */
	private int deleteDictionaryEntries(int dataTableID) {
		Uri uri = DataStoreContract.Dictionary.getContentUri(dataTableID);
		int numDeleted = mContentResolver.delete(uri, null, null);
		return numDeleted;
	}
	
	
	/** 
	 * Delete all dataItemDiscriptions 
	 * @param dataTableID identifies the table
	 * @return number of deleted items
	 */
	private int deleteDataItemDescriptions(int dataTableID) {
		Uri uri = DataStoreContract.DataItemDescr.getContentUri(dataTableID);
		int numDeleted = mContentResolver.delete(uri, null, null);
		return numDeleted;
	}
	
	/** 
	 * Delete all Records for the given Table 
	 * @param dataTableID identifies the table
	 * @return number of deleted items
	 */
	private int deleteRecords(int dataTableID) {
		Uri uri = DataStoreContract.Records.getContentUri(dataTableID);
		int numDeleted = mContentResolver.delete(uri, null, null);
		return numDeleted;
	}
	
	
	/** 
	 * Delete all dataItemDiscriptions 
	 * @param dataTableID identifies the table
	 * @return number of deleted items
	 */
	private int deleteDataItems(int dataTableID) {
		Uri uri = DataStoreContract.DataItem.getContentUri(dataTableID);
		int numDeleted = mContentResolver.delete(uri, null, null);
		return numDeleted;
	}
	
}