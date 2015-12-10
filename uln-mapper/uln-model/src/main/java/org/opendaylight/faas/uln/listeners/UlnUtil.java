/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.listeners;

import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

public class UlnUtil {

    public static CreateLneLayer2Input createLneLayer2Input(LogicalSwitch lsw) {
        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid lswId =
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid(
                        lsw.getUuid().getValue());
        builder.setLswUuid(lswId);
        return builder.build();
    }

    public static CreateLneLayer3Input createLneLayer3Input(LogicalRouter lr) {
        CreateLneLayer3InputBuilder builder = new CreateLneLayer3InputBuilder();
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid lrId =
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid(
                        lr.getUuid().getValue());
        builder.setLrUuid(lrId);
        return builder.build();
    }

    public static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid convertToYangUuid(
            Uuid faasUuid) {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid yangUuid =
                new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid(
                        faasUuid.getValue());
        return yangUuid;
    }

    public static EndpointAttachInfo createEpAttachmentInput(EndpointLocation epLocation, Subnet subnet, Port epPort) {
        EndpointAttachInfo info = null;

        Uuid epFaasUuid = epLocation.getUuid();

        //TODO: incomplete
        NodeId nodeId = epLocation.getNodeId();
        epLocation.getNodeConnectorId();

        subnet.getIpPrefix();
        epPort.getMacAddress();

        info = new EndpointAttachInfo();
        info.setEpYangUuid(UlnUtil.convertToYangUuid(epFaasUuid));

        return info;
    }

}
