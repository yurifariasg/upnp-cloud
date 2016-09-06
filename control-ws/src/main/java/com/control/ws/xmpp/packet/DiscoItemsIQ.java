package com.control.ws.xmpp.packet;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

public class DiscoItemsIQ extends IQ {

	@SuppressWarnings("unused")
	private final String TAG = getClass().getSimpleName();

	private final String rawBody;

	public DiscoItemsIQ() {
		super("childElementName");
		// Log.v(TAG, "Create SoapRequestIQ");
		this.setType(Type.get);
		rawBody = createBody();
	}

	private static final String createBody() {
		StringBuilder builder = new StringBuilder();
        builder.append("<query xmlns=\"http://jabber.org/protocol/disco#items\"/>");
		return builder.toString();
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		xml.append(rawBody);
		return xml;
	}
}
