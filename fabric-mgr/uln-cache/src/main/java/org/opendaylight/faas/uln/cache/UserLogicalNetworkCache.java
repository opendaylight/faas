/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.PortLocationAttributes.LocationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logical Network Cache within FaaS module.
 * It stores "enriched"logical network model stored from data store along with
 * operational state information such as rendered logical entities on fabric.
 * it is read only information except those state information regarding how rendering
 * is done.
 * The listeners provide synchronization from data store to the cache.
 * The state information is updated by the mapping engine.
 *
 */
public final class UserLogicalNetworkCache {

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkCache.class);

    private final Uuid tenantId;

    //From 3555 to 3999 are reserved glogl tag. assuming it is VLAN based for now
    public static final int GLOBAL_END_TAG = 3999;
    public static final int GLOBAL_START_TAG = 3555;

    private short global_tag = GLOBAL_START_TAG;

    private Map<Uuid, LogicalSwitchMappingInfo> lswStore;
    private Map<Uuid, LogicalRouterMappingInfo> lrStore;
    private final Map<NodeId, NodeId> renderedRouters;
    private final Map<RenderedLinkKey<RenderedRouter>, RenderedLayer3Link> renderedrLinks;
    //private Graph<NodeId, Link> topo;

    private Map<Uuid, SecurityRuleGroupsMappingInfo> securityRuleGroupsStore;
    private Map<Uuid, SubnetMappingInfo> subnetStore;
    private Map<Uuid, PortMappingInfo> portStore;
    private Map<Uuid, EdgeMappingInfo> edgeStore;
    private Map<Uuid, EndpointLocationMappingInfo> epLocationStore;

    private boolean hasExternalGateway = false;

