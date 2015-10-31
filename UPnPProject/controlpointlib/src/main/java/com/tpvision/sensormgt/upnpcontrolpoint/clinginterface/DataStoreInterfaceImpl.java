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

package com.tpvision.sensormgt.upnpcontrolpoint.clinginterface;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;



import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.CreateDataStoreTableCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.DeleteDataStoreTableCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.GetDataStoreGroupsCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.GetDataStoreInfoCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.GetDataStoreTableInfoCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.GetDataStoreTableKeyValueCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.GetDataStoreTransportURLCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.ModifyDataStoreTableCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.ReadDataStoreTableRecordsCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.RemoveDataStoreTableKeyValueCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.ResetDataStoreTableCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.SetDataStoreTableKeyValueCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.DataStoreCallbacks.WriteDataStoreTableRecordsCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.ConnectSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.DisconnectSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.GetSensorTransportConnectionsCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.ReadSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.SensorTransportGenericCallbacks.WriteSensorCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreInfo;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreTableInfo;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreTransportURL;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface;


public class DataStoreInterfaceImpl implements DataStoreCPInterface {

	
	private static final String ACTION_GETDSINFO = "GetDataStoreInfo";
	private static final String ACTION_GETDSTABLEINFO = "GetDataStoreTableInfo";
	
	private static final String ACTION_CREATEDSTABLE = "CreateDataStoreTable";
	private static final String ACTION_MODIFYDSTABLE = "ModifyDataStoreTable";
	private static final String ACTION_DELETEDSTABLE = "DeleteDataStoreTable";
	private static final String ACTION_RESETDSTABLE = "ResetDataStoreTable";
	
	private static final String ACTION_GETDSGROUPS = "GetDataStoreGroups";
	
	private static final String ACTION_GETDSTABLEKEYVALUE = "GetDataStoreTableKeyValue";
	private static final String ACTION_SETDSTABLEKEYVALUE = "SetDataStoreTableKeyValue";
	private static final String ACTION_REMOVEDSTABLEKEYVALUE = "RemoveDataStoreTableKeyValue";
	
	private static final String ACTION_READDSTABLERECS = "ReadDataStoreTableRecords";
	private static final String ACTION_WRITEDSTABLERECS = "WriteDataStoreTableRecords";

	private static final String ACTION_GETDSTRANSPORTURL = "GetDataStoreTransportURL";
	
	private Service<?, ?> mService;
	private AndroidUpnpService mAndroidUpnpService;
	private DataStoreCallbacks mDataStoreCallbacks;
	private SerialActionExecutor mSerialExecutor;
	
	public DataStoreInterfaceImpl(SerialActionExecutor executor, AndroidUpnpService androidUpnpService, Service<?, ?> dataStoreService) {
		mAndroidUpnpService = androidUpnpService;
		mService = dataStoreService;
		mDataStoreCallbacks = new DataStoreCallbacks();
		mSerialExecutor = executor;
	}
	
	@Override
	public void getDataStoreInfo(GetDataStoreInfo callback) {
		
		Action lAction = mService.getAction(ACTION_GETDSINFO);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			

			GetDataStoreInfoCallback lCallback = mDataStoreCallbacks.new GetDataStoreInfoCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}

	@Override
	public void getDataStoreTableInfo(String dataTableID, GetDataStoreTableInfo callback) {
		
		Action lAction = mService.getAction(ACTION_GETDSTABLEINFO);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", dataTableID);

			GetDataStoreTableInfoCallback lCallback = mDataStoreCallbacks.new GetDataStoreTableInfoCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}

	@Override
	public void createDataStoreTable(String dataTableInfo, CreateDataStoreTable callback) {
		
		Action lAction = mService.getAction(ACTION_CREATEDSTABLE);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableInfo", dataTableInfo);

			CreateDataStoreTableCallback lCallback = mDataStoreCallbacks.new CreateDataStoreTableCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}

	@Override
	public void modifyDataStoreTable(String dataTableInfo, String infoElementOrg, String infoElementNew, ModifyDataStoreTable callback) {
		
		Action lAction = mService.getAction(ACTION_MODIFYDSTABLE);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableInfo", dataTableInfo);
			lInvocation.setInput("DataTableInfoElementOrig", infoElementOrg);
			lInvocation.setInput("DataTableInfoElementNew", infoElementNew);

			ModifyDataStoreTableCallback lCallback = mDataStoreCallbacks.new ModifyDataStoreTableCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}
	
