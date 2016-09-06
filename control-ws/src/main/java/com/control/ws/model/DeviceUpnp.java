package com.control.ws.model;

import com.control.ws.model.interfaces.IDeviceUpnp;
import com.control.ws.xmpp.data.DeviceDescription;

public abstract class DeviceUpnp implements IDeviceUpnp {

    private DeviceDescription description;
    private final String uuid;
    private final String type;
    private String configIdCloud = null;

    private String name;

    protected DeviceUpnp(String uuid, String type) {
        this.uuid = uuid;
        this.type = type;
    }

    protected DeviceUpnp(DeviceUpnp device) {
        this.uuid = device.getUuid();
        this.type = device.getType();
        this.description = device.getDescription();
        this.configIdCloud = device.getConfigIdCloud();
        setName(device.getName());
    }

    protected DeviceUpnp(String uuid, String type, String name){
        this.uuid = uuid;
        this.type = type;
        this.name = name;
    }
    
    public String getType() {
		return type;
	}
    
    public String getKey(){
    	return getUuid();
    }
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getExternalID() {
    	return getDescription().getJid();
    }

    @Override
    public int compareTo(IDeviceUpnp another) {
        if (uuid == null && another.getUuid() == null) {
            return 0;
        } else if (another.getUuid() == null) {
            return -1;
        }
        return uuid.compareTo(another.getUuid());
    }

    @Override
    public int hashCode() {
        return uuid == null ? 0 : uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceUpnp other = (DeviceUpnp) obj;
        if (uuid == null) {
            return false;
        } else if (other.uuid == null) {
            return false;
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DeviceUpnp [uuid=" + uuid + ", name=" + name + "]";
    }

    public DeviceDescription getDescription() {
        return description;
    }

    public void setDescription(DeviceDescription description) {
        this.description = description;
    }

    public String getConfigIdCloud() {
        return configIdCloud;
    }

    public void setConfigIdCloud(String configIdCloud) {
        this.configIdCloud = configIdCloud;
    }

}
