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

import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.faas.uln.manager.UlnMapperDatastoreDependency;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
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
     * Subnet
     */
    public static void submitSubnetToDs(Subnet subnet) {
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.subnetIid(subnet.getTenantId(), subnet.getUuid()), subnet, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical subnet {} to datastore.", subnet.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical subnet {} to datastore.", subnet.getUuid().getValue());
        }
    }

    public static Subnet readSubnetFromDs(Uuid tenantId, Uuid subnetId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<Subnet> potentialSubnet = readFromDs(UlnIidFactory.subnetIid(tenantId, subnetId), t);
        if (!potentialSubnet.isPresent()) {
            LOG.warn("Logical Subnet {} does not exist.", subnetId.getValue());
            return null;
        }
        return potentialSubnet.get();
    }

    public static void removeSubnetFromDsIfExists(Uuid tenantId, Uuid subnetId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<Subnet> oldOptional = removeIfExists(UlnIidFactory.subnetIid(tenantId, subnetId), t);
        if (oldOptional.isPresent()) {
            Subnet subnet = oldOptional.get();
            if (subnet.getPort() != null) {
                for (Uuid port : subnet.getPort()) {
                    removePortFromDsIfExists(tenantId, port);
                }
            }
        }
    }

    /*
     * Router
     */
    public static void submitLogicalRouterToDs(LogicalRouter router) {
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.logicalRouterIid(router.getTenantId(), router.getUuid()), router,
                true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical router {} to datastore.", router.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical router {} to datastore.", router.getUuid().getValue());
        }
    }

    public static LogicalRouter readLogicalRouterFromDs(Uuid tenantId, Uuid routerId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<LogicalRouter> potentialRouter = readFromDs(UlnIidFactory.logicalRouterIid(tenantId, routerId), t);
        if (!potentialRouter.isPresent()) {
            LOG.warn("Logical Router {} does not exist.", routerId.getValue());
            return null;
        }
        return potentialRouter.get();
    }

    public static void removeLogicalRouterFromDsIfExists(Uuid tenantId, Uuid routerId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<LogicalRouter> oldOptional = removeIfExists(UlnIidFactory.logicalRouterIid(tenantId, routerId), t);
        if (oldOptional.isPresent()) {
            LogicalRouter router = oldOptional.get();
            if (router.getPort() != null) {
                for (Uuid port : router.getPort()) {
                    removePortFromDsIfExists(tenantId, port);
                }
            }
        }
    }

    /*
     * Switch
     */
    public static void submitLogicalSwitchToDs(LogicalSwitch lswitch) {
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.logicalSwitchIid(lswitch.getTenantId(), lswitch.getUuid()), lswitch,
                true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical switch {} to datastore.", lswitch.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical switch {} to datastore.", lswitch.getUuid().getValue());
        }
    }

    public static LogicalSwitch readLogicalSwitchFromDs(Uuid tenantId, Uuid switchId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<LogicalSwitch> potentialSwitch = readFromDs(UlnIidFactory.logicalSwitchIid(tenantId, switchId), t);
        if (!potentialSwitch.isPresent()) {
            LOG.warn("Logical Switch {} does not exist.", switchId.getValue());
            return null;
        }
        return potentialSwitch.get();
    }

    public static void removeLogicalSwitchFromDsIfExists(Uuid tenantId, Uuid switchId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<LogicalSwitch> oldOptional = removeIfExists(UlnIidFactory.logicalSwitchIid(tenantId, switchId), t);
        if (oldOptional.isPresent()) {
            LogicalSwitch lSwitch = oldOptional.get();
            if (lSwitch.getPort() != null) {
                for (Uuid port : lSwitch.getPort()) {
                    removePortFromDsIfExists(tenantId, port);
                }
            }
        }
    }

    /*
     * Security Rule Groups
     */
    public static void submitSecurityGroupsToDs(SecurityRuleGroups newSecurityGroups, boolean mergeWithExistingRefs) {
        /*
         * Make sure we don't overwrite certain existing links
         */
        SecurityRuleGroups updatedSecurityGroups = newSecurityGroups;
        if (mergeWithExistingRefs) {
            SecurityRuleGroups dsSecurityGroups = readSecurityGroupsFromDs(newSecurityGroups.getTenantId(),
                    newSecurityGroups.getUuid());
            if (dsSecurityGroups != null) {
                SecurityRuleGroupsBuilder bob = new SecurityRuleGroupsBuilder(newSecurityGroups);
                bob.setPorts(merge(dsSecurityGroups.getPorts(), newSecurityGroups.getPorts()));
                updatedSecurityGroups = bob.build();
            } else {
                updatedSecurityGroups = newSecurityGroups;
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
        } else {
            LOG.error("Failed to write logical securityGroups {} to datastore.", updatedSecurityGroups.getUuid()
                .getValue());
            return;
        }
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (updatedSecurityGroups.getPorts() != null && mergeWithExistingRefs) {
            for (Uuid portId : updatedSecurityGroups.getPorts()) {
                Port port = UlnDatastoreApi.readPortFromDs(updatedSecurityGroups.getTenantId(), portId);
                if (port != null) {
                    Set<Uuid> set = new HashSet<>();
                    if (port.getSecurityRulesGroups() != null) {
                        set.addAll(port.getSecurityRulesGroups());
                    }
                    set.add(updatedSecurityGroups.getUuid());
                    PortBuilder builder = new PortBuilder(port);
                    builder.setSecurityRulesGroups(new ArrayList<>(set));
                    UlnDatastoreApi.submitPortToDs(builder.build());
                }
            }
        }
    }

    public static SecurityRuleGroups readSecurityGroupsFromDs(Uuid tenantId, Uuid securityGroupId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<SecurityRuleGroups> potentialSecurityGroup = readFromDs(
                UlnIidFactory.securityGroupsIid(tenantId, securityGroupId), t);
        if (!potentialSecurityGroup.isPresent()) {
            LOG.debug("Logical SecurityGroup {} does not exist.", securityGroupId.getValue());
            return null;
        }
        return potentialSecurityGroup.get();
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
                            UlnDatastoreApi.submitPortToDs(builder.build());
                        }
                    }
                }
            }
        }
    }

    /*
     * Port
     */
    public static void submitPortToDs(Port port) {
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.portIid(port.getTenantId(), port.getUuid()), port, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical port {} to datastore.", port.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical port {} to datastore.", port.getUuid().getValue());
        }
    }

    public static Port readPortFromDs(Uuid tenantId, Uuid portId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<Port> potentialPort = readFromDs(UlnIidFactory.portIid(tenantId, portId), t);
        if (!potentialPort.isPresent()) {
            LOG.warn("Logical Port {} does not exist.", portId.getValue());
            return null;
        }
        return potentialPort.get();
    }

    public static void removePortFromDsIfExists(Uuid tenantId, Uuid portId) {
        removePortFromDsIfExists(tenantId, portId, true);
    }

    public static void removePortFromDsIfExists(Uuid tenantId, Uuid portId, boolean updateExistingRefs) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<Port> oldOption = removeIfExists(UlnIidFactory.portIid(tenantId, portId), t);
        /*
         * Make sure other logical network nodes links are updated as well
         */
        if (oldOption.isPresent() && updateExistingRefs) {
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
        }
    }

    /*
     * Edge
     */
    public static void submitEdgeToDs(Edge edge) {
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.edgeIid(edge.getTenantId(), edge.getUuid()), edge, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical edge {} to datastore.", edge.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical edge {} to datastore.", edge.getUuid().getValue());
        }
    }

    public static Edge readEdgeFromDs(Uuid tenantId, Uuid edgeId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<Edge> potentialEdge = readFromDs(UlnIidFactory.edgeIid(tenantId, edgeId), t);
        if (!potentialEdge.isPresent()) {
            LOG.warn("Logical Edge {} does not exist.", edgeId.getValue());
            return null;
        }
        return potentialEdge.get();
    }

    public static void removeEdgeFromDsIfExists(Uuid tenantId, Uuid edgeId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<Edge> oldOptional = removeIfExists(UlnIidFactory.edgeIid(tenantId, edgeId), t);
        if (oldOptional.isPresent()) {
            Edge edge = oldOptional.get();
            Port leftPort = readPortFromDs(tenantId, edge.getLeftPortId());
            if (leftPort != null && leftPort.getEdgeId() != null) {
                PortBuilder builder = new PortBuilder(leftPort);
                builder.setEdgeId(null);
                submitPortToDs(builder.build());
            }
            Port rightPort = readPortFromDs(tenantId, edge.getRightPortId());
            if (rightPort != null && rightPort.getEdgeId() != null) {
                PortBuilder builder = new PortBuilder(rightPort);
                builder.setEdgeId(null);
                submitPortToDs(builder.build());
            }

        }
    }

    /*
     * EndpointLocation
     */
    public static void submitEndpointLocationToDs(EndpointLocation endpointLocation) {
        WriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        t.put(logicalDatastoreType,
                UlnIidFactory.endpointLocationIid(endpointLocation.getTenantId(), endpointLocation.getUuid()),
                endpointLocation, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical endpointLocation {} to datastore.", endpointLocation.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical endpointLocation {} to datastore.", endpointLocation.getUuid()
                .getValue());
        }
    }

    public static EndpointLocation readEndpointLocationFromDs(Uuid tenantId, Uuid endpointLocationId) {
        ReadTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        Optional<EndpointLocation> potentialEndpointLocation = readFromDs(
                UlnIidFactory.endpointLocationIid(tenantId, endpointLocationId), t);
        if (!potentialEndpointLocation.isPresent()) {
            LOG.warn("Logical EndpointLocation {} does not exist.", endpointLocationId.getValue());
            return null;
        }
        return potentialEndpointLocation.get();
    }

    public static void removeEndpointLocationFromDsIfExists(Uuid tenantId, Uuid endpointLocationId) {
        ReadWriteTransaction t = UlnMapperDatastoreDependency.getDataProvider().newReadWriteTransaction();
        Optional<EndpointLocation> oldOptional = removeIfExists(
                UlnIidFactory.endpointLocationIid(tenantId, endpointLocationId), t);
        if (oldOptional.isPresent()) {
            EndpointLocation epLoc = oldOptional.get();
            if (epLoc.getPort() != null) {
                for (Uuid port : epLoc.getPort()) {
                    removePortFromDsIfExists(tenantId, port);
                }
            }
        }
    }

    /*
     * Common helper methods
     */
    private static <T extends DataObject> Optional<T> readFromDs(InstanceIdentifier<T> path, ReadTransaction rTx) {
        CheckedFuture<Optional<T>, ReadFailedException> resultFuture = rTx.read(logicalDatastoreType, path);
        try {
            return resultFuture.checkedGet();
        } catch (ReadFailedException e) {
            LOG.warn("Read failed from DS.", e);
            return Optional.absent();
        }
    }

    private static boolean submitToDs(WriteTransaction wTx) {
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
        try {
            submitFuture.checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Transaction commit failed to DS.", e);
            return false;
        }
    }

    private static <T extends DataObject> Optional<T> removeIfExists(InstanceIdentifier<T> path,
            ReadWriteTransaction rwTx) {
        Optional<T> potentialResult = readFromDs(path, rwTx);
        if (potentialResult.isPresent()) {
            rwTx.delete(logicalDatastoreType, path);
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
