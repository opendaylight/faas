/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.faas.fabricmgr.api.VContainerServiceProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.FabricEndpointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.UnregisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.GetAllFabricsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddAclInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddStaticRouteInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalRouterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalRouterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.DelAclInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.FabricServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.PortBindingLogicalToFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmGatewayInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicalRouterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicalSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.RouteBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.RemoveChildNetNodeInput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.ports.Port;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.update.lne.layer3.routingtable.input.Routingtable;

/**
 * VContainerNetNodeServiceProvider implements RPC stub for FaaS Services.
 *
 */
public class VContainerNetNodeServiceProvider implements AutoCloseable, VcNetNodeService {

    private static final Logger LOG = LoggerFactory.getLogger(VContainerNetNodeServiceProvider.class);

    private RpcRegistration<VcNetNodeService> rpcRegistration;
    private final ExecutorService threadPool;
    private FabricEndpointService epService;
    private FabricServiceService fabServiceService;
    private FabricService fabService;

    /**
     * Constructor.
     * @param executor - thread pool to run the tasks to program the devices.
     */
    public VContainerNetNodeServiceProvider(ExecutorService executor) {
        this.threadPool = executor;
    }

    /**
     * Injects services.
     */
    public void initialize() {
        this.rpcRegistration =
                FabMgrDatastoreDependency.getRpcRegistry().addRpcImplementation(VcNetNodeService.class, this);
        this.epService = FabMgrDatastoreDependency.getRpcRegistry().getRpcService(FabricEndpointService.class);
        this.fabServiceService = FabMgrDatastoreDependency.getRpcRegistry().getRpcService(FabricServiceService.class);
        this.fabService = FabMgrDatastoreDependency.getRpcRegistry().getRpcService(FabricService.class);
    }

    @Override
    public void close() throws Exception {
        if (this.rpcRegistration != null) {
            this.rpcRegistration.close();
        }
    }


    @Override
    public Future<RpcResult<CreateLneLayer2Output>> createLneLayer2(CreateLneLayer2Input input) {
        CreateLogicalSwitchInputBuilder lswInputBuilder = new CreateLogicalSwitchInputBuilder();
        TenantId tenantId = input.getTenantId();
        String lswName = input.getName();

        FabricId fabricId = new FabricId(input.getVfabricId());
        lswInputBuilder.setFabricId(fabricId);
        lswInputBuilder.setName(lswName);
        VContainerConfigMgr vcMgr =
                VContainerServiceProvider.getFabricMgrProvider().getVcConfigDataMgr(new Uuid(tenantId.getValue()));
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: createLneLayer2: vcMgr is null: {}", tenantId.getValue());
            return Futures.immediateFailedFuture(new IllegalArgumentException("vcMgr is null"));
        }

        int l2Resource = vcMgr.getLdNodeConfigDataMgr().getAvailableL2Resource(fabricId);
        Integer vni = new Integer(l2Resource);
        lswInputBuilder.setVni(vni);

        LOG.debug("FABMGR: createLneLayer2: lswName={}, fabricId={}, vni={}", lswName, fabricId.getValue(),
                vni.intValue());

        final RpcResultBuilder<CreateLneLayer2Output> resultBuilder = RpcResultBuilder.<CreateLneLayer2Output>success();
        Future<RpcResult<CreateLogicalSwitchOutput>> result =
                this.fabServiceService.createLogicalSwitch(lswInputBuilder.build());
        try {
            RpcResult<CreateLogicalSwitchOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer2: createLogicSwitch RPC success");
                CreateLneLayer2OutputBuilder builder = new CreateLneLayer2OutputBuilder();
                CreateLogicalSwitchOutput createLswOutput = output.getResult();

                NodeId nodeId = createLswOutput.getNodeId();
                builder.setLneId(new VcLneId(nodeId));

                //
                // create logical ports on the logical switch and binding physical ports to them
                //
                for (Port fp : input.getPort())
                {
                    TpId tp = this.createLogicalPortOnLsw(new Uuid (input.getTenantId().getValue()), new NodeId(fabricId), nodeId);
                    PortBindingLogicalToFabricInputBuilder inputBuilder = new PortBindingLogicalToFabricInputBuilder();
                    inputBuilder.setFabricPort(fp.getPortId());
                    inputBuilder.setFabricId(new FabricId(fabricId));
                    inputBuilder.setLogicalPort(tp);
                    this.fabServiceService.portBindingLogicalToFabric(inputBuilder.build());
                }
                //Binding over

                return Futures.immediateFuture(resultBuilder.withResult((builder.build())).build());
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer2: createLogicSwitch RPC failed.", e);
        }

