/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.Constants;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDeviceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDeviceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.AttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.AddToVxlanFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.FabricVxlanDeviceAdapterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.RmFromVxlanFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.VtepBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.bridge.attributes.BridgeExternalIds;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class FabricDeviceManager implements FabricVxlanDeviceAdapterService, DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FabricDeviceManager.class);

    private ListeningExecutorService executor;
    private final DataBroker databroker;

    private final Map<InstanceIdentifier<Node>, DeviceRenderer> renderers = Maps.newHashMap();

    private final RoutedRpcRegistration<FabricVxlanDeviceAdapterService> rpcRegistration;

    final InstanceIdentifier<OvsdbBridgeAugmentation> targetPath = InstanceIdentifier.create(NetworkTopology.class)
            .child(Topology.class).child(Node.class).augmentation(OvsdbBridgeAugmentation.class);;

    public FabricDeviceManager(final DataBroker databroker,
            final RpcProviderRegistry rpcRegistry) {
        this.databroker = databroker;
        executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

        rpcRegistration = rpcRegistry.addRoutedRpcImplementation(FabricVxlanDeviceAdapterService.class, this);

        databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, targetPath, this, DataChangeScope.BASE);
    }

    public DataBroker getDatabroker() {
        return databroker;
    }

    @Override
    public Future<RpcResult<Void>> addToVxlanFabric(final AddToVxlanFabricInput input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(input.getNodeId());
        Preconditions.checkNotNull(input.getFabricId());

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        @SuppressWarnings("unchecked")
        final InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) input.getNodeId();
        final FabricId fabricId = new FabricId(input.getFabricId());

        final Node bridgeNode = OvsSouthboundUtils.getOvsdbBridgeNode(deviceIId, databroker);

        Preconditions.checkNotNull(bridgeNode);

        if (!OvsSouthboundUtils.addVxlanTunnelPort(bridgeNode, databroker)) {
            LOG.error("can not create tunnel port!");
            return Futures.immediateFailedFuture(new RuntimeException("can not create tunnel port"));
        }

		if (!OvsSouthboundUtils.addVxlanGpeTunnelPort(bridgeNode, databroker)) {
		    LOG.error("can not create tunnel port!");
		    return Futures.immediateFailedFuture(new RuntimeException("can not create nsh tunnel port"));
		}

        FabricCapableDeviceBuilder deviceBuilder = new FabricCapableDeviceBuilder();
        AttributesBuilder attributesBuilder = new AttributesBuilder();

        attributesBuilder.setFabricId(input.getFabricId());

        InstanceIdentifier<Node> fabricpath =
                Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(input.getFabricId()));
        attributesBuilder.setFabricRef(new NodeRef(fabricpath));

        deviceBuilder.setAttributes(attributesBuilder.build());

        @SuppressWarnings("unchecked")
        final InstanceIdentifier<FabricCapableDevice> path
            = ((InstanceIdentifier<Node>) input.getNodeId()).augmentation(FabricCapableDevice.class);

        WriteTransaction wt = databroker.newWriteOnlyTransaction();
        wt.merge(LogicalDatastoreType.OPERATIONAL, path, deviceBuilder.build(), true);

        CheckedFuture<Void,TransactionCommitFailedException> future = wt.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void input) throws Exception {
                renderers.put(deviceIId, new DeviceRenderer(executor, databroker, deviceIId, bridgeNode, fabricId));

                return Futures.immediateFuture(result);
            }}, executor);

    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Map<InstanceIdentifier<?>, DataObject> newBridges = change.getCreatedData();

        if (newBridges != null) {
            for (InstanceIdentifier<?> nodeIId : newBridges.keySet()) {
                @SuppressWarnings("unchecked")
				InstanceIdentifier<OvsdbBridgeAugmentation> bridgeIId = (InstanceIdentifier<OvsdbBridgeAugmentation>) nodeIId;
                InstanceIdentifier<Node> targetIId = bridgeIId.firstIdentifierOf(Node.class);
                System.out.println("add routed rpc -->");
                System.out.println(targetIId);
                this.rpcRegistration.registerPath(FabricCapableDeviceContext.class, targetIId);


                OvsdbBridgeAugmentation ovsdbData = (OvsdbBridgeAugmentation) newBridges.get(bridgeIId);
                readPredefinedVtepIp(bridgeIId, ovsdbData);
            }
        }

        Set<InstanceIdentifier<?>> deletedBridges = change.getRemovedPaths();
        if (deletedBridges != null) {
        	for (InstanceIdentifier<?> nodeIId : deletedBridges) {
                @SuppressWarnings("unchecked")
				InstanceIdentifier<OvsdbBridgeAugmentation> bridgeIId = (InstanceIdentifier<OvsdbBridgeAugmentation>) nodeIId;
                InstanceIdentifier<Node> targetIId = bridgeIId.firstIdentifierOf(Node.class);

                this.rpcRegistration.unregisterPath(FabricCapableDeviceContext.class, targetIId);
        	}
        }
    }

    private void readPredefinedVtepIp(final InstanceIdentifier<OvsdbBridgeAugmentation> bridgeIId, OvsdbBridgeAugmentation ovsdbData) {

    	String vtepIp = null;

    	if (ovsdbData.getBridgeExternalIds() != null) {
    		for (BridgeExternalIds externalId : ovsdbData.getBridgeExternalIds()) {
    			if ("vtep-ip".equals(externalId.getBridgeExternalIdKey())) {
    				vtepIp = externalId.getBridgeExternalIdValue();
    				break;
    			}
    		}
    	}

    	if (vtepIp != null) {
			InstanceIdentifier<Node> nodeIId = bridgeIId.firstIdentifierOf(Node.class);
	        InstanceIdentifier<Vtep> vtepIId = nodeIId.augmentation(FabricCapableDevice.class).child(Attributes.class)
	                .augmentation(VtepAttribute.class).child(Vtep.class);

	        VtepBuilder builder = new VtepBuilder();
	        builder.setIp(new IpAddress(vtepIp.toCharArray()));

	        WriteTransaction wt = databroker.newWriteOnlyTransaction();
	        wt.put(LogicalDatastoreType.OPERATIONAL, vtepIId, builder.build(), true);
	        wt.submit();
    	}
    }

    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
    }


    @Override
    public Future<RpcResult<Void>> rmFromVxlanFabric(RmFromVxlanFabricInput input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(input.getNodeId());
        Preconditions.checkNotNull(input.getFabricId());

        @SuppressWarnings("unchecked")
		final InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) input.getNodeId();

        final Node bridgeNode = OvsSouthboundUtils.getOvsdbBridgeNode(deviceIId, databroker);

        DeviceRenderer renderer = renderers.remove(deviceIId);
        if (renderer != null) {
            renderer.close();
        }

        OvsSouthboundUtils.deleteVxlanTunnelPort(bridgeNode, databroker);

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }
}
