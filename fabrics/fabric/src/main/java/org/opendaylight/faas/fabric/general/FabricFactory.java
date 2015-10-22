/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

public class FabricFactory implements AutoCloseable {

    private FabricManagementApi manageAPI;

    private EndPointRegister epRegister;

    private final ExecutorService executor;

    public FabricFactory (final DataBroker dataProvider,
            final RpcProviderRegistry rpcRegistry,
            final NotificationProviderService notificationService) {

        executor = Executors.newFixedThreadPool(1);

        manageAPI = new FabricManagementApi(dataProvider, rpcRegistry, executor);
        manageAPI.start();

        epRegister = new EndPointRegister(dataProvider, rpcRegistry, executor);
        epRegister.start();
    }

    @Override
    public void close() throws Exception {
        manageAPI.close();
        epRegister.close();
        executor.shutdown();
    }
}