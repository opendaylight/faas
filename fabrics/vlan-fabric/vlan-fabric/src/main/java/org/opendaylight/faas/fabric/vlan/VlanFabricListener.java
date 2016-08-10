/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.faas.fabric.utils.InterfaceManager;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.faas.fabric.vlan.res.ResourceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vlan.rev160615.AddToVlanFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vlan.rev160615.FabricVlanDeviceAdapterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vlan.rev160615.VlanVrfRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vlan.rev160615.VlanVrfRouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceLinks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalRouterAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalSwitchAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.DeviceRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.PortLayer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.port.layer.Layer2Info;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.RouteKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
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

    private FabricVlanDeviceAdapterService getVlanDeviceAdapter() {
        return rpcRegistry.getRpcService(FabricVlanDeviceAdapterService.class);
    }

    @Override
    public void fabricCreated(FabricNode fabric) {

        List<DeviceLinks> links = fabric.getFabricAttribute().getDeviceLinks();
        if (links != null) {
            for (DeviceLinks link : links) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Link> linkIId = (InstanceIdentifier<Link>) link.getLinkRef().getValue();
            }
        }

        List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();
                DeviceRole role = deviceNode.getRole();
                deviceAdded(deviceIId, role);
            }
        }

        ResourceManager.initResourceManager(fabricid);
    }

    @Override
    public void deviceAdded(final InstanceIdentifier<Node> deviceIId, DeviceRole role) {
        AddToVlanFabricInputBuilder builder = new AddToVlanFabricInputBuilder();
        builder.setNodeId(deviceIId);
        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        try {
            getVlanDeviceAdapter().addToVlanFabric(builder.build()).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("", e);
        }

        ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = rt.read(LogicalDatastoreType.OPERATIONAL,
                deviceIId);
        Futures.addCallback(readFuture, new FutureCallback<Optional<Node>>() {

            @Override
            public void onSuccess(Optional<Node> result) {
                Node device = result.get();
                String mgntIp = device.getNodeId().getValue();

                DeviceContext devCtx = fabricCtx.addDeviceSwitch(deviceIId, mgntIp, role);

                // if the logical switch has already exists, add this device to members.
                Collection<LogicSwitchContext> lswCtxs = fabricCtx.getLogicSwitchCtxs();
                for (LogicSwitchContext lswCtx : lswCtxs) {
                    if (lswCtx.isExternal()) {
                        continue;
                    }
                    lswCtx.checkAndSetNewMember(DeviceKey.newInstance(deviceIId), mgntIp);
                    devCtx.createBridgeDomain(lswCtx);
                }
            }

            @Override
            public void onFailure(Throwable th) {
                LOG.error("unexecpted exception", th);
            }
        }, executor);
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
    public void aclUpdated(InstanceIdentifier<FabricAcl> iid, boolean isDelete) {
        InstanceIdentifier<TerminationPoint> tpiid = iid.firstIdentifierOf(TerminationPoint.class);
        String aclName = iid.firstKeyOf(FabricAcl.class).getFabricAclName();
        if (tpiid != null) {
            if (isDelete) {
                executor.submit(AclRenderer.newRmAclTask(dataBroker, tpiid, aclName));
            } else {
                executor.submit(AclRenderer.newAddAclTask(dataBroker, tpiid, aclName));
            }
            return;
        }

        NodeId nodeId = iid.firstKeyOf(Node.class).getNodeId();
        if (fabricCtx.isValidLogicSwitch(nodeId)) {
            LogicSwitchContext lswCtx = fabricCtx.getLogicSwitchCtx(nodeId);
            if (isDelete) {
                lswCtx.removeAcl(aclName);
            } else {
                lswCtx.addAcl(aclName);
            }

            for (DeviceKey key : fabricCtx.getSpineDevices()) {
                DeviceContext devCtx = fabricCtx.getDeviceCtx(key);

                devCtx.syncAcl(Lists.newArrayList(lswCtx));
            }
        } else {
            LogicRouterContext lrCtx = fabricCtx.getLogicRouterCtx(nodeId);
            lrCtx.addAcl(aclName);
            List<LogicSwitchContext> lswCtxs = Lists.newArrayList();
            for (int vlan : lrCtx.getVlans()) {
                NodeId lsw = lrCtx.getGatewayPortByVlan(vlan).getLogicSwitch();
                LogicSwitchContext lswCtx = fabricCtx.getLogicSwitchCtx(lsw);
                if (lswCtx != null) {
                    if (isDelete) {
                        lswCtx.removeVrfAcl(aclName);
                    } else {
                        lswCtx.addVrfAcl(aclName);
                    }
                    lswCtxs.add(lswCtx);
                }
            }

            for (DeviceKey key : fabricCtx.getSpineDevices()) {
                DeviceContext devCtx = fabricCtx.getDeviceCtx(key);

                devCtx.syncAcl(lswCtxs);
            }
        }
    }

    @Override
    public void logicSwitchCreated(NodeId nodeId, Node lse) {

        LswAttribute lswAttr = lse.getAugmentation(LogicalSwitchAugment.class).getLswAttribute();
        final boolean isExternal = lswAttr.isExternal() == null ? false : lswAttr.isExternal().booleanValue();
        long segmentId = lswAttr.getSegmentId();
        LogicSwitchContext lswCtx = fabricCtx.addLogicSwitch(nodeId, (int) segmentId, isExternal);

        if (!isExternal) {
            Collection<DeviceContext> devices = fabricCtx.getDeviceCtxs();
            if (devices != null) {
                for (DeviceContext devCtx : devices) {
                    devCtx.createBridgeDomain(lswCtx);
                }
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
        fabricCtx.addLogicRouter(nodeId, (int) vrfctx);
    }

    @Override
    public void logicRouterRemoved(Node lr) {
        fabricCtx.removeLogicSwitch(lr.getNodeId());
    }

    @Override
    public void gatewayRemoved(NodeId lswId, NodeId lrId) {
        fabricCtx.unAssociateSwitchToRouter(lswId, lrId);

    }

    @Override
    public void portFuncUpdated(InstanceIdentifier<PortFunction> iid, PortFunction function, boolean isDelete) {
        // NOT Supported now

    }

    @Override
    public void portLocated(InstanceIdentifier<TerminationPoint> iid, TpId fabricPort) {
        NodeId nodeId = iid.firstKeyOf(Node.class).getNodeId();
        if (fabricCtx.isValidLogicSwitch(nodeId)) {
            InstanceIdentifier<TerminationPoint> devtp = InterfaceManager.convFabricPort2DevicePort(
                    dataBroker, fabricid, fabricPort);

            final long vlan = fabricCtx.getLogicSwitchCtx(nodeId).getVlan();

            final LportAttribute logicalPortAttr = InterfaceManager.getLogicalPortAttr(dataBroker, iid);

            TpId tpid = devtp.firstKeyOf(TerminationPoint.class).getTpId();

            DeviceContext devCtx = fabricCtx.getDeviceCtx(
                    DeviceKey.newInstance(devtp.firstIdentifierOf(Node.class)));

            PortLayer layerInfo = logicalPortAttr.getPortLayer();
            Layer2Info layer2Info = layerInfo.getLayer2Info();
            if (layer2Info != null) {
                devCtx.createBdPort(vlan, tpid, layer2Info.getAccessType(), layer2Info.getAccessSegment());
            } else {
                devCtx.createBdPort(vlan, tpid, AccessType.Exclusive, 0);
            }

        } else {
            LOG.warn("Not yet support layer3 port located.");
        }

    }

    @Override
    public void routeUpdated(InstanceIdentifier<Node> lrIid, List<Route> routes, boolean isDelete) {
        NodeId nodeId = lrIid.firstKeyOf(Node.class).getNodeId();
        LogicRouterContext routerCtx = fabricCtx.getLogicRouterCtx(nodeId);

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        if (isDelete) {
            for (DeviceKey key : fabricCtx.getSpineDevices()) {
                DeviceContext devCtx = fabricCtx.getDeviceCtx(key);

                InstanceIdentifier<VlanVrfRoute> routeIid = devCtx.createVrfRouteIId((int) routerCtx.getVrfCtx());

                for (Route route : routes) {
                    wt.delete(LogicalDatastoreType.OPERATIONAL, routeIid.child(Route.class, new RouteKey(route.getKey())));
                }
            }
        } else {
            for (DeviceKey key : fabricCtx.getSpineDevices()) {
                DeviceContext devCtx = fabricCtx.getDeviceCtx(key);

                InstanceIdentifier<VlanVrfRoute> routeIid = devCtx.createVrfRouteIId((int) routerCtx.getVrfCtx());
                VlanVrfRouteBuilder builder = new VlanVrfRouteBuilder();
                builder.setRoute(routes);
                wt.merge(LogicalDatastoreType.OPERATIONAL, routeIid, builder.build());
            }
        }
        MdSalUtils.wrapperSubmit(wt, executor);
    }

    @Override
    public void routeCleared(InstanceIdentifier<Node> lrIid) {
        NodeId nodeId = lrIid.firstKeyOf(Node.class).getNodeId();
        LogicRouterContext routerCtx = fabricCtx.getLogicRouterCtx(nodeId);

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();

        for (DeviceKey key : fabricCtx.getSpineDevices()) {
            DeviceContext devCtx = fabricCtx.getDeviceCtx(key);
            wt.delete(LogicalDatastoreType.OPERATIONAL, devCtx.createVrfRouteIId((int) routerCtx.getVrfCtx()));
        }
        MdSalUtils.wrapperSubmit(wt, executor);
    }
}