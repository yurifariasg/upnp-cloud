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

package ufcg.embedded.upnp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import org.fourthline.cling.model.types.UDN;

import com.tpvision.sensormgt.datamodel.DataItem;
import com.tpvision.sensormgt.datamodel.Datamodel;
import com.tpvision.sensormgt.datamodel.DatamodelInterfaceImpl;
import com.tpvision.sensormgt.datamodel.DatamodelNode;
import com.tpvision.sensormgt.datamodel.DatamodelNode.NodeType;
import com.tpvision.sensormgt.datamodel.SensorReadListener;
import com.tpvision.sensormgt.datamodel.SensorWriteListener;
import com.tpvision.sensormgt.datamodel.SingleInstanceNode;
import com.tpvision.sensormgt.datastore.DataStoreInterfaceImpl;
import com.tpvision.sensormgt.devicelib.ClingUPnPInit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

//TODO: test attributes and EOC on SensorEvent on friendly name
//TODO: getInconsistentStatus
//TODO: adding devices dynamically, createInstance, deleteInstance
//TODO: debug XMLUPnPUtil

//TODO: javadoc
//TODO: check all todo items and warnings
//TODO: cleanup debug logging

/**
 * SensorManagement Device which implements the Fridge Device example
 * The Fridge contains 3 temperature sensors, a door open alarm, and reports energy consumption
 * 
 * Control Points can:
 * - obtain a list of available devices by invoking: getInstances("/UPnP/SensorMgt/SensorCollections/", "1");
 * The fridge will return a single sensor collection
 * 
 * - check which sensors are available within a Collection by invoking: 
 * getInstances("/UPnP/SensorMgt/SensorCollections/1/Sensors/", "1");
 * 
 * - check which parameters are available by invoking: 
 * For example to check which parameters are available for SensorCollections use
 * getSupportedParameters("/UPnP/SensorMgt/SensorCollections/#/",1);
 * 
 * - Read the value of a datamodel field by the invoking getValues action.
 * For example: 
 * GetValues(
 *	"<?xml version="1.0" encoding="UTF-8"?>
 *	 <cms:ContentPathList xmlns:cms="urn:schemas-upnp-org:dm:cms"
 *	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance
 *	 xsi:schemaLocation="urn:schemas-upnp-org:dm:cms
 *	 http://www.upnp.org/schemas/dm/cms.xsd">
 *		 <ContentPath>
 *			/UPnP/SensorMgt/SensorCollections/1/CollectionFriendlyName
 *	 	</ContentPath></cms:ContentPathList>" )
 * 
 * Eventing:
 * - Control points can subscribe to GENA events
 * Control points receive Sensor and SensorCollection configuration events (which are always enabled). 
 * - Control points can enable events of certain types from individual sensor instances.
 * Value changes of individual sensor instances are controlled by the SensorEventsEnable parameter. 
 * The SensorEventsEnable parameter is a comma separated value list, indicating which event type is enabled
 * The following example enables SOAP Eventing for sensor 1 of sensorCollection 1, and disables all other types
 * SetValues(
 *	"<?xml version="1.0" encoding="UTF-8"?>
 *	 <cms:ParameterValueList xmlns:cms="urn:schemas-upnp-org:dm:cms"
 *	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance
 *	 xsi:schemaLocation="urn:schemas-upnp-org:dm:cms
 *	 http://www.upnp.org/schemas/dm/cms.xsd">
 *	 	<Parameter>
 *			<ParameterPath>
 *				/UPnP/SensorMgt/SensorCollections/1/Sensors/1/SensorEventsEnable,
 *			</ParameterPath>
 *			<Value>
 *				SensorSOAPDataAvailableEnable,1,SensorSOAPDataOverrunEnable,0,
 *				SensorTransportDataAvailable,0,SensorTransportDataOverrun,0,
 *				SensorTransportConnectionError,0
 *			</Value>
 *		</Parameter>
 *	</cms:ParameterValueList>" ) 
 *  
 *  - Control points 
 * 
 */


public class FridgeDeviceMainActivity extends Activity implements SensorReadListener, SensorWriteListener {

	protected static final String TAG = "DeviceMainActivity";
	
	private static final long DOOROPENALARM_TIME = 5; //just 5 seconds for testing
	
