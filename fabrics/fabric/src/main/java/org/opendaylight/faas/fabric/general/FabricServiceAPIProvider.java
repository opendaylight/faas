/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.FabricServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicPortContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicPortContextBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicRouterContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicRouterContextBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicSwitchContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicSwitchContextBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmGatewayInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logic.port.Layer3InfoBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class FabricServiceAPIProvider implements AutoCloseable, FabricServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(FabricServiceAPIProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private RpcRegistration<FabricServiceService> rpcRegistration;

    private final ExecutorService executor;

    public FabricServiceAPIProvider (final DataBroker dataBroker,
            final RpcProviderRegistry rpcRegistry, ExecutorService executor) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;

        this.executor = executor;
    }

    public void start() {
        rpcRegistration = rpcRegistry.addRpcImplementation(FabricServiceService.class, this);
    }

    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
    }

    @Override
    public Future<RpcResult<Void>> rmGateway(RmGatewayInput input) {
        FabricId fabricid = input.getFabricId();
        NodeId routeid = input.getLogicRouter();
        IpAddress gwip = input.getIpAddress();

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        TpId tpOnRouter = null;
        NodeId lswId = null;
        TpId tpOnSwitch = null;
        Link link = null;

        tpOnRouter = new TpId(String.valueOf(gwip.getValue()));
        if (tpOnRouter != null) {
            link = findGWLink(trans, fabricid, tpOnRouter);
        }
        if (link != null) {
            lswId = link.getDestination().getDestNode();
            tpOnSwitch = link.getDestination().getDestTp();
        }

        trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLogicPortIId(fabricid, routeid, tpOnRouter));
        trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLogicPortIId(fabricid, lswId, tpOnSwitch));
        trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLinkIId(fabricid, link.getLinkId()));

        trans.submit();

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    private Link findGWLink(ReadWriteTransaction trans, FabricId fabricid, TpId tpid) {

        InstanceIdentifier<Link> linkIId = InstanceIdentifier.create(NetworkTopology.class)
        .child(Topology.class, new TopologyKey(new TopologyId(fabricid)))
        .child(Link.class);

        CheckedFuture<Optional<Link>,ReadFailedException> readFuture =  trans.read(LogicalDatastoreType.OPERATIONAL, linkIId);

        try {
            Optional<Link> optional = readFuture.get();
            Link link = optional.get();

            return link;

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("", e);
        }
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLogicSwitch(RmLogicSwitchInput input) {

        FabricId fabricId = input.getFabricId();
        NodeId nodeid = input.getNodeId();

        final InstanceIdentifier<Node> newRouterIId = MdSalUtils.createNodeIId(fabricId.getValue(), nodeid);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        trans.delete(LogicalDatastoreType.CONFIGURATION,newRouterIId);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {

                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }}, executor);
    }

    @Override
    public Future<RpcResult<Void>> createGateway(CreateGatewayInput input) {
        final FabricId fabricid = input.getFabricId();
        final NodeId routeId = input.getLogicRouter();
        final NodeId swId = input.getLogicSwitch();
        final IpAddress gwIp = input.getIpAddress();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        // add logic port to Router
        TpId tpid1 = createGWPortOnRouter(fabricid, routeId, gwIp, trans);

        // add logic port to switch
        TpId tpid2 = createGWPortOnSwitch(fabricid, swId, trans);

        // add link
        createLogicLink(fabricid, routeId, swId, trans, tpid1, tpid2);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.buildGateway(swId, gwIp, routeId, fabricid);
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }}, executor);
    }

    private void createLogicLink(FabricId fabricid, NodeId routeId, NodeId swId, WriteTransaction trans, TpId tpid1,
            TpId tpid2) {
        final LinkId linkid = new LinkId(UUID.randomUUID().toString());
        InstanceIdentifier<Link> linkIId = MdSalUtils.createLinkIId(fabricid, linkid);
        LinkBuilder linkBuilder = new LinkBuilder();
        linkBuilder.setLinkId(linkid);
        linkBuilder.setKey(new LinkKey(linkid));

        SourceBuilder srcBuilder = new SourceBuilder();
        srcBuilder.setSourceNode(routeId);
        srcBuilder.setSourceTp(tpid1);
        linkBuilder.setSource(srcBuilder.build());

        DestinationBuilder destBuilder = new DestinationBuilder();
        destBuilder.setDestNode(swId);
        destBuilder.setDestTp(tpid2);
        linkBuilder.setDestination(destBuilder.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,linkIId, linkBuilder.build());
    }

    private TpId createGWPortOnSwitch(FabricId fabricid, NodeId swId, WriteTransaction trans) {
        final TpId tpid = new TpId(UUID.randomUUID().toString());
        InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricid, swId, tpid);

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LogicPortContextBuilder lpCtx = new LogicPortContextBuilder();
        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setName("gateway port");
        lpCtx.setLportAttribute(lpAttr.build());
        tpBuilder.addAugmentation(LogicPortContext.class, lpCtx.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build());

        return tpid;
    }

    private TpId createGWPortOnRouter(FabricId fabricid, NodeId routeId, IpAddress gwIp, WriteTransaction trans) {
        final TpId tpid = new TpId(String.valueOf(gwIp.getValue()));
        InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricid, routeId, tpid);

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LogicPortContextBuilder lpCtx = new LogicPortContextBuilder();
        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setName("gateway port");
        lpAttr.setLayer3Info((new Layer3InfoBuilder()).setIp(gwIp).setForwardEnable(true).build());
        lpCtx.setLportAttribute(lpAttr.build());
        tpBuilder.addAugmentation(LogicPortContext.class, lpCtx.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build());

        return tpid;
    }

    @Override
    public Future<RpcResult<CreateLogicSwitchOutput>> createLogicSwitch(final CreateLogicSwitchInput input) {
        final RpcResultBuilder<CreateLogicSwitchOutput> resultBuilder = RpcResultBuilder.<CreateLogicSwitchOutput>success();
        final CreateLogicSwitchOutputBuilder outputBuilder = new CreateLogicSwitchOutputBuilder();

        FabricId fabricId = input.getFabricId();
        String name = input.getName();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        final String uuid = UUID.randomUUID().toString();
        NodeId newNodeId = new NodeId(name);
        final InstanceIdentifier<Node> newRouterIId = MdSalUtils.createNodeIId(fabricId.getValue(), newNodeId);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setKey(new NodeKey(newNodeId));
        nodeBuilder.setNodeId(newNodeId);

        LogicSwitchContextBuilder lswCtx = new LogicSwitchContextBuilder();
        LswAttributeBuilder lswAttr = new LswAttributeBuilder();
        lswAttr.setName(input.getName());
        lswAttr.setLswUuid(new Uuid(uuid));
        fabricObj.buildLogicSwitch(newNodeId, lswAttr, input);
        lswCtx.setLswAttribute(lswAttr.build());

        nodeBuilder.addAugmentation(LogicSwitchContext.class, lswCtx.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,newRouterIId, nodeBuilder.build(), true);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<CreateLogicSwitchOutput>>(){

            @Override
            public ListenableFuture<RpcResult<CreateLogicSwitchOutput>> apply(Void submitResult) throws Exception {

                outputBuilder.setLswUuid(new Uuid(uuid));
                outputBuilder.setName(input.getName());
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }}, executor);
    }

    @Override
    public Future<RpcResult<Void>> rmLogicRouter(RmLogicRouterInput input) {
        FabricId fabricId = input.getFabricId();
        NodeId nodeid = input.getNodeId();

        final InstanceIdentifier<Node> newRouterIId = MdSalUtils.createNodeIId(fabricId.getValue(), nodeid);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        trans.delete(LogicalDatastoreType.CONFIGURATION,newRouterIId);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {

                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }}, executor);
    }

    @Override
    public Future<RpcResult<Void>> rmLogicPort(RmLogicPortInput input) {
        FabricId fabricId = input.getFabricId();
        NodeId deviceId = input.getLogicDevice();
        TpId tpid = input.getTpId();

        final InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricId, deviceId, tpid);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        trans.delete(LogicalDatastoreType.CONFIGURATION, tpIId);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>(){

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {

                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }}, executor);
    }

    @Override
    public Future<RpcResult<CreateLogicRouterOutput>> createLogicRouter(final CreateLogicRouterInput input) {

        final RpcResultBuilder<CreateLogicRouterOutput> resultBuilder = RpcResultBuilder.<CreateLogicRouterOutput>success();
        final CreateLogicRouterOutputBuilder outputBuilder = new CreateLogicRouterOutputBuilder();

        FabricId fabricId = input.getFabricId();
        String name = input.getName();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        final String uuid = UUID.randomUUID().toString();

        NodeId newNodeId = new NodeId(name);
        final InstanceIdentifier<Node> newRouterIId = MdSalUtils.createNodeIId(fabricId.getValue(), newNodeId);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setKey(new NodeKey(newNodeId));
        nodeBuilder.setNodeId(newNodeId);

        LogicRouterContextBuilder lrCtx = new LogicRouterContextBuilder();
        LrAttributeBuilder lrAttr = new LrAttributeBuilder();
        lrAttr.setName(name);
        lrAttr.setLrUuid(new Uuid(uuid));
        fabricObj.buildLogicRouter(newNodeId, lrAttr, input);

        lrCtx.setLrAttribute(lrAttr.build());

        nodeBuilder.addAugmentation(LogicRouterContext.class, lrCtx.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,newRouterIId, nodeBuilder.build());

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<CreateLogicRouterOutput>>(){

            @Override
            public ListenableFuture<RpcResult<CreateLogicRouterOutput>> apply(Void submitResult) throws Exception {

                outputBuilder.setLrUuid(new Uuid(uuid));
                outputBuilder.setName(input.getName());
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }}, executor);

    }

    @Override
    public Future<RpcResult<CreateLogicPortOutput>> createLogicPort(CreateLogicPortInput input) {
        final RpcResultBuilder<CreateLogicPortOutput> resultBuilder = RpcResultBuilder.<CreateLogicPortOutput>success();
        final CreateLogicPortOutputBuilder outputBuilder = new CreateLogicPortOutputBuilder();

        final FabricId fabricId = input.getFabricId();
        final String name = input.getName();
        NodeId nodeId = input.getLogicDevice();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        //final TpId tpid = new TpId(UUID.randomUUID().toString());
        final TpId tpid = new TpId(input.getName());

        InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricId, nodeId, tpid);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LogicPortContextBuilder lpCtx = new LogicPortContextBuilder();
        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setName(input.getName());
        fabricObj.buildLogicPort(tpid, lpAttr, input);

        lpCtx.setLportAttribute(lpAttr.build());

        tpBuilder.addAugmentation(LogicPortContext.class, lpCtx.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build());

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<CreateLogicPortOutput>>(){

            @Override
            public ListenableFuture<RpcResult<CreateLogicPortOutput>> apply(Void submitResult) throws Exception {
                outputBuilder.setTpId(tpid);
                outputBuilder.setName(name);
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }}, executor);

    }

}