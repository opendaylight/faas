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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointLocationListener implements DataTreeChangeListener<EndpointLocation>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointLocationListener.class);

    private ListenerRegistration<?> registerListener;

    private final ScheduledExecutorService executor;
    private final UlnMappingEngine ulnMappingEngine;
    private final DataBroker dataBroker;

    public EndpointLocationListener(ScheduledExecutorService executor, UlnMappingEngine ulnMappingEngine,
            DataBroker dataBroker) {
        this.executor = executor;
        this.ulnMappingEngine = ulnMappingEngine;
        this.dataBroker = dataBroker;
    }

    public void init() {
        registerListener = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.endpointLocationIid()), this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<EndpointLocation>> changes) {
        executor.execute(() -> executeEvent(changes));
    }

    public void executeEvent(Collection<DataTreeModification<EndpointLocation>> changes) {
        for (DataTreeModification<EndpointLocation> change: changes) {
            DataObjectModification<EndpointLocation> rootNode = change.getRootNode();
            final EndpointLocation originalEPL = rootNode.getDataBefore();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final EndpointLocation updatedEPL = rootNode.getDataAfter();
                    if (originalEPL == null) {
                        LOG.debug("FABMGR: Create EndpointLocation event: {}", updatedEPL.getUuid().getValue());
                        ulnMappingEngine.handleEndpointLocationCreateEvent(updatedEPL);
                    } else {
                        LOG.debug("FABMGR: Update EndpointLocation event: {}", updatedEPL.getUuid().getValue());
                        ulnMappingEngine.handleEndpointLocationUpdateEvent(updatedEPL);
                    }
                    break;
                case DELETE:
                    LOG.debug("FABMGR: Remove EndpointLocation event: {}", originalEPL.getUuid().getValue());
                    ulnMappingEngine.handleEndpointLocationRemoveEvent(originalEPL);
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
