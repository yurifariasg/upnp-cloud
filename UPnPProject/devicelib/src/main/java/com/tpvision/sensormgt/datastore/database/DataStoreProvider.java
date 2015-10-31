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

package com.tpvision.sensormgt.datastore.database;

/* 
 * Implements a ContentProvider for accessing DataStore data tables
 * 
 * The content provider supports the following URIs
 * 
 * content://gem.datastore/datatableinfo which returns information on all datatables in the datastore (query, insert, delete)  
 * content://gem.datastore/datatableinfo/#/ which returns information on one specific datatable (query, delete)
 * content://gem.datastore/datatableinfo/#/dictionary allows access to a dictionary corresponding to a specific datatable (query, insert, delete)
 * content://gem.datastore/datatableinfo/#/dataitemdescr  (query, insert, delete)
 * content://gem.datastore/datatableinfo/#/dataitem (query, insert, delete)
 * content://gem.datastore/datatableinfo/#/dataitem/#/ //TODO:
 * 
 */

//TODO: what to do with _COUNT
//TODO: cleanup


import java.util.ArrayList;
import java.util.List;

import com.tpvision.sensormgt.datastore.DebugDataStore;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DataStoreProvider extends ContentProvider
{
	private static final String TAG = "DataStoreProvider";
	private static final boolean DEBUG=DebugDataStore.DEBUG_DATABASE;
	
	private DataStoreDatabase mDbHelper;

	//Defines the database file name
	private static final String DBNAME = "DataStore.db";
	
	private static final int MAXCOLUMNS = 64; //maximum number of columns for generated record Tables 
	private static final int MAXLIMIT = 500;
	
	//mime type definitions
    private static final String DATATABLEINFO_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE 
    		+ "/vnd." + DataStoreContract.AUTHORITY + "." + DataStoreContract.DATATABLEINFO;
    private static final String DATATABLEINFO_ID_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
    		+ "/vnd." + DataStoreContract.AUTHORITY + "." + DataStoreContract.DATATABLEINFO;
    //TODO: complete
	
    
	//all possible Query URIs are listed here
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int DATATABLEINFO = 1;
    private static final int DATATABLEINFO_ID = 2;
    private static final int DICTIONARY = 3;
    private static final int DICTIONARY_KEY = 4;
    private static final int DATAITEMDESCR = 5;
    private static final int DATAITEM = 6;
    private static final int DATAITEM_ID = 7;
    private static final int GROUPS = 8;
    private static final int RECORD = 9;
    private static final int RECORD_ID = 10;
	
	
    

	static
    {
		URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo", DATATABLEINFO);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#", DATATABLEINFO_ID);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/dictionary", DICTIONARY);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/dictionary/*", DICTIONARY_KEY);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/dataitemdescr", DATAITEMDESCR);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/dataitem", DATAITEM);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/dataitem/#", DATAITEM_ID);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/group/#", GROUPS);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/record", RECORD);
        URI_MATCHER.addURI(DataStoreContract.AUTHORITY, "datatableinfo/#/record/#", RECORD_ID);
        
    }
	
	// Holds the database object
	private SQLiteDatabase db;

	public boolean onCreate() {
		/*
		 * Creates a new helper object. This method always returns quickly.
		 * Notice that the database itself isn't created or opened
		 * until SQLiteOpenHelper.getWritableDatabase is called
		 */
		
		if (DEBUG) Log.v(TAG, "DataStore provider started");
		mDbHelper = new DataStoreDatabase(getContext(), DBNAME);
				
		return true;
	}	
	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
		String[] selectionArgs, String sortOrder) 
	{
		// This will trigger its creation if it doesn't already exist.
		db = mDbHelper.getWritableDatabase();
		if (db == null) return null;
		
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String limit = uri.getQueryParameter("limit");
        String start = uri.getQueryParameter("start");
        String limitClause = "";
        if ((start==null) && (limit==null)) limitClause = ""; 
        else {
        	
        	if (start==null) limitClause = "0";
        	else limitClause = start;
        	if ((limit!=null) && (!limit.contentEquals("0"))) limitClause += ", " +limit;
        	else limitClause += ", "+MAXLIMIT; //unlimited
        	
        	
        }      
        
        List<String> prependArgs = new ArrayList<String>();
        
		int match = URI_MATCHER.match(uri);
		if (DEBUG) Log.v(TAG,"query: uri= "+uri );
		if (DEBUG) Log.v(TAG,"start, limit: "+start + "," + limit + "= " + limitClause);
		switch (match) {
        case DATATABLEINFO: {
        	qb.setTables(DataStoreDatabase.DATATABLEINFO_TABLE);
            break;
        }
        case DATATABLEINFO_ID: {
        	if (DEBUG) Log.v(TAG,"query Datatable_ID : " + DataStoreContract.DataTableInfoColumns._ID + " = '" + uri.getPathSegments().get(1)+"'");
        	qb.setTables(DataStoreDatabase.DATATABLEINFO_TABLE);
        	qb.appendWhere(DataStoreContract.DataTableInfoColumns._ID + " = '" + uri.getPathSegments().get(1)+"'");
        	//prependArgs.add(uri.getPathSegments().get(2));
            break;
        }
        case GROUPS: {
        	if (DEBUG) Log.v(TAG,"query Groups : " + uri.getPathSegments().get(1));
        	qb.setTables(DataStoreDatabase.GROUPS_TABLE);
        	qb.appendWhere(DataStoreContract.Groups.GRP_DATATABLE_ID + " = '" + uri.getPathSegments().get(1)+"'");
        	break;
        }
        case DICTIONARY_KEY: {
			if (DEBUG) Log.v(TAG,"query Dictionary_key : " + uri.getPathSegments().get(1) + ", " + uri.getPathSegments().get(3));
			qb.setTables(DataStoreDatabase.DICTIONARY_TABLE);
			String where = "("+DataStoreContract.Dictionary.DICT_DATATABLE_ID + " = '" + uri.getPathSegments().get(1)+"'" 
					+ ") AND (" +
					DataStoreContract.Dictionary.DICT_KEY + " = '" + uri.getPathSegments().get(3)+"')";
			qb.appendWhere(where);
			break;
		}
        case DATAITEMDESCR: {
			if (DEBUG) Log.v(TAG,"query DataItemDescr : " + uri.getPathSegments().get(1));
			qb.setTables(DataStoreDatabase.DATAITEMDESCR_TABLE);
			qb.appendWhere(DataStoreContract.DataItemDescr.DII_DATATABLE_ID + " = '" + uri.getPathSegments().get(1)+"'");
			break;
		}
        case DATAITEM: {
			if (DEBUG) Log.v(TAG,"query DataItem : " + uri.getPathSegments().get(1));
			qb.setTables(DataStoreDatabase.DATAITEM_TABLE);
			qb.appendWhere(DataStoreContract.DataItem.DI_DATATABLE_ID + " = '" + uri.getPathSegments().get(1)+"'");
			break;
		}
        case RECORD: {
        	String dataTableId = uri.getPathSegments().get(1);
			if (DEBUG) Log.v(TAG,"query Record : " + dataTableId);
			qb.setTables("RecordTable_"+dataTableId);
			//qb.appendWhere(DataStoreContract.DataItem.DI_DATATABLE_ID + " = '" + uri.getPathSegments().get(1)+"'");
			break;
		}
        default:
        	throw new IllegalStateException("Unknown URL: " + uri.toString());
		}
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limitClause);
		if (DEBUG) Log.d(TAG,"query found num results: "+c.getCount());
