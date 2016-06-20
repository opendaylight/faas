/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.vlan_fabric.impl;

import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.FabricRendererRegistry;
import org.opendaylight.faas.fabric.vlan.VlanFabricProvider;

public class VlanFabricModule extends org.opendaylight.controller.config.yang.config.vlan_fabric.impl.AbstractVlanFabricModule {
    public VlanFabricModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public VlanFabricModule(ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, VlanFabricModule oldModule, AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        DataBroker databroker = this.getDataBrokerDependency();
        NotificationPublishService notificationPubService = this.getNotificationPublishServiceDependency();
        RpcProviderRegistry rpcRegistry = this.getRpcRegistryDependency();

        FabricRendererRegistry rendererRegistry = this.getRendererRegistryDependency();

        return new VlanFabricProvider(databroker, rpcRegistry, notificationPubService, rendererRegistry);
    }

}