	@Override
	public void deleteDataStoreTable(String dataTableID, DeleteDataStoreTable callback) {
		
		Action lAction = mService.getAction(ACTION_DELETEDSTABLE);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", dataTableID);

			DeleteDataStoreTableCallback lCallback = mDataStoreCallbacks.new DeleteDataStoreTableCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}
	
	@Override
	public void resetDataStoreTable(String dataTableID, boolean resetRecords, boolean resetDictionary, boolean resetTransport, ResetDataStoreTable callback) {
		
		Action lAction = mService.getAction(ACTION_RESETDSTABLE);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", dataTableID);
			lInvocation.setInput("ResetDataTableRecords", resetRecords);
			lInvocation.setInput("ResetDataTableDictionary", resetDictionary);
			lInvocation.setInput("ResetDataTableTransport", resetTransport);			

			ResetDataStoreTableCallback lCallback = mDataStoreCallbacks.new ResetDataStoreTableCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}
	
	
	@Override
	public void getDataStoreGroups(GetDataStoreGroups callback) {
		
		Action lAction = mService.getAction(ACTION_GETDSGROUPS);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
		
			GetDataStoreGroupsCallback lCallback = mDataStoreCallbacks.new GetDataStoreGroupsCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}
	
	@Override
	public void getDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, GetDataStoreTableKeyValue callback) {
		
		Action lAction = mService.getAction(ACTION_GETDSTABLEKEYVALUE);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", dataTableID);
			lInvocation.setInput("DataTableKeyName", dataTableKeyName);

			GetDataStoreTableKeyValueCallback lCallback = mDataStoreCallbacks.new GetDataStoreTableKeyValueCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}
	
	@Override
	public void setDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, String dataTableKeyValue, SetDataStoreTableKeyValue callback) {
		
		Action lAction = mService.getAction(ACTION_SETDSTABLEKEYVALUE);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", dataTableID);
			lInvocation.setInput("DataTableKeyName", dataTableKeyName);
			lInvocation.setInput("DataTableKeyValue", dataTableKeyValue);
			

			SetDataStoreTableKeyValueCallback lCallback = mDataStoreCallbacks.new SetDataStoreTableKeyValueCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}
	
	@Override
	public void removeDataStoreTableKeyValue(String dataTableID, String dataTableKeyName, RemoveDataStoreTableKeyValue callback) {
		
		Action lAction = mService.getAction(ACTION_REMOVEDSTABLEKEYVALUE);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", dataTableID);
			lInvocation.setInput("DataTableKeyName", dataTableKeyName);

			RemoveDataStoreTableKeyValueCallback lCallback = mDataStoreCallbacks.new RemoveDataStoreTableKeyValueCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
		
	}

	
	@Override
	public void readDataStoreTableRecords(String tableID, String dataRecordFilter,
			String dataRecordStart, int dataRecordCount, boolean dataRecordPropResolve, ReadDataStoreTableRecords callback) {
		
		Action lAction = mService.getAction(ACTION_READDSTABLERECS);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", tableID);
			lInvocation.setInput("DataRecordFilter", dataRecordFilter);
			lInvocation.setInput("DataRecordStart", dataRecordStart);
			lInvocation.setInput("DataRecordCount", new UnsignedIntegerFourBytes(dataRecordCount));
			lInvocation.setInput("DataRecordPropResolve", dataRecordPropResolve);
			

			ReadDataStoreTableRecordsCallback lCallback = mDataStoreCallbacks.new ReadDataStoreTableRecordsCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
	}
	
	@Override
	public void writeDataStoreTableRecords(String tableID, String dataRecords, WriteDataStoreTableRecords callback) {
		
		Action lAction = mService.getAction(ACTION_WRITEDSTABLERECS);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", tableID);
			lInvocation.setInput("DataRecords", dataRecords);

			WriteDataStoreTableRecordsCallback lCallback = mDataStoreCallbacks.new WriteDataStoreTableRecordsCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
	}
	
	
	@Override
	public void getDataStoreTransportURL(String tableID, GetDataStoreTransportURL callback) {
		
		Action lAction = mService.getAction(ACTION_GETDSTRANSPORTURL);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);
			
			lInvocation.setInput("DataTableID", tableID);

			GetDataStoreTransportURLCallback lCallback = mDataStoreCallbacks.new GetDataStoreTransportURLCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
			mSerialExecutor.execute(lCallback);
			//mAndroidUpnpService.getControlPoint().execute(lCallback);
		}
	}
	
	
	
	
	
}