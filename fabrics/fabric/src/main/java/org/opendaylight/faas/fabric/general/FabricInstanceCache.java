/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;

public enum FabricInstanceCache {
    INSTANCE;

    private Map<FabricId, FabricInstance> cache = new HashMap<FabricId, FabricInstance>();

    FabricInstance addFabric(FabricId fabricId, UnderlayerNetworkType type, FabricRenderer renderer) {
        FabricInstance newInstance = new FabricInstance(fabricId, type, renderer);
        cache.put(fabricId, newInstance);
        return newInstance;
    }

    void removeFabric(FabricId fabricId) {
        cache.remove(fabricId);
    }

    FabricInstance retrieveFabric(FabricId fabricId) {
        return cache.get(fabricId);
    }

    Optional<UnderlayerNetworkType> getFabricType(FabricId fabricId) {
        Optional<FabricInstance> instance = Optional.fromNullable(cache.get(fabricId));
        if (instance.isPresent()) {
            return Optional.of(instance.get().getType());
        } else {
            return Optional.absent();
        }
    }

}
