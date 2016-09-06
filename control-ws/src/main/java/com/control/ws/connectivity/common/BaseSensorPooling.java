package com.control.ws.connectivity.common;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import com.control.ws.UPnPManager;
import com.control.ws.connectivity.busevent.IServiceActionEvent;
import com.control.ws.model.DeviceUpnp;
import com.control.ws.model.Sensor;
import com.control.ws.xmpp.busevent.XmppServiceActionEvent;

import jersey.repackaged.com.google.common.collect.Lists;

public abstract class BaseSensorPooling {

	public interface SensorPoolingObserver {
		public void onDevicePropertiesChanged(DeviceUpnp device,
				Map<String, Object> changes);
	}

	private final String TAG = getClass().getSimpleName();

	private final int DELAY = 1000; // 1s
	private final int UPDATE_DELAY = 15000; // 15s
	private final int REQUEST_TIMEOUT = 40000; // 40s

	private SensorPoolingObserver observer;
	private final HashMap<String, Sensor> mKnownSensors;
	private final Map<Sensor, Long> mLastUpdateTime;
	private final HashMap<String, Long> mSentRequests;
	
//	private EventBus mEventBus;
	private UPnPManager mUpnpManager;
	private PoolingThread mPoolingThread;

	private DeviceUpnp mCurrentDevice;
	
	private Object monitor = new Object();
	private Long mLastPool = 0L;

	private class PoolingThread {
		private Thread mThread;
		private boolean mIsRunning;

		public void start() {
			mIsRunning = true;
			mThread.start();
		}

		public void stop() {
			mIsRunning = false;
		}

		public PoolingThread(String threadName) {
			mThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (mIsRunning) {
						try {
							Long now = System.currentTimeMillis();
							if (!mKnownSensors.isEmpty() && now - mLastPool > DELAY) {
								updateSensors(now);
								mLastPool = now;
							} else {
								Thread.sleep(100);
							}
						} catch (InterruptedException e) {
						}
					}
				}
			}, threadName);
		}

	}
	
	public boolean isRunning() {
		return mPoolingThread != null && mPoolingThread.mIsRunning;
	}

	public BaseSensorPooling(UPnPManager manager) {
		mUpnpManager = manager;
		mKnownSensors = new HashMap<String, Sensor>();
		mLastUpdateTime = new HashMap<Sensor, Long>();
		mSentRequests = new HashMap<String, Long>();
	}

	protected abstract String getThreadName();

	protected void updateSensors(Long now) {
		synchronized (monitor) {
			for (Sensor sensor : mKnownSensors.values()) {
				boolean isCurrent = mCurrentDevice!=null && mCurrentDevice.getKey().equals(sensor.getKey());
				Long lastTime = mLastUpdateTime.get(sensor);
				if (isCurrent || lastTime == null || now - lastTime > UPDATE_DELAY) {
					mLastUpdateTime.put(sensor, now);
					updateSensor(sensor);
				}
			}
		}
	}

	private void updateSensor(Sensor sensor) {
		updateGenericSensor(sensor);
//		if (sensor instanceof SensorTemperature) {
//			updateTemperatureSensor((SensorTemperature) sensor);
//		} else if (sensor instanceof SensorLight) {
//			updateLightSensor((SensorLight) sensor);
//		} else if (sensor instanceof SensorFridge) {
//			updateFridgeSensor((SensorFridge) sensor);
//			updateGenericSensor(sensor);
//		}
	}
	
	private void updateGenericSensor(final Sensor sensor) {
		Collection<String> urns = sensor.getSensorURNs();
		for (String sensorUrn : urns) {
			Map<String, String> args = sensor.prepareReadMap(sensorUrn,
					sensor.getDataItems(sensorUrn));
			UPnPManager.mReadSensorSend.add(System.currentTimeMillis());
			mUpnpManager.onEvent((XmppServiceActionEvent) createServiceActionEvent(sensor,
					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor, sensorUrn) {
						@Override
						public void updateSensor(Map<String, Object> dataRecords) {
							UPnPManager.mReadSensorReceive.add(System.currentTimeMillis());
							sensor.changeProperty(dataRecords);
							if (observer != null) {
								observer.onDevicePropertiesChanged(sensor,
										dataRecords);
							}
						}
					}));
		}
	}

