/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

public class VlanIf {

    private String phyNodeId_m;
    private String phyPortId_m;
    private String vlanId_m;
    private String ipAddress_m;
    private String netMask_m;
    private String vpnId_m;

    public VlanIf(String nodeId, String portId, String vlanId, String ip, String mask, String vpnId) {
        this.phyNodeId_m = nodeId;
        this.phyPortId_m = portId;
        this.vlanId_m = vlanId;
        this.ipAddress_m = ip;
        this.netMask_m = mask;
        this.setVpnId(vpnId);
    }

    public String getPhyNodeId() {
        return phyNodeId_m;
    }

    public void setPhyNodeId(String phyNodeId) {
        this.phyNodeId_m = phyNodeId;
    }

    public String getPhyPortId() {
        return phyPortId_m;
    }

    public void setPhyPortId(String phyPortId) {
        this.phyPortId_m = phyPortId;
    }

    public String getVlanId() {
        return vlanId_m;
    }

    public void setVlanId(String vlanId) {
        this.vlanId_m = vlanId;
    }

    public String getIpAddress() {
        return ipAddress_m;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress_m = ipAddress;
    }

    public String getNetMask() {
        return netMask_m;
    }

    public void setNetMask(String netMask) {
        this.netMask_m = netMask;
    }

    public String getVpnId() {
        return vpnId_m;
    }

    public void setVpnId(String vpnId) {
        this.vpnId_m = vpnId;
    }
}
