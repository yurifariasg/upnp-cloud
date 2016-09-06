package com.control.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.pubsub.PubSubManager;

import com.control.ws.connectivity.common.BaseSensorPooling.SensorPoolingObserver;
import com.control.ws.database.DBManager;
import com.control.ws.model.DeviceUpnp;
import com.control.ws.model.Sensor;
import com.control.ws.model.SourcedDeviceUpnp;
import com.control.ws.xmpp.XmppEventing;
import com.control.ws.xmpp.XmppSensorPooling;
import com.control.ws.xmpp.busevent.XmppServiceActionEvent;
import com.control.ws.xmpp.data.DeviceDescription;
import com.control.ws.xmpp.data.DeviceDescriptionFactory;
import com.control.ws.xmpp.data.DeviceDescriptionFetcher;
import com.control.ws.xmpp.data.SensorDeviceDescription;
import com.control.ws.xmpp.data.XmppDevicesStateObserver;
import com.control.ws.xmpp.listeners.CallbacksListener;
import com.control.ws.xmpp.listeners.PresenceListener;
import com.control.ws.xmpp.packet.SoapRequestIQ;
import com.control.ws.xmpp.providers.SOAPProvider;
import com.control.ws.xmpp.providers.UPNPQueryProvider;

import eu.geekplace.javapinning.JavaPinning;

public class UPnPManager implements XmppDevicesStateObserver, SensorPoolingObserver {
	
	// Profiling
	public static final ArrayList<Long> mConnectTimes = new ArrayList<>();
	public static final ArrayList<Long> mDetailsTimes = new ArrayList<>();
	public static final ArrayList<Long> mDisconnectTimes = new ArrayList<>();

	public static final ArrayList<Long> mReadSensorSend = new ArrayList<>();
	public static final ArrayList<Long> mReadSensorReceive = new ArrayList<>();
	
	
	
	public static final boolean DEBUG_VERBOSE = false;
	
	public static final boolean DEBUG_PROFILING = true;

	private static final String CERT = "CERTPLAIN:308203bc30820325a003020102020900e044efa7583ac76c300d06092a864886f70d010104050030819b310b3009060355040613024652310c300a06035504081303494446310e300c06035504071305506172697331173015060355040a130e546573742053657276657220503131143012060355040b130b50726f636573732d6f6e65311730150603550403130e4d69636b61656c2052656d6f6e643126302406092a864886f70d0109011617636f6e746163744070726f636573732d6f6e652e6e6574301e170d3036303330333136303634365a170d3136303232393136303634365a30819b310b3009060355040613024652310c300a06035504081303494446310e300c06035504071305506172697331173015060355040a130e546573742053657276657220503131143012060355040b130b50726f636573732d6f6e65311730150603550403130e4d69636b61656c2052656d6f6e643126302406092a864886f70d0109011617636f6e746163744070726f636573732d6f6e652e6e657430819f300d06092a864886f70d010101050003818d0030818902818100bcb19fd66f61aa60f2f8dce07304dcf48054f588364c2cb829318a8164494aba5ec618274b644960a95626f0a4713c889177904f6ee78d921c3f62185c4006b2e3e11fed8eb37635c0b181ad6217669acea270293547120c6b64ef27a75996b695d8a25d344c7184cb52ca3dcd055b38dbbe917194ae90dd80478fe9ce25b2b10203010001a382010430820100301d0603551d0e04160414c056f2596707b00086ef6efa2bc19dd0dd6e62d33081d00603551d230481c83081c58014c056f2596707b00086ef6efa2bc19dd0dd6e62d3a181a1a4819e30819b310b3009060355040613024652310c300a06035504081303494446310e300c06035504071305506172697331173015060355040a130e546573742053657276657220503131143012060355040b130b50726f636573732d6f6e65311730150603550403130e4d69636b61656c2052656d6f6e643126302406092a864886f70d0109011617636f6e746163744070726f636573732d6f6e652e6e6574820900e044efa7583ac76c300c0603551d13040530030101ff300d06092a864886f70d0101040500038181006b587952fb06592cb977fbf55fd67c13e8b0a0f8c95cececff46b204bf0f5d131cc2116e320e99ae08572f04b3f50127443880fa23560e253e8df1bff3d28120c97db709c968d26c0874786bc7b4d739e069d8571e68c74a3430147c32dd0285bc00e50312a3a6ea739b512baf81fa26f810b8b1ecc092ab7c1d87b1dcb8bf43";

	private static UPnPManager singleInstance;

	// private List<String> deviceList = new ArrayList<>();

	private Map<String, DeviceDescriptionFetcher> discoverers = new HashMap<String, DeviceDescriptionFetcher>();

	private AbstractXMPPConnection conn1;

	private DeviceDescriptionFactory mFactory;