//TODO add try and close
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        //db.close();
        
        return c;
	}
	
	
	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{

		// Gets a writeable database. This will trigger its creation if it doesn't already exist.
		db = mDbHelper.getWritableDatabase();
		//TODO: what to do if getWritebleDatabase fails

		Uri newUri = null;
		long rowId=0;

		if (DEBUG) Log.v(TAG,"insert: uri= "+uri );
		int match = URI_MATCHER.match(uri);
		
		if (DEBUG) Log.v(TAG, "matching=" + match);
		switch (match) {
		case DATATABLEINFO: {
			if (DEBUG) Log.v(TAG,"insert DatatableInfo ");
			rowId = db.insert(DataStoreDatabase.DATATABLEINFO_TABLE, null, values);
			newUri = ContentUris.withAppendedId(DataStoreContract.DataTableInfo.CONTENT_URI, rowId);
			
			int numColumns = values.getAsInteger(DataStoreContract.DataTableInfoColumns.DATATABLE_NUMCOLUMNS);
							
			//Create table dynamically
			String createRecTable = "CREATE TABLE RecordTable_"+rowId+" ("+ DataStoreDatabase.SQL_RECORD_TABLE_FIXEDCOLUMNS +");";
			if (DEBUG) Log.v(TAG,"insert DatatableInfo: CREATE TABLE RecordTable_"+rowId);
			db.execSQL(createRecTable);
			break;
		}
		case GROUPS: {
			if (DEBUG) Log.v(TAG,"insert Groups : " + uri.getPathSegments().get(1));
			values.put(DataStoreContract.Groups.GRP_NAME, uri.getPathSegments().get(1));
			rowId = db.insertWithOnConflict(DataStoreDatabase.GROUPS_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			newUri = ContentUris.withAppendedId(uri, rowId);
			break;
		}
		case DICTIONARY: {
			if (DEBUG) Log.v(TAG,"insert Dictionary : " + uri.getPathSegments().get(1));
			values.put(DataStoreContract.Dictionary.DICT_DATATABLE_ID, uri.getPathSegments().get(1));
			rowId = db.insertWithOnConflict(DataStoreDatabase.DICTIONARY_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			newUri = ContentUris.withAppendedId(uri, rowId);
			break;
		}
		case DATAITEMDESCR: {
			String dataTableId = uri.getPathSegments().get(1);
			if (DEBUG) Log.v(TAG,"insert DataItemDescr : " + dataTableId);
			values.put(DataStoreContract.DataItemDescr.DII_DATATABLE_ID, dataTableId);
			rowId = db.insertWithOnConflict(DataStoreDatabase.DATAITEMDESCR_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			newUri = ContentUris.withAppendedId(uri, rowId);
			
			//Dynamic addition of columns to table
			//String column_name = values.getAsString(DataStoreContract.DataItemDescr.DII_FIELDNAME);
			String addRecColumn = "ALTER TABLE RecordTable_"+dataTableId+" ADD COLUMN di_"+ rowId + " TEXT;";
			db.execSQL(addRecColumn);
			db.close();
			break;
		}
		case RECORD: {
			String dataTableId = uri.getPathSegments().get(1);
			if (DEBUG) Log.v(TAG,"insert Record : " + dataTableId);
			
			//insert it in the correct record table
			rowId = db.insert("RecordTable_"+dataTableId, null, values);
			newUri = ContentUris.withAppendedId(uri, rowId);
			break;
		}
		case DATAITEM: {
			if (DEBUG) Log.v(TAG,"insert DataItem : " + uri.getPathSegments().get(1));
			values.put(DataStoreContract.DataItem.DI_DATATABLE_ID, uri.getPathSegments().get(1));
			rowId = db.insert(DataStoreDatabase.DATAITEM_TABLE, null, values);
			newUri = ContentUris.withAppendedId(uri, rowId);
			break;
		}
		default:
			throw new UnsupportedOperationException("Invalid URI " + uri);
		}

//		if (newUri != null) {
//			getContext().getContentResolver().notifyChange(uri, null); //TODO check context for leaks
//		}

		if (DEBUG) Log.v(TAG, "insertFile: values= " + values + " returned: " + rowId);
		if (DEBUG) Log.v(TAG, "new URI " +newUri);
		db.close();
//TODO: close?
		return newUri;
	}
	
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		// Gets a writeable database. This will trigger its creation if it doesn't already exist.
		db = mDbHelper.getWritableDatabase();

		int numUpdated = 0;

		if (DEBUG) Log.v(TAG,"update: uri= "+uri );
		int match = URI_MATCHER.match(uri);

		if (DEBUG) Log.v(TAG, "matching=" + match);
		switch (match) {
		case DATATABLEINFO_ID: {
			String where = "( "+DataStoreContract.DataTableInfoColumns._ID + "= '"+ uri.getPathSegments().get(1)+"') ";
			if (DEBUG) Log.v(TAG,"update Datatable_ID : " + where);
			
			numUpdated = db.update(DataStoreDatabase.DATATABLEINFO_TABLE, values, where, null);
			break;
		}
		default:
			throw new UnsupportedOperationException("Invalid URI " + uri);

		}
		
		return numUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	
		// Gets a writeable database. This will trigger its creation if it doesn't already exist.
		db = mDbHelper.getWritableDatabase();
		//TODO: what to do if getWritebleDatabase fails

		int numDeleted=0;

		int match = URI_MATCHER.match(uri);
		if (DEBUG) Log.v(TAG, "delete: uri " + uri);
		if (DEBUG) Log.v(TAG, "matching=" + match);
		switch (match) {
//		case DATATABLEINFO: {
//			numDeleted = db.delete(DataStoreDatabase.DATATABLEINFO_TABLE, selection, selectionArgs);
//
//			
//			break;
//		}
		case DATATABLEINFO_ID: {
			String rowId = uri.getLastPathSegment();
			String where = "( "+DataStoreContract.DataTableInfo.DATATABLE_GUID + "= '"+rowId+"') ";
			if (DEBUG) Log.v(TAG,"delete DataTableInfo : " + where);
			if ((selection!=null) && !selection.isEmpty()) { 
				where = where + "AND (" + selection +" )";
			}
			if (DEBUG) Log.v(TAG,"Where clause: "+where);
			
			numDeleted = db.delete(DataStoreDatabase.DATATABLEINFO_TABLE, where, selectionArgs);
			
			//remove dynamically created table
			String createRecTable = "DROP TABLE RecordTable_"+rowId+";";
			if (DEBUG) Log.v(TAG,"delete DatatableInfo: DELETETABLE RecordTable_"+rowId);
			db.execSQL(createRecTable);
			
			break;
		}
		case DICTIONARY: {
			String where = "( "+DataStoreContract.Dictionary.DICT_DATATABLE_ID + "= '"+ uri.getPathSegments().get(1)+"') ";
			if ((selection!=null) && !selection.isEmpty()) { 
				where = where + "AND (" + selection +" )"; 
			}
			if (DEBUG) Log.v(TAG,"delete Dictionary: " + where);
			
			numDeleted = db.delete(DataStoreDatabase.DICTIONARY_TABLE, where , selectionArgs);
			break;
		}
		case DICTIONARY_KEY: {
			String where = "( "+DataStoreContract.Dictionary.DICT_DATATABLE_ID + "= '"+ uri.getPathSegments().get(1)+ "') AND (" +
					DataStoreContract.Dictionary.DICT_KEY + " = '" + uri.getPathSegments().get(3)+"')";
			if ((selection!=null) && !selection.isEmpty()) { 
				where = where + "AND (" + selection +" )"; 
			}
			if (DEBUG) Log.v(TAG,"delete Dictionary key : " + where);
			
			numDeleted = db.delete(DataStoreDatabase.DICTIONARY_TABLE, where , selectionArgs);
			break;
		}
		case DATAITEMDESCR: {
			String where = "( "+DataStoreContract.DataItemDescr.DII_DATATABLE_ID + "= '"+ uri.getPathSegments().get(1)+"') ";
			if ((selection!=null) && !selection.isEmpty()) { 
				where = where + "AND (" + selection +" )";
			}
			if (DEBUG) Log.v(TAG,"delete DataItemDescr clause: "+where);
			
			numDeleted = db.delete(DataStoreDatabase.DATAITEMDESCR_TABLE, where , selectionArgs);
			break;
		}
		case RECORD: {
			String dataTableId = uri.getPathSegments().get(1);
			String where = "( "+DataStoreContract.DataItemDescr.DII_DATATABLE_ID + "= '"+ dataTableId+"') ";
			if ((selection!=null) && !selection.isEmpty()) { 
				where = where + "AND (" + selection +" )";
			}
			if (DEBUG) Log.v(TAG,"delete Records clause: "+where);
			
			numDeleted =  db.delete("RecordTable_"+dataTableId, where , selectionArgs);
			break;
		}
		case DATAITEM: {
			String where = "( "+DataStoreContract.DataItemDescr.DII_DATATABLE_ID + "= '"+ uri.getPathSegments().get(1)+"') ";
			if ((selection!=null) && !selection.isEmpty()) { 
				where = where + "AND (" + selection +" )";
			}
			if (DEBUG) Log.v(TAG,"Delete DataItem: "+where);
			
			numDeleted = db.delete(DataStoreDatabase.DATAITEM_TABLE, where , selectionArgs);
			break;
		}
		default:
			throw new UnsupportedOperationException("Invalid URI " + uri);
		}

//TODO:
		//				getContext().getContentResolver().notifyChange(uri, null); //TODO check context for leaks

		if (DEBUG) Log.v(TAG, "numDeleted " +numDeleted);
		db.close();

		return numDeleted;
	}		
		
//TODO: complete
	/**
     * This method is required in order to query the supported types.
     * It's also useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case DATATABLEINFO:
                return DATATABLEINFO_MIME_TYPE;
            case DATATABLEINFO_ID:
                return DATATABLEINFO_ID_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }
    
}
