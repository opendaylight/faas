/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;


import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.FabricEndpointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.LocateEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.UnregisterEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.LocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.EndpointBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.EndpointKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;


public class EndPointRegister implements FabricEndpointService, AutoCloseable {

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private RpcRegistration<FabricEndpointService> rpcRegistration;

    private final ExecutorService executor;

    public EndPointRegister(final DataBroker dataBroker,
            final RpcProviderRegistry rpcRegistry, ExecutorService executor) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;

        this.executor = executor;
    }

    @Override
    public Future<RpcResult<Void>> unregisterEndpoint(UnregisterEndpointInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        if ( input == null) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException("Endpoint can not be empty!"));
        }
        final List<Uuid> toBeDeletedList = input.getIds();

        if ( toBeDeletedList == null || toBeDeletedList.isEmpty()) {
            return Futures.immediateFuture(result);
        }

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        for (Uuid ep : toBeDeletedList) {
            InstanceIdentifier<Endpoint> eppath = Constants.DOM_ENDPOINTS_PATH
                    .child(Endpoint.class, new EndpointKey(ep));
            trans.delete(LogicalDatastoreType.OPERATIONAL, eppath);
        }
        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transformAsync(future, new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void input) throws Exception {
                return Futures.immediateFuture(result);
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<RegisterEndpointOutput>> registerEndpoint(RegisterEndpointInput input) {

        final RpcResultBuilder<RegisterEndpointOutput> resultBuilder =
                RpcResultBuilder.<RegisterEndpointOutput>success();
        final RegisterEndpointOutputBuilder outputBuilder = new RegisterEndpointOutputBuilder();

        final FabricId fabricid = input.getFabricId();
        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        Uuid epId = input.getEndpointUuid();
        if (epId == null) {
            epId = new Uuid(UUID.randomUUID().toString());
        }
        final Uuid newepId = epId;


        final InstanceIdentifier<Endpoint> eppath = Constants.DOM_ENDPOINTS_PATH
                .child(Endpoint.class, new EndpointKey(newepId));

        EndpointBuilder epBuilder = new EndpointBuilder();
        epBuilder.setEndpointUuid(newepId);
        epBuilder.setGateway(input.getGateway());
        epBuilder.setIpAddress(input.getIpAddress());
        epBuilder.setLocation(input.getLocation());
        epBuilder.setLogicalLocation(input.getLogicalLocation());
        epBuilder.setMacAddress(input.getMacAddress());
        epBuilder.setPublicIp(input.getPublicIp());
        epBuilder.setOwnFabric(fabricid);

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();
        trans.put(LogicalDatastoreType.OPERATIONAL, eppath, epBuilder.build(), true);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transformAsync(future, new AsyncFunction<Void, RpcResult<RegisterEndpointOutput>>() {

            @Override
            public ListenableFuture<RpcResult<RegisterEndpointOutput>> apply(Void input) throws Exception {
                outputBuilder.setEndpointId(newepId);
                return Futures.immediateFuture(resultBuilder.withResult(outputBuilder.build()).build());
            }
        }, executor);
    }

    @Override
    public Future<RpcResult<Void>> locateEndpoint(LocateEndpointInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        if ( input == null) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException("endpoint can not be empty!"));
        }
        final Uuid epId = input.getEndpointId();

        if ( epId == null ) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException("endpoint can not be empty!"));
        }
        final FabricId fabricid = input.getFabricId();
        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricid);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        EndpointBuilder epBuilder = new EndpointBuilder();
        LocationBuilder locBuilder = new LocationBuilder(input.getLocation());
        epBuilder.setEndpointUuid(epId);
        epBuilder.setLocation(locBuilder.build());

        final InstanceIdentifier<Endpoint> eppath = Constants.DOM_ENDPOINTS_PATH
                .child(Endpoint.class, new EndpointKey(epId));
        trans.merge(LogicalDatastoreType.OPERATIONAL, eppath, epBuilder.build());

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transformAsync(future, new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void input) throws Exception {
                return Futures.immediateFuture(result);
            }
        }, executor);
    }

    public void start() {
        rpcRegistration = rpcRegistry.addRpcImplementation(FabricEndpointService.class, this);
    }

    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
    }
}
