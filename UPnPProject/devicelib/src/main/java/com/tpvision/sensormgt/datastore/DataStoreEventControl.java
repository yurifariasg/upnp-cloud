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

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.tpvision.sensormgt.datastore.LastChangeEventListener;


public class DataStoreEventControl {

	private static final String TAG = "DataStoreEventControl";
	private static final boolean DEBUG = DebugDataStore.DEBUG_EVENTS;
	
	private static final int TIMEOUT = 200; //moderation period
	
	private LastChangeInfo mLastChange;
	private TimerTask mTimerTask;
	private Timer mTimer;

	private LastChangeEventListener mLastChangeEventListener;
	
	public DataStoreEventControl() {
		
		mLastChange = new LastChangeInfo(); 
		mTimer = new Timer();
	}
	
	public void setLastChangeEventListener(LastChangeEventListener lastChangeEventListener) {
		mLastChangeEventListener = lastChangeEventListener;
	}
	
	public void dataTableCreated(DataTableInfo dataTable) {
		
		if (DEBUG) Log.d(TAG,"dataTableCreated event");
		//store pending event
		mLastChange.addCreatedTable(dataTable);
		startModerationPeriod();
		
	}
	
	public void dataTableModified(DataTableInfo dataTable) {
		
		if (DEBUG) Log.d(TAG,"dataTable modified event");
		//store pending event
		mLastChange.addModifiedTable(dataTable);
		startModerationPeriod();
		
	}

	public void dataTableDeleted(DataTableInfo dataTable) { 
		
		if (DEBUG) Log.d(TAG,"dataTableDeleted event");
		//store pending event
		mLastChange.addDeletedTable(dataTable);
		startModerationPeriod();
	}
	
	private void startModerationPeriod() {

		if (DEBUG) Log.d(TAG,"start DataStore event ModerationPeriod");
		
		if (mTimerTask!=null) mTimerTask.cancel();
		mTimerTask = new EventTimerTask();
		mTimer.purge();
		mTimer.schedule(mTimerTask, TIMEOUT);
	}
	
	
	private class EventTimerTask extends TimerTask {          
		@Override
		public void run() {
			
			//create LastChange event
			String event = XMLUPnPDataStoreUtil.createXMLLastChange(mLastChange);
			
			//inform upnp datastore to send event
			if (mLastChangeEventListener!=null) {
				if (DEBUG) Log.d(TAG,"call onLastChangeEvent: "+event);
				mLastChangeEventListener.onLastChangeEvent(event);
			} else {
				Log.e(TAG,"No LastChangeEventListener registered");
			}
			
			//events processed, clear the events
			mLastChange.clearPendingEventInfo(); 
		}
	}
}