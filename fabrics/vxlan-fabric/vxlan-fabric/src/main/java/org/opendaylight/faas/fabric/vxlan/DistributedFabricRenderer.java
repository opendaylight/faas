/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import com.google.common.util.concurrent.ListeningExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.faas.fabric.vxlan.res.ResourceManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.VtepBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.vxlan.rev150930.VxlanDeviceAddInput;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedFabricRenderer implements AutoCloseable, FabricRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedFabricRenderer.class);

    private final DataBroker dataBroker;

    private final ListeningExecutorService executor;
    private final FabricContext fabricCtx;

    public DistributedFabricRenderer(final DataBroker dataProvider,
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
        long segmentId = ResourceManager.getInstance(input.getFabricId()).allocSeg();
        lsw.setSegmentId(segmentId);
    }

    @Override
    public void buildLogicalRouter(NodeId nodeid, LrAttributeBuilder lr, CreateLogicalRouterInput input) {
        long vrfctx = ResourceManager.getInstance(input.getFabricId()).allocVrfCtx();
        lr.setVrfCtx(vrfctx);

        fabricCtx.addLogicRouter(nodeid, vrfctx);
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
        VxlanDeviceAddInput augment = input.getAugmentation(VxlanDeviceAddInput.class);
        IpAddress vtep = null;
        if (augment != null) {
            vtep = augment.getVtepIp();
        }
        if (vtep != null) {
            setVtep2Device(node.getDeviceRef(), vtep);
        }
        return true;
    }

    private void setVtep2Device(NodeRef node, IpAddress vtep) {
        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>) node .getValue();
        InstanceIdentifier<Vtep> vtepIId = nodeIId.augmentation(FabricCapableDevice.class).child(Attributes.class)
                .augmentation(VtepAttribute.class).child(Vtep.class);

        VtepBuilder builder = new VtepBuilder();
        builder.setIp(vtep);

        trans.put(LogicalDatastoreType.OPERATIONAL, vtepIId, builder.build(), true);
        MdSalUtils.wrapperSubmit(trans, executor);
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
