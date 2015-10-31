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

package com.tpvision.sensormgt.datamodel;

public enum UPnPSensorTransportErrorCode {
        
        INCORRECT_ARG_XML_SYNTAX(701, "The XML format of the argument is incorrect"),
        SENSORID_NOT_FOUND(702, "The SensorID does not correspond to a known sensor"),
        SENSORURN_NOT_FOUND(703, "The SensorURN provided does not correspond to a known URN for the indicated SensorID"), 
        TRANSPORTCONN_NOT_FOUND(704, "Transport connection not found"),
        DATAITEM_NOT_FOUND(705, "DataItem referenced by an action cannot be found"),
        SENSORDATAITEM_READONLY(706, "One or more Sensor DataItems to be written are marked read-only"),
        SENSOR_UNAVAILABLE(707, "The target sensor is disconnected and cannot be successfully written"),
        TRANSPORTCONN_LIMITEXCEEDED(708, "The number of available transport connections to the indicated SensorID has been exceeded");
        
        
        private int code;
        private String description;
        
        UPnPSensorTransportErrorCode(int code, String description) {
                this.code = code;
                this.description = description;
        }

        public int getCode() {
                return code;
        }

        public String getDescription() {
                return description;
        }
        
        public static UPnPSensorTransportErrorCode getByCode(int code) {
                for (UPnPSensorTransportErrorCode errorCode : values()) {
                        if (errorCode.getCode() == code)
                                return errorCode;
                }
                return null;
        }

}
