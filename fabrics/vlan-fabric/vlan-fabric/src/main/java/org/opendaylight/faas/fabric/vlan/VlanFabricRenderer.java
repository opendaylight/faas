/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import com.google.common.util.concurrent.ListeningExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalRouterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.CreateLogicalSwitchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalRouterAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalSwitchAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LrAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.LswAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VlanFabricRenderer implements AutoCloseable, FabricRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(VlanFabricRenderer.class);

    private final DataBroker dataBroker;

    private final ListeningExecutorService executor;
    private final FabricContext fabricCtx;

    public VlanFabricRenderer(final DataBroker dataProvider,
                             final FabricContext fabricCtx) {
        this.dataBroker = dataProvider;
        this.executor = fabricCtx.executor;
        this.fabricCtx = fabricCtx;
    }

    @Override
    public void close() throws Exception {
        // do nothing
    }


    @Override
    public void buildLogicalSwitch(NodeId nodeid, LswAttributeBuilder lsw, CreateLogicalSwitchInput input) {

    }

    @Override
    public void buildLogicalRouter(NodeId nodeid, LrAttributeBuilder lr, CreateLogicalRouterInput input) {

    }

    @Override
    public void buildLogicalPort(TpId tpid, LportAttributeBuilder lp, CreateLogicalPortInput input) {
        //do nothing
    }

    @Override
    public void buildGateway(NodeId switchid, IpPrefix ip, NodeId routerid,  FabricId fabricid) {
        fabricCtx.associateSwitchToRouter(fabricid, switchid, routerid, ip);
    }

    @Override
    public boolean addNodeToFabric(DeviceNodesBuilder node, AddNodeToFabricInput input) {

        return true;
    }

    @Override
    public InstanceIdentifier<FabricAcl> addAcl(NodeId deviceid, TpId tpid, String aclName) {
        return createAclIId(deviceid, tpid, aclName);
    }

    private InstanceIdentifier<FabricAcl> createAclIId(NodeId deviceid, TpId tpid, String aclName) {
        FabricId fabricid = fabricCtx.getFabricId();
        InstanceIdentifier<FabricAcl> aclIId = null;
        boolean isLsw = fabricCtx.isValidLogicSwitch(deviceid);
        boolean isLr = fabricCtx.isValidLogicRouter(deviceid);

        if (tpid != null) {
            aclIId = MdSalUtils.createLogicPortIId(fabricid, deviceid, tpid)
                    .augmentation(LogicalPortAugment.class)
                    .child(LportAttribute.class)
                    .child(FabricAcl.class, new FabricAclKey(aclName));
        } else {
            if (isLsw) {
                aclIId = MdSalUtils.createNodeIId(fabricid, deviceid)
                         .augmentation(LogicalSwitchAugment.class)
                         .child(LswAttribute.class)
                         .child(FabricAcl.class, new FabricAclKey(aclName));
            }
            if (isLr) {
                aclIId = MdSalUtils.createNodeIId(fabricid, deviceid)
                         .augmentation(LogicalRouterAugment.class)
                         .child(LrAttribute.class)
                         .child(FabricAcl.class, new FabricAclKey(aclName));
            }
        }
        return aclIId;
    }

    @Override
    public InstanceIdentifier<FabricAcl> delAcl(NodeId deviceid, TpId tpid, String aclName) {
        return createAclIId(deviceid, tpid, aclName);
    }
}
