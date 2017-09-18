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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityRuleGroupsListener implements DataTreeChangeListener<SecurityRuleGroups>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityRuleGroupsListener.class);

    private ListenerRegistration<?> registerListener;

    private final ScheduledExecutorService executor;
    private final UlnMappingEngine ulnMappingEngine;
    private final DataBroker dataBroker;

    public SecurityRuleGroupsListener(ScheduledExecutorService executor, UlnMappingEngine ulnMappingEngine,
            DataBroker dataBroker) {
        this.executor = executor;
        this.ulnMappingEngine = ulnMappingEngine;
        this.dataBroker = dataBroker;
    }

    public void init() {
        registerListener = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.securityGroupsIid()), this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<SecurityRuleGroups>> changes) {
        executor.execute(() -> executeEvent(changes));
    }

    public void executeEvent(Collection<DataTreeModification<SecurityRuleGroups>> changes) {
        for (DataTreeModification<SecurityRuleGroups> change: changes) {
            DataObjectModification<SecurityRuleGroups> rootNode = change.getRootNode();
            final SecurityRuleGroups originalGroups = rootNode.getDataBefore();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final SecurityRuleGroups updatedGroups = rootNode.getDataAfter();
                    if (rootNode.getDataBefore() == null) {
                        LOG.debug("FABMGR: Create SecurityRuleGroups event: {}", updatedGroups.getUuid().getValue());
                        ulnMappingEngine.handleSecurityRuleGroupsCreateEvent(updatedGroups);
                    } else {
                        LOG.debug("FABMGR: Update SecurityRuleGroups event: {}", updatedGroups.getUuid().getValue());
                        updateRules(updatedGroups, originalGroups);
                    }
                    break;
                case DELETE:
                    LOG.debug("FABMGR: Remove SecurityRuleGroups event: {}", originalGroups.getUuid().getValue());
                    ulnMappingEngine.handleSecurityRuleGroupsRemoveEvent(originalGroups);
                    break;
                default:
                    break;
            }
        }
    }

    private void updateRules(SecurityRuleGroups newData, SecurityRuleGroups oldData) {
        LOG.debug("FABMGR: Update SecurityRuleGroups event: {}", newData.getUuid().getValue());
        ulnMappingEngine.handleSecurityRuleGroupsUpdateEvent(newData);
    }

    @Override
    public void close() {
        if (registerListener != null) {
            registerListener.close();
        }
    }
}
