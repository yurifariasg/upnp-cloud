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

import java.util.ArrayList;

/**
 * Stores data table information. 
 */

public class DataTableInfo {
	
	private String mURN;
	private int mUpdateID;
	private DataItemInfo[] mDataItemInfoList;
	private int mTableID = -1;
	
	public enum ModificationType {
		records, properties, groups, reset, other
	};
	
	private boolean modTypeRecords = false;
	private boolean modTypeProperties = false;
	private boolean modTypeGroups = false;
	private boolean modTypeReset = false;
	private boolean modTypeOther = false;
	
	public DataTableInfo(String urn, int updateID, ArrayList<DataItemInfo> dataItemInfoList) {
		mURN = urn;
		mUpdateID = updateID;
		if (dataItemInfoList!= null) mDataItemInfoList =  (DataItemInfo[]) dataItemInfoList.toArray(new DataItemInfo[0]);
	}
	
	public DataTableInfo(int id, String urn, int updateID) {
		mTableID = id;
		mURN = urn;
		mUpdateID = updateID;
	}
	
	public int getTableID() {
		return mTableID;
	}
	
	public void setTableID(int tableID) {
		mTableID = tableID;
	}

	public String getURN() {
		if (mURN!=null)
			return mURN;
		else
			return "";
	}

	public int getUpdateID() {
			return mUpdateID;
	}

	public void incUpdateID() {
		mUpdateID++;
	}
	
	public DataItemInfo[] getDataItemInfoList() {
		return mDataItemInfoList;
	}
	
	public void setDataItemInfoList(ArrayList<DataItemInfo> dataItemInfoList) {
		if (dataItemInfoList!= null) mDataItemInfoList = (DataItemInfo[]) dataItemInfoList.toArray(new DataItemInfo[0]);
	}
	
	public void setDataItemInfoList(DataItemInfo[] dataItemInfoList) {
		mDataItemInfoList = dataItemInfoList;
	}
	
	public void registerModification(ModificationType modificationType) {
		
		switch (modificationType) {
			case records:
				modTypeRecords = true;
				break;
			case properties:
				modTypeProperties = true;
				break;
			case groups:
				modTypeGroups = true;
				break;
			case reset:
				modTypeReset = true;
				break;
			case other: 
				modTypeOther = true;
				break;
		}
	}
	
	public String getModifications() {
		String modifications="";
		boolean first=true; 
		if (modTypeRecords) {
			modifications += "R";
			first = first & false;
		}
		if (modTypeProperties) {
			if (!first) modifications += ",";
			modifications += "P";
			first = first & false;
		}
		if (modTypeGroups) {
			if (!first) modifications += ",";
			modifications += "G";
			first = first & false;
		}
		if (modTypeReset) {
			if (!first) modifications += ",";
			modifications += "X";
			first = first & false;
		}
		if (modTypeOther) {
			if (!first) modifications += ",";
			modifications += "O";
			first = first & false;
		}
		
		return modifications;
	}
	
}