/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.uln.datastore.api;

import com.google.common.base.Optional;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.RegisterEndpointLocationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.networks.rev151013.tenant.logical.networks.TenantLogicalNetwork;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.PortLocationAttributes.LocationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.port.PrivateIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @deprecated use {@link UlnDatastoreUtil} instead
 */
@Deprecated
public class UlnDatastoreApi {

    private static UlnDatastoreUtil ulnDatastoreUtil;

    public static void setDataBroker(DataBroker dataBroker) {
        UlnDatastoreApi.ulnDatastoreUtil = new UlnDatastoreUtil(dataBroker);
    }

    /*
     * Logical Network and individual elements Read Methods
     */
    public static TenantLogicalNetwork readTenantLogicalNetworkFromDs(Uuid tenantId) {
        return ulnDatastoreUtil.readTenantLogicalNetworkFromDs(tenantId);
    }

    public static TenantLogicalNetwork readTenantsLogicalNetworksFromDs() {
        return ulnDatastoreUtil.readTenantsLogicalNetworksFromDs();
    }

    public static Subnet readSubnetFromDs(Uuid tenantId, Uuid subnetId) {
        return ulnDatastoreUtil.readSubnetFromDs(tenantId, subnetId);
    }

    public static LogicalRouter readLogicalRouterFromDs(Uuid tenantId, Uuid routerId) {
        return ulnDatastoreUtil.readLogicalRouterFromDs(tenantId, routerId);
    }

    public static LogicalSwitch readLogicalSwitchFromDs(Uuid tenantId, Uuid switchId) {
        return ulnDatastoreUtil.readLogicalSwitchFromDs(tenantId, switchId);
    }

    public static SecurityRuleGroups readSecurityGroupsFromDs(Uuid tenantId, Uuid securityGroupId) {
        return ulnDatastoreUtil.readSecurityGroupsFromDs(tenantId, securityGroupId);
    }

    public static Port readPortFromDs(Uuid tenantId, Uuid portId) {
        return ulnDatastoreUtil.readPortFromDs(tenantId, portId);
    }

    public static Edge readEdgeFromDs(Uuid tenantId, Uuid edgeId) {
        return ulnDatastoreUtil.readEdgeFromDs(tenantId, edgeId);
    }

    public static EndpointLocation readEndpointLocationFromDs(Uuid tenantId, Uuid endpointLocationId) {
        return ulnDatastoreUtil.readEndpointLocationFromDs(tenantId, endpointLocationId);
    }

    /*
     * Logical Network and individual elements Submit Methods
     */
    public static void submitSubnetToDs(Subnet subnet) {
        ulnDatastoreUtil.submitSubnetToDs(subnet);
    }

    public static void submitLogicalRouterToDs(LogicalRouter router) {
        ulnDatastoreUtil.submitLogicalRouterToDs(router);
    }

    public static void submitLogicalRouterToDs(LogicalRouter newRouter, boolean updateAndMergeRefs) {
        ulnDatastoreUtil.submitLogicalRouterToDs(newRouter, updateAndMergeRefs);
    }

    public static void submitLogicalSwitchToDs(LogicalSwitch newSwitch) {
        ulnDatastoreUtil.submitLogicalSwitchToDs(newSwitch);
    }

    public static void submitSecurityGroupsToDs(SecurityRuleGroups newSecurityGroups) {
        ulnDatastoreUtil.submitSecurityGroupsToDs(newSecurityGroups);
    }

    public static void submitPortToDs(Port port) {
        ulnDatastoreUtil.submitPortToDs(port);
    }

    public static void submitEdgeToDs(Edge edge) {
        ulnDatastoreUtil.submitEdgeToDs(edge);
    }

    public static void submitEndpointLocationToDs(EndpointLocation endpointLocation) {
        ulnDatastoreUtil.submitEndpointLocationToDs(endpointLocation);
    }

    /*
     * Logical Network and individual elements Remove Methods
     */
    public static void removeTenantFromDsIfExists(Uuid tenantId) {
        ulnDatastoreUtil.removeTenantFromDsIfExists(tenantId);
    }

