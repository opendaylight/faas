package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

public interface NodeCacheListener {
    void notifyNode(Node node, Action action);
}