	private Map<String, DeviceDescription> mDeviceDescriptions = new HashMap<String, DeviceDescription>();

	private Map<String, SourcedDeviceUpnp> mUpnpDeviceList = new HashMap<String, SourcedDeviceUpnp>();

	private XmppSensorPooling mSensorPooling;

	private CallbacksListener callbacksListnener;

	private XmppEventing eventing;

	public static UPnPManager getInstance() {
		if (singleInstance == null) {
			singleInstance = new UPnPManager();
		}
		return singleInstance;
	}

	private UPnPManager() {
		SmackConfiguration.DEBUG = false;
		ProviderManager.addIQProvider(UPNPQueryProvider.ELEMENT_NAME, UPNPQueryProvider.NAMESPACE,
				new UPNPQueryProvider());
		ProviderManager.addIQProvider(SOAPProvider.ELEMENT_NAME, SOAPProvider.NAMESPACE, new SOAPProvider());
		mFactory = new DeviceDescriptionFactory();
		mUpnpDeviceList = new HashMap<String, SourcedDeviceUpnp>();
		mSensorPooling = new XmppSensorPooling(this);
		mSensorPooling.setObserver(this);
		callbacksListnener = new CallbacksListener();
	}

	public Collection<DeviceDescription> getDevices() {
		return mDeviceDescriptions.values();
	}

