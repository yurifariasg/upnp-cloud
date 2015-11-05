package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;

public class DiscoItemsIQ extends IQ {

	@SuppressWarnings("unused")
	private final String TAG = getClass().getSimpleName();

	private final String rawBody;

	public DiscoItemsIQ() {
		super();
		// Log.v(TAG, "Create SoapRequestIQ");
		this.setType(Type.GET);
		rawBody = createBody();
	}

	private static final String createBody() {
		StringBuilder builder = new StringBuilder();
        builder.append("<query xmlns=\"http://jabber.org/protocol/disco#items\"/>");
		return builder.toString();
	}

	@Override
	public String getChildElementXML() {
		return rawBody;
	}
}
