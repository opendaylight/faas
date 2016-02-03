package org.opendaylight.faas.fabric.general;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.FabricEndpointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.LocateEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.UnregisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;

import com.google.common.util.concurrent.CheckedFuture;

@RunWith(MockitoJUnitRunner.class)
public class EndPointRegisterTest {

    EndPointRegister target;

    @Mock private DataBroker dataBroker;
    @Mock private RpcProviderRegistry rpcRegistry;
    @Mock private ExecutorService executor;
    @Mock private RpcRegistration<FabricEndpointService> rpcRegistration;
    @Mock private ReadWriteTransaction rwt;
    @Mock private ReadOnlyTransaction rt;
    @Mock private WriteTransaction wt;


    @Mock private FabricRenderer fabricRenderer;

    FabricId fabricId = new FabricId("fabric:1");

    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        FabricInstanceCache.INSTANCE.addFabric(fabricId, UnderlayerNetworkType.VXLAN, fabricRenderer);
        when(rpcRegistry.addRpcImplementation(any(Class.class), any(FabricEndpointService.class))).thenReturn(rpcRegistration);

        when(dataBroker.newReadOnlyTransaction()).thenReturn(rt);
        when(dataBroker.newReadWriteTransaction()).thenReturn(rwt);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(wt);

        target = new EndPointRegister(dataBroker, rpcRegistry, executor);
        target.start();
    }

    @Test
    public void constructorTest() throws Exception {
        target.close();
        verify(rpcRegistration).close();
    }

    @Test
    public void registerEndpoint() throws Exception {
        RegisterEndpointInputBuilder builder = new RegisterEndpointInputBuilder();
        builder.setFabricId(fabricId);

        CheckedFuture future = mock(CheckedFuture.class);
        when (rwt.submit()).thenReturn(future);

        target.registerEndpoint(builder.build());
    }

    @Test
    public void unregisterEndpoint() throws Exception {
        UnregisterEndpointInputBuilder builder = new UnregisterEndpointInputBuilder();
        builder.setFabricId(fabricId);
        target.unregisterEndpoint(builder.build());
    }

    @Test
    public void locateEndpoint() throws Exception {
        LocateEndpointInputBuilder builder = new LocateEndpointInputBuilder();
        builder.setFabricId(fabricId);
        target.locateEndpoint(builder.build());
    }

}
