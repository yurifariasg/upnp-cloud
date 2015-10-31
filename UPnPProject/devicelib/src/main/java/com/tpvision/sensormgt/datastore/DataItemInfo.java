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

/**
 * Stores data item information. 
 */

public class DataItemInfo {
	
	final private int mTableId;
	final private int mId;
	final private String mFieldName;
	final private String mFieldType;
	final private String mEncoding;
	final private boolean mRequired;
	final private String mNamespace;
	final private boolean mTableProp;
	
	private String mValue;
	private String mReceivedTime;
	
	
	public DataItemInfo(String fieldName, String fieldType, String encoding, boolean required, String namespace, boolean tableprop) {
		this(0, 0,fieldName, fieldType, encoding, required, namespace, tableprop);
	}	
	
	public DataItemInfo(int id, int tableId, String fieldName, String fieldType, String encoding, boolean required, String namespace, boolean tableprop) {
		  mId = id;
		  mTableId = tableId;
		  mFieldName = fieldName;
		  mFieldType = fieldType;
		  mEncoding = encoding;
		  mRequired = required;
		  mNamespace = namespace;
		  mTableProp = tableprop;
	}
	
	public DataItemInfo copy() {
		return new DataItemInfo(mId, mTableId, mFieldName, mFieldType, mEncoding, mRequired, mNamespace, mTableProp);
	}

	public int getIndexId() {
		return mId;
	}
	
	public int getTableId() {
		return mTableId;
	}
	
	public String getFieldName() {
		if (mFieldName!=null)
			return mFieldName;
		return "";
	}

	public String getFieldType() {
		if (mFieldType!=null)
			return mFieldType;
		return "";
	}

	public String getEncoding() {
		if (mEncoding!=null)
			return mEncoding;
		return "";
	}

	public boolean isRequired() {
		return mRequired;
	}

	public String getNamespace() {
		if (mNamespace!=null)
			return mNamespace;
		return "";
	}

	public boolean isTableProp() {
		return mTableProp;
	}

	public void setValue(String value) {
		mValue = value;
	}
	
	public String getValue() {
		if (mValue!=null)
			return mValue;
		return "";
	}
	
	public void setReceivedTime(String receivedTime) {
		mReceivedTime = receivedTime;
	}
	
	public String getReceivedTime() {
		if (mReceivedTime!=null)
			return mReceivedTime;
		return "";
	}
	
}