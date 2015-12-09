/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.uln.datastore.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.faas.uln.manager.UlnMapperDatastoreDependency;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.EdgeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.networks.rev151013.tenant.logical.networks.TenantLogicalNetwork;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.PortLocationAttributes.LocationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.port.PrivateIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.SubnetBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

public class UlnDatastoreApi {

    private static final Logger LOG = LoggerFactory.getLogger(UlnDatastoreApi.class);
    private static final LogicalDatastoreType logicalDatastoreType = LogicalDatastoreType.OPERATIONAL;

    /*
     * Logical Network and individual elements Read Methods
     */
    public static TenantLogicalNetwork readTenantLogicalNetworkFromDs(Uuid tenantId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<TenantLogicalNetwork> potentialSubnet = readFromDs(UlnIidFactory.tenantLogicalNetworkIid(tenantId), t);
        if (!potentialSubnet.isPresent()) {
            LOG.info("Tenant {} Logical Network does not exist.", tenantId.getValue());
            return null;
        }
        return potentialSubnet.get();
    }

    public static TenantLogicalNetwork readTenantsLogicalNetworksFromDs() {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<TenantLogicalNetwork> potentialSubnet = readFromDs(UlnIidFactory.tenantsLogicalNetworksIid(), t);
        if (!potentialSubnet.isPresent()) {
            LOG.info("No Tenants Logical Networks exist in Datastore.");
            return null;
        }
        return potentialSubnet.get();
    }

