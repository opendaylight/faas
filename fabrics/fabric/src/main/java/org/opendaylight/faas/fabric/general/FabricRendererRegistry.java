/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;

public interface FabricRendererRegistry {

    void register(UnderlayerNetworkType fabricType, FabricRenderer impl);

    void unregister(UnderlayerNetworkType fabricType);

    FabricRenderer getFabricRenderer(UnderlayerNetworkType fabricType);
}