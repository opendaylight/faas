/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.listeners;

import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3InputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public final class UlnUtil {

    private UlnUtil(){
    }

    public static CreateLneLayer2Input createLneLayer2Input(Uuid tenantId, NodeId fabricid, LogicalSwitch lsw) {
        CreateLneLayer2InputBuilder builder = new CreateLneLayer2InputBuilder();
        builder.setTenantId(new TenantId(tenantId.getValue()));
        builder.setLswUuid(UlnUtil.convertToYangUuid(lsw.getUuid()));
        builder.setName(UlnUtil.convertToYangUuid(lsw.getUuid()).getValue());
        builder.setVfabricId(fabricid);
        return builder.build();
    }

    public static CreateLneLayer3Input createLneLayer3Input(Uuid tenantId, NodeId fabricId, LogicalRouter lr) {
        CreateLneLayer3InputBuilder builder = new CreateLneLayer3InputBuilder();
        builder.setTenantId(new TenantId(tenantId.getValue()));
        builder.setLrUuid(UlnUtil.convertToYangUuid(lr.getUuid()));
        builder.setName(UlnUtil.convertToYangUuid(lr.getUuid()).getValue());
        builder.setVfabricId(fabricId);
        return builder.build();
    }

    public static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid convertToYangUuid(
            Uuid faasUuid) {
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid(
                        faasUuid.getValue());
    }

    public static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress convertToYang130715MacAddress(
            MacAddress ulnMac) {
        return new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress(
                        ulnMac.getValue());
    }

    public static EndpointAttachInfo createEpAttachmentInput(EndpointLocation epLocation, Subnet subnet, Port epPort) {
        EndpointAttachInfo info;

        Uuid epFaasUuid = epLocation.getUuid();

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId nodeId = epLocation.getNodeId();
        NodeConnectorId nodeConnectorId = epLocation.getNodeConnectorId();

        IpAddress gatewayIpAddr = subnet.getVirtualRouterIp();
        MacAddress macAddress = epPort.getMacAddress();
        IpAddress ipAddress = epPort.getPrivateIps().get(0).getIpAddress();

        info = new EndpointAttachInfo();
        info.setEpYangUuid(UlnUtil.convertToYangUuid(epFaasUuid));
        info.setMacAddress(convertToYang130715MacAddress(macAddress));
        info.setIpAddress(ipAddress);
        info.setGatewayIpAddr(gatewayIpAddr);
        info.setInventoryNodeIdStr(nodeId.getValue());
        info.setInventoryNodeConnectorIdStr(nodeConnectorId.getValue());

        return info;
    }
}
