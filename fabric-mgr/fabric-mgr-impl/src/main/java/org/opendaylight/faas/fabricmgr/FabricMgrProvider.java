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
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.uln.cache.LogicalSwitchMappingInfo;
import org.opendaylight.faas.uln.cache.PortMappingInfo;
import org.opendaylight.faas.uln.cache.RenderedRouter;
import org.opendaylight.faas.uln.cache.RenderedSwitch;
import org.opendaylight.faas.uln.cache.SubnetMappingInfo;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.LogicalLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.VcNetNodeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.ports.Port;
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
public class FabricMgrProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FabricMgrProvider.class);
    private final ExecutorService threadPool;
    private final VContainerMgr vcProvider;
    private final VContainerNetNodeServiceProvider netNodeServiceProvider;
    private Map<Uuid, VContainerConfigMgr> vcConfigDataMgrList; // tenantId-Vcontainer lookup map
    private VcNetNodeService vcNetNodeService;
    private static FabricMgrProvider instance = null;

    private Map<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid, UserLogicalNetworkCache> ulnStore;

    private FabricMgrProvider(final DataBroker dataProvider, final RpcProviderRegistry rpcRegistry,
            final NotificationService notificationService) {
        super();
        FabMgrDatastoreDependency.setDataProvider(dataProvider);
        FabMgrDatastoreDependency.setRpcRegistry(rpcRegistry);
        FabMgrDatastoreDependency.setNotificationService(notificationService);

        int numCPU = Runtime.getRuntime().availableProcessors();
        this.threadPool = Executors.newFixedThreadPool(numCPU * 2);
        this.vcProvider = new VContainerMgr(this.threadPool);
        this.vcProvider.initialize();
        this.netNodeServiceProvider = new VContainerNetNodeServiceProvider(this.threadPool);
        this.netNodeServiceProvider.initialize();

        this.vcNetNodeService = FabMgrDatastoreDependency.getRpcRegistry().getRpcService(VcNetNodeService.class);
        this.vcConfigDataMgrList = new ConcurrentHashMap<>();
        this.ulnStore = new ConcurrentHashMap<>();

        LOG.info("FABMGR: FabricMgrProvider has Started with threadpool size {}", numCPU);
    }

    public Map<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid, UserLogicalNetworkCache> getCacheStore()
    {
        return this.ulnStore;
    }

    /**
     * Singleton method to return the singleton instance.
     * @param dataProvider - data store broker.
     * @param rpcRegistry - remote process call registry
     * @param notificationService - data store notification service.
     * @return the fabricMgrProvider instance.
     */
    public static synchronized FabricMgrProvider createInstance(
            final DataBroker dataProvider,
            final RpcProviderRegistry rpcRegistry,
            final NotificationService notificationService)
    {
        if(instance == null) {
            return new FabricMgrProvider(dataProvider, rpcRegistry, notificationService);
        }

        return instance;
    }

    public static FabricMgrProvider getInstance()
    {
        if(instance == null) {
            LOG.error("FabricMgrProvider hasn't been initialized yet!!!");
            return null;
        }

        return instance;
    }


    @Override
    public void close() throws Exception {

        LOG.info("Shuting down FabricMgrProvider ...");

        this.vcProvider.close();
        this.netNodeServiceProvider.close();
        this.threadPool.shutdown();
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
    public NodeId createLneLayer2(Uuid tenantId, NodeId fabricId, Uuid lsw, UserLogicalNetworkCache uln) {

        // Check resource availability
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(fabricId)) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vfabricId is not in tenant {} 's vcontainer.",
                    tenantId.getValue());
            return null; // ---->
        }

        builder.setVfabricId(fabricId);
        builder.setTenantId(new TenantId(tenantId));
        builder.setLswUuid(lsw);
        builder.setName(lsw.getValue());
        // builder.setSegmentId(); //TODO leave it to FaaS to determine for now.

        NodeId nodeId = null;
        Future<RpcResult<CreateLneLayer2Output>> result = this.vcNetNodeService.createLneLayer2(builder.build());
        try {
            RpcResult<CreateLneLayer2Output> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer2: createLneLayer2 RPC success");
                CreateLneLayer2Output createLswOutput = output.getResult();
                nodeId = createLswOutput.getLneId();

                LOG.debug("FABMGR: createLneLayer2: lswId={}", nodeId.getValue());

                RenderedSwitch renderedSW = new RenderedSwitch(fabricId, nodeId,
                        new NodeId(lsw.getValue()));
                uln.getLswStore().get(lsw).addRenderedSwitch(renderedSW);
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer2: createLneLayer2 RPC failed: {}", e);
        }

        Map<NodeId, RenderedSwitch> sws = uln.getLswStore().get(lsw).getRenderedSwitches();
        Graph<NodeId, Link> graph = calcMinimumSpanningTree(new ArrayList<>(sws.keySet()));
        TopologyBuilder topo = new TopologyBuilder();
        topo.setLink(new ArrayList(graph.getEdges()));
        topo.setNode(new ArrayList(graph.getVertices()));
        FabMgrDatastoreUtil.putData(LogicalDatastoreType.CONFIGURATION,
                FabMgrYangDataUtil.buildTopologyPath(lsw.getValue()),topo.build());

        bridgeAllSegments(tenantId, uln, topo.build(), sws);

        return nodeId;
    }

    /**
     * Calculate and return the minimum spanning free of the Graph top.
     * @param topo - the graph.
     * @return the "pruned" mst.
     */
    private Graph<NodeId, Link> calcMinimumSpanningTree(List<NodeId> fabrics)
    {
        Topology topo = this.getFabricTopology();
        if (topo == null) {
            LOG.error("Failed to read Fabric Topology!");
            return null;
        }

        Tree<NodeId, Link> tree = new DelegateTree<>();
        for (Node node : topo.getNode()) {
            tree.addVertex(node.getNodeId());
        }

        for (Link link : topo.getLink())
        {
            tree.addEdge(link, link.getSource().getSourceNode(), link.getDestination().getDestNode());
        }

        PrimMinimumSpanningTree<NodeId, Link> alg =
                new PrimMinimumSpanningTree<>(DelegateTree.<NodeId, Link>getFactory());

        Graph<NodeId, Link> miniTree = alg.transform(tree);

        return pruneTree(miniTree, fabrics);
    }

    private Graph<NodeId, Link> pruneTree(Graph<NodeId, Link> tree, List<NodeId> nodes)
    {
        //Prune the leaf nodes
        for (NodeId nodeId : tree.getVertices()) {
            if (!nodes.contains(nodeId) && tree.getNeighborCount(nodeId) == 1 )
            {
                tree.removeVertex(nodeId);
            }
        }

        //Prune the links
        for (Link link : tree.getEdges())
        {
            if (!tree.containsVertex(link.getSource().getSourceNode())
                    || !tree.containsVertex(link.getDestination().getDestNode()) ) {
                tree.removeEdge(link);
            }
        }
        return tree;
    }



    /**
     * Bridge two adjacent layer 2 segments into one
     * @param tenantId - tenant identifier.
     * @param tag - global tag shared by two access ports
     * @param link - the link between two fabrics
     * @param sseg - source l2 segment
     * @param dseg - dest l2 segment
     */
    private void bridgeTwoSegmentsOverALink(Uuid tenantId, int tag, Link link, NodeId sseg, NodeId dseg)
    {

        TpId tp = this.netNodeServiceProvider.createLogicalPortOnLsw(tenantId,
                link.getSource().getSourceNode(),
                sseg,
                AccessType.Vlan,
                tag);

        this.netNodeServiceProvider.portBindingLogicalToFabric(
                new FabricId(link.getSource().getSourceNode()),
                link.getSource().getSourceTp(),
                tp,
                sseg);


        tp = this.netNodeServiceProvider.createLogicalPortOnLsw(tenantId,
                link.getDestination().getDestNode(),
                dseg,
                AccessType.Vlan,
                tag);

        this.netNodeServiceProvider.portBindingLogicalToFabric(
                new FabricId(link.getDestination().getDestNode()),
                link.getDestination().getDestTp(),
                tp,
                dseg);
    }

    //
    // bridge the logical switch's all layer 2 segements on different fabrics
    //
    private void bridgeAllSegments(Uuid tenantId, UserLogicalNetworkCache uln, Topology t, Map<NodeId, RenderedSwitch> sws) {
        Map<FabricId, NodeId> maps = buildMaps(t.getNode());

        for (Link l : t.getLink()) {
            int tag = uln.getGlobalTag();
            if (tag != UserLogicalNetworkCache.GLOBAL_END_TAG) {
                this.bridgeTwoSegmentsOverALink(tenantId, tag, l, maps.get(new FabricId(l.getSource().getSourceNode())),
                        maps.get(new FabricId(l.getDestination().getDestNode())));
            }
        }
    }

    /**
     * To read from data store all the children nodes of the logical switch.
     * @param lswId - logical switch identifier.
     * @return the list of the children of the logical switch.
     */
    private List<Node> getAllChildren(NodeId lswId)
    {
        InstanceIdentifier<Topology> nodeId = FabMgrYangDataUtil.buildTopologyPath(lswId.getValue());
        Optional<Topology> topo = FabMgrDatastoreUtil.readData(LogicalDatastoreType.CONFIGURATION, nodeId);
        if (topo.isPresent()) {
            return topo.get().getNode();
        }

        LOG.error("Failed to get the children of hte logical switch " + lswId);
        return Collections.emptyList();
    }


    public void removeLneLayer2(Uuid tenantId, NodeId fabricId, NodeId lswId) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        for (Node vfabric : getAllChildren(lswId)) {
            if (vfabric.getNodeId() == null) {
                LOG.error("FABMGR: ERROR: removeLneLayer2: vfabricId is null: {}", tenantId.getValue());
                return; // ---->
            }

            RmLneLayer2InputBuilder builder = new RmLneLayer2InputBuilder();
            builder.setTenantId(new TenantId(tenantId));
            builder.setVfabricId(vfabric.getNodeId());
            builder.setLneId(new VcLneId(lswId));

            Future<RpcResult<Void>> result = this.vcNetNodeService.rmLneLayer2(builder.build());
            try {
                RpcResult<Void> output = result.get();
                if (output.isSuccessful()) {
                    LOG.debug("FABMGR: removeLneLayer2: rmLneLayer2 RPC success");
                }
            } catch (Exception e) {
                LOG.error("FABMGR: ERROR: removeLneLayer2: rmLneLayer2 RPC failed: {}", e);
            }
        }
    }

    /**
     * To create a logical router on a given fabric for a tenant.
     * @param tenantId - tenant identifier.
     * @param fabricId - fabric identifier.
     * @param uln - the user logical network info
     * @param lr - logical router to be rendered on this fabric. it might be distributed and
     *             contain multiple logical routers on different fabric. but each fabric only has one.
     * @return the rendered logical router identifier on the given fabric.
     */
    public NodeId createLneLayer3(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, Uuid lr) {
        CreateLneLayer3InputBuilder builder = new CreateLneLayer3InputBuilder();
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer3: vcMgr is null: tenantId={}", tenantId.getValue());
            return null;
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(fabricId)) {
            LOG.error("FABMGR: ERROR: createLneLayer3: vfabricId is null: {}", tenantId.getValue());
            return null;
        }

        builder.setVfabricId(fabricId);
        builder.setTenantId(new TenantId(tenantId));
        builder.setLrUuid(lr);
        builder.setName(lr.getValue());
        //builder.setRoutingTable(value);
        //builder.setVrfCtx(value);

        NodeId nodeId = null;
        Future<RpcResult<CreateLneLayer3Output>> result = this.vcNetNodeService.createLneLayer3(builder.build());
        try {
             RpcResult<CreateLneLayer3Output> output = result.get();
             if (output.isSuccessful()) {
                 LOG.debug("FABMGR: createLneLayer3: createLogicRouter RPC success");
                 CreateLneLayer3Output createLswOutput = output.getResult();
                 nodeId = createLswOutput.getLneId();
             }
        } catch (Exception e) {
                LOG.error("FABMGR: ERROR: createLneLayer3: createLogicRouter RPC failed: ", e);
                return null;
        }

        LOG.debug("FABMGR: createLneLayer3: lrId={}", nodeId.getValue());

        //connect all logical routers across multiple fabrics and
        //populate their routing tables.
        connectAllDVRs(tenantId, uln, lr);

        return nodeId;
    }

    //TODO
    private IpAddress getGatewayIP(TenantId tid,NodeId nid)
    {
        LogicalSwitchMappingInfo lmap = ulnStore.get(tid).getLswStore().get(nid);
         SubnetMappingInfo snm = ulnStore.get(tid).findSubnetFromLsw(lmap);
         ExternalGateways exg = snm.getSubnet().getExternalGateways().get(0);
        return exg.getExternalGateway();
    }

    //TODO
    private IpPrefix getGatewayPrefix(TenantId tid, NodeId nid)
    {
        LogicalSwitchMappingInfo lmap = ulnStore.get(tid).getLswStore().get(nid);
        SubnetMappingInfo snm = ulnStore.get(tid).findSubnetFromLsw(lmap);
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
        Capability a = new Capability("external", "port1");
        cap.add(a);

        throw new UnsupportedOperationException("Not implemented yet!");
    }

    private Entry<FabricId, String> getBorderInfo()
    {
        for (FabricId id : this.netNodeServiceProvider.getAllFabrics()) {
            for (Capability cap : getCapability(id)) {
                if (cap.getName().equalsIgnoreCase("external")) {
                    return new AbstractMap.SimpleEntry(id, cap.getValue());
                }
            }
        }
        return null;
    }


    /**
     * setupExternalGW - Configure a logical port which binding to a physical port with NAT functionality if NAT
     * is required.
     * @param tenantId
     * @param lrId
     * @param gatewayIPAddr
     * @param prefix
     */
    private void setupExternalGW(Uuid tenantId, UserLogicalNetworkCache uln, NodeId fabricId, NodeId lrId, IpAddress gatewayIPAddr, IpPrefix prefix)
    {
        Entry<FabricId, String> entry = getBorderInfo();
        PortBuilder pb = new PortBuilder();
        Port borderPort = pb.setPortId(new VfabricPortId(entry.getValue())).build();

        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
        builder.setLswUuid(new Uuid(UUID.randomUUID().toString()));
        builder.setName("");

        List<Port> ports = new ArrayList<>();
        ports.add(borderPort);
        builder.setPort(ports);

        builder.setSegmentId(new Long(uln.getGlobalTag()));
        builder.setTenantId(new TenantId(tenantId));
        builder.setVfabricId(entry.getKey());
        NodeId lswId = this.createLneLayer2(tenantId, new NodeId(entry.getKey()), null, this.ulnStore.get(tenantId));

        this.createLrLswGateway(tenantId, fabricId, lrId, lswId, gatewayIPAddr, prefix);

        //TODO
        //BGP or static routes to exchange with outside

        // create Default Routes using the gateway router for other logical routers.
    }

    private Topology getFabricTopology()
    {
        Optional<Topology> opt = FabMgrDatastoreUtil.readData(
                LogicalDatastoreType.OPERATIONAL, FabMgrYangDataUtil.FAAS_TOPLOGY_PATH);
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return null;
        }
    }


    private List<IpAddress> getAllHostIPs(TenantId tenantId, NodeId lr)
    {
        List<IpAddress> iplist = new ArrayList();
        List<Node> lsws = this.getAllChildren(lr);
        for(Node node : lsws)
        {
            for (TerminationPoint tp :  node.getTerminationPoint())
            {
                PortMappingInfo pmi = ulnStore.get(tenantId).getPortStore().get(tp.getTpId());
                for (PrivateIps ip : pmi.getPort().getPrivateIps())
                {
                    iplist.add(ip.getIpAddress());
                }
            }
        }

        return iplist;
    }


    private FabricId getFabricIDForNode(Node node)
    {
        return new FabricId(node.getAugmentation(FabricCapableDevice.class).getAttributes().getFabricId());
    }

    //TODO
    private IpAddress getIPAddress (TenantId tenantId, TpId tpID)
    {
        PortMappingInfo pmi = ulnStore.get(tenantId).getPortStore().get(tpID);
        for (PrivateIps ip : pmi.getPort().getPrivateIps())
        {
            //TODO, using the first one for now.
            return ip.getIpAddress();
        }

        return null;
    }

    private Map<FabricId, NodeId> buildMaps(List<Node> nodes)
    {
        Map<FabricId, NodeId> maps = new HashMap<>();
        for(Node node :  nodes)
        {
            maps.put(new FabricId(node.getAugmentation(FabricCapableDevice.class).getAttributes().getFabricId()), node.getNodeId());
        }

        return maps;
    }

    /**
     *
     * Connects all distributed logical routers into one big logical router
     * all the routes or host routes or prefix needs to be correctly configured on each
     * logical router on each participant fabric
     *
     * @param tenantID - tenant identifier.
     * @param uln - User logical network info.
     * @param lr - logical router's IETF UuID.
     */

    private void connectAllDVRs(Uuid tenantID, UserLogicalNetworkCache uln, Uuid lr)
    {
        Map<NodeId, RenderedRouter> maps = uln.getLrStore().get(lr).getRenderedRouters();
        Graph<NodeId, Link> tree = calcMinimumSpanningTree(new ArrayList<>(maps.keySet()));
        for (Link l : tree.getEdges())
        {
            int tag = uln.getGlobalTag();
            if (tag == UserLogicalNetworkCache.GLOBAL_END_TAG) {
                LOG.info("Global tags all used!");
                return;
            }

            CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
            builder.setSegmentId(new Long(tag));
            builder.setTenantId(new TenantId(tenantID));
            builder.setVfabricId(new FabricId(l.getSource().getSourceNode()));

            //
            NodeId lsw1 = this.createLneLayer2(tenantID,l.getSource().getSourceNode(),
                    null, //TODO - need to track the internal created logical-switch id value and for removal in the futrue.
                    this.ulnStore.get(tenantID));

            TpId tpId = this.createLogicalPortOnLneLayer2(tenantID, l.getSource().getSourceNode(),lsw1);
            this.netNodeServiceProvider.portBindingLogicalToFabric(new FabricId(l.getSource().getSourceNode()), l.getSource().getSourceTp(), tpId, lsw1);

            this.createLrLswGateway(tenantID,
                    l.getSource().getSourceNode(),
                    maps.get(l.getSource().getSourceNode()).getRouterID(),
                    lsw1, this.getGatewayIP(new TenantId(tenantID),lsw1),
                    this.getGatewayPrefix(new TenantId(tenantID),lsw1));

            builder.setVfabricId(new FabricId(l.getDestination().getDestNode()));

            NodeId lsw2 = this.createLneLayer2(tenantID, l.getDestination().getDestNode(),
                    null,//TODO - need to track the intr-logical-swtich id value and for removal in the futrue.
                    this.ulnStore.get(tenantID));

            TpId tpId2 = this.createLogicalPortOnLneLayer2(tenantID, l.getDestination().getDestNode(),lsw2);
            this.netNodeServiceProvider.portBindingLogicalToFabric(new FabricId(l.getDestination().getDestNode()), l.getDestination().getDestTp(), tpId2, lsw2);

            this.createLrLswGateway(tenantID,
                    l.getDestination().getDestNode(),
                    maps.get(l.getDestination().getDestNode()).getRouterID(),
                    lsw2, this.getGatewayIP(new TenantId(tenantID),lsw2),
                    this.getGatewayPrefix(new TenantId(tenantID),lsw2));


            // adding static routes
            for (RenderedRouter lrs : maps.values()) {
                for (RenderedRouter lrd : maps.values()) {
                    List<Link> ls = this.calcShortestPath(lrs.getFabricId(), lrd.getFabricId(), getFabricTopology());
                    UpdateLneLayer3RoutingtableInputBuilder rtinput = new UpdateLneLayer3RoutingtableInputBuilder();
                    rtinput.setLneId(new VcLneId(lrs.getRouterID()));
                    rtinput.setTenantId(new TenantId(tenantID));
                    rtinput.setVfabricId(lrd.getFabricId());

                    List<Routingtable> rtl = new ArrayList<>();
                    for (IpAddress ip : getAllHostIPs(new TenantId(tenantID), lrd.getRouterID())) {
                        RoutingtableBuilder rtbuilder = new RoutingtableBuilder();
                        rtbuilder.setVrfId(lrs.getRouterID().getValue());
                        rtbuilder.setDestIp(ip);
                        // Assuming the resulted path is ordered by distance from nearest
                        // to farthest, the second one should be the next hop.
                        rtbuilder.setNexthopIp(getIPAddress(new TenantId(tenantID), ls.get(0).getDestination().getDestTp()));

                        rtl.add(rtbuilder.build());
                    }
                    this.netNodeServiceProvider.updateLneLayer3Routingtable(rtinput.build());
                }
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
    private List<Link> calcShortestPath(NodeId lrs, NodeId lrd, Topology topo)
    {
        Graph<NodeId, Link> graph = new DirectedSparseMultigraph<>();
        for (Node node : topo.getNode()) {
            graph.addVertex(node.getNodeId());
        }

        for (Link link : topo.getLink())
        {
            graph.addEdge(link, link.getSource().getSourceNode(), link.getDestination().getDestNode());
        }

        DijkstraShortestPath<NodeId, Link> alg = new DijkstraShortestPath<>(graph);
        return alg.getPath(lrs, lrd);
    }

    /**
     * Remove a tenant defined logical router.
     * @param tenantId - tenant identifier.
     * @param lrId - logical router identifier.
     */
    public void removeLneLayer3(Uuid tenantId, NodeId fabrcId, NodeId lrId) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeLneLayer3: vcMgr is null: tenantId={}", tenantId.getValue());
            return;
        }

        for ( Node node : getAllChildren(lrId)) {

            RmLneLayer3InputBuilder builder = new RmLneLayer3InputBuilder();
            builder.setTenantId(new TenantId(tenantId));
            builder.setVfabricId(node.getNodeId());
            builder.setLneId(new VcLneId(lrId));

            Future<RpcResult<Void>> result = this.vcNetNodeService.rmLneLayer3(builder.build());
            try {
                RpcResult<Void> output = result.get();
                if (output.isSuccessful()) {
                    LOG.debug("FABMGR: removeLneLayer3: rmLneLayer3 RPC success");
                }
            } catch (Exception e) {
                LOG.error("FABMGR: ERROR: removeLneLayer3: rmLneLayer3 RPC failed: {}", e);
            }
        }
    }

    public List<FabricId> getAllFabrics()
    {
        return this.netNodeServiceProvider.getAllFabrics();
    }

    /**
     * Binding an physical location with logical port.
     * @param tenantId - tenant identifier.
     * @param lswId - logical switch
     * @param lswLogicalPortId - logical switch port id
     * @param endpoint - end point attributes
     * @return the endpoint uuid.
     */
    public Uuid attachEpToLneLayer2(Uuid tenantId, NodeId lswId, TpId lswLogicalPortId, EndpointAttachInfo endpoint) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = new NodeId(endpoint.getInventoryNodeIdStr());
        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        RegisterEndpointInputBuilder epInputBuilder = new RegisterEndpointInputBuilder();
        epInputBuilder.setEndpointUuid(endpoint.getEpYangUuid());
        FabricId fabricId = new FabricId(vfabricId);
        epInputBuilder.setFabricId(fabricId);
        epInputBuilder.setGateway(endpoint.getGatewayIpAddr());
        epInputBuilder.setIpAddress(endpoint.getIpAddress());
        epInputBuilder.setLocation(FabMgrYangDataUtil.getPhyLocation(new TopologyId("ovsdb:1"),
                endpoint.getInventoryNodeIdStr(), endpoint.getInventoryNodeConnectorIdStr()));
        epInputBuilder.setMacAddress(endpoint.getMacAddress());
        epInputBuilder.setOwnFabric(fabricId);

        LogicalLocationBuilder llb = new LogicalLocationBuilder();
        llb.setNodeId(lswId);
        llb.setTpId(lswLogicalPortId);

        epInputBuilder.setLogicalLocation(llb.build());

        Uuid epId = this.netNodeServiceProvider.registerEndpoint(tenantId, vfabricId, epInputBuilder.build());

        return epId;
    }

    public void unregisterEpFromLneLayer2(Uuid tenantId, NodeId vfabricId, NodeId lswId, Uuid epUuid) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: unregisterEpFromLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        //NodeId vfabricId = getFabricIdForPort(epUuid.getValue());
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: unregisterEpFromLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.unregisterEndpoint(tenantId, vfabricId, lswId, epUuid);
    }

    public TpId createLogicalPortOnLneLayer2(Uuid tenantId, NodeId vfabricId,  NodeId lswId) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null;
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null;
        }

        return this.netNodeServiceProvider.createLogicalPortOnLsw(tenantId, vfabricId, lswId, AccessType.Exclusive, 0);
    }

    public void createLrLswGateway(Uuid tenantId, NodeId vfabricId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createLrLswGateway(tenantId, vfabricId, lrId, lswId, gatewayIpAddr, ipPrefix);
    }

    public void removeLrLswGateway(Uuid tenantId, NodeId fabricId,  NodeId lrId, IpAddress gatewayIpAddr) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        this.netNodeServiceProvider.removeLrLswGateway(tenantId, fabricId, lrId, gatewayIpAddr);
    }

    public Map<Uuid, VContainerConfigMgr> getVcConfigDataMgrList() {
        return vcConfigDataMgrList;
    }

    public void setVcConfigDataMgrList(Map<Uuid, VContainerConfigMgr> vcConfigDataMgrList) {
        this.vcConfigDataMgrList = vcConfigDataMgrList;
    }

    public VContainerConfigMgr getVcConfigDataMgr(Uuid tenantId) {
        return this.vcConfigDataMgrList.get(tenantId);
    }

    public void OnVcCreated(Uuid tenantId) {
        VContainerConfigMgr vc = new VContainerConfigMgr(tenantId);
        this.vcConfigDataMgrList.put(tenantId, vc);
        LOG.debug("FABMGR: listenerActionOnVcCreate: add tenantId: {}", tenantId.getValue());
    }

    public void createAcl(Uuid tenantId, NodeId fabricId, NodeId nodeId, String aclName) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(fabricId)) {
            LOG.error("FABMGR: ERROR: createAcl: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createAcl(tenantId, fabricId, nodeId, aclName);
    }

    public void removeAcl(Uuid tenantId, NodeId fabricId, NodeId nodeId, String aclName) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }


        this.netNodeServiceProvider.removeAcl(tenantId, fabricId, nodeId, aclName);
    }
}
