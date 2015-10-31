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

package com.tpvision.sensormgt.datastore;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

/**
 * Implements parsing of XML parameters used by UPnP, and creation of XML for UPnP results
 */

public class XMLUPnPDataStoreUtil {

	private static final String TAG = "XMLUPnPDataStoreUtil";
	private static final boolean DEBUG = DebugDataStore.DEBUG_XML;
	
	private static final boolean CHECKNAMESPACE = true;
	
	
	private static final String SCHEMAINSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String SCHEMALOCATIONDSINFO = "urn:schemas-upnp-org:ds:dsinfo http://www.upnp.org/schemas/ds/dsinfo.xsd";
	private static final String SCHEMALOCATIONDRECS = "urn:schemas-upnp-org:ds:drecs http://www.upnp.org/schemas/ds/drecs.xsd";
	private static final String SCHEMALOCATIONDTINFO = "urn:schemas-upnp-org:ds:dtinfo http://www.upnp.org/schemas/ds/dtinfo.xsd";
	private static final String SCHEMALOCATIONDSGROUPS = "urn:schemas-upnp-org:ds:dsgroups http://www.upnp.org/schemas/ds/dsgroups.xsd";
	private static final String SCHEMALOCATIONDSEVENTS = "urn:schemas-upnp-org:ds:dsevent http://www.upnp.org/schemas/ds/dsevent.xsd";
	private static final String SCHEMALOCATIONDRECSTATUS = "urn:schemas-upnp-org:ds:drecstatus http://www.upnp.org/schemas/ds/drecstatus-v1.xsd";
	
	private static final String XMLDATASTOREINFO = "DataStoreInfo";
	private static final String XMLDATASTORETABLES = "datastoretables";
	private static final String XMLDATASTORETABLE = "datastoretable";
	private static final String XMLTABLEGUID = "tableGUID";
	private static final String XMLTABLEGRN = "tableURN";
	private static final String XMLTABLEUPDATEID = "updateID";
	private static final String XMLDATATABLEINFO = "DataTableInfo";
	private static final String XMLTABLEURN = "tableURN";
	private static final String XMLDATARECORD = "datarecord";
	private static final String XMLFIELD = "field";
	private static final String XMLFNAME = "name";
	private static final String XMLFTYPE = "type";
	private static final String XMLFENCODING = "encoding";
	private static final String XMLFNAMESPACE = "namespace";
	private static final String XMLFREQUIRED = "required";
	private static final String XMLFTABLEPROP = "tableprop";
	private static final String XMLDATARECORDS = "DataRecords";
	private static final String XMLDATASTOREGROUPS = "DataStoreGroups";
	private static final String XMLDATASTOREGROUP = "datastoregroup";
	private static final String XMLGROUPNAME = "groupName";
	private static final String XMLDATARECFILTER = "DataRecordFilter";
	private static final String XMLFILTERSET = "filterset";
	private static final String XMLFILTER = "filter";
	private static final String XMLCONDITION = "condition";
	private static final String XMLSTATEEVENT = "StateEvent";
	private static final String XMLCREATE = "create";
	private static final String XMLUPDATE = "update";
	private static final String XMLDELETE = "delete";
	private static final String XMLUPDATETYPE = "updateType";
	private static final String XMLDATARECSSTATUS = "DataRecordsStatus";
	private static final String XMLDATARECSTATUS = "datarecordstatus";
	private static final String XMLACCEPTED = "accepted";
	
	
	static private XmlPullParser xpp = null;

