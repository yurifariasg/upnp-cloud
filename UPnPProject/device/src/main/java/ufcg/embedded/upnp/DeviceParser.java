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

package ufcg.embedded.upnp;


import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tpvision.sensormgt.datamodel.DatamodelNode;
import com.tpvision.sensormgt.datamodel.InstanceNrNode;
import com.tpvision.sensormgt.datamodel.LeafNode;
import com.tpvision.sensormgt.datamodel.MultiInstanceNode;


import android.util.Log;

public class DeviceParser {

	private static final String TAG = "DeviceParser";
	
	private static final boolean DEBUG = true;
	private static final boolean DEBUGMESS = false;
	
	//TODO: get from data model
	private static List<String> MULTI_INSTANCE_NODES = Arrays.asList("SensorCollections","Sensors","SensorURNs","DataItems");
	
	static public void parse(InputStream stream, DatamodelNode tree) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			doc = db.parse(stream); 

			NodeList sensorDevices = doc.getElementsByTagName("SensorCollections"); //one sensordevices node
			if (DEBUGMESS) Log.d(TAG,"SensorCollections: length-"+sensorDevices.getLength());
			
			for (int i=0; i < sensorDevices.getLength(); i++) {
				parseMultiInstance(sensorDevices.item(i), tree);
			}
			
		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Parses multi-instance nodes and creates corresponding objects and values
	 * @param multiInstanceNode node to start parsing from, name of the 
	 * @param multiInstanceName
	 */
	//TODO: assumes correct XML input, make more robust
	static public void parseMultiInstance(Node multiInstanceNode, DatamodelNode tree) {

		String multiInstanceName = multiInstanceNode.getNodeName();
		if (MULTI_INSTANCE_NODES.contains(multiInstanceName)) 
		{
			if (DEBUG) Log.d(TAG,multiInstanceName+" found: "+multiInstanceNode.getNodeName());

			MultiInstanceNode multiInstance = (MultiInstanceNode) tree.addChildNode(new MultiInstanceNode(tree, multiInstanceNode.getNodeName()));
			InstanceNrNode instanceNode = null;

			NodeList instances = multiInstanceNode.getChildNodes();

			for (int i=0; i < instances.getLength(); i++) { //instances
				Node instance = (Node)instances.item(i);
				if (DEBUGMESS) Log.d(TAG,multiInstanceName+" instances: "+ i +" name-"+instance.getNodeName()+", value-"+instance.getNodeValue()+", type-"+instance.getNodeType());
				if (instance.getNodeType()==Node.ELEMENT_NODE) {
					NodeList instanceChildNodes = instance.getChildNodes();
					String inst_id = ((Element) instance).getAttribute("id");
					instanceNode = (InstanceNrNode) multiInstance.addChildNode(new InstanceNrNode(multiInstance, inst_id));

					for (int j=0; j < instanceChildNodes.getLength(); j++) {
						Node item = (Node)instanceChildNodes.item(j);

						if (item.getNodeType()==Node.ELEMENT_NODE) {
							if (DEBUGMESS) Log.d(TAG,"instance child: "+ j +" name-"+item.getNodeName()+", value-"+item.getNodeValue()+", type-"+item.getNodeType());
							String type="";
							//String access="";
							String value="";
							if (MULTI_INSTANCE_NODES.contains(item.getNodeName())) {
								parseMultiInstance(item, instanceNode); //Another level of multiInstance nodes
							} else
								//value = ((Element) item).getTextContent();
							type = ((Element) item).getAttribute("Type");
							if (item.getFirstChild()!=null) {
								if (DEBUG) Log.d(TAG,"Add Key-value of type "+type+" to "+multiInstanceName+": "+item.getNodeName()+"-"+value);
								LeafNode leafnode = new LeafNode(instanceNode, item.getNodeName(), type, item.getFirstChild().getNodeValue());
								instanceNode.addChildNode(leafnode);
								leafnode.setAccess(((Element) item).getAttribute("Access"));
								if (item.getNodeName().contentEquals("Description")) {
									//parseDataItemDescription(item.getFirstChild().getNodeValue());
								}
							}
							else {
								if (DEBUG) Log.d(TAG,"Add Key-value of type "+type+" to "+multiInstanceName+": "+item.getNodeName());
								Log.d(TAG,"Did not get value for leaf node");
								LeafNode leafnode = new LeafNode(instanceNode, item.getNodeName());
								instanceNode.addChildNode(leafnode);
								leafnode.setAccess(((Element) item).getAttribute("Access"));
							}
						}
					}
				}
			} 
		}
	}

}