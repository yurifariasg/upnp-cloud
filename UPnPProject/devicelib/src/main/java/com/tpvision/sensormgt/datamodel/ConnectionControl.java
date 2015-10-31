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

package com.tpvision.sensormgt.datamodel;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.tpvision.sensormgt.sensormodel.DataItemInfo;

import android.util.Log;

/**  
 * When a client wants to receive data, it calls the connect sensor action
 * The sensor indicated by SensorID and SensorURN contains a set of dataitems.
 * The connectSensor call identifies which of these dataItems the client is interested in.
 * 
 * In this implementation data items report changes via the onSensorValueChanged() callback function.
 * For the connectionControl a SensorValueChangedListener is added to each dataItem
 * DataItems keep track which client connections want to be informed of a change
 * 
 * When a dataItem changes its value, all clients which registered for this dataitem are informed.
 * For each client connection a list of DataItems that should be included in the DataRecord is maintained
 * Since dataItems belonging to a SensorURN, don't need to change at the same time in this implementation we apply moderation
 * 
 * A client can subscribe to multiple SensorURNs using the same URL
 * A client can subscribe to the same SensorURNs using different sets of dataItems
 * 
 * @author mekenkam
 *
 */		

public class ConnectionControl implements SensorChangeListener {
	
	protected static final String TAG = "ConnectionControl";
	protected static final boolean DEBUG = true;
		
	private int MAXCLIENTS=16; //maximum number of allowed connections.
	
	private ClientConnectionInfo[] mConnectedClients = new ClientConnectionInfo[MAXCLIENTS];

	private ArrayList<SensorEventData> mPendingSensorEvents = new ArrayList<SensorEventData>();

	private static final long TIMEOUT = 200; //200ms delay before sending event
	private Timer mTimer = new Timer();
	private TimerTask mTimerTask = new EventTimerTask();

	private ArrayList<TransportConnectionErrorListener> listeners = new ArrayList<TransportConnectionErrorListener>();

	public ConnectionControl() {
		
	}
	
	public void addOnTransportConnectionErrorListener(TransportConnectionErrorListener transportConnectionErrorListener) {
		listeners.add(transportConnectionErrorListener);
	}
	
	public void removeOnTransportConnectionErrorListener(TransportConnectionErrorListener transportConnectionErrorListener) {
		listeners.remove(transportConnectionErrorListener);
	}
	
	private void transportConnectionError(String collectionID, String sensorID) {
		
		for (TransportConnectionErrorListener listener: listeners) {
			listener.onTransportConnectionError(collectionID, sensorID);
		}
	}
	
	
	
	/**
	 * Sets up a connection to send future sensor events of the indicated sensor to the indicated transportURL
	 * The DataItemList indicates which dataItems need to be send for this sensor. 
	 * 
	 * This method assumes inputs are correct. Validity of SensorID, and SensorURN and list of dataItems needs to be verified before calling this method
	 * 
	 * @param sensorID
	 * @param sensorURN
	 * @param transportURL
	 * @param dataItem
	 * @param sensorDatatypeEnable
	 * @return
	 * @throws UPnPException 
	 */
	public String connectSensor(String sensorID, String clientID, String sensorURN, String transportURL, 
			List<SensorRecordInfo> requestedRecordInfoList, List<DataItem> dataItemList, boolean sensorDatatypeEnable) throws UPnPException {
		
		if (DEBUG) Log.d(TAG,"connectSensor("+sensorID+", "+sensorURN+", "+transportURL);
				
		int clientIndex=0;
		try {
			URL url = new URL(transportURL);
			
			//register the sensor and data items, throw UPnPException if num connections exceeded
			clientIndex = addClient(clientID, sensorID, sensorURN, url, requestedRecordInfoList, dataItemList, sensorDatatypeEnable);
			
			//register clientID in dataItems, for efficiently in dealing with large numbers of sensors
			registerClientWithDataItems(Integer.toString(clientIndex), dataItemList);
			
		} catch (MalformedURLException e) {
			throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX); //TODO: check which error
		}
		
