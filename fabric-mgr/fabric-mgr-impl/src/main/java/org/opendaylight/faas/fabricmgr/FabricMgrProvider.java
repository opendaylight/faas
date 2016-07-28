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
import java.util.Collection;
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
import java.util.stream.Collectors;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.fabricmgr.api.VContainerServiceProvider;
import org.opendaylight.faas.uln.cache.LogicalSwitchMappingInfo;
import org.opendaylight.faas.uln.cache.PortMappingInfo;
import org.opendaylight.faas.uln.cache.SubnetMappingInfo;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricPortAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.LogicalLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.port.PrivateIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.subnet.ExternalGateways;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VcLneId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VfabricPortId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.AddPortsToLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
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


    public final int EMPTY_TAG = -1;

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


        boolean useSyncMap = false;
        if (useSyncMap) {
            this.ulnStore = Collections.synchronizedMap(new HashMap<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid, UserLogicalNetworkCache>());
        } else {
            this.ulnStore = new ConcurrentHashMap<>();
        }

        VContainerServiceProvider.setFabricMgrProvider(this);

        LOG.info("FABMGR: FabricMgrProvider has Started with threadpool size {}", numCPU);
    }

    public Map<org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid, UserLogicalNetworkCache> getCacheStore()
    {
        return this.ulnStore;
    }

    public static synchronized FabricMgrProvider getInstance(final DataBroker dataProvider, final RpcProviderRegistry rpcRegistry,
            final NotificationService notificationService)
    {
        if(instance == null) {
            return new FabricMgrProvider(dataProvider, rpcRegistry, notificationService);
        }

        return instance;
    }

    @Override
    public void close() throws Exception {

        LOG.info("Shtting down FabricMgrProvider ...");

        this.vcProvider.close();
        this.netNodeServiceProvider.close();
        this.threadPool.shutdown();
    }


    /**
     * Figure out the association betwen each port and its owning fabric.
     * @param eps - the fabric ports list
     * @return - the mapping between each port and its hosting fabric.
     */
    private Map<String, List<FabricPort>> getPortsDistribution(List<FabricPort> eps)
    {
        return eps.stream().collect(Collectors.groupingBy(FabricPort::getFabricID));
    }

    //Internal Port Exchange Format
    private final class FabricPort {
        private final Port p;
        private String fabricID;

        FabricPort(Port port) {
            super();
            this.p = port;
        }

        public Port getPort() {
            return p;
        }

        public String getFabricID() {
            String portid = p.getPortId().getValue();

            //
            //  According to fabric port id naming convention.
            //  FabricPort :  [Node id] : [tp id]
            //
            String[] id = portid.split(":");
            String nodeid = id[0];
            String tpid = id[1];

            InstanceIdentifier<TerminationPoint> tppath = FabMgrYangDataUtil.createFabricTpPath(nodeid, tpid);
            Optional<TerminationPoint> opt = FabMgrDatastoreUtil.readData(LogicalDatastoreType.OPERATIONAL, tppath);

            if (opt.isPresent())
            {
                TerminationPoint tp = opt.get();
                FabricPortAug aug = tp.getAugmentation(FabricPortAug.class);
                //TODO
                return aug.getPortRef().getValue().toString();
            }

            return null;
        }
    }


    private List<FabricPort> convertToFabricPorts(List<Port> lp) {
        List<FabricPort> fp = new ArrayList<>();

        lp.parallelStream().forEach((port) -> {
            fp.add(new FabricPort(port));
        });
        return fp;
    }

    /**
     * createLneLayer2 - create a layer 2 logical network device. i.e. logical switch.
     * @param tenantId - tenant identifier
     * @param lneInput - input parameters
     * @return
     */
    public NodeId createLneLayer2(Uuid tenantId, CreateLneLayer2Input lneInput, UserLogicalNetworkCache cache) {

        // Check resource availability
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        Map<String, List<FabricPort>> dist = getPortsDistribution(convertToFabricPorts(lneInput.getPort()));

        List<NodeId> lsws = new ArrayList();
        for (String vfabricIdStr : dist.keySet()) {
            NodeId vfabricId = new NodeId(vfabricIdStr);
            CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder(lneInput);
            if (vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
                builder.setVfabricId(vfabricId);
            } else {
                LOG.error("FABMGR: ERROR: createLneLayer2: vfabricId is null: {}", tenantId.getValue());
                return null; // ---->
            }

            NodeId nodeId = null;
            Future<RpcResult<CreateLneLayer2Output>> result = this.vcNetNodeService.createLneLayer2(builder.build());
            try {
                RpcResult<CreateLneLayer2Output> output = result.get();
                if (output.isSuccessful()) {
                    LOG.debug("FABMGR: createLneLayer2: createLneLayer2 RPC success");
                    CreateLneLayer2Output createLswOutput = output.getResult();
                    nodeId = createLswOutput.getLneId();

                    LOG.debug("FABMGR: createLneLayer2: lswId={}", nodeId.getValue());
                    lsws.add(nodeId);

                }
            } catch (Exception e) {
                LOG.error("FABMGR: ERROR: createLneLayer2: createLneLayer2 RPC failed: {}", e);
            }

        }

        Graph<NodeId, Link> graph = calcMinimumSpanningTree(getTopology(lsws));
        TopologyBuilder topo = new TopologyBuilder();
        topo.setLink(new ArrayList(graph.getEdges()));
        topo.setNode(new ArrayList(graph.getVertices()));
        FabMgrDatastoreUtil.putData(LogicalDatastoreType.CONFIGURATION,
                FabMgrYangDataUtil.buildTopologyPath(lneInput.getName()), topo.build());

        InstanceIdentifier<Topology> nodeId = FabMgrYangDataUtil.buildTopologyPath(lneInput.getName());
        bridgeAllSegments(tenantId,nodeId);

        return new NodeId(lneInput.getLswUuid().getValue());
    }

    /**
     *  To allocate a global virtual network ID for both end.
     */
    //TODO
    private int allocGlobalTag(Link link)
    {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    //
    // TODO - to get the Topology which contains all the lsws' fabrics.
    //  FabricTopology is the biggest superset of the topology which satisfy the condition
    //   but it is not obviously optimal.
    //
    private Topology getTopology(List<NodeId> lsws)
    {
        return this.getFabricTopology();
    }

    /**
     * Calculate and return the minimum spanning free of the Graph top
     * @param topo - the graph.
     * @return the mst.
     */
    private Graph<NodeId, Link> calcMinimumSpanningTree(Topology topo)
    {

        Tree<NodeId, Link> tree = new DelegateTree<>();
        for(Node node : topo.getNode()) {
            tree.addVertex(node.getNodeId());
        }

        for (Link link : topo.getLink())
        {
            tree.addEdge(link, link.getSource().getSourceNode(), link.getDestination().getDestNode());
        }

        PrimMinimumSpanningTree<NodeId, Link> alg = new PrimMinimumSpanningTree<>
        (DelegateTree.<NodeId, Link>getFactory());

        return alg.transform(tree);
    }


    //TODO
    private void bridgeTwoSegmentsOverALink(Uuid tenantId, int tag, Link l, NodeId s, NodeId d)
    {
        AddPortsToLneLayer2InputBuilder builder = new AddPortsToLneLayer2InputBuilder();
        List<Port> ports = new ArrayList<>();
        PortBuilder pb = new PortBuilder();
        pb.setPortId(new VfabricPortId(l.getSource().getSourceTp()));

        ports.add(pb.build());
        builder.setPort(ports);
        builder.setTenantId(new TenantId(tenantId));
        builder.setLneId(new VcLneId(s));
        builder.setVfabricId(new FabricId((l.getSource().getSourceNode())));
        this.netNodeServiceProvider.addPortsToLneLayer2(builder.build());

        ports.clear();
        pb.setPortId(new VfabricPortId(l.getDestination().getDestTp()));
        ports.add(pb.build());
        builder.setPort(ports);

        builder.setTenantId(new TenantId(tenantId));

        builder.setLneId(new VcLneId(d));

        builder.setVfabricId(new FabricId(l.getDestination().getDestNode()));
        this.netNodeServiceProvider.addPortsToLneLayer2(builder.build());

    }

    //
    // bridge the logical switch's all layer 2 segements on different fabrics
    //
    private void bridgeAllSegments(Uuid tenantId,InstanceIdentifier<Topology> id)
    {
        Optional<Topology> topo = FabMgrDatastoreUtil.readData(LogicalDatastoreType.CONFIGURATION, id);
        if (topo.isPresent())
        {
            Topology t = topo.get();
            Map<FabricId, NodeId> maps = buildMaps(t.getNode());

            for(Link l : t.getLink())
            {
                int tag = allocGlobalTag(l);
                if (tag != EMPTY_TAG) {
                    this.bridgeTwoSegmentsOverALink(tenantId, tag, l,
                            maps.get(new FabricId(l.getSource().getSourceNode())),
                            maps.get(new FabricId(l.getDestination().getDestNode())));
                }
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
            Topology t = topo.get();
            return t.getNode();
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


    private Map<String, List<NodeId>> getSwitchDistribution(List<Port> ports) {
        new HashMap<>();
        throw new UnsupportedOperationException("Not implemented yet!");
    }


    public NodeId createLneLayer3(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, CreateLneLayer3Input lne3Input) {
        CreateLneLayer3InputBuilder builder = new CreateLneLayer3InputBuilder(lne3Input);
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer3: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        Map<String, List<NodeId>> dist = getSwitchDistribution((lne3Input.getPort()));

        List<NodeId> lrs = new ArrayList();
        for (String vfabricIdStr : dist.keySet()) {

            NodeId vfabricId = new NodeId(vfabricIdStr);
            if (vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
                builder.setVfabricId(vfabricId);
            } else {
                LOG.error("FABMGR: ERROR: createLneLayer3: vfabricId is null: {}", tenantId.getValue());
                return null; // ---->
            }

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
            lrs.add(nodeId);
        }

        //building the logical router identifier with its UUID.
        NodeId lrID = new NodeId(lne3Input.getLrUuid().getValue());

        //connect all logical routers across multiple fabrics and
        //populate their routing tables.
        connectAllDVRs(tenantId, lrID);

        return lrID;
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


    private int allocGlobalTagFromFabric(FabricId id)
    {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
    /**
     * setupExternalGW - Configure a logical port which binding to a physical port with NAT functionality if NAT
     * is required.
     * @param tenantId
     * @param lrId
     * @param gatewayIPAddr
     * @param prefix
     */
    //
    // Configure a logical port which binding to a physical port with NAT functionality if NAT
    // is required.
    //
    private void setupExternalGW(Uuid tenantId, NodeId lrId, IpAddress gatewayIPAddr, IpPrefix prefix)
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

        int tag = this.allocGlobalTagFromFabric(entry.getKey());
        builder.setSegmentId(new Long(tag));
        builder.setTenantId(new TenantId(tenantId));
        builder.setVfabricId(entry.getKey());
        NodeId lswId = this.createLneLayer2(tenantId, builder.build(), this.ulnStore.get(tenantId));

        this.createLrLswGateway(tenantId, lrId, lswId, gatewayIPAddr, prefix);

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
    //
    // Connects all distributed logical routers into one big logical router
    // all the routes or host routes or prefix needs to be correctly configured on each
    // logical router on each participant fabric
    //
    private void connectAllDVRs(Uuid tenantID, NodeId lr)
    {
        List<Node> nodes = getAllChildren(lr);
        Map<FabricId, NodeId> maps = buildMaps(nodes);
        Collection<Link> links = calcMinimumSpanningTree(getFabricTopology()).getEdges();
        for(Link l : links)
        {
            int tag = allocGlobalTag(l);
            CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
            builder.setSegmentId(new Long(tag));
            builder.setTenantId(new TenantId(tenantID));
            builder.setVfabricId(new FabricId(l.getSource().getSourceNode()));
            NodeId lsw1 = this.createLneLayer2(new TenantId(tenantID), builder.build(), this.ulnStore.get(tenantID));
            this.createLrLswGateway(new TenantId(tenantID), maps.get(l.getSource().getSourceNode()), lsw1, this.getGatewayIP(new TenantId(tenantID),lsw1), this.getGatewayPrefix(new TenantId(tenantID),lsw1));

            builder.setVfabricId(new FabricId(l.getDestination().getDestNode()));
            NodeId lsw2 = this.createLneLayer2(new TenantId(tenantID), builder.build(), this.ulnStore.get(tenantID));
            this.createLrLswGateway(new TenantId(tenantID), maps.get(l.getDestination().getDestNode()), lsw2, this.getGatewayIP(new TenantId(tenantID),lsw2), this.getGatewayPrefix(new TenantId(tenantID),lsw2));


            // adding static routes
            for (Node lrs : nodes) {
                for (Node lrd : nodes) {
                    List<Link> ls = this.calcShortestPath(lrs.getNodeId(), lrd.getNodeId(), getFabricTopology());
                    UpdateLneLayer3RoutingtableInputBuilder rtinput = new UpdateLneLayer3RoutingtableInputBuilder();
                    rtinput.setLneId(new VcLneId(lrs.getNodeId()));
                    rtinput.setTenantId(new TenantId(tenantID));
                    rtinput.setVfabricId(getFabricIDForNode(lrd));

                    List<Routingtable> rtl = new ArrayList<>();
                    for (IpAddress ip : getAllHostIPs(new TenantId(tenantID), lrd.getNodeId())) {
                        RoutingtableBuilder rtbuilder = new RoutingtableBuilder();
                        rtbuilder.setVrfId(lrs.getNodeId().getValue());
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

    //TODO
    private NodeId getFabricIdForPort(String portID)
    {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Uuid attachEpToLneLayer2(Uuid tenantId, NodeId lswId, TpId lswLogicalPortId, EndpointAttachInfo endpoint) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = getFabricIdForPort(endpoint.getInventoryNodeConnectorIdStr());
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

    public TpId createLogicalPortOnLneLayer2(Uuid tenantId, NodeId lswId) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = getFabricIdForPort(lswId.getValue());
        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        TpId logicalPortId = this.netNodeServiceProvider.createLogicalPortOnLsw(tenantId, vfabricId, lswId);

        return logicalPortId;
    }

    public void createLrLswGateway(Uuid tenantId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = getFabricIdForPort(lswId.getValue());
        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createLrLswGateway(tenantId, vfabricId, lrId, lswId, gatewayIpAddr, ipPrefix);
    }

    public void removeLrLswGateway(Uuid tenantId, NodeId lrId, IpAddress gatewayIpAddr) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = getFabricIdForPort(lrId.getValue());
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: removeLrLswGateway: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.removeLrLswGateway(tenantId, vfabricId, lrId, gatewayIpAddr);
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

    public void createAcl(Uuid tenantId, NodeId nodeId, String aclName) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = getFabricIdForPort(nodeId.getValue());
        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createAcl: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createAcl(tenantId, vfabricId, nodeId, aclName);
    }

    public void removeAcl(Uuid tenantId, NodeId fabricId, NodeId nodeId, String aclName) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = getFabricIdForPort(nodeId.getValue());
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: removeAcl: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.removeAcl(tenantId, vfabricId, nodeId, aclName);
    }
}
