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

package com.tpvision.sensormgt.devicelib;



import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;

import com.tpvision.sensormgt.DeviceChangedListener;
import com.tpvision.sensormgt.datamodel.ConfigurationUpdateListener;
import com.tpvision.sensormgt.datamodel.DatamodelInterfaceImpl;
import com.tpvision.sensormgt.datastore.DataStoreInterfaceImpl;
import com.tpvision.sensormgt.datastore.DataStoreWebServer;
import com.tpvision.sensormgt.datastore.NetworkInfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import upnp.devicelib.R;

/**
 *
 * Sets up the UPnP stack; Uses the cling library to setup an Android service.
 *
 *
 */

public class ClingUPnPInit implements ServiceConnection,ConfigurationUpdateListener, GenaSubscriptionListener {

	private static final String TAG = "ClingUPnPInit";

	private AndroidUpnpService mUPnPService;

	private Context mContext;

	private DatamodelInterfaceImpl mDatamodelInterface;
	private DataStoreInterfaceImpl mDataStoreInterface;

	private Icon mIcon;

	private UDN mUdn;

	private DataStoreWebServer mServer;

	//	Properties that can be set before start is called.
	public String deviceFriendlyName = "Sensor Management Device";
	public String deviceManufacturer = "Gerhard";
	//	public String deviceManufacturerURI = "";
	public String deviceModelName = "Sensor Management Device.";
	public String deviceModelDescription = "v1";
	//	public String deviceModelNumber;
    //	private URI deviceModelURI;

	private DeviceChangedListener mListener;

	public ClingUPnPInit(Context context, UDN udn, DatamodelInterfaceImpl datamodelInterface) {
		this(context, udn, datamodelInterface, null, null);
	}

	public ClingUPnPInit(Context context, UDN udn, DatamodelInterfaceImpl datamodelInterface,
						 DataStoreInterfaceImpl datastoreInterface, DeviceChangedListener listener) {

		Log.i(TAG,"Initialising ClingUPnP Service - Modified");

		mContext = context;
		mDatamodelInterface = datamodelInterface;
		mDataStoreInterface = datastoreInterface;
		mUdn = udn;
		mListener = listener;

		mDatamodelInterface.setConfigurationUpdateListener(this);

		setDeviceIcon(R.drawable.sensor_icon);

		start();
	}

	public void start() {
		Log.i(TAG,"ClingUPnPInit start()");

		//Start the Android Service that implements the Cling UPnP stack and bind to it
		//Service_UPnPBrowser class checks which android service we are interested in
		Intent intent = new Intent(); //new Intent(context, Service_UPnPBrowser.class)
		intent.setComponent(new ComponentName(mContext.getPackageName(),"com.tpvision.sensormgt.devicelib.SensorMgtService"));
		mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);

		if (null != mDataStoreInterface) {
			mServer = new DataStoreWebServer(NetworkInfo.getPort(), mDataStoreInterface);
			 try {
					mServer.start();
				} catch (IOException e) {
					// TODO: Disable datastore
					e.printStackTrace();
				}
		}
	}

