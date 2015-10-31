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

import java.util.LinkedList;
import java.util.Queue;

public class DatamodelUtils {

	/**
	 * Prints the tree to a String. Values are truncated to maximum 30 chars  
	 * @param tree, starting node to print from (can be any node in the tree)
	 * @param depth, number of levels to print 
	 * @return String
	 */
	static public String treeToString(DatamodelNode tree, int depth)
	{
		StringBuffer treeText = new StringBuffer();

		if (tree == null) return "Empty";

		/* Create a queue to hold node pointers. */
		Queue<DatamodelNode> queue =new LinkedList<DatamodelNode>();
		queue.add(tree); 
		
		int initialLevel = tree.getLevel();
		DatamodelNode traverse;
		while (!queue.isEmpty()) {

			traverse = queue.remove();  

			if ((depth==0) || (depth >= (traverse.getLevel() - initialLevel))) {
				treeText.append(traverse.toString()+"\n");
				//if (DEBUG) Log.d(TAG, "node :" + traverse.getPath()+ ", " +traverse.getNodeTypeString());
			}

			queue.addAll(traverse.getChildNodes());
		}

		/* Clean up the queue. */
		queue=null;
		return treeText.toString();
	}
	
	
	/**
	 * Uses a list of node names in path to construct a partial path consisting of n segments
	 * The first entry in the list is ignored
	 * Since the node names do not contain slashes to allow for detecting the node type automatically, leaf indicates if the path is a leaf node.   
	 * @param nodesInPath
	 * @param n
	 * @param leaf
	 * @return
	 */
	static public String createPartialPath(String[] nodesInPath, int n, boolean leaf) {
		StringBuffer partialPath = new StringBuffer("/");
		
		int i;
		for (i=1; (i<=n) && (i < nodesInPath.length); i++) {
			if (!nodesInPath[i].isEmpty()) {
				partialPath.append(nodesInPath[i]);
				if (i != (nodesInPath.length-1))
					partialPath.append('/'); //not the last one
				else
					if (!leaf) partialPath.append('/'); //last one, but not a leaf
			}
		}
		
		return partialPath.toString();
	}
}