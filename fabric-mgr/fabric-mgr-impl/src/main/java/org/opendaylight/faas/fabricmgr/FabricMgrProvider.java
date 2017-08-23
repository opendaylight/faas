/*
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import com.google.common.base.Optional;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.PrimMinimumSpanningTree;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.InterfaceManager;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.uln.cache.LogicalRouterMappingInfo;
import org.opendaylight.faas.uln.cache.LogicalSwitchMappingInfo;
import org.opendaylight.faas.uln.cache.PortMappingInfo;
import org.opendaylight.faas.uln.cache.RenderedLayer2Link;
import org.opendaylight.faas.uln.cache.RenderedLayer3Link;
import org.opendaylight.faas.uln.cache.RenderedLinkKey;
import org.opendaylight.faas.uln.cache.RenderedRouter;
import org.opendaylight.faas.uln.cache.RenderedSwitch;
import org.opendaylight.faas.uln.cache.SubnetMappingInfo;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.port.PrivateIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.subnet.ExternalGateways;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VcLneId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VfabricPortId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.UpdateLneLayer3RoutingtableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.ports.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.vfabric.service.rev151010.update.vf.lr.routingtable.input.Routingtable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.vfabric.service.rev151010.update.vf.lr.routingtable.input.RoutingtableBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FabricMgrProvider - fabric resource management via virtual container for each tenant.
 *
 */
public class FabricMgrProvider implements FabricMgrService {

    private static final Logger LOG = LoggerFactory.getLogger(FabricMgrProvider.class);
    private final VContainerNetNodeServiceProvider netNodeServiceProvider;
    private final FabMgrDatastoreUtil fabMgrDatastoreUtil;
    private final VContainerMgr containerMgr;

    //For internal L3 connection usage.
    private Ipv4Address reservedGatewayAddress = new Ipv4Address("10.123.17.1");
    private final Ipv4Address defaultIP = new Ipv4Address("0.0.0.0");
    private final Ipv4Prefix defaultIpv4LinkPrefix = new Ipv4Prefix("10.123.17.0/24");
    private final UserLogicalNetworkCacheStore ulnStore;

    public FabricMgrProvider(final VContainerMgr containerMgr,
            final VContainerNetNodeServiceProvider netNodeServiceProvider,
            final FabMgrDatastoreUtil fabMgrDatastoreUtil,
            final UserLogicalNetworkCacheStore ulnStore) {
        super();
        this.fabMgrDatastoreUtil = fabMgrDatastoreUtil;
        this.containerMgr = containerMgr;
        this.netNodeServiceProvider = netNodeServiceProvider;
        this.ulnStore = ulnStore;
    }

