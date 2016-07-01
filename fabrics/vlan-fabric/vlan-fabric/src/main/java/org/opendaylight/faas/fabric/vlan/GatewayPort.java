/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class GatewayPort {

    private IpPrefix ip;
    private int vlan;
    private int vrf;
    private MacAddress mac;

    private NodeId lsw;

    public GatewayPort(IpPrefix ip, int vlan, NodeId lsw, long vrf) {
        this.ip = ip;
        this.vlan = vlan;
        this.lsw = lsw;
        this.vrf = (int) vrf;
    }

    public void setMac(MacAddress mac) {
        this.mac = mac;
    }

    IpPrefix getIp() {
        return ip;
    }

    Integer getVlan() {
        return vlan;
    }

    Integer getVrf() {
        return vrf;
    }

    NodeId getLogicSwitch() {
        return lsw;
    }
}
