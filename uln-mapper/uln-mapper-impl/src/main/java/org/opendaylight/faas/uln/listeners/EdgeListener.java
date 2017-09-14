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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeListener implements DataTreeChangeListener<Edge>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EdgeListener.class);

    private ListenerRegistration<?> registerListener;

    private final ScheduledExecutorService executor;
    private final UlnMappingEngine ulnMappingEngine;
    private final DataBroker dataBroker;

    public EdgeListener(ScheduledExecutorService executor, UlnMappingEngine ulnMappingEngine, DataBroker dataBroker) {
        this.executor = executor;
        this.ulnMappingEngine = ulnMappingEngine;
        this.dataBroker = dataBroker;
    }

    public void init() {
        registerListener = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.edgeIid()), this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Edge>> changes) {
        executor.execute(() -> executeEvent(changes));
    }

    public void executeEvent(Collection<DataTreeModification<Edge>> changes) {
        for (DataTreeModification<Edge> change: changes) {
            DataObjectModification<Edge> rootNode = change.getRootNode();
            final Edge originalEdge = rootNode.getDataBefore();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final Edge updatedEdge = rootNode.getDataAfter();
                    if (originalEdge == null) {
                        LOG.debug("FABMGR: Create Edge event: {}", updatedEdge.getUuid().getValue());
                        ulnMappingEngine.handleEdgeCreateEvent(updatedEdge);
                    } else {
                        LOG.debug("FABMGR: Update Edge event: {}", updatedEdge.getUuid().getValue());
                        ulnMappingEngine.handleEdgeUpdateEvent(updatedEdge);
                    }
                    break;
                case DELETE:
                    LOG.debug("FABMGR: Remove Edge event: {}", originalEdge.getUuid().getValue());
                    ulnMappingEngine.handleEdgeRemoveEvent(originalEdge);
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
