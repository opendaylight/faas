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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
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

    private final ExecutorService executor;

    public FabricManagementAPIProvider (final DataBroker dataBroker,
            final RpcProviderRegistry rpcRegistry, ExecutorService executor) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;

        this.executor = executor;
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
    //FIXME add real process
    public Future<RpcResult<Void>> decomposeFabric(DecomposeFabricInput input) {
        final FabricId fabricId = input.getFabricId();
        final RpcResultBuilder<Void> resultBuilder = RpcResultBuilder.<Void>success();

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        InstanceIdentifier<Node> fabricpath = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId));

        trans.read(LogicalDatastoreType.OPERATIONAL, fabricpath);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void input) throws Exception {
                return Futures.immediateFuture(resultBuilder.build());
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
    //FIXME register service implements for special fabric instance
    public Future<RpcResult<ComposeFabricOutput>> composeFabric(ComposeFabricInput input) {

        final String newFabricId = UUID.randomUUID().toString();
        final RpcResultBuilder<ComposeFabricOutput> resultBuilder = RpcResultBuilder.<ComposeFabricOutput>success();
        final ComposeFabricOutputBuilder outputBuilder = new ComposeFabricOutputBuilder();


        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        InstanceIdentifier<Node> fabricpath = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(new NodeId(newFabricId)));

        FabricNodeBuilder fabricBuilder = new FabricNodeBuilder();
        FabricAttributeBuilder attrBuilder = new FabricAttributeBuilder();
        attrBuilder.setName(input.getName());
        attrBuilder.setDescription("a fabric node");
        fabricBuilder.setFabricAttribute(attrBuilder.build());

        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setKey(new NodeKey(new NodeId(newFabricId)));
        nodeBuilder.addAugmentation(FabricNode.class, fabricBuilder.build());

        trans.put(LogicalDatastoreType.OPERATIONAL, fabricpath, nodeBuilder.build(), true);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<ComposeFabricOutput>>(){

            @Override
            public ListenableFuture<RpcResult<ComposeFabricOutput>> apply(Void input) throws Exception {
                outputBuilder.setFabricId(new FabricId(newFabricId));
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }});
    }

    @Override
    public Future<RpcResult<Void>> rmNodeFromFabric(RmNodeFromFabricInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addNodeToFabric(AddNodeToFabricInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLinkFromFabric(RmLinkFromFabricInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addLinkToFabric(AddLinkToFabricInput input) {
        // TODO Auto-generated method stub
        return null;
    }
}
