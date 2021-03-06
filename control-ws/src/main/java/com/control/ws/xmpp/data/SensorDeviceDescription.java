/**
 *
 * Copyright 2013-2014 UPnP Forum All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE OR WARRANTIES OF 
 * NON-INFRINGEMENT, ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are 
 * those of the authors and should not be interpreted as representing official 
 * policies, either expressed or implied, by the UPnP Forum.
 *
 **/

package com.control.ws.xmpp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.control.ws.model.Sensor;

public class SensorDeviceDescription extends DeviceDescription {

//	private int numberOfSensors = -1;
	private List<Sensor> sensors;
	
	public SensorDeviceDescription(String jid, String uuid) {
		super(jid, uuid);
		sensors = new ArrayList<Sensor>();
	}

	public SensorDeviceDescription(DeviceDescription desc) {
		this(desc.getJid(),desc.getUuid());
	}

	public int getNumberOfSensors() {
		return sensors.size();
	}

	public void AddSensor(Sensor sensor){
		sensors.add(sensor);
		sensor.setDescription(this);
	}
	public Collection<Sensor> getSensors(){
		return sensors;
	}
	
	@Override
	public String toString() {
		return "[SensorDevice w/ " + sensors.size() + " sensors: " + super.toString() + "]";
	}
}
