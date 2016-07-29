/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.fabric_mgr.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabricmgr.FabricMgrProvider;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.controller.config.yang.config.fabric_mgr.impl.AbstractFabricMgrModule;
import org.opendaylight.controller.config.api.DependencyResolver;

public class FabricMgrModule extends AbstractFabricMgrModule {
    public FabricMgrModule(ModuleIdentifier identifier, DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public FabricMgrModule(ModuleIdentifier identifier, DependencyResolver dependencyResolver, FabricMgrModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        DataBroker databroker = getDataBrokerDependency();
        NotificationService notificationService = getNotificationAdapterDependency();
        RpcProviderRegistry rpcRegistry = getRpcRegistryDependency();

        return FabricMgrProvider.createInstance(databroker, rpcRegistry, notificationService);
    }

}
