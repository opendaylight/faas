/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.uln.datastore.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.EdgeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.EndpointsLocations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.networks.rev151013.TenantLogicalNetworks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.networks.rev151013.tenant.logical.networks.TenantLogicalNetwork;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.networks.rev151013.tenant.logical.networks.TenantLogicalNetworkKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.LogicalRouters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.LogicalSwitches;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitchKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.PortKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.groups.container.SecurityGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.groups.container.security.groups.SecurityGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.groups.container.security.groups.SecurityGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.Subnets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.SubnetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class UlnIidFactory {

    /*
     * Subnet
     */
    public static InstanceIdentifier<Subnet> subnetIid() {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class).child(Subnets.class).child(Subnet.class).build();
    }

    public static InstanceIdentifier<Subnet> subnetIid(Uuid tenantId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(Subnets.class)
                .child(Subnet.class).build();
    }

    public static InstanceIdentifier<Subnet> subnetIid(Uuid tenantId, Uuid subnetId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(Subnets.class)
                .child(Subnet.class, new SubnetKey(subnetId)).build();
    }

    /*
     * Switch
     */
    public static InstanceIdentifier<LogicalSwitch> logicalSwitchIid() {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class).child(LogicalSwitches.class).child(LogicalSwitch.class).build();
    }

    public static InstanceIdentifier<LogicalSwitch> logicalSwitchIid(Uuid tenantId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(LogicalSwitches.class)
                .child(LogicalSwitch.class).build();
    }

    public static InstanceIdentifier<LogicalSwitch> logicalSwitchIid(Uuid tenantId, Uuid switchId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(LogicalSwitches.class)
                .child(LogicalSwitch.class, new LogicalSwitchKey(switchId)).build();
    }

    /*
     * Router
     */
    public static InstanceIdentifier<LogicalRouter> logicalRouterIid() {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class).child(LogicalRouters.class).child(LogicalRouter.class).build();
    }

    public static InstanceIdentifier<LogicalRouter> logicalRouterIid(Uuid tenantId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(LogicalRouters.class)
                .child(LogicalRouter.class).build();
    }

    public static InstanceIdentifier<LogicalRouter> logicalRouterIid(Uuid tenantId, Uuid routerId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(LogicalRouters.class)
                .child(LogicalRouter.class, new LogicalRouterKey(routerId)).build();
    }

    /*
     * Security Rules
     */
    public static InstanceIdentifier<SecurityGroup> securityGroupIid() {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class).child(SecurityGroups.class).child(SecurityGroup.class).build();
    }

    public static InstanceIdentifier<SecurityGroup> securityGroupIid(Uuid tenantId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(SecurityGroups.class)
                .child(SecurityGroup.class).build();
    }

    public static InstanceIdentifier<SecurityGroup> securityGroupIid(Uuid tenantId, Uuid securityGroupId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(SecurityGroups.class)
                .child(SecurityGroup.class, new SecurityGroupKey(securityGroupId)).build();
    }

    /*
     * Port
     */
    public static InstanceIdentifier<Port> portIid() {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class).child(Ports.class).child(Port.class).build();
    }

    public static InstanceIdentifier<Port> portIid(Uuid tenantId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(Ports.class)
                .child(Port.class).build();
    }

    public static InstanceIdentifier<Port> portIid(Uuid tenantId, Uuid portId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(Ports.class)
                .child(Port.class, new PortKey(portId)).build();
    }

    /*
     * Edge
     */
    public static InstanceIdentifier<Edge> edgeIid() {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class).child(Edges.class).child(Edge.class).build();
    }

    public static InstanceIdentifier<Edge> edgeIid(Uuid tenantId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(Edges.class)
                .child(Edge.class).build();
    }

    public static InstanceIdentifier<Edge> edgeIid(Uuid tenantId, Uuid edgeId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(Edges.class)
                .child(Edge.class, new EdgeKey(edgeId)).build();
    }

    /*
     * Endpoint Location
     */
    public static InstanceIdentifier<EndpointLocation> endpointLocationIid() {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class).child(EndpointsLocations.class).child(EndpointLocation.class).build();
    }

    public static InstanceIdentifier<EndpointLocation> endpointLocationIid(Uuid tenantId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(EndpointsLocations.class)
                .child(EndpointLocation.class).build();
    }

    public static InstanceIdentifier<EndpointLocation> endpointLocationIid(Uuid tenantId, Uuid endpointLocationId) {
        return InstanceIdentifier.builder(TenantLogicalNetworks.class).child(TenantLogicalNetwork.class, new TenantLogicalNetworkKey(tenantId)).child(EndpointsLocations.class)
                .child(EndpointLocation.class, new EndpointLocationKey(endpointLocationId)).build();
    }
}