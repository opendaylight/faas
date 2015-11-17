/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

public class OvsAdapter implements AutoCloseable {

    private final FabricDeviceManager deviceManager;
    //private final HostRouteManager hostRouteManager;
    //private final BdIfManager bfIfManager;
    //private final AclManager aclManager;

    private final DataBroker dataBroker;

    public OvsAdapter (final DataBroker databroker,
                             final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = databroker;

        deviceManager = new FabricDeviceManager(dataBroker, rpcRegistry);
    }

    Openflow13Provider of13Provider = null;

    @Override
    public void close() throws Exception {
        deviceManager.close();

    }

}
