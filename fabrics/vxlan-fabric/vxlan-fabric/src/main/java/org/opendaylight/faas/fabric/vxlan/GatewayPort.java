/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import com.google.common.collect.Sets;

import java.util.Set;

import org.opendaylight.faas.fabric.utils.IpAddressUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class GatewayPort {

    private static final MacAddress GATEWAY_MAC = new MacAddress("80:38:bC:a1:33:c7");

    private IpPrefix ip;
    private long vni;
    private long vrf;
    private MacAddress mac;

    private Set<IpAddress> subips;

    private NodeId lsw;

    public GatewayPort(IpPrefix ip, long vni, NodeId lsw, long vrf) {
        this.ip = ip;
        this.vni = vni;
        this.lsw = lsw;
        this.vrf = vrf;

        subips = Sets.newHashSet(IpAddressUtils.getIpAddress(ip));
    }

    public void setMac(MacAddress mac) {
        this.mac = mac;
    }

    public boolean containsIp(IpAddress ip) {
        return subips.contains(ip);
    }

    IpPrefix getIp() {
        return ip;
    }

    MacAddress getMac() {
        return mac == null ? GATEWAY_MAC : mac;
    }

    Long getVni() {
        return vni;
    }

    Long getVrf() {
        return vrf;
    }

    NodeId getLogicSwitch() {
        return lsw;
    }
}