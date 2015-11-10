package org.opendaylight.controller.config.yang.config.uln_mapper.impl;

import org.opendaylight.faas.uln.manager.UlnMapperDatastoreDependency;
/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.faas.uln.manager.UserLogicalNetworkManager;

public class UlnMapperModule extends org.opendaylight.controller.config.yang.config.uln_mapper.impl.AbstractUlnMapperModule {
    public UlnMapperModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public UlnMapperModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            org.opendaylight.controller.config.yang.config.uln_mapper.impl.UlnMapperModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        UlnMapperDatastoreDependency.setDataProvider(getDataBrokerDependency());
        UlnMapperDatastoreDependency.setRpcRegistry(getRpcRegistryDependency());
        UlnMapperDatastoreDependency.setNotificationService(getNotificationAdapterDependency());
        return new UserLogicalNetworkManager();
    }

}
