/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

import java.util.List;
import org.opendaylight.faas.base.data.EndPoint;
import org.opendaylight.faas.base.data.Subnet;

public class LogicalSwitch {

    private String resourceId_m;
    private List<EndPoint> endPointList_m;
    private Subnet subnet_m;
    private String systemId_m;
    private String name_m;
    private String id_m;
    private String tenantId_m;
    private String vdcId_m;

    public LogicalSwitch(String name, String tenantId, String vdcId, Subnet subnet, String resourceId,
            List<EndPoint> eps) {
        this.name_m = name;
        this.id_m = name;
        this.systemId_m = name;
        this.subnet_m = subnet;
        this.endPointList_m = eps;
        this.resourceId_m = resourceId;
        this.tenantId_m = tenantId;
        this.vdcId_m = vdcId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endPointList_m == null) ? 0 : endPointList_m.hashCode());
        result = prime * result + ((name_m == null) ? 0 : name_m.hashCode());
        result = prime * result + ((id_m == null) ? 0 : id_m.hashCode());
        result = prime * result + ((resourceId_m == null) ? 0 : resourceId_m.hashCode());
        result = prime * result + ((subnet_m == null) ? 0 : subnet_m.hashCode());
        result = prime * result + ((systemId_m == null) ? 0 : systemId_m.hashCode());
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
        LogicalSwitch other = (LogicalSwitch) obj;
        if (endPointList_m == null) {
            if (other.endPointList_m != null)
                return false;
        } else if (!endPointList_m.equals(other.endPointList_m))
            return false;
        if (name_m == null) {
            if (other.name_m != null)
                return false;
        } else if (!name_m.equals(other.name_m))
            return false;
        if (id_m == null) {
            if (other.id_m != null)
                return false;
        } else if (!id_m.equals(other.id_m))
            return false;
        if (resourceId_m == null) {
            if (other.resourceId_m != null)
                return false;
        } else if (!resourceId_m.equals(other.resourceId_m))
            return false;
        if (subnet_m == null) {
            if (other.subnet_m != null)
                return false;
        } else if (!subnet_m.equals(other.subnet_m))
            return false;
        if (systemId_m == null) {
            if (other.systemId_m != null)
                return false;
        } else if (!systemId_m.equals(other.systemId_m))
            return false;
        return true;
    }

    public List<EndPoint> getEPs() {
        return endPointList_m;
    }

    public void setEPs(List<EndPoint> eps) {
        this.endPointList_m = eps;
    }

    public String getResourceId() {
        return resourceId_m;
    }

    public Subnet getSubnet() {
        return subnet_m;
    }

    public void setSubnet(Subnet subnet) {
        this.subnet_m = subnet;
    }

    @Override
    public String toString() {
        return "LogicalSwitchNode [subnet=" + subnet_m + "] resourceID " + resourceId_m;
    }

    public String getSystemId() {
        return systemId_m;
    }

    public void setSystemId(String systemId) {
        this.systemId_m = systemId;
    }

    public String getName() {
        return name_m;
    }

    public void setName(String name) {
        this.name_m = name;
    }

    public String getId() {
        return id_m;
    }

    public void setId(String id) {
        this.id_m = id;
    }

    public String getTenantId() {
        return tenantId_m;
    }

    public void setTenantId(String tenantId) {
        this.tenantId_m = tenantId;
    }

    public String getVdcId() {
        return vdcId_m;
    }

    public void setVdcId(String vdcId) {
        this.vdcId_m = vdcId;
    }

}