    public static void removeSubnetFromDsIfExists(Uuid tenantId, Uuid subnetId) {
        ulnDatastoreUtil.removeSubnetFromDsIfExists(tenantId, subnetId);
    }

    public static void removeLogicalRouterFromDsIfExists(Uuid tenantId, Uuid routerId) {
        ulnDatastoreUtil.removeLogicalRouterFromDsIfExists(tenantId, routerId);
    }

    public static void removeLogicalSwitchFromDsIfExists(Uuid tenantId, Uuid switchId) {
        ulnDatastoreUtil.removeLogicalSwitchFromDsIfExists(tenantId, switchId);
    }

    public static void removeSecurityGroupsFromDsIfExists(Uuid tenantId, Uuid securityGroupId) {
        ulnDatastoreUtil.removeSecurityGroupsFromDsIfExists(tenantId, securityGroupId);
    }

    public static void removePortFromDsIfExists(Uuid tenantId, Uuid portId) {
        ulnDatastoreUtil.removePortFromDsIfExists(tenantId, portId);
    }

    public static void removeEdgeFromDsIfExists(Uuid tenantId, Uuid edgeId) {
        ulnDatastoreUtil.removeEdgeFromDsIfExists(tenantId, edgeId);
    }

    public static void removeEndpointLocationFromDsIfExists(Uuid tenantId, Uuid endpointLocationId) {
        ulnDatastoreUtil.removeEndpointLocationFromDsIfExists(tenantId, endpointLocationId);
    }

    /*
     * Methods to join logical network elements together
     */

    public static boolean attachEndpointToSubnet(EndpointLocationBuilder epLocBuilder, Uuid faasSubnetId,
            MacAddress macAddress, List<PrivateIps> privateIpAddresses, List<IpAddress> publicIpAddresses) {
        return ulnDatastoreUtil.attachEndpointToSubnet(epLocBuilder, faasSubnetId, macAddress, privateIpAddresses,
                publicIpAddresses);
    }

    public static boolean attachEndpointToSubnet(RegisterEndpointLocationInput input) {
        return ulnDatastoreUtil.attachEndpointToSubnet(input);
    }

    public static boolean attachAndSubmitToDs(Uuid firstId, Uuid secondId, Uuid tenantId,
            Pair<LocationType, LocationType> nodeTypes) {
        return ulnDatastoreUtil.attachAndSubmitToDs(firstId, secondId, tenantId, nodeTypes);
    }

    public static boolean attachAndSubmitToDs(Uuid firstId, Uuid secondId, Uuid tenantId,
            Pair<LocationType, LocationType> nodeTypes, Pair<Uuid, Uuid> privateSecGroupRules,
            Pair<Uuid, Uuid> publicSecGroupRules) {
        return ulnDatastoreUtil.attachAndSubmitToDs(firstId, secondId, tenantId, nodeTypes, privateSecGroupRules,
                publicSecGroupRules);
    }

    public static boolean attachAndSubmitToDs(Object first, Object second) {
        return ulnDatastoreUtil.attachAndSubmitToDs(first, second);
    }

    public static boolean attachAndSubmitToDs(Object first, Object second, Pair<Uuid, Uuid> privateSecGroupRules) {
        return ulnDatastoreUtil.attachAndSubmitToDs(first, second, privateSecGroupRules);
    }

    public static boolean attachAndSubmitToDs(Object first, Object second, Pair<Uuid, Uuid> privateSecGroupRules,
            Pair<Uuid, Uuid> pubSecGroupRules) {
        return ulnDatastoreUtil.attachAndSubmitToDs(first, second, privateSecGroupRules, pubSecGroupRules);
    }

    /*
     * General & Common helper methods
     */
    public static <T extends DataObject> Optional<T> readFromDs(InstanceIdentifier<T> path, ReadTransaction rTx) {
        return ulnDatastoreUtil.readFromDs(path, rTx);
    }

    public static boolean submitToDs(WriteTransaction wTx) {
        return ulnDatastoreUtil.submitToDs(wTx);
    }

    public static <T extends DataObject> Optional<T> removeIfExists(InstanceIdentifier<T> path) {
        return ulnDatastoreUtil.removeIfExists(path);
    }
}
