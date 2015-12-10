/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr.api;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.Location;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.LogicLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;

public class EndpointAttachInfo {
    private IpAddress gatewayIpAddr;
    private IpAddress ipAddress;
    private Location phyLocation;
    private LogicLocation logicalPort;
    private MacAddress macAddress;
    private IpAddress publicIpAddress;
    private Uuid epYangUuid;
    private FabricId fabricId;

    public EndpointAttachInfo() {
    }

    public IpAddress getGatewayIpAddr() {
        return gatewayIpAddr;
    }

    public void setGatewayIpAddr(IpAddress gatewayIpAddr) {
        this.gatewayIpAddr = gatewayIpAddr;
    }

    public IpAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(IpAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Location getPhyLocation() {
        return phyLocation;
    }

    public void setPhyLocation(Location phyLocation) {
        this.phyLocation = phyLocation;
    }

    public LogicLocation getLogicalPort() {
        return logicalPort;
    }

    public void setLogicalPort(LogicLocation logicalPort) {
        this.logicalPort = logicalPort;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    public IpAddress getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(IpAddress publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public Uuid getEpYangUuid() {
        return epYangUuid;
    }

    public void setEpYangUuid(Uuid epYangUuid) {
        this.epYangUuid = epYangUuid;
    }

    public FabricId getFabricId() {
        return fabricId;
    }

    public void setFabricId(FabricId fabricId) {
        this.fabricId = fabricId;
    }

}
