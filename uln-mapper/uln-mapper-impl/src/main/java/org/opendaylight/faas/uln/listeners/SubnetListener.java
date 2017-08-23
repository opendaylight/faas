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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubnetListener implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SubnetListener.class);

    private ListenerRegistration<DataChangeListener> registerListener;

    private final ScheduledExecutorService executor;
    private final UlnMappingEngine ulnMappingEngine;
    private final DataBroker dataBroker;

    public SubnetListener(ScheduledExecutorService executor, UlnMappingEngine ulnMappingEngine, DataBroker dataBroker) {
        this.executor = executor;
        this.ulnMappingEngine = ulnMappingEngine;
        this.dataBroker = dataBroker;
    }

    public void init() {
        registerListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.subnetIid(), this,
                AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        executor.execute(() -> executeEvent(change));
    }

    public void executeEvent(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // Create
        for (DataObject dao : change.getCreatedData().values()) {
            if (dao instanceof Subnet) {
                LOG.debug("FABMGR: Create Subnet event: {}", ((Subnet) dao).getUuid().getValue());
                ulnMappingEngine.handleSubnetCreateEvent((Subnet) dao);
            }
        }
        // Update
        Map<InstanceIdentifier<?>, DataObject> dao = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dao.entrySet()) {
            if (entry.getValue() instanceof Subnet) {
                LOG.debug("FABMGR: Update Subnet event: {}", ((Subnet) entry.getValue()).getUuid().getValue());
                ulnMappingEngine.handleSubnetUpdateEvent((Subnet) entry.getValue());
            }
        }
        // Remove
        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject old = change.getOriginalData().get(iid);
            if (old == null) {
                continue;
            }
            if (old instanceof Subnet) {
                LOG.debug("FABMGR: Remove Subnet event: {}", ((Subnet) old).getUuid().getValue());
                ulnMappingEngine.handleSubnetRemoveEvent((Subnet) old);
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
