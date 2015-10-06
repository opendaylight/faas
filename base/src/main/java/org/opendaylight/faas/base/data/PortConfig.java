/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

public class PortConfig {

    private String networkElementID;
    private String portID;
    private LinkType type;
    private boolean isTagged;
    private String vFabricNodeId_m;

    public PortConfig(String elementID, String portID, LinkType type) {
        super();
        this.networkElementID = elementID;
        this.portID = portID;
        this.type = type;
        this.isTagged = true;
    }

    public PortConfig(String elementID, String portID, LinkType type, boolean isTagged) {
        super();
        this.networkElementID = elementID;
        this.portID = portID;
        this.type = type;
        this.isTagged = isTagged;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isTagged ? 1231 : 1237);
        result = prime * result + ((networkElementID == null) ? 0 : networkElementID.hashCode());
        result = prime * result + ((portID == null) ? 0 : portID.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        PortConfig other = (PortConfig) obj;
        if (isTagged != other.isTagged)
            return false;
        if (networkElementID == null) {
            if (other.networkElementID != null)
                return false;
        } else if (!networkElementID.equals(other.networkElementID))
            return false;
        if (portID == null) {
            if (other.portID != null)
                return false;
        } else if (!portID.equals(other.portID))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public String getNetworkElementID() {
        return networkElementID;
    }

    public void setNetworkElementID(String networkElementID) {
        this.networkElementID = networkElementID;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean isTagged) {
        this.isTagged = isTagged;
    }

    public String getPortID() {
        return portID;
    }

    public void setPortID(String portID) {
        this.portID = portID;
    }

    public LinkType getType() {
        return type;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PortConfig [networkElementID=" + networkElementID + ", portID=" + portID + ", type=" + type
                + ", isTagged=" + isTagged + "]";
    }

    public String getVFabricNodeId() {
        return vFabricNodeId_m;
    }

    public void setVFabricNodeId(String vFabricNodeId) {
        vFabricNodeId_m = vFabricNodeId;
    }

}
