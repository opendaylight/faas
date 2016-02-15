/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricRendererFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.AddAclInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateGatewayInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.DelAclInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.FabricServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmGatewayInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicRouterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.RmLogicSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

@RunWith(MockitoJUnitRunner.class)
public class FabricServiceAPIProviderTest extends AbstractDataStoreManager {

    private FabricServiceAPIProvider target;

    @Mock private RpcProviderRegistry rpcRegistry;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @Mock private RpcRegistration<FabricServiceService> rpcRegistration;

    FabricId fabricId = new FabricId("fabric:1");

    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        setOdlDataBroker();

        FabricRendererFactory factory = new MockFabricRendererFactory();
        when(rpcRegistry.addRpcImplementation(any(Class.class), any(FabricService.class))).thenReturn(rpcRegistration);

        FabricRendererRegistry reg = mock(FabricRendererRegistry.class);
        when(reg.getFabricRendererFactory(eq(UnderlayerNetworkType.VXLAN))).thenReturn(factory);

        target = new FabricServiceAPIProvider(dataBroker, rpcRegistry, executor);
        target.start();

        setupFabric(reg);
    }

    private void setupFabric(FabricRendererRegistry reg) {
        ComposeFabricInputBuilder builder = new ComposeFabricInputBuilder();
        builder.setDescription("first fabric");
        builder.setName("fabric1");
        builder.setType(UnderlayerNetworkType.VXLAN);

        FabricManagementAPIProvider fmgr = new FabricManagementAPIProvider(dataBroker, rpcRegistry, executor, reg);
        fmgr.composeFabric(builder.build());
    }

    @After
    public void destroy() throws Exception {
        target.close();
    }

    @Test
    public void rmLogicSwitch() throws Exception {
        createLogicSwitch();

        RmLogicSwitchInputBuilder builder = new RmLogicSwitchInputBuilder();
        builder.setFabricId(fabricId);
        builder.setNodeId(new NodeId("lsw1"));

        target.rmLogicSwitch(builder.build());
    }

    @Test
    public void rmGateway() {
        createGateway();

        RmGatewayInputBuilder builder = new RmGatewayInputBuilder();
        builder.setFabricId(fabricId);
        builder.setLogicRouter(new NodeId("lr1"));
        builder.setIpAddress(new IpAddress(new Ipv4Address("10.0.1.1")));

        target.rmGateway(builder.build());
    }

    @Test
    public void createGateway() {
        createLogicRouter();
        createLogicSwitch();

        CreateGatewayInputBuilder builder = new CreateGatewayInputBuilder();
        builder.setFabricId(fabricId);
        builder.setIpAddress(new IpAddress(new Ipv4Address("10.0.1.1")));
        builder.setLogicRouter(new NodeId("lr1"));
        builder.setLogicSwitch(new NodeId("lsw1"));

        target.createGateway(builder.build());
    }

    @Test
    public void createGateway_2() {
        createLogicRouter();
        createLogicSwitch();

        CreateGatewayInputBuilder builder = new CreateGatewayInputBuilder();
        builder.setFabricId(fabricId);
        builder.setIpAddress(new IpAddress(new Ipv4Address("192.168.1.1")));
        builder.setLogicRouter(new NodeId("lr1"));
        builder.setLogicSwitch(new NodeId("lsw1"));

        target.createGateway(builder.build());
    }

    @Test
    public void createGateway_3() {
        createLogicRouter();
        createLogicSwitch();

        CreateGatewayInputBuilder builder = new CreateGatewayInputBuilder();
        builder.setFabricId(fabricId);
        builder.setIpAddress(new IpAddress(new Ipv4Address("172.16.1.1")));
        builder.setLogicRouter(new NodeId("lr1"));
        builder.setLogicSwitch(new NodeId("lsw1"));

        target.createGateway(builder.build());
    }

    @Test
    public void createGateway_4() {
        createLogicRouter();
        createLogicSwitch();

        CreateGatewayInputBuilder builder = new CreateGatewayInputBuilder();
        builder.setFabricId(fabricId);
        builder.setIpAddress(new IpAddress(new Ipv4Address("172.16.1.1")));
        builder.setNetwork(new IpPrefix(new Ipv4Prefix("172.16.1.1/8")));
        builder.setLogicRouter(new NodeId("lr1"));
        builder.setLogicSwitch(new NodeId("lsw1"));

        target.createGateway(builder.build());
    }

    @Test
    public void createLogicSwitch() {

        CreateLogicSwitchInputBuilder builder = new CreateLogicSwitchInputBuilder();
        builder.setFabricId(fabricId);
        builder.setName("lsw1");

        target.createLogicSwitch(builder.build());
    }

    @Test
    public void rmLogicRouter() {
        createLogicRouter();

        RmLogicRouterInputBuilder builder = new RmLogicRouterInputBuilder();
        builder.setFabricId(fabricId);
        builder.setNodeId(new NodeId("lr1"));

        target.rmLogicRouter(builder.build());
    }

    @Test
    public void addAcl() throws InterruptedException, ExecutionException {
        // prepare
        this.createLogicPort();

        // test
        AddAclInputBuilder builder = new AddAclInputBuilder();
        builder.setFabricId(fabricId);
        builder.setAclName("acl1");
        builder.setLogicDevice(new NodeId("lsw1"));

        target.addAcl(builder.build());

        builder.setLogicPort(new TpId("lsw1-port1"));
        target.addAcl(builder.build());
    }

    @Test
    public void delAcl() {
        DelAclInputBuilder builder = new DelAclInputBuilder();
        builder.setFabricId(fabricId);
        builder.setLogicDevice(new NodeId("lsw1"));
        builder.setAclName("acl-1");

        target.delAcl(builder.build());

        builder.setLogicPort(new TpId("tpid"));
        target.delAcl(builder.build());
    }

    @Test
    public void rmLogicPort() {
        RmLogicPortInputBuilder builder = new RmLogicPortInputBuilder();
        builder.setFabricId(fabricId);
        builder.setLogicDevice(new NodeId("lsw1"));
        builder.setTpId(new TpId("lsw1-port1"));

        target.rmLogicPort(builder.build());
    }

    @Test
    public void createLogicRouter() {
        CreateLogicRouterInputBuilder builder = new CreateLogicRouterInputBuilder();
        builder.setFabricId(fabricId);
        builder.setName("lr1");

        target.createLogicRouter(builder.build());
    }

    @Test
    public void createLogicPort() {
        this.createLogicSwitch();

        CreateLogicPortInputBuilder builder = new CreateLogicPortInputBuilder();
        builder.setFabricId(fabricId);
        builder.setName("lsw1-port1");
        builder.setLogicDevice(new NodeId("lsw1"));

        target.createLogicPort(builder.build());
    }
}
