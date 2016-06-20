/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class DeviceKey {

    private TopologyId topoId;
    private NodeId nodeId;

    private DeviceKey() {
    }

    public DeviceKey(TopologyId topoId, NodeId nodeId) {
        this.topoId = topoId;
        this.nodeId = nodeId;
    }

    public static DeviceKey newInstance(InstanceIdentifier<Node> iid) {
        DeviceKey obj = new DeviceKey();
        obj.topoId = iid.firstKeyOf(Topology.class).getTopologyId();
        obj.nodeId = iid.firstKeyOf(Node.class).getNodeId();
        return obj;
    }

    @Override
    public boolean equals(Object val) {
        if (val instanceof DeviceKey) {
            DeviceKey obj = (DeviceKey) val;
            return topoId.equals(obj.topoId) && nodeId.equals(obj.nodeId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return topoId.hashCode() + nodeId.hashCode();
    }

    public TopologyId getTopoId() {
        return topoId;
    }

    public NodeId getNodeId() {
        return nodeId;
    }
}