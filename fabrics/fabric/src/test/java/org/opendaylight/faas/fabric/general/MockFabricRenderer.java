/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicSwitchAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MockFabricRenderer implements FabricRenderer {

    FabricId fabricid;
    public MockFabricRenderer(InstanceIdentifier<FabricNode> iid) {
        fabricid = new FabricId(iid.firstKeyOf(Node.class).getNodeId());
    }

    @Override
    public boolean addNodeToFabric(DeviceNodesBuilder node, AddNodeToFabricInput input) {
        return false;
    }

    @Override
    public void buildLogicSwitch(NodeId nodeid, LswAttributeBuilder lsw, CreateLogicSwitchInput input) {
    }

    @Override
    public void buildLogicRouter(NodeId nodeid, LrAttributeBuilder lr, CreateLogicRouterInput input) {
    }

    @Override
    public void buildLogicPort(TpId tpid, LportAttributeBuilder lp, CreateLogicPortInput input) {
    }

    @Override
    public void buildGateway(NodeId switchid, IpPrefix ip, NodeId routerid, FabricId fabricid) {
    }

    @Override
    public InstanceIdentifier<FabricAcl> addAcl(NodeId deviceid, TpId tpid, String aclName) {
        return createAclIId(deviceid, tpid, aclName);
    }

    private InstanceIdentifier<FabricAcl> createAclIId(NodeId deviceid, TpId tpid, String aclName) {
        InstanceIdentifier<FabricAcl> aclIId = null;

        if (tpid != null) {
            aclIId = MdSalUtils.createLogicPortIId(fabricid, deviceid, tpid)
                    .augmentation(LogicPortAugment.class)
                    .child(LportAttribute.class)
                    .child(FabricAcl.class, new FabricAclKey(aclName));
         } else {
             aclIId = MdSalUtils.createNodeIId(fabricid, deviceid)
                     .augmentation(LogicSwitchAugment.class)
                     .child(LswAttribute.class)
                     .child(FabricAcl.class, new FabricAclKey(aclName));
         }
        return aclIId;
    }

    @Override
    public InstanceIdentifier<FabricAcl> delAcl(NodeId deviceid, TpId tpid, String aclName) {
        return createAclIId(deviceid, tpid, aclName);
    }

}
