/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LogicRouterContext {
    private final int vrf;

    private Map<Integer, GatewayPort> gatewayPorts = Maps.newConcurrentMap();

    private List<String> acls = Lists.newArrayList();

    LogicRouterContext(int vrf) {
        this.vrf = vrf;
    }

    public long getVrfCtx() {
        return vrf;
    }

    public GatewayPort addGatewayPort(IpPrefix ip, int vlan, NodeId lsw) {
        return gatewayPorts.put(vlan, new GatewayPort(ip, vlan, lsw, vrf));
    }

    public GatewayPort removeGatewayPort(int vlan) {
        return gatewayPorts.remove(vlan);
    }

    public GatewayPort getGatewayPortByVlan(int vlan) {
        return gatewayPorts.get(vlan);
    }

    public void addAcl(String aclName) {
        acls.add(aclName);
    }

    public void removeAcl(String aclName) {
        acls.remove(aclName);
    }

    public List<String> getAcls() {
        return Collections.unmodifiableList(acls);
    }

    public Set<Integer> getVlans() {
        return gatewayPorts.keySet();
    }
}
