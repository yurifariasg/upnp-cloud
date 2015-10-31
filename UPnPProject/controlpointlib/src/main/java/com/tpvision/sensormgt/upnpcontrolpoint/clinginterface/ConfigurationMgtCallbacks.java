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

import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetAttributes;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetConfigurationUpdate;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetCurrentConfigurationVersion;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetInstances;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetSupportedDatamodels;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetSupportedParameters;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.GetValues;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.SetAttributes;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface.SetValues;

public class ConfigurationMgtCallbacks {
	
	public class GetSupportedDatamodelsCallback extends ActionCallback {
		private final static String TAG = "GetSupportedDatamodelsCallback";

		private GetSupportedDatamodels mCallback;

		public GetSupportedDatamodelsCallback(GetSupportedDatamodels callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onSupportedDatamodels(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class GetSupportedParametersCallback extends ActionCallback {
		private final static String TAG = "GetSupportedParametersCallback";

		private GetSupportedParameters mCallback;

		public GetSupportedParametersCallback(GetSupportedParameters callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetSupportedParameters(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class GetInstancesCallback extends ActionCallback {
		private final static String TAG = "GetInstancesCallback";

		private GetInstances mCallback;

		public GetInstancesCallback(GetInstances callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetInstances(invocation.getOutput()[0].getValue().toString());
		}
	}

	public class GetValuesCallback extends ActionCallback {
		private final static String TAG = "GetValuesCallback";

		private GetValues mCallback;

		public GetValuesCallback(GetValues callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetValues(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class SetValuesCallback extends ActionCallback {
		private final static String TAG = "SetValuesCallback";

		private SetValues mCallback;

		public SetValuesCallback(SetValues callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onSetValues(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class GetAttributesCallback extends ActionCallback {
		private final static String TAG = "GetAttributesCallback";

		private GetAttributes mCallback;

		public GetAttributesCallback(GetAttributes callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetAttributes(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class SetAttributesCallback extends ActionCallback {
		private final static String TAG = "SetAttributesCallback";

		private SetAttributes mCallback;

		public SetAttributesCallback(SetAttributes callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onSetAttributes(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class GetConfigurationUpdateCallback extends ActionCallback {
		private final static String TAG = "GetConfigurationUpdateCallback";

		private GetConfigurationUpdate mCallback;

		public GetConfigurationUpdateCallback(GetConfigurationUpdate callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetConfigurationUpdate(invocation.getOutput()[0].getValue().toString());
		}
	}
	
	public class GetCurrentConfigurationVersionCallback extends ActionCallback {
		private final static String TAG = "GetCurrentConfigurationVersionCallback";

		private GetCurrentConfigurationVersion mCallback;

		public GetCurrentConfigurationVersionCallback(GetCurrentConfigurationVersion callback, ActionInvocation actionInvocation, ControlPoint controlPoint) {
			super(actionInvocation, controlPoint);
			mCallback = callback;
		}

		@Override
		synchronized public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			Log.e(TAG, defaultMsg);
		}

		@Override
		synchronized public void success(ActionInvocation invocation) {
			mCallback.onGetCurrentConfigurationVersion(invocation.getOutput()[0].getValue().toString());
		}
	}
	
}