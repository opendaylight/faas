/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.listeners;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.uln.datastore.api.UlnIidFactory;
import org.opendaylight.faas.uln.manager.UlnMapperDatastoreDependency;
import org.opendaylight.faas.uln.manager.UserLogicalNetworkManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeListener implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EdgeListener.class);

    private final ListenerRegistration<DataChangeListener> registerListener;

    private final ScheduledExecutorService executor;

    public EdgeListener(ScheduledExecutorService executor) {
        this.executor = executor;
        this.registerListener = UlnMapperDatastoreDependency.getDataProvider().registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.edgeIid(), this,
                AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        executor.execute(new Runnable() {

            public void run() {
                executeEvent(change);
            }
        });
    }

    public void executeEvent(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // Create
        for (DataObject dao : change.getCreatedData().values()) {
            if (dao instanceof Edge) {
                LOG.debug("FABMGR: Create Edge event: {}", ((Edge) dao).getUuid().getValue());
                UserLogicalNetworkManager.getUlnMapper().handleEdgeCreateEvent((Edge) dao);
            }
        }
        // Update
        Map<InstanceIdentifier<?>, DataObject> dao = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dao.entrySet()) {
            if (entry.getValue() instanceof Edge) {
                LOG.debug("FABMGR: Update Edge event: {}", ((Edge) entry.getValue()).getUuid().getValue());
                UserLogicalNetworkManager.getUlnMapper().handleEdgeUpdateEvent((Edge) entry.getValue());
            }
        }
        // Remove
        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject old = change.getOriginalData().get(iid);
            if (old == null) {
                continue;
            }
            if (old instanceof Edge) {
                LOG.debug("FABMGR: Remove Edge event: {}", ((Edge) old).getUuid().getValue());
                UserLogicalNetworkManager.getUlnMapper().handleEdgeRemoveEvent((Edge) old);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (registerListener != null)
            registerListener.close();
    }
}
