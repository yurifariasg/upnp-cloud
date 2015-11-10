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

package com.comarch.android.upnp.ibcdemo.sensormanagement;

import android.util.Log;

import com.tpvision.sensormgt.datamodel.DataItem;

import java.util.Timer;
import java.util.TimerTask;


public class TemperatureSensor extends DataItem {

	protected static final String TAG = "TemperatureSensor";
	protected static final boolean DEBUG = false;
	
	private Timer mTimer;
	private long PERIOD=20000; //number of millis between updates
	private int min=60; //tenth of a degree celcius
	private int max=80;
	private long mUpdatePeriod = PERIOD;


	public TemperatureSensor(String collectionID, String sensorID, String sensorURN, String name) {
		super(collectionID, sensorID, sensorURN, name);

		setTimer();
	}

	public void setAvgTemp(int temp)
	{
		min = temp*10-10;
		max = temp*10+10;
	}
	
	synchronized public void setUpdatePeriod(long period) {
		mUpdatePeriod  = period*1000; 
		setTimer();
	}

	private void setTimer() {
		if (mTimer!=null) mTimer.cancel();
		if (mUpdatePeriod!=0) { 
			mTimer = new Timer();
			mUpdatePeriod = mUpdatePeriod + (int)(Math.random() * 100); //create some delay between individual sensors
			mTimer.scheduleAtFixedRate(new TimerTask() {          
				@Override
				public void run() {

					String temp = Float.toString(((float)(min + (int)(Math.random() * ((max - min) + 1))))/10);
					setSensorValue(temp);
					if (DEBUG) Log.d(TAG,"TEMP: "+ temp);
				}

			}, 0, mUpdatePeriod);
		}
	}
	
}