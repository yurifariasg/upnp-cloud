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
package com.control.ws.connectivity.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.control.ws.UPnPManager;
import com.control.ws.XmlUtils;
import com.control.ws.model.Sensor;

public abstract class CommonSensorFactory {
	
	public static class CommonSensorFactoryWithTag extends CommonSensorFactory{
		private String TAG;
		public CommonSensorFactoryWithTag(String tag) {
			TAG = tag;
		}

		@Override
		protected String getLogTag() {
			return TAG;
		}
	}
//	protected static final String LIGHT_SENSOR_TYPE = "urn:upnp-org:smgt-st:light:upnp-org:upnplight:Comarch:CloudLight";
//	protected static final String TEMPERATURE_SENSOR_TYPE = "urn:upnp-org:smgt-st:actuator:upnp-org:Temperature_cur:Comarch:CloudLightTemperature";
//	protected static final String FRIDGE_SENSOR_TYPE = "urn:upnp-org:smgt-st:refrigerator:AcmeSensorsCorp-com:AcmeIntegratedController:FrigidaireCorp:rf217acrs:monitor";
	
	protected abstract String getLogTag();
	
	protected Sensor createSensor(String uuid,String sensorPath,Map<String,String> args){
		if (UPnPManager.DEBUG_VERBOSE) {
			System.out.println("Creating A SENSOR");
			System.out.println(args.toString());
		}
		Sensor sensor = null;
		String sensorName = args.get(sensorPath+"/SensorGroups/1/SensorGroup");
		String sensorID = args.get(sensorPath+"/SensorID");
        String sensorType = args.get(sensorPath+"/SensorType");
        if (UPnPManager.DEBUG_VERBOSE) {
			System.out.println("SENSORTYPE: " + sensorType);
        }
        sensor = createCommonSensor(sensorName, sensorType, uuid, sensorPath, args);
        if(sensor!=null){
        	sensor.setSensorID(sensorID);
        }
        return sensor;
	}

	protected Map<String,List<String>> getSensorURNs(String sensorPath,Map<String,String> args){
		Map<String,List<String>> sensorURNs = new HashMap<String,List<String>>();
        int sensorURNsNumberOfEntries = Integer.parseInt(args.get(sensorPath + "/SensorURNsNumberOfEntries"));
        for(int j=1;j<=sensorURNsNumberOfEntries;++j){
        	String sensorURNPath = sensorPath+"/SensorURNs/"+j;
        	String sensorURN = args.get(sensorPath+"/SensorURNs/"+j+"/SensorURN");
        	List<String> dataItems = new ArrayList<String>();
        	int dataItemsCount = Integer.parseInt(args.get(sensorURNPath+"/DataItemsNumberOfEntries"));
        	for(int di=1;di<=dataItemsCount;++di){
        		dataItems.add(args.get(sensorURNPath+"/DataItems/"+di+"/Name"));
        	}
        	sensorURNs.put(sensorURN, dataItems);
        }
        return sensorURNs;
	}
	
	private Sensor createCommonSensor(String sensorName, String sensorType, String uuid, String sensorPath, Map<String, String> args) {
		Map<String,List<String>> sensorURNs = getSensorURNs(sensorPath,args);
		return new Sensor(uuid, sensorType, sensorName, sensorURNs);
	}
	
	protected Map<String,String> getParameterValueList(String response) throws XmlPullParserException, IOException{
		if (UPnPManager.DEBUG_VERBOSE) {
			System.out.println("RESPONSE");
			System.out.println(response);
		}
		Map<String,String> map = new HashMap<String,String>();
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser(); //. null;//Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(response));
        int eventType = parser.getEventType();
        String lastParameterPath=null;
        String lastValue=null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = XmlUtils.nameWithoutNS(parser.getName());

            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                break;
            case XmlPullParser.START_TAG:
                if(tagName.equalsIgnoreCase("Parameter")){
                	lastParameterPath = null;
                	lastValue = null;
                }else if(tagName.equalsIgnoreCase("ParameterPath")){
                	lastParameterPath = XmlUtils.readText(parser);
                }else if(tagName.equalsIgnoreCase("Value")){
                	lastValue = XmlUtils.readText(parser);
                }
                break;
            case XmlPullParser.END_TAG:
            	if(tagName.equalsIgnoreCase("Parameter")){
            		if(lastParameterPath!=null && lastValue!=null){
            			map.put(lastParameterPath, lastValue);
            		}
            	}
                break;
            }
            eventType = parser.next();
            
        }
		return map;
	}

    protected String createParameters(List<String> contentPaths){
		StringBuilder sb = new StringBuilder("<cms:ContentPathList xmlns:cms=\"urn:schemas-upnp-org:dm:cms\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd\" >"); 
		for(String s : contentPaths){
			sb.append("<ContentPath>");
			sb.append(s);
			sb.append("</ContentPath>");
		}
		sb.append("</cms:ContentPathList>");
		return sb.toString();
	}
    
}
