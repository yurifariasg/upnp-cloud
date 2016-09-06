package com.control.ws.xmpp;

import org.jivesoftware.smack.iqrequest.IQRequestHandler;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;

public class IQListener implements IQRequestHandler {
	
	private IQ mRequest;

	@Override
	public IQ handleIQRequest(IQ iqRequest) {
		System.out.println("handleIQRequest");
		System.out.println(iqRequest.toString());
		mRequest = iqRequest;
		return iqRequest;
	}

	@Override
	public Mode getMode() {
		System.out.println("getMode");
		return null;
	}

	@Override
	public Type getType() {
		System.out.println("getType");
		return Type.get; //mRequest == null ? null : mRequest.getType();
	}

	@Override
	public String getElement() {
		System.out.println("getElement");
		return mRequest == null ? null : mRequest.getChildElementName();
	}

	@Override
	public String getNamespace() {
		System.out.println("getNamespace");
		return mRequest == null ? null : mRequest.getChildElementNamespace();
	}
	
		

}
