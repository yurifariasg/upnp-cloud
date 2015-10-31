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

package com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface;

public interface ConfigurationManagementCPInterface {
	
	public String getSupportedDatamodels(GetSupportedDatamodels callback);
	
	public String getSupportedParameters(String startingNodePath, int searchDepth, GetSupportedParameters callback);
	
	public String getInstances(String startingNodePath, int searchDepth, GetInstances callback);
	
	public String getValues(String parameters, GetValues callback);
	
	public String setValues(String parametersValues, SetValues callback);
	
	public String getAttributes(String attributePaths, GetAttributes callback);
	
	public String setAttributes(String nodeAttrValueList, SetAttributes callback);
	
	public String getConfigurationUpdate(GetConfigurationUpdate callback);
	
	public String getCurrentConfigurationVersion(GetCurrentConfigurationVersion callback);
	
	
	public interface GetSupportedDatamodels {	
		public void onSupportedDatamodels(String datamodelsXml);
	}
	
	public interface GetSupportedParameters {
		public void onGetSupportedParameters(String parametersXml);
	}
		
	public interface GetInstances {	
		public void onGetInstances(String instancesXml);
	}

	public interface GetValues {
		public void onGetValues(String valuesXml);
	}

	public interface SetValues {
		public void onSetValues(String status);
	}
	
	public interface GetAttributes {
		public void onGetAttributes(String attributeValuesXml);
	}
	
	public interface SetAttributes {
		public void onSetAttributes(String status);
	}
	
	public interface GetConfigurationUpdate {
		public void onGetConfigurationUpdate(String configurationUpdate);
	}
	
	public interface GetCurrentConfigurationVersion {
		public void onGetCurrentConfigurationVersion(String version);
	}
}