        return Futures.immediateFailedFuture(new IllegalArgumentException("createLogicSwitch RPC failed"));

    }


    @Override
    public Future<RpcResult<Void>> addPortsToLneLayer2(AddPortsToLneLayer2Input input) {
        for (Port fp : input.getPort())
        {
            TpId tp = this.createLogicalPortOnLsw(new Uuid (input.getTenantId().getValue()), input.getVfabricId(), input.getLneId());
            PortBindingLogicalToFabricInputBuilder inputBuilder = new PortBindingLogicalToFabricInputBuilder();
            inputBuilder.setFabricPort(fp.getPortId());
            inputBuilder.setFabricId(new FabricId(input.getVfabricId()));
            inputBuilder.setLogicalPort(tp);
            this.fabServiceService.portBindingLogicalToFabric(inputBuilder.build());
        }

        return null;
    }

    @Override
    public Future<RpcResult<CreateLneLayer3Output>> createLneLayer3(CreateLneLayer3Input input) {
        String lrName = input.getName();

        CreateLogicalRouterInputBuilder lrInputBuilder = new CreateLogicalRouterInputBuilder();

        FabricId fabricId = new FabricId(input.getVfabricId());
        lrInputBuilder.setFabricId(fabricId);
        lrInputBuilder.setName(lrName);


        LOG.debug("FABMGR: createLneLayer3: lrName={}, fabricId={}", lrName, fabricId.getValue());

        final RpcResultBuilder<CreateLneLayer3Output> resultBuilder = RpcResultBuilder.<CreateLneLayer3Output>success();
        Future<RpcResult<CreateLogicalRouterOutput>> result =
                this.fabServiceService.createLogicalRouter(lrInputBuilder.build());
        try {
            RpcResult<CreateLogicalRouterOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer3: createLogicRouter RPC success");
                CreateLneLayer3OutputBuilder builder = new CreateLneLayer3OutputBuilder();
                CreateLogicalRouterOutput createLrOutput = output.getResult();
                NodeId nodeId = createLrOutput.getNodeId();
                // VcLneRef lrRef = new
                // VcLneRef(FabMgrYangDataUtil.createNodePath(fabricId.toString(), nodeId));
                builder.setLneId(new VcLneId(nodeId));

                //binding router
                //TODO TODO TODO
                for (Port lswId : input.getPort())
                {
                   createLrLswGateway(new Uuid (input.getTenantId().getValue()),
                            new NodeId(fabricId), nodeId, new NodeId(lswId.getPortId()), null, null);
                }
                //

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
        TenantId tenantId = input.getTenantId();
        VcLneId lswId = input.getLneId();

        VContainerConfigMgr vcMgr =
                VContainerServiceProvider.getFabricMgrProvider().getVcConfigDataMgr(new Uuid(tenantId.getValue()));
        if (vcMgr == null) {
            LOG.error("FABMGR: ERROR: rmLneLayer2: vcMgr is null: {}", tenantId.getValue());
            return Futures.immediateFailedFuture(new IllegalArgumentException("vcMgr is null"));
        }
        vcMgr.getLdNodeConfigDataMgr().releaseL2Resource(input.getVfabricId());

        RmLogicalSwitchInputBuilder inputBuilder = new RmLogicalSwitchInputBuilder();

        FabricId fabricId = new FabricId(input.getVfabricId());
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setNodeId(new NodeId(lswId));

        LOG.debug("FABMGR: rmLneLayer2: fabricId={}, lswId={}", fabricId.getValue(), lswId.getValue());

        final RpcResultBuilder<Void> resultBuilder = RpcResultBuilder.<Void>success();
        Future<RpcResult<Void>> result = this.fabServiceService.rmLogicalSwitch(inputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: rmLneLayer2: rmLogicSwitch RPC success");
                return result;
            } else {
                return Futures
                    .immediateFuture(resultBuilder.withError(ErrorType.RPC, "rmLogicSwitch RPC failed").build());
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: rmLneLayer2: rmLogicSwitch RPC failed.", e);
        }

        return Futures.immediateFailedFuture(new IllegalArgumentException("rmLogicSwitch RPC failed"));
    }


    @Override
    public Future<RpcResult<Void>> rmLneLayer3(RmLneLayer3Input input) {
        VcLneId lrId = input.getLneId();

        RmLogicalRouterInputBuilder inputBuilder = new RmLogicalRouterInputBuilder();

        FabricId fabricId = new FabricId(input.getVfabricId());
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setNodeId(new NodeId(lrId));

        LOG.debug("FABMGR: rmLneLayer3: fabricId={}, lrId={}", fabricId.getValue(), lrId.getValue());

        final RpcResultBuilder<Void> resultBuilder = RpcResultBuilder.<Void>success();
        Future<RpcResult<Void>> result = this.fabServiceService.rmLogicalRouter(inputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: rmLneLayer3: rmLogicRouter RPC success");
                return result;
            } else {
                return Futures
                    .immediateFuture(resultBuilder.withError(ErrorType.RPC, "rmLogicRouter RPC failed").build());
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: rmLneLayer3: rmLogicRouter RPC failed.", e);
        }

        return Futures.immediateFailedFuture(new IllegalArgumentException("rmLogicRouter RPC failed"));
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

        //TODO to remove all the existing route entries first.

        //add the complete the routing table.
        AddStaticRouteInputBuilder builder = new AddStaticRouteInputBuilder();
        builder.setFabricId(new FabricId(input.getVfabricId()));
        builder.setNodeId(input.getLneId());
        List<Route> rl = new ArrayList<>();
        for(Routingtable rt : input.getRoutingtable())
        {
            RouteBuilder rb = new RouteBuilder();
            rb.setDestinationPrefix(new Ipv4Prefix(rt.getDestIp().getIpv4Address().getValue()));
            rb.setNextHopOptions(rb.getNextHopOptions());
            rl.add(rb.build());
        }

        builder.setRoute(rl);
        this.fabServiceService.addStaticRoute(builder.build());
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

    public List<FabricId> getAllFabrics()
    {
        Future<RpcResult<GetAllFabricsOutput>> result = this.fabService.getAllFabrics();
        try {
            RpcResult<GetAllFabricsOutput> output = result.get();
            RpcResultBuilder.<GetAllFabricsOutput>success();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: rmLneLayer3: fabService.getAllFabrics rpc success");
                return output.getResult().getFabricId();
            } else {
                LOG.error("fabService.getAllFabrics RPC failed");
                return Collections.EMPTY_LIST;
            }
        } catch (Exception e) {
            LOG.error("ERROR: fabService.getAllFabrics RPC failed.", e);
        }

        return Collections.EMPTY_LIST;
    }

    public TpId createLogicalPortOnLsw(Uuid tenantId, NodeId vfabricId, NodeId lswId) {
        TpId tpId = null;
        CreateLogicalPortInputBuilder inputBuilder = new CreateLogicalPortInputBuilder();

        FabricId fabricId = new FabricId(vfabricId);
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setLogicalDevice(lswId);
        inputBuilder.setName("LswLogicalPort");

        Future<RpcResult<CreateLogicalPortOutput>> result = this.fabServiceService.createLogicalPort(inputBuilder.build());
        try {
            RpcResult<CreateLogicalPortOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLogicalPortOnLsw: createLogicPort RPC success");
                CreateLogicalPortOutput createLogicPortOutput = output.getResult();
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

    public void unregisterEndpoint(Uuid tenantId, NodeId vfabricId, NodeId lswId, Uuid epUuid) {

        UnregisterEndpointInputBuilder epInputBuilder = new UnregisterEndpointInputBuilder();
        FabricId fabricId = new FabricId(vfabricId);
        epInputBuilder.setFabricId(fabricId);
        List<Uuid> epUuidList = new ArrayList<>();
        epUuidList.add(new Uuid(epUuid));
        epInputBuilder.setIds(epUuidList);

        Future<RpcResult<Void>> result = this.epService.unregisterEndpoint(epInputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: unregisterEndpoint: unregisterEndpoint RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: unregisterEndpoint: unregisterEndpoint RPC failed.", e);
        }
    }

    public void createLrLswGateway(Uuid tenantId, NodeId vfabricId, NodeId lrId, NodeId lswId, IpAddress gatewayIpAddr,
            IpPrefix ipPrefix) {
        CreateGatewayInputBuilder inputBuilder = new CreateGatewayInputBuilder();
        FabricId fabricId = new FabricId(vfabricId);
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setLogicalRouter(new NodeId(lrId));
        inputBuilder.setLogicalSwitch(new NodeId(lswId));
        inputBuilder.setIpAddress(new IpAddress(gatewayIpAddr));
        inputBuilder.setNetwork(new IpPrefix(ipPrefix));

        Future<RpcResult<CreateGatewayOutput>> result = this.fabServiceService.createGateway(inputBuilder.build());
        try {
            RpcResult<CreateGatewayOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLrLswGateway: createGateway RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLrLswGateway: createGateway RPC failed.", e);
        }
    }

    public void removeLrLswGateway(Uuid tenantId, NodeId vfabricId, NodeId lrId, IpAddress gatewayIpAddr) {
        RmGatewayInputBuilder inputBuilder = new RmGatewayInputBuilder();
        FabricId fabricId = new FabricId(vfabricId);
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setLogicalRouter(new NodeId(lrId));
        inputBuilder.setIpAddress(new IpAddress(gatewayIpAddr));

        Future<RpcResult<Void>> result = this.fabServiceService.rmGateway(inputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: removeLrLswGateway: rmGateway RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: removeLrLswGateway: rmGateway RPC failed.", e);
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
        inputBuilder.setLogicalDevice(deviceId);

        LOG.debug("FABMGR: createAcl: fabricId={}, deviceId={}, aclName={}", fabricId.getValue(), deviceId.getValue(),
                aclName);
        Future<RpcResult<Void>> result = this.fabServiceService.addAcl(inputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createAcl: addAcl RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createAcl: addAcl RPC failed.", e);
        }
    }

    public void removeAcl(Uuid tenantId, NodeId vfabricId, NodeId nodeId, String aclName) {
        DelAclInputBuilder inputBuilder = new DelAclInputBuilder();
        FabricId fabricId = new FabricId(vfabricId);
        inputBuilder.setFabricId(fabricId);
        inputBuilder.setAclName(aclName);
        /*
         * NOTE: The NodeId must be new'd before passing to RPC. Otherwise,
         * addAcl() PRC return failure, because Fabric cannot find the logic
         * device in its Cache map using the nodeId as search key.
         */
        NodeId deviceId = new NodeId(nodeId.getValue());
        inputBuilder.setLogicalDevice(deviceId);

        LOG.debug("FABMGR: removeAcl: fabricId={}, deviceId={}, aclName={}", fabricId.getValue(), deviceId.getValue(),
                aclName);
        Future<RpcResult<Void>> result = this.fabServiceService.delAcl(inputBuilder.build());
        try {
            RpcResult<Void> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: removeAcl: delAcl RPC success");
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: removeAcl: delAcl RPC failed.", e);
        }
    }

    @Override
    public Future<RpcResult<Void>> removeChildNetNode(RemoveChildNetNodeInput input) {
        // TODO Auto-generated method stub
        return null;
    }
}
