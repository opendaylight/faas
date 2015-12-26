/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.faas.fabricmgr.api.VcontainerServiceProviderAPI;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.FabricEndpointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddAclInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.FabricServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VcLneId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.AddApplianceToNetNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.AddPortsToLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.AddPortsToLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.AddVfabricToNetNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateChildNetNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateChildNetNodeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer1Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer1Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2OutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3OutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RemovedChildNetNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmApplianceFromNetNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer1Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmNetNodeLogicalPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmPortsFromLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmPortsFromLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RmVfabricFromNetNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.UpdateLneLayer3RoutingtableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.UpdateNetNodeLogicalPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.VcNetNodeService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;

public class VcNetNodeServiceProvider implements AutoCloseable, VcNetNodeService {

    private static final Logger LOG = LoggerFactory.getLogger(VcNetNodeServiceProvider.class);

    private RpcRegistration<VcNetNodeService> rpcRegistration;
    private final ExecutorService threadPool;
    private FabricEndpointService epService;
    private FabricServiceService fabServiceService;

    public VcNetNodeServiceProvider(ExecutorService executor) {
        this.threadPool = executor;
    }

    public void initialize() {
        this.rpcRegistration =
                FabMgrDatastoreDependency.getRpcRegistry().addRpcImplementation(VcNetNodeService.class, this);
        this.epService = FabMgrDatastoreDependency.getRpcRegistry().getRpcService(FabricEndpointService.class);
        this.fabServiceService = FabMgrDatastoreDependency.getRpcRegistry().getRpcService(FabricServiceService.class);
    }

    @Override
    public void close() throws Exception {
        if (this.rpcRegistration != null) {
            this.rpcRegistration.close();
        }
    }

    @Override
    public Future<RpcResult<CreateLneLayer2Output>> createLneLayer2(CreateLneLayer2Input input) {
        TenantId tenantId = input.getTenantId();
        NodeId vfabricId = input.getVfabricId();
        String lswName = input.getName();

        CreateLogicSwitchInputBuilder lswInputBuilder = new CreateLogicSwitchInputBuilder();

        FabricId fabricId = new FabricId(vfabricId);
        lswInputBuilder.setFabricId(fabricId);
        lswInputBuilder.setName(lswName);
        VcConfigDataMgr vcMgr =
                VcontainerServiceProviderAPI.getFabricMgrProvider().getVcConfigDataMgr(new Uuid(tenantId.getValue()));
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vcMgr is null: {}", tenantId.getValue());
            return Futures.immediateFailedFuture(new IllegalArgumentException("vcMgr is null"));
        }

        int l2Resource = vcMgr.getLdNodeConfigDataMgr().getAvailableL2Resource(vfabricId);
        Integer vni = new Integer(l2Resource);
        lswInputBuilder.setVni(vni);

        LOG.debug("FABMGR: createLneLayer2: lswName={}, fabricId={}, vni={}", lswName, fabricId.getValue(),
                vni.intValue());

        final RpcResultBuilder<CreateLneLayer2Output> resultBuilder = RpcResultBuilder.<CreateLneLayer2Output>success();
        Future<RpcResult<CreateLogicSwitchOutput>> result =
                this.fabServiceService.createLogicSwitch(lswInputBuilder.build());
        try {
            RpcResult<CreateLogicSwitchOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer2: createLogicSwitch RPC success");
                CreateLneLayer2OutputBuilder builder = new CreateLneLayer2OutputBuilder();
                CreateLogicSwitchOutput createLswOutput = output.getResult();

                NodeId nodeId = createLswOutput.getNodeId();
                // VcLneRef lswRef = new
                // VcLneRef(FabMgrYangDataUtil.createNodePath(fabricId.toString(), nodeId));
                builder.setLneId(new VcLneId(nodeId));
                return Futures.immediateFuture(resultBuilder.withResult((builder.build())).build());
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer2: createLogicSwitch RPC failed.", e);
        }

