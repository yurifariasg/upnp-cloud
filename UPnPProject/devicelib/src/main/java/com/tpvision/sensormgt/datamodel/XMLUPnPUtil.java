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

package com.tpvision.sensormgt.datamodel;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.tpvision.sensormgt.sensormodel.DataItemInfo;

import android.util.Log;
import android.util.Xml;

public class XMLUPnPUtil {

	private static final String TAG = "XMLUPnPUtil";
	private static final boolean DEBUG = DebugDatamodel.DEBUG_XML;
	
	private static final boolean CHECKNAMESPACE = true;

	// values for generating XML
	private static final String XMLSUPPORTEDDATAMODELS = "SupportedDataModels";
	private static final String XMLSUBTREE = "SubTree";
	private static final String XMLURI = "URI";
	private static final String XMLLOCATION = "Location";
	private static final String XMLURL = "URL";
	private static final String XMLDESCRIPTION = "Description";
	private static final String XMLSENSOREVENTS = "SensorEvents";
	private static final String XMLSENSOREVENT = "sensorevent";
	private static final String XMLCOLLID = "collectionID";
	private static final String XMLSENSORID = "sensorID";
	private static final String XMLEVENTTYPE = "event";
	private static final String XMLSENSORRECORDINFO = "SensorRecordInfo";
	private static final String XMLSENSORRECORD = "sensorrecord";
	private static final String XMLDATARECORDS = "DataRecords";
	private static final String XMLDATARECORD = "datarecord";
	private static final String XMLFIELD = "field";
	private static final String XMLNAME = "name";
	private static final String XMLPREFIX = "prefix";
	private static final String XMLPARAMETERVALUELIST = "ParameterValueList";
	private static final String XMLPARAMETER = "Parameter";
	private static final String XMLPARAMETERPATH = "ParameterPath";
	private static final String XMLPARAMETERVALUE = "Value";
	private static final String XMLNODEATTRPATHLIST = "NodeAttributePathList";
	private static final String XMLNODEATTRPATH = "NodeAttributePath";
	private static final String XMLNODEATTRVALUELIST = "NodeAttributeValueList";
	private static final String XMLNODE = "Node";
	private static final String XMLEOC = "EventOnChange";
	private static final String XMLINSTANCEPATHLIST = "InstancePathList";
	private static final String XMLINSTANCEPATH = "InstancePath";
	private static final String XMLCONTENTPATHLIST = "ContentPathList";
	private static final String XMLCONTENTPATH = "ContentPath";
	private static final String XMLSTRUCTUREPATHLIST = "StructurePathList";
	private static final String XMLSTRUCTUREPATH = "StructurePath";
	private static final String XMLTRANSPORTCONNECTIONS = "TransportConnections";
	private static final String XMLTRANSPORTCONN = "transportconnection";
	private static final String XMLCONNID = "transportConnectionID";
	private static final String XMLTRANSPORTURL = "transportURL";
	private static final String XMLCLIENTID = "sensorClientID";
	private static final String XMLNAMESPACE = "xmlns";
	
	private static final String ns = null; //no namespace
	
	
	
	// TODO: clean up mixing of states
	private enum ParseState {
		start, srecord, sfield, fieldtext, efield, morefields, morerecords, erecord, end
	};

	private enum ParValParseState {
		start, sparameter, sparameterpath, parameterpathtext, svalue, valuetext, evalue, eparameterpath, eparameter, eparametervaluelist, end, error
	};

	private enum AttrValParseState {
		start, snode, snodeattrpath, enodeattrpath, seoc, eeoc, enode, enodeattrvaluelist, end, error
	};

	static private XmlPullParser xpp = null;

