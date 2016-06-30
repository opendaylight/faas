/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.utils;

import com.google.common.base.Optional;

import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricPortAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.LogicalPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.services.rev150930.network.topology.topology.node.termination.point.LportAttribute;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceManager {

    private static final Logger LOG = LoggerFactory.getLogger(InterfaceManager.class);

    public static TpId createFabricPort(NodeId nodeid, TpId tpid) {
        return new TpId(String.format("%s - %s", nodeid.getValue(), tpid.getValue()));
    }

    @SuppressWarnings("unchecked")
    public static InstanceIdentifier<TerminationPoint> convFabricPort2DevicePort(
            DataBroker broker, FabricId fabricid, TpId tpid) {
        InstanceIdentifier<TerminationPoint> iidFPort = MdSalUtils.createFabricPortIId(fabricid, tpid);

        ReadTransaction rt = broker.newReadOnlyTransaction();
        Optional<TerminationPoint> opt = syncReadOper(rt, iidFPort);
        if (opt.isPresent()) {
            TerminationPoint tp = opt.get();
            return (InstanceIdentifier<TerminationPoint>) tp.getAugmentation(FabricPortAugment.class)
                    .getFportAttribute()
                    .getDevicePort()
                    .getValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static InstanceIdentifier<TerminationPoint> convDevPort2FabricPort(
            DataBroker broker, FabricId fabricid, InstanceIdentifier<TerminationPoint> tpIid) {

        ReadTransaction rt = broker.newReadOnlyTransaction();
        Optional<TerminationPoint> opt = syncReadOper(rt, tpIid);
        if (opt.isPresent()) {
            TerminationPoint tp = opt.get();
            return (InstanceIdentifier<TerminationPoint>) tp.getAugmentation(FabricPortAug.class)
                    .getPortRef().getValue();
        }
        return null;
    }

    public static LportAttribute getLogicalPortAttr(DataBroker broker, InstanceIdentifier<TerminationPoint> iid) {
        ReadTransaction rt = broker.newReadOnlyTransaction();
        Optional<TerminationPoint> opt = syncReadOper(rt, iid);
        if (opt.isPresent()) {
            TerminationPoint tp = opt.get();
            return tp.getAugmentation(LogicalPortAugment.class).getLportAttribute();
        }
        return null;
    }

    private static <T extends DataObject> Optional<T> syncReadOper(ReadTransaction rt,
             InstanceIdentifier<T> path) {
        Optional<T> opt;
        try {
            opt = rt.read(LogicalDatastoreType.OPERATIONAL, path).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("unexcepte exception", e);
            opt = Optional.absent();
        }
        return opt;
    }
}