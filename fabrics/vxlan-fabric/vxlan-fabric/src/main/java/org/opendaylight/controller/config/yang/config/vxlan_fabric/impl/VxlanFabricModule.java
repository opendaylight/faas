package org.opendaylight.controller.config.yang.config.vxlan_fabric.impl;
public class VxlanFabricModule extends org.opendaylight.controller.config.yang.config.vxlan_fabric.impl.AbstractVxlanFabricModule {
    public VxlanFabricModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public VxlanFabricModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.vxlan_fabric.impl.VxlanFabricModule oldModule, java.lang.AutoCloseable oldInstance) {
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
