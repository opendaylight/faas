/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.faas.fabric.general.spi.FabricRendererFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FabricFactory implements FabricRendererRegistry {

    private final Map<UnderlayerNetworkType, FabricRendererFactory> registeredFabricImpls = Maps.newHashMap();

    private final FabricRendererFactory defaultRendererFactory = new FabricRendererFactory() {

        @Override
        public FabricListener createListener(InstanceIdentifier<FabricNode> iid, FabricAttribute fabric) {
            return null;
        }

        @Override
        public FabricRenderer composeFabric(InstanceIdentifier<FabricNode> iid, FabricAttributeBuilder fabric,
                ComposeFabricInput input) {
            return null;
        }
    };


    public FabricFactory() {
    }

    @Override
    public void unregister(UnderlayerNetworkType fabricType) {
        registeredFabricImpls.remove(fabricType);
    }

    @Override
    public void register(UnderlayerNetworkType fabricType, FabricRendererFactory impl) {
        registeredFabricImpls.put(fabricType, impl);

    }

    @Override
    public FabricRendererFactory getFabricRendererFactory(UnderlayerNetworkType fabricType) {
        FabricRendererFactory rendererFactory = registeredFabricImpls.get(fabricType);
        return rendererFactory == null ? defaultRendererFactory : rendererFactory;
    }

    public static ExecutorService newExecutorService() {
        return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("fabric-factory-%d").build());
    }
}
