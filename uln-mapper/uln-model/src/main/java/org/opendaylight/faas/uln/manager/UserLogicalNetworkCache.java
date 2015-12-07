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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLogicalNetworkCache {

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkCache.class);

    private Uuid tenantId;

    private Map<Uuid, LogicalSwitch> unrenderedLswStore;
    private Map<Uuid, LogicalSwitch> renderedLswStore;
    private Map<Uuid, LogicalRouter> unrenderedLrStore;
    private Map<Uuid, LogicalRouter> renderedLrStore;
    private Map<Uuid, SecurityRuleGroups> unrenderedSecurityRuleGroupsStore;
    private Map<Uuid, SecurityRuleGroups> renderedSecurityRuleGroupsStore;
    private Map<Uuid, Subnet> unrenderedSubnetStore;
    private Map<Uuid, Subnet> renderedSubnetStore;
    private Map<Uuid, Port> unrenderedPortStore;
    private Map<Uuid, Port> renderedPortStore;
    private Map<Uuid, Edge> unrenderedEdgeStore;
    private Map<Uuid, Edge> renderedEdgeStore;
    private Map<Uuid, EndpointLocation> unrenderedEpLocationStore;
    private Map<Uuid, EndpointLocation> renderedEpLocationStore;

    public UserLogicalNetworkCache(Uuid tenantId) {
        super();
        this.setTenantId(tenantId);
        unrenderedLswStore = new HashMap<Uuid, LogicalSwitch>();
        renderedLswStore = new HashMap<Uuid, LogicalSwitch>();
        unrenderedLrStore = new HashMap<Uuid, LogicalRouter>();
        renderedLrStore = new HashMap<Uuid, LogicalRouter>();
        unrenderedSecurityRuleGroupsStore = new HashMap<Uuid, SecurityRuleGroups>();
        renderedSecurityRuleGroupsStore = new HashMap<Uuid, SecurityRuleGroups>();
        unrenderedSubnetStore = new HashMap<Uuid, Subnet>();
        renderedSubnetStore = new HashMap<Uuid, Subnet>();
        unrenderedPortStore = new HashMap<Uuid, Port>();
        renderedPortStore = new HashMap<Uuid, Port>();
        unrenderedEdgeStore = new HashMap<Uuid, Edge>();
        renderedEdgeStore = new HashMap<Uuid, Edge>();
        unrenderedEpLocationStore = new HashMap<Uuid, EndpointLocation>();
        renderedEpLocationStore = new HashMap<Uuid, EndpointLocation>();
    }

    public Uuid getTenantId() {
        return tenantId;
    }

    public void setTenantId(Uuid tenantId) {
        this.tenantId = tenantId;
    }

    public Map<Uuid, LogicalSwitch> getUnrenderedLswStore() {
        return unrenderedLswStore;
    }

    public void setUnrenderedLswStore(Map<Uuid, LogicalSwitch> unrenderedLswStore) {
        this.unrenderedLswStore = unrenderedLswStore;
    }

    public Map<Uuid, LogicalSwitch> getRenderedLswStore() {
        return renderedLswStore;
    }

    public void setRenderedLswStore(Map<Uuid, LogicalSwitch> renderedLswStore) {
        this.renderedLswStore = renderedLswStore;
    }

    public Map<Uuid, LogicalRouter> getUnrenderedLrStore() {
        return unrenderedLrStore;
    }

    public void setUnrenderedLrStore(Map<Uuid, LogicalRouter> unrenderedLrStore) {
        this.unrenderedLrStore = unrenderedLrStore;
    }

    public Map<Uuid, LogicalRouter> getRenderedLrStore() {
        return renderedLrStore;
    }

    public void setRenderedLrStore(Map<Uuid, LogicalRouter> renderedLrStore) {
        this.renderedLrStore = renderedLrStore;
    }

    public boolean isLswAlreadyCached(LogicalSwitch lsw) {
        boolean found = false;

        Uuid lswId = lsw.getUuid();
        if (this.unrenderedLswStore.get(lswId) != null || this.renderedLswStore.get(lswId) != null) {
            found = true;
        }

        return found;
    }

    public boolean isLrAlreadyCached(LogicalRouter lr) {
        boolean found = false;

        Uuid lrId = lr.getUuid();
        if (this.unrenderedLrStore.get(lrId) != null || this.renderedLrStore.get(lrId) != null) {
            found = true;
        }

        return found;
    }

    public void markLswAsRendered(LogicalSwitch lsw) {
        Uuid lswId = lsw.getUuid();
        this.renderedLswStore.put(lswId, lsw);
        this.unrenderedLswStore.remove(lswId);
    }

    public void markLrAsRendered(LogicalRouter lr) {
        Uuid lrId = lr.getUuid();
        this.renderedLrStore.put(lrId, lr);
        this.unrenderedLrStore.remove(lrId);
    }

    public boolean isSecurityRuleGroupsAlreadyCached(SecurityRuleGroups ruleGroups) {
        boolean found = false;

        Uuid ruleGroupsId = ruleGroups.getUuid();
        if (this.unrenderedSecurityRuleGroupsStore.get(ruleGroupsId) != null
                || this.renderedSecurityRuleGroupsStore.get(ruleGroupsId) != null) {
            found = true;
        }

        return found;
    }

    public void markSecurityRuleGroupsAsRendered(SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        this.renderedSecurityRuleGroupsStore.put(ruleGroupsId, ruleGroups);
        this.unrenderedSecurityRuleGroupsStore.remove(ruleGroupsId);
    }

    public Map<Uuid, SecurityRuleGroups> getUnrenderedSecurityRuleGroupsStore() {
        return unrenderedSecurityRuleGroupsStore;
    }

    public void setUnrenderedSecurityRuleGroupsStore(Map<Uuid, SecurityRuleGroups> unrenderedSecurityRuleGroupsStore) {
        this.unrenderedSecurityRuleGroupsStore = unrenderedSecurityRuleGroupsStore;
    }

    public Map<Uuid, SecurityRuleGroups> getRenderedSecurityRuleGroupsStore() {
        return renderedSecurityRuleGroupsStore;
    }

    public void setRenderedSecurityRuleGroupsStore(Map<Uuid, SecurityRuleGroups> renderedSecurityRuleGroupsStore) {
        this.renderedSecurityRuleGroupsStore = renderedSecurityRuleGroupsStore;
    }

    public boolean isSubnetAlreadyCached(Subnet subnet) {
        boolean found = false;

        Uuid subnetId = subnet.getUuid();
        if (this.unrenderedSubnetStore.get(subnetId) != null || this.renderedSubnetStore.get(subnetId) != null) {
            found = true;
        }

        return found;
    }

    public void markSubnetAsRendered(Subnet subnet) {
        Uuid subnetId = subnet.getUuid();
        this.renderedSubnetStore.put(subnetId, subnet);
        this.unrenderedSubnetStore.remove(subnetId);
    }

    public Map<Uuid, Subnet> getUnrenderedSubnetStore() {
        return unrenderedSubnetStore;
    }

    public void setUnrenderedSubnetStore(Map<Uuid, Subnet> unrenderedSubnetStore) {
        this.unrenderedSubnetStore = unrenderedSubnetStore;
    }

    public Map<Uuid, Subnet> getRenderedSubnetStore() {
        return renderedSubnetStore;
    }

    public void setRenderedSubnetStore(Map<Uuid, Subnet> renderedSubnetStore) {
        this.renderedSubnetStore = renderedSubnetStore;
    }

    public boolean isPortAlreadyCached(Port port) {
        boolean found = false;

        Uuid portId = port.getUuid();
        if (this.unrenderedPortStore.get(portId) != null || this.renderedPortStore.get(portId) != null) {
            found = true;
        }

        return found;
    }

    public void markPortAsRendered(Port port) {
        Uuid portId = port.getUuid();
        this.renderedPortStore.put(portId, port);
        this.unrenderedPortStore.remove(portId);
    }

    public Map<Uuid, Port> getUnrenderedPortStore() {
        return unrenderedPortStore;
    }

    public void setUnrenderedPortStore(Map<Uuid, Port> unrenderedPortStore) {
        this.unrenderedPortStore = unrenderedPortStore;
    }

    public Map<Uuid, Port> getRenderedPortStore() {
        return renderedPortStore;
    }

    public void setRenderedPortStore(Map<Uuid, Port> renderedPortStore) {
        this.renderedPortStore = renderedPortStore;
    }

    public boolean isEdgeAlreadyCached(Edge edge) {
        boolean found = false;

        Uuid edgeId = edge.getUuid();
        if (this.unrenderedEdgeStore.get(edgeId) != null || this.renderedEdgeStore.get(edgeId) != null) {
            found = true;
        }

        return found;
    }

    public void markEdgeAsRendered(Edge edge) {
        Uuid edgeId = edge.getUuid();
        this.renderedEdgeStore.put(edgeId, edge);
        this.unrenderedEdgeStore.remove(edgeId);
    }

    public Map<Uuid, Edge> getUnrenderedEdgeStore() {
        return unrenderedEdgeStore;
    }

    public void setUnrenderedEdgeStore(Map<Uuid, Edge> unrenderedEdgeStore) {
        this.unrenderedEdgeStore = unrenderedEdgeStore;
    }

    public Map<Uuid, Edge> getRenderedEdgeStore() {
        return renderedEdgeStore;
    }

    public void setRenderedEdgeStore(Map<Uuid, Edge> renderedEdgeStore) {
        this.renderedEdgeStore = renderedEdgeStore;
    }

    public boolean isEpLocationAlreadyCached(EndpointLocation epLocation) {
        boolean found = false;

        Uuid epLocationId = epLocation.getUuid();
        if (this.unrenderedEpLocationStore.get(epLocationId) != null
                || this.renderedEpLocationStore.get(epLocationId) != null) {
            found = true;
        }

        return found;
    }

    public void markEpLocationAsRendered(EndpointLocation epLocation) {
        Uuid epLocationId = epLocation.getUuid();
        this.renderedEpLocationStore.put(epLocationId, epLocation);
        this.unrenderedEpLocationStore.remove(epLocationId);
    }

    public Map<Uuid, EndpointLocation> getUnrenderedEpLocationStore() {
        return unrenderedEpLocationStore;
    }

    public void setUnrenderedEpLocationStore(Map<Uuid, EndpointLocation> unrenderedEpLocationStore) {
        this.unrenderedEpLocationStore = unrenderedEpLocationStore;
    }

    public Map<Uuid, EndpointLocation> getRenderedEpLocationStore() {
        return renderedEpLocationStore;
    }

    public void setRenderedEpLocationStore(Map<Uuid, EndpointLocation> renderedEpLocationStore) {
        this.renderedEpLocationStore = renderedEpLocationStore;
    }

}
