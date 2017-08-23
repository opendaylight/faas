/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
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
import org.opendaylight.faas.fabric.utils.InterfaceManager;
import org.opendaylight.faas.fabric.utils.IpAddressUtils;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddAclInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddPortFunctionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddStaticRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.ClearStaticRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayOutputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmStaticRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.create.logical.port.input.Attribute;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.router.Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.router.RoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.RouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.RouteKey;
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

public class FabricServiceAPIProvider implements FabricServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(FabricServiceAPIProvider.class);

    private final DataBroker dataBroker;
    private final ExecutorService executor;

    public FabricServiceAPIProvider(final DataBroker dataBroker, final ExecutorService executor) {
        this.dataBroker = dataBroker;
        this.executor = executor;
    }

    @Override
    public Future<RpcResult<Void>> rmGateway(RmGatewayInput input) {
        FabricId fabricId = input.getFabricId();
        final NodeId routerId = input.getLogicalRouter();
        IpAddress gwIp = input.getIpAddress();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        TpId tpOnRouter = null;
        NodeId lswId = null;
        TpId tpOnSwitch = null;
        Link link = null;

        tpOnRouter = new TpId(String.valueOf(gwIp.getValue()));
        if (tpOnRouter != null) {
            link = findGWLink(trans, fabricId, tpOnRouter, routerId);
            trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLinkIId(fabricId, link.getLinkId()));
        }
        if (link != null) {
            lswId = link.getDestination().getDestNode();
            tpOnSwitch = link.getDestination().getDestTp();
            trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLogicPortIId(fabricId, lswId, tpOnSwitch));
        }

        final NodeId flswid = lswId == null ? null : lswId;

        trans.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createLogicPortIId(fabricId, routerId, tpOnRouter));

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyGatewayRemoved(flswid, routerId);

            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
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
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final InstanceIdentifier<Node> lswIId = MdSalUtils.createNodeIId(fabricId.getValue(), nodeid);

        ReadOnlyTransaction trans = dataBroker.newReadOnlyTransaction();

        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = trans.read(LogicalDatastoreType.OPERATIONAL,
                lswIId);

        return Futures.transformAsync(readFuture, optional -> {

            if (optional.isPresent()) {
                Node lsw = optional.get();
                fabricObj.notifyLogicSwitchRemoved(lsw);

                WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
                wt.delete(LogicalDatastoreType.OPERATIONAL, lswIId);
                MdSalUtils.wrapperSubmit(wt, executor);
            }

            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }

    @Override
    public Future<RpcResult<CreateGatewayOutput>> createGateway(CreateGatewayInput input) {
        final RpcResultBuilder<CreateGatewayOutput> resultBuilder = RpcResultBuilder
                .<CreateGatewayOutput>success();
        CreateGatewayOutputBuilder outputBuilder = new CreateGatewayOutputBuilder();

        final FabricId fabricId = input.getFabricId();
        final NodeId routerId = input.getLogicalRouter();
        final NodeId swId = input.getLogicalSwitch();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        // add logic port to Router
        TpId tpid1 = createGWPortOnRouter(input, outputBuilder, trans, fabricObj);

        // add logic port to switch
        TpId tpid2 = createGWPortOnSwitch(fabricId, swId, trans);

        // add link
        LinkId linkId = createGatewayLink(routerId, tpid1);
        createLogicLink(fabricId, routerId, swId, trans, tpid1, tpid2, linkId);

        return Futures.transformAsync(trans.submit(), submitResult -> {
            resultBuilder.withResult(outputBuilder);
            return Futures.immediateFuture(resultBuilder.build());
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
        lpCtx.setLportAttribute(lpAttr.build());
        tpBuilder.addAugmentation(LogicalPortAugment.class, lpCtx.build());

        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build(), true);

        return tpid;
    }

    private TpId createGWPortOnRouter(CreateGatewayInput input, CreateGatewayOutputBuilder outputBuilder, WriteTransaction trans, FabricInstance fabricObj) {
        final FabricId fabricId = input.getFabricId();
        final NodeId routerId = input.getLogicalRouter();
        final NodeId swId = input.getLogicalSwitch();

        final IpAddress gwIp = input.getIpAddress();
        IpPrefix network = input.getNetwork();
        if (network == null) {
            network = IpAddressUtils.createDefaultPrefix(gwIp);
        } else {
            network = IpAddressUtils.createGwPrefix(gwIp, network);
        }

        final IpPrefix ipPrefix = network;

        final TpId tpid = new TpId(String.valueOf(gwIp.getValue()));
        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setPortLayer(
                new PortLayerBuilder().setLayer3Info(
                        new Layer3InfoBuilder()
                            .setIp(gwIp)
                            .setNetwork(ipPrefix)
                            .setForwardEnable(true).build()).build());

        fabricObj.buildGateway(swId, ipPrefix, routerId, fabricId, lpAttr);

        LogicalPortAugmentBuilder lpCtx = new LogicalPortAugmentBuilder();
        lpCtx.setLportAttribute(lpAttr.build());
        tpBuilder.addAugmentation(LogicalPortAugment.class, lpCtx.build());

        InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricId, routerId, tpid);
        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build(), true);

        outputBuilder.setTpId(tpid);
        outputBuilder.setPortLayer(lpAttr.getPortLayer());

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
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final String uuid = UUID.randomUUID().toString();
        final NodeId newNodeId = new NodeId(name == null ? uuid : name);
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

        return Futures.transformAsync(trans.submit(), submitResult -> {

            outputBuilder.setLswUuid(new Uuid(uuid));
            outputBuilder.setName(input.getName());
            outputBuilder.setNodeId(newNodeId);
            fabricObj.notifyLogicSwitchCreated(newNodeId, lsw);
            return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> rmLogicalRouter(RmLogicalRouterInput input) {
        FabricId fabricId = input.getFabricId();
        NodeId nodeid = input.getNodeId();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final InstanceIdentifier<Node> routerIId = MdSalUtils.createNodeIId(fabricId.getValue(), nodeid);

        ReadOnlyTransaction trans = dataBroker.newReadOnlyTransaction();

        CheckedFuture<Optional<Node>,ReadFailedException> readFuture = trans.read(LogicalDatastoreType.OPERATIONAL,
                routerIId);

        return Futures.transformAsync(readFuture, optional -> {

            if (optional.isPresent()) {
                Node lr = optional.get();
                fabricObj.notifyLogicRouterRemoved(lr);

                WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
                wt.delete(LogicalDatastoreType.OPERATIONAL, routerIId);
                MdSalUtils.wrapperSubmit(wt, executor);
            }

            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
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

        return Futures.transformAsync(trans.submit(), submitResult -> Futures.immediateFuture(RpcResultBuilder.<Void>success().build()), executor);
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
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final String uuid = UUID.randomUUID().toString();

        final NodeId newNodeId = new NodeId(name == null ? uuid : name);
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

        return Futures.transformAsync(trans.submit(), submitResult -> {

            outputBuilder.setLrUuid(new Uuid(uuid));
            outputBuilder.setName(input.getName());
            outputBuilder.setNodeId(newNodeId);
            fabricObj.notifyLogicRouterCreated(newNodeId, lr);
            return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
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
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final TpId tpid = new TpId(name == null ? UUID.randomUUID().toString() : name);

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setTpId(tpid);
        tpBuilder.setKey(new TerminationPointKey(tpid));

        LportAttributeBuilder lpAttr = new LportAttributeBuilder();
        lpAttr.setName(input.getName());
        Attribute portAttr = input.getAttribute();
        if (portAttr != null) {
            lpAttr.setPortLayer(portAttr.getPortLayer());
            lpAttr.setFabricAcl(portAttr.getFabricAcl());
            lpAttr.setPortFunction(portAttr.getPortFunction());
        }

        fabricObj.buildLogicalPort(tpid, lpAttr, input);

        LogicalPortAugmentBuilder lpCtx = new LogicalPortAugmentBuilder();
        lpCtx.setLportAttribute(lpAttr.build());

        tpBuilder.addAugmentation(LogicalPortAugment.class, lpCtx.build());

        InstanceIdentifier<TerminationPoint> tpIId = MdSalUtils.createLogicPortIId(fabricId, nodeId, tpid);
        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.put(LogicalDatastoreType.OPERATIONAL,tpIId, tpBuilder.build());

        return Futures.transformAsync(trans.submit(), submitResult -> {
            outputBuilder.setTpId(tpid);
            outputBuilder.setName(name);
            return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
        }, executor);

    }

    @Override
    public Future<RpcResult<Void>> addAcl(AddAclInput input) {
        String aclName = input.getAclName();
        FabricId fabricId = input.getFabricId();
        NodeId ldev = input.getLogicalDevice();
        TpId tpId = input.getLogicalPort();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final InstanceIdentifier<FabricAcl> aclIId = fabricObj.addAcl(ldev, tpId, aclName);

        if (aclIId == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException("Can not add acl, maybe the target is not exists !"));
        }

        FabricAclBuilder aclBuilder = new FabricAclBuilder();
        aclBuilder.setFabricAclName(aclName);
        aclBuilder.setKey(new FabricAclKey(aclName));

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,aclIId, aclBuilder.build(), false);

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyAclUpdated(aclIId, false);
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> delAcl(DelAclInput input) {
        String aclName = input.getAclName();
        FabricId fabricId = input.getFabricId();
        NodeId ldev = input.getLogicalDevice();
        TpId tpid = input.getLogicalPort();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        InstanceIdentifier<FabricAcl> aclIId = null;

        if (tpid != null) {
            aclIId = MdSalUtils.createLogicPortIId(fabricId, ldev, tpid)
                        .augmentation(LogicalPortAugment.class)
                        .child(LportAttribute.class)
                        .child(FabricAcl.class, new FabricAclKey(aclName));
        } else {
            aclIId = MdSalUtils.createNodeIId(fabricId, ldev)
                     .augmentation(LogicalSwitchAugment.class)
                     .child(LswAttribute.class)
                     .child(FabricAcl.class, new FabricAclKey(aclName));
        }
        final InstanceIdentifier<FabricAcl> tgtAclIId = aclIId;

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.delete(LogicalDatastoreType.OPERATIONAL,aclIId);

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyAclUpdated(tgtAclIId, true);
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> addPortFunction(AddPortFunctionInput input) {
        final PortFunction function = input.getPortFunction();
        FabricId fabricId = input.getFabricId();
        TpId tpid = input.getLogicalPort();
        NodeId ldev = input.getLogicalDevice();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final InstanceIdentifier<PortFunction> fncIId = MdSalUtils.createLogicPortIId(fabricId, ldev, tpid)
                .augmentation(LogicalPortAugment.class)
                .child(LportAttribute.class)
                .child(PortFunction.class);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,fncIId, function, false);

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyPortFuncUpdated(fncIId, function, false);
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> addStaticRoute(AddStaticRouteInput input) {
        final List<Route> routes = input.getRoute();
        FabricId fabricId = input.getFabricId();
        NodeId ldev = input.getNodeId();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        for (Route route : routes) {
            if (route.getNextHopOptions() == null) {
                return Futures.immediateFailedFuture(
                        new IllegalArgumentException(String.format("next hop is required. (destination = %s)",
                                route.getDestinationPrefix().getValue())));
            }
        }

        final InstanceIdentifier<Routes> routesIId = MdSalUtils.createNodeIId(fabricId, ldev)
                .augmentation(LogicalRouterAugment.class)
                .child(LrAttribute.class)
                .child(Routes.class);

        final List<InstanceIdentifier<Route>> routeKeys = Lists.newArrayList();
        for (Route route : routes) {
            routeKeys.add(routesIId.child(Route.class, route.getKey()));
        }

        RoutesBuilder builder = new RoutesBuilder();
        builder.setRoute(routes);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL, routesIId, builder.build(), true);

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyRouteUpdated(routesIId.firstIdentifierOf(Node.class), routes, false);
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> portBindingLogicalToFabric(PortBindingLogicalToFabricInput input) {

        FabricId fabricId = input.getFabricId();
        TpId tpid = input.getLogicalPort();
        NodeId ldev = input.getLogicalDevice();
        TpId portId = input.getFabricPort();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final InstanceIdentifier<LportAttribute> attrIId = MdSalUtils.createLogicPortIId(fabricId, ldev, tpid)
                .augmentation(LogicalPortAugment.class)
                .child(LportAttribute.class);

        LportAttributeBuilder builder = new LportAttributeBuilder();
        builder.setPortLayer(
                new PortLayerBuilder().setLayer1Info(
                        new Layer1InfoBuilder().setLocation(portId).build()).build());

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,attrIId, builder.build(), false);

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyLogicalPortLocated(attrIId.firstIdentifierOf(TerminationPoint.class), portId);
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }


    private List<SupportingNode> createDefaultSuplNode(FabricId fabricid) {
        SupportingNodeBuilder builder = new SupportingNodeBuilder();

        builder.setNodeRef(fabricid);
        builder.setKey(new SupportingNodeKey(fabricid, new TopologyId(Constants.FABRICS_TOPOLOGY_ID)));

        return Lists.newArrayList(builder.build());
    }

    @Override
    public Future<RpcResult<Void>> portBindingLogicalToDevice(PortBindingLogicalToDeviceInput input) {

        FabricId fabricId = input.getFabricId();
        TpId tpid = input.getLogicalPort();
        NodeId ldev = input.getLogicalDevice();
        TpRef physicalPort = input.getPhysicalPort();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        @SuppressWarnings("unchecked")
        InstanceIdentifier<TerminationPoint> fportIid = InterfaceManager.convDevPort2FabricPort(
                dataBroker, fabricId, (InstanceIdentifier<TerminationPoint>) physicalPort.getValue());
        final TpId portId = fportIid.firstKeyOf(TerminationPoint.class).getTpId();


        final InstanceIdentifier<LportAttribute> attrIId = MdSalUtils.createLogicPortIId(fabricId, ldev, tpid)
                .augmentation(LogicalPortAugment.class)
                .child(LportAttribute.class);

        LportAttributeBuilder builder = new LportAttributeBuilder();
        builder.setPortLayer(
                new PortLayerBuilder().setLayer1Info(
                        new Layer1InfoBuilder().setLocation(
                                fportIid.firstKeyOf(TerminationPoint.class).getTpId())
                        .build())
                .build());

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL,attrIId, builder.build(), false);

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyLogicalPortLocated(attrIId.firstIdentifierOf(TerminationPoint.class), portId);
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> clearStaticRoute(ClearStaticRouteInput input) {
        FabricId fabricId = input.getFabricId();
        NodeId ldev = input.getNodeId();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final InstanceIdentifier<Routes> routesIId = MdSalUtils.createNodeIId(fabricId, ldev)
                .augmentation(LogicalRouterAugment.class)
                .child(LrAttribute.class)
                .child(Routes.class);

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();
        if (MdSalUtils.syncReadOper(trans, routesIId).isPresent()) {
            trans.delete(LogicalDatastoreType.OPERATIONAL,routesIId);

            return Futures.transformAsync(trans.submit(), submitResult -> {
                fabricObj.notifyRouteCleared(routesIId.firstIdentifierOf(Node.class));
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }, executor);
        } else {
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }
    }

    @Override
    public Future<RpcResult<Void>> rmStaticRoute(RmStaticRouteInput input) {
        final List<Ipv4Prefix> destIps = input.getDestinationPrefix();
        FabricId fabricId = input.getFabricId();
        NodeId ldev = input.getNodeId();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException(String.format("fabric %s does not exist", fabricId)));
        }

        final List<Route> routes = Lists.newArrayList();
        for (Ipv4Prefix destIp : destIps) {
            routes.add(new RouteBuilder().setKey(new RouteKey(destIp)).build());
        }

        final InstanceIdentifier<Routes> routesIId = MdSalUtils.createNodeIId(fabricId, ldev)
                .augmentation(LogicalRouterAugment.class)
                .child(LrAttribute.class)
                .child(Routes.class);

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        for (Route route : routes) {
            trans.delete(LogicalDatastoreType.OPERATIONAL,routesIId.child(Route.class, route.getKey()));
        }

        return Futures.transformAsync(trans.submit(), submitResult -> {
            fabricObj.notifyRouteUpdated(routesIId.firstIdentifierOf(Node.class), routes, true);
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }, executor);
    }
}
