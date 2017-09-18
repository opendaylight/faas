/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.listeners;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.uln.datastore.api.UlnIidFactory;
import org.opendaylight.faas.uln.manager.UlnMappingEngine;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterListener implements DataTreeChangeListener<LogicalRouter>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RouterListener.class);

    private ListenerRegistration<?> registerListener;

    private final ScheduledExecutorService executor;
    private final UlnMappingEngine ulnMappingEngine;
    private final DataBroker dataBroker;

    public RouterListener(ScheduledExecutorService executor, UlnMappingEngine ulnMappingEngine, DataBroker dataBroker) {
        this.executor = executor;
        this.ulnMappingEngine = ulnMappingEngine;
        this.dataBroker = dataBroker;
    }

    public void init() {
        registerListener = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.logicalRouterIid()), this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<LogicalRouter>> changes) {
        executor.execute(() -> executeEvent(changes));
    }

    public void executeEvent(Collection<DataTreeModification<LogicalRouter>> changes) {
        for (DataTreeModification<LogicalRouter> change: changes) {
            DataObjectModification<LogicalRouter> rootNode = change.getRootNode();
            final LogicalRouter originalRouter = rootNode.getDataBefore();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final LogicalRouter updatedRouter = rootNode.getDataAfter();
                    if (originalRouter == null) {
                        LOG.debug("FABMGR: Create Logical Router event: {}", updatedRouter.getUuid().getValue());
                        ulnMappingEngine.handleLrCreateEvent(updatedRouter);
                    } else {
                        LOG.debug("FABMGR: Logical Router Switch event: {}", updatedRouter.getUuid().getValue());
                        ulnMappingEngine.handleLrUpdateEvent(updatedRouter);
                    }
                    break;
                case DELETE:
                    LOG.debug("FABMGR: Remove Logical Router event: {}", originalRouter.getUuid().getValue());
                    ulnMappingEngine.handleLrRemoveEvent(originalRouter);
                    break;
                default:
                    break;
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
