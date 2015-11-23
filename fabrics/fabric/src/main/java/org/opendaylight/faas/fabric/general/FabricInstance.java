/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FabricInstance implements FabricRenderer {

    private final FabricId fabricId;

    private final UnderlayerNetworkType type;

    private final FabricRenderer delegate;

    public FabricInstance(FabricId fabricId, UnderlayerNetworkType type, FabricRenderer renderer) {
        this.fabricId = fabricId;
        this.type = type;
        this.delegate = renderer;
    }

    public FabricId getId() {
        return fabricId;
    }

    public UnderlayerNetworkType getType() {
        return type;
    }

    @Override
    public void fabricCreated(InstanceIdentifier<FabricNode> fabric) {
    	delegate.fabricCreated(fabric);
    }

    @Override
    public void deviceAdded(InstanceIdentifier<FabricNode> fabric, InstanceIdentifier<Node> device) {
    	delegate.deviceAdded(fabric, device);
    }

    @Override
    public void deviceRemoved(InstanceIdentifier<FabricNode> fabric, InstanceIdentifier<Node> device) {
    	delegate.deviceRemoved(fabric, device);

    }

    @Override
    public void buildLogicSwitch(NodeId nodeid, LswAttributeBuilder lsw, CreateLogicSwitchInput input) {
    	delegate.buildLogicSwitch(nodeid, lsw, input);
    }

    @Override
    public void buildLogicRouter(NodeId nodeid, LrAttributeBuilder lr, CreateLogicRouterInput input) {
    	delegate.buildLogicRouter(nodeid, lr, input);
    }

    @Override
    public void buildLogicPort(TpId tpid, LportAttributeBuilder lp, CreateLogicPortInput input) {
    	delegate.buildLogicPort(tpid, lp, input);
    }

    @Override
    public void endpointAdded(InstanceIdentifier<Endpoint> epIId) {
    	delegate.endpointAdded(epIId);
    }

    @Override
    public void endpointUpdated(InstanceIdentifier<Endpoint> epIId) {
    	delegate.endpointUpdated(epIId);
    }

    @Override
    public void buildGateway(NodeId switchid, IpPrefix ip, NodeId routerid,  FabricId fabricid) {
    	delegate.buildGateway(switchid, ip, routerid, fabricid);

    }

    @Override
    public boolean composeFabric(FabricAttributeBuilder fabric, ComposeFabricInput input) {
        return delegate.composeFabric(fabric, input);
    }

    @Override
    public boolean addNodeToFabric(DeviceNodesBuilder node, AddNodeToFabricInput input) {
        return delegate.addNodeToFabric(node, input);
    }

    @Override
    public void fabricDeleted(Node fabric) {
    	delegate.fabricDeleted(fabric);
    }

	@Override
	public void aclUpdate(InstanceIdentifier<?> iid, boolean port) {
		delegate.aclUpdate(iid, port);
	}
}