//TODO: check what else needs to be done in stop
	public void stop() {

		Log.i(TAG,"Stopping ClingUPnP Service");

		//stop the nanoHTTPd webserver used for transport connections
		if (null != mServer){
			mServer.stop();
			mServer = null;
		}
		// Stop monitoring configuration management events
		//LocalService<ConfigurationManagement> configurationManagementService = getConfigurationManagementService();
		//if (configurationManagementService != null)
		//	configurationManagementService.getManager().getImplementation().getPropertyChangeSupport()
		//	.removePropertyChangeListener(this);

		//LocalService<SensorTransportGeneric> sensorTransportService = getSensorTransportGenericService();
		// if (sensorTransportService!=null)
		// sensorTransportService.getManager().getImplementation().getPropertyChangeSupport()
		// .removePropertyChangeListener(this);

		mContext.unbindService(this);
	}

	public void setDeviceIcon(int resId) {
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
		ByteArrayOutputStream stream=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
		byte[] iconData =stream.toByteArray();
		mIcon = new Icon("image/png", 48, 48, 8, "icon.png", iconData);
	}


	public void onServiceConnected(ComponentName className, IBinder service) {
		Log.i(TAG,"ClingUPnPInit onServiceConnected");
		mUPnPService = (AndroidUpnpService) service;

		LocalService<ConfigurationManagement> configurationManagementService = getConfigurationManagementService();

		// Register the device when this activity binds to the service for
		// the first time
		if (configurationManagementService == null) {
			try {

				LocalDevice sensorMgtDevice = createDevice();

			//	Toast.makeText(FridgeDeviceMainActivity.this, R.string.registeringDevice, Toast.LENGTH_SHORT).show();
				mUPnPService.getRegistry().addDevice(sensorMgtDevice);
				if (mListener != null) {
					mListener.onDeviceChanged(sensorMgtDevice);
				}

				configurationManagementService = getConfigurationManagementService();
				configurationManagementService.getManager().getImplementation().setDatamodel(mDatamodelInterface);
				LocalService<SensorTransportGeneric> sensorTransportGenericService = getSensorTransportGenericService();
				sensorTransportGenericService.getManager().getImplementation().setDatamodel(mDatamodelInterface);

				if (null != mDataStoreInterface) {
					LocalService<DataStore> dataStoreService = getDataStoreService();
					dataStoreService.getManager().getImplementation().setDataStore(mDataStoreInterface);
				}

				Registry r = mUPnPService.getRegistry();
				((MyRegistryImpl) r).addGenaSubscriptionListener(this);


			} catch (Exception ex) {
				Log.d(TAG, "Creating Sensor Management device failed: " + ex);
				//Toast.makeText(FridgeDeviceMainActivity.this, R.string.createDeviceFailed, Toast.LENGTH_SHORT).show();
				return;
			}
		}

	}

	public void onServiceDisconnected(ComponentName className) {
//		if (mUPnPService != null)
//			mUPnPService.getRegistry().removeListener(mRegistrationListener);

		mUPnPService = null;
	}

	//TODO: check if we should directly send it to the ConfigurationManagementService
	/**
	 * callback from EventControl to indicate change in configuration variable
	 * Updates the ConfigurationUpdate statevariable, and sends the UPnP Event
	 * Additionally triggers, flashing of led in UI
	 *
	 */
	@Override
	public void onConfigurationUpdate(String configurationUpdate) {

		// set the state variable
		LocalService<ConfigurationManagement> configurationManagementService = getConfigurationManagementService();
		if (configurationManagementService != null) {
			// if (DEBUG) Log.d(TAG, "onConfigurationUpdate: "+
			// configurationUpdate);
			configurationManagementService.getManager().getImplementation().sendConfigurationUpdate();
		}

	//	sendUPnPEvent("ConfigurationUpdate", configurationUpdate);
		//mLed.blink();
		//runOnUiThread(setTextRunnable);
	}

	public void sendUPnPEvent(String stateVariableName, String eventValue) {
		LocalService<ConfigurationManagement> configurationManagementService = getConfigurationManagementService();
		if (configurationManagementService != null) {

			PropertyChangeSupport propertyChangeSupport = configurationManagementService.getManager().getImplementation().getPropertyChangeSupport();
			propertyChangeSupport.firePropertyChange(stateVariableName, false, true);
		}
	}

	public AndroidUpnpService getUPnPService() {
		return mUPnPService;
	}

	private <T> void addLocalService(List<LocalService> list, Class<T> clazz ) {
		LocalService<T> service  = new AnnotationLocalServiceBinder().read(clazz);
  		service.setManager(new DefaultServiceManager<T>(service, clazz));
  		list.add(service);
     }

     protected LocalDevice createDevice() throws ValidationException, LocalServiceBindingException {

    	 DeviceType type = new UDADeviceType("SensorManagement", 1);
    	 DeviceDetails details =
                 new DeviceDetails(
                         deviceFriendlyName,
                         new ManufacturerDetails(deviceManufacturer),
                         new ModelDetails(deviceModelName, deviceModelDescription)
                 );

        List<LocalService> list = new ArrayList<LocalService>();
 		addLocalService(list, ConfigurationManagement.class);
 		addLocalService(list, SensorTransportGeneric.class);
 		if (null!=mDataStoreInterface) {
 			addLocalService(list, DataStore.class);
 		}

 		LocalService[] services = new LocalService[list.size()];
 		list.toArray(services);

 		return new LocalDevice(new DeviceIdentity(mUdn), type, details, mIcon, services );
 	}


    private LocalService getLocalService( ServiceType serviceType) {
		if (mUPnPService == null)
			return null;

		LocalDevice dev = mUPnPService.getRegistry().getLocalDevice(mUdn, true);
		if (dev == null)
			return null;

		return dev.findService(serviceType);
    }

	protected LocalService<ConfigurationManagement> getConfigurationManagementService() {
		return (LocalService<ConfigurationManagement>) getLocalService(new UDAServiceType("ConfigurationManagement", 2));
	}

	protected LocalService<SensorTransportGeneric> getSensorTransportGenericService() {
		return (LocalService<SensorTransportGeneric>) getLocalService(new UDAServiceType("SensorTransportGeneric", 1));
	}

	protected LocalService<DataStore> getDataStoreService() {
		return (LocalService<DataStore>) getLocalService(new UDAServiceType("DataStore", 1));
	}


	@Override
	public void localGenaSubscriptionAdded(LocalGENASubscription subscription) {

		Log.w("SUBSCRIPTION", "+++ADDED+++ "+subscription.getCallbackURLs().toString());
		Log.w("SUBSCRIPTION", "duration: "+subscription.getRequestedDurationSeconds());
		Log.w("SUBSCRIPTION", "subscription id: "+subscription.getSubscriptionId());

	}


	@Override
	public void localGenaSubscriptionRemoved(LocalGENASubscription subscription) {
		// TODO Auto-generated method stub
		Log.w("SUBSCRIPTION", "+++REMOVED+++ "+subscription.getCallbackURLs().toString());
	}


	@Override
	public void localGenaSubscriptionUpdated(LocalGENASubscription subscription) {
		// TODO Auto-generated method stub
		Log.w("SUBSCRIPTION", "+++UPDATED+++ "+subscription.getCallbackURLs().toString());
	}


//	@Override
//	public void propertyChange(PropertyChangeEvent event) {
//		// TODO Auto-generated method stub
//
//	}


}
