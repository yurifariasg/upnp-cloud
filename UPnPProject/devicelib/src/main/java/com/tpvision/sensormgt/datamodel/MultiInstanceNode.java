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

import android.util.Log;

public class MultiInstanceNode extends DatamodelNode {

	private static final String TAG = "MultiInstanceNode";
	private static final boolean DEBUG = DebugDatamodel.DEBUG_DATAMODEL;

	private DatamodelNode mParent;
	private String mAccess="readOnly";

	public MultiInstanceNode(DatamodelNode parent, String nodeName) {
		super(parent, nodeName, NodeType.MULTI_INSTANCE);
		mParent = parent;
	}
	
	
	
	@Override
	public DatamodelNode addChildNode(DatamodelNode node) {
		
		super.addChildNode(node);
		if (node.getNodeType()==NodeType.INSTANCE)
			addNumEntries();
		
		return node;
	}

	private void addNumEntries() {
		LeafNode entriesNode = (LeafNode) mParent.getChildNode(mParent.getPath()+getNodeName()+"NumberOfEntries"); 
		
		if (entriesNode == null) {
			entriesNode = new LeafNode(mParent, getNodeName()+"NumberOfEntries", "0");
			mParent.addChildNode(entriesNode);
		}
		entriesNode.setValueType("int");
		entriesNode.setValue(getNumChildren());
	}
	
	public void setAccess(String mAccess) {
		
		if (DEBUG) Log.d(TAG,"SetAccess("+mAccess+")"+ "on node "+ getPath());
			
		this.mAccess = mAccess;
	}
	
	public String getAccess() {
		return mAccess;
	}
	
	public boolean isWritable() {
		return mAccess.contentEquals("readWrite");
	}
	
	public String getValueType() {
		return "MultiInstance";
	}

}