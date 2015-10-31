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

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import android.app.Activity;

public class SensorEventsSubscriptionCallback extends SubscriptionCallback {

	private static final int DEFAULT_DURATION = 600;

	public interface EventCallback {
		void onEventReceived(GENASubscription sub);
	}

	private EventCallback mCallback;
	private Device<?, ?, ?> mDevice;

	public SensorEventsSubscriptionCallback(Service service, EventCallback callback) {
		super(service, DEFAULT_DURATION);
		mCallback = callback;
	}

	@Override
	public void established(GENASubscription sub) {
		System.out.println("Established: " + sub.getSubscriptionId());
	}

	@Override
	protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
		System.err.println(defaultMsg);
	}

	@Override
	public void ended(GENASubscription sub, CancelReason reason, UpnpResponse response) {
		assert reason == null;
	}

	public void eventReceived(GENASubscription sub) {
		mCallback.onEventReceived(sub);
	}

	public void eventsMissed(GENASubscription sub, int numberOfMissedEvents) {
		System.out.println("Missed events: " + numberOfMissedEvents);
	}

}
