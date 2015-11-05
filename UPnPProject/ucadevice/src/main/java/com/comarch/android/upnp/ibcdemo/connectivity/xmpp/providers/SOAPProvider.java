/**
 *
 * Copyright 2013-2014 UPnP Forum All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE OR WARRANTIES OF 
 * NON-INFRINGEMENT, ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, by the UPnP Forum.
 *
 **/

/**
 * 
 */
package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.providers;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.DOM2XmlPullBuilder;
import com.comarch.android.upnp.ibcdemo.MainActivity;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapRequestIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResponseIQ;

import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.model.message.control.IncomingActionRequestMessage;
import org.fourthline.cling.model.message.control.IncomingActionResponseMessage;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.ServiceId;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fourthline.cling.model.meta.Action;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class SOAPProvider implements IQProvider {

    public final static String ELEMENT_NAME = "Envelope";
    public static final String NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
        boolean stop = false;
        String name = null;
        IQ iq = null;

        while(false == stop)
        {
            name = parser.getName();
            switch (parser.getEventType())
            {
                case XmlPullParser.START_TAG:
                {
                    if(ELEMENT_NAME.equals(name))
                    {
                        iq = parseSoap(parser);
                        stop = true;
                    }

                    break;
                }
                case XmlPullParser.END_TAG:
                {
                    stop = ELEMENT_NAME.equals(name);
                    break;
                }
            }
            if(!stop){
                parser.next();
            }
        }

        name = null;
        return iq;
	}
	
	private static final String extractActionName(final String responseTag) {
		int pos = responseTag.indexOf("Response");
		if (pos < 1) {
			return null;
		}
		return responseTag.substring(0, pos);
	}

    private String convertNodeToString(Element e) throws Exception {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(e),
                new StreamResult(buffer));
        String str = buffer.toString();
        return str;
    }
	
	public IQ parseSoap(XmlPullParser parser) throws Exception {

//        DOM2XmlPullBuilder dom2XmlPullBuilder = new DOM2XmlPullBuilder();
//        Element element = dom2XmlPullBuilder.parseSubTree(parser);
//        String soapMsg = convertNodeToString(element); // implement using a Transformer

        boolean isResponse = false;
        String actionName = null;
        HashMap<String, String> arguments = new HashMap<>();
        String serviceType = null;
        boolean stop = false;
        String lastOpenTag = null;
        
        while (!stop) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                lastOpenTag = parser.getName();
                if (!lastOpenTag.equalsIgnoreCase(ELEMENT_NAME) && !lastOpenTag.equalsIgnoreCase("body")) {
                    if (actionName == null) {
                        if (lastOpenTag.endsWith("Response")) {
                            isResponse = true;
                            actionName = extractActionName(parser.getName());
                        } else {
                            isResponse = false;
                            actionName = lastOpenTag;
                            serviceType = parser.getNamespace();
                        }
                    }
                }
            }else if(parser.getEventType() == XmlPullParser.TEXT){
                
            	String argName = lastOpenTag;
            	String value = parser.getText();
            	
            	arguments.put(argName, value);
            	
            }else if(parser.getEventType() == XmlPullParser.END_TAG){
                if(parser.getName().equalsIgnoreCase(ELEMENT_NAME)) {
                    stop=false;
                    break;
                }
            }
            if(!stop){
                parser.next();
            }
        }
        if (isResponse) {
            return new SoapResponseIQ(actionName, arguments);
        } else {
            int commaIndex = serviceType.lastIndexOf(":");
            if (commaIndex != -1) {
                return new SoapRequestIQ(actionName, serviceType.substring(0, commaIndex), arguments);
            } else {
                return new SoapRequestIQ(actionName, serviceType, arguments);
            }
        }

	}

    @SuppressWarnings("unused")
	private HashMap<String, String> parseArguments(XmlPullParser parser) throws XmlPullParserException, IOException {
        HashMap<String, String> arguments = new HashMap<String, String>();
        
        boolean stop = false;
        while (false == stop) {
            if(parser.getEventType() == XmlPullParser.START_TAG){
                arguments.put(parser.getName(),getContent(parser,parser.getName()));
            }else if(parser.getEventType() == XmlPullParser.END_TAG){
                if(parser.getName().equalsIgnoreCase(ELEMENT_NAME)){
                    stop=false;
                    break;
                }
            }
            if(!stop){
                parser.next();
            }
        }
        return arguments;
    }

    private String getContent(XmlPullParser parser, String elementName) throws XmlPullParserException, IOException {
        StringBuilder content = new StringBuilder();
        while(true){
            switch(parser.getEventType()){
                case XmlPullParser.END_TAG:
                    if(parser.getName().equals(elementName)){
                        return content.toString();
                    }
                    if(parser.isEmptyElementTag()){
                        content.append("<");
                        if(!parser.getNamespace().isEmpty()){
                            content.append(parser.getNamespace()+":");
                        }
                        content.append(parser.getName());
                        for(int i=0;i<parser.getAttributeCount();++i){
                            content.append(" ");
                            if(!parser.getAttributeNamespace(i).isEmpty()){
                                content.append(parser.getAttributeNamespace(i)+":");
                            }
                            content.append(parser.getAttributeName(i));
                            content.append("="+parser.getAttributeValue(i));
                        }
                        content.append(" />");
                        
                    }else{
                        content.append("</");
                        if(!parser.getNamespace().isEmpty()){
                            content.append(parser.getNamespace()+":");
                        }
                        content.append(parser.getName());
                        content.append(">");
                    }
                    break;
                case XmlPullParser.START_TAG:
                    content.append("<");
                    if(!parser.getNamespace().isEmpty()){
                        content.append(parser.getNamespace()+":");
                    }
                    content.append(parser.getName());
                    for(int i=0;i<parser.getAttributeCount();++i){
                        content.append(" ");
                        if(!parser.getAttributeNamespace(i).isEmpty()){
                            content.append(parser.getAttributeNamespace(i)+":");
                        }
                        content.append(parser.getAttributeName(i));
                        content.append("="+parser.getAttributeValue(i));
                    }
                    content.append(" >");
                    break;
                case XmlPullParser.TEXT:
                    content.append(parser.getText());
                    break;
            }
            parser.next();
        }
    }

}
