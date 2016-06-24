/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.FabricRendererRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.faas.fabric.general.spi.FabricRendererFactory;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.VtepBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.vxlan.rev150930.VxlanDeviceComposeInput;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VXLANFabricProvider implements AutoCloseable, FabricRendererFactory {

    private static final Logger LOG = LoggerFactory.getLogger(VXLANFabricProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private final FabricRendererRegistry rendererRegistry;

    private ListeningExecutorService executor;

    private final Map<InstanceIdentifier<FabricNode>, FabricContext> fabricCtxs = Maps.newHashMap();

    public VXLANFabricProvider(final DataBroker dataProvider,
                             final RpcProviderRegistry rpcRegistry,
                             final NotificationPublishService notificationService,
                             final FabricRendererRegistry rendererRegistry) {
        this.dataBroker = dataProvider;
        this.rpcRegistry = rpcRegistry;
        this.rendererRegistry = rendererRegistry;

        executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

        rendererRegistry.register(UnderlayerNetworkType.VXLAN, this);
    }

    @Override
    public void close() throws Exception {
        rendererRegistry.unregister(UnderlayerNetworkType.VXLAN);
        executor.shutdown();
        for (FabricContext ctx : fabricCtxs.values()) {
            ctx.close();
        }
    }


    @Override
    public FabricRenderer composeFabric(InstanceIdentifier<FabricNode> iid,
            FabricAttributeBuilder fabric, ComposeFabricInput input) {
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
        FabricId fabricId = new FabricId(iid.firstKeyOf(Node.class).getNodeId());
        FabricContext fabricCtx = new FabricContext(fabricId, dataBroker);
        fabricCtxs.put(iid, fabricCtx);

        return new DistributedFabricRenderer(dataBroker, fabricCtx);
    }

    @Override
    public FabricListener createListener(InstanceIdentifier<FabricNode> iid, FabricAttribute fabric) {

        FabricContext fabricCtx = fabricCtxs.get(iid);
        if (fabricCtx == null) {
            FabricId fabricId = new FabricId(iid.firstKeyOf(Node.class).getNodeId());
            fabricCtx = new FabricContext(fabricId, dataBroker);
            fabricCtxs.put(iid, fabricCtx);
        }

        return new DistributedFabricListener(iid, dataBroker, rpcRegistry, fabricCtx);
    }

    private void setVtep2Device(NodeRef node, IpAddress vtep) {
        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>) node .getValue();
        InstanceIdentifier<Vtep> vtepIId = nodeIId.augmentation(FabricCapableDevice.class).child(Attributes.class)
                .augmentation(VtepAttribute.class).child(Vtep.class);

        VtepBuilder builder = new VtepBuilder();
        builder.setIp(vtep);

        trans.put(LogicalDatastoreType.OPERATIONAL, vtepIId, builder.build(), true);
        MdSalUtils.wrapperSubmit(trans, executor);
    }

}
