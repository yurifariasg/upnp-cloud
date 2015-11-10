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

package com.comarch.android.upnp.ibcdemo.connectivity.xmpp.listeners;

import android.util.Log;

import com.comarch.android.upnp.ibcdemo.MainActivity;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.PacketListenerWithFilter;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.XmppConnector;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapRequestIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResponseIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.SoapResultRequestIQ;
import com.comarch.android.upnp.ibcdemo.connectivity.xmpp.packet.UPNPQuery;

import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.types.ServiceId;
import org.jivesoftware.smack.packet.Packet;

import java.util.HashMap;

public class CLPacketListener implements PacketListenerWithFilter {

    protected XmppConnector connector;
    
    public CLPacketListener(XmppConnector connector){
        this.connector = connector;
    }
    
    @Override
    public void processPacket(Packet packet) {
        Log.d("MySuperPacketListener", packet.toXML());

        if (packet instanceof SoapResponseIQ) {
            SoapResponseIQ iq = (SoapResponseIQ) packet;
            // DO STUFF
        } else if (packet instanceof SoapRequestIQ) {
            SoapRequestIQ iq = (SoapRequestIQ) packet;
            Log.i("CLPacketListener", "" + iq);
            final String serviceId = iq.getServiceType();
            LocalDevice device = MainActivity.MainDevice;

            LocalService localService = device.findService(
                    ServiceId.valueOf(serviceId));

            Log.i("ProcessSoapResponseIQ", "ActionName: " + iq.getActionName());
            Log.i("ProcessSoapResponseIQ", "Service? " + localService);
            if (localService == null) {
                Log.i("ProcessSoapResponseIQ", "no service found.");
                return;
            }
            final Action action = localService.getAction(iq.getActionName());

            if (iq.getArguments().isEmpty()) {
                localService.getExecutor(action).execute(new ActionInvocation<>(action));
            } else {
                ActionArgumentValue[] args = new ActionArgumentValue[iq.getArguments().size()];
                int i = 0;
                for (String argumentName : iq.getArguments().keySet()) {
                    ActionArgument actionArgument = action.getInputArgument(argumentName);
                    if (actionArgument != null) {
                        args[i++] = new ActionArgumentValue(actionArgument,
                                iq.getArguments().get(argumentName));
                    } else {
                        Log.i("ProcessSoapResponseIQ", "ActionArgument not found: " + argumentName);
                    }
                }

                localService.getExecutor(action).execute(new ActionInvocation<>(action, args));
            }

            ActionArgument[] arguments = action.getOutputArguments();
            final HashMap<String, String> args = new HashMap<>();

            for (int i = 0 ; i < arguments.length ; i++) {
                StateVariable variable = localService.getRelatedStateVariable(arguments[i]);
                Log.i("ProcessSoapResponseIQ", "Argument: " + arguments[i].getName());
                try {

                    Object value = localService.getAccessor(variable)
                            .read(localService.getManager().getImplementation());

                    if (value instanceof Boolean) {
                        if ((Boolean) value == true) {
                            args.put(arguments[i].getName(), "1");
                        } else {
                            args.put(arguments[i].getName(), "0");
                        }
                    } else {
                        args.put(arguments[i].getName(), String.valueOf(value));
                    }

                } catch (Exception e) {
                    Log.e("ProcessSoapResponseIQ", "Except..", e);
                }
            }

            SoapResultRequestIQ sendPacket = new SoapResultRequestIQ(action.getName() + "Result", iq.getActionName(), args);
            sendPacket.setTo(packet.getFrom());
            sendPacket.setFrom(packet.getTo());
            connector.sendPacket(sendPacket);

        } else if (packet instanceof UPNPQuery) {
            UPNPQuery query = (UPNPQuery) packet;
            final String type = query.getQueryType();
            Log.i("CLPacketListener", "Type: " + type);
            if (type != null && type.equalsIgnoreCase("description")) {
                connector.sendDeviceDetails(packet.getFrom(), packet.getTo());
            }
        } else {
            Log.i("CLPacketListener", "Unknown Packet: " + packet);
        }
    }



    @Override
    public boolean accept(Packet packet) {
        return true;
    }

}