		return Integer.toString(clientIndex);
	}
	
	
	/**
	 * 
	 * @param sensorID
	 * @param transportURL
	 * @param transportConnectionID
	 * @throws UPnPException
	 */
	public void disconnectSensor(String sensorID, String transportURL, String transportConnectionID) throws UPnPException {

		if (DEBUG) Log.d(TAG,"disconnectSensor("+sensorID+", "+transportURL+", "+transportConnectionID);
		
		URL url;
		
		try {
			url = new URL(transportURL);
		} catch (MalformedURLException e) {
			throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_NOT_FOUND);
		}
		
		if ((sensorID!=null) && (transportURL!=null)) {
			if ((transportConnectionID!=null) && !transportConnectionID.isEmpty()) {
				int connID=-1;
				try {
					connID = Integer.parseInt(transportConnectionID);
				} catch (NumberFormatException e){
					throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_NOT_FOUND);
				}
				if ((connID<0) || (connID>=mConnectedClients.length)) {
					//out of bounds
					throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_NOT_FOUND);
				} 
				if (mConnectedClients[connID]==null) {
					//was already disconnected
					throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_NOT_FOUND);
				} 
				if (!url.equals(mConnectedClients[connID].getTransportURL())) {
					//transportURL doesn't match
					throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_NOT_FOUND);
				}
				//everything OK, disconnect
				if (DEBUG) Log.d(TAG,"disconnect conn "+ connID);
				unregisterClientWithDataItems(transportConnectionID, mConnectedClients[connID].getDataItems());
				mConnectedClients[connID]=null;
			} else {
				
				
				//no transportConnectionID, disconnect all connections to the same transportURL from the sensor
				boolean anyDisconnected = false;
				for (int i=0; i < mConnectedClients.length; i++) {
					if ((mConnectedClients[i]!=null) 
							&& (sensorID.contentEquals(mConnectedClients[i].getSensorID())
									&& (url.equals(mConnectedClients[i].getTransportURL()))))	{
						//sensorID and transportURL match, disconnect this one					
						unregisterClientWithDataItems(Integer.toString(mConnectedClients[i].getConnectionID()), mConnectedClients[i].getDataItems());
						mConnectedClients[i]=null;
						anyDisconnected = true;
					}
				}
				if (!anyDisconnected) throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_NOT_FOUND);
			}
		} else {
			throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_NOT_FOUND);
		}
	}
	
	public ArrayList<ClientConnectionInfo> getSensorTransportConnections(String sensorID) throws UPnPException {
		
		ArrayList<ClientConnectionInfo> transportConnections = new ArrayList<ClientConnectionInfo>();
		Log.d(TAG,"getSensorTransportConnections ");
		
		for (ClientConnectionInfo connectedClient: mConnectedClients) {
			//only add the active connection slots
			if (connectedClient != null) {
				Log.d(TAG,"connected client "+connectedClient.getTransportURL()+", "+ connectedClient.getSensorID()+", "+connectedClient.getConnectionID());

				//only add connection to current sensor
				if (connectedClient.getSensorID().contentEquals(sensorID)) {
					Log.d(TAG,"OK connected client "+connectedClient.getTransportURL()+", "+ connectedClient.getSensorID()+", "+connectedClient.getConnectionID());

					transportConnections.add(connectedClient);
				}
			}
		}
		
		return transportConnections;
	}
	