	/**
	 * Use the XMLPullParser. Now one instance per parse method
	 */
	static private void setupParser() throws UPnPException {
		// {
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xpp = factory.newPullParser();

		} catch (XmlPullParserException e) {
			e.printStackTrace(); //unforeseen situation, so let's print the stack trace
			throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX); //not really an XML syntax problem, but what else to report
		}
		// }
	}
	
	static public String createXMLDataStoreGroups(String[] groups) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		if (groups!=null) {
			try {
				serializer.setOutput(writer);
				serializer.startDocument("UTF-8", false);
				
				final String nns = null; // no namespace
				final String ns = "urn:schemas-upnp-org:ds:dsgroups";
				serializer.setPrefix("xsi", SCHEMAINSTANCE);
				serializer.setPrefix("", ns);
				
				serializer.startTag(ns, XMLDATASTOREGROUPS);
				serializer.attribute(SCHEMAINSTANCE, "schemaLocation", SCHEMALOCATIONDSGROUPS);

				for (int i = 0; i < groups.length; i++) {
					serializer.startTag(nns, XMLDATASTOREGROUP);
					serializer.attribute(nns, XMLGROUPNAME, groups[i]);
					
					serializer.endTag(nns, XMLDATASTOREGROUP);
				}
				serializer.endTag(ns, XMLDATASTOREGROUPS);
				serializer.endDocument();
				
				return writer.toString();
			} catch (Exception e) {
				Log.e(TAG,"Error generating XML in createXMLDataStoreGroups: "+e);
			}
		}
		return "";
	}
	
					
	static public String createXMLDataStoreInfo(DataTableInfo[] datatable) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		if (datatable!=null) {
			try {
				serializer.setOutput(writer);
				serializer.startDocument("UTF-8", false);
				
				final String nns = null; // no namespace
				final String ns = "urn:schemas-upnp-org:ds:dsinfo";
				serializer.setPrefix("xsi", SCHEMAINSTANCE);
				serializer.setPrefix("", ns);
				
				serializer.startTag(ns, XMLDATASTOREINFO);
				serializer.attribute(SCHEMAINSTANCE, "schemaLocation", SCHEMALOCATIONDSINFO);

				if (datatable.length!=0) serializer.startTag(nns, XMLDATASTORETABLES);
				for (int i = 0; i < datatable.length; i++) {
					serializer.startTag(nns, XMLDATASTORETABLE);
					serializer.attribute(nns, XMLTABLEGUID, Integer.toString(datatable[i].getTableID()));
					if (datatable[i].getURN()!=null) serializer.attribute(nns, XMLTABLEGRN, datatable[i].getURN());
					serializer.attribute(nns, XMLTABLEUPDATEID, Integer.toString(datatable[i].getUpdateID()));
					serializer.endTag(nns, XMLDATASTORETABLE);
				}
				if (datatable.length!=0) serializer.endTag(nns, XMLDATASTORETABLES);
				serializer.endTag(ns, XMLDATASTOREINFO);
				serializer.endDocument();
				return writer.toString();
			} catch (Exception e) {
				Log.e(TAG,"Error generating XML in createXMLDataStoreInfo: "+e);
			}
		}
		return "";
	}

	
	static public String createXMLDataRecordsStatus(boolean[] accepted) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
			try {
				serializer.setOutput(writer);
				serializer.startDocument("UTF-8", false);
				
				final String nns = null; // no namespace
				final String ns = "urn:schemas-upnp-org:ds:drecstatus";
				serializer.setPrefix("xsi", SCHEMAINSTANCE);
				serializer.setPrefix("", ns);
				
				serializer.startTag(ns, XMLDATARECSSTATUS);
				serializer.attribute(SCHEMAINSTANCE, "schemaLocation", SCHEMALOCATIONDRECSTATUS);

				for (int i = 0; i < accepted.length; i++) {
					serializer.startTag(nns, XMLDATARECSTATUS);
					serializer.attribute(nns, XMLACCEPTED, accepted[i] ? "1" : "0" );
					
					serializer.endTag(nns, XMLDATARECSTATUS);
				}
				
				serializer.endTag(ns, XMLDATARECSSTATUS);
				serializer.endDocument();
				return writer.toString();
			} catch (Exception e) {
				Log.e(TAG,"Error generating XML in createXMLDataRecordsStatus: "+e);
			}
		
		return "";
	}
	
	static public String createXMLDataStoreTableInfo(DataTableInfo datatable) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		if (datatable!=null) {
			try {
				serializer.setOutput(writer);
				serializer.startDocument("UTF-8", false);
				final String nns = null; // no namespace
				final String ns = "urn:schemas-upnp-org:ds:dtinfo";
				serializer.setPrefix("xsi", SCHEMAINSTANCE);
				serializer.setPrefix("", ns);
				
				serializer.startTag(ns, XMLDATATABLEINFO);
				serializer.attribute(SCHEMAINSTANCE, "schemaLocation", SCHEMALOCATIONDTINFO);

				serializer.attribute(nns, XMLTABLEGUID, Integer.toString(datatable.getTableID()));
				serializer.attribute(nns, XMLTABLEGRN, datatable.getURN());
				serializer.attribute(nns, XMLTABLEUPDATEID, Integer.toString(datatable.getUpdateID()));
				
				
				DataItemInfo[] di = datatable.getDataItemInfoList();
				if (di.length!=0) serializer.startTag(nns, XMLDATARECORD);
				for (int i = 0; i < di.length; i++) {
					serializer.startTag(nns, XMLFIELD);

					serializer.attribute(nns, XMLFNAME, di[i].getFieldName());
					serializer.attribute(nns, XMLFTYPE, di[i].getFieldType());
					serializer.attribute(nns, XMLFENCODING, di[i].getEncoding());
					//serializer.attribute(nns, XMLFREQUIRED, Boolean.toString((di[i].isRequired())));
					serializer.attribute(nns, XMLFREQUIRED, (di[i].isRequired()) ? "1":"0");
					serializer.attribute(nns, XMLFNAMESPACE, di[i].getNamespace());
					//serializer.attribute(nns, XMLFTABLEPROP, Boolean.toString((di[i].isTableProp())));
					serializer.attribute(nns, XMLFTABLEPROP, (di[i].isTableProp()) ? "1":"0");
					
					serializer.endTag(nns, XMLFIELD);
				}
				if (di.length!=0) serializer.endTag(nns, XMLDATARECORD);
				
				serializer.endTag(ns, XMLDATATABLEINFO);
				serializer.endDocument();
				return writer.toString();
			} catch (Exception e) {
				Log.e(TAG,"Error generating XML in createXMLDataStoreTableInfo: "+e);
			}
		}
		return "";
	}
	
	static public String createXMLDataRecords(ArrayList<DataRecordInfo> dataRecords) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			final String nns = null; // no namespace
			final String ns = "urn:schemas-upnp-org:ds:drecs";

			serializer.setPrefix("", ns);
			serializer.setPrefix("xsi", SCHEMAINSTANCE);
			serializer.startTag(ns, XMLDATARECORDS);
			serializer.attribute(SCHEMAINSTANCE, "schemaLocation", SCHEMALOCATIONDRECS);

			for (DataRecordInfo record : dataRecords) {

				serializer.startTag(nns, XMLDATARECORD);
				for (DataItemInfo dataItem : record.dataRecord) {
					serializer.startTag(nns, XMLFIELD);

					serializer.attribute(nns, XMLFNAME, dataItem.getFieldName());
					serializer.attribute(nns, XMLFTYPE, dataItem.getFieldType());
					serializer.attribute(nns, XMLFENCODING, dataItem.getEncoding());
					serializer.attribute(nns, XMLFNAMESPACE, dataItem.getNamespace());

					serializer.text(dataItem.getValue());

					serializer.endTag(nns, XMLFIELD);
				}
				serializer.endTag(nns, XMLDATARECORD);
				
			}
			serializer.endTag(ns, XMLDATARECORDS);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			Log.e(TAG,"Error generating XML in createXMLDataRecords: "+e);
		}
		return "";
	}
	
	
	static public String createXMLLastChange(LastChangeInfo lastChange) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);
			
			final String nns = null; // no namespace
			final String ns = "urn:schemas-upnp-org:ds:dsevent";
			
			serializer.setPrefix("", ns);
			serializer.setPrefix("xsi", SCHEMAINSTANCE);
			serializer.startTag(ns, XMLSTATEEVENT);
			serializer.attribute(SCHEMAINSTANCE, "schemaLocation", SCHEMALOCATIONDSEVENTS);

			//Created tables
			ArrayList<DataTableInfo> createdTables = lastChange.getCreatedTables();
			if (!createdTables.isEmpty()) {
				serializer.startTag(nns, XMLCREATE);
				
				for (DataTableInfo dataTable : createdTables) {
					serializer.startTag(nns, XMLDATASTORETABLE);
					
					serializer.attribute(nns, XMLTABLEGUID, Integer.toString(dataTable.getTableID()));
					serializer.attribute(nns, XMLTABLEURN, dataTable.getURN()); 
					serializer.attribute(nns, XMLTABLEUPDATEID, Integer.toString(dataTable.getUpdateID())); 
					
					//TODO: groups
					
					serializer.endTag(nns, XMLDATASTORETABLE);
				}
				
				serializer.endTag(nns, XMLCREATE);
			}
			
			//update
			ArrayList<DataTableInfo> modifiedTables = lastChange.getModifiedTables();
			if (!modifiedTables.isEmpty()) {
				serializer.startTag(nns, XMLUPDATE);
				
				for (DataTableInfo dataTable : modifiedTables) {
					serializer.startTag(nns, XMLDATASTORETABLE);
					
					serializer.attribute(nns, XMLTABLEGUID, Integer.toString(dataTable.getTableID()));
					serializer.attribute(nns, XMLTABLEURN, dataTable.getURN()); 
					serializer.attribute(nns, XMLUPDATETYPE, dataTable.getModifications()); 
					serializer.attribute(nns, XMLTABLEUPDATEID, Integer.toString(dataTable.getUpdateID())); 
					
					serializer.endTag(nns, XMLDATASTORETABLE);
				}
				
				serializer.endTag(nns, XMLUPDATE);
			}
			
			
			//Deleted tables
			ArrayList<DataTableInfo> deletedTables = lastChange.getDeletedTables();
			if (!deletedTables.isEmpty()) {
				serializer.startTag(nns, XMLDELETE);
				
				for (DataTableInfo dataTable : deletedTables) {
					serializer.startTag(nns, XMLDATASTORETABLE);
					
					serializer.attribute(nns, XMLTABLEGUID, Integer.toString(dataTable.getTableID()));
					serializer.attribute(nns, XMLTABLEURN, dataTable.getURN()); 
					serializer.attribute(nns, XMLTABLEUPDATEID, Integer.toString(dataTable.getUpdateID())); 
					
					//TODO: groups
					
					serializer.endTag(nns, XMLDATASTORETABLE);
				}
				
				serializer.endTag(nns, XMLDELETE);
			}
			serializer.endTag(ns, XMLSTATEEVENT);
			
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			Log.e(TAG,"Error generating XML in createXMLLastChange: "+e);
		}
		return "";
	}
	
	
	private enum ParseGroupsEventState {
		start, sgroup,egroup, egroups, end, error
	};	
	
	synchronized static public ArrayList<String> parseDataStoreGroups(String dataStoreGroupsXML) throws UPnPException {

		ParseGroupsEventState parseState = ParseGroupsEventState.start;
		ArrayList<String> dataStoreGroups = new ArrayList<String>();
		String ns = null;
		
		setupParser();
		if (xpp != null) {
				try {
					xpp.setInput(new StringReader(dataStoreGroupsXML));
					
					int eventType = xpp.next();
					while ((parseState != ParseGroupsEventState.end) && eventType != XmlPullParser.END_DOCUMENT) {
						if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
							xpp.next();
							continue;
						}

						switch (parseState) {
						case start:
							xpp.require(XmlPullParser.START_TAG, ns, XMLDATASTOREGROUPS);
							parseState = ParseGroupsEventState.sgroup;	
							xpp.next();
							break;
						case sgroup:
							if (xpp.getName().contentEquals(XMLDATASTOREGROUP)) {
								dataStoreGroups.add(xpp.getAttributeValue(ns, XMLGROUPNAME));
								parseState = ParseGroupsEventState.egroup;		
							} else {
								parseState = ParseGroupsEventState.egroups;		
							}
							break;
						case egroup:
							xpp.require(XmlPullParser.END_TAG, ns, XMLDATASTOREGROUP);
							xpp.next();
							parseState = ParseGroupsEventState.egroups;	 
							break;
						case egroups:
							xpp.require(XmlPullParser.END_TAG, ns, XMLDATASTOREGROUPS);
							xpp.next();
							parseState = ParseGroupsEventState.end;	 
							break;	
							
						default:
							if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
							parseState = ParseGroupsEventState.error;	 
							break;
						}	
						if (DEBUG)
							Log.d(TAG, "ParseGroupsEventState state:" + parseState);

					}
					if (parseState != ParseGroupsEventState.end)
						throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
					
				} catch (XmlPullParserException e) {
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (IOException e) {
					e.printStackTrace(); //unforeseen situation, so let's print the stack trace
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
				}
		}
		
		return dataStoreGroups;
	}
	
