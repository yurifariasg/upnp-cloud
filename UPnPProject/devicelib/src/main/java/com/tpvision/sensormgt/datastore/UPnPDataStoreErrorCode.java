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

public enum UPnPDataStoreErrorCode {
        
        INCORRECT_ARG_XML_SYNTAX(701, "The XML format of the argument is incorrect"),
        DATATABLE_NOT_FOUND(702, "The DataTable indicated by DataTableID cannot be found"),
        INSUFFICIENT_ROLE_PERMISSIONS(703, "The control point does not have sufficient roles to perform the requested action"), 
        INVALID_GROUPS(704, "The DataStore group(s) already exist or are reserved by the implementation"),
        INVALID_ROLE(705, "The DataTable Role(s) or Permission(s) do not exist"),
        NO_TRANSPORTURL_AVAILABLE(706, "All available TransportURLs for the indicated DataTable have been allocated"),
        KEYNAME_NOT_FOUND(707, "The indicated DataTable Dictionary KeyName was not found"),
        KEYNAME_INVALID(708, "The key name provided is invalid (ex: empty string)"),
        INVALID_FILTER(709, "The filter argument (DataRecordFilter) is not valid"),
        GROUP_IN_USE(710, "The DataStore group(s) cannot be modified or deleted since they are currently referenced by DataTable(s)"),
        INVALID_RECORD_INDEX(711, "The record index argument  (DataRecordStart)  is no longer valid"),
        DATAITEM_NOT_FOUND(712, "A DataRecord to be written contains a DataItem not specified by this DataTable"),
        DATAITEM_MISSING(713, "The DataItem to be written is missing a DataItem required by this DataTable"),
        NO_DATATABLE_MODIFICATION(714, "The DataTable modification requested is not acceptable due to semantic errors"),
        RESERVED_GROUP_NAME(715, "The DataStore group name has been reserved for use by the implementation");
        
        private int code;
        private String description;
        
        UPnPDataStoreErrorCode(int code, String description) {
                this.code = code;
                this.description = description;
        }

        public int getCode() {
                return code;
        }

        public String getDescription() {
                return description;
        }
        
        public static UPnPDataStoreErrorCode getByCode(int code) {
                for (UPnPDataStoreErrorCode errorCode : values()) {
                        if (errorCode.getCode() == code)
                                return errorCode;
                }
                return null;
        }

}
