/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.listeners;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.uln.datastore.api.UlnIidFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointLocationListener implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointLocationListener.class);

    private final ListenerRegistration<DataChangeListener> registerListener;

    private final ScheduledExecutorService executor;

    public EndpointLocationListener(DataBroker dataProvider, ScheduledExecutorService executor) {
        this.executor = executor;
        this.registerListener = checkNotNull(dataProvider).registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, UlnIidFactory.endpointLocationIid(), this,
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
            if (dao instanceof EndpointLocation) {
                LOG.debug("Created EndpointLocation {}", (EndpointLocation) dao);
            }
        }
        // Update
        Map<InstanceIdentifier<?>, DataObject> dao = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dao.entrySet()) {
            if (entry.getValue() instanceof EndpointLocation) {
                LOG.debug("Updated EndpointLocation {}", (EndpointLocation) dao);
            }
        }
        // Remove
        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject old = change.getOriginalData().get(iid);
            if (old == null) {
                continue;
            }
            if (old instanceof EndpointLocation) {
                LOG.debug("Removed EndpointLocation {}", (EndpointLocation) old);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (registerListener != null)
            registerListener.close();
    }
}