    public static Subnet readSubnetFromDs(Uuid tenantId, Uuid subnetId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<Subnet> potentialSubnet = readFromDs(UlnIidFactory.subnetIid(tenantId, subnetId), t);
        if (!potentialSubnet.isPresent()) {
            LOG.info("Logical Subnet {} does not exist in tenant {}.", subnetId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialSubnet.get();
    }

    public static LogicalRouter readLogicalRouterFromDs(Uuid tenantId, Uuid routerId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<LogicalRouter> potentialRouter = readFromDs(UlnIidFactory.logicalRouterIid(tenantId, routerId), t);
        if (!potentialRouter.isPresent()) {
            LOG.info("Logical Router {} does not exist in tenant {}.", routerId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialRouter.get();
    }

    public static LogicalSwitch readLogicalSwitchFromDs(Uuid tenantId, Uuid switchId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<LogicalSwitch> potentialSwitch = readFromDs(UlnIidFactory.logicalSwitchIid(tenantId, switchId), t);
        if (!potentialSwitch.isPresent()) {
            LOG.info("Logical Switch {} does not exist in tenant {}.", switchId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialSwitch.get();
    }

    public static SecurityRuleGroups readSecurityGroupsFromDs(Uuid tenantId, Uuid securityGroupId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<SecurityRuleGroups> potentialSecurityGroup = readFromDs(
                UlnIidFactory.securityGroupsIid(tenantId, securityGroupId), t);
        if (!potentialSecurityGroup.isPresent()) {
            LOG.info("Logical SecurityGroup {} does not exist in tenant {}.", securityGroupId.getValue(),
                    tenantId.getValue());
            return null;
        }
        return potentialSecurityGroup.get();
    }

    public static Port readPortFromDs(Uuid tenantId, Uuid portId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<Port> potentialPort = readFromDs(UlnIidFactory.portIid(tenantId, portId), t);
        if (!potentialPort.isPresent()) {
            LOG.info("Logical Port {} does not exist in tenant {}.", portId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialPort.get();
    }

    public static Edge readEdgeFromDs(Uuid tenantId, Uuid edgeId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<Edge> potentialEdge = readFromDs(UlnIidFactory.edgeIid(tenantId, edgeId), t);
        if (!potentialEdge.isPresent()) {
            LOG.info("Logical Edge {} does not exist in tenant {}.", edgeId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialEdge.get();
    }

    public static EndpointLocation readEndpointLocationFromDs(Uuid tenantId, Uuid endpointLocationId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<EndpointLocation> potentialEndpointLocation = readFromDs(
                UlnIidFactory.endpointLocationIid(tenantId, endpointLocationId), t);
        if (!potentialEndpointLocation.isPresent()) {
            LOG.info("Logical EndpointLocation {} does not exist in tenant {}.", endpointLocationId.getValue(),
                    tenantId.getValue());
            return null;
        }
        return potentialEndpointLocation.get();
    }

    /*
     * Logical Network and individual elements Submit Methods
     */
    public static void submitSubnetToDs(Subnet subnet) {
        submitSubnetToDs(subnet, true);
    }

    private static void submitSubnetToDs(Subnet newSubnet, boolean updateAndMergeRefs) {
        /*
         * Make sure we don't overwrite certain existing links
         */
        Subnet updatedSubnet = newSubnet;
        if (updateAndMergeRefs) {
            Subnet dsSubnet = readSubnetFromDs(newSubnet.getTenantId(), newSubnet.getUuid());
            if (dsSubnet != null) {
                SubnetBuilder builder = new SubnetBuilder(newSubnet);
                builder.setPort(merge(dsSubnet.getPort(), newSubnet.getPort()));
                updatedSubnet = builder.build();
            }
        }
        /*
         * Write to DS
         */
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.subnetIid(updatedSubnet.getTenantId(), updatedSubnet.getUuid()),
                updatedSubnet, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical subnet {} to datastore.", updatedSubnet.getUuid().getValue());
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (updatedSubnet.getPort() != null && updateAndMergeRefs) {
                for (Uuid portId : updatedSubnet.getPort()) {
                    Port port = UlnDatastoreApi.readPortFromDs(updatedSubnet.getTenantId(), portId);
                    if ((port != null)
                            && (!updatedSubnet.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.SubnetType)) {
                        PortBuilder builder = new PortBuilder(port);
                        builder.setLocationId(updatedSubnet.getUuid());
                        builder.setLocationType(LocationType.SubnetType);
                        UlnDatastoreApi.submitPortToDs(builder.build(), false);
                    }
                }
            }
        } else {
            LOG.error("Failed to write logical subnet {} to datastore.", updatedSubnet.getUuid().getValue());
        }
    }

    public static void submitLogicalRouterToDs(LogicalRouter router) {
        submitLogicalRouterToDs(router, true);
    }

    public static void submitLogicalRouterToDs(LogicalRouter newRouter, boolean updateAndMergeRefs) {
        /*
         * Make sure we don't overwrite certain existing links
         */
        LogicalRouter updatedRouter = newRouter;
        if (updateAndMergeRefs) {
            LogicalRouter dsRouter = readLogicalRouterFromDs(newRouter.getTenantId(), newRouter.getUuid());
            if (dsRouter != null) {
                LogicalRouterBuilder builder = new LogicalRouterBuilder(newRouter);
                builder.setPort(merge(dsRouter.getPort(), newRouter.getPort()));
                updatedRouter = builder.build();
            }
        }
        /*
         * Write to DS
         */
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType,
                UlnIidFactory.logicalRouterIid(updatedRouter.getTenantId(), updatedRouter.getUuid()), updatedRouter,
                true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical router {} to datastore.", updatedRouter.getUuid().getValue());
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (updatedRouter.getPort() != null && updateAndMergeRefs) {
                for (Uuid portId : updatedRouter.getPort()) {
                    Port port = UlnDatastoreApi.readPortFromDs(updatedRouter.getTenantId(), portId);
                    if ((port != null)
                            && (!updatedRouter.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.RouterType)) {
                        PortBuilder builder = new PortBuilder(port);
                        builder.setLocationId(updatedRouter.getUuid());
                        builder.setLocationType(LocationType.RouterType);
                        UlnDatastoreApi.submitPortToDs(builder.build(), false);
                    }
                }
            }
        } else {
            LOG.error("Failed to write logical router {} to datastore.", updatedRouter.getUuid().getValue());
        }
    }

    public static void submitLogicalSwitchToDs(LogicalSwitch newSwitch) {
        submitLogicalSwitchToDs(newSwitch, true);
    }

    private static void submitLogicalSwitchToDs(LogicalSwitch newSwitch, boolean updateAndMergeRefs) {
        /*
         * Make sure we don't overwrite certain existing links
         */
        LogicalSwitch updatedSwitch = newSwitch;
        if (updateAndMergeRefs) {
            LogicalSwitch dsSwitch = readLogicalSwitchFromDs(newSwitch.getTenantId(), newSwitch.getUuid());
            if (dsSwitch != null) {
                LogicalSwitchBuilder builder = new LogicalSwitchBuilder(newSwitch);
                builder.setPort(merge(dsSwitch.getPort(), newSwitch.getPort()));
                updatedSwitch = builder.build();
            }
        }
        /*
         * Write to DS
         */
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType,
                UlnIidFactory.logicalSwitchIid(updatedSwitch.getTenantId(), updatedSwitch.getUuid()), updatedSwitch,
                true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical switch {} to datastore.", updatedSwitch.getUuid().getValue());
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (updatedSwitch.getPort() != null && updateAndMergeRefs) {
                for (Uuid portId : updatedSwitch.getPort()) {
                    Port port = UlnDatastoreApi.readPortFromDs(updatedSwitch.getTenantId(), portId);
                    if ((port != null)
                            && (!updatedSwitch.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.SwitchType)) {
                        PortBuilder builder = new PortBuilder(port);
                        builder.setLocationId(updatedSwitch.getUuid());
                        builder.setLocationType(LocationType.SwitchType);
                        UlnDatastoreApi.submitPortToDs(builder.build(), false);
                    }
                }
            }
        } else {
            LOG.error("Failed to write logical switch {} to datastore.", updatedSwitch.getUuid().getValue());
        }
    }

    public static void submitSecurityGroupsToDs(SecurityRuleGroups newSecurityGroups) {
        submitSecurityGroupsToDs(newSecurityGroups, true);
    }

    private static void submitSecurityGroupsToDs(SecurityRuleGroups newSecurityGroups, boolean updateAndMergeRefs) {
        /*
         * Make sure we don't overwrite certain existing links
         */
        SecurityRuleGroups updatedSecurityGroups = newSecurityGroups;
        if (updateAndMergeRefs) {
            SecurityRuleGroups dsSecurityGroups = readSecurityGroupsFromDs(newSecurityGroups.getTenantId(),
                    newSecurityGroups.getUuid());
            if (dsSecurityGroups != null) {
                SecurityRuleGroupsBuilder builder = new SecurityRuleGroupsBuilder(newSecurityGroups);
                builder.setPorts(merge(dsSecurityGroups.getPorts(), newSecurityGroups.getPorts()));
                updatedSecurityGroups = builder.build();
            }
        }

        /*
         * Submit rules to datastore
         */
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType,
                UlnIidFactory.securityGroupsIid(updatedSecurityGroups.getTenantId(), updatedSecurityGroups.getUuid()),
                updatedSecurityGroups, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical securityGroups {} to datastore.", updatedSecurityGroups.getUuid().getValue());
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (updatedSecurityGroups.getPorts() != null && updateAndMergeRefs) {
                for (Uuid portId : updatedSecurityGroups.getPorts()) {
                    Port port = UlnDatastoreApi.readPortFromDs(updatedSecurityGroups.getTenantId(), portId);
                    if (port != null) {
                        Set<Uuid> set = new HashSet<>();
                        if (port.getSecurityRulesGroups() != null) {
                            set.addAll(port.getSecurityRulesGroups());
                        }
                        if (!set.contains(updatedSecurityGroups.getUuid())) {
                            set.add(updatedSecurityGroups.getUuid());
                            PortBuilder builder = new PortBuilder(port);
                            builder.setSecurityRulesGroups(new ArrayList<>(set));
                            UlnDatastoreApi.submitPortToDs(builder.build(), false);
                        }
                    }
                }
            }
        } else {
            LOG.error("Failed to write logical securityGroups {} to datastore.", updatedSecurityGroups.getUuid()
                .getValue());
        }
    }

    public static void submitPortToDs(Port port) {
        submitPortToDs(port, true);
    }

    private static void submitPortToDs(Port newPort, boolean updateAndMergeRefs) {
        if (newPort.getLocationId() == null || newPort.getLocationType() == null) {
            LOG.error("Trying to submit Port {} without associating it with a logical node -- Ignored Request",
                    newPort.getUuid().getValue());
            return;
        }
        /*
         * Make sure we don't overwrite certain existing links
         */
        Port updatedPort = newPort;
        if (updateAndMergeRefs) {
            Port dsPort = readPortFromDs(newPort.getTenantId(), newPort.getUuid());
            if (dsPort != null) {
                PortBuilder builder = new PortBuilder(newPort);
                builder.setSecurityRulesGroups(merge(dsPort.getSecurityRulesGroups(), newPort.getSecurityRulesGroups()));
                updatedPort = builder.build();
            }
        }
        /*
         * Write to data store
         */
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.portIid(newPort.getTenantId(), newPort.getUuid()), newPort, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical port {} to datastore.", newPort.getUuid().getValue());
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (updateAndMergeRefs) {
                // update security rules
                if (updatedPort.getSecurityRulesGroups() != null) {
                    for (Uuid secGrpId : updatedPort.getSecurityRulesGroups()) {
                        SecurityRuleGroups secGrp = UlnDatastoreApi.readSecurityGroupsFromDs(updatedPort.getTenantId(),
                                secGrpId);
                        if (secGrp != null) {
                            Set<Uuid> set = new HashSet<>();
                            if (secGrp.getPorts() != null) {
                                set.addAll(secGrp.getPorts());
                            }
                            if (!set.contains(updatedPort.getUuid())) {
                                set.add(updatedPort.getUuid());
                                SecurityRuleGroupsBuilder builder = new SecurityRuleGroupsBuilder(secGrp);
                                builder.setPorts(new ArrayList<>(set));
                                UlnDatastoreApi.submitSecurityGroupsToDs(builder.build(), false);
                            }
                        }
                    }
                }

                // update edge
                if (updatedPort.getEdgeId() != null) {
                    Edge edge = UlnDatastoreApi.readEdgeFromDs(updatedPort.getTenantId(), updatedPort.getEdgeId());
                    if (edge != null) {
                        if (!updatedPort.getEdgeId().equals(edge.getLeftPortId())
                                && !updatedPort.getEdgeId().equals(edge.getRightPortId())) {
                            PortBuilder builder = new PortBuilder(updatedPort);
                            builder.setEdgeId(null);
                            submitPortToDs(builder.build(), false);
                            LOG.warn("Removed incorrect reference to edge {} from port {}", builder.getEdgeId()
                                .getValue(), builder.getUuid().getValue());
                        }

                    }
                }
            }
        } else {
            LOG.error("Failed to write logical port {} to datastore.", updatedPort.getUuid().getValue());
        }
    }

    public static void submitEdgeToDs(Edge edge) {
        if (edge.getLeftPortId() == null || edge.getRightPortId() == null) {
            LOG.error("Trying to Subnit an edge Edge with less than two ports -- Ignored Request");
            return;
        }
        Port lport = UlnDatastoreApi.readPortFromDs(edge.getTenantId(), edge.getLeftPortId());
        if (lport != null && lport.getEdgeId() != null && !edge.getUuid().equals(lport.getEdgeId())) {
            LOG.error(
                    "Trying to Submit Edge {} that references Port {}, but that Port already references Edge {}. Ignored Request.",
                    edge.getUuid().getValue(), lport.getUuid().getValue(), lport.getEdgeId().getValue());
            return;
        }
        Port rport = UlnDatastoreApi.readPortFromDs(edge.getTenantId(), edge.getRightPortId());
        if (rport != null && rport.getEdgeId() != null && !edge.getUuid().equals(rport.getEdgeId())) {
            LOG.error(
                    "Trying to Submit Edge {} that references Port {}, but that Port already references Edge {}. Ignored Request.",
                    edge.getUuid().getValue(), rport.getUuid().getValue(), rport.getEdgeId().getValue());
            return;
        }
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.edgeIid(edge.getTenantId(), edge.getUuid()), edge, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical edge {} to datastore.", edge.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical edge {} to datastore.", edge.getUuid().getValue());
        }
    }

    public static void submitEndpointLocationToDs(EndpointLocation endpointLocation) {
        submitEndpointLocationToDs(endpointLocation, true);
    }

    private static void submitEndpointLocationToDs(EndpointLocation newEndpointLocation, boolean updateAndMergeRefs) {
        /*
         * Make sure we don't overwrite certain existing links
         */
        EndpointLocation updatedEndpointLocation = newEndpointLocation;
        if (updateAndMergeRefs && newEndpointLocation.getPort() == null) {
            EndpointLocation dsEndpointLocation = readEndpointLocationFromDs(newEndpointLocation.getTenantId(),
                    newEndpointLocation.getUuid());
            if (dsEndpointLocation != null) {
                EndpointLocationBuilder builder = new EndpointLocationBuilder(newEndpointLocation);
                builder.setPort(dsEndpointLocation.getPort());
                updatedEndpointLocation = builder.build();
            }
        }
        if (updatedEndpointLocation.getPort() == null) {
            LOG.error("Endpoint Location {} has no port", updatedEndpointLocation);
            return;
        }
        /*
         * Write to DS
         */
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType,
                UlnIidFactory.endpointLocationIid(updatedEndpointLocation.getTenantId(),
                        updatedEndpointLocation.getUuid()), updatedEndpointLocation, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical endpointLocation {} to datastore.", updatedEndpointLocation.getUuid().getValue());
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (updatedEndpointLocation.getPort() != null && updateAndMergeRefs) {
                Port port = UlnDatastoreApi.readPortFromDs(updatedEndpointLocation.getTenantId(),
                        updatedEndpointLocation.getPort());
                if ((port != null)
                        && (!updatedEndpointLocation.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.EndpointType)) {
                    PortBuilder builder = new PortBuilder(port);
                    builder.setLocationId(updatedEndpointLocation.getUuid());
                    builder.setLocationType(LocationType.EndpointType);
                    UlnDatastoreApi.submitPortToDs(builder.build(), false);
                }

            }
        } else {
            LOG.error("Failed to write logical endpointLocation {} to datastore.", updatedEndpointLocation.getUuid()
                .getValue());
        }
    }

    /*
     * Logical Network and individual elements Remove Methods
     */
    public static void removeTenantFromDsIfExists(Uuid tenantId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<TenantLogicalNetwork> tenantULnOp = removeIfExists(UlnIidFactory.tenantLogicalNetworkIid(tenantId), t);
        if (tenantULnOp.isPresent()) {
            LOG.info("Removed Tenant {}", tenantId.getValue());
        } else {
            LOG.debug("Tenant {} wasn't found", tenantId.getValue());
        }
    }

    public static void removeSubnetFromDsIfExists(Uuid tenantId, Uuid subnetId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<Subnet> oldOptional = removeIfExists(UlnIidFactory.subnetIid(tenantId, subnetId), t);
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOptional.isPresent()) {
            Subnet subnet = oldOptional.get();
            if (subnet.getPort() != null) {
                for (Uuid port : subnet.getPort()) {
                    removePortFromDsIfExists(tenantId, port);
                }
            }
        }
    }

    public static void removeLogicalRouterFromDsIfExists(Uuid tenantId, Uuid routerId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<LogicalRouter> oldOptional = removeIfExists(UlnIidFactory.logicalRouterIid(tenantId, routerId), t);
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOptional.isPresent()) {
            LogicalRouter router = oldOptional.get();
            if (router.getPort() != null) {
                for (Uuid port : router.getPort()) {
                    removePortFromDsIfExists(tenantId, port);
                }
            }
        }
    }

