package com.control.ws.model.interfaces;

import org.json.simple.JSONObject;

public interface IDeviceUpnp extends Comparable<IDeviceUpnp>{

    public String getKey();
    public String getUuid();
    public String getName();
    public JSONObject toJSON();
    
}