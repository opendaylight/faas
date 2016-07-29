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
import org.opendaylight.faas.uln.listeners.SecurityRuleGroupsListener;
import org.opendaylight.faas.uln.listeners.SubnetListener;
import org.opendaylight.faas.uln.listeners.SwitchListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.FaasEndpointsLocationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserLogicalNetworkManager - A singleton object responsible for initiating the mapping engine.
 *
 */
public final class UserLogicalNetworkManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkManager.class);

    private final ScheduledExecutorService executor;

    private final RouterListener routerListener;
    private final EdgeListener edgeListener;
    private final EndpointLocationListener endpointLocationListener;
    private final PortListener portListener;
    private final SecurityRuleGroupsListener securityGroupListener;
    private final SubnetListener subnetListener;
    private final SwitchListener switchListener;

    private final UlnMappingEngine ulnMapper;
    private static UserLogicalNetworkManager instance = null;

    private UserLogicalNetworkManager() {
        LOG.debug("UserLogicalNetworkManager: Starting ulnmapper...");

        UlnMapperDatastoreDependency.getRpcRegistry().addRpcImplementation(
                FaasEndpointsLocationsService.class,
                new FaasServiceImpl());

        int numCPU = Runtime.getRuntime().availableProcessors();
        executor = Executors.newScheduledThreadPool(numCPU * 2);

        LOG.info("Start logical element listerners on logical network ...");

        ulnMapper = new UlnMappingEngine();
        routerListener = new RouterListener(executor);
        edgeListener = new EdgeListener(executor);
        endpointLocationListener = new EndpointLocationListener(executor);
        portListener = new PortListener(executor);
        securityGroupListener = new SecurityRuleGroupsListener(executor);
        subnetListener = new SubnetListener(executor);
        switchListener = new SwitchListener(executor);

        ulnMapper.initialize();
        LOG.info("Logical Network Manager: Uln-mapper has Started. threadpool size={}", numCPU * 2);
    }

    /**
     * Singleton function.
     * @return
     */
    public static synchronized UserLogicalNetworkManager getInstance() {
        if (instance == null) {
            instance = new UserLogicalNetworkManager();
        }

        return instance;
    }

    public static UlnMappingEngine getUlnMapper() {
        return instance.ulnMapper;
    }

    @Override
    public void close() throws Exception {

        LOG.debug("Shutting down thread pool and all the listeners ...");

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