    /**
     * createLneLayer2 - create a layer 2 logical network device. i.e. logical switch.
     *
     * @param tenantId - tenant identifier
     * @param fabricId - fabric identifier
     * @param lsw - the user level logical switch which may contains multiple rendered logical switches.
     * @param uln - the user logical network info
     * @return the rendered logical switch ID on the given fabric.
     */
    @Override
    public NodeId createLneLayer2(Uuid tenantId, NodeId fabricId, Uuid lsw, UserLogicalNetworkCache uln) {

        org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid flsw = toUuid(lsw);

        // Check resource availability
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(fabricId)) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vfabricId ({}) is not in tenant {} 's vcontainer.",
                    fabricId,
                    tenantId.getValue());
            return null; // ---->
        }

        builder.setVfabricId(fabricId);
        builder.setTenantId(new TenantId(tenantId));
        builder.setLswUuid(lsw);
        //builder.setName(lsw.getValue());
        //builder.setSegmentId(); //leave it to FaaS to determine for now.

        NodeId renderedLSWId = null;
        Future<RpcResult<CreateLneLayer2Output>> result = this.netNodeServiceProvider.createLneLayer2(builder.build());
        try {
            RpcResult<CreateLneLayer2Output> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer2: createLneLayer2 RPC success");
                CreateLneLayer2Output createLswOutput = output.getResult();
                renderedLSWId = createLswOutput.getLneId();

                LOG.debug("FABMGR: createLneLayer2: lswId={}", renderedLSWId.getValue());


                RenderedSwitch renderedSW = new RenderedSwitch(fabricId, lsw, renderedLSWId);
                uln.getLswStore()
                    .get(flsw)
                    .addRenderedSwitch(renderedSW);

            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer2: createLneLayer2 RPC failed: {}", e);
        }

        Map<String, RenderedSwitch> sws = uln.getLswStore().get(flsw).getRenderedSwitches();
        Graph<String, Link> graph = calcMinimumSpanningTree(new ArrayList<>(sws.keySet()));
        TopologyBuilder topo = new TopologyBuilder();

        //we only need to store the link info.
        topo.setTopologyId(new TopologyId(lsw.getValue()));
        topo.setLink(new ArrayList<>(graph.getEdges()));
        fabMgrDatastoreUtil.putData(LogicalDatastoreType.CONFIGURATION,
                FabMgrYangDataUtil.buildTopologyPath(lsw.getValue()),topo.build());

        bridgeAllSegments(tenantId, uln, lsw, topo.build(), sws);

        return renderedLSWId;
    }



    /**
     * Calculate and return the minimum spanning free of the Graph top.
     * @param topo - the graph.
     * @return the "pruned" minimal spanning tree.
     */
    private Graph<String, Link> calcMinimumSpanningTree(List<String> fabrics)
    {
        Topology topo = this.getFabricTopology();
        if (topo == null) {
            LOG.error("Failed to read Fabric Topology!");
            return null;
        }

        UndirectedSparseGraph<String, Link> graph = new UndirectedSparseGraph<>();
        for (Node node : topo.getNode()) {
            graph.addVertex(node.getNodeId().getValue());
        }

        if (topo.getLink() != null) {
            for (Link link : topo.getLink())
            {
                graph.addEdge(link, link.getSource().getSourceNode().getValue(), link.getDestination().getDestNode().getValue());
            }
        }

        PrimMinimumSpanningTree<String, Link> alg =
                new PrimMinimumSpanningTree<>(UndirectedSparseGraph.<String, Link>getFactory());

        Graph<String, Link> miniTree = alg.transform(graph);

        return pruneTree(miniTree, fabrics);
    }

    /**
     *
     * @param tree - it has to be a tree. otherwise the alg will fail. we need
     *               to enforce this in the code. to be done later.
     * @param nodes - the nodes need to be contained.
     * @return a pruned tree with minimal nodes to connect. notes that we can not remove
     *         the vertex or edge within the loop, it will generate concurrent access violation.
     */
    private Graph<String, Link> pruneTree(Graph<String, Link> tree, List<String> nodes)
    {
        boolean found = false;
        List<String> nodesToBePruned = new ArrayList<>();
        List<Link> edgesToBePruned = new ArrayList<>();

        if( tree == null || nodes == null || nodes.containsAll(tree.getVertices())) {
            return tree;
        }

        //TODO - enforce the graph is  a tree.
        do {
            found = false;
            // Prune the leaf nodes which is not part of nodes until
            // the given nodes will be disconnected  f one more node is pruned.
            for (String nodeId : tree.getVertices()) {
                if (!nodes.contains(nodeId) && tree.getNeighborCount(nodeId) == 1) {
                    nodesToBePruned.add(nodeId);
                    found = true;

                }
            }

            for (String id : nodesToBePruned) {
                tree.removeVertex(id);
            }

            // Prune the links
            for (Link link : tree.getEdges()) {
                if (!tree.containsVertex(link.getSource().getSourceNode().getValue())
                        || !tree.containsVertex(link.getDestination().getDestNode().getValue())) {
                    edgesToBePruned.add(link);
                }
            }

            for (Link link : edgesToBePruned) {
                tree.removeEdge(link);
            }


        } while (found);

        return tree;
    }

    /**
     * Bridge two adjacent layer 2 segments into one
     * @param tenantId - tenant identifier.
     * @param tag - global tag shared by two access ports
     * @param link - the link between two fabrics
     * @param sseg - source l2 segment
     * @param dseg - destination l2 segment
     */
    private RenderedLayer2Link bridgeTwoSegmentsOverALink(Uuid tenantId, int tag,
            Link link, RenderedSwitch sseg, RenderedSwitch dseg)
    {

        TpId sltp = this.netNodeServiceProvider.createLogicalPortOnLsw(
                link.getSource().getSourceNode(),
                sseg.getSwitchID(),
                AccessType.Vlan,
                tag);

        TpId sftp = link.getSource().getSourceTp();
        this.netNodeServiceProvider.portBindingLogicalToFabric(
                new FabricId(link.getSource().getSourceNode()),
                sftp,
                sseg.getSwitchID(),
                sltp);


        TpId dltp = this.netNodeServiceProvider.createLogicalPortOnLsw(
                link.getDestination().getDestNode(),
                dseg.getSwitchID(),
                AccessType.Vlan,
                tag);
        TpId dftp = link.getDestination().getDestTp();
        this.netNodeServiceProvider.portBindingLogicalToFabric(
                new FabricId(link.getDestination().getDestNode()),
                dftp,
                dseg.getSwitchID(),
                dltp);

        return new RenderedLayer2Link(sseg, dseg, tag, sftp, sltp, dftp, dltp);
    }

    final class RenderReadySwitchLink {
        private final Link l;
        private final int tag;
        private final RenderedSwitch switchA;
        private final RenderedSwitch switchB;

        public RenderReadySwitchLink(Link l, int tag, RenderedSwitch switchA,
                RenderedSwitch switchB) {
            super();
            this.l = l;
            this.tag = tag;
            this.switchA = switchA;
            this.switchB = switchB;
        }

        public Link getL() {
            return l;
        }
        public int getTag() {
            return tag;
        }
        public RenderedSwitch getSwitchA() {
            return switchA;
        }
        public RenderedSwitch getSwitchB() {
            return switchB;
        }
    }
    //
    // bridge the logical switch's all layer2 segments on different fabrics
    //
    private void bridgeAllSegments(Uuid tenantId, UserLogicalNetworkCache uln, Uuid lsw,
            Topology topo, Map<String, RenderedSwitch> maps) {

        org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid lswUuid = toUuid(lsw);
        List<RenderReadySwitchLink> tasks = new ArrayList<>();
        for (Link l : topo.getLink()) {
            RenderedSwitch sswitch  = maps.get(l.getSource().getSourceNode().getValue());
            if (sswitch == null) {
                NodeId slsw = this.createLneLayer2(
                        tenantId,l.getSource().getSourceNode(),
                        lsw,
                        uln);

                RenderedSwitch slswR = new RenderedSwitch(l.getSource().getSourceNode(), lsw, slsw);
                uln.getLswStore().get(lswUuid).addRenderedSwitch(slswR);
                //continue;
            }

            RenderedSwitch dswitch = maps.get(l.getDestination().getDestNode().getValue());
            if (dswitch == null) {
                NodeId dlsw = this.createLneLayer2(
                        tenantId,l.getDestination().getDestNode(),
                        lsw,
                        uln);
                RenderedSwitch dlswR = new RenderedSwitch(l.getDestination().getDestNode(), lsw, dlsw);
                uln.getLswStore().get(lswUuid).addRenderedSwitch(dlswR);
                //continue;
            }


            RenderedLinkKey<String> key = new RenderedLinkKey<>(sswitch.getSwitchID().getValue(), dswitch.getSwitchID().getValue());
            if (!uln.getLswStore().get(lswUuid).getRenderedL2Links().containsKey(key)) {
                int tag = uln.getGlobalTag();
                if (tag != UserLogicalNetworkCache.GLOBAL_END_TAG) {
                    tasks.add(new RenderReadySwitchLink(l, tag, sswitch, dswitch));
                } else {
                    LOG.error("Failed to bridge sswitcch {} and dswtich {} , global tag used up!", sswitch.getSwitchID(), dswitch.getSwitchID());
                    return; //gracefully give up
                }
            }
        }

        for (RenderReadySwitchLink task : tasks) {
            RenderedLayer2Link rl2link = this.bridgeTwoSegmentsOverALink(tenantId, task.getTag(), task.getL(),
                task.getSwitchA(),task.getSwitchB());

            RenderedLinkKey<String> key = new RenderedLinkKey<>(task.getSwitchA().getSwitchID().getValue(), task.getSwitchB().getSwitchID().getValue());
            uln.getLswStore().get(lswUuid).addRenderedLink(key, rl2link);
        }
    }

    @Override
    public void removeLneLayer2(Uuid tenantId, String fabricId, NodeId lswId) {

        RmLneLayer2InputBuilder builder = new RmLneLayer2InputBuilder();
        builder.setTenantId(new TenantId(tenantId));
        builder.setVfabricId(new NodeId(fabricId));
        builder.setLneId(new VcLneId(lswId));

        Future<RpcResult<Void>> result = this.netNodeServiceProvider.rmLneLayer2(builder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: removeLneLayer2: rmLneLayer2 RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: removeLneLayer2: rmLneLayer2 RPC failed: {}", e);
        }
    }

    /**
     * To create a logical router on a given fabric for a tenant.
     * @param tenantId - tenant identifier.
     * @param fabricId - fabric identifier.
     * @param uln - the user logical network info
     * @return the rendered logical router identifier on the given fabric.
     */
    @Override
    public NodeId createLneLayer3(
            Uuid tenantId,
            String fabricId,
            UserLogicalNetworkCache uln
            ) {
        NodeId oFabricId = new NodeId(fabricId);
        CreateLneLayer3InputBuilder builder = new CreateLneLayer3InputBuilder();
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer3: vcMgr is null: tenantId={}", tenantId.getValue());
            return null;
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(oFabricId)) {
            LOG.error("FABMGR: ERROR: createLneLayer3: vfabricId is null: {}", tenantId.getValue());
            return null;
        }

        builder.setVfabricId(oFabricId);
        builder.setName(tenantId.getValue());

        NodeId renderedLrId = null;
        Future<RpcResult<CreateLneLayer3Output>> result = this.netNodeServiceProvider.createLneLayer3(builder.build());
        try {
            RpcResult<CreateLneLayer3Output> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer3: createLogicRouter RPC success");
                CreateLneLayer3Output createLswOutput = output.getResult();
                renderedLrId = createLswOutput.getLneId();

                LOG.debug("FABMGR: createLneLayer3: lrId={}", renderedLrId.getValue());
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer3: createLogicRouter RPC failed: ", e);
            return null;
        }

        return renderedLrId;
    }




    private synchronized IpAddress[] allocateReservedGatewayIP()
    {
        String[] values = reservedGatewayAddress.getValue().split("\\.");
        if (Integer.parseInt(values[2]) == 255 && Integer.parseInt(values[3]) == 253) {
            System.out.println("we are running out of gateway IP address space!");
            return null;
        }

        IpAddress[]  pair = new IpAddress[2];

        int next =  Integer.parseInt(values[3]) + 1;
        values[3] = new Integer(next).toString();
        pair[0] = new IpAddress(new Ipv4Address(values[0] + "." + values[1] + "." + values[2] + "." + values[3]));


        next =  Integer.parseInt(values[3]) + 1;
        values[3] = new Integer(next).toString();
        pair[1] = new IpAddress(new Ipv4Address(values[0] + "." + values[1] + "." + values[2] + "." + values[3]));

        reservedGatewayAddress = pair[1].getIpv4Address();

        if (Integer.parseInt(values[3]) >= 253) {
            int next2 = Integer.parseInt(values[2]) + 1;
            values[2] = new Integer(next2).toString();
            values[3] = "1";
            reservedGatewayAddress = new Ipv4Address(values[0] + "." + values[1] + "." + values[2] + "." + values[3]);
        }

        return pair;

    }


    private IpPrefix alloateReservedGatewayPrefix()
    {
        return new IpPrefix(defaultIpv4LinkPrefix);
    }


    //TODO
    private IpAddress getGatewayIP(org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid tid, NodeId lswId)
    {
        org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid lswUuid = new
                org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid(lswId.getValue());
        LogicalSwitchMappingInfo lmap = ulnStore.get(tid).getLswStore().get(lswUuid);
        SubnetMappingInfo snm = ulnStore.get(tid).findSubnetFromLsw(lmap);
        ExternalGateways exg = snm.getSubnet().getExternalGateways().get(0);
        return exg.getExternalGateway();
    }

    //TODO
    private IpPrefix getGatewayPrefix(TenantId tid, NodeId lswId) {
        final UserLogicalNetworkCache cache = ulnStore.get(toUuid(tid));
        LogicalSwitchMappingInfo lmap = cache.getLswStore().get(lswId);
        SubnetMappingInfo snm = cache.findSubnetFromLsw(lmap);
        return snm.getSubnet().getIpPrefix();
    }

    //TODO
    private class Capability {
        private final String name;
        private final String value;
        public Capability(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        public String getName()
        {
            return name;
        }

        public String getValue()
        {
            return value;
        }
    }
    //TODO Dummy code
    private List<Capability> getCapability(FabricId id)
    {
        List<Capability> cap = new ArrayList();
        Capability acap = new Capability("external", "port1");
        cap.add(acap);

        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * To get the border Fabric and its port connects to the External network
     * for now we only only return the first one.
     * @return
     */
    private Entry<FabricId, TpId> getBorderInfo()
    {
        Topology fabricTopo = this.getFabricTopology();
        if (fabricTopo == null) {
            LOG.error("Fabric Topology is NULL!");
            return null;
        }
        for (Link l : fabricTopo.getLink()) {
            if ("external".equalsIgnoreCase(l.getSource().getSourceNode().getValue())) {
                return new AbstractMap.SimpleEntry(l.getDestination().getDestNode(), l.getDestination().getDestTp());
            }
            if ("external".equalsIgnoreCase(l.getDestination().getDestNode().getValue())) {
                return new AbstractMap.SimpleEntry(l.getSource().getSourceNode(), l.getSource().getSourceTp());
            }
        }

        LOG.error("No Fabric Topology found!");
        return null;
    }

    @Override
    public NodeId setupExternalGW(
            Uuid tenantId,
            UserLogicalNetworkCache uln,
            LogicalRouterMappingInfo lr,
            IpAddress gatewayIP,
            IpPrefix prefix,
            int tag)
    {
        Entry<FabricId, TpId> entry = getBorderInfo();
        if (entry == null) {
            return null;
        }

        NodeId renderedLrId;
        if( (renderedLrId = uln.getRenderedRouterOnFabirc(entry.getKey())) == null) {
            renderedLrId = this.createLneLayer3(tenantId, entry.getKey().getValue(), uln);
            if (renderedLrId == null) {
                LOG.error("Failed to create VRF on border Fabric {}" , entry.getKey());
                return null;
            }
            uln.addRenderedRouterOnFabric(entry.getKey(), renderedLrId);
        }

        RenderedRouter rr;
        if ((rr = lr.getRenderedRouterOnFabric(entry.getKey())) == null) {
                rr = new RenderedRouter(new NodeId(entry.getKey()),renderedLrId);
                uln.getExtGateways().put(entry.getKey(), rr);
                lr.addRenderedRouter(rr);
        }

        PortBuilder pb = new PortBuilder();
        pb.setPortId(new VfabricPortId(entry.getValue())).build();

        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
        //builder.setSegmentId(xxx)); //let FaaS to determine
        builder.setTenantId(new TenantId(tenantId));
        builder.setVfabricId(entry.getKey());

        NodeId slsw = this.createLneLayer2(
                tenantId,
                entry.getKey(),
                new Uuid(UUID.randomUUID().toString()),
       this.ulnStore.get(toUuid(tenantId)));

        TpId tpId = this.netNodeServiceProvider.createLogicalPortOnLsw(entry.getKey(),slsw, AccessType.Vlan, tag);
        this.netNodeServiceProvider.portBindingLogicalToFabric(entry.getKey(), entry.getValue(), slsw, tpId);

        Uuid gid = this.createLrLswGateway(tenantId,
                entry.getKey(),
                rr.getRouterID(),
                slsw, gatewayIP,
                prefix);

        rr.setExternal(true);
        rr.setExtAccessTag(tag);
        rr.setExtSwitch(slsw);
        rr.setAccessTP(tpId);
        rr.setGwid(gid);

        //default static routes to exchange with outside
        UpdateLneLayer3RoutingtableInputBuilder rtinput = new UpdateLneLayer3RoutingtableInputBuilder();
        rtinput.setLneId(new VcLneId(rr.getRouterID()));
        rtinput.setTenantId(new TenantId(tenantId));
        rtinput.setVfabricId(entry.getKey());

        List<Routingtable> rtl = new ArrayList<>();

        RoutingtableBuilder rtbuilder = new RoutingtableBuilder();
        rtbuilder.setVrfId(rr.getRouterID().getValue());
        rtbuilder.setDestIp(new IpAddress(defaultIP));
        //rtbuilder.setNexthopIp(); //TODO  where we can get it ?
        rtl.add(rtbuilder.build());

        this.netNodeServiceProvider.updateLneLayer3Routingtable(rtinput.build());

        return renderedLrId;
    }

    public Topology getFabricTopology() {
        Optional<Topology> opt = fabMgrDatastoreUtil.readData(
                LogicalDatastoreType.OPERATIONAL, FabMgrYangDataUtil.FAAS_TOPLOGY_PATH);
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return null;
        }
    }


    /**
     * Get all the IPs reachable by this rendered router.
     * @param tenantId - tenantId
     * @param uln
     * @param lr
     * @return list of IPs reachable from this rendered router.
     */
    private List<IpAddress> getAllHostIPs(UserLogicalNetworkCache uln, RenderedRouter lr)
    {
        List<IpAddress> iplist = new ArrayList<>();
        for(Map.Entry<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid, LogicalSwitchMappingInfo> entry : uln.getLswStore().entrySet())
        {
            RenderedSwitch rswitch = entry.getValue().getRenderedSwitchOnFabric(lr.getFabricId());
            if(rswitch == null) {
                continue;
            }

            for (Uuid eploc : rswitch.getEpLocations()) {
                PortMappingInfo pmi = uln.getPortStore().get(toUuid(eploc));
                for (PrivateIps ip : pmi.getPort().getPrivateIps())
                {
                    iplist.add(ip.getIpAddress());
                }
            }

//            for (Map.Entry<TpId, TpId> entry2 :  rswitch.getPortMappings().entrySet())
//            {
//                PortMappingInfo pmi = uln.getPortStore().get(entry2.getKey());
//                for (PrivateIps ip : pmi.getPort().getPrivateIps())
//                {
//                    iplist.add(ip.getIpAddress());
//                }
//            }
        }

        if(lr.isExternal()) {
            iplist.add(new IpAddress(new Ipv4Address("0.0.0.0"))); //use it as default gw for external access.
        }

        return iplist;
    }


    private FabricId getFabricIDForNode(Node node)
    {
        return new FabricId(node.getAugmentation(FabricCapableDevice.class).getAttributes().getFabricId());
    }

    private IpAddress getLogicalPortIPAddress(TenantId tenantId, TpId tpID)
    {
        List<IpAddress> pips = new ArrayList<>();
        PortMappingInfo pmi = ulnStore.get(toUuid(tenantId)).getPortStore().get(tpID);
        for (PrivateIps ip : pmi.getPort().getPrivateIps())
        {
            pips.add(ip.getIpAddress());
        }

        if (!pips.isEmpty()) {
            //TODO, using the first one for now.
            return pips.get(0);
        }

        return null;
    }

    final class RenderReadyL3Link {
        private final Link l;
        private final int tag;
        private final RenderedRouter routerA;
        private final RenderedRouter routerB;

        public RenderReadyL3Link(Link l, int tag, RenderedRouter routerA,
                RenderedRouter routerB) {
            super();
            this.l = l;
            this.tag = tag;
            this.routerA = routerA;
            this.routerB = routerB;
        }

        public Link getL() {
            return l;
        }
        public int getTag() {
            return tag;
        }
        public RenderedRouter getRouterA() {
            return routerA;
        }
        public RenderedRouter getRouterB() {
            return routerB;
        }
    }

    /**
     *
     * Connects all distributed logical routers into one big logical router
     * all the routes or host routes or prefix needs to be correctly configured on each
     * logical router on each participant fabric
     *
     *
     * @param tenantID - tenant identifier.
     * @param uln - User logical network info.
     * @param rmaps - RenderedRouter on Fabric mapping table
     */

    @Override
    public void connectAllDVRs(Uuid tenantID, UserLogicalNetworkCache uln, Map<String, RenderedRouter> rmaps)
    {
        org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid fTenantID =
                toUuid(tenantID);

        List<RenderReadyL3Link> tasks = new ArrayList<>();

        //For layer 3 , all we need are all shortest path links.
        // it is not like layer 2 which has to be a tree. to say the least.
        List<Link> alllinks = new ArrayList<>();
        for (RenderedRouter rrsentry : rmaps.values()) {
            for (RenderedRouter rrdentry : rmaps.values()) {
                List<Link> links = this.calcShortestPathOnFabricTopo(rrsentry.getFabricId(), rrdentry.getFabricId());
                alllinks.removeAll(links);
                alllinks.addAll(links);
            }
        }

        for (Link l : alllinks)
        {
            NodeId srcLr =  uln.getRenderedRouterOnFabirc(l.getSource().getSourceNode());
            if (srcLr == null) {
                srcLr = this.createLneLayer3(tenantID, l.getSource().getSourceNode().getValue(), uln);
                uln.addRenderedRouterOnFabric(l.getSource().getSourceNode(), srcLr);

            }
            RenderedRouter srcrr = new RenderedRouter(
                    l.getSource().getSourceNode(),
                    srcLr);
            NodeId destLr = uln.getRenderedRouterOnFabirc(l.getDestination().getDestNode());
            if (destLr == null) {
                destLr = this.createLneLayer3(tenantID, l.getDestination().getDestNode().getValue(), uln);
                uln.addRenderedRouterOnFabric(l.getDestination().getDestNode(), destLr);
            }

            RenderedRouter destrr = new RenderedRouter(
                    l.getSource().getSourceNode(),
                    destLr);

            RenderedLinkKey<String> key = new RenderedLinkKey<>(srcrr.getRouterID().getValue(), destrr.getRouterID().getValue());
            if (uln.getRenderedrLinks().containsKey(key)) {
                LOG.debug("From {" + srcrr.toString() + " } to {" + destrr.toString() + " } already connected!!");
                continue;
            }

            int tag = uln.getGlobalTag();
            if (tag == UserLogicalNetworkCache.GLOBAL_END_TAG) {
                LOG.info("Global tags all used!");
                return;
            }

            tasks.add(new RenderReadyL3Link(l, tag, srcrr, destrr));
        }

        for (RenderReadyL3Link task : tasks) {
            RenderedLinkKey<String> key = new RenderedLinkKey<>(task.getRouterA().getRouterID().getValue(), task.getRouterB().getRouterID().getValue());

            if (uln.getRenderedrLinks().containsKey(key)) {
                continue;
            }

            NodeId lfabricId = task.getL().getSource().getSourceNode();
            NodeId rfabricId = task.getL().getDestination().getDestNode();
            TpId lfTpId = task.getL().getSource().getSourceTp();
            TpId rfTpId = task.getL().getDestination().getDestTp();

            CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
            //builder.setSegmentId(xxx)); //let FaaS to determine
            builder.setTenantId(new TenantId(tenantID));
            builder.setVfabricId(new FabricId(task.getL().getSource().getSourceNode()));
//begin
            Uuid newLswId = new Uuid(UUID.randomUUID().toString());
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid flsw =
                    toUuid(newLswId);
            LogicalSwitchBuilder lswBuilder = new LogicalSwitchBuilder();
            lswBuilder.setUuid(flsw);
            lswBuilder.setAdminStateUp(true);

            this.ulnStore
                .get(fTenantID)
                .getLswStore()
                .put(flsw, new LogicalSwitchMappingInfo(lswBuilder.build()));
//end
            NodeId slsw = this.createLneLayer2(
                    tenantID,task.getL().getSource().getSourceNode(),
                    newLswId,
                    this.ulnStore.get(fTenantID));

            TpId tpId = this.netNodeServiceProvider.createLogicalPortOnLsw(lfabricId, slsw, AccessType.Vlan, task.getTag());
            this.netNodeServiceProvider.portBindingLogicalToFabric(new FabricId(lfabricId), lfTpId,  slsw, tpId);

            RenderedSwitch slswR = new RenderedSwitch(task.getL().getSource().getSourceNode(), tenantID, slsw);

            IpAddress[] gwpair = this.allocateReservedGatewayIP();

            MacAddress smac = this.createGateway(tenantID,
                    task.getL().getSource().getSourceNode(),
                    task.getRouterA().getRouterID(),
                    slsw, gwpair[0],
                    this.alloateReservedGatewayPrefix());

            /* right */

            builder.setVfabricId(new FabricId(task.getL().getDestination().getDestNode()));

//begin
            Uuid newdLswId = new Uuid(UUID.randomUUID().toString());
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid fdlsw =
                    toUuid(newdLswId);
            lswBuilder = new LogicalSwitchBuilder();
            lswBuilder.setUuid(flsw);
            lswBuilder.setAdminStateUp(true);

            this.ulnStore
                .get(fTenantID)
                .getLswStore()
                .put(fdlsw, new LogicalSwitchMappingInfo(lswBuilder.build()));
//end
            NodeId dlsw = this.createLneLayer2(tenantID, task.getL().getDestination().getDestNode(),
                    newdLswId,
                    this.ulnStore.get(fTenantID));

            TpId tpId2 = this.netNodeServiceProvider.createLogicalPortOnLsw(task.getL().getDestination().getDestNode(),dlsw, AccessType.Vlan, task.getTag());
            TpId tpIdf2 = task.getL().getDestination().getDestTp();
            this.netNodeServiceProvider.portBindingLogicalToFabric(new FabricId(task.getL().getDestination().getDestNode()), tpIdf2, dlsw, tpId2);

            RenderedSwitch dlswR = new RenderedSwitch(task.getL().getDestination().getDestNode(), tenantID, dlsw);

            MacAddress dmac = this.createGateway(tenantID,
                    task.getL().getDestination().getDestNode(),
                    task.getRouterB().getRouterID(),
                    dlsw, gwpair[1],
                    this.alloateReservedGatewayPrefix());

            // register right gw as an endpoint to left fabric.
            InstanceIdentifier<TerminationPoint> locIid = InterfaceManager.convFabricPort2DevicePort(
                    fabMgrDatastoreUtil.getDataBroker(), new FabricId(lfabricId), lfTpId);
            this.netNodeServiceProvider.registerEndpoint(
                    tenantID,
                    lfabricId,
                    null,
                    null,
                    gwpair[1],
                    locIid.firstKeyOf(Node.class).getNodeId().getValue(),
                    locIid.firstKeyOf(TerminationPoint.class).getTpId().getValue(),
                    dmac,
                    slsw,
                    tpId,
                    AccessType.Vlan,
                    task.getTag());

            // register left gw as an endpoint to right fabric.
            InstanceIdentifier<TerminationPoint> locIid2 = InterfaceManager.convFabricPort2DevicePort(
                    fabMgrDatastoreUtil.getDataBroker(), new FabricId(rfabricId), rfTpId);
            this.netNodeServiceProvider.registerEndpoint(
                    tenantID,
                    rfabricId,
                    null,
                    null,
                    gwpair[0],
                    locIid2.firstKeyOf(Node.class).getNodeId().getValue(),
                    locIid2.firstKeyOf(TerminationPoint.class).getTpId().getValue(),
                    smac,
                    dlsw,
                    tpId2,
                    AccessType.Vlan,
                    task.getTag());

            RenderedLayer2Link l2link = new RenderedLayer2Link(slswR,  dlswR, task.getTag(), lfTpId, tpId, rfTpId, tpId2);
//            RenderedLayer3Link l3link = new RenderedLayer3Link(task.getRouterA(), task.getRouterB(), slswR, dlswR,
//                    srcGWPort,this.getGatewayIP(fTenantID,slsw), destGWPort, this.getGatewayIP(fTenantID,dlsw), l2link);
            RenderedLayer3Link l3link = new RenderedLayer3Link(task.getRouterA(), task.getRouterB(), slswR, dlswR,
                    null,gwpair[0], null, gwpair[1], l2link);

            uln.addRenderedrLink(key, l3link);
            {
                RenderedLinkKey<String> reverseKey = new RenderedLinkKey<>(task.getRouterB().getRouterID().getValue(), task.getRouterA().getRouterID().getValue());
                RenderedLayer2Link reversel2link = new RenderedLayer2Link(dlswR,  slswR, task.getTag(), rfTpId, tpId2, lfTpId, tpId);
                RenderedLayer3Link reversel3link = new RenderedLayer3Link(task.getRouterB(), task.getRouterA(), dlswR, slswR,
                        null,gwpair[1], null, gwpair[0], reversel2link);
                uln.addRenderedrLink(reverseKey, reversel3link);
            }
        }

    }

    @Override
    public void updateRoutes(Uuid tenantID, UserLogicalNetworkCache uln, Map<String, RenderedRouter> maps) {
        for (RenderedRouter lrs : maps.values()) {
            for (RenderedRouter lrd : maps.values()) {

                if (lrs == lrd) {
                    continue;
                }

                // Calculate the sp from lrs to lrd on the pruned tree topology.
                // Ideally it should reused the results of the  connectAllDVR 's calculation to make sure
                // The data path is always connected. for now since the FabricTopology ususally not changed
                // after initialization. it is safe for now. Also the calculation should be very fast in
                // exchange of caching the results.
                List<Link> ls = this.calcShortestPathOnFabricTopo(lrs.getFabricId(), lrd.getFabricId());

                UpdateLneLayer3RoutingtableInputBuilder rtinput = new UpdateLneLayer3RoutingtableInputBuilder();
                rtinput.setLneId(new VcLneId(lrs.getRouterID()));
                rtinput.setTenantId(new TenantId(tenantID));
                rtinput.setVfabricId(lrs.getFabricId());

                List<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.update.lne.layer3.routingtable.input.Routingtable> rtl = new ArrayList<>();
                rtinput.setRoutingtable(rtl);

                //Get all the reacheable hosts on destiantion router lrd.
                //for each Host on lrd, a Host route is generated and isntalled on the
                // source router lrs.
                for (IpAddress ip : getAllHostIPs(uln, lrd)) {
                    org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.update.lne.layer3.routingtable.input.RoutingtableBuilder rtbuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.update.lne.layer3.routingtable.input.RoutingtableBuilder();
                    rtbuilder.setVrfId(lrs.getRouterID().getValue());
                    rtbuilder.setDestIp(ip);

                    // The resulted path is ordered by distance from
                    // nearest
                    // to farthest, the second one should be the next hop.
                    // the link 0 on short path connects lrs and next Hop.
                    // the link 0 's destination would be next Hop's rendered router.
                    // we need to get the gateway IP addess leading to the next hop.

                    RenderedRouter nextHop = maps.get(ls.get(0).getDestination().getDestNode().getValue());
                    RenderedLinkKey<String> key = new RenderedLinkKey<>(lrs.getRouterID().getValue(), lrd.getRouterID().getValue());

                    IpAddress nextHopIp =  uln.getRenderedrLinks().get(key).getDestGWIP();
                    rtbuilder.setNexthopIp(nextHopIp);

                    rtbuilder.setOutgointInterface(new String(uln.getRenderedrLinks().get(key).getSrcGWIP().getValue()));

                    rtl.add(rtbuilder.build());
                }
                this.netNodeServiceProvider.updateLneLayer3Routingtable(rtinput.build());
            }
        }

    }

    /**
     * To calculate the shortest path from lrs (source router) to lrd (destination)
     * @param lrs - source router identifier.
     * @param lrd - destination router identifier.
     * @param topo - topology
     * @return the links constructing the path.
     */
    private List<Link> calcShortestPath(NodeId nodes, NodeId noded, Graph<NodeId, Link> graph)
    {
        DijkstraShortestPath<NodeId, Link> alg = new DijkstraShortestPath<>(graph);
        return alg.getPath(nodes, noded);
    }

    private List<Link> calcShortestPathOnFabricTopo(NodeId fabrics, NodeId fabricd)
    {
        UndirectedSparseGraph<NodeId, Link> g = new UndirectedSparseGraph<>();
        Topology topo = this.getFabricTopology();
        if (topo == null) {
            LOG.error("Failed to get fabric topology!");
            return Collections.emptyList();
        }

        for (Node node : topo.getNode()) {
            g.addVertex(node.getNodeId());
        }

        if (topo.getLink() != null) {
            for (Link link : topo.getLink())
            {
                g.addEdge(link, link.getSource().getSourceNode(), link.getDestination().getDestNode());
            }
        }

        return calcShortestPath(fabrics, fabricd, g);
    }


    /**
     * Remove a tenant defined logical router.
     * @param tenantId - tenant identifier.
     * @param fabricId - fabric identifier.
     * @param lrId - logical router identifier.
     */
    @Override
    public void removeLneLayer3(Uuid tenantId, String fabricId, NodeId lrId) {

        RmLneLayer3InputBuilder builder = new RmLneLayer3InputBuilder();
        builder.setTenantId(new TenantId(tenantId));
        builder.setVfabricId(new NodeId(fabricId));
        builder.setLneId(new VcLneId(lrId));

        Future<RpcResult<Void>> result = this.netNodeServiceProvider.rmLneLayer3(builder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: removeLneLayer3: rmLneLayer3 RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: removeLneLayer3: rmLneLayer3 RPC failed: {}", e);
        }
    }

    public List<FabricId> getAllFabrics() {
        return this.netNodeServiceProvider.getAllFabrics();
    }

    /**
     * Binding an physical location with logical port.
     * @param tenantId - tenant identifier.
     * @param fabricId - fabric identifier.
     * @param lswId - logical switch
     * @param lswLogicalPortId - logical switch port id
     * @param endpoint - end point attributes
     * @return the endpoint uuid.
     */
    @Override
    public Uuid attachEpToLneLayer2(Uuid tenantId, NodeId fabricId, NodeId lswId, TpId lswLogicalPortId,
            EndpointAttachInfo endpoint) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(fabricId)) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        return this.netNodeServiceProvider.registerEndpoint(
                tenantId,
                fabricId,
                endpoint.getEpYangUuid(),
                endpoint.getGatewayIpAddr(),
                endpoint.getIpAddress(),
                endpoint.getInventoryNodeIdStr(),
                endpoint.getInventoryNodeConnectorIdStr(),
                endpoint.getMacAddress(),
                lswId,
                lswLogicalPortId);
    }

    @Override
    public void unregisterEpFromLneLayer2(Uuid tenantId, NodeId fabricId, NodeId lswId, Uuid epUuid) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: unregisterEpFromLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        if (fabricId == null) {
            LOG.error("FABMGR: ERROR: unregisterEpFromLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.unregisterEndpoint(tenantId, fabricId, lswId, epUuid);
    }

    @Override
    public TpId createLogicalPortOnLneLayer2(Uuid tenantId, NodeId vfabricId,  NodeId lswId) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null;
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null;
        }

        return this.netNodeServiceProvider.createLogicalPortOnLsw(vfabricId, lswId, AccessType.Exclusive, 0);
    }

    @Override
    public Uuid createLrLswGateway(Uuid tenantId, NodeId vfabricId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        return  this.netNodeServiceProvider.createLrLswGateway(vfabricId, lrId, lswId, gatewayIpAddr, ipPrefix);
    }

    public MacAddress createGateway(Uuid tenantId, NodeId vfabricId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        return  this.netNodeServiceProvider.createGateway(vfabricId, lrId, lswId, gatewayIpAddr, ipPrefix);
    }

    @Override
    public void removeLrLswGateway(Uuid tenantId, String fabricId,  NodeId lrId, IpAddress gatewayIpAddr) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        this.netNodeServiceProvider.removeLrLswGateway(fabricId, lrId, gatewayIpAddr);
    }

    @Override
    public void createAcl(Uuid tenantId, String fabricId, NodeId nodeId, String aclName) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(fabricId)) {
            LOG.error("FABMGR: ERROR: createAcl: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createAcl(fabricId, nodeId, aclName);
    }

    @Override
    public void removeAcl(Uuid tenantId, String fabricId, NodeId nodeId, String aclName) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }


        this.netNodeServiceProvider.removeAcl(fabricId, nodeId, aclName);
    }

    @Override
    public void setIPMapping(Uuid tenantId, NodeId fabricId, NodeId lrId, TpId tpid,  IpAddress pubIP, IpAddress priip) {
        VContainerConfigMgr vcMgr = containerMgr.getVcConfigDataMgr(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        this.netNodeServiceProvider.addIPMapping(new FabricId(fabricId), lrId, tpid, pubIP.getIpv4Address(), priip.getIpv4Address());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid toUuid(Uuid id) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid(id.getValue());

    }
}
