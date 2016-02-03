package org.opendaylight.faas.fabric.general;

import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.DecomposeFabricInputBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FabricManagementAPIProviderTest {

    private FabricManagementAPIProvider target;

    @Mock private DataBroker dataBroker;
    @Mock private RpcProviderRegistry rpcRegistry;
    @Mock private ExecutorService executor;

    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        FabricRendererRegistry reg = mock(FabricRendererRegistry.class);
        target = new FabricManagementAPIProvider(dataBroker, rpcRegistry, executor, reg);
    }


    @Test
    public void composeFabric() throws Exception {
        ComposeFabricInputBuilder builder = new ComposeFabricInputBuilder();
        //target.composeFabric(builder.build());
    }

    @Test
    public void decomposeFabric() throws Exception {
        DecomposeFabricInputBuilder builder = new DecomposeFabricInputBuilder();
        target.decomposeFabric(builder.build());
    }

}