//	private void updateFridgeSensor(final SensorFridge sensor) {
//		long now = System.currentTimeMillis();
//		
//		String sentRequestKey = getSentRequestKey(sensor, SensorFridge.TEMPERATURE_SENSOR_URN);
//		boolean sendRequestNow = canSendRequest(sentRequestKey);
//		if (sendRequestNow) {
//			mSentRequests.put(sentRequestKey, now);
//			String sensorUrn = sensor
//					.getSensorURNWhichBegin(SensorFridge.TEMPERATURE_SENSOR_URN);
//			Map<String, String> args = sensor.prepareReadMap(sensorUrn,
//					Lists.newArrayList(SensorFridge.TEMPERATURE_SENSOR_NAME));
//			
//			System.out.println("Args:" + args.toString());
//			mUpnpManager.onEvent((XmppServiceActionEvent) createServiceActionEvent(sensor,
//					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
//					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor,SensorFridge.TEMPERATURE_SENSOR_URN) {
//						@Override
//						public void updateSensor(Map<String, Object> dataRecords) {
//							if (dataRecords.containsKey(SensorFridge.TEMPERATURE_SENSOR_NAME)) {
//								double temp = Double.parseDouble((String) dataRecords
//										.get(SensorFridge.TEMPERATURE_SENSOR_NAME));
////								System.out.println("Temp1: " + temp);
//								if (sensor.getThemperature() != temp) {
//									sensor.setThemperature(temp);
////									Log.i("Fridge", "New Temperature: " + temp);
//									if (observer != null) {
//										observer.onDevicePropertiesChanged(sensor,
//												dataRecords);
//									}
//								}
//							}
//						}
//					}));
//		}
//	}

	protected abstract IServiceActionEvent createServiceActionEvent(
			DeviceUpnp device, String serviceName, String actionName,
			Map<String, String> args, UpnpActionCallback callback);

	private boolean canSendRequest(String sentRequestKey) {
		boolean result = true;
		long now = System.currentTimeMillis();
		
		if (mSentRequests.containsKey(sentRequestKey)) {
			long lastRequestTime = mSentRequests.get(sentRequestKey);
			if (now - lastRequestTime > REQUEST_TIMEOUT) {
				mSentRequests.remove(sentRequestKey);
			} else {
				result = false;
			}
		}
		
		return result;
	}
	
