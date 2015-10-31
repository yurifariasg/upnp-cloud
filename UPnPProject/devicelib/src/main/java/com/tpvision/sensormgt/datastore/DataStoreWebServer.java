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

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import android.util.Log;

/**
 * Implements the HTTP server using the NanoHTTPD server
 * Supports posting DataRecord Items, which are stored in the database
 * Additionally, supports getting HTML view
 */

public class DataStoreWebServer extends NanoHTTPD {
   
	private static final String TAG = "DataStoreServer";
	private DataStoreInterfaceImpl mDataStore;
	private static final boolean DEBUG=DebugDataStore.DEBUG_DATASTORESERVER;

	/**
	 * Creates the DataStore web server. dataStore is set to allow access to database information 
	 * @param port portnumber on which the server is available
	 * @param dataStore access to all database functions
	 */
	public DataStoreWebServer(int port, DataStoreInterfaceImpl dataStore) {
        super(port);
        mDataStore = dataStore;
    }

	
	/**
	 * Serve function, callback from server implementation. Implements the responses to GET and POST
	 */
    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> params, Map<String, String> files) {
        if (DEBUG) Log.d(TAG, "Serve :"+method + " '" + uri + "' ");

        //printMap(header);
        //printMap(params);
        
        String msg="";
        if (method == Method.GET) {
        	
        	if (uri.contentEquals("/datastore/datatables")) {
        		msg = "<html><body><h1>DataStore Server</h1>\n";
        		msg += createHTML();
        		msg += "</body></html>\n";
        	}
        }
        if (method == Method.POST) {
        	
        	if (DEBUG) Log.d(TAG, params.get("NanoHttpd.QUERY_STRING"));
        	String response="";
        	
        	int index = uri.lastIndexOf('/');
        	if (index!=-1) {
        		String path = uri.substring(0, index+1);
        		String id = uri.substring(index+1, uri.length());
        		String received = params.get("NanoHttpd.QUERY_STRING");
        		
        		if (path.contentEquals(NetworkInfo.getDatatablePath())) {
        			try {
        				response = mDataStore.writeDataStoreTableRecords(id,  received);
        				
                		//TODO: return appropriate error response message
                	} catch (UPnPException e) 
                	{
                		Log.e(TAG, "Error writing data record: "+e);	
                		Log.w(TAG, "Recieved: "+received);	
                	}
        		}
        	}
        	
        	return new Response(response);
        }

        return new Response(msg);
    }
    
    
    @SuppressWarnings("unused")
	private void printMap(Map<String, String> map) {
    	for (Map.Entry<String, String> entry : map.entrySet()) { Log.d(TAG, "Key = " + entry.getKey() + ", Value = " + entry.getValue()); }
    }
    
    
    /**
     * Generates list of dataItems in HTML.
     */
    private String createHTML() {
    	String msg = "<table summary=\"DataStore\">"; 
    	msg += "<caption> DataStoreTables </caption>\n";
    	msg += "<tr>\n  <th scope=\"col\"> Time </th> <th scope=\"col\"> Name </th>\n <th scope=\"col\"> Value </th>\n</tr>\n"; 
 //FIXME:   	
//    	try {
//			DataItemInfo[] dataItems = mDataStore.readDataStoreRecordsArray("1", null, 0, 50, false);
//			for (int i=0; i < dataItems.length; i++) {
//				msg += "<tr>\n  <th scope=\"col\">" + dataItems[i].getReceivedTime();
//				msg += "</th> <th scope=\"col\">" + dataItems[i].getFieldName();
//				msg += "</th> <th scope=\"col\">" + dataItems[i].getValue();
//				msg += "</th>\n</tr>\n"; 
//			}
//			
//			
//		} catch (UPnPException e) {
//			e.printStackTrace();
//		}
    	
    	msg += "</table>"; 
    	
    	return msg;
    }
    
}
