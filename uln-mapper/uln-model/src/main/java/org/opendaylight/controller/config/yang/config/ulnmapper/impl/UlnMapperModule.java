/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.ulnmapper.impl;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.faas.uln.manager.UlnMapperDatastoreDependency;
import org.opendaylight.faas.uln.manager.UserLogicalNetworkManager;

/**
 *
 * <p> Module entry point for user logical network mapping module.</p>
 */
public class UlnMapperModule extends AbstractUlnMapperModule {

    /**
     * Constructor.
     * @param identifier - identifier
     * @param dependencyResolver - dependencyResolver
     */
    public UlnMapperModule(ModuleIdentifier identifier, DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    /**
     * Constructor with module parameters.
     * @param identifier - identifier
     * @param dependencyResolver - dependencyResolver
     * @param oldModule - oldModule
     * @param oldInstance - oldInstance
     */
    public UlnMapperModule(ModuleIdentifier identifier, DependencyResolver dependencyResolver,
            UlnMapperModule oldModule, java.lang.AutoCloseable oldInstance) {
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

        return UserLogicalNetworkManager.getInstance();
    }

}
