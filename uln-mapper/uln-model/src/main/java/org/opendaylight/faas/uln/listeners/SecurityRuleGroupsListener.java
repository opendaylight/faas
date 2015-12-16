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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityRuleGroupsListener implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityRuleGroupsListener.class);

    private final ListenerRegistration<DataChangeListener> registerListener;

    private final ScheduledExecutorService executor;

    public SecurityRuleGroupsListener(ScheduledExecutorService executor) {
        this.executor = executor;
        this.registerListener = UlnMapperDatastoreDependency.getDataProvider().registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.securityGroupsIid(), this,
                AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                executeEvent(change);
            }
        });
    }

    public void executeEvent(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // Create
        for (DataObject dao : change.getCreatedData().values()) {
            if (dao instanceof SecurityRuleGroups) {
                LOG.debug("FABMGR: Created SecurityRuleGroups {}", dao);
                UserLogicalNetworkManager.getUlnMapper().handleSecurityRuleGroupsCreateEvent((SecurityRuleGroups) dao);
            }
        }
        // Update
        Map<InstanceIdentifier<?>, DataObject> dao = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dao.entrySet()) {
            if (entry.getValue() instanceof SecurityRuleGroups) {
                InstanceIdentifier<?> iid = entry.getKey();
                DataObject old = change.getOriginalData().get(iid);
                updateRules((SecurityRuleGroups) entry.getValue(), (SecurityRuleGroups) old);
            }
        }
        // Remove
        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject old = change.getOriginalData().get(iid);
            if (old == null) {
                continue;
            }
            if (old instanceof SecurityRuleGroups) {
                LOG.debug("FABMGR: Removed SecurityRuleGroups {}", old);
            }
        }
    }

    private void updateRules(SecurityRuleGroups newData, SecurityRuleGroups oldData) {
        LOG.debug("FABMGR: Updated SecurityRuleGroups {}", newData);
        UserLogicalNetworkManager.getUlnMapper().handleSecurityRuleGroupsUpdateEvent(newData);
    }

    @Override
    public void close() throws Exception {
        if (registerListener != null)
            registerListener.close();
    }
}
