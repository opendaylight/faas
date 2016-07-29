/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VfabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.vc.ld.node.attributes.Vfabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.CreateVcontainerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.CreateVcontainerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.CreateVcontainerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.TopologyTypes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.TopologyTypes1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.VcontainerTopologyService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.create.vcontainer.input.VcontainerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.vcontainer.topology.type.VcontainerTopology;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.topology.rev151010.vcontainer.topology.type.VcontainerTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.TopologyTypes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;

/**
 * VContainerMgr - create, delete and modify Virtual Container objects.
 *
 */
public class VContainerMgr implements AutoCloseable, VcontainerTopologyService {

    private static final Logger LOG = LoggerFactory.getLogger(VContainerMgr.class);
    private static final LogicalDatastoreType OPERATIONAL = LogicalDatastoreType.OPERATIONAL;

    private RpcRegistration<VcontainerTopologyService> rpcRegistration;
    private final ExecutorService threadPool;

    public VContainerMgr(ExecutorService executor) {
        this.threadPool = executor;
    }

    public void initialize() {
        this.rpcRegistration =
                FabMgrDatastoreDependency.getRpcRegistry().addRpcImplementation(VcontainerTopologyService.class, this);
    }

    @Override
    public void close() throws Exception {
        if (this.rpcRegistration != null) {
            this.rpcRegistration.close();
        }
    }

    @Override
    public Future<RpcResult<CreateVcontainerOutput>> createVcontainer(CreateVcontainerInput input) {
        final RpcResultBuilder<CreateVcontainerOutput> resultBuilder =
                RpcResultBuilder.<CreateVcontainerOutput>success();
        final CreateVcontainerOutputBuilder outputBuilder = new CreateVcontainerOutputBuilder();

        VcontainerConfig vcConfig = input.getVcontainerConfig();
        TenantId tenantId = input.getTenantId();
        TopologyId vcTopologyId = createVcTopology(tenantId, vcConfig);
        outputBuilder.setVcTopologyId(vcTopologyId);

        // TODO: This should be implemented as datastore listener event.
        FabricMgrProvider.getInstance().OnVcCreated(new Uuid(tenantId.getValue()));

        List<Vfabric> vfabricList = vcConfig.getVfabric();
        List<NodeId> vfabricIdList = new ArrayList<>();
        if (vfabricList != null && vfabricList.isEmpty() == false) {
            for (Vfabric vfab : vfabricList) {
                VfabricId vfabId = vfab.getVfabricId();
                vfabricIdList.add(new NodeId(vfabId.getValue()));
            }
        }

        VContainerConfigMgr vc =
                FabricMgrProvider.getInstance().getVcConfigDataMgr(new Uuid(tenantId.getValue()));
        if (vc == null) {
            LOG.error("FABMGR: ERROR: createVcontainer: vc is null");
        } else {
            vc.getLdNodeConfigDataMgr().addVFabrics(tenantId, vfabricIdList);
        }

        return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
    }

    private TopologyId createVcTopology(TenantId tenantId, VcontainerConfig vcConfig) {
        TopologyId topoId = null;

        /*
         * Create topology instance. Its topology key will be the tenantId, and
         * its topology type will be vcontainer-topology
         */
        InstanceIdentifier<Topology> topoPath = this.createVcTopologyInstance(tenantId.getValue());

        this.createVcontainer(topoPath, vcConfig);

        return topoId;
    }

    private void createVcontainer(InstanceIdentifier<Topology> topoPath, VcontainerConfig vcConfig) {
        InstanceIdentifier<Node> ldNodePath = FabMgrYangDataUtil.vcLdNodePath(topoPath);
        Node ldNode = FabMgrYangDataUtil.createBasicVcLdNode();
        ldNode = FabMgrYangDataUtil.updateVcLdNode(ldNode, vcConfig);

        InstanceIdentifier<Node> netNodePath = FabMgrYangDataUtil.vcNetNodePath(topoPath);
        Node netNode = FabMgrYangDataUtil.createBasicVcNetNode();

        InstanceIdentifier<Link> linkPath = FabMgrYangDataUtil.vcLinkPath(topoPath);
        Link link = FabMgrYangDataUtil.createBasicVcLink(ldNode, netNode);

        FabMgrDatastoreUtil.putData(OPERATIONAL, ldNodePath, ldNode);
        FabMgrDatastoreUtil.putData(OPERATIONAL, netNodePath, netNode);
        FabMgrDatastoreUtil.putData(OPERATIONAL, linkPath, link);
    }

    private InstanceIdentifier<Topology> createVcTopologyInstance(String topoIdStr) {
        TopologyId topoId = new TopologyId(topoIdStr);
        TopologyKey topoKey = new TopologyKey(topoId);

        InstanceIdentifier<Topology> topoPath = FabMgrYangDataUtil.buildTopologyPath(topoIdStr);
        TopologyBuilder topoBuilder = new TopologyBuilder();
        topoBuilder.setKey(topoKey);
        topoBuilder.setTopologyId(topoId);

        Topology topo = topoBuilder.build();
        FabMgrDatastoreUtil.putData(OPERATIONAL, topoPath, topo);

        InstanceIdentifier<TopologyTypes1> topoTypePath =
                topoPath.child(TopologyTypes.class).augmentation(TopologyTypes1.class);

        VcontainerTopology vcTopoType = new VcontainerTopologyBuilder().build();
        TopologyTypes1 topologyTypeAugment = new TopologyTypes1Builder().setVcontainerTopology(vcTopoType).build();
        FabMgrDatastoreUtil.putData(OPERATIONAL, topoTypePath, topologyTypeAugment);

        return topoPath;
    }

}
