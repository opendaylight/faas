package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

public interface OvsSouthbound {
    OvsdbBridgeAugmentation getBridge(Node node);
    String getBridgeName(Node node);
    long getDataPathId(Node node);
    String getDatapathId(Node node);
}
