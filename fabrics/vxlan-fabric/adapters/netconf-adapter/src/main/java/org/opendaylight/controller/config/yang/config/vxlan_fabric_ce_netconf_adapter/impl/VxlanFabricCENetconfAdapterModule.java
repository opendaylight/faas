/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.vxlan_fabric_ce_netconf_adapter.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.vxlan.adapter.ce.VrfNetConfAdapter;

public class VxlanFabricCENetconfAdapterModule extends org.opendaylight.controller.config.yang.config.vxlan_fabric_ce_netconf_adapter.impl.AbstractVxlanFabricCENetconfAdapterModule {
    public VxlanFabricCENetconfAdapterModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public VxlanFabricCENetconfAdapterModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.vxlan_fabric_ce_netconf_adapter.impl.VxlanFabricCENetconfAdapterModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        DataBroker databroker = this.getDataBrokerDependency();
        RpcProviderRegistry rpcRegistry = this.getRpcRegistryDependency();

        return new VrfNetConfAdapter(databroker, rpcRegistry);
    }

}
