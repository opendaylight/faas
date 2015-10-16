package org.opendaylight.controller.config.yang.config.vxlan_fabric_ovs_adapter.impl;
public class VxlanFabricOVSAdapterModule extends org.opendaylight.controller.config.yang.config.vxlan_fabric_ovs_adapter.impl.AbstractVxlanFabricOVSAdapterModule {
    public VxlanFabricOVSAdapterModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public VxlanFabricOVSAdapterModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.vxlan_fabric_ovs_adapter.impl.VxlanFabricOVSAdapterModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        // TODO:implement
        return new AutoCloseable() {

            @Override
            public void close() throws Exception {

            }
        };
    }

}