	private static final CharSequence APP_ABOUT_TITLE = "UPnP SensorManagement Fridge Device";
	private static final CharSequence APP_ABOUT_MESSAGE = "UPnP SensorManagement Fridge Device\n\nVersion 1.11";
	
	private DatamodelInterfaceImpl mDatamodelInterface;
	private DataStoreInterfaceImpl mDataStoreInterfaceImpl;
	private ClingUPnPInit mCling;
	private UDN udn;

	private TemperatureSensor mFreezerTemp;
	private TemperatureSensor mGroceryTemp;
	private TemperatureSensor mVegetableTemp;
	private AlarmSensor mDoorOpenAlarm;

	private Led mSensorChangedLed;
	private Led mSensorReadLed;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String udnPref = prefs.getString("UDN",null);
		if (udnPref!=null) {
			//we have a stored UDN, so use it
			udn = new UDN(udnPref);
			Log.i("udn","old udn: "+udn.toString() );
		} else {
			//we need to generate a udn and store it
			udn = new UDN(UUID.randomUUID());	
			Log.i("udn","new udn: "+udn.toString() );
			SharedPreferences.Editor editor = prefs.edit();
		    editor.putString("UDN", udn.getIdentifierString());
		    editor.commit();
		}
		
		mDataStoreInterfaceImpl = new DataStoreInterfaceImpl(getContentResolver());
		
		setContentView(R.layout.activity_main);

		final ImageView fridgeImg = (ImageView) findViewById(R.id.imageView1);
		fridgeImg.setOnClickListener(new View.OnClickListener() {
			private boolean fridgeState;

			public void onClick(View v) {
				if (fridgeState) {
					fridgeImg.setImageResource(R.drawable.fridge_closed);
					fridgeState = false;
					mDoorOpenAlarm.resetAlarm();
				} else {
					fridgeImg.setImageResource(R.drawable.fridge_off);
					fridgeState = true;
					mDoorOpenAlarm.setAlarm();
				}
			}
		});
		
		mSensorChangedLed = (Led) findViewById(R.id.redled);
		mSensorReadLed = (Led) findViewById(R.id.greenled);

		setupSensorNetwork();
		//setText();	
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.about:
	        	showAboutDialog();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	public void showAboutDialog() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle(APP_ABOUT_TITLE);

		// set dialog message
		String message = APP_ABOUT_MESSAGE + "\n" + udn.toString();
		alertDialogBuilder
		.setMessage(message)
		.setCancelable(false)
		.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	//starting the android service when the activity becomes visible
	@Override
	protected void onStart() {
		super.onStart();
		mCling = new ClingUPnPInit(getApplicationContext(), udn, mDatamodelInterface, mDataStoreInterfaceImpl);
		
	}

	//stopping the upnp service when the activity is invisible
	@Override 
	protected void onStop() {
		mCling.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		//To really kill the app during development
		//Process.killProcess(Process.myPid());

		super.onDestroy();
	}

