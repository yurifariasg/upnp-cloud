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

import android.net.Uri;
import android.provider.BaseColumns;


public final class DataStoreContract {
	
	 /** The authority for the DataStore Provider */
    public static final String AUTHORITY = "com.tpvision.sensormgt.datastore";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final String DATATABLEINFO = "datatableinfo";
    
    /**
     * Column names for DataTableInfo table
     */
    public interface DataTableInfoColumns extends BaseColumns {
    	
    	//BaseColumns include _ID and _COUNT
    	
    	/**
         * Each DataTable is uniquely identified by a GUID, for which the _ID primary key is used.
         * <P>Type: DATATABLE_GUID</P>
         */
    	public static final String DATATABLE_GUID="_ID";
    	
    	/**
         * URN associated with the DataTable
         * <P>Type: DATATABLE_URN</P>
         */
    	public static final String DATATABLE_URN="urn";
    	
    	
    	/**
         * Indicates the number of record columns
         * <P>Type: DATATABLE_URN</P>
         */
    	public static final String DATATABLE_NUMCOLUMNS = "num_reccolumns";
    	
    	
    	/**
         * DataTable Dictionary ID
         * <P>Type: DATATABLE_DICTIONARY_ID</P>
         */
    	public static final String DATATABLE_DICTIONARY_ID = "dictionaryid";
    	
    	/**
         * DataTable UpdateID
         * <P>Type: DATATABLE_UPDATEID</P>
         */
    	public static final String DATATABLE_UPDATEID = "updateid";
    	
    }
    
    public static final class DataTableInfo implements DataTableInfoColumns {
        
    	/**
         * A URI that can be used to retrieve info on all available DataTables
         *
         * <P>Type: TEXT</P>
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+DATATABLEINFO);
    
        /**
         * Get the content:// style URI for a single row in the DataTableInfo table
         *
         * @param rowId the DataTableInfo row to get the URI for
         * @return the URI to DataTableInfo row
         */
        public static final Uri getContentUri(int rowId) {

        	return Uri.parse("content://" + AUTHORITY + "/"+DATATABLEINFO+"/" + rowId);
        }   
    }
    
    /**
     * Column names for Group table
     */
    public interface GroupColumns extends BaseColumns {
    	
    	//BaseColumns include _ID and _COUNT
    	
    	/**
         * DataTable to which the group belongs
         * <P>Type: DICT_DATATABLE_ID</P>
         */
    	public static final String GRP_DATATABLE_ID="datatableid";
    	
    	/**
         * Group nameKey entry
         * <P>Type: GRP_NAME</P>
         */
    	public static final String GRP_NAME="name";
    	
    }
    
    public static final class Groups implements GroupColumns {
        
    	
    	private static final String CONTENT_URI = "content://" + AUTHORITY + "/"+DATATABLEINFO+"/";
    
        /**
         * Get the content:// style URI for a groups corresponding to the dataTable
         *
         * @param tableID the DataTable for which to get the groups
         * @return the URI to dictionary
         */
        public static final Uri getContentUri(int tableID) {

        	return Uri.parse(CONTENT_URI + tableID + "/groups");
        }   
    }
    
    /**
     * Column names for Dictionary table
     */
    public interface DictionaryColumns extends BaseColumns {
    	
    	//BaseColumns include _ID and _COUNT
    	
    	/**
         * DataTable to which the key value pair belongs
         * <P>Type: DICT_DATATABLE_ID</P>
         */
    	public static final String DICT_DATATABLE_ID="datatableid";
    	
    	/**
         * Key entry for Dictionary
         * <P>Type: DICT_KEY</P>
         */
    	public static final String DICT_KEY="key";
    	
    	/**
         * Value entry for Dictionary
         * <P>Type: DICT_VALUE</P>
         */
    	public static final String DICT_VALUE="value";
    }
    
    public static final class Dictionary implements DictionaryColumns {
        
    	
        private static final String CONTENT_URI = "content://" + AUTHORITY + "/"+DATATABLEINFO+"/";
    
        /**
         * Get the content:// style URI for all dictionary items corresponding to the dataTable
         *
         * @param tableID the DataTable for which to get the dictionary
         * @return the URI to dictionary
         */
        public static final Uri getContentUri(int tableID) {

        	return Uri.parse(CONTENT_URI + tableID+ "/dictionary");
        }   
        
