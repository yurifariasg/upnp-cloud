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

import org.apache.commons.lang3.StringEscapeUtils;

import android.util.Log;


public class LeafNode extends DatamodelNode {

	private static final String TAG = "LeafNode";
	private static final boolean DEBUG = DebugDatamodel.DEBUG_DATAMODEL;
	
	private String mValue;
	private String mType=null;
	private String mAccess="readOnly";
	private boolean mEOC=false;
	private boolean mHasEOC=false;
	private int mVersion = 0; 
	private boolean mHasVersion=false;
	
	private ArrayList<SensorChangeListener> mValueListeners = new ArrayList<SensorChangeListener>();
	private boolean mHasAOC=false;
	private int mAOC;
	
	/**
	 * Creates leaf node, setting is value, defaulting to sting type
	 * @param parent
	 * @param nodeName
	 * @param type
	 * @param value
	 */
	public LeafNode(DatamodelNode parent, String nodeName, String type, String value) {
		this(parent, nodeName);
		mType=type;
		if (value.startsWith("&lt;")) {
			mValue = StringEscapeUtils.unescapeXml(value);
		} else {
			mValue = value;	
		}
	}
	
	/**
	 * Creates leaf node, setting is value, defaulting to sting type
	 * @param parent
	 * @param nodeName
	 * @param value
	 */
	public LeafNode(DatamodelNode parent, String nodeName, String value) {
		this(parent, nodeName);
		
		mValue = value;
		setValueType("string");
	}
	
	/**
	 * Creates leaf node, setting type to string by default, its value is null
	 * @param parent
	 * @param nodeName
	 */
	public LeafNode(DatamodelNode parent, String nodeName) {
		super(parent, nodeName, NodeType.LEAF);
	}

	public void addOnParameterValueChangedListener(SensorChangeListener sensorListener) {
		mValueListeners.add(sensorListener);
	}
	
	//TODO: test it
	public void remoteOnParameterValueChangedListener(SensorChangeListener sensorListener) {
		mValueListeners.remove(sensorListener);
	}
	
	//sends an event to every registered listener
	private void parameterValueChanged() {
		Iterator<SensorChangeListener> parameterListenerIterator = mValueListeners.iterator();
		while (parameterListenerIterator.hasNext()) {
			ParameterEventListener parameterListener = (ParameterEventListener) parameterListenerIterator.next();
			parameterListener.onParameterValueChanged(this);
		}	
	}
	
	public String getValue() {
		if (mValue==null)
			return "";
		else
			return mValue;
	}
	
	public int getIntValue() {
		return Integer.parseInt(mValue);
	}
	
	public boolean getBoolValue() {
		return Boolean.valueOf(mValue);
	}
	
	public void setValue(String value) {
		if (value!=null) {

			if ((mValue==null) || !value.contentEquals(mValue))
			{
				if (value.startsWith("&lt;")) {
					Log.w(TAG,"Unescaping XML passed in setValue");
					mValue = StringEscapeUtils.unescapeXml(value);
				}
				else {
					mValue = value;
				}
					
				if (DEBUG) Log.d(TAG, "set value: "+ value +" on " + getNodeName());
				parameterValueChanged();
			}
		} else {
			Log.i(TAG, "set null value, on " + getNodeName());
		}
	}
	
	public void setValue(int value) {
		
		if (DEBUG) Log.d(TAG, "set value: "+ value +" on " + getNodeName());
		mValue = Integer.toString(value);
	}
	
	public void setValue(boolean value) {
		
		if (DEBUG) Log.d(TAG, "set value: "+ value +" on " + getNodeName());
		mValue = Boolean.toString(value);
	}
	
	public String toString() {
		if (getValue().length() > 30)
			return super.mPath+" = "+getValue().substring(0, 30); //limit value output to 30 chars
		else 
			return super.mPath+" = "+getValue();
	}

	public void setAccess(String mAccess) {
		if (DEBUG) Log.d(TAG, "setAcess("+mAccess+")");
		Log.d(TAG, "setAcess("+mAccess+")");
		this.mAccess = mAccess;
	}
	
	public String getAccess() {
		return mAccess;
	}
	
	public boolean isWritable() {
		return mAccess.contentEquals("readWrite");
	}

	public boolean hasEOCAttribute() {
		return mHasEOC;
	}
	
	public void setHasEOCAttribute(boolean hasEOC) {
		mHasEOC = hasEOC;
	}
	
	public boolean isEOC() {
		return mHasEOC && mEOC;
	}

	public void setEOC(boolean mEOC) {
		if (DEBUG) Log.d(TAG, "setEOC("+mEOC+")");
		this.mEOC = mEOC;
	}

	public int getVersion() {
		return mVersion;
	}

	public void setVersion(int mVersion) {
		this.mVersion = mVersion;
	}

	public String getValueType() {
		return mType;
	}

	public void setValueType(String mType) {
		this.mType = mType;
	}

	public boolean hasVersion() {
		return mHasVersion;
	}

	public void setHasVersion(boolean mHasVersion) {
		this.mHasVersion = mHasVersion;
	}

	public boolean hasAOC() {
		return mHasAOC;
	}

	public void setHasAOC(boolean hasAOC) {
		this.mHasAOC = hasAOC;
	}

	public int getAOC() {
		return mAOC;
	}

	public void setAOC(int aoc) {
		this.mAOC = aoc;
	}
	
	
}