        return Futures.immediateFailedFuture(new IllegalArgumentException("createLogicSwitch RPC failed"));
    }

    @Override
    public Future<RpcResult<Void>> addPortsToLneLayer2(AddPortsToLneLayer2Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<CreateLneLayer3Output>> createLneLayer3(CreateLneLayer3Input input) {
        NodeId vfabricId = input.getVfabricId();
        String lrName = input.getName();

        CreateLogicRouterInputBuilder lrInputBuilder = new CreateLogicRouterInputBuilder();

        FabricId fabricId = new FabricId(vfabricId);
        lrInputBuilder.setFabricId(fabricId);
        lrInputBuilder.setName(lrName);

        LOG.debug("FABMGR: createLneLayer3: lrName={}, fabricId={}", lrName, fabricId.getValue());

        final RpcResultBuilder<CreateLneLayer3Output> resultBuilder = RpcResultBuilder.<CreateLneLayer3Output>success();
        Future<RpcResult<CreateLogicRouterOutput>> result =
                this.fabServiceService.createLogicRouter(lrInputBuilder.build());
        try {
            RpcResult<CreateLogicRouterOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer3: createLogicRouter RPC success");
                CreateLneLayer3OutputBuilder builder = new CreateLneLayer3OutputBuilder();
                CreateLogicRouterOutput createLrOutput = output.getResult();
                NodeId nodeId = createLrOutput.getNodeId();
                // VcLneRef lrRef = new
                // VcLneRef(FabMgrYangDataUtil.createNodePath(fabricId.toString(), nodeId));
                builder.setLneId(new VcLneId(nodeId));
                return Futures.immediateFuture(resultBuilder.withResult((builder.build())).build());
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer3: createLogicRouter RPC failed.", e);
        }

        return Futures.immediateFailedFuture(new IllegalArgumentException("createLogicRouter RPC failed"));
    }

    @Override
    public Future<RpcResult<Void>> addPortsToLneLayer3(AddPortsToLneLayer3Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<CreateLneLayer1Output>> createLneLayer1(CreateLneLayer1Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<CreateChildNetNodeOutput>> createChildNetNode(CreateChildNetNodeInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addVfabricToNetNode(AddVfabricToNetNodeInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removedChildNetNode(RemovedChildNetNodeInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateNetNodeLogicalPort(UpdateNetNodeLogicalPortInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmPortsFromLneLayer2(RmPortsFromLneLayer2Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmPortsFromLneLayer3(RmPortsFromLneLayer3Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmVfabricFromNetNode(RmVfabricFromNetNodeInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLneLayer2(RmLneLayer2Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLneLayer3(RmLneLayer3Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmNetNodeLogicalPort(RmNetNodeLogicalPortInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLneLayer1(RmLneLayer1Input input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateLneLayer3Routingtable(UpdateLneLayer3RoutingtableInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addApplianceToNetNode(AddApplianceToNetNodeInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmApplianceFromNetNode(RmApplianceFromNetNodeInput input) {
        return null;
    }

    public TpId createLogicalPortOnLsw(Uuid tenantId, NodeId vfabricId, NodeId lswId) {
        TpId tpId = null;
        CreateLogicPortInputBuilder inputBuilder = new CreateLogicPortInputBuilder();

        FabricId fabricId = new FabricId(vfabricId);
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setLogicDevice(lswId);
        inputBuilder.setName("LswLogicalPort");

        Future<RpcResult<CreateLogicPortOutput>> result = this.fabServiceService.createLogicPort(inputBuilder.build());
        try {
            RpcResult<CreateLogicPortOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLogicalPortOnLsw: createLogicPort RPC success");
                CreateLogicPortOutput createLogicPortOutput = output.getResult();
                tpId = createLogicPortOutput.getTpId();
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLogicalPortOnLsw: createLogicPort RPC failed.", e);
        }

        return tpId;
    }

    public Uuid registerEndpoint(Uuid tenantId, NodeId vfabricId, RegisterEndpointInput epInput) {
        Uuid epId = null;

        RegisterEndpointInputBuilder inputBuilder = new RegisterEndpointInputBuilder(epInput);

        Future<RpcResult<RegisterEndpointOutput>> result = this.epService.registerEndpoint(inputBuilder.build());
        try {
            RpcResult<RegisterEndpointOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: registerEndpoint: registerEndpoint RPC success");
                RegisterEndpointOutput epOutput = output.getResult();
                epId = epOutput.getEndpointId();
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: registerEndpoint: registerEndpoint RPC failed.", e);
        }

        return epId;
    }

    public void createLrLswGateway(Uuid tenantId, NodeId vfabricId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        CreateGatewayInputBuilder inputBuilder = new CreateGatewayInputBuilder();
        FabricId fabricId = new FabricId(vfabricId);
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setLogicRouter(lrId);
        inputBuilder.setLogicSwitch(lswId);
        inputBuilder.setIpAddress(gatewayIpAddr);
        inputBuilder.setNetwork(ipPrefix);

        Future<RpcResult<Void>> result = this.fabServiceService.createGateway(inputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.info("FABMGR: createLrLswGateway: createGateway RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: createGateway RPC failed.", e);
        }
    }

    public void createAcl(Uuid tenantId, NodeId vfabricId, NodeId nodeId, String aclName) {
        AddAclInputBuilder inputBuilder = new AddAclInputBuilder();
        FabricId fabricId = new FabricId(vfabricId);
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setAclName(aclName);
        /*
         * NOTE: The NodeId must be new'd before passing to RPC. Otherwise,
         * addAcl() PRC return failure, because Fabric cannot find the logic
         * device in its Cache map using the nodeId as search key.
         */
        NodeId deviceId = new NodeId(nodeId.getValue());
        inputBuilder.setLogicDevice(deviceId);

        LOG.debug("FABMGR: createAcl: fabricId={}, deviceId={}, aclName={}", fabricId.getValue(), deviceId.getValue(),
                aclName);
        Future<RpcResult<Void>> result = this.fabServiceService.addAcl(inputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.info("FABMGR: createAcl: addAcl RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createAcl: addAcl RPC failed.", e);
        }
    }

}