    public static void removeLogicalSwitchFromDsIfExists(Uuid tenantId, Uuid switchId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<LogicalSwitch> oldOptional = removeIfExists(UlnIidFactory.logicalSwitchIid(tenantId, switchId), t);
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOptional.isPresent()) {
            LogicalSwitch lSwitch = oldOptional.get();
            if (lSwitch.getPort() != null) {
                for (Uuid port : lSwitch.getPort()) {
                    removePortFromDsIfExists(tenantId, port);
                }
            }
        }
    }

    public static void removeSecurityGroupsFromDsIfExists(Uuid tenantId, Uuid securityGroupId) {
        removeSecurityGroupsFromDsIfExists(tenantId, securityGroupId, true);
    }

    private static void removeSecurityGroupsFromDsIfExists(Uuid tenantId, Uuid securityGroupId,
            boolean updateExistingRefs) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<SecurityRuleGroups> oldOption = removeIfExists(
                UlnIidFactory.securityGroupsIid(tenantId, securityGroupId), t);
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOption.isPresent() && updateExistingRefs) {
            SecurityRuleGroups securityRuleGroups = oldOption.get();
            if (securityRuleGroups.getPorts() != null) {
                for (Uuid portId : securityRuleGroups.getPorts()) {
                    Port port = UlnDatastoreApi.readPortFromDs(securityRuleGroups.getTenantId(), portId);
                    if (port != null && port.getSecurityRulesGroups() != null) {
                        Set<Uuid> set = new HashSet<>(port.getSecurityRulesGroups());
                        if (set.remove(securityRuleGroups.getUuid())) {
                            PortBuilder builder = new PortBuilder(port);
                            builder.setSecurityRulesGroups(new ArrayList<>(set));
                            UlnDatastoreApi.submitPortToDs(builder.build(), false);
                        }
                    }
                }
            }
        }
    }

    public static void removePortFromDsIfExists(Uuid tenantId, Uuid portId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<Port> oldOption = removeIfExists(UlnIidFactory.portIid(tenantId, portId), t);
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOption.isPresent()) {
            Port port = oldOption.get();
            // update security groups
            if (port.getSecurityRulesGroups() != null) {
                for (Uuid sId : port.getSecurityRulesGroups()) {
                    SecurityRuleGroups secGrps = UlnDatastoreApi.readSecurityGroupsFromDs(tenantId, sId);
                    if (secGrps != null && secGrps.getPorts() != null) {
                        Set<Uuid> set = new HashSet<>(secGrps.getPorts());
                        if (set.remove(port.getUuid())) {
                            SecurityRuleGroupsBuilder builder = new SecurityRuleGroupsBuilder(secGrps);
                            builder.setPorts(new ArrayList<>(set));
                            UlnDatastoreApi.submitSecurityGroupsToDs(builder.build(), false);
                        }
                    }
                }
            }
            // remove edge
            UlnDatastoreApi.removeEdgeFromDsIfExists(tenantId, port.getEdgeId());

            // update node
            if (port.getLocationId() != null) {
                if (port.getLocationType() == LocationType.EndpointType) {
                    removeEndpointLocationFromDsIfExists(tenantId, port.getLocationId());
                }
            }
        }
    }

    public static void removeEdgeFromDsIfExists(Uuid tenantId, Uuid edgeId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<Edge> oldOptional = removeIfExists(UlnIidFactory.edgeIid(tenantId, edgeId), t);
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOptional.isPresent()) {
            Edge edge = oldOptional.get();
            if (edge.getLeftPortId() != null) {
                removePortFromDsIfExists(tenantId, edge.getLeftPortId());
            }
            if (edge.getRightPortId() != null) {
                removePortFromDsIfExists(tenantId, edge.getRightPortId());
            }
        }
    }

    public static void removeEndpointLocationFromDsIfExists(Uuid tenantId, Uuid endpointLocationId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        /*
         * Make sure other logical network nodes links are updated as well
         */
        Optional<EndpointLocation> oldOptional = removeIfExists(
                UlnIidFactory.endpointLocationIid(tenantId, endpointLocationId), t);
        if (oldOptional.isPresent()) {
            EndpointLocation epLoc = oldOptional.get();
            if (epLoc.getPort() != null) {
                removePortFromDsIfExists(tenantId, epLoc.getPort());
            }
        }
    }

    /*
     * Methods to join logical network elements together
     */

    public static boolean attachEndpointToSubnetAndSubmitToDs(EndpointLocationBuilder epLocBuilder, Uuid faasSubnetId,
            MacAddress macAddress, List<PrivateIps> privateIpAddresses, List<IpAddress> publicIpAddresses) {
        synchronized (UlnDatastoreApi.class) {
            // remove endpoint location, if exists, to handle the case of moving endpoints
            removeEndpointLocationFromDsIfExists(epLocBuilder.getTenantId(), epLocBuilder.getUuid());
            Uuid epLocPortId = new Uuid(UUID.randomUUID().toString());
            epLocBuilder.setPort(epLocPortId);
            PortBuilder epLocPortbuilder = new PortBuilder();
            epLocPortbuilder.setUuid(epLocPortId);
            epLocPortbuilder.setAdminStateUp(true);
            epLocPortbuilder.setLocationId(epLocBuilder.getUuid());
            epLocPortbuilder.setLocationType(LocationType.EndpointType);
            epLocPortbuilder.setTenantId(epLocBuilder.getTenantId());
            epLocPortbuilder.setMacAddress(macAddress);
            epLocPortbuilder.setPrivateIps(privateIpAddresses);
            epLocPortbuilder.setPublicIps(publicIpAddresses);

            Uuid subnetPortId = new Uuid(UUID.randomUUID().toString());
            PortBuilder subnetPortbuilder = new PortBuilder();
            subnetPortbuilder.setUuid(subnetPortId);
            subnetPortbuilder.setAdminStateUp(true);
            subnetPortbuilder.setLocationId(faasSubnetId);
            subnetPortbuilder.setLocationType(LocationType.SubnetType);
            subnetPortbuilder.setTenantId(epLocBuilder.getTenantId());

            Subnet subnet = readSubnetFromDs(epLocBuilder.getTenantId(), faasSubnetId);
            if (subnet == null) {
                LOG.error("Failed to attach endpoint -- unable to find subnet {} in tenant {}", faasSubnetId,
                        epLocBuilder.getTenantId());
                return false;
            }

            SubnetBuilder subnetBuilder = new SubnetBuilder(subnet);
            List<Uuid> ports = new ArrayList<>();
            ports.add(subnetPortId);
            subnetBuilder.setPort(merge(subnetBuilder.getPort(), ports));

            List<Pair<Port, Port>> portLinks = new ArrayList<>();
            portLinks.add(new Pair<>(epLocPortbuilder.build(), subnetPortbuilder.build()));

            WriteTransaction wTx = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
            wTx.put(logicalDatastoreType,
                    UlnIidFactory.subnetIid(subnetBuilder.getTenantId(), subnetBuilder.getUuid()),
                    subnetBuilder.build(), true);
            wTx.put(logicalDatastoreType,
                    UlnIidFactory.endpointLocationIid(epLocBuilder.getTenantId(), epLocBuilder.getUuid()),
                    epLocBuilder.build(), true);

            attachPorts(portLinks, wTx);

            return submitToDs(wTx);
        }
    }

    public static boolean attachAndSubmitToDs(Uuid firstId, Uuid secondId, Uuid tenantId,
            Pair<LocationType, LocationType> nodeTypes) {
        return attachAndSubmitToDs(firstId, secondId, tenantId, nodeTypes, null);
    }

    public static boolean attachAndSubmitToDs(Uuid firstId, Uuid secondId, Uuid tenantId,
            Pair<LocationType, LocationType> nodeTypes, Pair<Uuid, Uuid> secGroupRules) {
        if (firstId == null || secondId == null || nodeTypes == null || tenantId == null
                || nodeTypes.getFirst() == null || nodeTypes.getSecond() == null) {
            LOG.error("Couldn't join logical Network entities -- Missing required info. Nothing Submitted to DS");
            return false;
        }
        synchronized (UlnDatastoreApi.class) {
            DataObject first = null;
            DataObject second = null;
            if (nodeTypes.getFirst() == LocationType.SubnetType) {
                first = readSubnetFromDs(tenantId, firstId);
            } else if (nodeTypes.getFirst() == LocationType.RouterType) {
                first = readLogicalRouterFromDs(tenantId, firstId);
            } else if (nodeTypes.getFirst() == LocationType.SwitchType) {
                first = readLogicalSwitchFromDs(tenantId, firstId);
            }
            if (nodeTypes.getSecond() == LocationType.SubnetType) {
                second = readSubnetFromDs(tenantId, secondId);
            } else if (nodeTypes.getSecond() == LocationType.RouterType) {
                second = readLogicalRouterFromDs(tenantId, secondId);
            } else if (nodeTypes.getSecond() == LocationType.SwitchType) {
                second = readLogicalSwitchFromDs(tenantId, secondId);
            }
            return attachAndSubmitToDs(first, second, secGroupRules);
        }
    }

    public static boolean attachAndSubmitToDs(Object first, Object second) {
        return attachAndSubmitToDs(first, second, null);
    }

    public static boolean attachAndSubmitToDs(Object first, Object second, Pair<Uuid, Uuid> secGroupRules) {
        SubnetBuilder subnetBuilder = null;
        Pair<LogicalSwitchBuilder, LogicalSwitchBuilder> lSwitchPair = new Pair<LogicalSwitchBuilder, LogicalSwitchBuilder>(
                null, null);
        Pair<LogicalRouterBuilder, LogicalRouterBuilder> lRouterPair = new Pair<LogicalRouterBuilder, LogicalRouterBuilder>(
                null, null);
        Pair<SecurityRuleGroupsBuilder, SecurityRuleGroupsBuilder> secGroupsPair = new Pair<SecurityRuleGroupsBuilder, SecurityRuleGroupsBuilder>(
                null, null);

        if (first == null || second == null) {
            LOG.error("Not Allowed to attach NULL Entity -- Nothing Submitted to DS.");
            return false;
        }
        if (first instanceof Subnet) {
            subnetBuilder = new SubnetBuilder((Subnet) first);
        } else if (first instanceof SubnetBuilder) {
            subnetBuilder = (SubnetBuilder) first;
        } else if (first instanceof LogicalSwitch) {
            lSwitchPair.setFirst(new LogicalSwitchBuilder((LogicalSwitch) first));
        } else if (first instanceof LogicalSwitchBuilder) {
            lSwitchPair.setFirst((LogicalSwitchBuilder) first);
        } else if (first instanceof LogicalRouter) {
            lRouterPair.setFirst(new LogicalRouterBuilder((LogicalRouter) first));
        } else if (first instanceof LogicalRouterBuilder) {
            lRouterPair.setFirst((LogicalRouterBuilder) first);
        } else {
            LOG.error("Couldn't join an Object of type {} -- Nothing submitted to DS.", first.getClass().getName());
            return false;
        }
        if (second instanceof Subnet) {
            if (subnetBuilder != null) {
                LOG.error("Not Allowed to join two subnets in a logical network -- Not Submitted to DS");
                return false;
            }
            subnetBuilder = new SubnetBuilder((Subnet) second);
        } else if (second instanceof SubnetBuilder) {
            if (subnetBuilder != null) {
                LOG.error("Not Allowed to join two subnets in a logical network -- Not Submitted to DS");
                return false;
            }
            subnetBuilder = (SubnetBuilder) second;
        } else if (second instanceof LogicalSwitch) {
            lSwitchPair.setSecond(new LogicalSwitchBuilder((LogicalSwitch) second));
        } else if (second instanceof LogicalSwitchBuilder) {
            lSwitchPair.setSecond((LogicalSwitchBuilder) second);
        } else if (second instanceof LogicalRouter) {
            lRouterPair.setSecond(new LogicalRouterBuilder((LogicalRouter) second));
        } else if (second instanceof LogicalRouterBuilder) {
            lRouterPair.setSecond((LogicalRouterBuilder) second);
        } else {
            LOG.error("Couldn't join an Object of type {} -- Nothing submitted to DS.", second.getClass().getName());
            return false;
        }

        synchronized (UlnDatastoreApi.class) {
            Uuid firstSecurityGroupId = null;
            if (secGroupRules != null && secGroupRules.getFirst() != null) {
                firstSecurityGroupId = secGroupRules.getFirst();
            }
            PortBuilder firstPortbuilder = buildPort(subnetBuilder, firstSecurityGroupId, lSwitchPair.getFirst(),
                    lRouterPair.getFirst());
            if (firstSecurityGroupId != null) {
                SecurityRuleGroups dsSecurityGroups = readSecurityGroupsFromDs(firstPortbuilder.getTenantId(),
                        firstSecurityGroupId);
                if (dsSecurityGroups == null) {
                    LOG.error("Couldn't Find Security Rules {} in DS -- Didn't Join Logical Network Elements.",
                            firstSecurityGroupId);
                    return false;
                }
                SecurityRuleGroupsBuilder firstSecRulesGrpbuilder = new SecurityRuleGroupsBuilder(dsSecurityGroups);
                List<Uuid> firstSecRulesPorts = new ArrayList<>();
                firstSecRulesPorts.add(firstPortbuilder.getUuid());
                firstSecRulesGrpbuilder.setPorts(merge(firstSecRulesGrpbuilder.getPorts(), firstSecRulesPorts));
                secGroupsPair.setFirst(firstSecRulesGrpbuilder);
            }

            Uuid secondSecurityGroupId = null;
            if (secGroupRules != null && secGroupRules.getSecond() != null) {
                secondSecurityGroupId = secGroupRules.getSecond();
            }
            PortBuilder secondPortbuilder = buildPort(subnetBuilder, secondSecurityGroupId, lSwitchPair.getSecond(),
                    lRouterPair.getSecond());
            if (secondSecurityGroupId != null) {
                SecurityRuleGroups dsSecurityGroups = readSecurityGroupsFromDs(secondPortbuilder.getTenantId(),
                        secondSecurityGroupId);
                if (dsSecurityGroups == null) {
                    LOG.error("Couldn't Find Security Rules {} in DS -- Didn't Join Logical Network Elements.",
                            secondSecurityGroupId);
                    return false;
                }
                SecurityRuleGroupsBuilder secondSecRulesGrpbuilder = new SecurityRuleGroupsBuilder(dsSecurityGroups);
                List<Uuid> secondSecRulesPorts = new ArrayList<>();
                secondSecRulesPorts.add(secondPortbuilder.getUuid());
                secondSecRulesGrpbuilder.setPorts(merge(secondSecRulesGrpbuilder.getPorts(), secondSecRulesPorts));
                secGroupsPair.setSecond(secondSecRulesGrpbuilder);
            }

            WriteTransaction wTx = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
            if (lRouterPair.getFirst() != null) {
                wTx.put(logicalDatastoreType, UlnIidFactory.logicalRouterIid(lRouterPair.getFirst().getTenantId(),
                        lRouterPair.getFirst().getUuid()), lRouterPair.getFirst().build(), true);
            }
            if (lRouterPair.getSecond() != null) {
                wTx.put(logicalDatastoreType, UlnIidFactory.logicalRouterIid(lRouterPair.getSecond().getTenantId(),
                        lRouterPair.getSecond().getUuid()), lRouterPair.getSecond().build(), true);
            }
            if (lSwitchPair.getFirst() != null) {
                wTx.put(logicalDatastoreType, UlnIidFactory.logicalSwitchIid(lSwitchPair.getFirst().getTenantId(),
                        lSwitchPair.getFirst().getUuid()), lSwitchPair.getFirst().build(), true);
            }
            if (lSwitchPair.getSecond() != null) {
                wTx.put(logicalDatastoreType, UlnIidFactory.logicalSwitchIid(lSwitchPair.getSecond().getTenantId(),
                        lSwitchPair.getSecond().getUuid()), lSwitchPair.getSecond().build(), true);
            }
            if (subnetBuilder != null) {
                wTx.put(logicalDatastoreType,
                        UlnIidFactory.subnetIid(subnetBuilder.getTenantId(), subnetBuilder.getUuid()),
                        subnetBuilder.build(), true);
            }
            if (secGroupsPair.getFirst() != null) {
                wTx.put(logicalDatastoreType, UlnIidFactory.securityGroupsIid(secGroupsPair.getFirst().getTenantId(),
                        secGroupsPair.getFirst().getUuid()), secGroupsPair.getFirst().build(), true);
            }
            if (secGroupsPair.getSecond() != null) {
                wTx.put(logicalDatastoreType, UlnIidFactory.securityGroupsIid(secGroupsPair.getSecond().getTenantId(),
                        secGroupsPair.getSecond().getUuid()), secGroupsPair.getSecond().build(), true);
            }

            List<Pair<Port, Port>> portLinks = new ArrayList<>();
            portLinks.add(new Pair<>(firstPortbuilder.build(), secondPortbuilder.build()));

            attachPorts(portLinks, wTx);

            return submitToDs(wTx);
        }
    }

    private static PortBuilder buildPort(SubnetBuilder subnetBuilder, Uuid securityGroupId,
            LogicalSwitchBuilder lSwitchBuilder, LogicalRouterBuilder lRouterBuilder) {
        PortBuilder portbuilder = new PortBuilder();
        portbuilder.setUuid(new Uuid(UUID.randomUUID().toString()));
        portbuilder.setAdminStateUp(true);
        if (securityGroupId != null) {
            ArrayList<Uuid> firstNodeSecGrps = new ArrayList<>();
            firstNodeSecGrps.add(securityGroupId);
            portbuilder.setSecurityRulesGroups(firstNodeSecGrps);
        }
        List<Uuid> firstNodePorts = new ArrayList<>();
        firstNodePorts.add(portbuilder.getUuid());
        if (subnetBuilder != null) {
            portbuilder.setLocationId(subnetBuilder.getUuid());
            portbuilder.setLocationType(LocationType.SubnetType);
            portbuilder.setTenantId(subnetBuilder.getTenantId());
            subnetBuilder.setPort(merge(subnetBuilder.getPort(), firstNodePorts));
        } else if (lSwitchBuilder != null) {
            portbuilder.setLocationId(lSwitchBuilder.getUuid());
            portbuilder.setLocationType(LocationType.SwitchType);
            portbuilder.setTenantId(lSwitchBuilder.getTenantId());
            lSwitchBuilder.setPort(merge(lSwitchBuilder.getPort(), firstNodePorts));
        } else if (lRouterBuilder != null) {
            portbuilder.setLocationId(lRouterBuilder.getUuid());
            portbuilder.setLocationType(LocationType.RouterType);
            portbuilder.setTenantId(lRouterBuilder.getTenantId());
            lRouterBuilder.setPort(merge(lRouterBuilder.getPort(), firstNodePorts));
        }

        return portbuilder;
    }

    private static void attachPorts(List<Pair<Port, Port>> portLinks, WriteTransaction t) {
        if (portLinks == null) {
            return;
        }
        for (Pair<Port, Port> portLink : portLinks) {
            EdgeBuilder edgeb = new EdgeBuilder();
            Uuid edgeId = new Uuid(UUID.randomUUID().toString());
            edgeb.setUuid(edgeId);
            edgeb.setLeftPortId(portLink.getFirst().getUuid());
            edgeb.setRightPortId(portLink.getSecond().getUuid());
            edgeb.setTenantId(portLink.getFirst().getTenantId());
            t.put(logicalDatastoreType, UlnIidFactory.edgeIid(edgeb.getTenantId(), edgeId), edgeb.build(), true);

            PortBuilder lportb = new PortBuilder(portLink.getFirst());
            lportb.setEdgeId(edgeId);
            t.put(logicalDatastoreType, UlnIidFactory.portIid(lportb.getTenantId(), lportb.getUuid()), lportb.build(),
                    true);
            PortBuilder rportb = new PortBuilder(portLink.getSecond());
            rportb.setEdgeId(edgeId);
            t.put(logicalDatastoreType, UlnIidFactory.portIid(rportb.getTenantId(), rportb.getUuid()), rportb.build(),
                    true);
        }
    }

    /*
     * General & Common helper methods
     */
    public static <T extends DataObject> Optional<T> readFromDs(InstanceIdentifier<T> path, ReadTransaction rTx) {
        CheckedFuture<Optional<T>, ReadFailedException> resultFuture = rTx.read(logicalDatastoreType, path);
        try {
            return resultFuture.checkedGet();
        } catch (ReadFailedException e) {
            LOG.warn("Read failed from DS.", e);
            return Optional.absent();
        }
    }

    public static boolean submitToDs(WriteTransaction wTx) {
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
        try {
            submitFuture.checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Transaction commit failed to DS.", e);
            return false;
        }
    }

    public static <T extends DataObject> Optional<T> removeIfExists(InstanceIdentifier<T> path,
            ReadWriteTransaction rwTx) {
        Optional<T> potentialResult = readFromDs(path, rwTx);
        if (potentialResult.isPresent()) {
            rwTx.delete(logicalDatastoreType, path);
            LOG.debug("Removed present path {}", path);
        } else {
            LOG.debug("No need to remove Path {} -- it is NOT present", path);
        }
        return potentialResult;
    }

    private static List<Uuid> merge(List<Uuid> list1, List<Uuid> list2) {
        if (list1 == null && list2 == null) {
            return null;
        }
        if (list1 == null) {
            return list2;
        }
        if (list2 == null) {
            return list1;
        }
        Set<Uuid> set1 = new HashSet<>(list1);
        Set<Uuid> set2 = new HashSet<>(list2);
        set1.addAll(set2);
        return new ArrayList<>(set1);
    }
}