//TODO: extensive testing
	//TODO: multiple filter sets
	private enum ParseFilterState {
		start, sfilterset,sfilter, efilter, efilterset, edatarecfilter, end, error
	};	
	
	synchronized static public String parseDataRecordFilter(String filtersXML) throws UPnPException {

		ParseFilterState parseState = ParseFilterState.start;
		String conditions = "";
		String ns = null;
		
		setupParser();
		if (xpp != null) {
				try {
					xpp.setInput(new StringReader(filtersXML));
					
					int eventType = xpp.next();
					while ((parseState != ParseFilterState.end) && eventType != XmlPullParser.END_DOCUMENT) {
						if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
							xpp.next();
							continue;
						}

						switch (parseState) {
						case start:
							xpp.require(XmlPullParser.START_TAG, ns, XMLDATARECFILTER);
							parseState = ParseFilterState.sfilterset;	
							xpp.next();
							break;
						case sfilterset:
							xpp.require(XmlPullParser.START_TAG, ns, XMLFILTERSET);
							parseState = ParseFilterState.sfilter;		
							xpp.next();
							conditions += "(";
							break;
						case sfilter:
							if (xpp.getName().contentEquals(XMLFILTER)) {
								conditions += " AND " + xpp.getAttributeValue(ns, XMLCONDITION);
								parseState = ParseFilterState.efilterset;		
							} else {
								parseState = ParseFilterState.efilter;		
							}
							break;
						case efilter:
							xpp.require(XmlPullParser.END_TAG, ns, XMLFILTER);
							xpp.next();
							parseState = ParseFilterState.efilterset;	 
							break;	
						case efilterset:
							xpp.require(XmlPullParser.END_TAG, ns, XMLDATARECFILTER);
							xpp.next();
							conditions += ")";
							parseState = ParseFilterState.end;	 
							break;	
						default:
							if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
							parseState = ParseFilterState.error;	 
							break;
						}	
						if (DEBUG)
							Log.d(TAG, "ParseGroupsEventState state:" + parseState);

					}
					if (parseState != ParseFilterState.end)
						throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
					
				} catch (XmlPullParserException e) {
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (IOException e) {
					e.printStackTrace(); //unforeseen situation, so let's print the stack trace
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
				}
		}
		
		return conditions;
	}
	
	private enum DataTableInfoParseState {
		start, sdatatablerecords, records, edatatableinfo, end, error
	};
	
	
	static public DataTableInfo parseDataTableInfo(String datainfo) throws UPnPException { //throws UPnPException {

		setupParser();
		DataTableInfoParseState parseState = DataTableInfoParseState.start;

		String ns = null;
		//String guid = null;
		String urn = null;
		//String updateid = null;
		DataTableInfo dataTableInfo = null;
		
		if (xpp != null) {
			try {
				xpp.setInput(new StringReader(datainfo));

				while (xpp.next() != XmlPullParser.END_DOCUMENT) {
					if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
						continue;
					}

					switch (parseState) {
					case start:
						xpp.require(XmlPullParser.START_TAG, ns, XMLDATATABLEINFO);
						parseState = DataTableInfoParseState.sdatatablerecords;
						//guid = xpp.getAttributeValue(ns, XMLTABLEGUID); should not be present
						urn = xpp.getAttributeValue(ns, XMLTABLEURN);
						//updateid = xpp.getAttributeValue(ns, XMLTABLEUPDATEID);
						parseState = DataTableInfoParseState.records;	
						xpp.next();	
						break;
					case records:
						dataTableInfo = new DataTableInfo(urn, 0, parseDataRecord(xpp, false));
						parseState = DataTableInfoParseState.edatatableinfo;	
						break;
					case edatatableinfo:
						xpp.require(XmlPullParser.END_TAG, ns, XMLDATATABLEINFO);
						parseState = DataTableInfoParseState.end;
						break;
					default:
						if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
						break;
					}
						
					if (DEBUG)
						Log.d(TAG, "parseDataTableInfo state:" + parseState);

				}
				if (parseState != DataTableInfoParseState.end)
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (XmlPullParserException e) {
				if (DEBUG)
					Log.d(TAG, "XmlPullParserException: " + e);
				throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				e.printStackTrace(); //unforeseen situation, so let's print the stack trace
				throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
			}
		}
		
		return dataTableInfo;
	}
	
	
	private enum ParseDataRecordsState {
		start, sdatarecord, edatarecords, end, error
	};
	
	private enum ParseDataRecordState {
		start, sfield, edatarecord, efield, end, error
	};	
	
	static public ArrayList<DataRecordInfo> parseDataRecords(String dataRecordsXML) throws UPnPException {

		ParseDataRecordsState parseState = ParseDataRecordsState.start;
		ArrayList<DataRecordInfo> dataRecords = new ArrayList<DataRecordInfo>();
		String ns = null;
		
		setupParser();
		if (xpp != null) {
				try {
					xpp.setInput(new StringReader(dataRecordsXML));
					
					int eventType = xpp.next();
					while ((parseState != ParseDataRecordsState.end) && eventType != XmlPullParser.END_DOCUMENT) {
						if ((xpp.getEventType() != XmlPullParser.START_TAG) && (xpp.getEventType() != XmlPullParser.END_TAG)) {
							xpp.next();
							continue;
						}

						switch (parseState) {
						case start:
							xpp.require(XmlPullParser.START_TAG, ns, XMLDATARECORDS);
							parseState = ParseDataRecordsState.sdatarecord;	
							if (CHECKNAMESPACE) {
									if ((!xpp.getNamespace().isEmpty()) && (!xpp.getNamespace().contentEquals("urn:schemas-upnp-org:ds:drecs"))) {
										throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
									}
							}
							xpp.next();
							break;
						case sdatarecord:
							if (xpp.getName().contentEquals(XMLDATARECORD)) 
							{
								dataRecords.add(new DataRecordInfo (parseDataRecord(xpp, true)));
								parseState = ParseDataRecordsState.sdatarecord;
							} else {
								parseState = ParseDataRecordsState.edatarecords;
							}
							break;
						case edatarecords:
							xpp.require(XmlPullParser.END_TAG, ns, XMLDATARECORDS);
							xpp.next();
							parseState = ParseDataRecordsState.end;	
							break;
						default:
							if (DEBUG) Log.d(TAG, "Unexpected tag: "+xpp.getName());
							break;	
						}
						if (DEBUG)
							Log.d(TAG, "parseDataRecords state:" + parseState);

					}
					if (parseState != ParseDataRecordsState.end)
						throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
					
				} catch (XmlPullParserException e) {
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
				} catch (IOException e) {
					e.printStackTrace(); //unforeseen situation, so let's print the stack trace
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
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
				
				int eventType = xpp.getEventType();
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
					throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (XmlPullParserException e) {
				if (DEBUG)
					Log.d(TAG, "XmlPullParserException: " + e);
				throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
			} catch (IOException e) {
				e.printStackTrace(); //unforeseen situation, so let's print the stack trace
				throw new UPnPException(UPnPDataStoreErrorCode.INCORRECT_ARG_XML_SYNTAX);
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

}
