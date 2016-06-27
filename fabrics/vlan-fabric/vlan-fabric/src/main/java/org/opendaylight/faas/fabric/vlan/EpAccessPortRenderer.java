/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.BridgeDomainPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.BridgeDomainPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.LogicalLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.TpRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.UnderlayerPorts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.UnderlayerPortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.UnderlayerPortsKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;

public class EpAccessPortRenderer {

    private InstanceIdentifier<TerminationPoint> lportIid;

    private final DataBroker databroker;

    public static EpAccessPortRenderer newRenderer(DataBroker databroker) {
        return new EpAccessPortRenderer(databroker);
    }

    public static EpAccessPortRenderer newRenderer(DataBroker databroker,
            InstanceIdentifier<TerminationPoint> lportIid) {
        EpAccessPortRenderer obj = new EpAccessPortRenderer(databroker);
        obj.lportIid = lportIid;
        return obj;
    }

    private EpAccessPortRenderer(DataBroker databroker) {
        this.databroker = databroker;
    }

    @SuppressWarnings("unchecked")
    public void createEpAccessPort(WriteTransaction trans,
            Endpoint ep, InstanceIdentifier<BdPort> bdPortIid) throws Exception {
        if (!calcLogicPortIId(ep)) {
            return;
        }

        InstanceIdentifier<Node> devNode = (InstanceIdentifier<Node>) ep.getLocation().getNodeRef().getValue();

        Optional<TerminationPoint> optional = readTp();

        InstanceIdentifier<UnderlayerPorts> iid = lportIid.augmentation(LogicalPortAugment.class)
                .child(LportAttribute.class)
                .child(UnderlayerPorts.class, new UnderlayerPortsKey(bdPortIid));

        UnderlayerPortsBuilder builder = new UnderlayerPortsBuilder();
        builder.setPortRef(bdPortIid);

        trans.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());

        if (optional.isPresent()) {
            LogicalPortAugment lpAug = optional.get().getAugmentation(LogicalPortAugment.class);
            if (lpAug != null) {
                LportAttribute lattr = lpAug.getLportAttribute();
                List<FabricAcl> acls = lattr.getFabricAcl();
                if (acls != null && acls.isEmpty()) {
                    cpAcls(acls, bdPortIid, trans);
                }
            }
        }

    }

    void removeEpAccessPort() throws Exception {

        WriteTransaction trans = databroker.newWriteOnlyTransaction();

        Optional<TerminationPoint> optional = readTp();
        if (optional.isPresent()) {
            LogicalPortAugment lpAug = optional.get().getAugmentation(LogicalPortAugment.class);
            if (lpAug != null) {
                LportAttribute lattr = lpAug.getLportAttribute();
                List<UnderlayerPorts> uports = lattr.getUnderlayerPorts();
                if (uports == null || uports.isEmpty()) {
                    return;
                }
                List<FabricAcl> acls = lattr.getFabricAcl();
                if (acls == null || acls.isEmpty()) {
                    for (UnderlayerPorts uport : uports) {
                        trans.delete(LogicalDatastoreType.OPERATIONAL, uport.getPortRef());
                    }
                }

                InstanceIdentifier<UnderlayerPorts> iid = lportIid.augmentation(LogicalPortAugment.class)
                        .child(LportAttribute.class)
                        .child(UnderlayerPorts.class);

                trans.delete(LogicalDatastoreType.OPERATIONAL, iid);
                MdSalUtils.wrapperSubmit(trans);
            }
        }
    }

    private void cpAcls(List<FabricAcl> acls, InstanceIdentifier<BdPort> bdPortIid, WriteTransaction trans) {

        BdPortBuilder bdportBuilder = new  BdPortBuilder();
        bdportBuilder.setFabricAcl(acls);

        trans.merge(LogicalDatastoreType.OPERATIONAL, bdPortIid, bdportBuilder.build());

    }

    private Optional<TerminationPoint> readTp() throws Exception {
        if (lportIid != null) {
            ReadWriteTransaction trans = databroker.newReadWriteTransaction();
            return trans.read(LogicalDatastoreType.OPERATIONAL, lportIid).get();
        } else {
            return Optional.absent();
        }
    }

    private boolean calcLogicPortIId(Endpoint ep) {
        LogicalLocation ll = ep.getLogicalLocation();
        if (ll == null) {
            return false;
        }
        lportIid = MdSalUtils.createLogicPortIId(ep.getOwnFabric(), ll.getNodeId(), ll.getTpId());
        return true;
    }
}