//TODO: get Namespace from SensorRecordInfo
	/**
	 * 
	 * Checks if the number of allowed connections is not exceeded. 
	 * If connections are possible the URL and SensorID are stored for sending future sensor updates
	 * The connectionID is returned
	 * 	
	 */
	private int addClient(String ClientID, String sensorID, String sensorURN, URL transportURL, 
			List<SensorRecordInfo> requestedRecordInfoList, List<DataItem> dataItemList, boolean sensorDatatypeEnable) throws UPnPException {
		
		int i=0;
		boolean freeSlot=false;
		while ((i< mConnectedClients.length) && !freeSlot) {
			freeSlot = (mConnectedClients[i]==null);
			i++;
		}
		if (freeSlot) {
			Log.d(TAG,"new connection added: "+ transportURL+"on slot "+(i-1));
			mConnectedClients[i-1] = new ClientConnectionInfo(ClientID, i-1, sensorID, sensorURN, transportURL, requestedRecordInfoList, dataItemList, sensorDatatypeEnable);
		} else {
			throw new UPnPException(UPnPSensorTransportErrorCode.TRANSPORTCONN_LIMITEXCEEDED);
		}
		
		return i-1;
	}
	
	private void registerClientWithDataItems(String clientID, List<DataItem> dataItemList) {
		
		for (DataItem dataItem: dataItemList)  {
			if (DebugDatamodel.DEBUG_EVENTING) Log.d(TAG, "Registered client connection "+ clientID+"with dataItem:" + dataItem.getName());
			dataItem.registerClient(clientID);
		}	
	}
	
	private void unregisterClientWithDataItems(String clientID, List<DataItem> dataItemList) {
		
		for (DataItem dataItem: dataItemList)  {
			if (DebugDatamodel.DEBUG_EVENTING) Log.d(TAG, "Unregister client connection "+ clientID+"for dataItem:" + dataItem.getName());
			dataItem.unregisterClient(clientID);			
		}	
	}
	
	/**
	 * Callback, dataItems call this function when a dataItem's value changed
	 */
	@Override
	public void onSensorChange(String collectionID, String sensorID, boolean valueRead, boolean valueTransported,  
			DataItem dataItem) {

		if (DebugDatamodel.DEBUG_EVENTING) Log.d(TAG, "onSensorValueChanged: "+ collectionID+" ," + sensorID);
		
		//for each client, send a datarecord including the additionally requested data items 
		List<String> clients = dataItem.getRegisteredClients();
		if (clients!=null) {
			for (String client : clients) {
				//find client
				int clientID = Integer.parseInt(client);
				String datarecord = createDataRecord(mConnectedClients[clientID].getDataItems(), mConnectedClients[clientID].getSensorDatatypeEnable(), mConnectedClients[clientID].getRequestedDataItemInfo());

				//send to client URL
				if (DebugDatamodel.DEBUG_EVENTING) Log.d(TAG, "Send to URL: "+ mConnectedClients[clientID].getTransportURL());
//TODO: make asynchronous				
				sendRecordToURL(datarecord, mConnectedClients[clientID]);
			}
		}

		//		addPendingSensorEvent(collectionID,  sensorID, eventType)
	}
	
	public void sendRecordToURL(String datarecord, ClientConnectionInfo clientConnection) {
		
		URL transportURL = clientConnection.getTransportURL();
		
		//Setup the connection
		HttpURLConnection conn=null;
//TODO: store connection for faster implementation
		try {
			conn = (HttpURLConnection) transportURL.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");	

			//conn.setChunkedStreamingMode(0);

			conn.setFixedLengthStreamingMode(datarecord.getBytes().length);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// set the connection timeout to 5 seconds and the read timeout to 10 seconds
		     //conn.setConnectTimeout(5000);
		     //conn.setReadTimeout(10000);
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

		//POST the data
		OutputStream out;
		try {
			out = new BufferedOutputStream(conn.getOutputStream());
			out.write(datarecord.getBytes());
			out.close();
			
			// NOW you can look at the status.
		    int http_status = conn.getResponseCode();
		    if (http_status != 200) {
		     //error
//FIXME: hardcoded collection ID		    	
		    	transportConnectionError("SensorCollection0001",clientConnection.getSensorID());
		    	 Scanner s = new Scanner(conn.getErrorStream()); 
		    }
		    
		  } catch (IOException e) {
		    
		  }
			
			
			//process response
			String response= "";
			Scanner in=null;
			try {
				in = new Scanner(conn.getInputStream());
				while(in.hasNextLine()) {
					response+=(in.nextLine());
				}
				in.close();
				
				//ok nothing went wrong, register that the sensordata was processed
				List<DataItem> dataItems = clientConnection.getDataItems();
				for (DataItem dataItem: dataItems) {
					dataItem.setTransportProcessed();
				}
				
				
			} catch (IOException e) {
//FIXME: hardcoded collection ID		    	
				transportConnectionError("SensorCollection0001",clientConnection.getSensorID());
		    	Log.w(TAG, "Error posting sensor records");
		    	
			}
			

	}
	
	
	private String createDataRecord(List<DataItem> dataItems, boolean sensorDatatypeEnable, List<SensorRecordInfo> requestedRecordInfoList) {

		ArrayList<DataItemInfo> dataItemInfoList = new ArrayList<DataItemInfo>(dataItems.size());
		
		//copy dataItem info from "real" sensor to DataItemInfo
		for (DataItem di: dataItems) {
			DataItemInfo dii = new DataItemInfo(di.getName(), di.getType(), di.getEncoding(), di.getNameSpace());
			dii.setValue(di.getSensorValue()); 
		
			//check if we need to set a prefix for this dataItem, search the RequestedRecordInfo list for the current dataItem
			for (SensorRecordInfo sensorRecord: requestedRecordInfoList) {
				if (sensorRecord.getFieldName().contentEquals(di.getName())) {
					dii.setPrefix(sensorRecord.getPrefix());
				}
			}
			
			dataItemInfoList.add(dii);
		}
		
		
		//TODO: add sensorDatatypeEnable
		return XMLUPnPUtil.createXMLDataRecords(new DataRecordInfo(dataItemInfoList), sensorDatatypeEnable);
	}
	
	/**
	 * Add the current event to the list of pending sensor events, and set the timer.
	 * It will be sent if within 200ms no new event is added
	 * 
	 * @param collectionID
	 * @param sensorID
	 * @param eventType
	 */
	private void addPendingSensorEvent(String collectionID, String sensorID, String eventType) {

		if (DEBUG) Log.i(TAG,"add Pending Event: "+collectionID+" ,"+sensorID+", "+eventType);

		mPendingSensorEvents.add(new SensorEventData(collectionID, sensorID, eventType));
		
		mTimerTask.cancel();
		mTimerTask = new EventTimerTask();
		mTimer.purge();
		mTimer.schedule(mTimerTask, TIMEOUT);
	}

	

	
	/**
	 * Gets the current date-time as ISO8601 formatted string
	 * @return date-time string, IDO8601 formatted
	 */
	private String getCurrentDateTime() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	    df.setTimeZone(tz);
	    return df.format(new Date());
	}


	
	private class EventTimerTask extends TimerTask {          
		@Override
		public void run() {
			if (DEBUG) Log.i(TAG,"Event timer expired, setting SensorEvents parameter...");	
			
			
			//now send all sensor events to all connections
			//sendDataItems(mPendingSensorEvents);
		}
	}
		
	
}

