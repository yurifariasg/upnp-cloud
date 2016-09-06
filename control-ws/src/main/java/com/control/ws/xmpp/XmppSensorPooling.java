package com.control.ws.xmpp;

import java.util.Map;

import com.control.ws.UPnPManager;
import com.control.ws.connectivity.busevent.IServiceActionEvent;
import com.control.ws.connectivity.common.BaseSensorPooling;
import com.control.ws.connectivity.common.UpnpActionCallback;
import com.control.ws.model.DeviceUpnp;
import com.control.ws.xmpp.busevent.XmppServiceActionEvent;

public class XmppSensorPooling extends BaseSensorPooling {

	public XmppSensorPooling(UPnPManager manager) {//EventBus eventBus) {
//		super(eventBus);
		super(manager);
	}

	@Override
	protected String getThreadName() {
		return getClass().getSimpleName();
	}

	@Override
	protected IServiceActionEvent createServiceActionEvent(DeviceUpnp device,String serviceName, String actionName, Map<String, String> args,UpnpActionCallback callback) {
		return new XmppServiceActionEvent(device, serviceName, actionName, args, callback);
	}


}
