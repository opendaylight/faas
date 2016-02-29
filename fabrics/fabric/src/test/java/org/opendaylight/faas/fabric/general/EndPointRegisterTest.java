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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricRendererFactory;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.FabricEndpointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.LocateEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.UnregisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.locate.endpoint.input.Location;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.locate.endpoint.input.LocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.TpRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class EndPointRegisterTest extends AbstractDataStoreManager {

    EndPointRegister target;

    @Mock private RpcProviderRegistry rpcRegistry;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @Mock private RpcRegistration<FabricEndpointService> rpcRegistration;

    FabricId fabricId = new FabricId("fabric:1");

    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {

        setOdlDataBroker();

        FabricRendererFactory factory = new MockFabricRendererFactory();

        FabricRendererRegistry reg = mock(FabricRendererRegistry.class);
        when(reg.getFabricRendererFactory(eq(UnderlayerNetworkType.VXLAN))).thenReturn(factory);

        when(rpcRegistry.addRpcImplementation(any(Class.class), any(FabricEndpointService.class))).thenReturn(rpcRegistration);

        target = new EndPointRegister(dataBroker, rpcRegistry, executor);
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

    @Test
    public void constructorTest() throws Exception {
        target.close();
        verify(rpcRegistration).close();
    }

    private Uuid epUuid = new Uuid("75a4451e-eed0-4645-9194-64454bda2902");

    @Test
    public void registerEndpoint() throws Exception {
        RegisterEndpointInputBuilder builder = new RegisterEndpointInputBuilder();
        builder.setFabricId(fabricId);
        builder.setEndpointUuid(epUuid);
        builder.setGateway(new IpAddress(new Ipv4Address("192.168.50.1")));
        builder.setIpAddress(new IpAddress(new Ipv4Address("192.168.50.101")));
        builder.setMacAddress(new MacAddress("00:10:13:11:22:33"));

        target.registerEndpoint(builder.build());
    }

    @Test
    public void unregisterEndpoint() throws Exception {
        UnregisterEndpointInputBuilder builder = new UnregisterEndpointInputBuilder();
        builder.setFabricId(fabricId);
        List<Uuid> lst = Lists.newArrayList();
        lst.add(epUuid);
        builder.setIds(lst);

        target.unregisterEndpoint(builder.build());
    }

    @Test
    public void locateEndpoint() throws Exception {
        LocateEndpointInputBuilder builder = new LocateEndpointInputBuilder();
        builder.setFabricId(fabricId);
        builder.setEndpointId(epUuid);

        InstanceIdentifier<Node> noderef = MdSalUtils.createNodeIId("ovsdb:1", "ovs1");
        InstanceIdentifier<TerminationPoint> tpref = noderef.child(TerminationPoint.class, new TerminationPointKey(new TpId("")));
        Location loc = new LocationBuilder()
                .setNodeRef(new NodeRef(noderef))
                .setTpRef(new TpRef(tpref))
                .build();
        builder.setLocation(loc);

        target.locateEndpoint(builder.build());
    }

}
