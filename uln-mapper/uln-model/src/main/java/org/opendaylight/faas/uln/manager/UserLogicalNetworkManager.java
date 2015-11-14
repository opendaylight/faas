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

import org.opendaylight.faas.uln.listeners.EdgeListener;
import org.opendaylight.faas.uln.listeners.EndpointLocationListener;
import org.opendaylight.faas.uln.listeners.PortListener;
import org.opendaylight.faas.uln.listeners.RouterListener;
import org.opendaylight.faas.uln.listeners.SecurityGroupListener;
import org.opendaylight.faas.uln.listeners.SubnetListener;
import org.opendaylight.faas.uln.listeners.SwitchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLogicalNetworkManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkManager.class);

    private final ScheduledExecutorService executor;
    private final RouterListener routerListener;
    private final EdgeListener edgeListener;
    private final EndpointLocationListener endpointLocationListener;
    private final PortListener portListener;
    private final SecurityGroupListener securityGroupListener;
    private final SubnetListener subnetListener;
    private final SwitchListener switchListener;

    public UserLogicalNetworkManager() {
        LOG.debug("Starting Uln-mapper...");
        int numCPU = Runtime.getRuntime().availableProcessors();
        executor = Executors.newScheduledThreadPool(numCPU * 2);
        routerListener = new RouterListener(executor);
        edgeListener = new EdgeListener(executor);
        endpointLocationListener = new EndpointLocationListener(executor);
        portListener = new PortListener(executor);
        securityGroupListener = new SecurityGroupListener(executor);
        subnetListener = new SubnetListener(executor);
        switchListener = new SwitchListener(executor);
        LOG.info("Uln-mapper has Started");
    }

    @Override
    public void close() throws Exception {
        executor.shutdownNow();
        routerListener.close();
        edgeListener.close();
        endpointLocationListener.close();
        portListener.close();
        securityGroupListener.close();
        subnetListener.close();
        switchListener.close();
    }
}
