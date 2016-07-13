/*
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.fabricmgr.api.VContainerServiceProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.LogicalLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VcLneId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.VcNetNodeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.ports.Port;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
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

        VContainerServiceProvider.setFabricMgrProvider(this);

        LOG.info("FABMGR: FabricMgrProvider has Started with threadpool size {}", numCPU);
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


    private Map<String, List<FabricPort>> getPortsDistribution(List<FabricPort> eps)
    {
        return eps.stream().collect(Collectors.groupingBy(FabricPort::getFabricID));
    }

    private final class FabricPort {
        private final Port p;
        private String fabricID;

        public FabricPort(Port port) {
            super();
            this.p = port;
        }

        public Port getP() {
            return p;
        }

        //TODO
        public String getFabricID() {
            return fabricID;
        }
    }


    private List<FabricPort> convertToFabricPorts(List<Port> lp) {
        List<FabricPort> fp = new ArrayList<>();

        lp.parallelStream().forEach((port) -> {
            fp.add(new FabricPort(port));
        });
        return fp;
    }

    public NodeId createLneLayer2(Uuid tenantId, CreateLneLayer2Input lneInput) {
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
                }
            } catch (Exception e) {
                LOG.error("FABMGR: ERROR: createLneLayer2: createLneLayer2 RPC failed: {}", e);
            }

            LOG.debug("FABMGR: createLneLayer2: lswId={}", nodeId.getValue());

            lsws.add(nodeId);
        }

        //TODO
        return new NodeId("todo");
    }

    //TODO - to read from datastore.
    List<NodeId> getChildren(NodeId lswId)
    {
        return null;
    }


    public void removeLneLayer2(Uuid tenantId, NodeId lswId) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        for (NodeId vfabricId : getChildren(lswId)) {
            if (vfabricId == null) {
                LOG.error("FABMGR: ERROR: removeLneLayer2: vfabricId is null: {}", tenantId.getValue());
                return; // ---->
            }

            RmLneLayer2InputBuilder builder = new RmLneLayer2InputBuilder();
            builder.setTenantId(new TenantId(tenantId));
            builder.setVfabricId(vfabricId);
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
        Map<String, List<NodeId>> map = new HashMap<>();
        return map;
    }


    public NodeId createLneLayer3(Uuid tenantId, CreateLneLayer3Input lne3Input) {
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

        return (new NodeId("todo"));
    }

    public void removeLneLayer3(Uuid tenantId, NodeId lrId) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeLneLayer3: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        for (NodeId vfabricId : getChildren(lrId)) {

            RmLneLayer3InputBuilder builder = new RmLneLayer3InputBuilder();
            builder.setTenantId(new TenantId(tenantId));
            builder.setVfabricId(vfabricId);
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

    //TODO
    private NodeId getFabricId(String portID)
    {
        return null;
    }

    public Uuid attachEpToLneLayer2(Uuid tenantId, NodeId lswId, TpId lswLogicalPortId, EndpointAttachInfo endpoint) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = getFabricId(endpoint.getInventoryNodeConnectorIdStr());
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

    public void unregisterEpFromLneLayer2(Uuid tenantId, NodeId lswId, Uuid epUuid) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: unregisterEpFromLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = getFabricId(epUuid.getValue());
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

        NodeId vfabricId = getFabricId(lswId.getValue());
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

        NodeId vfabricId = getFabricId(lswId.getValue());
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

        NodeId vfabricId = getFabricId(lrId.getValue());
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

        NodeId vfabricId = getFabricId(nodeId.getValue());
        if (!vcMgr.getLdNodeConfigDataMgr().isVFabricAvailable(vfabricId)) {
            LOG.error("FABMGR: ERROR: createAcl: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createAcl(tenantId, vfabricId, nodeId, aclName);
    }

    public void removeAcl(Uuid tenantId, NodeId nodeId, String aclName) {
        VContainerConfigMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: removeAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = getFabricId(nodeId.getValue());
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: removeAcl: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.removeAcl(tenantId, vfabricId, nodeId, aclName);
    }
}
