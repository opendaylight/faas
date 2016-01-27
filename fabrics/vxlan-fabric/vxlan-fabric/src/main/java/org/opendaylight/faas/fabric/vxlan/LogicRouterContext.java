/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LogicRouterContext {
    private final long vrf;

    private Map<Long, GatewayPort> gatewayPorts = Maps.newConcurrentMap();

    private List<String> acls = Lists.newArrayList();

    LogicRouterContext(long vrf) {
        this.vrf = vrf;
    }

    public long getVrfCtx() {
        return vrf;
    }

    public GatewayPort addGatewayPort(IpPrefix ip, long vni, NodeId lsw) {
        return gatewayPorts.put(vni, new GatewayPort(ip, vni, lsw, vrf));
    }

    public GatewayPort removeGatewayPort(long vni) {
    	return gatewayPorts.remove(vni);
    }

    public GatewayPort getGatewayPortByVni(long vni) {
        return gatewayPorts.get(vni);
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

	public Set<Long> getVnis() {
		return gatewayPorts.keySet();
	}
}