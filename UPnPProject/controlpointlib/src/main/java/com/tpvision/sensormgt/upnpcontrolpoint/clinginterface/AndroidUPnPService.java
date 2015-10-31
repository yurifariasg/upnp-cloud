/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.tpvision.sensormgt.upnpcontrolpoint.clinginterface;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.RegistryListener;

public class AndroidUPnPService extends AndroidUpnpServiceImpl {
	public static final String TYPE_CONFIGURATIONMANAGEMENT = "ConfigurationManagement";
	public static final String TYPE_SENSORTRANSPORTGENERIC = "SensorTransportGeneric";
	public static final String TYPE_DATASTORE = "DataStore";
	
	private static final int INTERVAL = 10000;
	private static final boolean DISCOVERALL=true;
	
	@Override
	protected UpnpServiceConfiguration createConfiguration() {
		return new AndroidUpnpServiceConfiguration() {

			

			@Override
			public int getRegistryMaintenanceIntervalMillis() {
				return INTERVAL;
			}

			@Override
			public ServiceType[] getExclusiveServiceTypes() {
				if (DISCOVERALL)
					return new ServiceType[] {};
				else
					return new ServiceType[] { new UDAServiceType(TYPE_CONFIGURATIONMANAGEMENT), 
						new UDAServiceType(TYPE_SENSORTRANSPORTGENERIC), 
						new UDAServiceType(TYPE_DATASTORE)};
			}
		};
	}
}
