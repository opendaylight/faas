/*
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.fabricmgr.api.VcontainerServiceProviderAPI;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.LogicLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Output;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricMgrProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FabricMgrProvider.class);
    private final ExecutorService threadPool;
    private final VcontainerServiceProvider vcProvider;
    private final VcNetNodeServiceProvider netNodeServiceProvider;
    private Map<Uuid, VcConfigDataMgr> vcConfigDataMgrList; // tenantId-Vcontainer lookup map

    public FabricMgrProvider(final DataBroker dataProvider, final RpcProviderRegistry rpcRegistry,
            final NotificationService notificationService) {
        super();
        FabMgrDatastoreDependency.setDataProvider(dataProvider);
        FabMgrDatastoreDependency.setRpcRegistry(rpcRegistry);
        FabMgrDatastoreDependency.setNotificationService(notificationService);

        int numCPU = Runtime.getRuntime().availableProcessors();
        this.threadPool = Executors.newFixedThreadPool(numCPU * 2);
        this.vcProvider = new VcontainerServiceProvider(this.threadPool);
        this.vcProvider.initialize();
        this.netNodeServiceProvider = new VcNetNodeServiceProvider(this.threadPool);
        this.netNodeServiceProvider.initialize();

        this.vcConfigDataMgrList = Collections.synchronizedMap(new HashMap<Uuid, VcConfigDataMgr>());

        VcontainerServiceProviderAPI.setFabricMgrProvider(this);

        LOG.info("FABMGR: FabricMgrProvider has Started");
    }

    @Override
    public void close() throws Exception {
        this.vcProvider.close();
        this.netNodeServiceProvider.close();
        this.threadPool.shutdown();
    }

    public NodeId createLneLayer2(Uuid tenantId, CreateLneLayer2Input lneInput) {
        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder(lneInput);
        if (this.vcConfigDataMgrList.containsKey(tenantId) == false) {
            LOG.error("FABMGR: ERROR: createLneLayer2: tenant nout found: {}", tenantId.getValue());
            for (Uuid key : this.vcConfigDataMgrList.keySet()) {
                LOG.debug("FABMGR: createLneLayer2: key={}", key.getValue());
            }
            return null; // ----->
        } else {
            LOG.debug("FABMGR: createLneLayer2: found tenant key: {}", tenantId.getValue());
        }

        VcConfigDataMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = vcMgr.getAvailabeVfabricResource();
        if (vfabricId != null) {
            builder.setVfabricId(vfabricId);
        } else {
            LOG.error("FABMGR: ERROR: createLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        NodeId nodeId = null;
        Future<RpcResult<CreateLneLayer2Output>> result = this.netNodeServiceProvider.createLneLayer2(builder.build());
        try {
            RpcResult<CreateLneLayer2Output> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer2: createLogicRouter RPC success");
                CreateLneLayer2Output createLswOutput = output.getResult();
                nodeId = createLswOutput.getLneId();
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer2: createLogicSwitch RPC failed: {}", e);
        }

        return nodeId;
    }

    public NodeId createLneLayer3(Uuid tenantId, CreateLneLayer3Input lne3Input) {
        CreateLneLayer3InputBuilder builder = new CreateLneLayer3InputBuilder(lne3Input);
        VcConfigDataMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer3: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = vcMgr.getAvailabeVfabricResource();
        if (vfabricId != null) {
            builder.setVfabricId(vfabricId);
        } else {
            LOG.error("FABMGR: ERROR: createLneLayer3: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        this.netNodeServiceProvider.createLneLayer3(builder.build());
        NodeId nodeId = null;
        Future<RpcResult<CreateLneLayer3Output>> result = this.netNodeServiceProvider.createLneLayer3(builder.build());
        try {
            RpcResult<CreateLneLayer3Output> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer3: createLogicRouter RPC success");
                CreateLneLayer3Output createLswOutput = output.getResult();
                nodeId = createLswOutput.getLneId();
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer3: createLogicRouter RPC failed: {}", e);
        }

        return nodeId;
    }

    public Uuid attachEpToLneLayer2(Uuid tenantId, NodeId lswId, TpId lswLogicalPortId, EndpointAttachInfo endpoint) {
        VcConfigDataMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = vcMgr.getAvailabeVfabricResource();
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: attachEpToLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        RegisterEndpointInputBuilder epInputBuilder = new RegisterEndpointInputBuilder();
        epInputBuilder.setEndpointUuid(endpoint.getEpYangUuid());
        FabricId fabricId = new FabricId(vfabricId);
        epInputBuilder.setFabricId(fabricId);
        epInputBuilder.setGateway(endpoint.getGatewayIpAddr());
        epInputBuilder.setIpAddress(endpoint.getIpAddress());
        epInputBuilder.setLocation(FabMgrYangDataUtil.getPhyLocation(new TopologyId(fabricId),
                endpoint.getInventoryNodeIdStr(), endpoint.getInventoryNodeConnectorIdStr()));
        epInputBuilder.setMacAddress(endpoint.getMacAddress());
        epInputBuilder.setOwnFabric(fabricId);

        LogicLocationBuilder llb = new LogicLocationBuilder();
        llb.setNodeId(lswId);
        llb.setTpId(lswLogicalPortId);

        epInputBuilder.setLogicLocation(llb.build());

        Uuid epId = this.netNodeServiceProvider.registerEndpoint(tenantId, vfabricId, epInputBuilder.build());

        return epId;
    }

    public TpId createLogicalPortOnLneLayer2(Uuid tenantId, NodeId lswId) {
        VcConfigDataMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vcMgr is null: tenantId={}", tenantId.getValue());
            return null; // ----->
        }

        NodeId vfabricId = vcMgr.getAvailabeVfabricResource();
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLneLayer2: vfabricId is null: {}", tenantId.getValue());
            return null; // ---->
        }

        TpId logicalPortId = this.netNodeServiceProvider.createLogicalPortOnLsw(tenantId, vfabricId, lswId);

        return logicalPortId;
    }

    public void createLrLswGateway(Uuid tenantId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        VcConfigDataMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = vcMgr.getAvailabeVfabricResource();
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createLrLswGateway(tenantId, vfabricId, lrId, lswId, gatewayIpAddr, ipPrefix);
    }

    public Map<Uuid, VcConfigDataMgr> getVcConfigDataMgrList() {
        return vcConfigDataMgrList;
    }

    public void setVcConfigDataMgrList(Map<Uuid, VcConfigDataMgr> vcConfigDataMgrList) {
        this.vcConfigDataMgrList = vcConfigDataMgrList;
    }

    public VcConfigDataMgr getVcConfigDataMgr(Uuid tenantId) {
        return this.vcConfigDataMgrList.get(tenantId);
    }

    public void listenerActionOnVcCreate(Uuid tenantId) {
        VcConfigDataMgr vc = new VcConfigDataMgr(tenantId);
        this.vcConfigDataMgrList.put(tenantId, vc);
        LOG.debug("FABMGR: listenerActionOnVcCreate: add tenantId: {}", tenantId.getValue());
    }

    public void createAcl(Uuid tenantId, NodeId nodeId, String aclName) {
        VcConfigDataMgr vcMgr = this.vcConfigDataMgrList.get(tenantId);
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createAcl: vcMgr is null: tenantId={}", tenantId.getValue());
            return; // ----->
        }

        NodeId vfabricId = vcMgr.getAvailabeVfabricResource();
        if (vfabricId == null) {
            LOG.error("FABMGR: ERROR: createAcl: vfabricId is null: {}", tenantId.getValue());
            return; // ---->
        }

        this.netNodeServiceProvider.createAcl(tenantId, vfabricId, nodeId, aclName);

    }
}
