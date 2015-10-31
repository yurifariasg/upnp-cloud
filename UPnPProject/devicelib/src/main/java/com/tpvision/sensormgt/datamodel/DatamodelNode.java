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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DatamodelNode {

	private static final boolean TOLERATE_MISSING_SLASH = true;
	protected String mPath; 
	private int mLevel;
	private ArrayList<DatamodelNode> mChildren= new ArrayList<DatamodelNode>();
	private NodeType mNodeType;
	private DatamodelNode mParent;
	private String mNodeName;
	
	public enum NodeType {
	    SINGLE_INSTANCE, MULTI_INSTANCE, LEAF, INSTANCE 
	}
	
	/** 
	 * Create a datamodel node. Sets path, level and node type. 
	 * Path is created by appending nodeName to parent path, level is parent level + 1.
	 * If the parent is null, Path will be "/"+nodeName and level will be 1
	 * If the node is not a leave node, it is checked if the path ends with a "/", if not it is added automatically
	 * @param parent, Parent of this node 
	 * @param NodeName, Name of this node
	 * @param nodeType, Node type of this node
	 */
	protected DatamodelNode(DatamodelNode parent, String nodeName, NodeType nodeType) {
		
		mNodeName = removeSlash(nodeName);
		
		if (parent==null) 
		{
			mPath = "/"+nodeName;
		}
		else
		{
			mPath = parent.getPath()+nodeName;
			mLevel = parent.getLevel()+1;
		}
			
		mNodeType = nodeType;
		if (mNodeType!=NodeType.LEAF) { 
			mPath = appendSlash(mPath);
		}
		
		mParent = parent;
	}
	
	
//	/** 
//	 * Create a datamodel node, path is created by appending nodeName to parent path, level is parent level + 1. Path and multi flag are used to determine NodeType 
//	 * @param path, The path to refer to the node. Paths without a "/" at the end are leaves 
//	 * @param level, The depth level counting from the root of the tree. Used to check depth when performing searches
//	 * @param multi, If set, the node type is set to MULTI_INSTANCE. (No additional checks are performed to verify correctness)
//	 */
//	protected DatamodelNode(DatamodelNode parent, String nodeName, boolean multi) {
//		this(parent.getPath()+nodeName, parent.getLevel()+1, multi);
//	}
//	
//	
//	/** 
//	 * Create a datamodel node, path, and level are set. Path and multi flag are used to determine NodeType 
//	 * @param path, The path to refer to the node. Paths without a "/" at the end are leaves 
//	 * @param level, The depth level counting from the root of the tree. Used to check depth when performing searches
//	 * @param multi, If set, the node type is set to MULTI_INSTANCE. (No additional checks are performed to verify correctness)
//	 */
//	protected DatamodelNode(String path, int level, boolean multi) {
//		mPath = path;
//		mLevel = level;
//		
//		//set node type
//		if (multi) { 
//			mNodeType = NodeType.MULTI_INSTANCE;
//			mPath = appendSlash(mPath);
//		}
//		else
//			if (path.endsWith("/")) mNodeType = NodeType.SINGLE_INSTANCE;
//			else
//				mNodeType = NodeType.LEAF;
//		
//	}
	
	
	private String appendSlash(String path) {
		
		if (path.endsWith("/")) 
			return path;
		else		
			return path+"/";
	}
	
	private String removeSlash(String name) {
		int begin =0;
		int end = name.length();
		if (name.startsWith("/")) begin = 1;
		if (name.endsWith("/")) end = end - 1;
		
		return name.substring(begin, end);
	}
	
	public DatamodelNode clone() {
		return new DatamodelNode(mParent, mNodeName, mNodeType);
	}
	
	
	public DatamodelNode addChildNode(DatamodelNode node) {
		if ((node!=null) && getChildNode(node.getPath()) == null) {
			mChildren.add(node);
		}
		
		return node;
	}
	
	
	/**
	 * Finds a child node according to path name. If the name is not found null is returned 
	 * @param path, the path value of the intended childNode
	 * @return DatamodelNode or null
	 */
	public DatamodelNode getChildNode(String path) {
		
		Iterator<DatamodelNode> it = mChildren.iterator();
		boolean found = false;
		DatamodelNode node=null;
		while(it.hasNext()&&!found)
		{
		    node = it.next();
		    String nodePath = node.getPath(); 
		    found = nodePath.equals(path); 
		    if (TOLERATE_MISSING_SLASH && nodePath.endsWith("/") && !path.endsWith("/")) {
		    	found = ((path.length()+1) == nodePath.length() && nodePath.startsWith(path));  		
		    }
		}
		
		if (found)
			return node;
		else
			return null;
	}
	
	
	public List<DatamodelNode> getChildNodes() {
		return mChildren;
	}
	
	
	public void removeChildNodes() {
		mChildren.clear();
	}
	
	
	public int getNumChildren() {
		return mChildren.size();
	}
	
	
	public String getPath() {
		return mPath;
	}

	
	public int getLevel() {
		return mLevel;
	}
	
	
	public NodeType getNodeType() {
		return mNodeType;
	}

	
	public String getNodeTypeString() {
		String type;

		switch (mNodeType) {
		case LEAF:
			type = "Leaf";
			break;
		case SINGLE_INSTANCE:
			type = "Single Instance";
			break;
		case MULTI_INSTANCE:
			type = "Multi Instance";
			break;
		case INSTANCE:
			type = "Instance";
			break;
		default:
			type = "";
			break;
		}

		return type;
	}
//TODO: abstract	
	public String getValueType() {
		return "";
	}
	
	public String getAccess() {
		return "readOnly";
	}
	
	public String getNodeName() {
		return mNodeName;
	}

	
	public String toString() {
		return mPath;
	}
}
	