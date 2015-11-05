package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet;


import org.jivesoftware.smack.packet.IQ;

public class ResultIQ extends IQ {

    @SuppressWarnings("unused")
    private final String TAG = getClass().getSimpleName();

    private final String rawBody;

    public ResultIQ(String rawBody) {
        super();
        this.setType(Type.RESULT);
        this.rawBody = rawBody;
    }

    @Override
    public String getChildElementXML() {
        return "<query xmlns=\"urn:schemas-upnp-org:cloud-1-0\" name=\"uuid:efe2b823-e08c-4a75-a9bc-822d5025d1d2\" type=\"described\">" + rawBody + "</query>";
    }
}
