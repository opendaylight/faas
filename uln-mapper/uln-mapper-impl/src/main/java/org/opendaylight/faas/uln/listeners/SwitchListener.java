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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchListener implements DataTreeChangeListener<LogicalSwitch>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchListener.class);

    private ListenerRegistration<?> registerListener;

    private final ScheduledExecutorService executor;
    private final UlnMappingEngine ulnMappingEngine;
    private final DataBroker dataBroker;

    public SwitchListener(ScheduledExecutorService executor, UlnMappingEngine ulnMappingEngine, DataBroker dataBroker) {
        this.executor = executor;
        this.ulnMappingEngine = ulnMappingEngine;
        this.dataBroker = dataBroker;
    }

    public void init() {
        registerListener = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.logicalSwitchIid()), this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<LogicalSwitch>> changes) {
        executor.execute(() -> executeEvent(changes));
    }

    public void executeEvent(Collection<DataTreeModification<LogicalSwitch>> changes) {
        for (DataTreeModification<LogicalSwitch> change: changes) {
            DataObjectModification<LogicalSwitch> rootNode = change.getRootNode();
            final LogicalSwitch originalSwitch = rootNode.getDataBefore();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final LogicalSwitch updatedSwitch = rootNode.getDataAfter();
                    if (originalSwitch == null) {
                        LOG.debug("FABMGR: Create Switch event: {}", updatedSwitch.getUuid().getValue());
                        ulnMappingEngine.handleLswCreateEvent(updatedSwitch);
                    } else {
                        LOG.debug("FABMGR: Update Switch event: {}", updatedSwitch.getUuid().getValue());
                        ulnMappingEngine.handleLswUpdateEvent(updatedSwitch);
                    }
                    break;
                case DELETE:
                    final LogicalSwitch deletedSwitch = rootNode.getDataBefore();
                    LOG.debug("FABMGR: Remove Switch event: {}", deletedSwitch.getUuid().getValue());
                    ulnMappingEngine.handleLswRemoveEvent(deletedSwitch);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void close() {
        if (registerListener != null) {
            registerListener.close();
        }
    }
}
