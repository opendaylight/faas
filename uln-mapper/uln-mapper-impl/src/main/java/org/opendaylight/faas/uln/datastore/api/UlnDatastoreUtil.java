/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.uln.datastore.api;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.EdgeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.RegisterEndpointLocationInput;
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

public class UlnDatastoreUtil {

    private final Logger LOG = LoggerFactory.getLogger(UlnDatastoreUtil.class);
    private final LogicalDatastoreType logicalDatastoreType = LogicalDatastoreType.OPERATIONAL;
    private final Map<Uuid, RegisterEndpointLocationInput> registerEndpointInputMap = new HashMap<>();
    private final Map<Uuid, EndpointLocationBuilderCache> registerEndpointLocationInfoMap = new HashMap<>();

    private final DataBroker dataBroker;

    public UlnDatastoreUtil(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /*
     * Logical Network and individual elements Read Methods
     */
    public TenantLogicalNetwork readTenantLogicalNetworkFromDs(Uuid tenantId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<TenantLogicalNetwork> potentialSubnet = readFromDs(UlnIidFactory.tenantLogicalNetworkIid(tenantId), t);
        if (!potentialSubnet.isPresent()) {
            LOG.debug("Tenant {} Logical Network does not exist.", tenantId.getValue());
            return null;
        }
        return potentialSubnet.get();
    }

    public TenantLogicalNetwork readTenantsLogicalNetworksFromDs() {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<TenantLogicalNetwork> potentialSubnet = readFromDs(UlnIidFactory.tenantsLogicalNetworksIid(), t);
        if (!potentialSubnet.isPresent()) {
            LOG.debug("No Tenants Logical Networks exist in Datastore.");
            return null;
        }
        return potentialSubnet.get();
    }

    public Subnet readSubnetFromDs(Uuid tenantId, Uuid subnetId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<Subnet> potentialSubnet = readFromDs(UlnIidFactory.subnetIid(tenantId, subnetId), t);
        if (!potentialSubnet.isPresent()) {
            LOG.debug("Logical Subnet {} does not exist in tenant {}.", subnetId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialSubnet.get();
    }

    public LogicalRouter readLogicalRouterFromDs(Uuid tenantId, Uuid routerId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<LogicalRouter> potentialRouter = readFromDs(UlnIidFactory.logicalRouterIid(tenantId, routerId), t);
        if (!potentialRouter.isPresent()) {
            LOG.debug("Logical Router {} does not exist in tenant {}.", routerId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialRouter.get();
    }

    public LogicalSwitch readLogicalSwitchFromDs(Uuid tenantId, Uuid switchId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<LogicalSwitch> potentialSwitch = readFromDs(UlnIidFactory.logicalSwitchIid(tenantId, switchId), t);
        if (!potentialSwitch.isPresent()) {
            LOG.debug("Logical Switch {} does not exist in tenant {}.", switchId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialSwitch.get();
    }

    public SecurityRuleGroups readSecurityGroupsFromDs(Uuid tenantId, Uuid securityGroupId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<SecurityRuleGroups> potentialSecurityGroup = readFromDs(
                UlnIidFactory.securityGroupsIid(tenantId, securityGroupId), t);
        if (!potentialSecurityGroup.isPresent()) {
            LOG.debug("Logical SecurityGroup {} does not exist in tenant {}.", securityGroupId.getValue(),
                    tenantId.getValue());
            return null;
        }
        return potentialSecurityGroup.get();
    }

    public Port readPortFromDs(Uuid tenantId, Uuid portId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<Port> potentialPort = readFromDs(UlnIidFactory.portIid(tenantId, portId), t);
        if (!potentialPort.isPresent()) {
            LOG.debug("Logical Port {} does not exist in tenant {}.", portId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialPort.get();
    }

    public Edge readEdgeFromDs(Uuid tenantId, Uuid edgeId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<Edge> potentialEdge = readFromDs(UlnIidFactory.edgeIid(tenantId, edgeId), t);
        if (!potentialEdge.isPresent()) {
            LOG.debug("Logical Edge {} does not exist in tenant {}.", edgeId.getValue(), tenantId.getValue());
            return null;
        }
        return potentialEdge.get();
    }

    public EndpointLocation readEndpointLocationFromDs(Uuid tenantId, Uuid endpointLocationId) {
        ReadTransaction t = dataBroker.newReadOnlyTransaction();
        Optional<EndpointLocation> potentialEndpointLocation = readFromDs(
                UlnIidFactory.endpointLocationIid(tenantId, endpointLocationId), t);
        if (!potentialEndpointLocation.isPresent()) {
            LOG.debug("Logical EndpointLocation {} does not exist in tenant {}.", endpointLocationId.getValue(),
                    tenantId.getValue());
            return null;
        }
        return potentialEndpointLocation.get();
    }

    /*
     * Logical Network and individual elements Submit Methods
     */
    public void submitSubnetToDs(Subnet subnet) {
        submitSubnetToDs(subnet, true);
    }

    private void submitSubnetToDs(Subnet newSubnet, boolean updateAndMergeRefs) {
        synchronized (UlnDatastoreUtil.class) {
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
            WriteTransaction t = dataBroker.newWriteOnlyTransaction();
            t.put(logicalDatastoreType, UlnIidFactory.subnetIid(updatedSubnet.getTenantId(), updatedSubnet.getUuid()),
                    updatedSubnet, true);
            if (submitToDs(t)) {
                LOG.debug("Wrote logical subnet {} to datastore.", updatedSubnet.getUuid().getValue());
                /*
                 * Make sure other logical network nodes links are updated as well
                 */
                if (updatedSubnet.getPort() != null && updateAndMergeRefs) {
                    for (Uuid portId : updatedSubnet.getPort()) {
                        Port port = readPortFromDs(updatedSubnet.getTenantId(), portId);
                        if (port != null
                                && (!updatedSubnet.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.SubnetType)) {
                            PortBuilder builder = new PortBuilder(port);
                            builder.setLocationId(updatedSubnet.getUuid());
                            builder.setLocationType(LocationType.SubnetType);
                            submitPortToDs(builder.build(), false);
                        }
                    }
                }
            } else {
                LOG.error("Failed to write logical subnet {} to datastore.", updatedSubnet.getUuid().getValue());
            }
        }
    }

    public void submitLogicalRouterToDs(LogicalRouter router) {
        submitLogicalRouterToDs(router, true);
    }

    public void submitLogicalRouterToDs(LogicalRouter newRouter, boolean updateAndMergeRefs) {
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
        WriteTransaction t = dataBroker.newWriteOnlyTransaction();
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
                    Port port = readPortFromDs(updatedRouter.getTenantId(), portId);
                    if (port != null
                            && (!updatedRouter.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.RouterType)) {
                        PortBuilder builder = new PortBuilder(port);
                        builder.setLocationId(updatedRouter.getUuid());
                        builder.setLocationType(LocationType.RouterType);
                        submitPortToDs(builder.build(), false);
                    }
                }
            }
        } else {
            LOG.error("Failed to write logical router {} to datastore.", updatedRouter.getUuid().getValue());
        }
    }

    public void submitLogicalSwitchToDs(LogicalSwitch newSwitch) {
        submitLogicalSwitchToDs(newSwitch, true);
    }

    private void submitLogicalSwitchToDs(LogicalSwitch newSwitch, boolean updateAndMergeRefs) {
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
        WriteTransaction t = dataBroker.newWriteOnlyTransaction();
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
                    Port port = readPortFromDs(updatedSwitch.getTenantId(), portId);
                    if (port != null
                            && (!updatedSwitch.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.SwitchType)) {
                        PortBuilder builder = new PortBuilder(port);
                        builder.setLocationId(updatedSwitch.getUuid());
                        builder.setLocationType(LocationType.SwitchType);
                        submitPortToDs(builder.build(), false);
                    }
                }
            }
        } else {
            LOG.error("Failed to write logical switch {} to datastore.", updatedSwitch.getUuid().getValue());
        }
    }

    public void submitSecurityGroupsToDs(SecurityRuleGroups newSecurityGroups) {
        submitSecurityGroupsToDs(newSecurityGroups, true);
    }

    private void submitSecurityGroupsToDs(SecurityRuleGroups newSecurityGroups, boolean updateAndMergeRefs) {
        synchronized (UlnDatastoreUtil.class) {
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
            WriteTransaction t = dataBroker.newWriteOnlyTransaction();
            t.put(logicalDatastoreType,
                    UlnIidFactory.securityGroupsIid(updatedSecurityGroups.getTenantId(),
                            updatedSecurityGroups.getUuid()), updatedSecurityGroups, true);
            if (submitToDs(t)) {
                LOG.debug("Wrote logical securityGroups {} to datastore.", updatedSecurityGroups.getUuid().getValue());
                /*
                 * Make sure other logical network nodes links are updated as well
                 */
                if (updatedSecurityGroups.getPorts() != null && updateAndMergeRefs) {
                    for (Uuid portId : updatedSecurityGroups.getPorts()) {
                        Port port = readPortFromDs(updatedSecurityGroups.getTenantId(), portId);
                        if (port != null) {
                            Set<Uuid> set = new HashSet<>();
                            if (port.getSecurityRulesGroups() != null) {
                                set.addAll(port.getSecurityRulesGroups());
                            }
                            if (!set.contains(updatedSecurityGroups.getUuid())) {
                                set.add(updatedSecurityGroups.getUuid());
                                PortBuilder builder = new PortBuilder(port);
                                builder.setSecurityRulesGroups(new ArrayList<>(set));
                                submitPortToDs(builder.build(), false);
                            }
                        }
                    }
                }
            } else {
                LOG.error("Failed to write logical securityGroups {} to datastore.", updatedSecurityGroups.getUuid()
                    .getValue());
            }
        }
    }

    public void submitPortToDs(Port port) {
        submitPortToDs(port, true);
    }

    private void submitPortToDs(Port newPort, boolean updateAndMergeRefs) {
        if (newPort.getLocationId() == null || newPort.getLocationType() == null) {
            LOG.error("Trying to submit Port {} without associating it with a logical node -- Ignored Request",
                    newPort.getUuid().getValue());
            return;
        }
        synchronized (UlnDatastoreUtil.class) {
            /*
             * Make sure we don't overwrite certain existing links
             */
            Port updatedPort = newPort;
            if (updateAndMergeRefs) {
                Port dsPort = readPortFromDs(newPort.getTenantId(), newPort.getUuid());
                if (dsPort != null) {
                    PortBuilder builder = new PortBuilder(newPort);
                    builder.setSecurityRulesGroups(merge(dsPort.getSecurityRulesGroups(),
                            newPort.getSecurityRulesGroups()));
                    updatedPort = builder.build();
                }
            }
            /*
             * Write to data store
             */
            WriteTransaction t = dataBroker.newWriteOnlyTransaction();
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
                            SecurityRuleGroups secGrp = readSecurityGroupsFromDs(
                                    updatedPort.getTenantId(), secGrpId);
                            if (secGrp != null) {
                                Set<Uuid> set = new HashSet<>();
                                if (secGrp.getPorts() != null) {
                                    set.addAll(secGrp.getPorts());
                                }
                                if (!set.contains(updatedPort.getUuid())) {
                                    set.add(updatedPort.getUuid());
                                    SecurityRuleGroupsBuilder builder = new SecurityRuleGroupsBuilder(secGrp);
                                    builder.setPorts(new ArrayList<>(set));
                                    submitSecurityGroupsToDs(builder.build(), false);
                                }
                            }
                        }
                    }

                    // update edge
                    if (updatedPort.getEdgeId() != null) {
                        Edge edge = readEdgeFromDs(updatedPort.getTenantId(), updatedPort.getEdgeId());
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
    }

    public void submitEdgeToDs(Edge edge) {
        if (edge.getLeftPortId() == null || edge.getRightPortId() == null) {
            LOG.error("Trying to Subnit an edge Edge with less than two ports -- Ignored Request");
            return;
        }
        Port lport = readPortFromDs(edge.getTenantId(), edge.getLeftPortId());
        if (lport != null && lport.getEdgeId() != null && !edge.getUuid().equals(lport.getEdgeId())) {
            LOG.error(
                    "Trying to Submit Edge {} that references Port {}, but that Port already references Edge {}. Ignored Request.",
                    edge.getUuid().getValue(), lport.getUuid().getValue(), lport.getEdgeId().getValue());
            return;
        }
        Port rport = readPortFromDs(edge.getTenantId(), edge.getRightPortId());
        if (rport != null && rport.getEdgeId() != null && !edge.getUuid().equals(rport.getEdgeId())) {
            LOG.error(
                    "Trying to Submit Edge {} that references Port {}, but that Port already references Edge {}. Ignored Request.",
                    edge.getUuid().getValue(), rport.getUuid().getValue(), rport.getEdgeId().getValue());
            return;
        }
        WriteTransaction t = dataBroker.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.edgeIid(edge.getTenantId(), edge.getUuid()), edge, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical edge {} to datastore.", edge.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical edge {} to datastore.", edge.getUuid().getValue());
        }
    }

    public void submitEndpointLocationToDs(EndpointLocation endpointLocation) {
        submitEndpointLocationToDs(endpointLocation, true);
    }

    private void submitEndpointLocationToDs(EndpointLocation newEndpointLocation, boolean updateAndMergeRefs) {
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
        WriteTransaction t = dataBroker.newWriteOnlyTransaction();
        t.put(logicalDatastoreType,
                UlnIidFactory.endpointLocationIid(updatedEndpointLocation.getTenantId(),
                        updatedEndpointLocation.getUuid()), updatedEndpointLocation, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical endpointLocation {} to datastore.", updatedEndpointLocation.getUuid().getValue());
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (updatedEndpointLocation.getPort() != null && updateAndMergeRefs) {
                Port port = readPortFromDs(updatedEndpointLocation.getTenantId(),
                        updatedEndpointLocation.getPort());
                if (port != null
                        && (!updatedEndpointLocation.getUuid().equals(port.getLocationId()) || port.getLocationType() != LocationType.EndpointType)) {
                    PortBuilder builder = new PortBuilder(port);
                    builder.setLocationId(updatedEndpointLocation.getUuid());
                    builder.setLocationType(LocationType.EndpointType);
                    submitPortToDs(builder.build(), false);
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
    public void removeTenantFromDsIfExists(Uuid tenantId) {
        Optional<TenantLogicalNetwork> tenantULnOp = removeIfExists(UlnIidFactory.tenantLogicalNetworkIid(tenantId));
        if (tenantULnOp.isPresent()) {
            LOG.info("Removed Tenant {}", tenantId.getValue());
        } else {
            LOG.debug("Tenant {} wasn't found", tenantId.getValue());
        }
    }

    public void removeSubnetFromDsIfExists(Uuid tenantId, Uuid subnetId) {
        Optional<Subnet> oldOptional = removeIfExists(UlnIidFactory.subnetIid(tenantId, subnetId));
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

    public void removeLogicalRouterFromDsIfExists(Uuid tenantId, Uuid routerId) {
        Optional<LogicalRouter> oldOptional = removeIfExists(UlnIidFactory.logicalRouterIid(tenantId, routerId));
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

    public void removeLogicalSwitchFromDsIfExists(Uuid tenantId, Uuid switchId) {
        Optional<LogicalSwitch> oldOptional = removeIfExists(UlnIidFactory.logicalSwitchIid(tenantId, switchId));
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

    public void removeSecurityGroupsFromDsIfExists(Uuid tenantId, Uuid securityGroupId) {
        removeSecurityGroupsFromDsIfExists(tenantId, securityGroupId, true);
    }

    private void removeSecurityGroupsFromDsIfExists(Uuid tenantId, Uuid securityGroupId,
            boolean updateExistingRefs) {
        Optional<SecurityRuleGroups> oldOption = removeIfExists(UlnIidFactory.securityGroupsIid(tenantId,
                securityGroupId));
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOption.isPresent() && updateExistingRefs) {
            SecurityRuleGroups securityRuleGroups = oldOption.get();
            if (securityRuleGroups.getPorts() != null) {
                for (Uuid portId : securityRuleGroups.getPorts()) {
                    Port port = readPortFromDs(securityRuleGroups.getTenantId(), portId);
                    if (port != null && port.getSecurityRulesGroups() != null) {
                        Set<Uuid> set = new HashSet<>(port.getSecurityRulesGroups());
                        if (set.remove(securityRuleGroups.getUuid())) {
                            PortBuilder builder = new PortBuilder(port);
                            builder.setSecurityRulesGroups(new ArrayList<>(set));
                            submitPortToDs(builder.build(), false);
                        }
                    }
                }
            }
        }
    }

    public void removePortFromDsIfExists(Uuid tenantId, Uuid portId) {
        synchronized (UlnDatastoreUtil.class) {
            Optional<Port> oldOption = removeIfExists(UlnIidFactory.portIid(tenantId, portId));
            /*
             * Make sure other logical network nodes links are updated as well
             */
            if (oldOption.isPresent()) {
                Port port = oldOption.get();
                // update security groups
                if (port.getSecurityRulesGroups() != null) {
                    for (Uuid sId : port.getSecurityRulesGroups()) {
                        SecurityRuleGroups secGrps = readSecurityGroupsFromDs(tenantId, sId);
                        if (secGrps != null && secGrps.getPorts() != null) {
                            Set<Uuid> set = new HashSet<>(secGrps.getPorts());
                            if (set.remove(port.getUuid())) {
                                SecurityRuleGroupsBuilder builder = new SecurityRuleGroupsBuilder(secGrps);
                                builder.setPorts(new ArrayList<>(set));
                                submitSecurityGroupsToDs(builder.build(), false);
                            }
                        }
                    }
                }
                // remove edge
                removeEdgeFromDsIfExists(tenantId, port.getEdgeId());

                // update node
                if (port.getLocationId() != null) {
                    if (port.getLocationType() == LocationType.EndpointType) {
                        removeEndpointLocationFromDsIfExists(tenantId, port.getLocationId());
                    }
                }
            }
        }
    }

    public void removeEdgeFromDsIfExists(Uuid tenantId, Uuid edgeId) {
        Optional<Edge> oldOptional = removeIfExists(UlnIidFactory.edgeIid(tenantId, edgeId));
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

    public void removeEndpointLocationFromDsIfExists(Uuid tenantId, Uuid endpointLocationId) {
        /*
         * Make sure other logical network nodes links are updated as well
         */
        Optional<EndpointLocation> oldOptional = removeIfExists(UlnIidFactory.endpointLocationIid(tenantId,
                endpointLocationId));
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

    public boolean attachEndpointToSubnet(EndpointLocationBuilder epLocBuilder, Uuid faasSubnetId,
            MacAddress macAddress, List<PrivateIps> privateIpAddresses, List<IpAddress> publicIpAddresses) {
        if (epLocBuilder.getFaasPortRefId() == null) {
            LOG.error("Missing Required Info in EndpointLocationBuilder: {}", epLocBuilder.build());
            return false;
        }
        synchronized (UlnDatastoreUtil.class) {
            RegisterEndpointLocationInput input = registerEndpointInputMap.get(epLocBuilder.getFaasPortRefId());
            if (input != null) {
                epLocBuilder.setNodeConnectorId(input.getNodeConnectorId());
                epLocBuilder.setNodeId(input.getNodeId());
                epLocBuilder.setPortName(input.getPortName());
                return attachEndpointToSubnetAndSubmitToDs(epLocBuilder, faasSubnetId, macAddress, privateIpAddresses,
                        publicIpAddresses);
            } else {
                EndpointLocationBuilderCache cache = new EndpointLocationBuilderCache();
                cache.setEpLocBuilder(epLocBuilder);
                cache.setFaasSubnetId(faasSubnetId);
                cache.setMacAddress(macAddress);
                cache.setPrivateIpAddresses(privateIpAddresses);
                cache.setPublicIpAddresses(publicIpAddresses);
                registerEndpointLocationInfoMap.put(epLocBuilder.getFaasPortRefId(), cache);
                LOG.debug("Caching EndpointLocationBuilder: {}", cache);
                return true;
            }
        }
    }

    public boolean attachEndpointToSubnet(RegisterEndpointLocationInput input) {
        if (input.getFaasPortRefId() == null) {
            LOG.error("Missing Required Info in RegisterEndpointLocationInput: {}", input);
            return false;
        }
        synchronized (UlnDatastoreUtil.class) {
            EndpointLocationBuilderCache endpointLocInfo = registerEndpointLocationInfoMap.get(input.getFaasPortRefId());
            if (endpointLocInfo != null) {
                endpointLocInfo.getEpLocBuilder().setNodeConnectorId(input.getNodeConnectorId());
                endpointLocInfo.getEpLocBuilder().setNodeId(input.getNodeId());
                endpointLocInfo.getEpLocBuilder().setPortName(input.getPortName());
                return attachEndpointToSubnetAndSubmitToDs(endpointLocInfo.getEpLocBuilder(),
                        endpointLocInfo.getFaasSubnetId(), endpointLocInfo.getMacAddress(),
                        endpointLocInfo.getPrivateIpAddresses(), endpointLocInfo.getPublicIpAddresses());
            } else {
                if (input.getNodeId() != null && input.getNodeConnectorId() != null) {
                    registerEndpointInputMap.put(input.getFaasPortRefId(), input);
                    LOG.debug("Caching RegisterEndpointLocationInput: {}", input);
                    return true;
                } else if (input.getPortName() != null) {
                    LOG.warn("Neutron is not Handled Yet By Uln-Mapper. Ignored Port Name {}", input.getPortName());
                } else {
                    LOG.error("Missing Required Info in RegisterEndpointLocationInput: {}", input);
                }

            }
        }
        return false;

    }

    private boolean attachEndpointToSubnetAndSubmitToDs(EndpointLocationBuilder epLocBuilder, Uuid faasSubnetId,
            MacAddress macAddress, List<PrivateIps> privateIpAddresses, List<IpAddress> publicIpAddresses) {

        if (epLocBuilder.getNodeConnectorId() == null || epLocBuilder.getNodeId() == null) {
            LOG.error("Endpoint Location does NOT have Node Location. Endpoint = {}", epLocBuilder.build());
            return false;
        }
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

        WriteTransaction wTx = dataBroker.newWriteOnlyTransaction();
        wTx.put(logicalDatastoreType, UlnIidFactory.subnetIid(subnetBuilder.getTenantId(), subnetBuilder.getUuid()),
                subnetBuilder.build(), true);
        wTx.put(logicalDatastoreType,
                UlnIidFactory.endpointLocationIid(epLocBuilder.getTenantId(), epLocBuilder.getUuid()),
                epLocBuilder.build(), true);

        attachPorts(portLinks, wTx);

        return submitToDs(wTx);
    }

    public boolean attachAndSubmitToDs(Uuid firstId, Uuid secondId, Uuid tenantId,
            Pair<LocationType, LocationType> nodeTypes) {
        return attachAndSubmitToDs(firstId, secondId, tenantId, nodeTypes, null, null);
    }

    public boolean attachAndSubmitToDs(Uuid firstId, Uuid secondId, Uuid tenantId,
            Pair<LocationType, LocationType> nodeTypes, Pair<Uuid, Uuid> privateSecGroupRules,
            Pair<Uuid, Uuid> publicSecGroupRules) {
        if (firstId == null || secondId == null || nodeTypes == null || tenantId == null
                || nodeTypes.getFirst() == null || nodeTypes.getSecond() == null) {
            LOG.error("Couldn't join logical Network entities -- Missing required info. Nothing Submitted to DS");
            return false;
        }
        synchronized (UlnDatastoreUtil.class) {
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
            return attachAndSubmitToDs(first, second, privateSecGroupRules, publicSecGroupRules);
        }
    }

    public boolean attachAndSubmitToDs(Object first, Object second) {
        return attachAndSubmitToDs(first, second, null, null);
    }

    public boolean attachAndSubmitToDs(Object first, Object second, Pair<Uuid, Uuid> privateSecGroupRules) {
        return attachAndSubmitToDs(first, second, privateSecGroupRules, null);
    }

    public boolean attachAndSubmitToDs(Object first, Object second, Pair<Uuid, Uuid> privateSecGroupRules,
            Pair<Uuid, Uuid> pubSecGroupRules) {
        Pair<SubnetBuilder, SubnetBuilder> lSubnetPair = new Pair<>(null, null);
        Pair<LogicalSwitchBuilder, LogicalSwitchBuilder> lSwitchPair = new Pair<>(
                null, null);
        Pair<LogicalRouterBuilder, LogicalRouterBuilder> lRouterPair = new Pair<>(
                null, null);
        Pair<SecurityRuleGroupsBuilder, SecurityRuleGroupsBuilder> secGroupsPair = new Pair<>(
                null, null);

        if (first == null || second == null) {
            LOG.error("Not Allowed to attach NULL Entity -- Nothing Submitted to DS.");
            return false;
        }
        if (first instanceof Subnet) {
            lSubnetPair.setFirst(new SubnetBuilder((Subnet) first));
        } else if (first instanceof SubnetBuilder) {
            lSubnetPair.setFirst((SubnetBuilder) first);
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
            if (lSubnetPair.getFirst() != null) {
                LOG.error("Not Allowed to join two subnets in a logical network -- Not Submitted to DS");
                return false;
            }
            lSubnetPair.setSecond(new SubnetBuilder((Subnet) second));
        } else if (second instanceof SubnetBuilder) {
            if (lSubnetPair.getFirst() != null) {
                LOG.error("Not Allowed to join two subnets in a logical network -- Not Submitted to DS");
                return false;
            }
            lSubnetPair.setSecond((SubnetBuilder) second);
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

        synchronized (UlnDatastoreUtil.class) {
            Uuid firstPrivateSecurityGroupId = null;
            if (privateSecGroupRules != null && privateSecGroupRules.getFirst() != null) {
                firstPrivateSecurityGroupId = privateSecGroupRules.getFirst();
            }
            PortBuilder firstPortbuilder = buildPort(lSubnetPair.getFirst(), firstPrivateSecurityGroupId,
                    lSwitchPair.getFirst(), lRouterPair.getFirst());
            if (firstPrivateSecurityGroupId != null) {
                SecurityRuleGroups dsSecurityGroups = readSecurityGroupsFromDs(firstPortbuilder.getTenantId(),
                        firstPrivateSecurityGroupId);
                if (dsSecurityGroups == null) {
                    LOG.error("Couldn't Find Security Rules {} in DS -- Didn't Join Logical Network Elements.",
                            firstPrivateSecurityGroupId);
                    return false;
                }
                SecurityRuleGroupsBuilder firstSecRulesGrpbuilder = new SecurityRuleGroupsBuilder(dsSecurityGroups);
                List<Uuid> firstSecRulesPorts = new ArrayList<>();
                firstSecRulesPorts.add(firstPortbuilder.getUuid());
                firstSecRulesGrpbuilder.setPorts(merge(firstSecRulesGrpbuilder.getPorts(), firstSecRulesPorts));
                secGroupsPair.setFirst(firstSecRulesGrpbuilder);
            }

            Uuid secondSecurityGroupId = null;
            if (privateSecGroupRules != null && privateSecGroupRules.getSecond() != null) {
                secondSecurityGroupId = privateSecGroupRules.getSecond();
            }
            PortBuilder secondPortbuilder = buildPort(lSubnetPair.getSecond(), secondSecurityGroupId,
                    lSwitchPair.getSecond(), lRouterPair.getSecond());
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

            WriteTransaction wTx = dataBroker.newWriteOnlyTransaction();
            if (lRouterPair.getFirst() != null) {
                if (needToAddPublicPort(lRouterPair.getFirst())) {
                    PortBuilder lportb;
                    if (pubSecGroupRules == null) {
                        lportb = buildPort(null, null, null, lRouterPair.getFirst());
                    } else {
                        lportb = buildPort(null, pubSecGroupRules.getFirst(), null, lRouterPair.getFirst());
                    }
                    lportb.setIsPublic(true);
                    wTx.put(logicalDatastoreType, UlnIidFactory.portIid(lportb.getTenantId(), lportb.getUuid()),
                            lportb.build(), true);
                }
                wTx.put(logicalDatastoreType, UlnIidFactory.logicalRouterIid(lRouterPair.getFirst().getTenantId(),
                        lRouterPair.getFirst().getUuid()), lRouterPair.getFirst().build(), true);
            }
            if (lRouterPair.getSecond() != null) {
                if (needToAddPublicPort(lRouterPair.getSecond())) {
                    PortBuilder lportb;
                    if (pubSecGroupRules == null) {
                        lportb = buildPort(null, null, null, lRouterPair.getSecond());
                    } else {
                        lportb = buildPort(null, pubSecGroupRules.getSecond(), null, lRouterPair.getSecond());
                    }
                    lportb.setIsPublic(true);
                    wTx.put(logicalDatastoreType, UlnIidFactory.portIid(lportb.getTenantId(), lportb.getUuid()),
                            lportb.build(), true);
                }
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
            if (lSubnetPair.getFirst() != null) {
                wTx.put(logicalDatastoreType,
                        UlnIidFactory.subnetIid(lSubnetPair.getFirst().getTenantId(), lSubnetPair.getFirst().getUuid()),
                        lSubnetPair.getFirst().build(), true);
            }
            if (lSubnetPair.getSecond() != null) {
                wTx.put(logicalDatastoreType, UlnIidFactory.subnetIid(lSubnetPair.getSecond().getTenantId(),
                        lSubnetPair.getSecond().getUuid()), lSubnetPair.getSecond().build(), true);
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

    private boolean needToAddPublicPort(LogicalRouterBuilder logicalRouter) {
        if (logicalRouter.isPublic() != null && logicalRouter.isPublic().booleanValue()) {
            if (logicalRouter.getPort() == null) {
                return true;
            }
            for (Uuid portId : logicalRouter.getPort()) {
                Port port = readPortFromDs(logicalRouter.getTenantId(), portId);
                if (port != null && port.isIsPublic() != null && port.isIsPublic().booleanValue()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private PortBuilder buildPort(SubnetBuilder subnetBuilder, Uuid securityGroupId,
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
            LOG.debug("Inserting New Port {} in Subnet {}", portbuilder.getUuid().getValue(), subnetBuilder.getUuid()
                .getValue());
        } else if (lSwitchBuilder != null) {
            portbuilder.setLocationId(lSwitchBuilder.getUuid());
            portbuilder.setLocationType(LocationType.SwitchType);
            portbuilder.setTenantId(lSwitchBuilder.getTenantId());
            lSwitchBuilder.setPort(merge(lSwitchBuilder.getPort(), firstNodePorts));
            LOG.debug("Inserting New Port {} in Switch {}", portbuilder.getUuid().getValue(), lSwitchBuilder.getUuid()
                .getValue());
        } else if (lRouterBuilder != null) {
            portbuilder.setLocationId(lRouterBuilder.getUuid());
            portbuilder.setLocationType(LocationType.RouterType);
            portbuilder.setTenantId(lRouterBuilder.getTenantId());
            lRouterBuilder.setPort(merge(lRouterBuilder.getPort(), firstNodePorts));
            LOG.debug("Inserting New Port {} in Router {}", portbuilder.getUuid().getValue(), lRouterBuilder.getUuid()
                .getValue());
        }

        return portbuilder;
    }

    private void attachPorts(List<Pair<Port, Port>> portLinks, WriteTransaction t) {
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
            LOG.debug("Attached Port {} to Port {} via Edge {}", lportb.getUuid().getValue(), rportb.getUuid()
                .getValue(), edgeb.getUuid().getValue());
        }
    }

    /*
     * General & Common helper methods
     */
    public <T extends DataObject> Optional<T> readFromDs(InstanceIdentifier<T> path, ReadTransaction rTx) {
        CheckedFuture<Optional<T>, ReadFailedException> resultFuture = rTx.read(logicalDatastoreType, path);
        try {
            return resultFuture.checkedGet();
        } catch (ReadFailedException e) {
            LOG.warn("Read failed from DS.", e);
            return Optional.absent();
        }
    }

    public boolean submitToDs(WriteTransaction wTx) {
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
        try {
            submitFuture.checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Transaction commit failed to DS.", e);
            return false;
        }
    }

    public <T extends DataObject> Optional<T> removeIfExists(InstanceIdentifier<T> path) {
        ReadWriteTransaction rwTx = dataBroker.newReadWriteTransaction();
        Optional<T> potentialResult = readFromDs(path, rwTx);
        if (potentialResult.isPresent()) {
            rwTx.delete(logicalDatastoreType, path);
            submitToDs(rwTx);
            LOG.debug("Removed present path {}", path);
        } else {
            LOG.debug("No need to remove Path {} -- it is NOT present", path);
        }
        return potentialResult;
    }

    private List<Uuid> merge(List<Uuid> list1, List<Uuid> list2) {
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

    private static class EndpointLocationBuilderCache {

        private EndpointLocationBuilder epLocBuilder;
        private Uuid faasSubnetId;
        private MacAddress macAddress;
        private List<PrivateIps> privateIpAddresses;
        private List<IpAddress> publicIpAddresses;

        public EndpointLocationBuilder getEpLocBuilder() {
            return epLocBuilder;
        }

        public void setEpLocBuilder(EndpointLocationBuilder epLocBuilder) {
            this.epLocBuilder = epLocBuilder;
        }

        public Uuid getFaasSubnetId() {
            return faasSubnetId;
        }

        public void setFaasSubnetId(Uuid faasSubnetId) {
            this.faasSubnetId = faasSubnetId;
        }

        public MacAddress getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(MacAddress macAddress) {
            this.macAddress = macAddress;
        }

        public List<PrivateIps> getPrivateIpAddresses() {
            return privateIpAddresses;
        }

        public void setPrivateIpAddresses(List<PrivateIps> privateIpAddresses) {
            this.privateIpAddresses = privateIpAddresses;
        }

        public List<IpAddress> getPublicIpAddresses() {
            return publicIpAddresses;
        }

        public void setPublicIpAddresses(List<IpAddress> publicIpAddresses) {
            this.publicIpAddresses = publicIpAddresses;
        }
    }

}
