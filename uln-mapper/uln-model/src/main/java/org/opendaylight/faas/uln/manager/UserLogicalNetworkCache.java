/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLogicalNetworkCache {

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkCache.class);

    private Uuid tenantId;

    private Map<Uuid, LogicalSwitchMappingInfo> lswStore;
    private Map<Uuid, LogicalRouterMappingInfo> lrStore;
    private Map<Uuid, SecurityRuleGroupsMappingInfo> securityRuleGroupsStore;
    private Map<Uuid, SubnetMappingInfo> subnetStore;
    private Map<Uuid, PortMappingInfo> portStore;
    private Map<Uuid, EdgeMappingInfo> edgeStore;
    private Map<Uuid, EndpointLocationMappingInfo> epLocationStore;

    public UserLogicalNetworkCache(Uuid tenantId) {
        super();
        this.setTenantId(tenantId);
        lswStore = new HashMap<Uuid, LogicalSwitchMappingInfo>();
        lrStore = new HashMap<Uuid, LogicalRouterMappingInfo>();
        securityRuleGroupsStore = new HashMap<Uuid, SecurityRuleGroupsMappingInfo>();
        subnetStore = new HashMap<Uuid, SubnetMappingInfo>();
        portStore = new HashMap<Uuid, PortMappingInfo>();
        edgeStore = new HashMap<Uuid, EdgeMappingInfo>();
        epLocationStore = new HashMap<Uuid, EndpointLocationMappingInfo>();
    }

    public Uuid getTenantId() {
        return tenantId;
    }

    public void setTenantId(Uuid tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isLswAlreadyCached(LogicalSwitch lsw) {
        boolean found = false;

        Uuid lswId = lsw.getUuid();
        if (this.lswStore.get(lswId) != null) {
            found = true;
        }

        return found;
    }

    public boolean isLrAlreadyCached(LogicalRouter lr) {
        boolean found = false;

        Uuid lrId = lr.getUuid();
        if (this.lrStore.get(lrId) != null) {
            found = true;
        }

        return found;
    }

    public void markLswAsRendered(LogicalSwitch lsw, NodeId renderedLswId) {
        Uuid lswId = lsw.getUuid();
        this.lswStore.get(lswId).markAsRendered(renderedLswId);
    }

    public void markLrAsRendered(LogicalRouter lr, NodeId renderedLrId) {
        Uuid lrId = lr.getUuid();
        this.lswStore.get(lrId).markAsRendered(renderedLrId);
    }

    public boolean isSecurityRuleGroupsAlreadyCached(SecurityRuleGroups ruleGroups) {
        boolean found = false;

        Uuid ruleGroupsId = ruleGroups.getUuid();
        if (this.securityRuleGroupsStore.get(ruleGroupsId) != null) {
            found = true;
        }

        return found;
    }

    public void markSecurityRuleGroupsAsRendered(SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        this.lswStore.get(ruleGroupsId).setServiceHasBeenRendered(true);
    }

    public boolean isSubnetAlreadyCached(Subnet subnet) {
        boolean found = false;

        Uuid subnetId = subnet.getUuid();
        if (this.subnetStore.get(subnetId) != null) {
            found = true;
        }

        return found;
    }

    public void markSubnetAsRendered(Subnet subnet) {
        Uuid subnetId = subnet.getUuid();
        this.lswStore.get(subnetId).setServiceHasBeenRendered(true);
    }

    public boolean isPortAlreadyCached(Port port) {
        boolean found = false;

        Uuid portId = port.getUuid();
        if (this.portStore.get(portId) != null) {
            found = true;
        }

        return found;
    }

    public void markPortAsRendered(Port port) {
        Uuid portId = port.getUuid();
        this.lswStore.get(portId).setServiceHasBeenRendered(true);
    }

    public boolean isEdgeAlreadyCached(Edge edge) {
        boolean found = false;

        Uuid edgeId = edge.getUuid();
        if (this.edgeStore.get(edgeId) != null) {
            found = true;
        }

        return found;
    }

    public void markEdgeAsRendered(Edge edge) {
        Uuid edgeId = edge.getUuid();
        this.lswStore.get(edgeId).setServiceHasBeenRendered(true);
    }

    public boolean isEpLocationAlreadyCached(EndpointLocation epLocation) {
        boolean found = false;

        Uuid epLocationId = epLocation.getUuid();
        if (this.epLocationStore.get(epLocationId) != null) {
            found = true;
        }

        return found;

    }

    public void markEpLocationAsRendered(EndpointLocation epLocation) {
        Uuid epLocationId = epLocation.getUuid();
        this.lswStore.get(epLocationId).setServiceHasBeenRendered(true);
    }

    public void cacheLsw(LogicalSwitch lsw) {
        if (this.isLswAlreadyCached(lsw) == true) {
            return;
        }

        this.lswStore.put(lsw.getUuid(), new LogicalSwitchMappingInfo(lsw));
    }

    public void cacheLr(LogicalRouter lr) {
        if (this.isLrAlreadyCached(lr) == true) {
            return;
        }

        this.lrStore.put(lr.getUuid(), new LogicalRouterMappingInfo(lr));
    }

    public void cacheSecurityRuleGroups(SecurityRuleGroups ruleGroups) {
        if (this.isSecurityRuleGroupsAlreadyCached(ruleGroups) == true) {
            return;
        }

        this.securityRuleGroupsStore.put(ruleGroups.getUuid(), new SecurityRuleGroupsMappingInfo(ruleGroups));
    }

    public void cacheSubnet(Subnet subnet) {
        if (this.isSubnetAlreadyCached(subnet) == true) {
            return;
        }

        this.subnetStore.put(subnet.getUuid(), new SubnetMappingInfo(subnet));
    }

    public void cachePort(Port port) {
        if (this.isPortAlreadyCached(port) == true) {
            return;
        }

        this.portStore.put(port.getUuid(), new PortMappingInfo(port));
    }

    public void cacheEdge(Edge edge) {
        if (this.isEdgeAlreadyCached(edge) == true) {
            return;
        }

        this.edgeStore.put(edge.getUuid(), new EdgeMappingInfo(edge));
    }

    public void cacheEpLocation(EndpointLocation epLocation) {
        if (this.isEpLocationAlreadyCached(epLocation) == true) {
            return;
        }

        this.epLocationStore.put(epLocation.getUuid(), new EndpointLocationMappingInfo(epLocation));
    }

    public boolean isLswRendered(LogicalSwitch lsw) {
        if (this.isLswAlreadyCached(lsw) == false) {
            return false;
        }
        return this.lswStore.get(lsw.getUuid()).hasServiceBeenRendered();
    }

    public boolean isLrRendered(LogicalRouter lr) {
        if (this.isLrAlreadyCached(lr) == false) {
            return false;
        }
        return this.lrStore.get(lr.getUuid()).hasServiceBeenRendered();
    }

    public boolean isSubnetRendered(Subnet subnet) {
        if (this.isSubnetAlreadyCached(subnet) == false) {
            return false;
        }
        return this.subnetStore.get(subnet.getUuid()).hasServiceBeenRendered();
    }

    public boolean isPortRendered(Port port) {
        if (this.isPortAlreadyCached(port) == false) {
            return false;
        }
        return this.portStore.get(port.getUuid()).hasServiceBeenRendered();
    }

    public boolean isSecurityRuleGroupsRendered(SecurityRuleGroups ruleGroups) {
        if (this.isSecurityRuleGroupsAlreadyCached(ruleGroups) == false) {
            return false;
        }
        return this.securityRuleGroupsStore.get(ruleGroups.getUuid()).hasServiceBeenRendered();
    }

    public boolean isEdgeRendered(Edge edge) {
        if (this.isEdgeAlreadyCached(edge) == false) {
            return false;
        }
        return this.edgeStore.get(edge.getUuid()).hasServiceBeenRendered();
    }

    public boolean isEpLocationRendered(EndpointLocation epLocation) {
        if (this.isEpLocationAlreadyCached(epLocation) == false) {
            return false;
        }
        return this.epLocationStore.get(epLocation.getUuid()).hasServiceBeenRendered();
    }

    public EdgeMappingInfo findEpLocationSubnetEdge(EndpointLocation epLocation) {
        // TODO Auto-generated method stub
        return null;
    }

    public PortMappingInfo findOtherPortInEdge(EdgeMappingInfo epEdge, Uuid epPortId) {
        // TODO Auto-generated method stub
        return null;
    }

    public SubnetMappingInfo findSubnetFromItsPort(PortMappingInfo subnetPort) {
        // TODO Auto-generated method stub
        return null;
    }

    public EdgeMappingInfo findSubnetLswEdge(SubnetMappingInfo subnet) {
        // TODO Auto-generated method stub
        return null;
    }

    public PortMappingInfo findSubnetPortOnEdge(EdgeMappingInfo subnetLswEdge) {
        // TODO Auto-generated method stub
        return null;
    }

    public LogicalSwitchMappingInfo findLswFromItsPort(PortMappingInfo lswPort) {
        // TODO Auto-generated method stub
        return null;
    }

}
