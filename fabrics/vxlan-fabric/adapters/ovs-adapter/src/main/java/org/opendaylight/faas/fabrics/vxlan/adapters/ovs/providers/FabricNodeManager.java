package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDeviceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.AttributesBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDeviceAttribute;

public class FabricNodeManager {
    public void addDeviceToFabric(InstanceIdentifier<Node> deviceNodeId,  NodeId fabricId) {
        //fabricCapableDeviceBuilder need to get from DOM store
        FabricCapableDeviceBuilder fabricCapableDeviceBuilder = new FabricCapableDeviceBuilder();
        AttributesBuilder attributesBuilder = new AttributesBuilder();
        attributesBuilder.setFabricId(fabricId);
        fabricCapableDeviceBuilder.setAttributes(attributesBuilder.build());

    }
}
