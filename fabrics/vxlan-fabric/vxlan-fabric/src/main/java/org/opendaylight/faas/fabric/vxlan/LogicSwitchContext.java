/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

import com.google.common.collect.Sets;

public class LogicSwitchContext {
    private final long vni;

    private Set<NodeId> members = Sets.newConcurrentHashSet();

    private LogicRouterContext vrfCtx = null;

    LogicSwitchContext(long vni) {
        this.vni = vni;
    }

    public long getVni() {
        return vni;
    }

    public boolean checkAndSetNewMember(NodeId nodeid) {
        return members.add(nodeid);
    }

    public void associateToRouter(LogicRouterContext vrfCtx, IpAddress ip) {
        this.vrfCtx = vrfCtx;
        vrfCtx.addGatewayPort(ip, vni);
    }

    public LogicRouterContext getVrfCtx() {
        return vrfCtx;
    }
}