	/**
	 * This is where the sensor network is initialised, the datamodel, is read,
	 * the device info is read, and sensors are created
	 * 
	 */
	private void setupSensorNetwork() {

		DatamodelNode instanceTree = new SingleInstanceNode(null, "UPnP");
		DatamodelNode child = new SingleInstanceNode(instanceTree, "SensorMgt");
		instanceTree.addChildNode(child);
		DeviceParser.parse(getResources().openRawResource(R.raw.sensor_config), child);

		List<DataItem> dataItemList = new ArrayList<DataItem>();

		String urn = "urn:upnp-org:smgt-surn:refrigerator:AcmeSensorsCorp-com:AcmeIntegratedController:FrigidaireCorp:rf217acrs:monitor";
		
		DataItem mAccumulatedPowerUsed = new DataItem("SensorCollection0001", "Sensor0001", urn, "AccumulatedPowerUsed");
		mAccumulatedPowerUsed.setSensorValue("50");
		
		int updatePeriod = 10; //seconds between sensor updates
		
		// three sensors
		mFreezerTemp = new TemperatureSensor("SensorCollection0001", "Sensor0001", urn, "FreezerTemp");
		mFreezerTemp.setAvgTemp(-21);
		mFreezerTemp.setSensorValue("-21");
		mFreezerTemp.setUpdatePeriod(updatePeriod);
		
		mGroceryTemp = new TemperatureSensor("SensorCollection0001", "Sensor0001", urn, "GroceryTemp");
		mGroceryTemp.setAvgTemp(6);
		mGroceryTemp.setSensorValue("6");
		mGroceryTemp.setUpdatePeriod(updatePeriod);
		
		mVegetableTemp = new TemperatureSensor("SensorCollection0001", "Sensor0001", urn, "VegetableTemp");
		mVegetableTemp.setAvgTemp(8);
		mVegetableTemp.setSensorValue("8");
		mVegetableTemp.setUpdatePeriod(updatePeriod);

		mDoorOpenAlarm = new AlarmSensor("SensorCollection0001", "Sensor0001", urn, "DoorOpenAlarm");
		mDoorOpenAlarm.setSensorValue("0");
		mDoorOpenAlarm.setTimeOut(DOOROPENALARM_TIME); 
		
		DataItem mPowerFaultAlarm = new DataItem("SensorCollection0001", "Sensor0001", urn, "PowerFaultAlarm");
		mPowerFaultAlarm.setSensorValue("0");
		DataItem mStatusInterval = new DataItem("SensorCollection0001", "Sensor0001", urn, "StatusInterval");
		mStatusInterval.setSensorValue(Integer.toString(updatePeriod));
		
		DataItem mClientID = new DataItem("SensorCollection0001", "Sensor0001", urn, "ClientID");

		dataItemList.add(mAccumulatedPowerUsed);
		dataItemList.add(mFreezerTemp);
		dataItemList.add(mGroceryTemp);
		dataItemList.add(mVegetableTemp);
		dataItemList.add(mDoorOpenAlarm);
		dataItemList.add(mPowerFaultAlarm);
		dataItemList.add(mStatusInterval);
		
		dataItemList.add(mClientID);
		

		mDatamodelInterface = new DatamodelInterfaceImpl(Datamodel.inflateDatamodelTree(Datamodel.mDatamodelDefinition), instanceTree, dataItemList);

		//inform me on any sensor access
		for (DataItem sensor : dataItemList) {
			sensor.addOnSensorWriteListener(this);
			sensor.addOnSensorReadListener(this);
		}

		Log.d(TAG, "SensorEventsEnable");
		// get all sensors in all collections
		List<DatamodelNode> sensorInstances = mDatamodelInterface.findNodes(instanceTree, "/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorEventsEnable");
		Iterator<DatamodelNode> iterator = sensorInstances.iterator();
		while (iterator.hasNext()) {
			DatamodelNode sensorEnable = (DatamodelNode) iterator.next();
			if (sensorEnable.getNodeType() == NodeType.LEAF) {
				Log.d(TAG, "SensorEventsEnable: " + sensorEnable);

				// set event listener
			}
		}
	}
	
	private Runnable blinkSensorChangedLedRunnable = new Runnable() {
		
		@Override
		public void run() {
			mSensorChangedLed.blink(); 
		}
	};
	
	private Runnable blinkSensorReadLedRunnable = new Runnable() {
		

		@Override
		public void run() {
			mSensorReadLed.blink(); 
		}
	};

	@Override
	public void onSensorWrite(String collectionID, String sensorID,
			DataItem dataItem) {
		//if a controlpoint changed the statusInterval, update the temperature sensor intervals
				if (dataItem.getName().contentEquals("StatusInterval")) {
					String statusIntervalString = dataItem.getSensorValue();
					
					int interval = 0;
					try {
						interval = Integer.parseInt(statusIntervalString);
					} catch (NumberFormatException e) {
						//Value is not ok, turn statusInterval update off
					}
					if ((interval>=0) && (interval<=600)) {
						mFreezerTemp.setUpdatePeriod(interval);
						mGroceryTemp.setUpdatePeriod(interval);
						mVegetableTemp.setUpdatePeriod(interval);
					}	
				}
				
				runOnUiThread(blinkSensorChangedLedRunnable);
		
	}

	@Override
	public void onSensorRead(String mCollectionID, String mSensorID,
			DataItem dataItem) {
		runOnUiThread(blinkSensorReadLedRunnable);	
	}
	
}
