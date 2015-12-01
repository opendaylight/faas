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

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.VcLneRef;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.FabricServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;

public class VcNetNodeServiceApiProvider implements AutoCloseable, VcNetNodeService {

    private static final Logger LOG = LoggerFactory.getLogger(VcNetNodeServiceApiProvider.class);
    private static final LogicalDatastoreType OPERATIONAL = LogicalDatastoreType.OPERATIONAL;

    private RpcRegistration<VcNetNodeService> rpcRegistration;
    private final ExecutorService threadPool;
    private FabricService fabService;
    private FabricServiceService fabServiceService;

    public VcNetNodeServiceApiProvider(ExecutorService executor) {
        this.threadPool = executor;
    }

    public void initialize() {
        this.rpcRegistration =
                FabMgrDatastoreDependency.getRpcRegistry().addRpcImplementation(VcNetNodeService.class, this);
        this.fabService = FabMgrDatastoreDependency.getRpcRegistry().getRpcService(FabricService.class);
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
        NodeId vfabricId = input.getVfabricId();
        String lswName = input.getName();

        CreateLogicSwitchInputBuilder lswInputBuilder = new CreateLogicSwitchInputBuilder();

        FabricId fabricId = new FabricId(vfabricId);
        lswInputBuilder.setFabricId(fabricId);
        lswInputBuilder.setName(lswName);
        Integer vni = new Integer(100);
        lswInputBuilder.setVni(vni);

        RpcResult<CreateLneLayer2Output> value;
        Future<RpcResult<CreateLogicSwitchOutput>> result = this.fabServiceService.createLogicSwitch(lswInputBuilder.build());
        try {
            RpcResult<CreateLogicSwitchOutput> output = result.get();
            if (output.isSuccessful()) {
                LOG.debug("FABMGR: createLneLayer2: createLogicSwitch RPC success");
                CreateLneLayer2OutputBuilder builder = new CreateLneLayer2OutputBuilder();
                CreateLogicSwitchOutput createLswOutput = output.getResult();
                NodeId nodeId = createLswOutput.getNodeRef();
                VcLneRef lswRef = new VcLneRef(FabMgrYangDataUtil.createNodePath(fabricId.toString(), nodeId));
                builder.setLneRef(lswRef);
            }
        } catch (Exception e) {
            LOG.error("FABMGR: ERROR: createLneLayer2: createLogicSwitch RPC failed: {}", e);
        }

        return Futures.immediateFailedFuture(new IllegalArgumentException("createLogicSwitch RPC failed"));
    }

    @Override
    public Future<RpcResult<Void>> addPortsToLneLayer2(AddPortsToLneLayer2Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<CreateLneLayer3Output>> createLneLayer3(CreateLneLayer3Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addPortsToLneLayer3(AddPortsToLneLayer3Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<CreateLneLayer1Output>> createLneLayer1(CreateLneLayer1Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<CreateChildNetNodeOutput>> createChildNetNode(CreateChildNetNodeInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addVfabricToNetNode(AddVfabricToNetNodeInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> removedChildNetNode(RemovedChildNetNodeInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateNetNodeLogicalPort(UpdateNetNodeLogicalPortInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmPortsFromLneLayer2(RmPortsFromLneLayer2Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmPortsFromLneLayer3(RmPortsFromLneLayer3Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmVfabricFromNetNode(RmVfabricFromNetNodeInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLneLayer2(RmLneLayer2Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLneLayer3(RmLneLayer3Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmNetNodeLogicalPort(RmNetNodeLogicalPortInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmLneLayer1(RmLneLayer1Input input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateLneLayer3Routingtable(UpdateLneLayer3RoutingtableInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> addApplianceToNetNode(AddApplianceToNetNodeInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> rmApplianceFromNetNode(RmApplianceFromNetNodeInput input) {
        // TODO Auto-generated method stub
        return null;
    }

}
