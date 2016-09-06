package com.control.ws.model.interfaces;

import java.util.EnumSet;

import com.control.ws.model.Source;

public interface ISourcedDeviceUpnp extends IDeviceUpnp,Cloneable {
	public EnumSet<Source> getSources();

}