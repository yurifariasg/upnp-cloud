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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataTableInfo;

import android.util.Log;
import android.util.Xml;

public class XMLUPnPCPCreateUtil {
	
	private static final String TAG = "XMLUPnPCPCreateUtil";
	private static final boolean DEBUG = DebugControlPoint.DEBUG_XML;
	
	private static final String XMLCONTENTPATHLIST = "ContentPathList";
	private static final String XMLCONTENTPATH = "ContentPath";
	
	
	synchronized static public String createXMLContentPathList(List<String> paths) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);
			
			final String nns = null; // no namespace
			final String ns = "urn:schemas-upnp-org:dm:cms";
			
			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("cms", ns);
			
			serializer.startTag(ns, XMLCONTENTPATHLIST);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd");
			
			
			for (String path : paths) {
				serializer.startTag(nns, XMLCONTENTPATH);
				serializer.text(path);
				serializer.endTag(nns, XMLCONTENTPATH);
			}
			
			serializer.endTag(ns, XMLCONTENTPATHLIST);

			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final String XMLDATARECORDS = "DataRecords";
	private static final String XMLDATARECORD = "datarecord";
	private static final String XMLFIELD = "field";
	private static final String XMLNAME = "name";
//TODO: copy from deviceLib parsing, where DataRecordInfo structs is used to support dataTypeEnable	
	static public String createXMLDataRecords(HashMap<String, String> fieldValues) {
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
					"urn:schemas-upnp-org:ds:drecs http://www.upnp.org/schemas/ehs/stg-v1.xsd");

			serializer.startTag(nns, XMLDATARECORD);
			for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
				
				serializer.startTag(nns, XMLFIELD);

				serializer.attribute(nns, XMLNAME, entry.getKey());
				if (entry.getValue() != null)
					serializer.text(entry.getValue());
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
	
	
	
	private static final String XMLSENSORRECORDINFO = "SensorRecordInfo";
	private static final String XMLSENSORRECORD = "sensorrecord";

	static public String createXMLSensorRecordInfo(List<String> fields) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
	
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);

			final String nns = null; // no namespace
			final String ns = "urn:schemas-upnp-org:smgt:srecinfo";
			
			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.setPrefix("", ns);
			serializer.startTag(ns, XMLSENSORRECORDINFO);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:smgt:srecinfo http://www.upnp.org/schemas/ds/drecs-v1.xsd");

			serializer.startTag(nns, XMLSENSORRECORD);
			for (String field : fields) {
				
				serializer.startTag(nns, XMLFIELD);
				serializer.attribute(nns, XMLNAME, field);
				serializer.endTag(nns, XMLFIELD);
				
			}
			serializer.endTag(nns, XMLSENSORRECORD);
			serializer.endTag(ns, XMLSENSORRECORDINFO);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final String XMLDATATABLEINFO = "DataTableInfo";
	private static final String XMLTABLEGUID = "tableGUID";
	private static final String XMLTABLEGRN = "tableURN";
	private static final String XMLTABLEUPDATEID = "updateID";
	private static final String XMLTABLEURN = "tableURN";
	private static final String XMLFNAME = "name";
	private static final String XMLFTYPE = "type";
	private static final String XMLFENCODING = "encoding";
	private static final String XMLFNAMESPACE = "namespace";
	private static final String XMLFREQUIRED = "required";
	private static final String XMLFTABLEPROP = "tableprop";
	
	static public String createXMLDataStoreTableInfo(DataTableInfo datatable, boolean omitID) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		if (datatable!=null) {
			try {
				serializer.setOutput(writer);
				serializer.startDocument("UTF-8", false);
				final String nns = null; // no namespace
				final String ns = "urn:schemas-upnp-org:ds:dtinfo";
				
				serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
				serializer.setPrefix("", ns);	
				serializer.startTag(ns, XMLDATATABLEINFO);
				serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
						"urn:schemas-upnp-org:ds:dtinfo http://www.upnp.org/schemas/ds/dtinfo.xsd");

				if (omitID) {
					serializer.attribute(nns, XMLTABLEGUID, "");
					serializer.attribute(nns, XMLTABLEUPDATEID, "");
				}
				else {
					serializer.attribute(nns, XMLTABLEGUID, Integer.toString(datatable.getTableID()));
					serializer.attribute(nns, XMLTABLEUPDATEID, Integer.toString(datatable.getUpdateID()));
				}
				serializer.attribute(nns, XMLTABLEGRN, datatable.getURN());
				
				
				
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

	
	
	private static final String XMLPARAMETERVALUELIST = "ParameterValueList";
	private static final String XMLPARAMETER = "Parameter";
	private static final String XMLPARAMETERPATH = "ParameterPath";
	private static final String XMLPARAMETERVALUE = "Value";
	
	synchronized static public String createXMLParameterValueList(HashMap<String, String> parameterValues) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);
			
			String ns = null;
			serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.startTag(ns, XMLPARAMETERVALUELIST);
			serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"urn:schemas-upnp-org:ehs:stg http://www.upnp.org/schemas/ehs/stg-v1.xsd");

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
			serializer.endTag(ns, XMLPARAMETERVALUELIST);
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
