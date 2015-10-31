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

import com.tpvision.sensormgt.datastore.DebugDataStore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//TODO: description of database design

public class DataStoreDatabase extends SQLiteOpenHelper 
{
    public final String TAG="DataStoreDatabase";
    private static final boolean DEBUG=DebugDataStore.DEBUG_DATABASE;
    
    /** The version of the database that this class understands. */ 
    private static final int DATABASE_VERSION = 1; 
   
    /** Database table names */
  	public static final String DATATABLEINFO_TABLE="DataTableInfo ";
  	public static final String GROUPS_TABLE="Groups ";
  	public static final String DATA_TABLE="Data ";
  	public static final String DICTIONARY_TABLE="Dictionary ";
  	public static final String DATAITEMDESCR_TABLE="DataItemDescr ";
  	public static final String DATAITEM_TABLE="DataItem ";
  	
  	/** Strings to create tables */
  	private static final String SQL_CREATE_DATATABLEINFO_TABLE = "CREATE TABLE " +
  			DATATABLEINFO_TABLE +                       // Table's name
  	    "(" +                           // The columns in the table
  	    DataStoreContract.DataTableInfoColumns._ID + " INTEGER PRIMARY KEY, "+	
  	    DataStoreContract.DataTableInfoColumns.DATATABLE_URN + " VARCHAR(255), "+
  	    DataStoreContract.DataTableInfoColumns.DATATABLE_DICTIONARY_ID + " VARCHAR(255), " +
  	    DataStoreContract.DataTableInfoColumns.DATATABLE_UPDATEID + " SMALLINT, " +
  	  DataStoreContract.DataTableInfoColumns.DATATABLE_NUMCOLUMNS + " SMALLINT " +
  	    ")";

  	private static final String SQL_CREATE_GROUPS_TABLE = "CREATE TABLE " +
  			GROUPS_TABLE +                       // Table's name
  			"(" +                           // The columns in the table
  			DataStoreContract.Groups._ID + " INTEGER PRIMARY KEY, "+	
  			DataStoreContract.Groups.GRP_DATATABLE_ID + " SMALLINT, " +
  			DataStoreContract.Groups.GRP_NAME + " TEXT " +
  			")";
  	
  	private static final String SQL_CREATE_DICTIONARY_TABLE = "CREATE TABLE " +
  			DICTIONARY_TABLE +                       // Table's name
  			"(" +                           // The columns in the table
  			DataStoreContract.Dictionary._ID + " INTEGER PRIMARY KEY, "+	
  			DataStoreContract.Dictionary.DICT_DATATABLE_ID + " SMALLINT, " +
  			DataStoreContract.Dictionary.DICT_KEY + " TEXT, " + 
  			DataStoreContract.Dictionary.DICT_VALUE + " TEXT " + 
  			", UNIQUE(" + DataStoreContract.Dictionary.DICT_DATATABLE_ID + ", " +DataStoreContract.Dictionary.DICT_KEY+")" +
  			")";
  	
  	private static final String SQL_CREATE_DATAITEMDESCR_TABLE = "CREATE TABLE " +
  			DATAITEMDESCR_TABLE +                       // Table's name
  			"(" +                           // The columns in the table
  			DataStoreContract.DataItemDescr._ID + " INTEGER PRIMARY KEY, "+	
  			DataStoreContract.DataItemDescr.DII_DATATABLE_ID + " SMALLINT, " +
  			DataStoreContract.DataItemDescr.DII_FIELDNAME + " VARCHAR(255), " + 
  			DataStoreContract.DataItemDescr.DII_FIELDTYPE + " VARCHAR(255), " + 
  			DataStoreContract.DataItemDescr.DII_ENCODING + " VARCHAR(16), " + 
  			DataStoreContract.DataItemDescr.DII_REQUIRED + " BOOLEAN, " + 
  			DataStoreContract.DataItemDescr.DII_NAMESPACE + " VARCHAR(16), " + 
  			DataStoreContract.DataItemDescr.DII_TABLEPROP + " BOOLEAN " +
  			", UNIQUE(" + DataStoreContract.Dictionary.DICT_DATATABLE_ID + ", "  +DataStoreContract.DataItemDescr.DII_FIELDNAME+")" +
  			")";
  
 
  	private static final String SQL_CREATE_DATAITEM_TABLE = "CREATE TABLE " +
  			DATAITEM_TABLE +                       // Table's name
  			"(" +                           // The columns in the table
  			DataStoreContract.DataItem._ID + " INTEGER PRIMARY KEY, "+	
  			DataStoreContract.DataItem.DI_RECEIVE_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "+
  			DataStoreContract.DataItem.DI_DATATABLE_ID + " SMALLINT, " +
  			DataStoreContract.DataItem.DI_DESCR_ID + " SMALLINT, " + 
  			DataStoreContract.DataItem.DI_VALUE + " TEXT " + 
  			")";
  	
