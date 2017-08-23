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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.uln.datastore.api.UlnIidFactory;
import org.opendaylight.faas.uln.manager.UlnMappingEngine;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterListener implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RouterListener.class);

    private ListenerRegistration<DataChangeListener> registerListener;

    private final ScheduledExecutorService executor;
    private final UlnMappingEngine ulnMappingEngine;
    private final DataBroker dataBroker;

    public RouterListener(ScheduledExecutorService executor, UlnMappingEngine ulnMappingEngine, DataBroker dataBroker) {
        this.executor = executor;
        this.ulnMappingEngine = ulnMappingEngine;
        this.dataBroker = dataBroker;
    }

    public void init() {
        registerListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.logicalRouterIid(), this,
                AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        executor.execute(() -> executeEvent(change));
    }

    public void executeEvent(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // Create
        for (DataObject dao : change.getCreatedData().values()) {
            if (dao instanceof LogicalRouter) {
                LOG.debug("FABMGR: Create Logical Router event: {}", ((LogicalRouter) dao).getUuid().getValue());
                ulnMappingEngine.handleLrCreateEvent((LogicalRouter) dao);
            }
        }
        // Update
        Map<InstanceIdentifier<?>, DataObject> dao = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dao.entrySet()) {
            if (entry.getValue() instanceof LogicalRouter) {
                LOG.debug("FABMGR: Update Logical Router event: {}",
                        ((LogicalRouter) entry.getValue()).getUuid().getValue());
                ulnMappingEngine.handleLrUpdateEvent((LogicalRouter) entry.getValue());
            }
        }
        // Remove
        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject old = change.getOriginalData().get(iid);
            if (old == null) {
                continue;
            }
            if (old instanceof LogicalRouter) {
                LOG.debug("FABMGR: Remove Logical Router event: {}", ((LogicalRouter) old).getUuid().getValue());
                ulnMappingEngine.handleLrRemoveEvent((LogicalRouter) old);
            }
        }
    }

    @Override
    public void close()  {
        if (registerListener != null) {
            registerListener.close();
        }
    }
}