	synchronized static private void setupParser() {
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

	/**
	 * 
	 * XML CREATION FUNCTIONS
	 * 
	 */
	
	synchronized static public String createXMLSupportedDatamodels(String uri, String location, String url, String description) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("cms", "urn:schemas-upnp-org:dm:cms");
			
			serializer.startTag("urn:schemas-upnp-org:dm:cms", XMLSUPPORTEDDATAMODELS);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd");
			
			serializer.startTag(ns, XMLSUBTREE);
			
			serializer.startTag(ns, XMLURI);
			serializer.text(uri);
			serializer.endTag(ns, XMLURI);
			
			serializer.startTag(ns, XMLLOCATION);
			serializer.text(location);
			serializer.endTag(ns, XMLLOCATION);
			
			if ((url!=null)&&(!url.isEmpty())) {
				serializer.startTag(ns, XMLURL);
				serializer.text(url);
				serializer.endTag(ns, XMLURL);
			}
			
			if ((description!=null)&&(!description.isEmpty())) {
				serializer.startTag(ns, XMLDESCRIPTION);
				serializer.text(description);
				serializer.endTag(ns, XMLDESCRIPTION);
			}
			
			serializer.endTag(ns, XMLSUBTREE);
			
			serializer.endTag("urn:schemas-upnp-org:dm:cms", XMLSUPPORTEDDATAMODELS);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	synchronized static public String createXMLInstancePathList(List<String> paths) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("cms", "urn:schemas-upnp-org:dm:cms");
			
			serializer.startTag("urn:schemas-upnp-org:dm:cms", XMLINSTANCEPATHLIST);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd");
			
			
			for (String path : paths) {
				serializer.startTag(ns, XMLINSTANCEPATH);
				serializer.text(path);
				serializer.endTag(ns, XMLINSTANCEPATH);
			}
			
			serializer.endTag("urn:schemas-upnp-org:dm:cms", XMLINSTANCEPATHLIST);

			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	synchronized static public String createXMLContentPathList(List<String> paths) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("cms", "urn:schemas-upnp-org:dm:cms");
			
			serializer.startTag("urn:schemas-upnp-org:dm:cms", XMLCONTENTPATHLIST);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd");
			
			
			for (String path : paths) {
				serializer.startTag(ns, XMLCONTENTPATH);
				serializer.text(path);
				serializer.endTag(ns, XMLCONTENTPATH);
			}
			
			serializer.endTag("urn:schemas-upnp-org:dm:cms", XMLCONTENTPATHLIST);

			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	synchronized static public String createXMLStructurePathList(List<String> paths) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("cms", "urn:schemas-upnp-org:dm:cms");
			
			serializer.startTag("urn:schemas-upnp-org:dm:cms", XMLSTRUCTUREPATHLIST);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd");
			
			
			for (String path : paths) {
				serializer.startTag(ns, XMLSTRUCTUREPATH);
				serializer.text(path);
				serializer.endTag(ns, XMLSTRUCTUREPATH);
			}
			
			serializer.endTag("urn:schemas-upnp-org:dm:cms", XMLSTRUCTUREPATHLIST);

			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	synchronized static public String createXMLParameterValueList(HashMap<String, String> parameterValues) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("cms", "urn:schemas-upnp-org:dm:cms");
			serializer.startTag("urn:schemas-upnp-org:dm:cms", XMLPARAMETERVALUELIST);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd");

			for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
				serializer.startTag(ns, XMLPARAMETER);
				serializer.startTag(ns, XMLPARAMETERPATH);
				serializer.text(entry.getKey());
				serializer.endTag(ns, XMLPARAMETERPATH);
				
				serializer.startTag(ns, XMLPARAMETERVALUE);
				if (entry.getValue() != null)
					serializer.text(entry.getValue());
				else
					serializer.text("");
				serializer.endTag(ns, XMLPARAMETERVALUE);
				
				serializer.endTag(ns, XMLPARAMETER);
			}
			serializer.endTag("urn:schemas-upnp-org:dm:cms", XMLPARAMETERVALUELIST);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	synchronized static public String createXMLNodeAttributeValueList(HashMap<String, String> parameterValues) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("cms", "urn:schemas-upnp-org:dm:cms");
			serializer.startTag("urn:schemas-upnp-org:dm:cms", XMLNODEATTRVALUELIST);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd");

			for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
				serializer.startTag(ns, XMLNODE);

				serializer.startTag(ns, XMLNODEATTRPATH);
				serializer.text(entry.getKey());
				serializer.endTag(ns, XMLNODEATTRPATH);

