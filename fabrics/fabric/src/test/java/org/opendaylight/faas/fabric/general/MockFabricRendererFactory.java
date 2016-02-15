/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.faas.fabric.general.spi.FabricRendererFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MockFabricRendererFactory implements FabricRendererFactory {

    @Override
    public FabricRenderer composeFabric(InstanceIdentifier<FabricNode> iid, FabricAttributeBuilder fabric,
            ComposeFabricInput input) {

        return new MockFabricRenderer(iid);
    }

    @Override
    public FabricListener createListener(InstanceIdentifier<FabricNode> iid, FabricAttribute fabric) {

        return new MockFabricListener();
    }

}
