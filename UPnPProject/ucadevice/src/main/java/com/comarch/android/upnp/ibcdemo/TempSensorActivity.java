package com.comarch.android.upnp.ibcdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.comarch.android.upnp.ibcdemo.busevent.ConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.DeviceListRefreshRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.LocalConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionCloseRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionOpenRequestEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.connection.XmppConnectionStateChangedEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.UpdateDeviceListEvent;
import com.comarch.android.upnp.ibcdemo.busevent.connector.data.UpdateDeviceProperty;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppChatMessageRecivedEvent;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.busevent.XmppExceptionEvent;
import com.comarch.android.upnp.ibcdemo.deliverer.ActivityWithBusDeliverer;
import com.comarch.android.upnp.ibcdemo.sensormanagement.DeviceParser;
import com.comarch.android.upnp.ibcdemo.sensormanagement.TemperatureSensor;
import com.comarch.android.upnp.ibcdemo.ui.newview.busevents.NotifyDeviceListChangedEvent;
import com.tpvision.sensormgt.DeviceChangedListener;
import com.tpvision.sensormgt.datamodel.DataItem;
import com.tpvision.sensormgt.datamodel.Datamodel;
import com.tpvision.sensormgt.datamodel.DatamodelInterfaceImpl;
import com.tpvision.sensormgt.datamodel.DatamodelNode;
import com.tpvision.sensormgt.datamodel.SensorChangeListener;
import com.tpvision.sensormgt.datamodel.SensorReadListener;
import com.tpvision.sensormgt.datamodel.SensorWriteListener;
import com.tpvision.sensormgt.datamodel.SingleInstanceNode;
import com.tpvision.sensormgt.datastore.DataStoreInterfaceImpl;
import com.tpvision.sensormgt.devicelib.ClingUPnPInit;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TempSensorActivity extends ActivityWithBusDeliverer implements SensorReadListener,
        SensorWriteListener, DeviceChangedListener, SensorChangeListener {

    private UDN udn = new UDN(UUID.randomUUID());

    @BindView(R.id.status_tv)
    TextView mStatusTv;

    @BindView(R.id.sensor_value_tv)
    TextView mSensorValueTv;

    private static boolean shouldRepeat = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        ButterKnife.bind(this);

        // Stuff from SensorManagement Device
        mDataStoreInterfaceImpl = new DataStoreInterfaceImpl(getContentResolver());
        setupSensorNetwork();


        if (shouldRepeat) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setResult(1);
                    finish();
                }
            }, 20000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
        getBus().unregister(this);
        Log.i("startXMPP", "DisconnectTime: " + System.currentTimeMillis());
    }

    private void disconnect() {
        stopService(new Intent(this, AndroidUpnpServiceImpl.class));
        UtilClass.upnpService.get().shutdown();
        getBus().post(new XmppConnectionCloseRequestEvent());
        UtilClass.mDisconnectXMPPTimes.add(System.currentTimeMillis());
    }



    @Override
    protected void onResume() {
        super.onResume();
        getBus().registerSticky(this);
        getBus().postSticky(new DeviceListRefreshRequestEvent());
    }

    private void startXMPP() {
        final String deviceName = UtilClass.MainDevice.getDetails().getFriendlyName();

//        Log.i("startXMPP", "StartTime: " + System.currentTimeMillis());
        UtilClass.mStartXMPPTimes.add(System.currentTimeMillis());

        getBus().postSticky(
                new XmppConnectionOpenRequestEvent(
                        "test@brigadeiro.local",
                        "test",
//                        "192.168.31.132",
//                        "10.90.90.253",
//                        "10.100.100.124",
                        "10.90.90.253",
                        5222,
                        "pubsub.brigadeiro.local",
                        deviceName
                ));
    }

    public void onEventMainThread(XmppExceptionEvent ev) {
        Toast.makeText(this, ev.getMessage(), Toast.LENGTH_LONG).show();
        Log.i("MainActivity", "onEvent: XmppExceptionEvent");
    }
    public void onEventMainThread(UpdateDeviceListEvent ev) {
        Log.i("MainActivity", "onEvent: UpdateDeviceListEvent");
    }

    public void onEventMainThread(UpdateDeviceProperty ev){
        Log.i("MainActivity", "onEvent: UpdateDeviceProperty");
    }

    public void onEventMainThread(XmppChatMessageRecivedEvent event){
        Log.i("MainActivity", "onEvent: XmppChatMessageRecivedEvent");
    }


    public void onEventMainThread(LocalConnectionStateChangedEvent ev) {
        Log.i("MainActivity", "onEvent: LocalConnectionStateChangedEvent");
    }

    public void onEventMainThread(XmppConnectionStateChangedEvent ev) {
        Log.i("MainActivity", "onEvent: XmppConnectionStateChangedEvent");
        if (ev.getState() == ConnectionStateChangedEvent.ConnectionState.CONNECTED) {
            mStatusTv.setText("CONNECTED");
        } else {
            mStatusTv.setText("Problem: " + ev.getState().name());
        }
    }

    public void onEventMainThread(NotifyDeviceListChangedEvent event) {
        Log.i("MainActivity", "onEvent: NotifyDeviceListChangedEvent");
    }

    // SensorManagement Stuff
    private TemperatureSensor mFreezerTemp;
    private DatamodelInterfaceImpl mDatamodelInterface;
    private DataStoreInterfaceImpl mDataStoreInterfaceImpl;
    private ClingUPnPInit mCling;

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

        List<DataItem> dataItemList = new ArrayList<>();

        String urn = "urn:upnp-org:smgt-surn:refrigerator:AcmeSensorsCorp-com:AcmeIntegratedController:FrigidaireCorp:rf217acrs:monitor";

        // three sensors
        mFreezerTemp = new TemperatureSensor("SensorCollection0001", "Sensor0001", urn,
                "temperature");
        mFreezerTemp.setAvgTemp(-21);
        mFreezerTemp.setSensorValue("-21");
        mFreezerTemp.setUpdatePeriod(30);
        mFreezerTemp.addOnSensorChangeListener(this);

        DataItem mClientID = new DataItem("SensorCollection0001", "Sensor0001", urn, "ClientID");

        dataItemList.add(mFreezerTemp);

        dataItemList.add(mClientID);

        mDatamodelInterface = new DatamodelInterfaceImpl(Datamodel.inflateDatamodelTree(Datamodel.mDatamodelDefinition), instanceTree, dataItemList);

        //inform me on any sensor access
        for (DataItem sensor : dataItemList) {
            sensor.addOnSensorWriteListener(this);
            sensor.addOnSensorReadListener(this);
        }
    }

    @Override
    public void onSensorRead(String mCollectionID, String mSensorID, DataItem dataItem) {

    }

    @Override
    public void onSensorWrite(String collectionID, String sensorID, DataItem dataItem) {

    }

    @Override
    public void onSensorChange(String collectionID, String sensorID,
                               boolean valueRead, boolean valueTransported,
                               final DataItem dataItem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSensorValueTv.setText(dataItem.getName() + ": " + dataItem.getSensorValue());
            }
        });
    }

    //starting the android service when the activity becomes visible
    @Override
    protected void onStart() {
        super.onStart();
        mCling = new ClingUPnPInit(getApplicationContext(), udn, mDatamodelInterface,
                mDataStoreInterfaceImpl, this);
    }

    @Override
    public void onDeviceChanged(LocalDevice localDevice, AndroidUpnpService service) {
        UtilClass.MainDevice = localDevice;
        UtilClass.upnpService = service;
        if (localDevice != null) {
            startXMPP();
        }
    }

    //stopping the upnp service when the activity is invisible
    @Override
    protected void onStop() {
        mCling.stop();
        super.onStop();
    }
}