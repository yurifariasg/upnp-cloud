package com.tpvision.sensormgt;


import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;

public interface DeviceChangedListener {

    void onDeviceChanged(LocalDevice localDevice, AndroidUpnpService upnpService);

}
