package com.comarch.android.upnp.ibcdemo;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.model.types.UnsignedIntegerOneByte;

import java.beans.PropertyChangeSupport;

@UpnpService(
        serviceId = @UpnpServiceId("Dimming"),
        serviceType = @UpnpServiceType(value = "Dimming", version = 1)
)
public class Dimming {

    private final PropertyChangeSupport propertyChangeSupport;

    public Dimming() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @UpnpStateVariable(defaultValue = "0", sendEvents = false)
    private UnsignedIntegerOneByte loadLevelTarget = new UnsignedIntegerOneByte(0);

    @UpnpStateVariable(defaultValue = "0", sendEvents = true)
    private UnsignedIntegerOneByte loadLevelStatus = new UnsignedIntegerOneByte(0);

    @UpnpAction
    public void setLoadLevelTarget(@UpnpInputArgument(name = "newLoadlevelTarget") UnsignedIntegerOneByte newLoadLevelTarget) {
        UnsignedIntegerOneByte targetOldValue = loadLevelTarget;
        loadLevelTarget = newLoadLevelTarget;
        UnsignedIntegerOneByte statusOldValue = loadLevelStatus;
        loadLevelStatus = newLoadLevelTarget;

        // These have no effect on the UPnP monitoring but it's JavaBean compliant
        getPropertyChangeSupport().firePropertyChange("loadLevelTarget", targetOldValue, loadLevelTarget);
        getPropertyChangeSupport().firePropertyChange("loadLevelStatus", statusOldValue, loadLevelStatus);

        // This will send a UPnP event, it's the name of a state variable that sends events
        getPropertyChangeSupport().firePropertyChange("LoadLevelStatus", statusOldValue, loadLevelStatus);
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "RetLoadlevelTarget"))
    public UnsignedIntegerOneByte getLoadLevelTarget() {
        return loadLevelTarget;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "RetLoadlevelStatus"))
    public UnsignedIntegerOneByte getLoadLevelStatus() {
        return loadLevelStatus;
    }
}