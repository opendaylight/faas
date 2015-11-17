/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddLinkToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.DecomposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.GetAllFabricOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.GetAllFabricOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.RmLinkFromFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.RmNodeFromFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceLinks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceLinksBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceLinksKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.LinkRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.fabric.impl.rev150930.FabricsSetting;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.fabric.impl.rev150930.FabricsSettingBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class FabricManagementAPIProvider implements AutoCloseable, FabricService {

    private static final Logger LOG = LoggerFactory.getLogger(FabricManagementAPIProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private RpcRegistration<FabricService> rpcRegistration;

    private final FabricRendererRegistry rendererMgr;

    private final ExecutorService executor;

    public FabricManagementAPIProvider (final DataBroker dataBroker,
            final RpcProviderRegistry rpcRegistry, ExecutorService executor, FabricRendererRegistry rendererMgr) {

        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;

        this.executor = executor;

        this.rendererMgr = rendererMgr;

    }

    public void start() {
        rpcRegistration = rpcRegistry.addRpcImplementation(FabricService.class, this);
    }

    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
    }

    @Override
    public Future<RpcResult<Void>> decomposeFabric(DecomposeFabricInput input) {
        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        if ( input == null) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException("fabricId can not be empty!"));
        }
        final FabricId fabricId = input.getFabricId();

        if ( fabricId == null) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException("fabricId can not be empty!"));
        }

        ReadWriteTransaction rt = dataBroker.newReadWriteTransaction();

        final InstanceIdentifier<Node> fabricpath = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId));

        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = rt.read(LogicalDatastoreType.OPERATIONAL, fabricpath);

        return Futures.transform(readFuture, new AsyncFunction<Optional<Node>, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Optional<Node> optional) throws Exception {

                Node fabric = optional.get();
                FabricInstanceCache.INSTANCE.retrieveFabric(fabricId).fabricDeleted(fabric);

                WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
                wt.delete(LogicalDatastoreType.OPERATIONAL, fabricpath);
                wt.submit();

                FabricInstanceCache.INSTANCE.removeFabric(fabricId);

                return Futures.immediateFuture(result);
            }});
    }

    @Override
    public Future<RpcResult<GetAllFabricOutput>> getAllFabric() {

        final SettableFuture<RpcResult<GetAllFabricOutput>> futureResult = SettableFuture.create();
        final RpcResultBuilder<GetAllFabricOutput> resultBuilder = RpcResultBuilder.<GetAllFabricOutput>success();
        final GetAllFabricOutputBuilder outputBuilder = new GetAllFabricOutputBuilder();

        ReadOnlyTransaction trans = dataBroker.newReadOnlyTransaction();
        ListenableFuture<Optional<Topology>> readFuture = trans.read(LogicalDatastoreType.OPERATIONAL, Constants.DOM_FABRICS_PATH);
        Futures.addCallback(readFuture, new FutureCallback<Optional<Topology>>() {

            @Override
            public void onSuccess(Optional<Topology> result) {
                if (result.isPresent()) {
                    List<Node> nodes = result.get().getNode();
                    List<FabricId> fabricIds = new ArrayList<FabricId>();
                    if (nodes != null) {
                        for (Node node : nodes) {
                            FabricNode fnode = node.getAugmentation(FabricNode.class);
                            if (fnode != null) {
                                fabricIds.add(new FabricId(node.getNodeId()));
                            }
                        }
                        outputBuilder.setFabricId(fabricIds);
                    }
                }
                futureResult.set(resultBuilder.withResult(outputBuilder.build()).build());

            }

            @Override
            public void onFailure(Throwable t) {
                LOG.debug( "Failed to read network-topology dom", t);
                futureResult.setException(t);
            }}, executor);

        return futureResult;
    }

    @Override
    public Future<RpcResult<ComposeFabricOutput>> composeFabric(final ComposeFabricInput input)  {

        final FabricId fabricId = new FabricId(String.format("fabric:%d", this.genNextFabricNum()));
        final RpcResultBuilder<ComposeFabricOutput> resultBuilder = RpcResultBuilder.<ComposeFabricOutput>success();
        final ComposeFabricOutputBuilder outputBuilder = new ComposeFabricOutputBuilder();

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        final InstanceIdentifier<FabricNode> fabricpath = MdSalUtils.createFabricIId(fabricId);

        FabricNodeBuilder fabricBuilder = new FabricNodeBuilder();
        FabricAttributeBuilder attrBuilder = new FabricAttributeBuilder();
        attrBuilder.setName(input.getName());

        attrBuilder.setDescription(input.getDescription());
        attrBuilder.setType(input.getType());
        attrBuilder.setDeviceLinks(input.getDeviceLinks());
        attrBuilder.setDeviceNodes(getDeviceNodesInput(input.getDeviceNodes()));
        attrBuilder.setOptions(input.getOptions());

        if (!rendererMgr.getFabricRenderer(input.getType()).composeFabric(attrBuilder, input)) {
            LOG.error("can not compose fabric due the renderer return false.");
        }

        fabricBuilder.setFabricAttribute(attrBuilder.build());
        FabricInstanceCache.INSTANCE.addFabric(fabricId, input.getType(), rendererMgr.getFabricRenderer(input.getType()));

        trans.put(LogicalDatastoreType.OPERATIONAL, fabricpath, fabricBuilder.build(), true);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        FabricInstanceCache.INSTANCE.addFabric(fabricId, input.getType(), rendererMgr.getFabricRenderer(input.getType()));

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<ComposeFabricOutput>>(){

            @Override
            public ListenableFuture<RpcResult<ComposeFabricOutput>> apply(Void submitResult) throws Exception {
                outputBuilder.setFabricId(fabricId);

                FabricInstanceCache.INSTANCE.retrieveFabric(fabricId).fabricCreated(fabricpath);
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }});
    }

    private List<DeviceNodes> getDeviceNodesInput(List<DeviceNodes> input) {
        if (input == null) {
            return null;
        }
        List<DeviceNodes> ret = Lists.newArrayList();
        for (DeviceNodes node : input) {
            DeviceNodesBuilder builder = new DeviceNodesBuilder();
            builder.setDeviceRef(node.getDeviceRef());
            builder.setKey(node.getKey());
            builder.setRole(node.getRole());
            ret.add(builder.build());
        }
        return ret;
    }

    private long genNextFabricNum() {
        long ret = 1;

        FabricsSettingBuilder settingBuilder = new FabricsSettingBuilder();
        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        InstanceIdentifier<FabricsSetting> fabricImplPath = InstanceIdentifier.create(FabricsSetting.class);
        ListenableFuture<Optional<FabricsSetting>> readFuture = trans.read(LogicalDatastoreType.OPERATIONAL, fabricImplPath);
        Optional<FabricsSetting> optional;
        try {
            optional = readFuture.get();
            if (optional.isPresent()) {
                ret = optional.get().getNextFabricNum();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("can not read fabric setting", e);
        }

        settingBuilder.setNextFabricNum(ret + 1);
        trans.put(LogicalDatastoreType.OPERATIONAL, fabricImplPath, settingBuilder.build());
        trans.submit();

        return ret;
    }

    @Override
    public Future<RpcResult<Void>> rmNodeFromFabric(RmNodeFromFabricInput input) {
        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        FabricId fabricId = input.getFabricId();
        final NodeRef node = input.getNodeRef();

        final InstanceIdentifier<FabricNode> fabricpath = MdSalUtils.createFabricIId(fabricId);

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        CheckedFuture<Optional<FabricNode>,ReadFailedException> readfuture = trans.read(LogicalDatastoreType.OPERATIONAL, fabricpath);
        Optional<FabricNode> optional = null;
        try {
            optional = readfuture.checkedGet();
        } catch (ReadFailedException e) {
            return Futures.immediateFailedCheckedFuture(e);
        }

        if (!optional.isPresent()) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        FabricNode fabric = optional.get();
        final UnderlayerNetworkType fabricType = fabric.getFabricAttribute().getType();

        InstanceIdentifier<DeviceNodes> devicepath = Constants.DOM_FABRICS_PATH
                    .child(Node.class, new NodeKey(fabricId)).augmentation(FabricNode.class)
                    .child(FabricAttribute.class)
                    .child(DeviceNodes.class, new DeviceNodesKey(node));

        trans.delete(LogicalDatastoreType.OPERATIONAL, devicepath);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                rendererMgr.getFabricRenderer(fabricType).deviceRemoved(fabricpath, (InstanceIdentifier<Node>) node.getValue());
                return Futures.immediateFuture(result);
            }});
    }

    @Override
    public Future<RpcResult<Void>> addNodeToFabric(AddNodeToFabricInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        final FabricId fabricId = input.getFabricId();
        final NodeRef node = input.getNodeRef();

        final InstanceIdentifier<DeviceNodes> path = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId))
                .augmentation(FabricNode.class).child(FabricAttribute.class)
                .child(DeviceNodes.class, new DeviceNodesKey(node));

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        DeviceNodesBuilder nodeBuilder = new DeviceNodesBuilder();
        nodeBuilder.setKey(new DeviceNodesKey(node)).setDeviceRef(node).build();
        fabricObj.addNodeToFabric(nodeBuilder, input);
        trans.put(LogicalDatastoreType.OPERATIONAL, path, nodeBuilder.build(), true);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.deviceAdded(path.firstIdentifierOf(FabricNode.class), (InstanceIdentifier<Node>) node.getValue());
                return Futures.immediateFuture(result);
            }});
    }

    @Override
    public Future<RpcResult<Void>> rmLinkFromFabric(RmLinkFromFabricInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        FabricId fabricId = input.getFabricId();
        LinkRef link = input.getLinkRef();

        InstanceIdentifier<DeviceLinks> devicepath = Constants.DOM_FABRICS_PATH
                    .child(Node.class, new NodeKey(fabricId)).augmentation(FabricNode.class)
                    .child(FabricAttribute.class)
                    .child(DeviceLinks.class, new DeviceLinksKey(link));

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        trans.delete(LogicalDatastoreType.OPERATIONAL, devicepath);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                return Futures.immediateFuture(result);
            }});
    }

    @Override
    public Future<RpcResult<Void>> addLinkToFabric(AddLinkToFabricInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        FabricId fabricId = input.getFabricId();
        LinkRef link = input.getLinkRef();

        final InstanceIdentifier<DeviceLinks> path = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId))
                .augmentation(FabricNode.class).child(FabricAttribute.class)
                .child(DeviceLinks.class, new DeviceLinksKey(link));

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        DeviceLinksBuilder linkBuilder = new DeviceLinksBuilder();
        linkBuilder.setKey(new DeviceLinksKey(link)).setLinkRef(link).build();

        trans.put(LogicalDatastoreType.OPERATIONAL, path, linkBuilder.build(), false);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                return Futures.immediateFuture(result);
            }});
    }
}
