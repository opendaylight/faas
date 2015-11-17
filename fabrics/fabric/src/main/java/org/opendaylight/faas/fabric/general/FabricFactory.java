/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;

public class FabricFactory implements AutoCloseable, FabricRendererRegistry {

    private FabricManagementAPIProvider manageAPI;

    private FabricServiceAPIProvider serviceAPI;

    private EndPointRegister epRegister;

    private final ExecutorService executor;

    private final Map<UnderlayerNetworkType, FabricRenderer> registeredFabricImpls = new HashMap<UnderlayerNetworkType, FabricRenderer>();

    private FabricRenderer defaultRenderer = new DummyFabricRenderer();


    public FabricFactory (final DataBroker dataProvider,
            final RpcProviderRegistry rpcRegistry,
            final NotificationProviderService notificationService) {

        executor = Executors.newFixedThreadPool(1);

        manageAPI = new FabricManagementAPIProvider(dataProvider, rpcRegistry, executor, this);
        manageAPI.start();

        serviceAPI = new FabricServiceAPIProvider(dataProvider, rpcRegistry, executor);
        serviceAPI.start();

        epRegister = new EndPointRegister(dataProvider, rpcRegistry, executor);
        epRegister.start();
    }

    @Override
    public void close() throws Exception {
        manageAPI.close();
        serviceAPI.close();
        epRegister.close();
        executor.shutdown();
    }

    @Override
    public void unregister(UnderlayerNetworkType fabricType) {
        registeredFabricImpls.remove(fabricType);
    }

    @Override
    public void register(UnderlayerNetworkType fabricType, FabricRenderer impl) {
        registeredFabricImpls.put(fabricType, impl);

    }

    @Override
    public FabricRenderer getFabricRenderer(UnderlayerNetworkType fabricType) {
    	FabricRenderer renderer = registeredFabricImpls.get(fabricType);
        return renderer == null ? defaultRenderer : renderer;
    }

}