//	private void updateLightSensor(final SensorLight sensor) {
//		long now = System.currentTimeMillis();
//		
//		String sentRequestKey = getSentRequestKey(sensor, SensorLight.BRIGHTNESS_SENSOR_URN);
//		boolean sendRequestNow = canSendRequest(sentRequestKey);
//		if (sendRequestNow) {
//			mSentRequests.put(sentRequestKey, now);
//			String sensorUrn = sensor.getSensorURNWhichBegin(SensorLight.BRIGHTNESS_SENSOR_URN);
//			Map<String, String> args = sensor.prepareReadMap(sensorUrn,Lists.newArrayList(SensorLight.BRIGHTNES_SENSOR_ARG_NAME));
//			mUpnpManager.onEvent((XmppServiceActionEvent) createServiceActionEvent(sensor,
//					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
//					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor,SensorLight.BRIGHTNESS_SENSOR_URN) {
//						@Override
//						public void updateSensor(Map<String, Object> dataRecords) {
//							if (dataRecords
//									.containsKey(SensorLight.BRIGHTNES_SENSOR_ARG_NAME)) {
//								int temp = (Integer) dataRecords
//										.get(SensorLight.BRIGHTNES_SENSOR_ARG_NAME);
//								double newB = temp / 100.0;
//								if (!sensor.getBrightness().equals(temp)) {
//									sensor.setBrightness(newB);
//									if (observer != null) {
//										observer.onDevicePropertiesChanged(sensor,
//												dataRecords);
//									}
//								}
//							}
//						}
//					}));
//		}
//		
//		sentRequestKey = getSentRequestKey(sensor, SensorLight.SWITCH_SENSOR_URN);
//		sendRequestNow = canSendRequest(sentRequestKey);
//		if (sendRequestNow) {
//			mSentRequests.put(sentRequestKey, now);
//			String sensorUrn = sensor.getSensorURNWhichBegin(SensorLight.SWITCH_SENSOR_URN);
//			Map<String, String> args = sensor.prepareReadMap(sensorUrn,Lists.newArrayList(SensorLight.SWITCH_SENSOR_ARG_NAME));
//			mUpnpManager.onEvent((XmppServiceActionEvent) createServiceActionEvent(sensor,
//					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
//					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor,SensorLight.SWITCH_SENSOR_URN) {
//						@Override
//						public void updateSensor(Map<String, Object> dataRecords) {
//							if (dataRecords
//									.containsKey(SensorLight.SWITCH_SENSOR_ARG_NAME)) {
//								boolean temp = (Boolean) dataRecords
//										.get(SensorLight.SWITCH_SENSOR_ARG_NAME);
//								if (!sensor.isSwitched().equals(temp)) {
//									sensor.setSwitched(temp);
//									if (observer != null) {
//										observer.onDevicePropertiesChanged(sensor,
//												dataRecords);
//									}
//								}
//							}
//						}
//					}));
//		}
//	}
//
//	private void updateTemperatureSensor(final SensorTemperature sensor) {
//		long now = System.currentTimeMillis();
//		String sentRequestKey = getSentRequestKey(sensor, SensorTemperature.TEMPERATURE_SENSOR_URN);
//		boolean sendRequestNow = canSendRequest(sentRequestKey);
//		if (sendRequestNow) {
//			mSentRequests.put(sentRequestKey, now);
//			String sensorUrn = sensor
//					.getSensorURNWhichBegin(SensorTemperature.TEMPERATURE_SENSOR_URN);
//			Map<String, String> args = sensor.prepareReadMap(sensorUrn,
//					Lists.newArrayList("Temperature Sensor"));
//			mUpnpManager.onEvent((XmppServiceActionEvent) createServiceActionEvent(sensor,
//					Sensor.SENSOR_TRANSPORT_GENERIC_SERVICE,
//					Sensor.READ_SENSOR_ACTION, args, new ReadSensorCallback(sensor,SensorTemperature.TEMPERATURE_SENSOR_URN) {
//						@Override
//						public void updateSensor(Map<String, Object> dataRecords) {
//							if (dataRecords.containsKey("Temperature Sensor")) {
//								int temp = (Integer) dataRecords
//										.get("Temperature Sensor");
//								if (sensor.getThemperature() != temp) {
////									System.out.println("Temp2: " + temp);
//									sensor.setThemperature(temp);
//									if (observer != null) {
//										observer.onDevicePropertiesChanged(sensor,
//												dataRecords);
//									}
//								}
//							}
//						}
//					}));
//		}
//	}

	public void start() {
		mKnownSensors.clear();
		mLastUpdateTime.clear();
		if (mPoolingThread != null) {
			mPoolingThread.stop();
		}
		mPoolingThread = new PoolingThread(getThreadName());
		mPoolingThread.start();
		
		
		// Do not uncomment
//		mEventBus.registerSticky(this);

	}

	public void stop() {
		if (mPoolingThread != null) {
			mPoolingThread.stop();
		}
		mPoolingThread = null;
//		mEventBus.unregister(this);
	}

//	public void onEvent(UICurrentDevice event){
//		mCurrentDevice = event.getDevice();
//	}
	
	public void setObserver(SensorPoolingObserver observer) {
		this.observer = observer;
	}

	public void addSensor(Sensor sensor) {
		synchronized (monitor) {
			mKnownSensors.put(sensor.getKey(), sensor);
		}
	}

	public void removeSensor(Sensor sensor) {
		synchronized (monitor) {
			mKnownSensors.remove(sensor.getKey());
		}
	}
	private String getSentRequestKey(Sensor sensor,String sensorUrn){
		return sensor.getKey()+"/"+sensorUrn;
	}
	abstract class ReadSensorCallback extends UpnpActionCallback {
		private Sensor mSensor;
		private String mSensorUrn;
		public ReadSensorCallback(Sensor sensor, String sensorUrn) {
			mSensor = sensor;
			mSensorUrn = sensorUrn;
		}

		public abstract void updateSensor(Map<String, Object> dataRecords);

		@Override
		public void run() {
			Map<String, Object> response = getResponse();
			String dataRecords = (String) response.get("DataRecords");
			mSentRequests.remove(getSentRequestKey(mSensor,mSensorUrn));
			if (dataRecords != null) {
				Map<String, Object> records;
				try {
					records = Sensor.parseDataRecords(dataRecords);
					updateSensor(records);
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}