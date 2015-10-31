/* Copyright (c) 2013, TP Vision Holding B.V. 
 * All rights reserved.
 
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of TP Vision nor the  names of its contributors may
      be used to endorse or promote products derived from this software
      without specific prior written permission.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL TP VISION HOLDING B.V. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.tpvision.sensormgt.upnpcontrolpoint.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.util.Log;

public class XMLUPnPCPParseUtil {

	private static final String TAG = "XMLUPnPUtil";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_XML;

	static private XmlPullParser xpp = null;

	static private void setupParser() {
		// {
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xpp = factory.newPullParser();

		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// }
	}
	
	//TODO: rewrite to same style as in device datamodel
	//TODO: check parsing alternatives
	static public List<String> parseInstancePathList(String xml) throws UPnPException {

		List<String> instancePaths = new ArrayList<String>();

		setupParser();
		String ns=null;
		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(xml));
				if (xpp.next() != XmlPullParser.END_TAG) {
					if (DEBUG)
						Log.d(TAG, "instance path:" + xpp.getName());
					xpp.require(XmlPullParser.START_TAG, ns, "InstancePathList");
				}
				while (xpp.next() != XmlPullParser.END_DOCUMENT) {
					if (xpp.getEventType() != XmlPullParser.START_TAG) {
						continue;
					}
					String name = xpp.getName();
					if (name.equals("InstancePath")) {
						xpp.require(XmlPullParser.START_TAG, ns, "InstancePath");
						String instancePath = readText(xpp).trim();
						instancePaths.add(instancePath);
						xpp.require(XmlPullParser.END_TAG, ns, "InstancePath");

						if (DEBUG)
							Log.d(TAG, "content path:" + instancePath);
					} else {
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					}
				}
				// TODO: check final tag, not correct after end of doc
			} catch (XmlPullParserException e) {
				if (DEBUG) {
					Log.e(TAG, "XmlPullParserException: " + xml);
					Log.e(TAG, "XmlPullParserException: " + e.getMessage());
				}
				throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return instancePaths;
	}
	
	
	private enum ParValParseState {
		start, sparameter, sparameterpath, parameterpathtext, svalue, valuetext, evalue, eparameterpath, eparameter, eparametervaluelist, end, error
	};
	
	private static final String XMLPARAMETERVALUELIST = "ParameterValueList";
	private static final String XMLPARAMETER = "Parameter";
	private static final String XMLPARAMETERPATH = "ParameterPath";
	private static final String XMLPARAMETERVALUE = "Value";
	
	static public HashMap<String, String> parseParameterValuesList(String parameterValues) throws UPnPException {

		if (DEBUG)
			Log.d(TAG, "parseParameterValuesList: " + parameterValues);

		HashMap<String, String> parameterValuesList = new HashMap<String, String>();

		ParValParseState parseState = ParValParseState.start;
		String path = null;
		String value = null;

		setupParser();

		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(parameterValues));
				String ns=null;
				int eventType = xpp.next();
				while ((eventType != XmlPullParser.END_DOCUMENT) && (parseState != ParValParseState.error)) {
					if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)
							&& (xpp.getEventType() != XmlPullParser.TEXT)) {
						eventType = xpp.next();
						continue;
					}
					switch (parseState) {
					case start:
						xpp.require(XmlPullParser.START_TAG, ns, XMLPARAMETERVALUELIST);
						parseState = ParValParseState.sparameter;
						eventType = xpp.next();
						break;
					case sparameter:
						if (xpp.getName().contentEquals(XMLPARAMETER)) {
							xpp.require(XmlPullParser.START_TAG, ns, XMLPARAMETER);

							parseState = ParValParseState.sparameterpath;
							eventType = xpp.next();
						} else { // no more parameters
							parseState = ParValParseState.eparametervaluelist;
						}
						break;
					case sparameterpath:
						xpp.require(XmlPullParser.START_TAG, ns, XMLPARAMETERPATH);
						parseState = ParValParseState.parameterpathtext;
						eventType = xpp.next();
						break;
					case parameterpathtext:
						if (xpp.getText() != null && xpp.getText().length() > 0) {
							path = xpp.getText();
							if (path != null)
								path = path.trim();
						} else {
							path = ""; // read empty path
						}

						if (DEBUG)
							Log.d(TAG, "path:" + path);

						if (xpp.getEventType() != XmlPullParser.TEXT && xpp.getName().contentEquals(XMLPARAMETERPATH)) {
							parseState = ParValParseState.svalue; // if no
																	// value,
																	// efield
																	// was
																	// already
																	// read
						} else {
							parseState = ParValParseState.eparameterpath;
						}
						eventType = xpp.next();
						break;
					case eparameterpath:
						xpp.require(XmlPullParser.END_TAG, ns, XMLPARAMETERPATH);
						parseState = ParValParseState.svalue;
						eventType = xpp.next();
						break;
					case svalue:
						xpp.require(XmlPullParser.START_TAG, ns, XMLPARAMETERVALUE);
						parseState = ParValParseState.valuetext;
						eventType = xpp.next();
						break;
					case valuetext:
						if (xpp.getText() != null && xpp.getText().length() > 0) {
							value = xpp.getText();
							if (value != null)
								value = value.trim();
						} else {
							value = ""; // read empty value
						}
						parameterValuesList.put(path, value);
						if (DEBUG)
							Log.d(TAG, "value:" + value);

						if (xpp.getEventType() != XmlPullParser.TEXT && xpp.getName().contentEquals(XMLPARAMETERVALUE)) {
							parseState = ParValParseState.eparameter; // if no
																		// value,
							// evalue was
							// already read
						} else {
							parseState = ParValParseState.evalue;
						}
						eventType = xpp.next();
						break;
					case evalue:
						xpp.require(XmlPullParser.END_TAG, ns, XMLPARAMETERVALUE);
						parseState = ParValParseState.eparameter;
						eventType = xpp.next();
						break;
					case eparameter:
						xpp.require(XmlPullParser.END_TAG, ns, XMLPARAMETER);
						parseState = ParValParseState.sparameter;
						eventType = xpp.next();
						break;
					case eparametervaluelist:
						xpp.require(XmlPullParser.END_TAG, ns, XMLPARAMETERVALUELIST);
						parseState = ParValParseState.end;
						eventType = xpp.next();
						break;
					case end:
						eventType = xpp.next();
						break;
					default:
						parseState = ParValParseState.error;
						break;
					}

					if (DEBUG)
						Log.d(TAG, "state:" + parseState);
				}
				if (parseState != ParValParseState.end)
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (XmlPullParserException e) {
				if (DEBUG) {
					Log.e(TAG, "XmlPullParserException: " + parameterValues);
					Log.e(TAG, "XmlPullParserException: " + parseState);
					Log.e(TAG, "XmlPullParserException: " + e.getMessage());
				}
				throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return parameterValuesList;
	}
	
	private enum ParseDataRecordsState {
		start, edatarecords, end, error
	};
	
	private enum ParseDataRecordState {
		start, sfield, edatarecord, efield, end, error
	};	
	
	private static final String XMLDATARECORDS = "DataRecords";
	private static final String XMLDATARECORD = "datarecord";
	private static final String XMLFNAME = "name";
	private static final String XMLFTYPE = "type";
	private static final String XMLFENCODING = "encoding";
	private static final String XMLFNAMESPACE = "namespace";
	private static final String XMLFREQUIRED = "required";
	private static final String XMLFTABLEPROP = "tableprop";
	private static final String XMLFIELD = "field";
	private static final String XMLNAME = "name";
	
	//TODO: support for more than one datarecord
	synchronized static public ArrayList<DataItemInfo> parseDataRecords(String dataRecordsXML) throws UPnPException {

			ParseDataRecordsState parseState = ParseDataRecordsState.start;
			ArrayList<DataItemInfo> dataRecords = new ArrayList<DataItemInfo>();
			String ns = null;
			
			setupParser();
			if (xpp != null) {
					try {
						xpp.setInput(new StringReader(dataRecordsXML));
						
						while (xpp.next() != XmlPullParser.END_DOCUMENT) {
							if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
								continue;
							}

							switch (parseState) {
							case start:
								xpp.require(XmlPullParser.START_TAG, ns, XMLDATARECORDS);
								
								dataRecords = parseDataRecord(xpp, true);
								parseState = ParseDataRecordsState.end;		
								break;
							case edatarecords:
								xpp.require(XmlPullParser.END_TAG, ns, XMLDATARECORDS);
								parseState = ParseDataRecordsState.end;	
								break;
							default:
								if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
								parseState = ParseDataRecordsState.error;	
								break;
							}
								
							if (DEBUG)
								Log.d(TAG, "parseDataRecords state:" + parseState);
							
						}
						if (parseState != ParseDataRecordsState.end)
							throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
						
					} catch (XmlPullParserException e) {
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					} catch (IOException e) {
						e.printStackTrace(); //unforeseen situation, so let's print the stack trace
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					}
			}
			
			return dataRecords;
		}
		
		static private ArrayList<DataItemInfo>  parseDataRecord(XmlPullParser xpp, boolean needsValue) throws UPnPException {

			ParseDataRecordState parseState = ParseDataRecordState.start;
			
			ArrayList<DataItemInfo> fields = new ArrayList<DataItemInfo>();
			
			if (DEBUG) Log.d(TAG, "parseDataRecords "+xpp.getName());
			
			String ns = null;
			if (xpp != null) {
				try {
					
					int eventType = xpp.next();
					while ((parseState != ParseDataRecordState.end) && eventType != XmlPullParser.END_DOCUMENT) {
						if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
							xpp.next();
							continue;
						}

						switch (parseState) {
						case start:
							xpp.require(XmlPullParser.START_TAG, ns, XMLDATARECORD);
							parseState = ParseDataRecordState.sfield;
							xpp.next();
							break;
						case sfield:
							if (xpp.getName().contentEquals(XMLFIELD)) 
							{
								xpp.require(XmlPullParser.START_TAG, ns, XMLFIELD);
								String fname = xpp.getAttributeValue(ns, XMLFNAME); 
								String ftype = xpp.getAttributeValue(ns, XMLFTYPE);
								String fencoding = xpp.getAttributeValue(ns, XMLFENCODING);
								String frequired= xpp.getAttributeValue(ns, XMLFREQUIRED);
								String fnamespace= xpp.getAttributeValue(ns, XMLFNAMESPACE);
								String ftableprop= xpp.getAttributeValue(ns, XMLFTABLEPROP);
						    
								boolean frequiredBool = false;
								boolean ftablepropBool = false;
								if (!needsValue) {
									if ((frequired!=null) && (frequired.contentEquals("1"))) frequiredBool = true;
									if ((ftableprop!=null) && (ftableprop.contentEquals("1"))) ftablepropBool = true;
								}	
								
								DataItemInfo dii = new DataItemInfo(fname, ftype, fencoding, frequiredBool , fnamespace, ftablepropBool);
								fields.add(dii);
						    
								if (needsValue) dii.setValue(getRecordValue(xpp));
								else xpp.next();
								
								parseState = ParseDataRecordState.efield;
							} else 
								parseState = ParseDataRecordState.edatarecord;
							break;
						case efield: 
							xpp.require(XmlPullParser.END_TAG, ns, XMLFIELD);
							parseState = ParseDataRecordState.sfield;
							xpp.next();
							break;
						case edatarecord: 
							xpp.require(XmlPullParser.END_TAG, ns, XMLDATARECORD);
							parseState = ParseDataRecordState.end;
							xpp.next();
							break;
						default:
							if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
							parseState = ParseDataRecordState.error;	
							break;
						}

						if (DEBUG)
							Log.d(TAG, "parseDataRecord state:" + parseState);
					}
					
					if (parseState != ParseDataRecordState.end)
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (XmlPullParserException e) {
					if (DEBUG)
						Log.d(TAG, "XmlPullParserException: " + e);
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (IOException e) {
					e.printStackTrace(); //unforeseen situation, so let's print the stack trace
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				}
			}

			return fields;
		}
	
		private enum ParseSensorEventsState {
			start, ssensorevent, esensorevents, end, error
		};
		
		private enum ParseSensorEventState {
			start, esensorevent, end, error
		};	
		
		private static final String XMLSENSOREVENTS = "SensorEvents";
		private static final String XMLSENSOREVENT = "sensorevent";
		private static final String XMLSENSORCOLLECTIONID = "collectionID";
		private static final String XMLSENSORID = "sensorID";
		private static final String XMLSENSOREVENTTYPE = "event";
		
		synchronized static public ArrayList<SensorEventInfo> parseSensorEvents(String sensorEventsXML) throws UPnPException {

			ParseSensorEventsState parseState = ParseSensorEventsState.start;
			ArrayList<SensorEventInfo> sensorEvents = new ArrayList<SensorEventInfo>();
			String ns = null;
			
			setupParser();
			if (xpp != null) {
					try {
						xpp.setInput(new StringReader(sensorEventsXML));
						
						int eventType = xpp.next();
						while ((parseState != ParseSensorEventsState.end) && eventType != XmlPullParser.END_DOCUMENT) {
							if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
								xpp.next();
								continue;
							}

							switch (parseState) {
							case start:
								xpp.require(XmlPullParser.START_TAG, ns, XMLSENSOREVENTS);
								parseState = ParseSensorEventsState.ssensorevent;	
								xpp.next();
								break;
							case ssensorevent:
								if (xpp.getName().contentEquals(XMLSENSOREVENT)) {
									sensorEvents.add(parseSensorEvent(xpp));
								} else {
									parseState = ParseSensorEventsState.esensorevents;		
								}
								break;
							case esensorevents:
								xpp.require(XmlPullParser.END_TAG, ns, XMLSENSOREVENTS);
								xpp.next();
								parseState = ParseSensorEventsState.end;	 
								break;
							default:
								if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
								parseState = ParseSensorEventsState.error;	 
								break;
							}	
							if (DEBUG)
								Log.d(TAG, "parseSensorEvents state:" + parseState);

						}
						if (parseState != ParseSensorEventsState.end)
							throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
						
					} catch (XmlPullParserException e) {
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					} catch (IOException e) {
						e.printStackTrace(); //unforeseen situation, so let's print the stack trace
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					}
			}
			
			return sensorEvents;
		}
		
		
		static private SensorEventInfo parseSensorEvent(XmlPullParser xpp) throws UPnPException {

			ParseSensorEventState parseState = ParseSensorEventState.start;
			
			if (DEBUG) Log.d(TAG, "parseSensorEvent "+xpp.getName());
			
			String ns = null;
			SensorEventInfo sensorEventInfo = null;
			if (xpp != null) {
				try {
					
					int eventType = xpp.getEventType();
					while ((parseState != ParseSensorEventState.end) && eventType != XmlPullParser.END_DOCUMENT) {
						if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
							xpp.next();
							continue;
						}

						switch (parseState) {
						case start:
							xpp.require(XmlPullParser.START_TAG, ns, XMLSENSOREVENT);
							parseState = ParseSensorEventState.esensorevent;
							String collectionID = xpp.getAttributeValue(ns, XMLSENSORCOLLECTIONID); 
							String sensorID = xpp.getAttributeValue(ns, XMLSENSORID);
							String sensorEventType = xpp.getAttributeValue(ns, XMLSENSOREVENTTYPE);
							sensorEventInfo = new SensorEventInfo(collectionID, sensorID, sensorEventType);
							xpp.next();
							break;
						case esensorevent:
							xpp.require(XmlPullParser.END_TAG, ns, XMLSENSOREVENT);
							parseState = ParseSensorEventState.end;
							xpp.next();
							break;
						default:
							if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
							break;
						}

						if (DEBUG)
							Log.d(TAG, "parseDataRecord state:" + parseState);
					}
					
					if (parseState != ParseSensorEventState.end)
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (XmlPullParserException e) {
					if (DEBUG)
						Log.d(TAG, "XmlPullParserException: " + e);
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (IOException e) {
					e.printStackTrace(); //unforeseen situation, so let's print the stack trace
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				}
			}

			return sensorEventInfo;
		}
		
		
		/**
		 * Support function to get the text value. Returns null if no TEXT value was found
		 */		
		static private String  getRecordValue(XmlPullParser xpp) throws XmlPullParserException, IOException {
			
			int eventType = xpp.next();
			//skip everything thats is not a startag, endtag or text
			while ((eventType != XmlPullParser.END_DOCUMENT) && 
				((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG) 
						&& (xpp.getEventType() != XmlPullParser.TEXT)))  {
					eventType = xpp.next(); 
					continue;
			}
			
			if (xpp.getEventType() == XmlPullParser.TEXT) 
			{
				String value= xpp.getText();
				if (value!=null) value = value.trim();
				if (DEBUG) Log.d(TAG, "Value = "+value);
				return value; 
			}
					
			if (DEBUG) Log.d(TAG, "No Value found");
			return null;
		}
	
	static private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}
	
}