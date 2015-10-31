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

import java.util.Arrays;
import java.util.List;

import android.util.Log;

public class Datamodel {

	private static final String TAG = "Datamodel";
	private static final boolean DEBUG = DebugDatamodel.DEBUG_DATAMODEL;
	
	//Value for SupportedDataModelUpdate statevariable. The datamodels will never be updated in this implementation
	static final String SUPPORTED_DATAMODELS_UPDATE = "1,2013-07-30T10:05:30"; 
	//Value for SupportedParametersUpdate statevariable. The datamodel parameters will never be updated in this implementation
	static final String SUPPORTED_PARAMETERS_UPDATE = "1,2013-07-30T10:05:30"; 
	
	static final String DATAMODEL_URI = "urn:upnp-org:smgt:1";
	static final String DATAMODEL_LOCATION = "/UPnP/SensorMgt";
	static final String DATAMODEL_URL = "http://www.upnp.org/specs/smgt/UPnP-smgt-SensorManagement_Data_Model-v1.pdf";
	static final String DATAMODEL_DESCRIPTION = "Sensor Management Datamodel";
	
	public static List<String> mDatamodelDefinition = Arrays.asList(
		"/UPnP/", 
		"/UPnP/SensorMgt/", 
		"/UPnP/SensorMgt/SensorEvents",
		"/UPnP/SensorMgt/SensorCollectionsNumberOfEntries",
		
		"/UPnP/SensorMgt/SensorCollections/#",
		"/UPnP/SensorMgt/SensorCollections/#/CollectionID",
		"/UPnP/SensorMgt/SensorCollections/#/CollectionType",
		"/UPnP/SensorMgt/SensorCollections/#/CollectionFriendlyName",
		"/UPnP/SensorMgt/SensorCollections/#/CollectionInformation",
		"/UPnP/SensorMgt/SensorCollections/#/CollectionUniqueIdentifier",
		"/UPnP//SensorMgt/SensorCollections/#/CollectionSpecific/",
//		"/UPnP/SensorMgt/SensorCollections/#/CollectionSpecific", //hack
		"/UPnP/SensorMgt/SensorCollections/#/SensorsNumberOfEntries",
		
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorID",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorType",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorUpdateRequest",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorEventsEnable", //for eventing
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorPollingInterval",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorReportChangeOnly",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNsNumberOfEntries",
	
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/SensorURN",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/DataItemsNumberOfEntries",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/DataItems/#/",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/DataItems/#/Name",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/DataItems/#/Type",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/DataItems/#/Encoding",
		"/UPnP/SensorMgt/SensorCollections/#/Sensors/#/SensorURNs/#/DataItems/#/Description"
	);
	
	/**
	 * Generates a tree based on the datamodel path list. A tree is constructed from the paths in the datamodelList.
	 * It is assumed that the path list is correct. No checks are performed.  
	 * A tree of nodes is constructed in memory
	 * Paths and levels are assigned to the nodes, where also the multi instance "#" is counted as a level
	 * @param datamodelList
	 */
	static public DatamodelNode inflateDatamodelTree(List<String> datamodelList) {

		DatamodelNode tree = new SingleInstanceNode(null,"UPnP");

		String[] nodesInPath;



		DatamodelNode newNode;
		for (String path: datamodelList) {

			DatamodelNode currentNode = tree;
			nodesInPath = path.split("/");

			int i;
			String partialPath;
			boolean leaf = !path.endsWith("/");
			for (i=1; i < nodesInPath.length; i++) {
				partialPath = DatamodelUtils.createPartialPath(nodesInPath,i, leaf);
				if (!currentNode.getPath().equals(partialPath)) {
					if ((currentNode.getChildNode(partialPath)==null)) {

						if (peekMultiInstance(nodesInPath, i)) 
						{
							newNode = new MultiInstanceNode(currentNode, nodesInPath[i]);
						} else {
							if (leaf && (i==nodesInPath.length-1)) //last node 
								newNode = new LeafNode(currentNode, nodesInPath[i]);
							else
								newNode = new SingleInstanceNode(currentNode, nodesInPath[i]);
						}

						if (currentNode != null) currentNode.addChildNode(newNode);
						currentNode = newNode;
						if (DEBUG) Log.d(TAG,"New node: "+ newNode.getPath() + ", " + newNode.getLevel() + ", " + newNode.getNodeTypeString());
					} else {	
						if (DEBUG) Log.d(TAG,"Existing node: "+ currentNode.getPath() + ", " + currentNode.getLevel() + ", " + currentNode.getNodeTypeString());
						currentNode = currentNode.getChildNode(partialPath);
					}
				}
			}	
		}

		return tree;
	}

	
	
	
	/**
	 * Checks from the nodes list if the current or the next one is a '#'-node
	 * @param nodesInPath, list of node names
	 * @param i, current index
	 * @return true if next is a #, false otherwise
	 */
	static private boolean peekMultiInstance(String[] nodesInPath, int i) {
		if ((i < nodesInPath.length) && (nodesInPath[i].equals("#")))  //check if current node is '#'
			return true;
		if ((i+1) < nodesInPath.length) { //next path available
			return nodesInPath[i+1].equals("#"); //check if next is a # node
		} else
			return false; 
	}
}