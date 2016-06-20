/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddAclInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddPortFunctionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddStaticRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalRouterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalRouterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.DelAclInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.FabricServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalPortAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalRouterAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalRouterAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalSwitchAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalSwitchAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.PortBindingLogicalToDeviceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.PortBindingLogicalToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmGatewayInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicalPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicalRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicalSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.TpRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.PortLayerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.port.layer.Layer1InfoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.port.layer.Layer3InfoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
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

    public FabricServiceAPIProvider(final DataBroker dataBroker,
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
        final NodeId routerid = input.getLogicalRouter();
        IpAddress gwip = input.getIpAddress();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        TpId tpOnRouter = null;
        NodeId lswId = null;
        TpId tpOnSwitch = null;
        Link link = null;

        tpOnRouter = new TpId(String.valueOf(gwip.getValue()));
        if (tpOnRouter != null) {
            link = findGWLink(trans, fabricid, tpOnRouter, routerid);
            trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLinkIId(fabricid, link.getLinkId()));
        }
        if (link != null) {
            lswId = link.getDestination().getDestNode();
            tpOnSwitch = link.getDestination().getDestTp();
            trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLogicPortIId(fabricid, lswId, tpOnSwitch));
        }

        final NodeId flswid = lswId == null ? null : lswId;

        trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLogicPortIId(fabricid, routerid, tpOnRouter));

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyGatewayRemoved(flswid, routerid);

                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    private Link findGWLink(ReadWriteTransaction trans, FabricId fabricid, TpId tpid, NodeId routerid) {

        InstanceIdentifier<Link> linkIId = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(fabricid)))
                .child(Link.class, new LinkKey(this.createGatewayLink(routerid, tpid)));

        CheckedFuture<Optional<Link>,ReadFailedException> readFuture =  trans.read(LogicalDatastoreType.OPERATIONAL,
                linkIId);

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
    public Future<RpcResult<Void>> rmLogicalSwitch(RmLogicalSwitchInput input) {

        final FabricId fabricId = input.getFabricId();
        NodeId nodeid = input.getNodeId();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        final InstanceIdentifier<Node> lswIId = MdSalUtils.createNodeIId(fabricId.getValue(), nodeid);

        ReadOnlyTransaction trans = dataBroker.newReadOnlyTransaction();

        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = trans.read(LogicalDatastoreType.OPERATIONAL,
                lswIId);

        return Futures.transform(readFuture, new AsyncFunction<Optional<Node>, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Optional<Node> optional) throws Exception {

                if (optional.isPresent()) {
                    Node lsw = optional.get();
                    fabricObj.notifyLogicSwitchRemoved(lsw);

                    WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
                    wt.delete(LogicalDatastoreType.OPERATIONAL, lswIId);
                    MdSalUtils.wrapperSubmit(wt, executor);
                }

                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<CreateGatewayOutput>> createGateway(CreateGatewayInput input) {
        final RpcResultBuilder<CreateGatewayOutput> resultBuilder = RpcResultBuilder
                .<CreateGatewayOutput>success();
        final FabricId fabricid = input.getFabricId();
        final NodeId routerId = input.getLogicalRouter();
        final NodeId swId = input.getLogicalSwitch();

        final IpAddress gwIp = input.getIpAddress();
        IpPrefix network = input.getNetwork();
        if (network == null) {
            network = createDefaultPrefix(gwIp);
        } else {
            network = createGwPrefix(gwIp, network);
        }

        final IpPrefix ipPrefix = network;

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        // add logic port to Router
        TpId tpid1 = createGWPortOnRouter(fabricid, routerId, gwIp, ipPrefix, trans);

        // add logic port to switch
        TpId tpid2 = createGWPortOnSwitch(fabricid, swId, trans);

        // add link
        LinkId linkId = createGatewayLink(routerId, tpid1);
        createLogicLink(fabricid, routerId, swId, trans, tpid1, tpid2, linkId);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<CreateGatewayOutput>>() {

            @Override
            public ListenableFuture<RpcResult<CreateGatewayOutput>> apply(Void submitResult) throws Exception {
                fabricObj.buildGateway(swId, ipPrefix, routerId, fabricid);
                return Futures.immediateFuture(resultBuilder.build());
            }
        }, executor);
    }

    private LinkId createGatewayLink(NodeId routerId, TpId gwPort) {
        return new LinkId(String.format("gateway:%s, router:%s", gwPort.getValue(), routerId));
    }

    private void createLogicLink(FabricId fabricid, NodeId routeId, NodeId swId, WriteTransaction trans, TpId tpid1,
            TpId tpid2, LinkId lid) {
        final LinkId linkid = lid == null ? new LinkId(UUID.randomUUID().toString()) : lid;
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

        InstanceIdentifier<Link> linkIId = MdSalUtils.createLinkIId(fabricid, linkid);
        trans.put(LogicalDatastoreType.OPERATIONAL,linkIId, linkBuilder.build());
    }

    private TpId createGWPortOnSwitch(FabricId fabricid, NodeId swId, WriteTransaction trans) {
        final TpId tpid = new TpId(UUID.randomUUID().toString());
        final InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricid, swId, tpid);

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LogicalPortAugmentBuilder lpCtx = new LogicalPortAugmentBuilder();
        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setName("gateway port");
        lpCtx.setLportAttribute(lpAttr.build());
        tpBuilder.addAugmentation(LogicalPortAugment.class, lpCtx.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build());

        return tpid;
    }

    private TpId createGWPortOnRouter(FabricId fabricid, NodeId routeId, IpAddress gwIp,
            IpPrefix prefix, WriteTransaction trans) {
        final TpId tpid = new TpId(String.valueOf(gwIp.getValue()));
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setPortLayer(
                new PortLayerBuilder().setLayer3Info(
                        new Layer3InfoBuilder()
                            .setIp(gwIp)
                            .setNetwork(prefix)
                            .setForwardEnable(true).build()).build())
            .setName("gateway port");

        LogicalPortAugmentBuilder lpCtx = new LogicalPortAugmentBuilder();
        lpCtx.setLportAttribute(lpAttr.build());
        tpBuilder.addAugmentation(LogicalPortAugment.class, lpCtx.build());

        InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricid, routeId, tpid);
        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build());

        return tpid;
    }

    @Override
    public Future<RpcResult<CreateLogicalSwitchOutput>> createLogicalSwitch(final CreateLogicalSwitchInput input) {
        final RpcResultBuilder<CreateLogicalSwitchOutput> resultBuilder = RpcResultBuilder
                .<CreateLogicalSwitchOutput>success();
        final CreateLogicalSwitchOutputBuilder outputBuilder = new CreateLogicalSwitchOutputBuilder();

        FabricId fabricId = input.getFabricId();
        String name = input.getName();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        final String uuid = UUID.randomUUID().toString();
        final NodeId newNodeId = new NodeId(name);
        final InstanceIdentifier<Node> newRouterIId = MdSalUtils.createNodeIId(fabricId.getValue(), newNodeId);

        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setKey(new NodeKey(newNodeId));
        nodeBuilder.setNodeId(newNodeId);

        nodeBuilder.setSupportingNode(createDefaultSuplNode(fabricId));

        LswAttributeBuilder lswAttr = new LswAttributeBuilder();
        lswAttr.setName(input.getName());
        lswAttr.setLswUuid(new Uuid(uuid));
        fabricObj.buildLogicalSwitch(newNodeId, lswAttr, input);

        LogicalSwitchAugmentBuilder lswCtx = new LogicalSwitchAugmentBuilder();
        lswCtx.setLswAttribute(lswAttr.build());

        nodeBuilder.addAugmentation(LogicalSwitchAugment.class, lswCtx.build());

        final Node lsw = nodeBuilder.build();
        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.put(LogicalDatastoreType.OPERATIONAL,newRouterIId, lsw);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<CreateLogicalSwitchOutput>>() {

            @Override
            public ListenableFuture<RpcResult<CreateLogicalSwitchOutput>> apply(Void submitResult) throws Exception {

                outputBuilder.setLswUuid(new Uuid(uuid));
                outputBuilder.setName(input.getName());
                outputBuilder.setNodeId(newNodeId);
                fabricObj.notifyLogicSwitchCreated(newNodeId, lsw);
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> rmLogicalRouter(RmLogicalRouterInput input) {
        FabricId fabricId = input.getFabricId();
        NodeId nodeid = input.getNodeId();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        final InstanceIdentifier<Node> routerIId = MdSalUtils.createNodeIId(fabricId.getValue(), nodeid);

        ReadOnlyTransaction trans = dataBroker.newReadOnlyTransaction();

        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = trans.read(LogicalDatastoreType.OPERATIONAL,
                routerIId);

        return Futures.transform(readFuture, new AsyncFunction<Optional<Node>, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Optional<Node> optional) throws Exception {

                if (optional.isPresent()) {
                    Node lr = optional.get();
                    fabricObj.notifyLogicRouterRemoved(lr);

                    WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
                    wt.delete(LogicalDatastoreType.OPERATIONAL, routerIId);
                    MdSalUtils.wrapperSubmit(wt, executor);
                }

                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> rmLogicalPort(RmLogicalPortInput input) {
        FabricId fabricId = input.getFabricId();
        NodeId deviceId = input.getLogicalDevice();
        TpId tpid = input.getTpId();

        final InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricId, deviceId, tpid);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        trans.delete(LogicalDatastoreType.OPERATIONAL, tpIId);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {

                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<CreateLogicalRouterOutput>> createLogicalRouter(final CreateLogicalRouterInput input) {

        final RpcResultBuilder<CreateLogicalRouterOutput> resultBuilder = RpcResultBuilder
                .<CreateLogicalRouterOutput>success();
        final CreateLogicalRouterOutputBuilder outputBuilder = new CreateLogicalRouterOutputBuilder();

        FabricId fabricId = input.getFabricId();
        String name = input.getName();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        final String uuid = UUID.randomUUID().toString();

        final NodeId newNodeId = new NodeId(name);
        final InstanceIdentifier<Node> newRouterIId = MdSalUtils.createNodeIId(fabricId.getValue(), newNodeId);

        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setKey(new NodeKey(newNodeId));
        nodeBuilder.setNodeId(newNodeId);

        nodeBuilder.setSupportingNode(createDefaultSuplNode(fabricId));

        LrAttributeBuilder lrAttr = new LrAttributeBuilder();
        lrAttr.setName(name);
        lrAttr.setLrUuid(new Uuid(uuid));
        fabricObj.buildLogicalRouter(newNodeId, lrAttr, input);

        LogicalRouterAugmentBuilder lrCtx = new LogicalRouterAugmentBuilder();
        lrCtx.setLrAttribute(lrAttr.build());

        nodeBuilder.addAugmentation(LogicalRouterAugment.class, lrCtx.build());

        final Node lr = nodeBuilder.build();
        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.put(LogicalDatastoreType.OPERATIONAL,newRouterIId, lr, true);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<CreateLogicalRouterOutput>>() {

            @Override
            public ListenableFuture<RpcResult<CreateLogicalRouterOutput>> apply(Void submitResult) throws Exception {

                outputBuilder.setLrUuid(new Uuid(uuid));
                outputBuilder.setName(input.getName());
                outputBuilder.setNodeId(newNodeId);
                fabricObj.notifyLogicRouterCreated(newNodeId, lr);
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }
        }, executor);

    }

    @Override
    public Future<RpcResult<CreateLogicalPortOutput>> createLogicalPort(CreateLogicalPortInput input) {
        final RpcResultBuilder<CreateLogicalPortOutput> resultBuilder = RpcResultBuilder
                .<CreateLogicalPortOutput>success();
        final CreateLogicalPortOutputBuilder outputBuilder = new CreateLogicalPortOutputBuilder();

        final FabricId fabricId = input.getFabricId();
        final String name = input.getName();
        final NodeId nodeId = input.getLogicalDevice();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        //final TpId tpid = new TpId(UUID.randomUUID().toString());
        final TpId tpid = new TpId(input.getName());

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LogicalPortAugmentBuilder lpCtx = new LogicalPortAugmentBuilder();
        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setName(input.getName());
        fabricObj.buildLogicalPort(tpid, lpAttr, input);

        lpCtx.setLportAttribute(lpAttr.build());

        tpBuilder.addAugmentation(LogicalPortAugment.class, lpCtx.build());

        InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricId, nodeId, tpid);
        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build());

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<CreateLogicalPortOutput>>() {

            @Override
            public ListenableFuture<RpcResult<CreateLogicalPortOutput>> apply(Void submitResult) throws Exception {
                outputBuilder.setTpId(tpid);
                outputBuilder.setName(name);
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }
        }, executor);

    }

    @Override
    public Future<RpcResult<Void>> addAcl(AddAclInput input) {
        String aclName = input.getAclName();
        FabricId fabricid = input.getFabricId();
        NodeId ldev = input.getLogicalDevice();
        TpId tpid = input.getLogicalPort();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("Fabric is not exist!"));
        }

        final InstanceIdentifier<FabricAcl> aclIId = fabricObj.addAcl(ldev, tpid, aclName);

        if (aclIId == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException("Can not add acl, maybe the target is not exists !"));
        }

        FabricAclBuilder aclBuilder = new FabricAclBuilder();
        aclBuilder.setFabricAclName(aclName);
        aclBuilder.setKey(new FabricAclKey(aclName));

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,aclIId, aclBuilder.build(), false);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyAclUpdate(aclIId, false);
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> delAcl(DelAclInput input) {
        String aclName = input.getAclName();
        FabricId fabricid = input.getFabricId();
        NodeId ldev = input.getLogicalDevice();
        TpId tpid = input.getLogicalPort();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        InstanceIdentifier<FabricAcl> aclIId = null;

        if (tpid != null) {
            aclIId = MdSalUtils.createLogicPortIId(fabricid, ldev, tpid)
                        .augmentation(LogicalPortAugment.class)
                        .child(LportAttribute.class)
                        .child(FabricAcl.class, new FabricAclKey(aclName));
        } else {
            aclIId = MdSalUtils.createNodeIId(fabricid, ldev)
                     .augmentation(LogicalSwitchAugment.class)
                     .child(LswAttribute.class)
                     .child(FabricAcl.class, new FabricAclKey(aclName));
        }
        final InstanceIdentifier<FabricAcl> tgtAclIId = aclIId;

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.delete(LogicalDatastoreType.OPERATIONAL,aclIId);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyAclUpdate(tgtAclIId, true);
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> addPortFunction(AddPortFunctionInput input) {
        final PortFunction function = input.getPortFunction();
        FabricId fabricid = input.getFabricId();
        TpId tpid = input.getLogicalPort();
        NodeId ldev = input.getLogicalDevice();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("Fabric is not exist!"));
        }

        final InstanceIdentifier<PortFunction> fncIId = MdSalUtils.createLogicPortIId(fabricid, ldev, tpid)
                .augmentation(LogicalPortAugment.class)
                .child(LportAttribute.class)
                .child(PortFunction.class);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,fncIId, function, false);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyPortFuncUpdate(fncIId, function, false);
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> addStaticRoute(AddStaticRouteInput input) {
        final List<Route> routes = input.getRoute();
        FabricId fabricid = input.getFabricId();
        NodeId ldev = input.getNodeId();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("Fabric is not exist!"));
        }

        final InstanceIdentifier<LrAttribute> attrIId = MdSalUtils.createNodeIId(fabricid, ldev)
                .augmentation(LogicalRouterAugment.class)
                .child(LrAttribute.class);

        final List<InstanceIdentifier<Route>> routeKeys = Lists.newArrayList();
        for (Route route : routes) {
            routeKeys.add(attrIId.child(Route.class, route.getKey()));
        }

        LrAttributeBuilder builder = new LrAttributeBuilder();
        builder.setRoute(routes);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,attrIId, builder.build(), false);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyRouteUpdate(attrIId.firstIdentifierOf(Node.class), routes, false);
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> portBindingLogicalToFabric(PortBindingLogicalToFabricInput input) {

        FabricId fabricid = input.getFabricId();
        TpId tpid = input.getLogicalPort();
        NodeId ldev = input.getLogicalDevice();
        TpId portId = input.getFabricPort();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("Fabric is not exist!"));
        }

        final InstanceIdentifier<LportAttribute> attrIId = MdSalUtils.createLogicPortIId(fabricid, ldev, tpid)
                .augmentation(LogicalPortAugment.class)
                .child(LportAttribute.class);

        LportAttributeBuilder builder = new LportAttributeBuilder();
        builder.setPortLayer(
                new PortLayerBuilder().setLayer1Info(
                        new Layer1InfoBuilder().setLocation(portId).build()).build());

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,attrIId, builder.build(), false);

        return Futures.transform(trans.submit(), new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyLogicalPortLocated(attrIId.firstIdentifierOf(TerminationPoint.class), portId);
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }, executor);
    }

    private IpPrefix createGwPrefix(IpAddress ipAddress, IpPrefix network) {
        StringBuilder buf = new StringBuilder();
        buf.append(ipAddress.getValue());
        String str = network.getIpv4Prefix().getValue();
        buf.append(str.substring(str.indexOf("/")));
        return new IpPrefix(new Ipv4Prefix(buf.toString()));
    }

    private IpPrefix createDefaultPrefix(IpAddress ipAddress) {
        if (ipAddress.getIpv4Address() == null) {
            return null;
        }
        String ipv4 = ipAddress.getIpv4Address().getValue();
        long mask = getDefaultMask(ipv4);
        return new IpPrefix(new Ipv4Prefix(String.format("%s/%d", ipv4, mask)));
    }

    private static long getDefaultMask(String ipv4Address) {
        long ipLong = (InetAddresses.coerceToInteger(InetAddresses.forString(ipv4Address))) & 0xFFFFFFFFL;
        if (ipLong < 2147483647L) {    // 0.0.0.0 - 127.255.255.255
            return 8;
        }
        if (ipLong < 3221225471L) { // 128.0.0.0 - 191.255.255.255
            return 16;
        }
        if (ipLong < 3758096383L) { // 192.0.0.0 - 223.255.255.255
            return 24;
        }
        return 32;// other
    }

    private List<SupportingNode> createDefaultSuplNode(FabricId fabricid) {
        SupportingNodeBuilder builder = new SupportingNodeBuilder();

        builder.setNodeRef(fabricid);
        builder.setKey(new SupportingNodeKey(fabricid, new TopologyId(Constants.FABRICS_TOPOLOGY_ID)));

        return Lists.newArrayList(builder.build());
    }

    @Override
    public Future<RpcResult<Void>> portBindingLogicalToDevice(PortBindingLogicalToDeviceInput input) {
        // TODO Auto-generated method stub
        return null;
    }

}