        /**
         * Get the content:// style URI for the dictionary item with keyName corresponding to the dataTable
         *
         * @param tableID the DataTable for which to get the dictionary
         * @return the URI to dictionary
         */
        public static final Uri getContentUri(int tableID, String keyName) {

        	return Uri.parse(CONTENT_URI + tableID+ "/dictionary/"+keyName);
        }   
        
        
    }
    
    /**
     * Column names for Records (dataItem descriptions)
     */
    public interface DataItemDescrColumns extends BaseColumns {
    	
    	//BaseColumns include _ID and _COUNT
    	
    	/**
         * DataTable to which the record belongs
         * <P>Type: DII_DATATABLE_ID</P>
         */
    	public static final String DII_DATATABLE_ID="datatableid";
    	
    	/**
         * Field name for the Record
         * <P>Type: DII_FIELDNAME</P>
         */
    	public static final String DII_FIELDNAME="fieldname";
    	
    	/**
         * Field type for the Record
         * <P>Type: DII_FIELDTYPE</P>
         */
    	public static final String DII_FIELDTYPE="fieldtype";
    	
    	/**
         * Encoding of the dataItem
         * <P>Type: DII_ENCODING</P>
         */
    	public static final String  DII_ENCODING="encoding";
    	
    	/**
         * Indicates if the dataItem is required
         * <P>Type: DII_REQUIRED</P>
         */
    	public static final String  DII_REQUIRED="required";
    	
    	/**
         * namespace(For XML-based DataItem)
         * <P>Type: DII_NAMESPACE</P>
         */
    	public static final String  DII_NAMESPACE="namespace";
    	
    	/**
         * Indicates if DataItem refers to DataTable Dictionary
         * <P>Type: DII_TABLEPROP</P>
         */
    	public static final String  DII_TABLEPROP="tableprop";
    	
    	
    }
    
    public static final class DataItemDescr implements DataItemDescrColumns {
        
    	
        private static final String CONTENT_URI = "content://" + AUTHORITY + "/"+DATATABLEINFO+"/";
    
        /**
         * Get the content:// style URI for all info on dataitems associated with a table
         *
         * @param rowId the DataTableInfo row to get the DataItemDescr URI for
         * @return the URI to the DataItemDescr
         */
        public static final Uri getContentUri(int tableID) {

        	return Uri.parse(CONTENT_URI + tableID+ "/dataitemdescr");
        }   
    }
    
    public static final class Records  {
        
    	
        private static final String CONTENT_URI = "content://" + AUTHORITY + "/"+DATATABLEINFO+"/";
    
        /**
         * Get the content:// style URI for all info on records for a table
         *
         * @param rowId the DataTableInfo row to get the record URI for
         * @return the URI to the records
         */
        public static final Uri getContentUri(int tableID) {

        	return Uri.parse(CONTENT_URI + tableID+ "/record");
        }   
    }
    
    /**
     * Column names for DataItemColumns table
     */
    public interface DataItemColumns extends BaseColumns {
    	
    	//BaseColumns include _ID and _COUNT
    	
    	/**
         * DataTable to which the dataitem belongs
         * <P>Type: DI_DATATABLE_ID</P>
         */
    	public static final String DI_DATATABLE_ID="datatableid";
    	
    	/**
         * Indicates when the dataitem was recieved/stored
         * <P>Type: DI_RECEIVE_TIMESTAMP</P>
         */
    	public static final String DI_RECEIVE_TIMESTAMP="receivetimestamp";
    	
    	/**
         * Refers to the index in the DataItem description table
         * <P>Type: DI_DESCR_ID</P>
         */
    	public static final String DI_DESCR_ID="descriptionid";
    	
    	/**
         * Value entry for Dictionary
         * <P>Type: DI_VALUE</P>
         */
    	public static final String DI_VALUE="value";
    }
    
    public static final class DataItem implements DataItemColumns {
        
    	
        private static final String CONTENT_URI = "content://" + AUTHORITY + "/"+DATATABLEINFO+"/";
    
        /**
         * Get the content:// style URI for all data items corresponding to a single row in the DataTableInfo table
         *
         * @param tableID the Table for which to get the dataItems
         * @return the URI to the dataitems
         */
        public static final Uri getContentUri(int tableID) {

        	return Uri.parse(CONTENT_URI + tableID+ "/dataitem");
        }   
    }
    
}
    
    