/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;

import com.google.common.collect.Maps;

public class LogicRouterContext {
    private final long vrf;

//    private BiMap<Long, GatewayPort> gatewayPorts = HashBiMap.create();
    private Map<Long, GatewayPort> gatewayPorts = Maps.newConcurrentMap();

    LogicRouterContext(long vrf) {
        this.vrf = vrf;
    }

    public long getVrfCtx() {
        return vrf;
    }

    public void addGatewayPort(IpPrefix ip, long vni) {
        gatewayPorts.put(vni, new GatewayPort(ip, vni));
    }

    public GatewayPort getGatewayPortByVni(long vni) {
        return gatewayPorts.get(vni);
    }
}