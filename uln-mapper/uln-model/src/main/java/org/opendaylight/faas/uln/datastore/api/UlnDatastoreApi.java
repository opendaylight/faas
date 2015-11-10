/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.uln.datastore.api;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.groups.container.security.groups.SecurityGroup;
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
    private static DataBroker dataProvider;

    public static void submitToDs(DataObject dao) {
        if (dao == null) {
            return;
        }
        if (dao instanceof Subnet) {
            submitSubnetToDs((Subnet) dao);
        } else if (dao instanceof LogicalRouter) {
            submitLogicalRouterToDs((LogicalRouter) dao);
        } else if (dao instanceof LogicalSwitch) {
            submitLogicalSwitchToDs((LogicalSwitch) dao);
        } else if (dao instanceof SecurityGroup) {
            submitSecurityGroupToDs((SecurityGroup) dao);
        } else if (dao instanceof Port) {
            submitPortToDs((Port) dao);
        } else if (dao instanceof Edge) {
            submitEdgeToDs((Edge) dao);
        } else if (dao instanceof EndpointLocation) {
            submitEndpointLocationToDs((EndpointLocation) dao);
        } else {
            LOG.error("submitToDs method doesn't support object of type {}", dao.getClass().getName());
        }
    }

    public static void setDataBroker(DataBroker dataBroker) {
        if (dataBroker != null)
            dataProvider = dataBroker;
    }

    /*
     * Subnet
     */
    private static void submitSubnetToDs(Subnet subnet) {
        WriteTransaction t = dataProvider.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.subnetIid(subnet.getTenantId(), subnet.getUuid()), subnet, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical subnet {} to datastore.", subnet.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical subnet {} to datastore.", subnet.getUuid().getValue());
        }
    }

    public static Subnet readSubnetFromDs(Uuid tenantId, Uuid subnetId) {
        ReadTransaction t = dataProvider.newReadOnlyTransaction();
        Optional<Subnet> potentialSubnet = readFromDs(UlnIidFactory.subnetIid(tenantId, subnetId), t);
        if (!potentialSubnet.isPresent()) {
            LOG.warn("Logical Subnet {} does not exist.", subnetId.getValue());
            return null;
        }
        return potentialSubnet.get();
    }

    public static void removeSubnetFromDsIfExists(Uuid tenantId, Uuid subnetId) {
        ReadWriteTransaction t = dataProvider.newReadWriteTransaction();
        removeIfExists(UlnIidFactory.subnetIid(tenantId, subnetId), t);
    }

    /*
     * Router
     */
    private static void submitLogicalRouterToDs(LogicalRouter router) {
        WriteTransaction t = dataProvider.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.logicalRouterIid(router.getTenantId(), router.getUuid()), router, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical router {} to datastore.", router.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical router {} to datastore.", router.getUuid().getValue());
        }
    }

    public static LogicalRouter readLogicalRouterFromDs(Uuid tenantId, Uuid routerId) {
        ReadTransaction t = dataProvider.newReadOnlyTransaction();
        Optional<LogicalRouter> potentialRouter = readFromDs(UlnIidFactory.logicalRouterIid(tenantId, routerId), t);
        if (!potentialRouter.isPresent()) {
            LOG.warn("Logical Router {} does not exist.", routerId.getValue());
            return null;
        }
        return potentialRouter.get();
    }

    public static void removeLogicalRouterFromDsIfExists(Uuid tenantId, Uuid routerId) {
        ReadWriteTransaction t = dataProvider.newReadWriteTransaction();
        removeIfExists(UlnIidFactory.logicalRouterIid(tenantId, routerId), t);
    }

    /*
     * Switch
     */
    private static void submitLogicalSwitchToDs(LogicalSwitch lswitch) {
        WriteTransaction t = dataProvider.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.logicalSwitchIid(lswitch.getTenantId(), lswitch.getUuid()), lswitch, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical switch {} to datastore.", lswitch.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical switch {} to datastore.", lswitch.getUuid().getValue());
        }
    }

    public static LogicalSwitch readLogicalSwitchFromDs(Uuid tenantId, Uuid switchId) {
        ReadTransaction t = dataProvider.newReadOnlyTransaction();
        Optional<LogicalSwitch> potentialSwitch = readFromDs(UlnIidFactory.logicalSwitchIid(tenantId, switchId), t);
        if (!potentialSwitch.isPresent()) {
            LOG.warn("Logical Switch {} does not exist.", switchId.getValue());
            return null;
        }
        return potentialSwitch.get();
    }

    public static void removeLogicalSwitchFromDsIfExists(Uuid tenantId, Uuid switchId) {
        ReadWriteTransaction t = dataProvider.newReadWriteTransaction();
        removeIfExists(UlnIidFactory.logicalSwitchIid(tenantId, switchId), t);
    }

    /*
     * SecurityGroup
     */
    private static void submitSecurityGroupToDs(SecurityGroup securityGroup) {
        WriteTransaction t = dataProvider.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.securityGroupIid(securityGroup.getTenantId(), securityGroup.getUuid()), securityGroup, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical securityGroup {} to datastore.", securityGroup.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical securityGroup {} to datastore.", securityGroup.getUuid().getValue());
        }
    }

    public static SecurityGroup readSecurityGroupFromDs(Uuid tenantId, Uuid securityGroupId) {
        ReadTransaction t = dataProvider.newReadOnlyTransaction();
        Optional<SecurityGroup> potentialSecurityGroup = readFromDs(UlnIidFactory.securityGroupIid(tenantId, securityGroupId), t);
        if (!potentialSecurityGroup.isPresent()) {
            LOG.warn("Logical SecurityGroup {} does not exist.", securityGroupId.getValue());
            return null;
        }
        return potentialSecurityGroup.get();
    }

    public static void removeSecurityGroupFromDsIfExists(Uuid tenantId, Uuid securityGroupId) {
        ReadWriteTransaction t = dataProvider.newReadWriteTransaction();
        removeIfExists(UlnIidFactory.securityGroupIid(tenantId, securityGroupId), t);
    }

    /*
     * Port
     */
    private static void submitPortToDs(Port port) {
        WriteTransaction t = dataProvider.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.portIid(port.getTenantId(), port.getUuid()), port, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical port {} to datastore.", port.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical port {} to datastore.", port.getUuid().getValue());
        }
    }

    public static Port readPortFromDs(Uuid tenantId, Uuid portId) {
        ReadTransaction t = dataProvider.newReadOnlyTransaction();
        Optional<Port> potentialPort = readFromDs(UlnIidFactory.portIid(tenantId, portId), t);
        if (!potentialPort.isPresent()) {
            LOG.warn("Logical Port {} does not exist.", portId.getValue());
            return null;
        }
        return potentialPort.get();
    }

    public static void removePortFromDsIfExists(Uuid tenantId, Uuid portId) {
        ReadWriteTransaction t = dataProvider.newReadWriteTransaction();
        removeIfExists(UlnIidFactory.portIid(tenantId, portId), t);
    }

    /*
     * Edge
     */
    private static void submitEdgeToDs(Edge edge) {
        WriteTransaction t = dataProvider.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.edgeIid(edge.getTenantId(), edge.getUuid()), edge, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical edge {} to datastore.", edge.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical edge {} to datastore.", edge.getUuid().getValue());
        }
    }

    public static Edge readEdgeFromDs(Uuid tenantId, Uuid edgeId) {
        ReadTransaction t = dataProvider.newReadOnlyTransaction();
        Optional<Edge> potentialEdge = readFromDs(UlnIidFactory.edgeIid(tenantId, edgeId), t);
        if (!potentialEdge.isPresent()) {
            LOG.warn("Logical Edge {} does not exist.", edgeId.getValue());
            return null;
        }
        return potentialEdge.get();
    }

    public static void removeEdgeFromDsIfExists(Uuid tenantId, Uuid edgeId) {
        ReadWriteTransaction t = dataProvider.newReadWriteTransaction();
        removeIfExists(UlnIidFactory.edgeIid(tenantId, edgeId), t);
    }

    /*
     * EndpointLocation
     */
    private static void submitEndpointLocationToDs(EndpointLocation endpointLocation) {
        WriteTransaction t = dataProvider.newWriteOnlyTransaction();
        t.put(logicalDatastoreType, UlnIidFactory.endpointLocationIid(endpointLocation.getTenantId(), endpointLocation.getUuid()), endpointLocation, true);
        if (submitToDs(t)) {
            LOG.debug("Wrote logical endpointLocation {} to datastore.", endpointLocation.getUuid().getValue());
        } else {
            LOG.error("Failed to write logical endpointLocation {} to datastore.", endpointLocation.getUuid().getValue());
        }
    }

    public static EndpointLocation readEndpointLocationFromDs(Uuid tenantId, Uuid endpointLocationId) {
        ReadTransaction t = dataProvider.newReadOnlyTransaction();
        Optional<EndpointLocation> potentialEndpointLocation = readFromDs(UlnIidFactory.endpointLocationIid(tenantId, endpointLocationId), t);
        if (!potentialEndpointLocation.isPresent()) {
            LOG.warn("Logical EndpointLocation {} does not exist.", endpointLocationId.getValue());
            return null;
        }
        return potentialEndpointLocation.get();
    }

    public static void removeEndpointLocationFromDsIfExists(Uuid tenantId, Uuid endpointLocationId) {
        ReadWriteTransaction t = dataProvider.newReadWriteTransaction();
        removeIfExists(UlnIidFactory.endpointLocationIid(tenantId, endpointLocationId), t);
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

    private static <T extends DataObject> Optional<T> removeIfExists(InstanceIdentifier<T> path, ReadWriteTransaction rwTx) {
        Optional<T> potentialResult = readFromDs(path, rwTx);
        if (potentialResult.isPresent()) {
            rwTx.delete(logicalDatastoreType, path);
        }
        return potentialResult;
    }
}
