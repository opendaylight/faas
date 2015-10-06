/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

@Immutable
final public class Route {

    final private Subnet destination;
    final private String egressIf;
    final private String gateway;
    final private String priority;

    public Subnet getDestination() {
        return destination;
    }

    public String getOif() {
        return egressIf;
    }

    public String getGateway() {
        return gateway;
    }

    public String getPriority() {
        return priority;
    }

    public Route(Subnet destination, String egressIf, String gateway, String priority) {
        super();
        this.destination = destination;
        this.egressIf = egressIf;
        this.gateway = gateway;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Route [destination=" + destination + ", egressIf=" + egressIf + ", gateway=" + gateway + ", priority="
                + priority + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((egressIf == null) ? 0 : egressIf.hashCode());
        result = prime * result + ((gateway == null) ? 0 : gateway.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Route other = (Route) obj;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (egressIf == null) {
            if (other.egressIf != null)
                return false;
        } else if (!egressIf.equals(other.egressIf))
            return false;
        if (gateway == null) {
            if (other.gateway != null)
                return false;
        } else if (!gateway.equals(other.gateway))
            return false;
        if (priority == null) {
            if (other.priority != null)
                return false;
        } else if (!priority.equals(other.priority))
            return false;
        return true;
    }
}
