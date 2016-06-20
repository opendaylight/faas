/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import com.google.common.base.Optional;

import java.util.List;
import java.util.concurrent.Callable;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.BridgeDomainPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.logical.port.UnderlayerPorts;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclRenderer implements Callable<Void> {

    private enum RENDERER_CMD {addAcl, rmAcl};

    private String aclname;
    private InstanceIdentifier<TerminationPoint> lPortIid;

    private final RENDERER_CMD cmd;
    private final DataBroker databroker;

    public static AclRenderer newAddAclTask(DataBroker databroker,InstanceIdentifier<TerminationPoint> lPortIid, String aclname) {
        AclRenderer o = new AclRenderer(databroker, RENDERER_CMD.addAcl);
        o.lPortIid = lPortIid;
        o.aclname = aclname;
        return o;
    }

    public static AclRenderer newRmAclTask(DataBroker databroker,InstanceIdentifier<TerminationPoint> lPortIid, String aclname) {
        AclRenderer o = new AclRenderer(databroker, RENDERER_CMD.rmAcl);
        o.lPortIid = lPortIid;
        o.aclname = aclname;
        return o;
    }

    private AclRenderer(DataBroker databroker, RENDERER_CMD cmd) {
        this.databroker = databroker;
        this.cmd = cmd;
    }

    @Override
    public Void call() throws Exception {

        switch (cmd) {
            case addAcl:
                addAcl();
                break;
            case rmAcl:
                rmAcl();
                break;
            default:
                break;
        }
        return null;
    }


    void addAcl() throws Exception {
        Optional<TerminationPoint> optional = readTp();
        if (optional.isPresent()) {
            LogicalPortAugment lpAug = optional.get().getAugmentation(LogicalPortAugment.class);
            if (lpAug == null) {
                return;
            }
            LportAttribute lattr = lpAug.getLportAttribute();
            List<UnderlayerPorts> uports = lattr.getUnderlayerPorts();
            if (uports == null || uports.isEmpty()) {
                return;
            }

            WriteTransaction trans = databroker.newWriteOnlyTransaction();
            FabricAclBuilder builder = new FabricAclBuilder();
            builder.setFabricAclName(aclname);
            for (UnderlayerPorts uport : uports) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<TerminationPoint> tpIid = (InstanceIdentifier<TerminationPoint>) uport.getPortRef().getValue();
                InstanceIdentifier<FabricAcl> path = tpIid.augmentation(BridgeDomainPort.class).child(FabricAcl.class, new FabricAclKey(aclname));

                trans.merge(LogicalDatastoreType.OPERATIONAL, path, builder.build(), true);
            }
            MdSalUtils.wrapperSubmit(trans);
        }
    }

    void rmAcl() throws Exception {
        Optional<TerminationPoint> optional = readTp();
        if (optional.isPresent()) {
            LogicalPortAugment lpAug = optional.get().getAugmentation(LogicalPortAugment.class);
            if (lpAug == null) {
                return;
            }
            LportAttribute lattr = lpAug.getLportAttribute();
            List<UnderlayerPorts> uports = lattr.getUnderlayerPorts();
            if (uports == null || uports.isEmpty()) {
                return;
            }
            WriteTransaction trans = databroker.newWriteOnlyTransaction();
            for (UnderlayerPorts uport : uports) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier<TerminationPoint> tpIid = (InstanceIdentifier<TerminationPoint>) uport.getPortRef().getValue();
                InstanceIdentifier<FabricAcl> path = tpIid.augmentation(BridgeDomainPort.class).child(FabricAcl.class, new FabricAclKey(aclname));
                trans.delete(LogicalDatastoreType.OPERATIONAL, path);
            }
            MdSalUtils.wrapperSubmit(trans);
        }
    }

    private Optional<TerminationPoint> readTp() throws Exception {
        if (lPortIid != null) {
            ReadWriteTransaction trans = databroker.newReadWriteTransaction();
            return trans.read(LogicalDatastoreType.OPERATIONAL, lPortIid).get();
        } else {
            return Optional.absent();
        }
    }
}