/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.FabricRenderer;
import org.opendaylight.faas.fabric.general.FabricRendererRegistry;
import org.opendaylight.faas.fabric.vxlan.res.ResourceManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.AddToVxlanFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.FabricVxlanDeviceAdapterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.RmFromVxlanFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.VtepBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.vxlan.rev150930.VxlanDeviceAddInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.vxlan.rev150930.VxlanDeviceComposeInput;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class VXLANFabricProvider implements AutoCloseable, FabricRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(VXLANFabricProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private final FabricRendererRegistry rendererRegistry;

    private ExecutorService executor;
    private EndPointManager epMgr;
    private SwitchManager switchMgr;

    public VXLANFabricProvider (final DataBroker dataProvider,
                             final RpcProviderRegistry rpcRegistry,
                             final NotificationProviderService notificationService,
                             final FabricRendererRegistry rendererRegistry) {
        this.dataBroker = dataProvider;
        this.rpcRegistry = rpcRegistry;
        this.rendererRegistry = rendererRegistry;

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

        switchMgr = new SwitchManager(dataProvider, executor);
        epMgr = new EndPointManager(dataProvider, executor, switchMgr);

        rendererRegistry.register(UnderlayerNetworkType.VXLAN, this);
    }

    @Override
    public void close() throws Exception {
        rendererRegistry.unregister(UnderlayerNetworkType.VXLAN);
        epMgr.close();
        executor.shutdown();
    }

    private FabricVxlanDeviceAdapterService getVlanDeviceAdapter() {
        return rpcRegistry.getRpcService(FabricVxlanDeviceAdapterService.class);
    }

    @Override
    public void fabricCreated(final InstanceIdentifier<FabricNode> fabricIId) {
        ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<FabricNode>,ReadFailedException> future = rt.read(LogicalDatastoreType.OPERATIONAL, fabricIId);

        Futures.addCallback(future, new FutureCallback<Optional<FabricNode>>(){

            @Override
            public void onSuccess(Optional<FabricNode> result) {
                FabricNode fabric = result.get();

                List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
                if (devices != null) {
                    for (DeviceNodes deviceNode : devices) {
                        @SuppressWarnings("unchecked")
                        InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();

                        deviceAdded(fabricIId, deviceIId);
//                        AddToVxlanFabricInputBuilder builder = new AddToVxlanFabricInputBuilder();
//                        builder.setNodeId(deviceIId);
//                        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
//                        getVlanDeviceAdapter().addToVxlanFabric(builder.build());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("unexecpted exception", t);
            }}, executor);

        ResourceManager.initResourceManager(new FabricId(fabricIId.firstKeyOf(Node.class).getNodeId()));
    }

    @Override
    public void deviceAdded(final InstanceIdentifier<FabricNode> fabricIId, final InstanceIdentifier<Node> deviceIId) {

        AddToVxlanFabricInputBuilder builder = new AddToVxlanFabricInputBuilder();
        builder.setNodeId(deviceIId);
        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        getVlanDeviceAdapter().addToVxlanFabric(builder.build());

        ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = rt.read(LogicalDatastoreType.OPERATIONAL, deviceIId);
        Futures.addCallback(readFuture, new FutureCallback<Optional<Node>>(){

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

                switchMgr.addDeviceSwitch(new FabricId(fabricIId.firstKeyOf(Node.class).getNodeId()), deviceIId, vtep);
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("unexecpted exception", t);
            }}, executor);

    }

    @Override
    public void deviceRemoved(InstanceIdentifier<FabricNode> fabricIId, InstanceIdentifier<Node> deviceIId) {

        RmFromVxlanFabricInputBuilder builder = new RmFromVxlanFabricInputBuilder();
        builder.setNodeId(deviceIId);
        builder.setFabricId(fabricIId.firstKeyOf(Node.class).getNodeId());
        getVlanDeviceAdapter().rmFromVxlanFabric(builder.build());

        InstanceIdentifier<DeviceNodes> devicepath = fabricIId.builder().child(FabricAttribute.class)
                .child(DeviceNodes.class, new DeviceNodesKey(new NodeRef(deviceIId))).build();

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.delete(LogicalDatastoreType.OPERATIONAL, devicepath);
        trans.submit();
    }

    @Override
    public void buildLogicSwitch(NodeId nodeid, LswAttributeBuilder lsw, CreateLogicSwitchInput input) {
        long segmentId = ResourceManager.getInstance(input.getFabricId()).allocSeg();
        lsw.setSegmentId(segmentId);

        switchMgr.addLogicSwitch(input.getFabricId(), nodeid, segmentId);

    }

    @Override
    public void buildLogicRouter(NodeId nodeid, LrAttributeBuilder lr, CreateLogicRouterInput input) {
        long vrfctx = ResourceManager.getInstance(input.getFabricId()).allocVrfCtx();
        lr.setVrfCtx(vrfctx);

        switchMgr.addLogicRouter(input.getFabricId(), nodeid, vrfctx);
    }

    @Override
    public void buildLogicPort(TpId tpid, LportAttributeBuilder lp, CreateLogicPortInput input) {

    }

    @Override
    public void endpointAdded(InstanceIdentifier<Endpoint> epIId) {
        // epMgr.addEndPointIId(epIId);
    }

    @Override
    public void endpointUpdated(InstanceIdentifier<Endpoint> epIId) {
        epMgr.addEndPointIId(epIId);
    }

    @Override
    public void buildGateway(NodeId switchid, IpAddress ip, NodeId routerid,  FabricId fabricid) {
        LogicRouterContext routerCtx = switchMgr.getLogicRouterCtx(fabricid, routerid);
        LogicSwitchContext switchCtx = switchMgr.getLogicSwitchCtx(fabricid, switchid);
        switchCtx.associateToRouter(routerCtx, ip);
    }

    @Override
    public boolean composeFabric(FabricAttributeBuilder fabric, ComposeFabricInput input) {
        List<DeviceNodes> devices = input.getDeviceNodes();
        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {

                VxlanDeviceComposeInput augment = deviceNode.getAugmentation(VxlanDeviceComposeInput.class);
                IpAddress vtep = null;
                if (augment != null) {
                    vtep = augment.getVtepIp();
                }
                if (vtep != null) {
                    setVtep2Device(deviceNode.getDeviceRef(), vtep);
                }
            }
        }
        return true;
    }

    private void setVtep2Device(NodeRef node, IpAddress vtep) {
        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();

        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>) node .getValue();
        InstanceIdentifier<Vtep> vtepIId = nodeIId.augmentation(FabricCapableDevice.class).child(Attributes.class)
                .augmentation(VtepAttribute.class).child(Vtep.class);

        VtepBuilder builder = new VtepBuilder();
        builder.setIp(vtep);

        wt.put(LogicalDatastoreType.OPERATIONAL, vtepIId, builder.build(), true);
        wt.submit();
    }

    @Override
    public boolean addNodeToFabric(DeviceNodesBuilder node, AddNodeToFabricInput input) {
        VxlanDeviceAddInput augment = input.getAugmentation(VxlanDeviceAddInput.class);
        IpAddress vtep = null;
        if (augment != null) {
            vtep = augment.getVtepIp();
        }
        if (vtep != null) {
            setVtep2Device(node.getDeviceRef(), vtep);
        }
        return true;
    }

    @Override
    public void fabricDeleted(Node node) {
        FabricNode fabric = node.getAugmentation(FabricNode.class);
        List<DeviceNodes> devices = fabric.getFabricAttribute().getDeviceNodes();
        if (devices != null) {
            for (DeviceNodes deviceNode : devices) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) deviceNode.getDeviceRef().getValue();

                RmFromVxlanFabricInputBuilder builder = new RmFromVxlanFabricInputBuilder();
                builder.setNodeId(deviceIId);
                builder.setFabricId(node.getNodeId());
                getVlanDeviceAdapter().rmFromVxlanFabric(builder.build());
            }
        }

        ResourceManager.freeResourceManager(new FabricId(node.getNodeId()));
    }
}