package com.control.ws.model.interfaces;

public interface IDimmableLight extends ISourcedDeviceUpnp {

	public Double getBrightness();
	public void setBrightness(Double brightness);
	public Boolean isSwitched();
	public void setSwitched(Boolean switched);
}