/**
 * Constructor. each Cache has exactly one tenant owner.
 * @param tenantId - tenant identifier.
 */
    public UserLogicalNetworkCache(Uuid tenantId) {
        super();

        this.tenantId = tenantId;

        lswStore = new ConcurrentHashMap<>();
        lrStore = new ConcurrentHashMap<>();
        securityRuleGroupsStore = new ConcurrentHashMap<>();
        subnetStore = new ConcurrentHashMap<>();
        portStore = new ConcurrentHashMap<>();
        edgeStore = new ConcurrentHashMap<>();
        epLocationStore = new ConcurrentHashMap<>();

        this.renderedRouters = new ConcurrentHashMap<>();
        this.renderedrLinks = new ConcurrentHashMap<>();
    }


    public Map<RenderedLinkKey<RenderedRouter>, RenderedLayer3Link> getRenderedrLinks() {
        return renderedrLinks;
    }

    public void  addRenderedrLink(RenderedLinkKey<RenderedRouter> key, RenderedLayer3Link link) {
        renderedrLinks.put(key,  link);
    }

    public void  rmRenderedrLink(RenderedLinkKey<RenderedRouter> key, RenderedLayer3Link link) {
        renderedrLinks.remove(key);
    }




    public Uuid getTenantId() {
        return tenantId;
    }


    public synchronized int getGlobalTag() {
        return this.global_tag ++;
    }

    /**
     * Check if a logical switch already has a cache entry.
     * @param lsw - the logical switch object.
     * @return true if found.
     */
    public boolean isLswAlreadyCached(LogicalSwitch lsw) {
        return this.lswStore.get(lsw.getUuid()) != null;
    }

    /**
     * Check if a logical router already has a cache entry.
     * @param lr - the logical router object.
     * @return true if found
     */
    public boolean isLrAlreadyCached(LogicalRouter lr) {
        return this.lrStore.get(lr.getUuid()) != null;
    }

    /**
     * Cache a rendered logical router
     * @param fabricId - fabric identifier
     * @param renderedLr - the corresponding rendered logical router on a fabric.
     */
    public void addRenderedRouterOnFabric(NodeId fabricId, NodeId renderedLr) {
        this.renderedRouters.put(fabricId,  renderedLr);
    }

    public NodeId getRenderedRouterOnFabirc(NodeId fabricId) {
        return renderedRouters.get(fabricId);
    }



    public Map<NodeId, NodeId> getRenderedRouters() {
        return renderedRouters;
    }


    /**
     * Check if a given security group has been rendered.
     * @param ruleGroups - the group of rules to be rendered.
     * @return true if rendered, false otherwise.
     */
    public boolean isSecurityRuleGroupsAlreadyCached(SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        return this.securityRuleGroupsStore.get(ruleGroupsId) != null;
    }



    public boolean isHasExternalGateway() {
        return hasExternalGateway;
    }


    public void setHasExternalGateway(boolean hasExternalGateway) {
        this.hasExternalGateway = hasExternalGateway;
    }


    /**
     * To mark a security group's render status.
     * @param ruleGroups - the group of rules to be rendered.
     */
    public void markSecurityRuleGroupsAsRendered(SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        this.securityRuleGroupsStore.get(ruleGroupsId).setServiceHasBeenRendered(true);
    }

    /**
     * check if a subnet has been cached.
     * @param subnet - the subnet object to be rendered.
     * @return true if rendered, false otherwise.
     */
    public boolean isSubnetAlreadyCached(Subnet subnet) {
        Uuid subnetId = subnet.getUuid();
        return this.subnetStore.get(subnetId) != null;
    }

    /**
     * To mark a subnet object's rendere status.
     * @param subnet - rendered subnet object.
     */
    public void markSubnetAsRendered(Subnet subnet) {
        Uuid subnetId = subnet.getUuid();
        this.subnetStore.get(subnetId).setServiceHasBeenRendered(true);
    }

    /**
     * check if a port object has been cached.
     * @param port to be cached.
     * @return ture if found, false otherwise
     */
    public boolean isPortAlreadyCached(Port port) {
        Uuid portId = port.getUuid();
        return this.portStore.get(portId) != null;
    }

    /**
     * TO mark a port as rendered.
     * @param port to be rendered.
     */
    public void markPortAsRendered(Port port) {
        Uuid portId = port.getUuid();
        this.portStore.get(portId).setServiceHasBeenRendered(true);
    }

    /**
     * Check if an edge object has been cached.
     * @param edge to be cached.
     * @return true if found, false otherwise.
     */
    public boolean isEdgeAlreadyCached(Edge edge) {
        Uuid edgeId = edge.getUuid();
        return this.edgeStore.get(edgeId) != null;
    }

    /**
     * To mark an edge as rendered.
     * @param edge rendered object.
     */
    public void markEdgeAsRendered(Edge edge) {
        Uuid edgeId = edge.getUuid();
        this.edgeStore.get(edgeId).setServiceHasBeenRendered(true);
    }

    /**
     * Check if an Eplocaiton has been cached.
     * @param epLocation to be checked.
     * @return true if cached, false otherwise.
     */
    public boolean isEpLocationAlreadyCached(EndpointLocation epLocation) {
        Uuid epLocationId = epLocation.getUuid();
        return this.epLocationStore.get(epLocationId) != null;
    }

    /**
     * To mark an end point location as rendered.
     * @param epLocation - the object to be marked
     * @param renderedEpId - rendered Ep identifier.
     */
    public void markEpLocationAsRendered(EndpointLocation epLocation,
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid renderedEpId) {
        Uuid epLocationId = epLocation.getUuid();
        this.epLocationStore.get(epLocationId).setRenderedDeviceId(renderedEpId);
        this.epLocationStore.get(epLocationId).setServiceHasBeenRendered(true);
    }

    /**
     * To cache an LogicalSwitch.
     * @param lsw to be cached.
     */

    public void cacheLsw(LogicalSwitch lsw) {
        if (!this.isLswAlreadyCached(lsw)) {
            this.lswStore.put(lsw.getUuid(), new LogicalSwitchMappingInfo(lsw));
        }
    }

    /**
     * To cache an LogicalRouter.
     * @param lr to be cached.
     */

    public void cacheLr(LogicalRouter lr) {
        if (!this.isLrAlreadyCached(lr)) {
            this.lrStore.put(lr.getUuid(), new LogicalRouterMappingInfo(lr));
        }
    }

    /**
     * To cache an SecurityRuleGroups.
     * @param ruleGroups to be cached.
     */

    public void cacheSecurityRuleGroups(SecurityRuleGroups ruleGroups) {
        if (!this.isSecurityRuleGroupsAlreadyCached(ruleGroups)) {
            this.securityRuleGroupsStore.put(ruleGroups.getUuid(), new SecurityRuleGroupsMappingInfo(ruleGroups));
        }
    }

    /**
     * To cache an subnet.
     * @param subnet to be cached.
     */

    public void cacheSubnet(Subnet subnet) {
        if (!this.isSubnetAlreadyCached(subnet)) {
            this.subnetStore.put(subnet.getUuid(), new SubnetMappingInfo(subnet));
        }
    }

    /**
     * To cache an Port.
     * @param port to be cached.
     */

    public void cachePort(Port port) {
        if (!this.isPortAlreadyCached(port)) {
            this.portStore.put(port.getUuid(), new PortMappingInfo(port));
        }
    }

    /**
     * To cache an edge.
     * @param edge to be cached.
     */

    public void cacheEdge(Edge edge) {
        if (!this.isEdgeAlreadyCached(edge)) {
            this.edgeStore.put(edge.getUuid(), new EdgeMappingInfo(edge));
        }
    }

    /**
     * To cache an EndpointLocation.
     * @param epLocation to be cached.
     */
    public void cacheEpLocation(EndpointLocation epLocation) {
        if (!this.isEpLocationAlreadyCached(epLocation)) {
            this.epLocationStore.put(epLocation.getUuid(), new EndpointLocationMappingInfo(epLocation));
        }
    }

    /**
     * Check if an logical switch has been rendered.
     * @param lsw - the logical switch to be checked.
     * @param fabricID - the target fabric
     * @return true if rendered, false otherwise.
     */

    public boolean isLswRendered(LogicalSwitch lsw, NodeId fabricID) {
        if (!this.isLswAlreadyCached(lsw)) {
            return false;
        }
        return this.lswStore.get(lsw.getUuid()).hasServiceBeenRenderedOnFabric(fabricID);
    }

    /**
     * Check if an logical router has been rendered.
     * @param lr - the logical router to be checked.
     * @param fabricId - the target fabric
     * @return true if rendered, false otherwise.
     */
    public boolean isLrRendered(LogicalRouter lr, NodeId fabricId) {
        if (!this.isLrAlreadyCached(lr)) {
            return false;
        }
        return this.lrStore.get(lr.getUuid()).hasServiceBeenRenderedOnFabric(fabricId);
    }

    /**
     * Check if an subnet has been rendered.
     * @param subnet - the subnet to be checked.
     * @return true if rendered. false otherwise.
     */

    public boolean isSubnetRendered(Subnet subnet) {
        if (!this.isSubnetAlreadyCached(subnet)) {
            return false;
        }
        return this.subnetStore.get(subnet.getUuid()).hasServiceBeenRendered();
    }

    /**
     * Check if an Port has been rendered.
     * @param port - the port to be checked.
     * @return true if rendered. false otherwise.
     */

    public boolean isPortRendered(Port port) {
        if (!this.isPortAlreadyCached(port)) {
            return false;
        }
        return this.portStore.get(port.getUuid()).hasServiceBeenRendered();
    }

    /**
     * Check if a security group  has been rendered.
     * @param ruleGroups - the group to be checked.
     * @return true if rendered. false otherwise.
     */

    public boolean isSecurityRuleGroupsRendered(SecurityRuleGroups ruleGroups) {
        if (!this.isSecurityRuleGroupsAlreadyCached(ruleGroups)) {
            return false;
        }
        return this.securityRuleGroupsStore.get(ruleGroups.getUuid()).hasServiceBeenRendered();
    }

    /**
     * Check if an Edge has been rendered.
     * @param edge - the edge  to be checked.
     * @return true if rendered. false otherwise.
     */

    public boolean isEdgeRendered(Edge edge) {
        if (!this.isEdgeAlreadyCached(edge)) {
            return false;
        }
        return this.edgeStore.get(edge.getUuid()).hasServiceBeenRendered();
    }

    /**
     * Check if an EP has been rendered.
     * @param epLocation - the end point to be checked.
     * @return true if rendered. false otherwise.
     */
    public boolean isEpLocationRendered(EndpointLocation epLocation) {
        if (!this.isEpLocationAlreadyCached(epLocation)) {
            return false;
        }
        return this.epLocationStore.get(epLocation.getUuid()).hasServiceBeenRendered();
    }

    /**
     * Find edge that connects the given EP with its belonging subnet
     * @param epLocation - the end point location which attaches the subnet to be found.
     * @return the edge object to be cached.
     */
    public EdgeMappingInfo findEpLocationSubnetEdge(EndpointLocation epLocation) {

        Uuid epPortId = epLocation.getPort();
        PortMappingInfo epPort = this.portStore.get(epPortId);
        if (epPort == null) {
            return null;
        }

        Uuid edgeId = epPort.getPort().getEdgeId();
        return this.edgeStore.get(edgeId);
    }

    /**
     * Given an edge and one port, find the other port.
     * @param epEdge - the edge.
     * @param epPortId - one known port.
     * @return the other port.
     */
    public PortMappingInfo findOtherPortInEdge(EdgeMappingInfo epEdge, Uuid epPortId) {
        Uuid leftPortId = epEdge.getEdge().getLeftPortId();
        Uuid rightPortId = epEdge.getEdge().getRightPortId();

        Uuid otherPortId;
        if (leftPortId.equals(epPortId)) {
            otherPortId = rightPortId;
        } else if (rightPortId.equals(epPortId)) {
            otherPortId = leftPortId;
        } else {
            LOG.error("FABMGR: ERROR: findOtherPortInEdge: port id is wrong: ep={}, left={}, right={}",
                    epPortId.getValue(), leftPortId.getValue(), rightPortId.getValue());
            return null;
        }

        return this.portStore.get(otherPortId);
    }

    /**
     * Find the left port.
     * @param edge - the edge to be searched.
     * @return the left port.
     */
    public PortMappingInfo findLeftPortOnEdge(Edge edge) {
        Uuid leftPortId = edge.getLeftPortId();
        return this.portStore.get(leftPortId);
    }

    /**
     * Find the right port.
     * @param edge - the edge to be searched.
     * @return the right port.
     */

    public PortMappingInfo findRightPortOnEdge(Edge edge) {
        Uuid rightPortId = edge.getRightPortId();
        return this.portStore.get(rightPortId);
    }

    /**
     * Given a port, find the subnet to which this port belongs.
     *
     * @param subnetPort the given port
     * @return associated subnet object.
     */
    public SubnetMappingInfo findSubnetFromItsPort(PortMappingInfo subnetPort) {
        LocationType portLocationType = subnetPort.getPort().getLocationType();

        if (portLocationType != LocationType.SubnetType) {
            LOG.error("FABMGR: ERROR: wrong port type: {}", portLocationType.name());
            return null;
        }

        Uuid subnetId = subnetPort.getPort().getLocationId();
        return this.subnetStore.get(subnetId);
    }

    /**
     * Given a LSW, find its associated subnet. The ULN model allows a LSW
     * to have more than one subnet. However, this function only returns the
     * first one that it finds.
     *
     * @param lsw - the logical switch information.
     * @return - the associated subnet information.
     */
    public SubnetMappingInfo findSubnetFromLsw(LogicalSwitchMappingInfo lsw) {

        EdgeMappingInfo edge = this.findSubnetLswEdgeFromLsw(lsw);
        if (edge == null) {
            return null;
        }

        PortMappingInfo subnetPort = this.findSubnetPortOnEdge(edge);
        if (subnetPort == null) {
            return null;
        }

        return this.findSubnetFromItsPort(subnetPort);
    }

    /**
     * Given a subnet, find the edge that connects this subnet with
     * a logical switch. The ULN model allows multiple logical switches
     * to be connected to one subnet. To accommodate this bug (Bug 5144),
     * this function returns all the lswToSubnet edges.
     *
     * @param subnet - the subnet information.
     * @return - list of associated edges.
     */
    @Nonnull
    public List<EdgeMappingInfo> findAllSubnetLswEdgesFromSubnet(SubnetMappingInfo subnet) {
        List<EdgeMappingInfo> subnetLswEdgeList = new ArrayList<>();
        Uuid subnetId = subnet.getSubnet().getUuid();
        for (Entry<Uuid, EdgeMappingInfo> entry : this.edgeStore.entrySet()) {
            EdgeMappingInfo edge = entry.getValue();
            if (this.findEdgeType(edge) == LogicalEdgeType.LSW_SUBNET) {
                PortMappingInfo subnetPort = this.findSubnetPortInEdge(edge, subnetId);
                if (subnetPort != null) {
                    PortMappingInfo otherPort = this.findOtherPortInEdge(edge, subnetPort.getPort().getUuid());
                    if (otherPort != null) {
                        LocationType portType = otherPort.getPort().getLocationType();
                        if (portType == LocationType.SwitchType) {
                            subnetLswEdgeList.add(edge);
                        } else {
                            LOG.error("FABMGR: ERROR: findSubnetLswEdge: port should be lsw type: port={}, edge={}",
                                    otherPort.getPort().getUuid().getValue(), edge.getEdge().getUuid().getValue());
                        }
                    }
                }
            }
        }

        return subnetLswEdgeList;
    }

    /**
     * subnetLswEdgeList may contain multiple edges, because the
     * ULN model allows a subnet to connect to multiple LSWs (Bug 5144).
     * To handle this bug, we allow only one subnet-lsw edge to be
     * rendered. And we do this by always picking up the same edge in the list.
     * @param subnet to be searched.
     * @return Edge information associated with the subnet.
     */
    public EdgeMappingInfo findSingleSubnetLswEdgeFromSubnet(SubnetMappingInfo subnet) {
        List<EdgeMappingInfo> subnetLswEdgeList = this.findAllSubnetLswEdgesFromSubnet(subnet);
        if (subnetLswEdgeList.isEmpty()) {
            LOG.debug("FABMGR: findSingleSubnetLswEdge: cannot find subnetLswEdge in cache");
            return null;
        }

        /*
         * No LSW has been rendered for this subnet yet, so just pick
         * up the first edge in the list.
         */
        if (!subnet.hasServiceBeenRendered()) {
            return subnetLswEdgeList.get(0);
        }

        /*
         * Now that we know the LSW for this subnet has been chosen and rendered.
         * We need to find the edge that connects subnet with that LSW. Note that
         * we may not find the edge even when LSW is rendered. This could happen
         * if gateway is created by LR-LSW edge rendering before the LSW-Subnet edge
         * is cached.
         */
        EdgeMappingInfo subnetLswEdge = null;
        int renderedLswCounter = 0;
        for (EdgeMappingInfo edge : subnetLswEdgeList) {
            LogicalSwitchMappingInfo lsw = this.findLswFromSubnetLswEdge(edge);
            if (lsw != null && lsw.hasServiceBeenRendered()) {
                subnetLswEdge = edge;
                renderedLswCounter++;
            }
        }
        if (subnetLswEdge != null && renderedLswCounter != 1) {
            LOG.error("FABMGR: ERROR: findSingleSubnetLswEdgeFromSubnet: renderedLswCounter={}", renderedLswCounter);
        }

        return subnetLswEdge;
    }

    public EdgeMappingInfo findSubnetLswEdgeFromLsw(LogicalSwitchMappingInfo lsw) {
        EdgeMappingInfo subnetLswEdge = null;

        Uuid lswId = lsw.getLsw().getUuid();
        for (Entry<Uuid, EdgeMappingInfo> entry : this.edgeStore.entrySet()) {
            EdgeMappingInfo edge = entry.getValue();
            if (this.findEdgeType(edge) == LogicalEdgeType.LSW_SUBNET) {
                PortMappingInfo lswPort = this.findLswPortOnEdge(edge);
                if (lswPort != null && lswPort.getPort().getLocationId().equals(lswId)) {
                    PortMappingInfo otherPort = this.findOtherPortInEdge(edge, lswPort.getPort().getUuid());
                    if (otherPort != null && otherPort.getPort().getLocationType() == LocationType.SubnetType) {
                        subnetLswEdge = edge;
                    } else {
                        LOG.error("FABMGR: ERROR: findSubnetLswEdge: otherPort is not subnet type");
                    }
                    break;
                }
            }
        }

        return subnetLswEdge;
    }

    private boolean isEdgeConnectingTheLRToALSW(LogicalRouterMappingInfo lr, EdgeMappingInfo edge)
    {
        if (this.findEdgeType(edge) == LogicalEdgeType.LR_LSW) {
            PortMappingInfo lrPort = this.findLrPortOnEdge(edge);
            Uuid lrId = lr.getLr().getUuid();
            if (lrPort != null && lrPort.getPort().getLocationId().equals(lrId)) {
                PortMappingInfo otherPort = this.findOtherPortInEdge(edge, lrPort.getPort().getUuid());
                if (otherPort != null && otherPort.getPort().getLocationType() == LocationType.SwitchType) {
                    return true;
                } else {
                    LOG.error("FABMGR: ERROR: findLrLswEdge: otherPort is not LSW type: {}",
                        otherPort.getPort().getLocationType().toString());
                }
            }
        }
        return false;
    }

    /**
     * Find all edges connect to the given logical router.
     * @param lr - the logical router to be search against.
     * @return list of edges connect to the given logical router.
     */
    @Nonnull
    public List<EdgeMappingInfo> findLrLswEdge(LogicalRouterMappingInfo lr) {
        List<EdgeMappingInfo> lrLswEdges = new ArrayList<>();

        for (Entry<Uuid, EdgeMappingInfo> entry : this.edgeStore.entrySet()) {
            EdgeMappingInfo edge = entry.getValue();
            if (isEdgeConnectingTheLRToALSW(lr, edge)) {
                lrLswEdges.add(edge);
            }
        }
        return lrLswEdges;
    }


    public EdgeMappingInfo findLswLrEdge(LogicalSwitchMappingInfo lsw) {
        EdgeMappingInfo lswLrEdge = null;

        Uuid lswId = lsw.getLsw().getUuid();
        for (Entry<Uuid, EdgeMappingInfo> entry : this.edgeStore.entrySet()) {
            EdgeMappingInfo edge = entry.getValue();
            if (this.findEdgeType(edge) == LogicalEdgeType.LR_LSW) {
                PortMappingInfo lswPort = this.findLswPortOnEdge(edge);
                if (lswPort != null && lswPort.getPort().getLocationId().equals(lswId)) {
                    PortMappingInfo otherPort = this.findOtherPortInEdge(edge, lswPort.getPort().getUuid());
                    if (otherPort != null && otherPort.getPort().getLocationType() == LocationType.RouterType) {
                        lswLrEdge = edge;
                    } else {
                        LOG.error("FABMGR: ERROR: findLswLrEdge: otherPort is not LR type: {}",
                                otherPort.getPort().getLocationType().toString());
                    }
                    break;
                }
            }
        }

        return lswLrEdge;
    }

    /**
     * Find the edge connects the given port.
     * @param port - the target port.
     * @return the Endge connects the given port.
     */
    @Nonnull
    public EdgeMappingInfo findTheEdge(PortMappingInfo port) {
        for (Entry<Uuid, EdgeMappingInfo> entry : this.edgeStore.entrySet()) {
            EdgeMappingInfo edge = entry.getValue();
            if (edge.getEdge().getUuid().equals(port.getPort().getEdgeId())) {
                return edge;
            }
        }
        return null;
    }


    public PortMappingInfo findPortFromPortId(Uuid portId) {
        return this.portStore.get(portId);
    }

    public LogicalSwitchMappingInfo findLswFromLswId(Uuid lswId) {
        return this.lswStore.get(lswId);
    }

    public LogicalRouterMappingInfo findLrFromLrId(Uuid lrId) {
        return this.lrStore.get(lrId);
    }

    public EndpointLocationMappingInfo findEpLocationFromEpLocationId(Uuid epLocationId) {
        return this.epLocationStore.get(epLocationId);
    }

    public SecurityRuleGroupsMappingInfo findSecurityRuleGroupsFromRuleGroupsId(Uuid ruleGroupsId) {
        return this.securityRuleGroupsStore.get(ruleGroupsId);
    }

    public SubnetMappingInfo findSubnetFromSubnetId(Uuid subnetId) {
        return this.subnetStore.get(subnetId);
    }

    public PortMappingInfo findSubnetPortInEdge(EdgeMappingInfo edge, Uuid subnetId) {
        PortMappingInfo subnetPort = null;
        Uuid leftPortId = edge.getEdge().getLeftPortId();
        Uuid rightPortId = edge.getEdge().getRightPortId();

        if (this.isPortInSubnet(leftPortId, subnetId) == true) {
            subnetPort = this.portStore.get(leftPortId);
        } else if (this.isPortInSubnet(rightPortId, subnetId) == true) {
            subnetPort = this.portStore.get(rightPortId);
        }

        return subnetPort;

    }

    /*
     * Given an edge which connects a subnet to a LSW, find the subnet port
     * on edge.
     */
    public PortMappingInfo findSubnetPortOnEdge(EdgeMappingInfo edge) {
        Uuid leftPortId = edge.getEdge().getLeftPortId();
        if (this.isPortSubnetType(leftPortId) == true) {
            return this.portStore.get(leftPortId);
        }

        Uuid rightPortId = edge.getEdge().getRightPortId();
        if (this.isPortSubnetType(rightPortId) == true) {
            return this.portStore.get(rightPortId);
        }

        return null;
    }

    /*
     * Given an edge which connects a subnet and a LSW, find the LSW port
     * on edge.
     *
     * Warning: this function does not work correctly if the input edge is lsw-to-lsw.
     */
    public PortMappingInfo findLswPortOnEdge(EdgeMappingInfo edge) {
        Uuid leftPortId = edge.getEdge().getLeftPortId();
        if (this.isPortLswType(leftPortId)) {
            return this.portStore.get(leftPortId);
        }

        Uuid rightPortId = edge.getEdge().getRightPortId();
        if (this.isPortLswType(rightPortId)) {
            return this.portStore.get(rightPortId);
        }

        return null;
    }

    public PortMappingInfo findLrPortOnEdge(EdgeMappingInfo edge) {
        Uuid leftPortId = edge.getEdge().getLeftPortId();
        if (this.isPortLrType(leftPortId)) {
            return this.portStore.get(leftPortId);
        }

        Uuid rightPortId = edge.getEdge().getRightPortId();
        if (this.isPortLrType(rightPortId)) {
            return this.portStore.get(rightPortId);
        }

        return null;
    }

    public LogicalEdgeType findEdgeType(EdgeMappingInfo edge) {
        LogicalEdgeType edgeType = LogicalEdgeType.UNKNOWNTYPE;

        Uuid leftPortId = edge.getEdge().getLeftPortId();
        Uuid rightPortId = edge.getEdge().getRightPortId();
        if (this.isPortLrType(leftPortId) && this.isPortLrType(rightPortId)) {
            edgeType = LogicalEdgeType.LR_LR;
        } else if (this.isPortLswType(leftPortId) && this.isPortLswType(rightPortId)) {
            edgeType = LogicalEdgeType.LSW_LSW;
        } else if (this.isPortLswType(leftPortId) && this.isPortLrType(rightPortId)
                || this.isPortLrType(leftPortId) && this.isPortLswType(rightPortId)) {
            edgeType = LogicalEdgeType.LR_LSW;
        } else if (this.isPortLswType(leftPortId) && this.isPortSubnetType(rightPortId)
                || this.isPortSubnetType(leftPortId) && this.isPortLswType(rightPortId)) {
            edgeType = LogicalEdgeType.LSW_SUBNET;
        } else if (this.isPortSubnetType(leftPortId) && this.isPortEpLocationType(rightPortId)
                || this.isPortEpLocationType(leftPortId) && this.isPortSubnetType(rightPortId)) {
            edgeType = LogicalEdgeType.SUBNET_EPLOCATION;
        } else {
            LOG.trace("FABMGR: findEdgeType: unknown type: leftPortId={}, rightPortId={}", leftPortId.getValue(),
                    rightPortId.getValue());
        }

        return edgeType;
    }

    /**
     * Find all rendered switches on a given fabric belongs to the logical router domain.
     * @param lr - the logical router to be searched against.
     * @param fabricId - the fabric identifier.
     * @return list of th rendered switches ids.
     */
    @Nonnull
    public List<NodeId> findLswRenderedDeviceIdFromLr(LogicalRouterMappingInfo lr, NodeId fabricId) {
        List<EdgeMappingInfo> lrLswEdges = this.findLrLswEdge(lr);
        List<NodeId> lsws = new ArrayList<>();

        for (EdgeMappingInfo lrLswEdge:lrLswEdges)
        {
            LogicalSwitchMappingInfo lsw = this.findLswFromLrLswEdge(lrLswEdge);
            if (lsw != null && lsw.getRenderedSwitches().containsKey(fabricId)) {
                lsws.add(lsw.getRenderedSwitchOnFabric(fabricId).getSwitchID());
            }
        }
        return lsws;
    }

    @Nonnull
    public List<NodeId> findAllFabricsOfRenderedLswsFromLr(LogicalRouterMappingInfo lr) {
        List<EdgeMappingInfo> lrLswEdges = this.findLrLswEdge(lr);
        List<NodeId> lsws = new ArrayList<>();

        for (EdgeMappingInfo lrLswEdge:lrLswEdges)
        {
            LogicalSwitchMappingInfo lsw = this.findLswFromLrLswEdge(lrLswEdge);
            if (lsw != null && !lsw.getRenderedSwitches().isEmpty()) {
                lsws.addAll(lsw.getRenderedSwitches().keySet());
            }
        }
        return lsws;
    }


    private LogicalSwitchMappingInfo findLswFromLrLswEdge(EdgeMappingInfo lrLswEdge) {
        PortMappingInfo lswPort = this.findLswPortOnEdge(lrLswEdge);
        if (lswPort == null) {
            return null;
        }
        return this.findLswFromItsPort(lswPort.getPort());
    }

    private LogicalSwitchMappingInfo findLswFromSubnetLswEdge(EdgeMappingInfo subnetLswEdge) {
        PortMappingInfo lswPort = this.findLswPortOnEdge(subnetLswEdge);
        if (lswPort == null) {
            return null;
        }
        return this.findLswFromItsPort(lswPort.getPort());
    }

    /*
     * Given an port on LSW, find the LSW.
     */
    public LogicalSwitchMappingInfo findLswFromItsPort(Port port) {
        Uuid portId = port.getUuid();
        if (!this.isPortLswType(portId)) {
            return null;
        }

        Uuid lswId = port.getLocationId();

        return this.lswStore.get(lswId);
    }

    public LogicalSwitchMappingInfo findLswFromPortId(Uuid portId) {
        PortMappingInfo port = this.findPortFromPortId(portId);
        if (port == null) {
            return null;
        }

        return this.findLswFromItsPort(port.getPort());
    }

    /*
     * Given an port on LR, find the LR.
     */
    public LogicalRouterMappingInfo findLrFromItsPort(Port port) {
        Uuid portId = port.getUuid();
        if (!this.isPortLrType(portId)) {
            return null;
        }

        Uuid lrId = port.getLocationId();

        return this.lrStore.get(lrId);
    }

    public LogicalRouterMappingInfo findLrFromPortId(Uuid portId) {
        PortMappingInfo port = this.findPortFromPortId(portId);
        if (port == null) {
            return null;
        }

        return this.findLrFromItsPort(port.getPort());
    }

    public PortMappingInfo findEpPortFromEpLocation(EndpointLocation epLocation) {
        Uuid epPortId = epLocation.getPort();
        return this.portStore.get(epPortId);
    }

    public boolean isPortInSubnet(Uuid portId, Uuid subnetId) {
        PortMappingInfo port = this.portStore.get(portId);

        if (!this.isPortSubnetType(portId)) {
            return false;
        }

        if (!port.getPort().getLocationId().equals(subnetId)) {
            return false;
        }

        return true;
    }

    public boolean isEdgeLrToLrType(Edge edge) {
        EdgeMappingInfo edgeInfo = this.edgeStore.get(edge.getUuid());
        if (edgeInfo == null) {
            // edge should already be cached when this function is called.
            LOG.error("FABMGR: ERROR: isEdgeLrToLrType: edge not in cache: {}", edge.getUuid().getValue());
            return false;
        }

        if (this.findEdgeType(edgeInfo) == LogicalEdgeType.LR_LR) {
            return true;
        }

        return false;
    }

    public boolean isEdgeLswToLswType(Edge edge) {
        EdgeMappingInfo edgeInfo = this.edgeStore.get(edge.getUuid());
        if (edgeInfo == null) {
            // edge should already be cached when this function is called.
            LOG.error("FABMGR: ERROR: edgeIsLswToLswType: edge not in cache: {}", edge.getUuid().getValue());
            return false;
        }

        if (this.findEdgeType(edgeInfo) == LogicalEdgeType.LSW_LSW) {
            return true;
        }

        return false;
    }

    public boolean isEdgeLrToLswType(Edge edge) {
        EdgeMappingInfo edgeInfo = this.edgeStore.get(edge.getUuid());
        if (edgeInfo == null) {
            // edge should already be cached when this function is called.
            LOG.error("FABMGR: ERROR: isEdgeLrToLswType: edge not in cache: {}", edge.getUuid().getValue());
            return false;
        }

        if (this.findEdgeType(edgeInfo) == LogicalEdgeType.LR_LSW) {
            return true;
        }

        return false;
    }

    public boolean isEdgeLswToSubnetType(Edge edge) {
        EdgeMappingInfo edgeInfo = this.edgeStore.get(edge.getUuid());
        if (edgeInfo == null) {
            // edge should already be cached when this function is called.
            LOG.error("FABMGR: ERROR: edgeIsLswToSubnetType: edge not in cache: {}", edge.getUuid().getValue());
            return false;
        }

        if (this.findEdgeType(edgeInfo) == LogicalEdgeType.LSW_SUBNET) {
            return true;
        }

        return false;
    }

    public boolean isEdgeSubnetToEpLocationType(Edge edge) {
        EdgeMappingInfo edgeInfo = this.edgeStore.get(edge.getUuid());
        if (edgeInfo == null) {
            // edge should already be cached when this function is called.
            LOG.error("FABMGR: ERROR: edgeIsSubnetToEpLocationType: edge not in cache: {}", edge.getUuid().getValue());
            return false;
        }

        if (this.findEdgeType(edgeInfo) == LogicalEdgeType.SUBNET_EPLOCATION) {
            return true;
        }

        return false;
    }

    public boolean isPortSubnetType(Uuid portId) {
        return this.portIsType(portId, LocationType.SubnetType);
    }

    public boolean isPortLswType(Uuid portId) {
        return this.portIsType(portId, LocationType.SwitchType);
    }

    public boolean isPortLrType(Uuid portId) {
        return this.portIsType(portId, LocationType.RouterType);
    }

    public boolean isPortEpLocationType(Uuid portId) {
        return this.portIsType(portId, LocationType.EndpointType);
    }

    public boolean portIsType(Uuid portId, LocationType portType) {
        PortMappingInfo port = this.findPortFromPortId(portId);

        if (port == null) {
            LOG.trace("FABMGR: portIsType: port not found: portId={}", portId.getValue());
            return false;
        }

        LocationType myType = port.getPort().getLocationType();
        if (myType == portType) {
            return true;
        }

        return false;
    }

    public LogicalRouterMappingInfo getLrMappingInfo(LogicalRouter lr) {
        return this.lrStore.get(lr.getUuid());
    }

    public PortMappingInfo getPortMappingInfo(Port port) {
        return this.portStore.get(port.getUuid());
    }

    public Map<Uuid, LogicalSwitchMappingInfo> getLswStore() {
        return lswStore;
    }

    public Map<Uuid, LogicalRouterMappingInfo> getLrStore() {
        return lrStore;
    }

    public Map<Uuid, SecurityRuleGroupsMappingInfo> getSecurityRuleGroupsStore() {
        return securityRuleGroupsStore;
    }

    public Map<Uuid, SubnetMappingInfo> getSubnetStore() {
        return subnetStore;
    }

    public Map<Uuid, PortMappingInfo> getPortStore() {
        return portStore;
    }

    public Map<Uuid, EdgeMappingInfo> getEdgeStore() {
        return edgeStore;
    }

    public Map<Uuid, EndpointLocationMappingInfo> getEpLocationStore() {
        return epLocationStore;
    }

    public void removeLswFromCache(LogicalSwitch lsw) {
        this.lswStore.remove(lsw.getUuid());
    }

    public void removeLrFromCache(LogicalRouter lr) {
        this.lrStore.remove(lr.getUuid());
    }

    public void removeEdgeFromCache(Edge edge) {
        this.edgeStore.remove(edge.getUuid());
    }

    public void removePortFromCache(Port port) {
        this.portStore.remove(port.getUuid());
    }

    public void removeEpLocationFromCache(EndpointLocation epLocation) {
        this.epLocationStore.remove(epLocation.getUuid());
    }

    public void removeSubnetFromCache(Subnet subnet) {
        this.subnetStore.remove(subnet.getUuid());
    }

    public void removeSecurityRuleGroupsFromCache(SecurityRuleGroups ruleGroups) {
        this.securityRuleGroupsStore.remove(ruleGroups.getUuid());
    }

    public String dumpUlnInstance() {
        StringBuilder sb = new StringBuilder();
        sb.append("*********** Logical Switch table *****************\n");
        for (Entry<Uuid, LogicalSwitchMappingInfo> entry : this.lswStore.entrySet()) {
            LogicalSwitchMappingInfo info = entry.getValue();
            sb.append("lswId=" + entry.getKey().getValue());
            for (NodeId fabricId : info.getRenderedSwitches().keySet())
            {
                sb.append(", renderedDevId="
                    + (info.getRenderedSwitchOnFabric(fabricId) == null ? "null" : info.getRenderedSwitchOnFabric(fabricId).getSwitchID().getValue()));
            }
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** Logical Router table *****************\n");
        for (Entry<Uuid, LogicalRouterMappingInfo> entry : this.lrStore.entrySet()) {
            LogicalRouterMappingInfo info = entry.getValue();
            sb.append("lrId=" + entry.getKey().getValue());
            for (NodeId fabricId : info.getRenderedRouters().keySet())
            {
                sb.append(", renderedDevId="
                    + (info.getRenderedDeviceIdOnFabric(fabricId) == null ? "null" : info.getRenderedDeviceIdOnFabric(fabricId).getValue()));
            }
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** SecurityRuleGroups table *****************\n");
        for (Entry<Uuid, SecurityRuleGroupsMappingInfo> entry : securityRuleGroupsStore.entrySet()) {
            SecurityRuleGroupsMappingInfo info = entry.getValue();
            sb.append("ruleId=" + entry.getKey().getValue());
            List<Uuid> ports = info.getSecurityRuleGroups().getPorts();
            sb.append(", portId=" + (ports == null || ports.isEmpty() ? "null" : ports.get(0).getValue()));
            List<String> aclNameList = info.getRenderedAclNameList();
            if (aclNameList != null && aclNameList.isEmpty() == false) {
                for (String aclName : aclNameList) {
                    sb.append(", " + aclName);
                }
            }
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** Subnet table *****************\n");
        for (Entry<Uuid, SubnetMappingInfo> entry : subnetStore.entrySet()) {
            SubnetMappingInfo info = entry.getValue();
            sb.append("subnetId=" + entry.getKey().getValue() + ", " + info.getSubnet().getUuid().getValue());
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** Port table *****************\n");
        for (Entry<Uuid, PortMappingInfo> entry : portStore.entrySet()) {
            PortMappingInfo info = entry.getValue();
            sb.append("portId=" + entry.getKey().getValue() + ", type=" + info.getPort().getLocationType().toString());
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** Edge table *****************\n");
        for (Entry<Uuid, EdgeMappingInfo> entry : edgeStore.entrySet()) {
            EdgeMappingInfo info = entry.getValue();
            sb.append("edgeId=" + entry.getKey().getValue() + ", type=" + this.findEdgeType(info).toString());
            sb.append(", leftPort=" + info.getEdge().getLeftPortId().getValue());
            sb.append(", rightPort=" + info.getEdge().getRightPortId().getValue());
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** EpLocation table *****************\n");
        for (Entry<Uuid, EndpointLocationMappingInfo> entry : epLocationStore.entrySet()) {
            EndpointLocationMappingInfo info = entry.getValue();
            sb.append("epLocationId=" + entry.getKey().getValue() + ", portId="
                    + info.getEpLocation().getPort().getValue());
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        return sb.toString();
    }

    public void addRequestRemoveLsw(LogicalSwitch lsw) {
        Uuid lswId = lsw.getUuid();
        LogicalSwitchMappingInfo info = this.lswStore.get(lswId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeLsw: info is null");
            return;
        }
        info.markDeleted();
    }

    public void addRequestRemoveLr(LogicalRouter lr) {
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeLr: info is null");
            return;
        }
        info.markDeleted();
    }

    public void addRequestRemoveEdge(Edge edge) {
        Uuid edgeId = edge.getUuid();
        EdgeMappingInfo info = this.edgeStore.get(edgeId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeEdge: info is null");
            return;
        }
        info.markDeleted();
    }

    public void addRequestRemovePort(Port port) {
        Uuid portId = port.getUuid();
        PortMappingInfo info = this.portStore.get(portId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removePort: info is null");
            return;
        }
        info.markDeleted();
    }

    public void addRequestRemoveSubnet(Subnet subnet) {
        Uuid subnetId = subnet.getUuid();
        SubnetMappingInfo info = this.subnetStore.get(subnetId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeSubnet: info is null");
            return;
        }
        info.markDeleted();
    }

    public void addRequestRemoveEpLocation(EndpointLocation epLocation) {
        Uuid epLocationId = epLocation.getUuid();
        EndpointLocationMappingInfo info = this.epLocationStore.get(epLocationId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeEpLocation: info is null");
            return;
        }
        info.markDeleted();
    }

    public void addRequestRemoveSecurityRuleGroups(SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        SecurityRuleGroupsMappingInfo info = this.securityRuleGroupsStore.get(ruleGroupsId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroups: info is null");
            return;
        }
        info.markDeleted();
    }

    public void removeSecurityRuleGroupsFromLsw(LogicalSwitch lsw, SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        Uuid lswId = lsw.getUuid();
        LogicalSwitchMappingInfo info = this.lswStore.get(lswId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromLsw: info is null: {}", lswId.getValue());
            return;
        }
        info.removeSecurityRuleGroups(ruleGroupsId);
    }

    public void removeSecurityRuleGroupsFromLr(LogicalRouter lr, SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromLr: info is null: {}", lrId.getValue());
            return;
        }
        info.removeSecurityRuleGroups(ruleGroupsId);
    }

    public void removePortFromLsw(LogicalSwitch lsw, Port port) {
        Uuid portId = port.getUuid();
        Uuid lswId = lsw.getUuid();
        LogicalSwitchMappingInfo info = this.lswStore.get(lswId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removePortFromLsw: info is null: {}", lswId.getValue());
            return;
        }
        info.removePort(portId);
    }

    public void removeLrLswEdgeFromLsw(LogicalSwitch lsw, Edge edge) {
        Uuid edgeId = edge.getUuid();
        Uuid lswId = lsw.getUuid();
        LogicalSwitchMappingInfo info = this.lswStore.get(lswId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeLrLswEdgeFromLsw: info is null: {}", lswId.getValue());
            return;
        }
        info.removeLrLswEdge(edgeId);
    }

    public void removePortFromLr(LogicalRouter lr, Port port) {
        Uuid portId = port.getUuid();
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removePortFromLr: info is null: {}", lrId.getValue());
            return;
        }
        info.removePort(portId);
    }

    public void removeLrLswEdgeFromLr(LogicalRouter lr, Edge edge) {
        Uuid edgeId = edge.getUuid();
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeLrLswEdgeFromLr: info is null: {}", lrId.getValue());
            return;
        }
        info.removeLrLswEdge(edgeId);
    }

    public void removeLrLswEdgeFromPort(Port port, Edge edge) {
        Uuid edgeId = edge.getUuid();
        Uuid portId = port.getUuid();
        PortMappingInfo info = this.portStore.get(portId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: removeLrLswEdgeFromPort: info is null: {}", portId.getValue());
            return;
        }
        info.removeLrLswEdge(edgeId);
    }

    public void addSecurityRuleGroupsToLsw(LogicalSwitch lsw, SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        Uuid lswId = lsw.getUuid();
        LogicalSwitchMappingInfo info = this.lswStore.get(lswId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addSecurityRuleGroupsToLsw: info is null: {}", lswId.getValue());
            return;
        }
        info.addSecurityRuleGroups(ruleGroupsId);
    }

    public void addPortToLsw(LogicalSwitch lsw, Port port) {
        Uuid portId = port.getUuid();
        Uuid lswId = lsw.getUuid();
        LogicalSwitchMappingInfo info = this.lswStore.get(lswId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addPortToLsw: info is null: {}", lswId.getValue());
            return;
        }
        info.addPort(portId);
    }

    public void addLrLswEdgeToLsw(LogicalSwitch lsw, Edge edge) {
        Uuid edgeId = edge.getUuid();
        Uuid lswId = lsw.getUuid();
        LogicalSwitchMappingInfo info = this.lswStore.get(lswId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addLrLswEdgeToLsw: info is null: {}", lswId.getValue());
            return;
        }
        info.addLrLswEdge(edgeId);
    }

    public void addSecurityRuleGroupsToLr(LogicalRouter lr, SecurityRuleGroups ruleGroups) {
        Uuid ruleGroupsId = ruleGroups.getUuid();
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addSecurityRuleGroupsToLr: info is null: {}", lrId.getValue());
            return;
        }
        info.addSecurityRuleGroups(ruleGroupsId);
    }

    public void addPortToLr(LogicalRouter lr, Port port) {
        Uuid portId = port.getUuid();
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addPortToLr: info is null: {}", lrId.getValue());
            return;
        }
        info.addPort(portId);
    }

    public void addLrLswEdgeToLr(LogicalRouter lr, Edge edge) {
        Uuid edgeId = edge.getUuid();
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addLrLswEdgeToLr: info is null: {}", lrId.getValue());
            return;
        }
        info.addLrLswEdge(edgeId);
    }

    public void addLrLswEdgeToPort(Port port, Edge edge) {
        Uuid edgeId = edge.getUuid();
        Uuid portId = port.getUuid();
        PortMappingInfo info = this.portStore.get(portId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addLrLswEdgeToPort: info is null: {}", portId.getValue());
            return;
        }
        info.addLrLswEdge(edgeId);
    }

    public void addGatewayIpToLr(LogicalRouter lr, IpAddress gatewayIp) {
        Uuid lrId = lr.getUuid();
        LogicalRouterMappingInfo info = this.lrStore.get(lrId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: addLrLswEdgeToLr: info is null: {}", lrId.getValue());
            return;
        }
        info.addGatewayIp(gatewayIp);
    }

    public void setLswIdOnEpLocation(EndpointLocation epLocation, Uuid lswId) {
        Uuid epLocationId = epLocation.getUuid();
        EndpointLocationMappingInfo info = this.epLocationStore.get(epLocationId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: setLswIdOnEpLocation: info is null: {}", epLocationId.getValue());
            return;
        }
        info.setLswId(lswId);
    }

    public void setLswPortIdOnEpLocation(EndpointLocation epLocation, Uuid lswPortId) {
        Uuid epLocationId = epLocation.getUuid();
        EndpointLocationMappingInfo info = this.epLocationStore.get(epLocationId);
        if (info == null) {
            LOG.error("FABMGR: ERROR: setLswPortIdOnEpLocation: info is null: {}", epLocationId.getValue());
            return;
        }
        info.setLswPortId(lswPortId);
    }

    public void updateLSWsConnectedToSubnet(Subnet subnet) {
        SubnetMappingInfo subnetInfo = this.findSubnetFromSubnetId(subnet.getUuid());
        if (subnetInfo == null || !subnetInfo.hasServiceBeenRendered()) {
            // If the subnet has not been rendered, then that means no LSW that connects
            // to this subnet has been rendered.
            return;
        }

        List<EdgeMappingInfo> subnetLswEdgeList = this.findAllSubnetLswEdgesFromSubnet(subnetInfo);
        if (subnetLswEdgeList.isEmpty()) {
            return;
        }

        Map<NodeId, RenderedSwitch> renderedLsws = null;
        for (EdgeMappingInfo edge : subnetLswEdgeList) {
            LogicalSwitchMappingInfo lswInfo = this.findLswFromSubnetLswEdge(edge);
            if (lswInfo != null && lswInfo.hasServiceBeenRendered()) {
                renderedLsws = lswInfo.getRenderedSwitches();
                break;
            }
        }

        if (renderedLsws == null) {
            LOG.error(
                    "FABMGR: ERROR: updateLSWsConnectedToSubnet: subnet is marked as rendered, but cannot find rendered LSW");
            return;
        }

        for (EdgeMappingInfo edge : subnetLswEdgeList) {
            LogicalSwitchMappingInfo lswInfo = this.findLswFromSubnetLswEdge(edge);
            if (lswInfo != null && !lswInfo.hasServiceBeenRendered()) {
                for (Map.Entry<NodeId, RenderedSwitch> entry : renderedLsws.entrySet())
                {
                    lswInfo.addRenderedSwitch(entry.getValue());
                }
            }
        }
    }
}
