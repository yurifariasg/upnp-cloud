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

public class AlarmSensor extends DataItem {

	protected static final String TAG = "AlarmSensor";
	protected static final boolean DEBUG = false;
	
	private Timer mTimer;
	private long mTimeOut=60000; //alarm will go off in one minute


	public AlarmSensor(String collectionID, String sensorID, String sensorURN, String name) {
		super(collectionID, sensorID, sensorURN, name);

	}

	public void setAlarm() {
		mTimer = new Timer();
		mTimer. schedule(new TimerTask() {          
			@Override
			public void run() {
				if (DEBUG) Log.d(TAG,"ALARM TRIGGERED");
				
				setSensorValue("1");
			}

		}, mTimeOut);
	}
	
	public void resetAlarm() {
		if (mTimer!=null) mTimer.cancel();
		
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {          
			@Override
			public void run() {
				if (DEBUG) Log.d(TAG,"ALARM CANCELLED");
				
				setSensorValue("0");
			}

		}, 500); //500ms delay on cancelling alarm. 
	}
	
	public void setTimeOut(long time) {
		mTimeOut = time*1000;
	}

}