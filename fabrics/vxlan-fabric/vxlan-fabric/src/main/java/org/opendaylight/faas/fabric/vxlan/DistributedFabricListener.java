/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
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
import org.opendaylight.faas.fabric.vxlan.res.ResourceManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.AddToVxlanFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.FabricVxlanDeviceAdapterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.RmFromVxlanFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalRouterAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalSwitchAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.DeviceRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.PortLayer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.port.layer.Layer2Info;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.RouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.RouteKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.route.NextHopOptions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.route.next.hop.options.SimpleNextHop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Rib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.RibBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.RibKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.rib.VxlanRouteAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.rib.VxlanRouteAugBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedFabricListener implements AutoCloseable, FabricListener {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedFabricListener.class);

    private final InstanceIdentifier<FabricNode> fabricIId;
    private final FabricId fabricid;
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private final ListeningExecutorService executor;
    private final EndPointManager epMgr;
    private final FabricContext fabricCtx;

    public DistributedFabricListener(InstanceIdentifier<FabricNode> fabricIId,
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

    private FabricVxlanDeviceAdapterService getVxlanDeviceAdapter() {
        return rpcRegistry.getRpcService(FabricVxlanDeviceAdapterService.class);
    }

    @Override
    public void fabricCreated(FabricNode fabric) {

        List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();

                deviceAdded(deviceIId, deviceNode.getRole());
            }
        }

        ResourceManager.initResourceManager(fabricid);
    }

    @Override
    public void deviceAdded(final InstanceIdentifier<Node> deviceIId, DeviceRole role) {

        AddToVxlanFabricInputBuilder builder = new AddToVxlanFabricInputBuilder();
        builder.setNodeId(deviceIId);
        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        try {
            getVxlanDeviceAdapter().addToVxlanFabric(builder.build()).get();
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
                FabricCapableDevice augment = device.getAugmentation(FabricCapableDevice.class);
                IpAddress vtep = null;
                if (augment != null) {
                    VtepAttribute vtepAttr = augment.getAttributes().getAugmentation(VtepAttribute.class);
                    if (vtepAttr != null) {
                        vtep = vtepAttr.getVtep().getIp();
                    }
                }

                // if the logical switch has already exists, add this device to members.
                System.out.println("deviceSwitch added :" + deviceIId + "vtep ip:" + vtep);
                DeviceContext devCtx = fabricCtx.addDeviceSwitch(deviceIId, vtep);
                Collection<LogicSwitchContext> lswCtxs = fabricCtx.getLogicSwitchCtxs();
                for (LogicSwitchContext lswCtx : lswCtxs) {
                    if (lswCtx.isExternal()) {
                        continue;
                    }
                    lswCtx.checkAndSetNewMember(DeviceKey.newInstance(deviceIId), vtep);
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

        RmFromVxlanFabricInputBuilder builder = new RmFromVxlanFabricInputBuilder();
        builder.setNodeId(deviceIId);
        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        getVxlanDeviceAdapter().rmFromVxlanFabric(builder.build());

        InstanceIdentifier<DeviceNodes> devicepath = fabricIId.builder().child(FabricAttribute.class)
                .child(DeviceNodes.class, new DeviceNodesKey(new NodeRef(deviceIId))).build();

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.delete(LogicalDatastoreType.OPERATIONAL, devicepath);
        trans.delete(LogicalDatastoreType.OPERATIONAL,
                deviceIId.augmentation(FabricCapableDevice.class).child(Config.class));
        MdSalUtils.wrapperSubmit(trans, executor);

        DeviceKey devKey = DeviceKey.newInstance(deviceIId);
        fabricCtx.removeDeviceSwitch(devKey);
        Collection<LogicSwitchContext> lswCtxs = fabricCtx.getLogicSwitchCtxs();
        for (LogicSwitchContext lswCtx : lswCtxs) {
            lswCtx.removeMember(devKey);
        }
    }

    @Override
    public void fabricDeleted(Node node) {
        FabricNode fabric = node.getAugmentation(FabricNode.class);
        FabricId fabricid = new FabricId(node.getNodeId());
        List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.delete(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricid)));

        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();

                RmFromVxlanFabricInputBuilder builder = new RmFromVxlanFabricInputBuilder();
                builder.setNodeId(deviceIId);
                builder.setFabricId(node.getNodeId());
                getVxlanDeviceAdapter().rmFromVxlanFabric(builder.build());

                wt.delete(LogicalDatastoreType.OPERATIONAL, deviceIId.augmentation(FabricCapableDevice.class)
                        .child(Config.class));
            }
        }
        MdSalUtils.wrapperSubmit(wt, executor);

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
            if (isDelete) {
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
                    if (isDelete) {
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

        LswAttribute lswAttr = lse.getAugmentation(LogicalSwitchAugment.class).getLswAttribute();
        final boolean isExternal = lswAttr.isExternal() == null ? false : lswAttr.isExternal().booleanValue();
        long segmentId = lswAttr.getSegmentId();
        LogicSwitchContext lswCtx = fabricCtx.addLogicSwitch(nodeId, segmentId, isExternal);

        if (!lswCtx.isExternal()) {
            Collection<DeviceContext> devices = fabricCtx.getDeviceCtxs();
            if (devices != null) {
                for (DeviceContext devCtx : devices) {
                    lswCtx.checkAndSetNewMember(devCtx.getKey(), devCtx.getVtep());
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

    @Override
    public void portFuncUpdated(InstanceIdentifier<PortFunction> iid, PortFunction function, boolean isDelete) {
        InstanceIdentifier<TerminationPoint> tpiid = iid.firstIdentifierOf(TerminationPoint.class);
        NodeId nodeId = MdSalUtils.getNodeId(tpiid);
        if (fabricCtx.isValidLogicSwitch(nodeId)) {
            LOG.warn("Not support port function on l2 logical port. %s", nodeId);
            return;
        }
        IpAddress gwIp = new IpAddress(MdSalUtils.getTpId(tpiid).getValue().toCharArray());
        GatewayPort gwport = fabricCtx.getLogicRouterCtx(nodeId).getGatewayPort(gwIp);
        long vni = gwport.getVni();

        Set<DeviceKey> devs = fabricCtx.getLogicSwitchCtx(gwport.getLogicSwitch()).getMembers();
        for (DeviceKey devKey : devs) {
            fabricCtx.getDeviceCtx(devKey).addFunction2Bdif(vni, function);
        }
    }

    @Override
    public void portLocated(InstanceIdentifier<TerminationPoint> iid, TpId fabricPort) {

        NodeId nodeId = iid.firstKeyOf(Node.class).getNodeId();
        if (fabricCtx.isValidLogicSwitch(nodeId)) {
            InstanceIdentifier<TerminationPoint> devtp = InterfaceManager.convFabricPort2DevicePort(
                    dataBroker, fabricid, fabricPort);

            final long vni = fabricCtx.getLogicSwitchCtx(nodeId).getVni();

            final LportAttribute logicalPortAttr = InterfaceManager.getLogicalPortAttr(dataBroker, iid);

            TpId tpid = devtp.firstKeyOf(TerminationPoint.class).getTpId();

            DeviceContext devCtx = fabricCtx.getDeviceCtx(
                    DeviceKey.newInstance(devtp.firstIdentifierOf(Node.class)));

            PortLayer layerInfo = logicalPortAttr.getPortLayer();
            Layer2Info layer2Info = layerInfo.getLayer2Info();
            if (layer2Info != null) {
                devCtx.createBdPort(vni, tpid, layer2Info.getAccessType(), layer2Info.getAccessSegment());
            } else {
                devCtx.createBdPort(vni, tpid, AccessType.Exclusive, 0);
            }

        } else {
            LOG.warn("Not yet support layer3 port located.({})", nodeId.getValue());
        }
    }

    @Override
    public void routeUpdated(InstanceIdentifier<Node> lrIid, List<Route> routes, boolean isDelete) {
        NodeId nodeId = lrIid.firstKeyOf(Node.class).getNodeId();
        List<Route> renderedRoutes = Lists.newArrayList();

        if (fabricCtx.isValidLogicSwitch(nodeId)) {
            LOG.warn("Not support route on l2 logical port. %s", nodeId);
            return;
        }

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        long vrf = fabricCtx.getLogicRouterCtx(nodeId).getVrfCtx();
        InstanceIdentifier<Rib> iid = createRibIId(fabricCtx.getFabricId(), vrf);

        if (isDelete) {
            for (Route route : routes) {
                wt.delete(LogicalDatastoreType.OPERATIONAL, iid.child(Route.class, new RouteKey(route.getKey())));
            }
        } else {
            // calculate outgoing vni
            for (Route route : routes) {
                RouteBuilder builder = new RouteBuilder(route);
                renderRoute(builder, nodeId);

                renderedRoutes.add(builder.build());
            }
            wt.merge(LogicalDatastoreType.OPERATIONAL, iid,
                    new RibBuilder().setVrf(vrf).setRoute(renderedRoutes).build());
        }
        MdSalUtils.wrapperSubmit(wt, executor);
    }

    private void renderRoute(RouteBuilder builder, NodeId routerId) {

        NextHopOptions nexthop = builder.getNextHopOptions();
        if (nexthop instanceof SimpleNextHop) {
            SimpleNextHop simpleNh  = (SimpleNextHop) nexthop;

            TpId tpid = simpleNh.getOutgoingInterface();

            if (tpid != null) {
                IpAddress gwIp = new IpAddress(tpid.getValue().toCharArray());
                GatewayPort gwport = fabricCtx.getLogicRouterCtx(routerId).getGatewayPort(gwIp);
                long vni = gwport.getVni();
                builder.addAugmentation(VxlanRouteAug.class, new VxlanRouteAugBuilder().setOutgoingVni(vni).build());
            }
        } else {
            LOG.warn("nexthop is not simple. {}", nexthop.getClass().getName());
        }
    }

    private InstanceIdentifier<Rib> createRibIId(FabricId fabricId, long vrf) {
        return InstanceIdentifier.create(FabricRenderedMapping.class).child(Fabric.class, new FabricKey(fabricId))
                .child(Rib.class, new RibKey(vrf));
    }

    @Override
    public void routeCleared(InstanceIdentifier<Node> lrIid) {
        NodeId nodeId = lrIid.firstKeyOf(Node.class).getNodeId();

        if (fabricCtx.isValidLogicSwitch(nodeId)) {
            LOG.warn("Not support route on l2 logical port. %s", nodeId);
            return;
        }

        long vrf = fabricCtx.getLogicRouterCtx(nodeId).getVrfCtx();
        InstanceIdentifier<Rib> iid = createRibIId(fabricCtx.getFabricId(), vrf);

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.delete(LogicalDatastoreType.OPERATIONAL, iid);
        MdSalUtils.wrapperSubmit(wt, executor);

    }
}
