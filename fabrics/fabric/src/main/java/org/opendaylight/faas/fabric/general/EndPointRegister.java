package org.opendaylight.faas.fabric.general;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.FabricEndpointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.LocateEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.UnregisterEndpointInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPointRegister implements FabricEndpointService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FabricManagementApi.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private RpcRegistration<FabricEndpointService> rpcRegistration;

    private final ExecutorService executor;

    public EndPointRegister (final DataBroker dataBroker,
            final RpcProviderRegistry rpcRegistry, ExecutorService executor) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;

        this.executor = executor;
    }

    @Override
    public Future<RpcResult<Void>> unregisterEndpoint(UnregisterEndpointInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<RegisterEndpointOutput>> registerEndpoint(RegisterEndpointInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> locateEndpoint(LocateEndpointInput input) {
        return null;
    }

    public void start() {
        rpcRegistration = rpcRegistry.addRpcImplementation(FabricEndpointService.class, this);
    }

    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
    }

}