				String attrCSV = entry.getValue();

				if (attrCSV != null) {
					String[] attrList = attrCSV.split(",");

					for (int i = 0; (i + 1) < attrList.length; i += 2) {
						serializer.startTag(ns, attrList[i]);
						serializer.text(attrList[i + 1]);
						serializer.endTag(ns, attrList[i]);
					}
				}

				serializer.endTag(ns, XMLNODE);
			}
			serializer.endTag("urn:schemas-upnp-org:dm:cms", XMLNODEATTRVALUELIST);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	synchronized static public String createXMLDataRecords(DataRecordInfo record, boolean sensorDataTypeEnable) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);
			
			final String nns = null; // no namespace
			final String ns = "urn:schemas-upnp-org:ds:drecs";
			
			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("", ns);
			serializer.startTag(ns, XMLDATARECORDS);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:ds:drecs http://www.upnp.org/schemas/ds/drecs-v1.xsd");

			serializer.startTag(nns, XMLDATARECORD);
			for (DataItemInfo di: record.dataRecord) {
				
				serializer.startTag(nns, XMLFIELD);

				String name = di.getFieldName();
				if (!di.getPrefix().isEmpty()) {
					name = '['+di.getPrefix()+']'+name;
				}
				serializer.attribute(nns, XMLNAME, name);
				if (sensorDataTypeEnable) {
					serializer.attribute(nns, XMLFTYPE, di.getFieldType());
//TODO: check namespace					
					serializer.attribute(nns, XMLFNAMESPACE, di.getNamespace());
				}
				serializer.attribute(nns, XMLFENCODING, di.getEncoding());

				if (di.getValue() != null)
					serializer.text(di.getValue());
				else
					serializer.text("");
				
				
				

				serializer.endTag(nns, XMLFIELD);
				
			}
			serializer.endTag(nns, XMLDATARECORD);
			serializer.endTag(ns, XMLDATARECORDS);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	synchronized static public String createXMLSensorEvents(List<SensorEventData> events) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			final String nns = null; // no namespace
			final String ns = "urn:schemas-upnp-org:smgt:sdmevent";
			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("", ns);
			
			serializer.startTag(ns, XMLSENSOREVENTS);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:smgt:sdmevent http://www.upnp.org/schemas/smgt/sdmevent-v1.xsd");

			for (SensorEventData event : events) {
				serializer.startTag(nns, XMLSENSOREVENT);
				serializer.attribute(nns, XMLCOLLID, event.getCollectionID());
				if (event.getSensorID()!=null)
					serializer.attribute(nns, XMLSENSORID, event.getSensorID());
				else
					serializer.attribute(nns, XMLSENSORID, "");
				serializer.attribute(nns, XMLEVENTTYPE, event.getEventType());
				serializer.endTag(nns, XMLSENSOREVENT);
			}
			serializer.endTag(ns, XMLSENSOREVENTS);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	
	synchronized static public String createXMLTransportConnections (ArrayList<ClientConnectionInfo> transportConnections) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			final String nns = null; // no namespace
			final String ns = "urn:schemas-upnp-org:smgt:tspc";
			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("", ns);
			
			serializer.startTag(ns, XMLTRANSPORTCONNECTIONS);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:smgt:tspc http://www.upnp.org/schemas/smgt/tspc-v1.xsd");

			for (ClientConnectionInfo conn : transportConnections) {
				serializer.startTag(nns, XMLTRANSPORTCONN);
				serializer.attribute(nns, XMLSENSORID, conn.getSensorID());
				serializer.attribute(nns, XMLCONNID, Integer.toString(conn.getConnectionID()));
				serializer.attribute(nns, XMLTRANSPORTURL, conn.getTransportURL().toString());
				serializer.attribute(nns, XMLCLIENTID, conn.getClientID());
		//		serializer.attribute(nns, XMLNAMESPACE, conn.getNamespace());
				serializer.endTag(nns, XMLTRANSPORTCONN);
			}
			serializer.endTag(ns, XMLTRANSPORTCONNECTIONS);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 
	 * XML PARSING FUNCTIONS
	 * 
	 */
	

	synchronized static public List<String> parseInstancePathList(String xml) throws UPnPException {

		List<String> instancePaths = new ArrayList<String>();

		setupParser();

		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(xml));
				if (xpp.next() != XmlPullParser.END_TAG) {
					if (DEBUG)
						Log.d(TAG, "instance path:" + xpp.getName());
					xpp.require(XmlPullParser.START_TAG, ns, "InstancePathList");
					if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:dm:cms"))) {
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					}
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

	synchronized static public List<String> parseAttributePathList(String xml) throws UPnPException {

		List<String> attributePaths = new ArrayList<String>();

		setupParser();

		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(xml));
				if (xpp.next() != XmlPullParser.END_TAG) {
					if (DEBUG)
						Log.d(TAG, "instance path:" + xpp.getName());
					xpp.require(XmlPullParser.START_TAG, ns, XMLNODEATTRPATHLIST);
					if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:dm:cms"))) {
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					}
				}
				while (xpp.next() != XmlPullParser.END_DOCUMENT) {
					if (xpp.getEventType() != XmlPullParser.START_TAG) {
						continue;
					}
					xpp.require(XmlPullParser.START_TAG, ns, XMLNODEATTRPATH);
					String attributePath = readText(xpp).trim();
					attributePaths.add(attributePath);
					xpp.require(XmlPullParser.END_TAG, ns, XMLNODEATTRPATH);

					if (DEBUG)
						Log.d(TAG, "content path:" + attributePath);
				}
				// TODO: check final tag, not correct after end of doc
			} catch (XmlPullParserException e) {
				throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return attributePaths;
	}

	synchronized static public HashMap<String, String> parseNodeAttrValueList(String nodeAttrValues) throws UPnPException {

		if (DEBUG)
			Log.d(TAG, "parseNodeAttrValueList: " + nodeAttrValues);

		HashMap<String, String> nodeAtttrValueList = new HashMap<String, String>();

		AttrValParseState parseState = AttrValParseState.start;
		String path = null;
		String value = null;

		setupParser();

		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(nodeAttrValues));

				int eventType = xpp.next();
				while ((eventType != XmlPullParser.END_DOCUMENT) && (parseState != AttrValParseState.error)) {
					if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
						eventType = xpp.next();
						continue;
					}
					switch (parseState) {
					case start:
						xpp.require(XmlPullParser.START_TAG, ns, XMLNODEATTRVALUELIST);
						if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:dm:cms"))) {
							throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
						}
						parseState = AttrValParseState.snode;
						eventType = xpp.next();
						break;
					case snode:
						if (xpp.getName().contentEquals(XMLNODE)) {
							xpp.require(XmlPullParser.START_TAG, ns, XMLNODE); //more nodes

							parseState = AttrValParseState.snodeattrpath;
							eventType = xpp.next();
						} else { // no more parameters
							parseState = AttrValParseState.enode;
						}
						break;
					case snodeattrpath:
						xpp.require(XmlPullParser.START_TAG, ns, XMLNODEATTRPATH);
						eventType = xpp.next();
						path = xpp.getText();
						if (path != null)
							path = path.trim();
						if (DEBUG)
							Log.d(TAG, "path:" + path);
						parseState = AttrValParseState.enodeattrpath;
						// eventType=xpp.next(); //No idea why getText needs a
						// next(), but not for reading end tag
						break;
					case enodeattrpath:
						xpp.require(XmlPullParser.END_TAG, ns, XMLNODEATTRPATH);
						parseState = AttrValParseState.seoc;
						eventType = xpp.next();
						break;
					case seoc:
						xpp.require(XmlPullParser.START_TAG, ns, XMLEOC);
						eventType = xpp.next();
						value = xpp.getText();
						if (value != null)
							value = value.trim();
						if (DEBUG)
							Log.d(TAG, "value:" + value);

						nodeAtttrValueList.put(path, value);

						parseState = AttrValParseState.eeoc;
						// eventType=xpp.next(); //No idea why getText needs a
						// next(), but not for reading end tag
						break;
					case eeoc:
						xpp.require(XmlPullParser.END_TAG, ns, XMLEOC);
						parseState = AttrValParseState.enode;
						eventType = xpp.next();
						break;
					case enode:
						xpp.require(XmlPullParser.END_TAG, ns, XMLNODE);
						parseState = AttrValParseState.enodeattrvaluelist;
						eventType = xpp.next();
						break;
					case enodeattrvaluelist:
						xpp.require(XmlPullParser.END_TAG, ns, XMLNODEATTRVALUELIST);
						parseState = AttrValParseState.end;
						eventType = xpp.next();
						break;
					case end:
						eventType = xpp.next();
						break;
					default:
						parseState = AttrValParseState.error;
						break;
					}

					if (DEBUG)
						Log.d(TAG, "state:" + parseState);
				}
				if (parseState != AttrValParseState.end)
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (XmlPullParserException e) {
				if (DEBUG)
					Log.d(TAG, "XmlPullParserException: " + e);
				throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return nodeAtttrValueList;
	}

	private enum ParseContenPathListState {
		start, scontentpath, value, econtentpath, econtentpathlist, end, error
	};	
	
	synchronized static public ArrayList<String> parseContentPathList(String parameterXML) throws UPnPException {

		ParseContenPathListState parseState = ParseContenPathListState.start;
		ArrayList<String> contentPaths = new ArrayList<String>();
		String ns = null;
		
		setupParser();
		if (xpp != null) {
				try {
					xpp.setInput(new StringReader(parameterXML));
					
					int eventType = xpp.next();
					while ((parseState != ParseContenPathListState.end) && eventType != XmlPullParser.END_DOCUMENT) {
						if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
							xpp.next();
							continue;
						}

						switch (parseState) {
						case start:
							xpp.require(XmlPullParser.START_TAG, ns, XMLCONTENTPATHLIST);
							if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:dm:cms"))) {
								throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
							}
							parseState = ParseContenPathListState.scontentpath;	
							xpp.next();
							break;
						case scontentpath:
							if (xpp.getName().contentEquals(XMLCONTENTPATH)) {
								xpp.next();
								contentPaths.add(getText(xpp));
								parseState = ParseContenPathListState.econtentpath;	
								
							} else {
								parseState = ParseContenPathListState.econtentpathlist;		
							}
							break;
						case econtentpath:
							xpp.require(XmlPullParser.END_TAG, ns, XMLCONTENTPATH);
							xpp.next();
							parseState = ParseContenPathListState.scontentpath;	 
							break;
						case econtentpathlist:
							xpp.require(XmlPullParser.END_TAG, ns, XMLCONTENTPATHLIST);
							xpp.next();
							parseState = ParseContenPathListState.end;	 
							break;	
							
						default:
							if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
							parseState = ParseContenPathListState.error;	 
							break;
						}	
						if (DEBUG)
							Log.d(TAG, "parseContentPathList state:" + parseState);

					}
					if (parseState != ParseContenPathListState.end)
						throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
					
				} catch (XmlPullParserException e) {
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (IOException e) {
					e.printStackTrace(); //unforeseen situation, so let's print the stack trace
					throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
				}
		}
		
		return contentPaths;
	}
	

	synchronized static public HashMap<String, String> parseDataItemDescription(String values) throws UPnPException {
		final String KEY_ROOT = "DataItemDescription";
		final String KEY_DESCRIPTION = "description";
		final String KEY_INTERVAL = "interval";
		final String KEY_RELATED_TIME = "relatedItem";

		HashMap<String, String> map = new HashMap<String, String>();

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setCoalescing(true);

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(values));

			Document doc = dbf.newDocumentBuilder().parse(is);

			if (doc != null) {
				NodeList nList = doc.getElementsByTagName(KEY_ROOT);

				for (int i = 0; i < nList.getLength(); i++) {
					Node nNode = nList.item(i);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						map.put(KEY_DESCRIPTION, eElement.getElementsByTagName(KEY_DESCRIPTION).item(0).getTextContent());
					}
				}

			}

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}

		return map;
	}

	static public String getValue(Element item, String str) {
		NodeList n = item.getElementsByTagName(str);
		return getElementValue(n.item(0));
	}

	static public final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	synchronized static public HashMap<String, String> parseParameterValuesList(String parameterValues) throws UPnPException {

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
						if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:dm:cms"))) {
							throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
						}
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
	
	private static final String XMLFNAME = "name";
	private static final String XMLFTYPE = "type";
	private static final String XMLFENCODING = "encoding";
	private static final String XMLFNAMESPACE = "namespace";
	private static final String XMLFREQUIRED = "required";
	private static final String XMLFTABLEPROP = "tableprop";
	
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
								if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:ds:drecs"))) {
									throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
								}
								
								dataRecords = parseDataRecord(xpp, true);
								parseState = ParseDataRecordsState.end;		
								break;
							case edatarecords:
								xpp.require(XmlPullParser.END_TAG, ns, XMLDATARECORDS);
								parseState = ParseDataRecordsState.end;	
								break;
							}
								
							if (DEBUG)
								Log.d(TAG, "parseDataRecords state:" + parseState);

						}
						if (parseState != ParseDataRecordsState.end)
							throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
						
					} catch (XmlPullParserException e) {
						throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
					} catch (IOException e) {
						e.printStackTrace(); //unforeseen situation, so let's print the stack trace
						throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
					}
			}
			
			return dataRecords;
		}
		
		static private ArrayList<DataItemInfo>  parseDataRecord(XmlPullParser xpp, boolean needsValue) throws UPnPException {

			ParseDataRecordState parseState = ParseDataRecordState.start;
			
			ArrayList<DataItemInfo> fields = new ArrayList<DataItemInfo>();
			
			Log.d(TAG, "parseDataRecords "+xpp.getName());
			
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
							break;
						}

						if (DEBUG)
							Log.d(TAG, "parseDataRecord state:" + parseState);
					}
					
					if (parseState != ParseDataRecordState.end)
						throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (XmlPullParserException e) {
					if (DEBUG)
						Log.d(TAG, "XmlPullParserException: " + e);
					throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (IOException e) {
					e.printStackTrace(); //unforeseen situation, so let's print the stack trace
					throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
				}
			}

			return fields;
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
		
		
	static public HashMap<String, String> parseDataRecords2(String dataRecords) throws UPnPException {

		HashMap<String, String> fieldValues = new HashMap<String, String>();

		ParseState parseState = ParseState.start;

		setupParser();

		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(dataRecords));

				String field = "";
				while (xpp.next() != XmlPullParser.END_DOCUMENT) {
					if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)
							&& (xpp.getEventType() != XmlPullParser.TEXT)) {
						continue;
					}

					switch (parseState) {
					case start:
						xpp.require(XmlPullParser.START_TAG, ns, XMLDATARECORDS);
						if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:ds:drecs"))) {
							throw new UPnPException(UPnPConfMgtErrorCode.INCORRECT_ARG_XML_SYNTAX);
						}
						parseState = ParseState.srecord;
						break;
					case srecord:
						xpp.require(XmlPullParser.START_TAG, ns, XMLDATARECORD);
						parseState = ParseState.sfield;
						break;
					case sfield:
						xpp.require(XmlPullParser.START_TAG, ns, XMLFIELD);
						field = xpp.getAttributeValue(ns, XMLNAME);

						parseState = ParseState.fieldtext;
						break;
					case fieldtext:
						if (DEBUG)
							Log.d(TAG, "ParseDataRecords got: " + field + ", " + xpp.getText());
						if (xpp.getText() != null && xpp.getText().length() > 0) {
							fieldValues.put(field, xpp.getText());
						} else {
							fieldValues.put(field, "");
						}

						if (xpp.getEventType() != XmlPullParser.TEXT && xpp.getName().contentEquals(XMLFIELD)) {
							parseState = ParseState.morefields; // if no value,
																// efield was
																// already read
						} else {
							parseState = ParseState.efield;
						}
						break;
					case efield:
						xpp.require(XmlPullParser.END_TAG, ns, XMLFIELD);
						parseState = ParseState.morefields;
						break;
					case morefields:
						if (xpp.getName().contentEquals(XMLFIELD)) {
							

							parseState = ParseState.sfield;
						} else {
							parseState = ParseState.morerecords;
						}
						break;
					case morerecords:
						if (xpp.getName().contentEquals(XMLDATARECORD)) {
							parseState = ParseState.srecord;
						} else {
							xpp.require(XmlPullParser.END_TAG, ns, XMLDATARECORDS);
							parseState = ParseState.end;
						}
						break;
					}

					if (DEBUG)
						Log.d(TAG, "state:" + parseState);

				}
				if (parseState != ParseState.end)
					throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (XmlPullParserException e) {
				if (DEBUG)
					Log.d(TAG, "XmlPullParserException: " + e);
				throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return fieldValues;
	}

	synchronized static public List<SensorRecordInfo> parseSensorRecordInfo(String sensorRecordInfo) throws UPnPException {

		List<SensorRecordInfo> sensorRecordInfoList = new ArrayList<SensorRecordInfo>();
		ParseState parseState = ParseState.start;

		setupParser();

		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(sensorRecordInfo));

				while (xpp.next() != XmlPullParser.END_DOCUMENT) {
					if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
						continue;
					}
					switch (parseState) {
					case start:
						xpp.require(XmlPullParser.START_TAG, ns, XMLSENSORRECORDINFO);
						if ((CHECKNAMESPACE) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:smgt:srecinfo"))) {
							throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
						}
						parseState = ParseState.srecord;
						break;
					case srecord:
						xpp.require(XmlPullParser.START_TAG, ns, XMLSENSORRECORD);
						parseState = ParseState.sfield;
						break;
					case sfield:
						xpp.require(XmlPullParser.START_TAG, ns, XMLFIELD);
						String fieldName = xpp.getAttributeValue(ns, XMLNAME);
						String prefix = xpp.getAttributeValue(ns, XMLPREFIX);
						if (prefix==null) prefix ="";
						sensorRecordInfoList.add(new SensorRecordInfo(fieldName, prefix));
						
						parseState = ParseState.efield;
						break;
					case efield:
						xpp.require(XmlPullParser.END_TAG, ns, XMLFIELD);
						parseState = ParseState.morefields;
						break;
					case morefields:
						if (xpp.getName().contentEquals(XMLFIELD)) {
							xpp.require(XmlPullParser.START_TAG, ns, XMLFIELD);
							fieldName = xpp.getAttributeValue(ns, XMLNAME);
							prefix = xpp.getAttributeValue(ns, XMLPREFIX);
							if (prefix==null) prefix ="";
							sensorRecordInfoList.add(new SensorRecordInfo(fieldName, prefix));
							
							parseState = ParseState.efield;
						} else {
							xpp.require(XmlPullParser.END_TAG, ns, XMLSENSORRECORD);
							parseState = ParseState.erecord;
						}
						break;
					case erecord:
						xpp.require(XmlPullParser.END_TAG, ns, XMLSENSORRECORDINFO);
						parseState = ParseState.end;
						break;
					}
					// xpp.next();
					if (DEBUG)
						Log.d(TAG, "state:" + parseState);

				}
				if (parseState != ParseState.end)
					throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (XmlPullParserException e) {
				if (DEBUG)
					Log.d(TAG, "XmlPullParserException: " + e);
				throw new UPnPException(UPnPSensorTransportErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return sensorRecordInfoList;
	}

	// TODO: integrate in datamodel tree setup
	synchronized static public List<String> parseInstanceTree(InputStream stream) {

		List<String> contentPaths = new ArrayList<String>();

		setupParser();

		if (xpp != null) {
			try {
				xpp.setInput(new InputStreamReader(stream));

				// skip tags till sensorDevices found
				while (xpp.next() != XmlPullParser.END_DOCUMENT) {
					if (DEBUG)
						Log.d(TAG, "next:");
					if (xpp.getEventType() != XmlPullParser.START_TAG) {
						continue;
					}
					String name = xpp.getName();
					if (name.equals("SensorDevices")) {
						parseSensorDevices(xpp);
					}

				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (DEBUG)
				Log.d(TAG, "Reading input stream sensor config:");

		}

		return contentPaths;
	}

	// TODO: check interface
	static private void parseSensorDevices(XmlPullParser xpp) {
		
		setupParser();
		try {
			// iterate over sensordevices
			do {

				if (DEBUG)
					Log.d(TAG, "SensorDevice found");
				xpp.require(XmlPullParser.START_TAG, ns, "SensorDevices");
				if (DEBUG)
					Log.d(TAG, "SensorDevice start");
				xpp.nextTag();
				if (DEBUG)
					Log.d(TAG, "now " + xpp.getName());
				xpp.require(XmlPullParser.START_TAG, ns, "Instance");
				if (DEBUG)
					Log.d(TAG, "Instance start");

				Integer id = Integer.valueOf(xpp.getAttributeValue(0));
				// create new device and add all tags
				Log.d(TAG, "New sensorDevice" + id);

				boolean endInstance = false;
				while (xpp.next() != XmlPullParser.END_DOCUMENT && !endInstance) {
					if (xpp.getEventType() == XmlPullParser.START_TAG) {

						if (xpp.getName().equals("Sensors")) {
							parseSensors(xpp);
						} else {
							if (DEBUG)
								Log.d(TAG, "new field: " + xpp.getName());
						}
					}
					if (xpp.getEventType() == XmlPullParser.END_TAG) {
						endInstance = xpp.getName().equals("Instance");
					}
				}
				xpp.require(XmlPullParser.END_TAG, ns, "Instance");
				xpp.require(XmlPullParser.END_TAG, ns, "SensorDevices");
			} while (xpp.next() != XmlPullParser.END_DOCUMENT);
			// if (DEBUG) Log.d(TAG,"content path:"+contentPath);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO: check interface
	static private void parseSensors(XmlPullParser xpp) {
		
		setupParser();
		
		try {
			// iterate over sensors
			do {

				if (DEBUG)
					Log.d(TAG, "Sensor found");
				xpp.require(XmlPullParser.START_TAG, ns, "Sensors");

				xpp.nextTag();
				if (DEBUG)
					Log.d(TAG, "now " + xpp.getName());
				xpp.require(XmlPullParser.START_TAG, ns, "Instance");

				Integer id = Integer.valueOf(xpp.getAttributeValue(0));
				// create new device and add all tags
				Log.d(TAG, "New sensor" + id);

				boolean endInstance = false;
				while (xpp.next() != XmlPullParser.END_DOCUMENT && !endInstance) {
					if (xpp.getEventType() == XmlPullParser.START_TAG) {

						String field = xpp.getName();
						if (field.equals("SensorsRelated")) {
							//
						} else if (field.equals("SensorGroups")) {

						} else if (field.equals("SensorPermissions")) {
							if (DEBUG)
								Log.d(TAG, "new field: " + field);
						}

					}
					if (xpp.getEventType() == XmlPullParser.END_TAG) {
						endInstance = xpp.getName().equals("Instance");
					}
				}
				xpp.require(XmlPullParser.END_TAG, ns, "Instance");
				xpp.require(XmlPullParser.END_TAG, ns, "Sensors");

			} while (xpp.next() != XmlPullParser.END_DOCUMENT);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static private String getText(XmlPullParser parser) throws IOException, XmlPullParserException {
		
		int eventType = xpp.getEventType();
		//skip anything that's not a starttag, endtag, or text
		while ((eventType != XmlPullParser.END_DOCUMENT) &&
				(eventType != XmlPullParser.START_TAG) && (eventType != XmlPullParser.END_TAG) && (eventType != XmlPullParser.TEXT)) {
			eventType = xpp.next();
		}
		
		if (xpp.getEventType() == XmlPullParser.TEXT) {
  			return parser.getText().trim();
		}
		
		return "";
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
