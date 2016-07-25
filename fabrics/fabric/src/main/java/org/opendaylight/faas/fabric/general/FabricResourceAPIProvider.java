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

import java.util.UUID;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.resources.rev160530.AddFabricLinkInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.resources.rev160530.AddFabricLinkOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.resources.rev160530.AddFabricLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.resources.rev160530.CreateFabricPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.resources.rev160530.FabricResourcesService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.resources.rev160530.SetFabricPortRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.termination.point.FportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.termination.point.FportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricResourceAPIProvider implements AutoCloseable, FabricResourcesService {

    private static final Logger LOG = LoggerFactory.getLogger(FabricResourceAPIProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private RpcRegistration<FabricResourcesService> rpcRegistration;


    public FabricResourceAPIProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {

        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
    }

    public void start() {
        rpcRegistration = rpcRegistry.addRpcImplementation(FabricResourcesService.class, this);
    }

    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
    }

    @Override
    public Future<RpcResult<AddFabricLinkOutput>> addFabricLink(AddFabricLinkInput input) {

        FabricId fabric1 = input.getSourceFabric();
        FabricId fabric2 = input.getDestFabric();
        TpId tp1 = input.getSourceFabricPort();
        TpId tp2 = input.getDestFabricPort();

        final LinkId lnkId = new LinkId(UUID.randomUUID().toString());

        InstanceIdentifier<Link> path = MdSalUtils.createInterFabricLinkIId(lnkId);
        Link data = new LinkBuilder()
                .setSource(
                        new SourceBuilder().setSourceNode(fabric1).setSourceTp(tp1).build())
                .setDestination(
                        new DestinationBuilder().setDestNode(fabric2).setDestTp(tp2).build())
                .setLinkId(lnkId)
                .build();

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.CONFIGURATION, path, data);
        wt.put(LogicalDatastoreType.OPERATIONAL, path, data);

        CheckedFuture<Void,TransactionCommitFailedException> future = wt.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<AddFabricLinkOutput>>() {

            @Override
            public ListenableFuture<RpcResult<AddFabricLinkOutput>> apply(Void submitResult) throws Exception {
                RpcResultBuilder<AddFabricLinkOutput> resultBuilder = RpcResultBuilder.<AddFabricLinkOutput>success();
                AddFabricLinkOutput output = new AddFabricLinkOutputBuilder().setLinkId(lnkId).build();
                return Futures.immediateFuture(resultBuilder.withResult(output).build());
            }
        });
    }

    @Override
    public Future<RpcResult<Void>> createFabricPort(CreateFabricPortInput input) {
        return Futures.immediateFailedFuture(
                new RuntimeException("Not implemet yet!"));
    }

    @Override
    public Future<RpcResult<Void>> setFabricPortRole(SetFabricPortRoleInput input) {

        InstanceIdentifier<FportAttribute> iid = MdSalUtils.createFabricPortIId(input.getFabricId(), input.getFabricPortId())
                .augmentation(FabricPortAugment.class).child(FportAttribute.class);

        FportAttributeBuilder builder = new FportAttributeBuilder();
        builder.setRole(input.getPortRole());

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());

        CheckedFuture<Void,TransactionCommitFailedException> future = wt.submit();

        return Futures.transform(future, new AsyncFunction<Void, RpcResult<Void>>() {

            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                RpcResultBuilder<Void> resultBuilder = RpcResultBuilder.<Void>success();
                return Futures.immediateFuture(resultBuilder.build());
            }
        });
    }
}
