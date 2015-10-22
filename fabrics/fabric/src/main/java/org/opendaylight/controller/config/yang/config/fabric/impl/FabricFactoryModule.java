package org.opendaylight.controller.config.yang.config.fabric.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.FabricFactory;

public class FabricFactoryModule extends org.opendaylight.controller.config.yang.config.fabric.impl.AbstractFabricFactoryModule {
    public FabricFactoryModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public FabricFactoryModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.fabric.impl.FabricFactoryModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        DataBroker databroker = this.getDataBrokerDependency();
        NotificationProviderService notificationService = this.getNotificationServiceDependency();
        RpcProviderRegistry rpcRegistry = this.getRpcRegistryDependency();

        return new FabricFactory(databroker, rpcRegistry, notificationService);
    }

}