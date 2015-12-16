/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import java.io.Serializable;

import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.StatusCode;


/**
 * Represents the return object of the osgi service interfaces function calls.
 * It contains a code {@code StatusCode} representing the result of the call and
 * a string which describes a failure reason (if any) in human readable form.
 */
public class Status implements Serializable {
    private static final long serialVersionUID = 0L;
    private StatusCode code;
    private String description;
    private long requestId;


    public Status(StatusCode errorCode, String description) {
        this.code = (errorCode != null) ? errorCode : StatusCode.UNDEFINED;
        this.description = (description != null) ? description : this.code
                .toString();
        this.requestId = 0;
    }

    public Status(StatusCode errorCode) {
        this.code = (errorCode != null) ? errorCode : StatusCode.UNDEFINED;
        this.description = (description != null) ? description : this.code
                .toString();
        this.requestId = 0;
    }

    public Status(StatusCode errorCode, long requestId) {
        this.code = (errorCode != null) ? errorCode : StatusCode.UNDEFINED;
        this.description = (description != null) ? description : this.code
                .toString();
        this.requestId = requestId;
    }

    /**
     * Returns the status code
     *
     * @return the {@code StatusCode} representing the status code
     */
    public StatusCode getCode() {
        return code;
    }

    /**
     * Returns a human readable description of the failure if any
     *
     * @return a string representing the reason of failure
     */
    public String getDescription() {
        return description;
    }

    /**
     * Tells whether the status is successful
     *
     * @return true if the Status code is {@code StatusCode.SUCCESS}
     */
    public boolean isSuccess() {
        return code == StatusCode.SUCCESS || code == StatusCode.CREATED;
    }

    /**
     * Return the request id assigned by underlying infrastructure in case of
     * asynchronous request. In case of synchronous requests, the returned id
     * is expected to be 0
     *
     * @return The request id assigned for this asynchronous request
     */
    public long getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return code + ": " + description + " (" + requestId + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.calculateConsistentHashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Status other = (Status) obj;
        if (code != other.code) {
            return false;
        }
        return true;
    }
}
