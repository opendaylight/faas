/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Collection;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.faas.fabric.vlan.res.ResourceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalRouterAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalSwitchAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VlanFabricListener implements AutoCloseable, FabricListener {

    private static final Logger LOG = LoggerFactory.getLogger(VlanFabricListener.class);

    private final InstanceIdentifier<FabricNode> fabricIId;
    private final FabricId fabricid;
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private ListeningExecutorService executor;
    private EndPointManager epMgr;
    private final FabricContext fabricCtx;

    public VlanFabricListener(InstanceIdentifier<FabricNode> fabricIId,
                            final DataBroker dataProvider,
                             final RpcProviderRegistry rpcRegistry,
                             final FabricContext fabricCtx) {
        this.fabricIId = fabricIId;
        this.fabricid = new FabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        this.dataBroker = dataProvider;
        this.rpcRegistry = rpcRegistry;

        executor = fabricCtx.executor;

        this.fabricCtx = fabricCtx;
        epMgr = new EndPointManager(dataProvider, rpcRegistry, fabricCtx);
    }

    @Override
    public void close() {
        epMgr.close();
    }

    @Override
    public void fabricCreated(FabricNode fabric) {

        List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();

                deviceAdded(deviceIId);
            }
        }

        ResourceManager.initResourceManager(fabricid);
    }

    @Override
    public void deviceAdded(final InstanceIdentifier<Node> deviceIId) {
    }

    @Override
    public void deviceRemoved(final InstanceIdentifier<Node> deviceIId) {

    }

    @Override
    public void fabricDeleted(Node node) {

        ResourceManager.freeResourceManager(new FabricId(node.getNodeId()));
        fabricCtx.close();
        this.close();
    }

    @Override
    public void aclUpdate(InstanceIdentifier<FabricAcl> iid, boolean delete) {
        InstanceIdentifier<TerminationPoint> tpiid = iid.firstIdentifierOf(TerminationPoint.class);
        String aclName = iid.firstKeyOf(FabricAcl.class).getFabricAclName();
        if (tpiid != null) {
            if (delete) {
                executor.submit(AclRenderer.newRmAclTask(dataBroker, tpiid, aclName));
            } else {
                executor.submit(AclRenderer.newAddAclTask(dataBroker, tpiid, aclName));
            }
            return;
        }

        NodeId nodeId = iid.firstKeyOf(Node.class).getNodeId();
        if (fabricCtx.isValidLogicSwitch(nodeId)) {
            if (delete) {
                fabricCtx.getLogicSwitchCtx(nodeId).removeAcl(aclName);
            } else {
                fabricCtx.getLogicSwitchCtx(nodeId).addAcl(aclName);
            }
        } else {
            LogicRouterContext lrCtx = fabricCtx.getLogicRouterCtx(nodeId);
            lrCtx.addAcl(aclName);
            for (Long vni : lrCtx.getVnis()) {
                NodeId lsw = lrCtx.getGatewayPortByVni(vni).getLogicSwitch();
                LogicSwitchContext lswCtx = fabricCtx.getLogicSwitchCtx(lsw);
                if (lswCtx != null) {
                    if (delete) {
                        lswCtx.removeVrfAcl(aclName);
                    } else {
                        lswCtx.addVrfAcl(aclName);
                    }
                }
            }
        }
    }

    @Override
    public void logicSwitchCreated(NodeId nodeId, Node lse) {
        // for distributed Fabric, we add logic switch to all device

        long segmentId = lse.getAugmentation(LogicalSwitchAugment.class).getLswAttribute().getSegmentId();
        LogicSwitchContext lswCtx = fabricCtx.addLogicSwitch(nodeId, segmentId);

        Collection<DeviceContext> devices = fabricCtx.getDeviceCtxs();
        if (devices != null) {
            for (DeviceContext devCtx : devices) {
                lswCtx.checkAndSetNewMember(devCtx.getKey(), devCtx.getVtep());
                devCtx.createBridgeDomain(lswCtx);
            }
        }
    }

    @Override
    public void logicSwitchRemoved(Node lsw) {

        LogicSwitchContext lswCtx = fabricCtx.getLogicSwitchCtx(lsw.getNodeId());

        Collection<DeviceContext> devices = fabricCtx.getDeviceCtxs();
        if (devices != null) {
            for (DeviceContext devCtx : devices) {
                devCtx.removeBridgeDomain(lswCtx);
            }
        }
        fabricCtx.removeLogicSwitch(lsw.getNodeId());
        lswCtx.close();
    }

    @Override
    public void logicRouterCreated(NodeId nodeId, Node lr) {
        long vrfctx = lr.getAugmentation(LogicalRouterAugment.class).getLrAttribute().getVrfCtx();
        fabricCtx.addLogicRouter(nodeId, vrfctx);
    }

    @Override
    public void logicRouterRemoved(Node lr) {
        fabricCtx.removeLogicSwitch(lr.getNodeId());
    }

    @Override
    public void gatewayRemoved(NodeId lswId, NodeId lrId) {
        fabricCtx.unAssociateSwitchToRouter(lswId, lrId);

    }
}