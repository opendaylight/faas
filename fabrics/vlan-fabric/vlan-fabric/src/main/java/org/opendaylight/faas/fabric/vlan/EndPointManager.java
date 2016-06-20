/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.Constants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class EndPointManager implements AutoCloseable, DataTreeChangeListener<Endpoint> {

    private static final Logger LOG = LoggerFactory.getLogger(EndPointManager.class);

    private final DataBroker databroker;

    private final RpcProviderRegistry rpcRegistry;

    private final FabricContext fabricCtx;

    private final ListenerRegistration<EndPointManager> epListener;

    private static FutureCallback<Void> simpleFutureMonitor = new FutureCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
            // do nothing
        }

        @Override
        public void onFailure(Throwable th) {
            LOG.error("Exception in onDataChanged", th);
        }

    };

    public EndPointManager(DataBroker databroker, RpcProviderRegistry rpcRegistry, FabricContext fabricCtx) {
        this.databroker = databroker;
        this.rpcRegistry = rpcRegistry;
        this.fabricCtx = fabricCtx;

        DataTreeIdentifier<Endpoint> dtid = new DataTreeIdentifier<Endpoint>(LogicalDatastoreType.OPERATIONAL,
                Constants.DOM_ENDPOINTS_PATH.child(Endpoint.class));
        epListener = databroker.registerDataTreeChangeListener(dtid, this);
    }

    private void removeEndPointIId(final InstanceIdentifier<Endpoint> epIId, Endpoint ep) {

    }

    private void rendererEndpoint(Endpoint ep) throws Exception {

    }

    @Override
    public void close() {
        epListener.close();
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Endpoint>> changes) {

        for (DataTreeModification<Endpoint> change : changes) {
            switch (change.getRootNode().getModificationType()) {
                case DELETE: {
                    final Endpoint ep = change.getRootNode().getDataBefore();
                    final InstanceIdentifier<Endpoint> iid = change.getRootPath().getRootIdentifier();
                    if (ep.getLocation() != null && fabricCtx.getFabricId().equals(ep.getOwnFabric())) {
                        Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                removeEndPointIId(iid, ep);
                                return null;
                            }
                        }), simpleFutureMonitor, fabricCtx.executor);
                    }
                    break;
                }
                case WRITE: {
                    final Endpoint ep = change.getRootNode().getDataAfter();
                    if (ep != null) {
                        if (ep.getLocation() != null && fabricCtx.getFabricId().equals(ep.getOwnFabric())) {
                            Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>() {

                                @Override
                                public Void call() throws Exception {
                                    rendererEndpoint(ep);
                                    return null;
                                }
                            }), simpleFutureMonitor, fabricCtx.executor);
                        }
                    }
                    break;
                }
                case SUBTREE_MODIFIED: {
                    // DO NOTHING
                    break;
                }
                default:
                    break;
            }
        }
    }
}