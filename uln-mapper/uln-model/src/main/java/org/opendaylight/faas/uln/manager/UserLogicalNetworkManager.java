/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.uln.datastore.api.UlnDatastoreApi;
import org.opendaylight.faas.uln.listeners.RouterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLogicalNetworkManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkManager.class);

    private final ScheduledExecutorService executor;
    private final RouterListener logicalRouterListener;

    public UserLogicalNetworkManager(final DataBroker dataProvider, RpcProviderRegistry rpcRegistry, NotificationService notificationService) {
        int numCPU = Runtime.getRuntime().availableProcessors();
        executor = Executors.newScheduledThreadPool(numCPU * 2);
        UlnDatastoreApi.setDataBroker(dataProvider);
        logicalRouterListener = new RouterListener(dataProvider, executor);
        LOG.info("FAAS Renderer has Started");
    }

    @Override
    public void close() throws Exception {
        executor.shutdownNow();
        logicalRouterListener.close();
    }
}
