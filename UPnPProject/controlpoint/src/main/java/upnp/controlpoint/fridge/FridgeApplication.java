package upnp.controlpoint.fridge;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.UPnPInit;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.UPnPDevice;

public class FridgeApplication extends Application 
{
	private static final String SHAREDPREF_DEVICEUDN = "PreferredDeviceUDN";
	private static final String TAG = "FridgeApplication";
	private UPnPInit UPnP = new UPnPInit();
	private Activity mActivity;
	private UPnPDeviceInfo mCurrentDevice=null;
	private ArrayList<UPnPDeviceInfo> mDiscoveredDevices = new ArrayList<UPnPDeviceInfo>();

	public boolean InitializeUPnP() {
		return InitializeUPnP(getLocalIpAddress());
	}
	
	public boolean InitializeUPnP(String IPaddress) {

		UPnP = new UPnPInit();
		boolean initialized = UPnP.isInitialized();      
		if (!initialized)
		{

			Log.d("UPnPApplication : IP address", IPaddress);
			return UPnP.Init(getBaseContext(), IPaddress,0);	        
		}
		return initialized;	    
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public UPnPInit getUPnP() {
	    return UPnP;
	}
	
	public void stopUPnP() {
//FIXME:
//		UPnP.stopUPnP();
	}

	public  void setActivity(Activity activity) {
		this.mActivity = activity;
	}
	
	public ArrayList<UPnPDeviceInfo> getDiscoveredDevices() {

		if (getUPnP()!=null) {
			//get all discovered devices
			ArrayList<UPnPDevice> devices = getUPnP().getDevices();

			//refresh the list
			mDiscoveredDevices .clear();
			for (UPnPDevice device: devices) {
				mDiscoveredDevices.add(new UPnPDeviceInfo(device));
			}

		}
		return mDiscoveredDevices;
	}
	
	/**
	 * Retrieves the UPnP device that was previously selected by the user
	 */
	public UPnPDeviceInfo getPreferredDevice() {
		// Get preference list of devices we want to show in the UI
		SharedPreferences settings = mActivity.getPreferences(MODE_PRIVATE);
		String preferredDevice =  settings.getString(SHAREDPREF_DEVICEUDN, "");
		
		String split[] = preferredDevice.split("/");
		if (split.length==2) {
//			UPnPDeviceInfo upnpDevice = new UPnPDeviceInfo(split[0],split[1], false, true);
//			mSensorMgtDevice.add(upnpDevice);
//			upnpDevice.setSensorCollectionChangedListener(this); //just add always, when the device is not set to use, no updates will be sent
//			upnpDevice.setSensorCollectionDataAvailableListener(this);
//			
//			return upnpDevice;
		}
		
		return null;
	}
	
	public void storePreferredDevices(List<UPnPDeviceInfo> devices) {
		//write the device preferences
		SharedPreferences sharedPreferences = mActivity.getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		Set<String> preferredDevices = new HashSet<String>();
		for (UPnPDeviceInfo device: devices) {
			preferredDevices.add(device.getUDN()+"/"+device.getFriendlyName());
		}

		editor.putStringSet("UDN", preferredDevices);
		editor.commit();
	}
	
	public void setCurrentDevice(UPnPDeviceInfo device) {
		Log.d(TAG, "SetCurrentDevice "+device);
		mCurrentDevice = device;
	}
	
	public UPnPDeviceInfo getCurrentDevice() {
		return mCurrentDevice;
	}
	
}