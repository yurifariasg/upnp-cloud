package com.control.ws.model;

import org.json.simple.JSONObject;

import com.control.ws.model.interfaces.IDimmableLight;

public class DimmableLight extends SourcedDeviceUpnp implements IDimmableLight {

	public static final String DIMMING_SERVICE = "urn:schemas-upnp-org:service:Dimming:1";
	public static final String SWITCH_SERVICE = "urn:schemas-upnp-org:service:SwitchPower:1";
	public static final String SET_TARGET_ACTION = "SetTarget";
	public static final String NEW_LOAD_LEVEL_TARGET_ARG = "newLoadlevelTarget";
	public static final String SET_LOAD_LEVEL_TARGET_ACTION = "SetLoadLevelTarget";
	public static final String NEW_TARGET_VALUE_ARG = "NewTargetValue";
	
    private Double brightness;
    private Boolean switched;
    
    public DimmableLight(String uuid, String type, String name) {
        super(uuid, type, name);
        switched = false;
        brightness = 0.0;
    }
    public DimmableLight(DimmableLight dimmableLight){
        super(dimmableLight);
        switched = dimmableLight.isSwitched();
        brightness = dimmableLight.getBrightness();
    }
	public Double getBrightness() {
		return brightness;
	}
	public void setBrightness(Double brightness) {
		this.brightness = brightness;
	}
	public Boolean isSwitched() {
		return switched;
	}
	public void setSwitched(Boolean switched) {
		this.switched = switched;
	}
	
	@Override
	public final String getType() {
		return "oic.r.light.brightness";
	}
	
	@Override
	public JSONObject toJSON() {
		return super.toJSON();
	}
	
}