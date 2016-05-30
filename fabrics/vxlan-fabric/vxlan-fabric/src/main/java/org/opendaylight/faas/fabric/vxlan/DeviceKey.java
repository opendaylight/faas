/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DeviceKey {

    TopologyId topoid;
    NodeId nodeid;

    private DeviceKey() {
    }

    public DeviceKey(TopologyId topoid, NodeId nodeid) {
        this.topoid = topoid;
        this.nodeid = nodeid;
    }

    public static DeviceKey newInstance(InstanceIdentifier<Node> iid) {
        DeviceKey obj = new DeviceKey();
        obj.topoid = iid.firstKeyOf(Topology.class).getTopologyId();
        obj.nodeid = iid.firstKeyOf(Node.class).getNodeId();
        return obj;
    }

    @Override
    public boolean equals(Object val) {
        if (val instanceof DeviceKey) {
            DeviceKey o = (DeviceKey) val;
            return topoid.equals(o.topoid) && nodeid.equals(o.nodeid);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return topoid.hashCode() + nodeid.hashCode();
    }
}