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

package upnp.controlpoint.fridge;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tpvision.sensormgt.controlpoint.fridge.R;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem.SensorDataItemReadListener;
import com.tpvision.sensormgt.upnpcontrolpoint.model.Sensor;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorCollection;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorCollection.SensorCollectionDataAvailableListener;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorMgtDevice;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorURN;

import java.util.ArrayList;

import upnp.controlpoint.fridge.view.EnergySensorView;
import upnp.controlpoint.fridge.view.SensorViewBase;
import upnp.controlpoint.fridge.view.TemperatureSensorView;

//TODO: enable SOAP eventing, using synchronous call
//TODO: parse dataItemDescriptions
//TODO: handle disappearance of device, and selecting new device

//TODO: homeautomationcontrol : some code cleanup + disappeareance of device

//TODO: move store of preferences, enable single and multiple selected devices, incorporate into library


/**
 * 
 * Creates a UserInterface to display information from a FridgeSensorMgt device
 * 
 * Detects sensorManagementDevices and allows to user to chose one
 * Checks if the selected device is a refrigerator
 * Finds all Temperature sensors, reads their values one by one 
 * ControlPointLib Subscribes GENA events which triggers sensordataAvailable callback
 * 
 */

public class FridgeActivity extends FragmentActivity  implements DevicesDialog.DialogNotificationListener,
		SensorCollectionDataAvailableListener, SensorDataItemReadListener {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "FridgeActivity";
	
	private static final CharSequence APP_ABOUT_TITLE = "UPnP SensorManagement Fridge";
	private static final CharSequence APP_ABOUT_MESSAGE = "ControlPoint app for \nUPnP SensorManagement Fridge device\n\nVersion 1.1";
	
	private FridgeApplication mApplication;
	private boolean mAlwaysShowDeviceDialog=true;
	private SensorMgtDevice mCurrentSensormgtDevice;
	private SensorCollection mFridgeSensorCollection;
	
	
	private ArrayList<DataItem> mTempSensors;
	private ArrayList<DataItem> mEnergySensors;
	private ArrayList<DataItem> mAlarms;
	private ArrayList<SensorViewBase> mTemperatureSensorViews = new ArrayList<SensorViewBase>();
	private ArrayList<SensorViewBase> mEnergySensorViews = new ArrayList<SensorViewBase>();
	
	private TextView mHeaderFriendlyName;
	private ListView mTempSensorListView;
	private SensorAdapter mTempSensorListAdapter;
	private ListView mEnergySensorListView;
	private SensorAdapter mEnergySensorListAdapter;
	private ArrayList<DataItem> mConfigurationDataItems;
	private FridgeActivity mActivity;
	private String mPreviousAlarm="0";
	private TextView mTempSensorListHeader;
	private TextView mEnergySensorListHeader;
	
	

	private static final String SCT_FRIDGE = "urn:upnp-org:smgt-sct:refrigerator:";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mActivity = this;
		mApplication = ((FridgeApplication)this.getApplication());

		if (mApplication.InitializeUPnP()) {
			//			mApplication.getUPnP().setDeviceListListener(this);
		}
		
		setContentView(R.layout.fridge_activity);
		mHeaderFriendlyName = (TextView) findViewById(R.id.header_friendlyName);
		mTempSensorListView = (ListView) findViewById(R.id.tempSensors);
		View tempListHeader = getLayoutInflater().inflate(R.layout.list_header, null);
		mTempSensorListHeader = (TextView) tempListHeader.findViewById(R.id.listHeader);
		mTempSensorListHeader.setText("");
		mTempSensorListView.addHeaderView(tempListHeader);
		mTempSensorListAdapter = new SensorAdapter(this, mTemperatureSensorViews);
		mTempSensorListView.setAdapter(mTempSensorListAdapter);
		mTempSensorListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				showConfigurationDialog(mConfigurationDataItems);

				return false;
			}
		});
		
		mEnergySensorListView = (ListView) findViewById(R.id.energySensors);
		View energyListHeader = getLayoutInflater().inflate(R.layout.list_header, null);
		mEnergySensorListHeader = (TextView) energyListHeader.findViewById(R.id.listHeader);
		mEnergySensorListHeader.setText("");
		mEnergySensorListView.addHeaderView(energyListHeader);
		mEnergySensorListAdapter = new SensorAdapter(this, mEnergySensorViews);
		mEnergySensorListView.setAdapter(mEnergySensorListAdapter);
		
		if (mAlwaysShowDeviceDialog || mApplication.getCurrentDevice()==null) {
			showDevicesDialog();
		}
	}

	//stopping the upnp service when the activity is invisible
	@Override 
	protected void onStop() {
		mApplication.stopUPnP();
		super.onStop();
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
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
	        case R.id.search:
	        	showDevicesDialog();
	        	return true;
	        case R.id.about:
	        	showAboutDialog();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}

	public void showDevicesDialog() {

	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    Fragment prev = getFragmentManager().findFragmentByTag("DevicesDialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
  
	    // Create and show the dialog.
	    DevicesDialog newFragment = DevicesDialog.newInstance();   
	    
	    mApplication.getUPnP().search(); //trigger M-Search when we open the dialog
	    newFragment.show(ft, "DevicesDialog");
	}
	
	public void showConfigurationDialog(ArrayList<DataItem> dataItems) {

	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    Fragment prev = getFragmentManager().findFragmentByTag("ConfigurationDialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
  
	    // Create and show the dialog.
	    ConfigurationDialog newFragment = ConfigurationDialog.newInstance();
	    newFragment.setAssociatedDataItems(dataItems);
	    
	    mApplication.getUPnP().search(); //trigger M-Search when we open the dialog
	    newFragment.show(ft, "ConfigurationDialog");
	}
	
	public void showAboutDialog() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle(APP_ABOUT_TITLE);

		// set dialog message
		alertDialogBuilder
		.setMessage(APP_ABOUT_MESSAGE)
		.setCancelable(false)
		.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	public void setFridgeHeaders(final String friendlyName, String information) {
			
		Runnable fridgeHeaderRunnable = new Runnable() {
			
			@Override
			public void run() {
				mHeaderFriendlyName.setText(friendlyName);		
				mTempSensorListHeader.setText("Temperature Sensors");
				mEnergySensorListHeader.setText("Energy Sensors");
			}
		};
		
		runOnUiThread(fridgeHeaderRunnable);
		
	}
	
	public ArrayList<DataItem> findDataItems(String collectionType, String sensorType, String dataItemUnits) {
		
		ArrayList<DataItem> dataItemsFound = new ArrayList<DataItem>();
		
		if (mFridgeSensorCollection!=null) {
			
			ArrayList<Sensor> sensors = mFridgeSensorCollection.getSensors();
			for (Sensor sensor: sensors) {
				if (sensor.getType().endsWith(collectionType)) {
					//found a monitor sensor
					if (DEBUG) Log.d(TAG, "Found Sensor of type: "+ sensor.getType());
					ArrayList<SensorURN> sensorURNs = sensor.getSensorURNs();
					for (SensorURN sensorURN: sensorURNs) {
						if (sensor.getType().endsWith(sensorType)) {
							//found a monitor sensorURN
							if (DEBUG) Log.d(TAG, "Found SensorURN of type: "+ sensorURN.getType());
							ArrayList<DataItem> dataItems = sensorURN.getDataItems();
							for (DataItem dataItem : dataItems) {
								dataItem.getName();
								String description = dataItem.getDescription();
								//FIXME: parse the dataItem description
								if (description.contains(dataItemUnits)) {
									if (DEBUG) Log.d(TAG, "Added dataItem" + dataItem.getName());
									dataItemsFound.add(dataItem);
								}
							}
						}
					}
				}				
			}
			
		}
		
		return dataItemsFound;
	}

	/** called when the device selection dialog is closed and a new sensormgt device was selected 
	 *	triggers reading of device model data, and registers for data update notifications 
	 */
	@Override
	public void onSelectionUpdated(DialogFragment dialog) {
		
		if (DEBUG) Log.d(TAG, " onSelectionUpdated ");
		
		UPnPDeviceInfo selectedDevice = mApplication.getCurrentDevice(); 
		if (selectedDevice!=null) {
			mCurrentSensormgtDevice = selectedDevice.getsensorMgtDevice();
			
			//new device, remove previously detected fridge
			mFridgeSensorCollection = null;
			mTempSensors = null;
			
			//subscribe to ConfigurationManagement gena events
			//new values will be read automatically which will trigger data available events
			mCurrentSensormgtDevice.subscribeConfigurationManagementServiceEvents();
					
			//get the data for all sensorCollections
			mCurrentSensormgtDevice.setSensorCollectionDataAvailableListener(this);
			mCurrentSensormgtDevice.updateSensorCollections();
			
		}
		
	}

	//TODO: separate listener for new sensor collections
	/**
	 * called when sensor collection and sensor data is available
	 * checks if the device is a fridge, searches for temperature sensors
	 */
	@Override
	public void onSensorCollectionDataAvailable(
			SensorCollection sensorCollection) {
		if (DEBUG) Log.e(TAG, "CollectionDataAvailable: "+sensorCollection.getType());
		
		if (sensorCollection.getType().startsWith(SCT_FRIDGE)) {
			Log.e(TAG,"FOUND ******************************************************");
			mFridgeSensorCollection = sensorCollection;
			
			setFridgeHeaders(mFridgeSensorCollection.getFriendlyName(), mFridgeSensorCollection.getInformation());
			
			//get temperature sensors
			mTempSensors = findDataItems("monitor","monitor", "units=\"degC\"");
			for (DataItem dataItem : mTempSensors) {
				
				if (!mTempSensorListAdapter.containsDataItem(dataItem)) {
					TemperatureSensorView sensor = new TemperatureSensorView(dataItem);
					mTemperatureSensorViews.add(sensor);
					sensor.setOnDataChangeListener(mTempSensorListAdapter);
				}
			}
			
			//TODO: make more dynamic
			//get related dataItems
			mConfigurationDataItems = findDataItems("monitor","monitor", "itemname=\"StatusInterval\"");
			
			//get energy sensors
			mEnergySensors = findDataItems("monitor","monitor", "units=\"kW-h\"");
			for (DataItem dataItem : mEnergySensors) {
				
				if (!mEnergySensorListAdapter.containsDataItem(dataItem)) {
					EnergySensorView sensor = new EnergySensorView(dataItem);
					mEnergySensorViews.add(sensor);
					sensor.setOnDataChangeListener(mTempSensorListAdapter);
				}
			}
			
			//get alarms
			mAlarms = findDataItems("monitor","monitor", "alarm");
			for (DataItem dataItem : mAlarms) {
				Log.e(TAG,"subscribe");
				dataItem.addSensorDataItemReadListener(this);
			}
			
		}
	}

	//TODO: react to datachange event instead of sensorread
	@Override
	public void onSensorDataItemRead(DataItem dataItem) {

		//TODO: make more dynamic
		if (dataItem.getName().equals("DoorOpenAlarm")) {

			if (dataItem.getValue().equals("1") && mPreviousAlarm.equals("0")) {
				
				runOnUiThread(new Runnable() {
					public void run() { 
						Toast toast = Toast.makeText(mActivity, "Safe virtual energy!\nClose the virtual door.", Toast.LENGTH_LONG);
						toast.show();
					}
				});
			} 
			
			mPreviousAlarm = dataItem.getValue();
		}

	}

	

}
