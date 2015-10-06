/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

public class RoutingTableEntry {

    private String destIpAddr_m;
    private String destIpNetMask_m;
    private String nextHopIpAddr_m;
    private String vpnId_m;

    public RoutingTableEntry(String vpnId, String destIp, String destIpMask, String nextHop) {
        this.vpnId_m = vpnId;
        this.destIpAddr_m = destIp;
        this.destIpNetMask_m = destIpMask;
        this.nextHopIpAddr_m = nextHop;
    }

    public String getDestIpAddr() {
        return destIpAddr_m;
    }

    public void setDestIpAddr(String destIpAddr) {
        this.destIpAddr_m = destIpAddr;
    }

    public String getDestIpNetMask() {
        return destIpNetMask_m;
    }

    public void setDestIpNetMask(String destIpNetMask) {
        this.destIpNetMask_m = destIpNetMask;
    }

    public String getNextHopIpAddr() {
        return nextHopIpAddr_m;
    }

    public void setNextHopIpAddr(String nextHopIpAddr) {
        this.nextHopIpAddr_m = nextHopIpAddr;
    }

    public String getVpnId() {
        return vpnId_m;
    }

    public void setVpnId(String vpnId) {
        this.vpnId_m = vpnId;
    }

}
