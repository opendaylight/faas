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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserLogicalNetworkManager - A singleton object responsible for initiating the mapping engine.
 *
 */
public final class UserLogicalNetworkManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkManager.class);

    private final ScheduledExecutorService executor;

    public UserLogicalNetworkManager() {
        int numCPU = Runtime.getRuntime().availableProcessors();
        executor = Executors.newScheduledThreadPool(numCPU * 2);

        LOG.info("Logical Network Manager: Uln-mapper has Started. threadpool size={}", numCPU * 2);
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void close() {
        LOG.debug("Shutting down thread pool...");

        executor.shutdownNow();
    }
}