  	public static final String SQL_RECORD_TABLE_FIXEDCOLUMNS = 
  			DataStoreContract.DataItem._ID + " INTEGER PRIMARY KEY, "+	
  			DataStoreContract.DataItem.DI_DATATABLE_ID + " SMALLINT, " +
  			DataStoreContract.DataItem.DI_DESCR_ID + " SMALLINT, " + 
  			DataStoreContract.DataItem.DI_VALUE + " TEXT " + 
  			DataStoreContract.DataItem.DI_RECEIVE_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP ";
  	
  	
  	
    public DataStoreDatabase(Context context, String dbname) {
		 
	    	super(context, dbname, null, DATABASE_VERSION);    	
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		if (DEBUG) Log.v(TAG,"Creating DataStore database");
		try 
        {
			db.beginTransaction();
			if (DEBUG) Log.v(TAG,"Creating table: " + SQL_CREATE_DATATABLEINFO_TABLE);
			db.execSQL(SQL_CREATE_DATATABLEINFO_TABLE);
			if (DEBUG) Log.v(TAG,"Creating table: " + SQL_CREATE_GROUPS_TABLE);
			db.execSQL(SQL_CREATE_GROUPS_TABLE);
			if (DEBUG) Log.v(TAG,"Creating table: " + SQL_CREATE_DICTIONARY_TABLE);
			db.execSQL(SQL_CREATE_DICTIONARY_TABLE);
			if (DEBUG) Log.v(TAG,"Creating dataitemdescr: " + SQL_CREATE_DATAITEMDESCR_TABLE);
			db.execSQL(SQL_CREATE_DATAITEMDESCR_TABLE);
			if (DEBUG) Log.v(TAG,"Creating dataitem: " + SQL_CREATE_DATAITEM_TABLE);
			db.execSQL(SQL_CREATE_DATAITEM_TABLE);
				
            db.setTransactionSuccessful();
            
        }
		catch (Exception e) 
        {
			Log.e(TAG,"Error creating DataStore database: "+e);
        } 
        finally
        { 
            db.endTransaction(); 
        } 
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
//	public String[] initDabaseWithData(int resourceId) 
//    {
//    	List<String> results = new ArrayList<String>();
//    	try 
//        {
//    		InputStream is = mContext.getResources().openRawResource(resourceId);
//    		BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf8"));
//    		String readLine = null;
//            while ((readLine = br.readLine()) != null) 
//            {       
//            	results.add(readLine);
//            }
//            is.close();
//            br.close();
//        } catch (Exception e) 
//        {
//            e.printStackTrace();
//        }
//        return results.toArray(new String[0]);
//    }
//	
//	private void execMultipleSQL(SQLiteDatabase db, String[] sql)
//    { 
//        for( String s : sql )
//        {
//            if (s.trim().length()>0)
//            {    
//            	try
//            	{
//            		db.execSQL(s);
//            	}catch(Exception e)
//            	{
//            		Log.e(TAG,"Error exucuting sql: "+e);
//            	}
//            }
//        }
//    }
	
}