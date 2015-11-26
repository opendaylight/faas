/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import org.opendaylight.faas.fabric.general.spi.FabricRendererFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;

public interface FabricRendererRegistry {

    void unregister(UnderlayerNetworkType fabricType);

    void register(UnderlayerNetworkType fabricType, FabricRendererFactory impl);

    FabricRendererFactory getFabricRendererFactory(UnderlayerNetworkType fabricType);
}