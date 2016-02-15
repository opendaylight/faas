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

import java.util.List;
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
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddLinkToFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.DecomposeFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.RmLinkFromFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.RmNodeFromFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.LinkRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.UnderlayerNetworkType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class FabricManagementAPIProviderTest extends AbstractDataStoreManager {

    private FabricManagementAPIProvider target;

    @Mock private RpcProviderRegistry rpcRegistry;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @Mock private RpcRegistration<FabricService> rpcRegistration;

    FabricId fabricId = new FabricId("fabric:1");

    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        setOdlDataBroker();

        FabricRendererFactory factory = new MockFabricRendererFactory();
        when(rpcRegistry.addRpcImplementation(any(Class.class), any(FabricService.class))).thenReturn(rpcRegistration);

        FabricRendererRegistry reg = mock(FabricRendererRegistry.class);
        when(reg.getFabricRendererFactory(eq(UnderlayerNetworkType.VXLAN))).thenReturn(factory);

        target = new FabricManagementAPIProvider(dataBroker, rpcRegistry, executor, reg);
        target.start();
    }

    @After
    public void destroy() throws Exception {
         target.close();
    }

    private ComposeFabricInput setComposeFabricArg(List<DeviceNodes> lst) {
        ComposeFabricInputBuilder builder = new ComposeFabricInputBuilder();
        builder.setDescription("first fabric");
        builder.setName("fabric1");
        builder.setType(UnderlayerNetworkType.VXLAN);
        builder.setDeviceNodes(lst);

        return builder.build();
    }

    @Test
    public void test_composeFabric() throws Exception {
        // test1
        ComposeFabricInput input1 = setComposeFabricArg(null);
        target.composeFabric(input1);

        // test2
        DeviceNodesBuilder devBuilder = new DeviceNodesBuilder();
        devBuilder.setDeviceRef(new NodeRef(MdSalUtils.createNodeIId("ovsdb:1", "ovs1")));
        List<DeviceNodes> lst = Lists.newArrayList();
        lst.add(devBuilder.build());

        ComposeFabricInput input2 = setComposeFabricArg(lst);
        target.composeFabric(input2);
    }

    @Test
    public void decomposeFabric() throws Exception {
        ComposeFabricInput input1 = setComposeFabricArg(null);
        target.composeFabric(input1);

        DecomposeFabricInputBuilder builder = new DecomposeFabricInputBuilder();
        builder.setFabricId(fabricId);

        target.decomposeFabric(builder.build());
    }

    @Test
    public void getAllFabric() throws Exception {
        ComposeFabricInput input1 = setComposeFabricArg(null);
        target.composeFabric(input1);

        target.getAllFabric();
    }

    @Test
    public void rmNodeFromFabric() throws Exception {
        ComposeFabricInput input1 = setComposeFabricArg(null);
        target.composeFabric(input1);

        RmNodeFromFabricInputBuilder builder = new RmNodeFromFabricInputBuilder();
        builder.setFabricId(fabricId);
        builder.setNodeRef(new NodeRef(MdSalUtils.createNodeIId("ovsdb:1", "ovs:1")));

        target.rmNodeFromFabric(builder.build());
    }

    @Test
    public void addNodeToFabric() throws Exception {
        ComposeFabricInput input1 = setComposeFabricArg(null);
        target.composeFabric(input1);

        AddNodeToFabricInputBuilder builder = new AddNodeToFabricInputBuilder();
        builder.setFabricId(fabricId);
        builder.setNodeRef(new NodeRef(MdSalUtils.createNodeIId("ovsdb:1", "ovs:1")));

        target.addNodeToFabric(builder.build());
    }

    @Test
    public void rmLinkFromFabric() throws Exception {
        ComposeFabricInput input1 = setComposeFabricArg(null);
        target.composeFabric(input1);

        RmLinkFromFabricInputBuilder builder = new RmLinkFromFabricInputBuilder();
        builder.setFabricId(fabricId);
        InstanceIdentifier<Link> link = MdSalUtils.createLinkIId(fabricId, new LinkId("link1"));
        builder.setLinkRef(new LinkRef(link));

        target.rmLinkFromFabric(builder.build());
    }

    @Test
    public void addLinkToFabric() throws Exception {
        ComposeFabricInput input1 = setComposeFabricArg(null);
        target.composeFabric(input1);

        AddLinkToFabricInputBuilder builder = new AddLinkToFabricInputBuilder();
        builder.setFabricId(fabricId);
        InstanceIdentifier<Link> link = MdSalUtils.createLinkIId(fabricId, new LinkId("link1"));
        builder.setLinkRef(new LinkRef(link));

        target.addLinkToFabric(builder.build());
    }
}