	public void connect() {

		try {
			SSLContext sc = JavaPinning.forPin(CERT);

			// Create a connection to the jabber.org server.
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
					.setUsernameAndPassword("test", "test").setServiceName("brigadeiro.local")
					.setResource("WebAPI")
					// .setHost("192.168.130.57")
					.setHost("127.0.0.1").setPort(5222)
					 .setDebuggerEnabled(SmackConfiguration.DEBUG)
					 .setSecurityMode(SecurityMode.disabled)
//					.setCustomSSLContext(sc)
					.setHostnameVerifier(new HostnameVerifier() {

						@Override
						public boolean verify(String arg0, SSLSession arg1) {
							return true;
						}
					}).build();
			conn1 = new XMPPTCPConnection(config);
			conn1.connect();
			conn1.login();
			// conn1.registerIQRequestHandler(new IQListener());

			PresenceListener presenceListener = new PresenceListener(this);
			conn1.addSyncStanzaListener(presenceListener, presenceListener);
			conn1.addSyncStanzaListener(callbacksListnener, callbacksListnener);
			PubSubManager pubSubManager = new PubSubManager(conn1, "pubsub.brigadeiro.local");

			eventing = new XmppEventing(conn1, UPnPManager.this, "test/resource", "pubsub.brigadeiro.local");

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void onEvent(XmppServiceActionEvent event) {
		// System.out.println("onEvent XmppServiceActionEvent");
		SoapRequestIQ request = new SoapRequestIQ(event.getActionName(), event.getServiceName(), event.getArgs());
		request.setTo(event.getDevice().getDescription().getJid());
		if (event.getCallback() != null) {
			callbacksListnener.registerCallback(request.getStanzaId(), event.getCallback());
		}
		try {
			conn1.sendStanza(request);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		if (conn1 != null) {
			eventing.finish();
			conn1.disconnect();
			mSensorPooling.stop();
		}
		
//		System.out.println("Connect Times:");
//		System.out.println(toStr(mConnectTimes));
//		System.out.println("Details Times:");
//		System.out.println(toStr(mDetailsTimes));
//		System.out.println("Disconnect Times:");
//		System.out.println(toStr(mDisconnectTimes));

		System.out.println("Send Time:");
		System.out.println(toStr(mReadSensorSend));
		System.out.println("Receive Time:");
		System.out.println(toStr(mReadSensorReceive));
	}
	
	private String toStr(ArrayList<Long> longList) {
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0 ; i < longList.size(); i++) {
			strBuilder.append(longList.get(i));
			if (i < longList.size() - 1) {
				strBuilder.append(",");
			}
		}
		return strBuilder.toString();
	}

	@Override
	public void onDeviceDiscoveredFinished(DeviceDescription deviceDescription) {
		if (DEBUG_VERBOSE) {
			System.out.println("onDeviceDiscoveredFinished");
		}
		// TODO Auto-generated method stub
		discoverers.remove(deviceDescription.getJid());

	}

	@Override
	public void onDeviceFound(DeviceDescription deviceDescription) {

		// DBManager.getInstance().addDevice(deviceDescription);
		
		discoverers.remove(deviceDescription.getJid());

		mDeviceDescriptions.put(deviceDescription.getUuid(), deviceDescription);


		if (deviceDescription instanceof SensorDeviceDescription) {
			if (!mSensorPooling.isRunning()) {
				mSensorPooling.start();
			}
			SensorDeviceDescription sdd = (SensorDeviceDescription) deviceDescription;
			for (Sensor sensor : sdd.getSensors()) {
				mUpnpDeviceList.put(sensor.getExternalID(), sensor);
				DBManager.getInstance().addDevice(sensor);
				mSensorPooling.addSensor(sensor);
			}
			
//			printProfiling("All Sensors added at: " + System.currentTimeMillis());
			mDetailsTimes.add(System.currentTimeMillis());
		} else {
			if (DEBUG_VERBOSE) {
				System.out.println("Adding non-sensor device.");
			}
			mUpnpDeviceList.put(deviceDescription.getJid(), deviceDescription.getBoundDevice());
			if (DEBUG_VERBOSE) {
				System.out.println("DEVICE DESCRIPTION: " + deviceDescription);
				System.out.println("DEVICE DESCRIPTION toJSON: " + deviceDescription.toJSON());
				System.out.println("DEVICE DESCRIPTION getResource: " + deviceDescription.getResource());
				System.out.println("DEVICE DESCRIPTION getDeviceType: " + deviceDescription.getDeviceType());
				System.out.println("DEVICE DESCRIPTION needed? " + deviceDescription.isDeviceDescriptionNeeded());
			}
			DBManager.getInstance().addDevice(deviceDescription.getBoundDevice());
			eventing.onNewDeviceDiscovered(deviceDescription);
		}

	}

	@Override
	public void onNoDevicesFound() {
		printDebug("onNoDevicesFound");
		// TODO Auto-generated method stub

	}

	@Override
	public void onDevicePropertiesChanged(DeviceUpnp device, Map<String, Object> changes) {
		printDebug("onDevicePropertiesChanged");

		String key = null;

		if (device instanceof Sensor) {
			Sensor sensor = (Sensor) device;

			key = sensor.getExternalID();

			for (String dataItem : changes.keySet()) {
				DBManager.getInstance().updateValue(key, dataItem, String.valueOf(changes.get(dataItem)));
			}

			// DBManager.getInstance().updateSensor()

		}
	}

	@Override
	public void onNewDeviceConnected(String jidd, String hash, String status) {
		printDebug("onNewDeviceConnected");
		
//		printProfiling("Device Connected: " + System.currentTimeMillis());
		if (status != null) {
			mConnectTimes.add(System.currentTimeMillis());
		}

		if (conn1 != null && conn1.isConnected()) {
			DeviceDescription dd = mFactory.createDeviceDescription(jidd, hash);
			printDebug("DeviceDescription: " + dd);
			if (dd != null) {
				getDeviceDetail(dd, status);
			}
		}
	}

	public void getDeviceDetail(DeviceDescription deviceDescription, String status) {
		if (!discoverers.containsKey(deviceDescription.getJid())) {
			printDebug("Fetching Device: " + deviceDescription.getJid());
			DeviceDescriptionFetcher ddf = new DeviceDescriptionFetcher(conn1, deviceDescription);
			ddf.setDeviceStatus(status);
			discoverers.put(deviceDescription.getJid(), ddf);
			ddf.registerObserver(this);
			ddf.start();
		}
	}

	@Override
	public void onDeviceDisconnected(String jidd) {
		printDebug("onDeviceDisconnected");
		String type = mFactory.getTypeFromJid(jidd);
		String uuid = null;

		if ("SensorManagement".equalsIgnoreCase(type)) {
			List<String> keysToRemove = new ArrayList<String>();
			for (String key : mUpnpDeviceList.keySet()) {
				printDebug("key: " + key + " - starts wth:" + jidd);
				if (key.startsWith(jidd)) {
					Sensor sensor = (Sensor) mUpnpDeviceList.get(key);
					mSensorPooling.removeSensor(sensor);
					keysToRemove.add(key);
					uuid = sensor.getUuid();
				}
			}
			for (String key : keysToRemove) {
				mUpnpDeviceList.remove(key);
			}
		} else {
			if (mUpnpDeviceList.containsKey(jidd)) {
				uuid = mUpnpDeviceList.get(jidd).getUuid();
				mUpnpDeviceList.remove(jidd);
			}
		}
		DeviceDescription device = mDeviceDescriptions.remove(uuid);
		DBManager.getInstance().removeDevice(device);
		if (discoverers.containsKey(jidd)) {
			discoverers.get(jidd).stop();
		}
//		printProfiling("Disconnect at: " + System.currentTimeMillis());
		mDisconnectTimes.add(System.currentTimeMillis());
	}
	
	
	public static void printDebug(String str) {
		if (UPnPManager.DEBUG_VERBOSE) {
			System.out.println(str);
		}
	}
	
	public static void printProfiling(String str) {
		if (UPnPManager.DEBUG_PROFILING) {
			System.out.println(str);
		}
	}

}
