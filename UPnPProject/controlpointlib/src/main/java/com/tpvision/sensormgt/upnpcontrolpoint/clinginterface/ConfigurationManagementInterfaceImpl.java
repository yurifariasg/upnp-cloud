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
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;



import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.GetAttributesCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.GetConfigurationUpdateCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.GetCurrentConfigurationVersionCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.GetInstancesCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.GetSupportedDatamodelsCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.GetSupportedParametersCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.GetValuesCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.SetAttributesCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.clinginterface.ConfigurationMgtCallbacks.SetValuesCallback;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.ConfigurationManagementCPInterface;


public class ConfigurationManagementInterfaceImpl implements ConfigurationManagementCPInterface {

	
	private static final String ACTION_GETSUPPORTEDDATAMODELS = "GetSupportedDataModels";
	private static final String ACTION_GETSUPPORTEDPARAMETERS = "GetSupportedParameters";
	private static final String ACTION_GETINSTANCES = "GetInstances";
	private static final String ACTION_GETVALUES = "GetValues";
	private static final String ACTION_SETVALUES = "SetValues";
	private static final String ACTION_GETATTRIBUTES = "GetAttributes";
	private static final String ACTION_SETATTRIBUTES = "SetAttributes";
	private static final String ACTION_GETCONFIGURATIONUPD = "getConfigurationUpdate";	
	
	
	private Service<?, ?> mService;
	private AndroidUpnpService mAndroidUpnpService;
	private ConfigurationMgtCallbacks mConfigurationMgtCallbacks;
	private SerialActionExecutor mSerialExecutor;
	
	public ConfigurationManagementInterfaceImpl(SerialActionExecutor executor, AndroidUpnpService androidUpnpService, Service<?, ?> confMgtService) {
		mAndroidUpnpService = androidUpnpService;
		mService = confMgtService;
		mConfigurationMgtCallbacks = new ConfigurationMgtCallbacks();
		mSerialExecutor = executor;
	}
	
	synchronized public String getSupportedDatamodels(GetSupportedDatamodels callback) {
		Action lAction = mService.getAction(ACTION_GETSUPPORTEDDATAMODELS);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			if (callback!=null) {
				GetSupportedDatamodelsCallback lCallback = mConfigurationMgtCallbacks.new GetSupportedDatamodelsCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();
				
				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp
				
				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	synchronized public String getSupportedParameters(String startingNodePath, int searchDepth, GetSupportedParameters callback) {
		Action lAction = mService.getAction(ACTION_GETSUPPORTEDPARAMETERS);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			lInvocation.setInput("StartingNode", startingNodePath);
			lInvocation.setInput("SearchDepth", new UnsignedIntegerFourBytes(searchDepth));

			if (callback!=null) {
				GetSupportedParametersCallback lCallback = mConfigurationMgtCallbacks.new GetSupportedParametersCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	synchronized public String getInstances(String startingNodePath, int searchDepth, GetInstances callback) {
		Action lAction = mService.getAction(ACTION_GETINSTANCES);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			lInvocation.setInput("StartingNode", startingNodePath);
			lInvocation.setInput("SearchDepth", new UnsignedIntegerFourBytes(searchDepth));

			if (callback!=null) {
				GetInstancesCallback lCallback = mConfigurationMgtCallbacks.new GetInstancesCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			}
			else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	@Override
	synchronized public String getValues(String parameters, GetValues callback) {
		Action lAction = mService.getAction(ACTION_GETVALUES);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			lInvocation.setInput("Parameters", parameters);

			if (callback!=null) {
				GetValuesCallback lCallback = mConfigurationMgtCallbacks.new GetValuesCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			}
			else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	@Override
	synchronized public String setValues(String parametersValues, SetValues callback) {
		Action lAction = mService.getAction(ACTION_SETVALUES);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			lInvocation.setInput("ParameterValueList", parametersValues);

			if (callback!=null) {
				SetValuesCallback lCallback = mConfigurationMgtCallbacks.new SetValuesCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	@Override
	synchronized public String getAttributes(String attributePaths, GetAttributes callback) {
		Action lAction = mService.getAction(ACTION_GETATTRIBUTES);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			lInvocation.setInput("Parameters", attributePaths);

			if (callback!=null) {
				GetAttributesCallback lCallback = mConfigurationMgtCallbacks.new GetAttributesCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	@Override
	synchronized public String setAttributes(String nodeAttrValueList, SetAttributes callback) {
		Action lAction = mService.getAction(ACTION_SETATTRIBUTES);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			lInvocation.setInput("NodeAttributeValueList", nodeAttrValueList);

			if (callback!=null) {
				SetAttributesCallback lCallback = mConfigurationMgtCallbacks.new SetAttributesCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	@Override
	synchronized public String getConfigurationUpdate(GetConfigurationUpdate callback) {
		Action lAction = mService.getAction(ACTION_GETCONFIGURATIONUPD);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			if (callback!=null) {
				GetConfigurationUpdateCallback lCallback = mConfigurationMgtCallbacks.new GetConfigurationUpdateCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	@Override
	synchronized public String getCurrentConfigurationVersion(
			GetCurrentConfigurationVersion callback) {
		Action lAction = mService.getAction(ACTION_GETCONFIGURATIONUPD);

		if (lAction != null) {
			ActionInvocation lInvocation = new ActionInvocation(lAction);

			if (callback!=null) {
				GetCurrentConfigurationVersionCallback lCallback = mConfigurationMgtCallbacks.new GetCurrentConfigurationVersionCallback(callback, lInvocation, mAndroidUpnpService.getControlPoint());
				mSerialExecutor.execute(lCallback);
			} else {
				ActionCallback actionCallback = new ActionCallback.Default(lInvocation, mAndroidUpnpService.getControlPoint());
				actionCallback.run();

				ActionException actionException = lInvocation.getFailure(); 
				//TODO:throw upnp

				return lInvocation.getOutput()[0].getValue().toString();
			}	
		}
		return null;
	}

	
	
}