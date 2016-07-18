/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import com.google.common.collect.Lists;

import java.util.List;

import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.DeviceRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FabricInstance implements FabricRenderer {

    private final FabricId fabricId;

    private final UnderlayerNetworkType type;

    private final FabricRenderer renderer;

    private List<FabricListener> listeners = Lists.newArrayList();

    public FabricInstance(FabricId fabricId, UnderlayerNetworkType type, FabricRenderer renderer) {
        this.fabricId = fabricId;
        this.type = type;
        this.renderer = renderer;
    }

    public FabricId getId() {
        return fabricId;
    }

    public UnderlayerNetworkType getType() {
        return type;
    }

    public void addListener(FabricListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(FabricListener listener) {
        this.listeners.remove(listener);
    }


    @Override
    public void buildLogicalSwitch(NodeId nodeid, LswAttributeBuilder lsw, CreateLogicalSwitchInput input) {
        renderer.buildLogicalSwitch(nodeid, lsw, input);
    }

    @Override
    public void buildLogicalRouter(NodeId nodeid, LrAttributeBuilder lr, CreateLogicalRouterInput input) {
        renderer.buildLogicalRouter(nodeid, lr, input);
    }

    @Override
    public void buildLogicalPort(TpId tpid, LportAttributeBuilder lp, CreateLogicalPortInput input) {
        renderer.buildLogicalPort(tpid, lp, input);
    }

    @Override
    public void buildGateway(NodeId switchid, IpPrefix ip, NodeId routerid,  FabricId fabricid) {
        renderer.buildGateway(switchid, ip, routerid, fabricid);

    }

    @Override
    public boolean addNodeToFabric(DeviceNodesBuilder node, AddNodeToFabricInput input) {
        return renderer.addNodeToFabric(node, input);
    }

    @Override
    public InstanceIdentifier<FabricAcl> addAcl(NodeId deviceid, TpId tpid, String aclName) {
        return renderer.addAcl(deviceid, tpid, aclName);

    }

    @Override
    public InstanceIdentifier<FabricAcl> delAcl(NodeId deviceid, TpId tpid, String aclName) {
        return renderer.delAcl(deviceid, tpid, aclName);
    }

    public void notifyFabricCreated(FabricNode node) {
        for (FabricListener listener : listeners) {
            listener.fabricCreated(node);
        }
    }

    public void notifyGatewayRemoved(NodeId lswId, NodeId lrId) {
        for (FabricListener listener : listeners) {
            listener.gatewayRemoved(lswId, lrId);
        }
    }

    public void notifyLogicSwitchCreated(NodeId nodeId, Node lsw) {
        for (FabricListener listener : listeners) {
            listener.logicSwitchCreated(nodeId, lsw);
        }
    }

    public void notifyLogicSwitchRemoved(Node lsw) {
        for (FabricListener listener : listeners) {
            listener.logicSwitchRemoved(lsw);
        }
    }

    public void notifyLogicRouterCreated(NodeId nodeId, Node lr) {
        for (FabricListener listener : listeners) {
            listener.logicRouterCreated(nodeId, lr);
        }
    }

    public void notifyLogicRouterRemoved(Node lr) {
        for (FabricListener listener : listeners) {
            listener.logicRouterRemoved(lr);
        }
    }

    public void notifyDeviceAdded(InstanceIdentifier<Node> device, DeviceRole role) {
        for (FabricListener listener : listeners) {
            listener.deviceAdded(device, role);
        }
    }

    public void notifyDeviceRemoved(InstanceIdentifier<Node> device) {
        for (FabricListener listener : listeners) {
            listener.deviceRemoved(device);
        }
    }

    public void notifyFabricDeleted(Node fabric) {
        for (FabricListener listener : listeners) {
            listener.fabricDeleted(fabric);
        }
    }

    public void notifyAclUpdated(InstanceIdentifier<FabricAcl> iid, boolean isDelete) {
        for (FabricListener listener : listeners) {
            listener.aclUpdated(iid, isDelete);
        }
    }

    public void notifyPortFuncUpdated(InstanceIdentifier<PortFunction> iid, PortFunction function, boolean isDelete) {
        for (FabricListener listener : listeners) {
            listener.portFuncUpdated(iid, function, isDelete);
        }
    }

    public void notifyRouteUpdated(InstanceIdentifier<Node> lrIid, List<Route> routes, boolean isDelete) {
        for (FabricListener listener : listeners) {
            listener.routeUpdated(lrIid, routes, isDelete);
        }
    }

    public void notifyRouteCleared(InstanceIdentifier<Node> lrIid) {
        for (FabricListener listener : listeners) {
            listener.routeCleared(lrIid);
        }
    }

    public void notifyLogicalPortLocated(InstanceIdentifier<TerminationPoint> iid, TpId fabricPort) {
        for (FabricListener listener : listeners) {
            listener.portLocated(iid, fabricPort);
        }
    }
}
