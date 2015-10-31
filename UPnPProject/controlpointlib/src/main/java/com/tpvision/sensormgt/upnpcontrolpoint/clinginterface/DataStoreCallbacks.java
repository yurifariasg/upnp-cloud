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

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

import android.util.Log;

import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.CreateDataStoreTable;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.DeleteDataStoreTable;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreGroups;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreInfo;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreTableInfo;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreTableKeyValue;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.GetDataStoreTransportURL;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.ModifyDataStoreTable;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.ReadDataStoreTableRecords;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.RemoveDataStoreTableKeyValue;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.ResetDataStoreTable;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.SetDataStoreTableKeyValue;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.DataStoreCPInterface.WriteDataStoreTableRecords;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.ConnectSensor;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.DisconnectSensor;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.GetSensorTransportConnections;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.ReadSensor;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.SensorTransportGenericCPInterface.WriteSensor;

public class DataStoreCallbacks {
	
	public class GetDataStoreInfoCallback extends ActionCallback {
		private final static String TAG = "GetDataStoreInfoCallback";

		private GetDataStoreInfo mCallback;

		public GetDataStoreInfoCallback(GetDataStoreInfo callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetDataStoreInfo(invocation.getOutput()[0].getValue().toString());
		}
	}

	public class GetDataStoreTableInfoCallback extends ActionCallback {
		private final static String TAG = "GetDataStoreTableInfoCallback";

		private GetDataStoreTableInfo mCallback;

		public GetDataStoreTableInfoCallback(GetDataStoreTableInfo callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetDataStoreTableInfo(invocation.getOutput()[0].getValue().toString());
		}
	}

	
	
	public class CreateDataStoreTableCallback extends ActionCallback {
		private final static String TAG = "CreateDataStoreTableCallback";

		private CreateDataStoreTable mCallback;

		public CreateDataStoreTableCallback(CreateDataStoreTable callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onCreateDataStoreTable(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class ModifyDataStoreTableCallback extends ActionCallback {
		private final static String TAG = "ModifyDataStoreTableCallback";

		private ModifyDataStoreTable mCallback;

		public ModifyDataStoreTableCallback(ModifyDataStoreTable callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onModifyDataStoreTable(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class DeleteDataStoreTableCallback extends ActionCallback {
		private final static String TAG = "DeleteDataStoreTableCallback";

		private DeleteDataStoreTable mCallback;

		public DeleteDataStoreTableCallback(DeleteDataStoreTable callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onDeleteDataStoreTable();
		}
	}
	
	public class ResetDataStoreTableCallback extends ActionCallback {
		private final static String TAG = "ResetDataStoreTableCallback";

		private ResetDataStoreTable mCallback;

		public ResetDataStoreTableCallback(ResetDataStoreTable callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onResetDataStoreTable();
		}
	}
	
	public class GetDataStoreGroupsCallback extends ActionCallback {
		private final static String TAG = "GetDataStoreGroupsCallback";

		private  GetDataStoreGroups mCallback;

		public GetDataStoreGroupsCallback(GetDataStoreGroups callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetDataStoreGroups(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class GetDataStoreTableKeyValueCallback extends ActionCallback {
		private final static String TAG = "GetDataStoreTableKeyValueCallback";

		private GetDataStoreTableKeyValue mCallback;

		public GetDataStoreTableKeyValueCallback(GetDataStoreTableKeyValue callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetDataStoreTableKeyValue(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class SetDataStoreTableKeyValueCallback extends ActionCallback {
		private final static String TAG = "SetDataStoreTableKeyValueCallback";

		private SetDataStoreTableKeyValue mCallback;

		public SetDataStoreTableKeyValueCallback(SetDataStoreTableKeyValue callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onSetDataStoreTableKeyValue();
		}
	}
	
	public class RemoveDataStoreTableKeyValueCallback extends ActionCallback {
		private final static String TAG = "RemoveDataStoreTableKeyValueCallback";

		private RemoveDataStoreTableKeyValue mCallback;

		public RemoveDataStoreTableKeyValueCallback(RemoveDataStoreTableKeyValue callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onRemoveDataStoreTableKeyValue();
		}
	}
	
	public class ReadDataStoreTableRecordsCallback extends ActionCallback {
		private final static String TAG = "ReadDataStoreTableRecords";

		private ReadDataStoreTableRecords mCallback;

		public ReadDataStoreTableRecordsCallback(ReadDataStoreTableRecords callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onReadDataStoreTableRecords(invocation.getOutput()[0].getValue().toString(),invocation.getOutput()[1].getValue().toString());
		}
	}
	
	public class WriteDataStoreTableRecordsCallback extends ActionCallback {
		private final static String TAG = "WriteDataStoreTableRecords";

		private WriteDataStoreTableRecords mCallback;

		public WriteDataStoreTableRecordsCallback(WriteDataStoreTableRecords callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			if (invocation.getOutput()[0].getValue()!=null)
				mCallback.onWriteDataStoreTableRecords(invocation.getOutput()[0].getValue().toString());
			else 
				mCallback.onWriteDataStoreTableRecords("");
		}
	}
	
	public class GetDataStoreTransportURLCallback extends ActionCallback {
		private final static String TAG = "GetDataStoreTransportURLCallback";

		private GetDataStoreTransportURL mCallback;

		public GetDataStoreTransportURLCallback(GetDataStoreTransportURL callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetDataStoreTransportURL(invocation.getOutput()[0].getValue().toString());
		}
	}

	
}