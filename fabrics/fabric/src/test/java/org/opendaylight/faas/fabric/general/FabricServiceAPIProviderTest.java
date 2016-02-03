package org.opendaylight.faas.fabric.general;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddAclInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.DelAclInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmGatewayInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicRouterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicSwitchInputBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FabricServiceAPIProviderTest{

    private FabricServiceAPIProvider target;

    @Mock private DataBroker dataBroker;
    @Mock private RpcProviderRegistry rpcRegistry;
    @Mock private ExecutorService executor;

    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        target = new FabricServiceAPIProvider(dataBroker, rpcRegistry, executor);
    }

    @Test
    public void rmLogicSwitch() throws Exception {
        RmLogicSwitchInputBuilder builder = new RmLogicSwitchInputBuilder();
        target.rmLogicSwitch(builder.build());
    }

    @Test
    public void rmGateway() {
        RmGatewayInputBuilder builder = new RmGatewayInputBuilder();
        target.rmGateway(builder.build());
    }


    @Test
    public void createGateway() {
        CreateGatewayInputBuilder builder = new CreateGatewayInputBuilder();
        //target.createGateway(builder.build());
    }

    @Test
    public void createLogicSwitch() {
        CreateLogicSwitchInputBuilder builder = new CreateLogicSwitchInputBuilder();
        target.createLogicSwitch(builder.build());
    }

    @Test
    public void rmLogicRouter() {
        RmLogicRouterInputBuilder builder = new RmLogicRouterInputBuilder();
        target.rmLogicRouter(builder.build());
    }

    @Test
    public void addAcl() {
        AddAclInputBuilder builder = new AddAclInputBuilder();
        target.addAcl(builder.build());
    }

    @Test
    public void delAcl() {
        DelAclInputBuilder builder = new DelAclInputBuilder();
        target.delAcl(builder.build());
    }

    @Test
    public void rmLogicPort() {
        RmLogicPortInputBuilder builder = new RmLogicPortInputBuilder();
        //target.rmLogicPort(builder.build());
    }

    @Test
    public void createLogicRouter() {
        CreateLogicRouterInputBuilder builder = new CreateLogicRouterInputBuilder();
        target.createLogicRouter(builder.build());
    }

    @Test
    public void createLogicPort() {
        CreateLogicPortInputBuilder builder = new CreateLogicPortInputBuilder();
        target.createLogicPort(builder.build());
    }
}
