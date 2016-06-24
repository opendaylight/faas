/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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

public class UserLogicalNetworkCache {

    public enum EdgeType {
        unknownType, LrToLr, LswToLsw, LrToLsw, LswToSubnet, SubnetToEpLocation;
    }

    private static final Logger LOG = LoggerFactory.getLogger(UserLogicalNetworkCache.class);

    private Uuid tenantId;

    private Map<Uuid, LogicalSwitchMappingInfo> lswStore;
    private Map<Uuid, LogicalRouterMappingInfo> lrStore;
    private Map<Uuid, SecurityRuleGroupsMappingInfo> securityRuleGroupsStore;
    private Map<Uuid, SubnetMappingInfo> subnetStore;
    private Map<Uuid, PortMappingInfo> portStore;
    private Map<Uuid, EdgeMappingInfo> edgeStore;
    private Map<Uuid, EndpointLocationMappingInfo> epLocationStore;
    private LogicalRouterMappingInfo renderedlrOnFabric; // workaround for Bug 5146: We cache
                                                         // the LR once it is rendered.

    public UserLogicalNetworkCache(Uuid tenantId) {
        super();
        this.setTenantId(tenantId);
        /*
         * TODO: We are testing Full Sync vs. concurrentMap.
         */
        boolean useSyncMap = false;
        if (useSyncMap) {
            lswStore = Collections.synchronizedMap(new HashMap<Uuid, LogicalSwitchMappingInfo>());
            lrStore = Collections.synchronizedMap(new HashMap<Uuid, LogicalRouterMappingInfo>());
            securityRuleGroupsStore = Collections.synchronizedMap(new HashMap<Uuid, SecurityRuleGroupsMappingInfo>());
            subnetStore = Collections.synchronizedMap(new HashMap<Uuid, SubnetMappingInfo>());
            portStore = Collections.synchronizedMap(new HashMap<Uuid, PortMappingInfo>());
            edgeStore = Collections.synchronizedMap(new HashMap<Uuid, EdgeMappingInfo>());
            epLocationStore = Collections.synchronizedMap(new HashMap<Uuid, EndpointLocationMappingInfo>());
        } else {
            lswStore = new ConcurrentHashMap<Uuid, LogicalSwitchMappingInfo>();
            lrStore = new ConcurrentHashMap<Uuid, LogicalRouterMappingInfo>();
            securityRuleGroupsStore = new ConcurrentHashMap<Uuid, SecurityRuleGroupsMappingInfo>();
            subnetStore = new ConcurrentHashMap<Uuid, SubnetMappingInfo>();
            portStore = new ConcurrentHashMap<Uuid, PortMappingInfo>();
            edgeStore = new ConcurrentHashMap<Uuid, EdgeMappingInfo>();
            epLocationStore = new ConcurrentHashMap<Uuid, EndpointLocationMappingInfo>();
        }
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
        this.lrStore.get(lrId).markAsRendered(renderedLrId);
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
        this.securityRuleGroupsStore.get(ruleGroupsId).setServiceHasBeenRendered(true);
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
        this.subnetStore.get(subnetId).setServiceHasBeenRendered(true);
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
        this.portStore.get(portId).setServiceHasBeenRendered(true);
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
        this.edgeStore.get(edgeId).setServiceHasBeenRendered(true);
    }

    public boolean isEpLocationAlreadyCached(EndpointLocation epLocation) {
        boolean found = false;

        Uuid epLocationId = epLocation.getUuid();
        if (this.epLocationStore.get(epLocationId) != null) {
            found = true;
        }

        return found;

    }

    public void markEpLocationAsRendered(EndpointLocation epLocation,
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid renderedEpId) {
        Uuid epLocationId = epLocation.getUuid();
        this.epLocationStore.get(epLocationId).setRenderedDeviceId(renderedEpId);
        this.epLocationStore.get(epLocationId).setServiceHasBeenRendered(true);
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

    /*
     * Find edge that connects the given EP with its belonging subnet
     */
    public EdgeMappingInfo findEpLocationSubnetEdge(EndpointLocation epLocation) {
        EdgeMappingInfo edge = null;

        Uuid epPortId = epLocation.getPort();
        PortMappingInfo epPort = this.portStore.get(epPortId);
        if (epPort == null) {
            return null;
        }

        Uuid edgeId = epPort.getPort().getEdgeId();
        edge = this.edgeStore.get(edgeId);

        return edge;
    }

    /*
     * Given an edge and one port, find the other port.
     */
    public PortMappingInfo findOtherPortInEdge(EdgeMappingInfo epEdge, Uuid epPortId) {
        Uuid leftPortId = epEdge.getEdge().getLeftPortId();
        Uuid rightPortId = epEdge.getEdge().getRightPortId();

        Uuid otherPortId;
        if (leftPortId.equals(epPortId) == true) {
            otherPortId = rightPortId;
        } else if (rightPortId.equals(epPortId) == true) {
            otherPortId = leftPortId;
        } else {
            LOG.error("FABMGR: ERROR: findOtherPortInEdge: port id is wrong: ep={}, left={}, right={}",
                    epPortId.getValue(), leftPortId.getValue(), rightPortId.getValue());
            return null;
        }

        PortMappingInfo otherPort = this.portStore.get(otherPortId);

        return otherPort;
    }

    public PortMappingInfo findLeftPortOnEdge(Edge edge) {
        Uuid leftPortId = edge.getLeftPortId();
        return this.portStore.get(leftPortId);
    }

    public PortMappingInfo findRightPortOnEdge(Edge edge) {
        Uuid rightPortId = edge.getRightPortId();
        return this.portStore.get(rightPortId);
    }

    /*
     * Given a port, find the subnet to which this port belongs.
     */
    public SubnetMappingInfo findSubnetFromItsPort(PortMappingInfo subnetPort) {
        LocationType portLocationType = subnetPort.getPort().getLocationType();

        if (portLocationType != LocationType.SubnetType) {
            LOG.error("FABMGR: ERROR: wrong port type: {}", portLocationType.name());
            return null;
        }

        Uuid subnetId = subnetPort.getPort().getLocationId();
        SubnetMappingInfo subnet = this.subnetStore.get(subnetId);

        return subnet;
    }

    /*
     * Given a LSW, find its associated subnet. The ULN model allows a LSW
     * to have more than one subnet. However, this function only returns the
     * first one that it finds.
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

    /*
     * Given a subnet, find the edge that connects this subnet with
     * a logical switch. The ULN model allows multiple logical switches
     * to be connected to one subnet. To accommodate this bug (Bug 5144),
     * this function returns all the lswToSubnet edges.
     */
    public List<EdgeMappingInfo> findAllSubnetLswEdgesFromSubnet(SubnetMappingInfo subnet) {
        List<EdgeMappingInfo> subnetLswEdgeList = null;
        Uuid subnetId = subnet.getSubnet().getUuid();
        for (Entry<Uuid, EdgeMappingInfo> entry : this.edgeStore.entrySet()) {
            EdgeMappingInfo edge = entry.getValue();
            if (this.findEdgeType(edge) == EdgeType.LswToSubnet) {
                PortMappingInfo subnetPort = this.findSubnetPortInEdge(edge, subnetId);
                if (subnetPort != null) {
                    PortMappingInfo otherPort = this.findOtherPortInEdge(edge, subnetPort.getPort().getUuid());
                    if (otherPort != null) {
                        LocationType portType = otherPort.getPort().getLocationType();
                        if (portType == LocationType.SwitchType) {
                            if (subnetLswEdgeList == null) {
                                subnetLswEdgeList = new ArrayList<EdgeMappingInfo>();
                            }
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

    /*
     * subnetLswEdgeList may contain multiple edges, because the
     * ULN model allows a subnet to connect to multiple LSWs (Bug 5144).
     * To handle this bug, we allow only one subnet-lsw edge to be
     * rendered. And we do this by always picking up the same edge in the list.
     */
    public EdgeMappingInfo findSingleSubnetLswEdgeFromSubnet(SubnetMappingInfo subnet) {
        List<EdgeMappingInfo> subnetLswEdgeList = this.findAllSubnetLswEdgesFromSubnet(subnet);
        if (subnetLswEdgeList == null || subnetLswEdgeList.isEmpty()) {
            LOG.debug("FABMGR: findSingleSubnetLswEdgeFromSubnet: cannot find subnetLswEdge in cache");
            return null;
        }

        /*
         * No LSW has been rendered for this subnet yet, so just pick
         * up the first edge in the list.
         */
        if (subnet.hasServiceBeenRendered() == false) {
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
            if (lsw != null && lsw.hasServiceBeenRendered() == true) {
                subnetLswEdge = edge;
                renderedLswCounter++;
            }
        }

        // Just checking
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
            if (this.findEdgeType(edge) == EdgeType.LswToSubnet) {
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

    public EdgeMappingInfo findLrLswEdge(LogicalRouterMappingInfo lr) {
        EdgeMappingInfo lrLswEdge = null;

        Uuid lrId = lr.getLr().getUuid();
        for (Entry<Uuid, EdgeMappingInfo> entry : this.edgeStore.entrySet()) {
            EdgeMappingInfo edge = entry.getValue();
            if (this.findEdgeType(edge) == EdgeType.LrToLsw) {
                PortMappingInfo lrPort = this.findLrPortOnEdge(edge);
                if (lrPort != null && lrPort.getPort().getLocationId().equals(lrId)) {
                    PortMappingInfo otherPort = this.findOtherPortInEdge(edge, lrPort.getPort().getUuid());
                    if (otherPort != null && otherPort.getPort().getLocationType() == LocationType.SwitchType) {
                        lrLswEdge = edge;
                    } else {
                        LOG.error("FABMGR: ERROR: findLrLswEdge: otherPort is not LSW type: {}",
                                otherPort.getPort().getLocationType().toString());
                    }
                    break;
                }
            }
        }

        return lrLswEdge;
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
        if (this.isPortLswType(leftPortId) == true) {
            return this.portStore.get(leftPortId);
        }

        Uuid rightPortId = edge.getEdge().getRightPortId();
        if (this.isPortLswType(rightPortId) == true) {
            return this.portStore.get(rightPortId);
        }

        return null;
    }

    public PortMappingInfo findLrPortOnEdge(EdgeMappingInfo edge) {
        Uuid leftPortId = edge.getEdge().getLeftPortId();
        if (this.isPortLrType(leftPortId) == true) {
            return this.portStore.get(leftPortId);
        }

        Uuid rightPortId = edge.getEdge().getRightPortId();
        if (this.isPortLrType(rightPortId) == true) {
            return this.portStore.get(rightPortId);
        }

        return null;
    }

    public EdgeType findEdgeType(EdgeMappingInfo edge) {
        EdgeType edgeType = EdgeType.unknownType;

        Uuid leftPortId = edge.getEdge().getLeftPortId();
        Uuid rightPortId = edge.getEdge().getRightPortId();
        if (this.isPortLrType(leftPortId) && this.isPortLrType(rightPortId)) {
            edgeType = EdgeType.LrToLr;
        } else if (this.isPortLswType(leftPortId) && this.isPortLswType(rightPortId)) {
            edgeType = EdgeType.LswToLsw;
        } else if ((this.isPortLswType(leftPortId) && this.isPortLrType(rightPortId))
                || (this.isPortLrType(leftPortId) && this.isPortLswType(rightPortId))) {
            edgeType = EdgeType.LrToLsw;
        } else if ((this.isPortLswType(leftPortId) && this.isPortSubnetType(rightPortId))
                || (this.isPortSubnetType(leftPortId) && this.isPortLswType(rightPortId))) {
            edgeType = EdgeType.LswToSubnet;
        } else if ((this.isPortSubnetType(leftPortId) && this.isPortEpLocationType(rightPortId))
                || (this.isPortEpLocationType(leftPortId) && this.isPortSubnetType(rightPortId))) {
            edgeType = EdgeType.SubnetToEpLocation;
        } else {
            LOG.trace("FABMGR: findEdgeType: unknown type: leftPortId={}, rightPortId={}", leftPortId.getValue(),
                    rightPortId.getValue());
        }

        return edgeType;
    }

    public NodeId findLswRenderedDeviceIdFromLr(LogicalRouterMappingInfo lr) {
        EdgeMappingInfo lrLswEdge = this.findLrLswEdge(lr);
        if (lrLswEdge == null) {
            return null;
        }

        LogicalSwitchMappingInfo lsw = this.findLswFromLrLswEdge(lrLswEdge);
        if (lsw == null) {
            return null;
        }

        /*
         * Solution to Bug 5144.
         * We do not check if LSW has been rendered or not. Due to
         * Bug 5144, we cannot map a ULN lsw directly to Fabric. We can
         * only render one LSW per subnet. If a subnet has more than one LSW
         * connecting to it, we set the rendered deviceID of the rendered LSW
         * to all other unrendered LSWs. i.e., To the caller of this function,
         * unrendered LSWs behaves as if they were rendered.
         */
        return lsw.getRenderedDeviceId();
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
        if (this.isPortLswType(portId) == false) {
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
        if (this.isPortLrType(portId) == false) {
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

        if (this.isPortSubnetType(portId) == false) {
            return false;
        }

        if (port.getPort().getLocationId().equals(subnetId) == false) {
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

        if (this.findEdgeType(edgeInfo) == EdgeType.LrToLr) {
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

        if (this.findEdgeType(edgeInfo) == EdgeType.LswToLsw) {
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

        if (this.findEdgeType(edgeInfo) == EdgeType.LrToLsw) {
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

        if (this.findEdgeType(edgeInfo) == EdgeType.LswToSubnet) {
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

        if (this.findEdgeType(edgeInfo) == EdgeType.SubnetToEpLocation) {
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
            sb.append(", renderedDevId="
                    + ((info.getRenderedDeviceId() == null) ? "null" : info.getRenderedDeviceId().getValue()));
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** Logical Router table *****************\n");
        for (Entry<Uuid, LogicalRouterMappingInfo> entry : this.lrStore.entrySet()) {
            LogicalRouterMappingInfo info = entry.getValue();
            sb.append("lrId=" + entry.getKey().getValue());
            sb.append(", renderedDevId="
                    + ((info.getRenderedDeviceId() == null) ? "null" : info.getRenderedDeviceId().getValue()));
            sb.append(", isRendered=" + info.hasServiceBeenRendered() + "\n");
        }
        if (this.renderedlrOnFabric != null) {
            sb.append("*LrId=" + this.renderedlrOnFabric.getLr().getUuid().getValue());
            sb.append(", renderedDevId="
                    + ((this.renderedlrOnFabric.getRenderedDeviceId() == null) ? "null" : this.renderedlrOnFabric
                        .getRenderedDeviceId().getValue()));
            sb.append(", isRendered=" + this.renderedlrOnFabric.hasServiceBeenRendered() + "\n");
        }

        sb.append("*********** SecurityRuleGroups table *****************\n");
        for (Entry<Uuid, SecurityRuleGroupsMappingInfo> entry : securityRuleGroupsStore.entrySet()) {
            SecurityRuleGroupsMappingInfo info = entry.getValue();
            sb.append("ruleId=" + entry.getKey().getValue());
            List<Uuid> ports = info.getSecurityRuleGroups().getPorts();
            sb.append(", portId=" + ((ports == null || ports.isEmpty()) ? "null" : ports.get(0).getValue()));
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

    public boolean hasLrBeenRenderedOnFabric() {
        if (this.renderedlrOnFabric == null) {
            return false;
        } else {
            return true;
        }
    }

    public LogicalRouterMappingInfo getRenderedlrOnFabric() {
        return renderedlrOnFabric;
    }

    synchronized public void setRenderedlrOnFabric(LogicalRouterMappingInfo renderedlrOnFabric) {
        this.renderedlrOnFabric = renderedlrOnFabric;
    }

    public NodeId getRenderedLrDeviceId() {
        if (this.renderedlrOnFabric == null) {
            return null;
        } else {
            return this.renderedlrOnFabric.getRenderedDeviceId();
        }
    }

    public void updateLSWsConnectedToSubnet(Subnet subnet) {
        SubnetMappingInfo subnetInfo = this.findSubnetFromSubnetId(subnet.getUuid());
        if (subnetInfo == null || subnetInfo.hasServiceBeenRendered() == false) {
            // If subnet has not been rendered, then that means no LSW that connects
            // to this subnet has been redndered.
            return;
        }

        List<EdgeMappingInfo> subnetLswEdgeList = this.findAllSubnetLswEdgesFromSubnet(subnetInfo);
        if (subnetLswEdgeList == null || subnetLswEdgeList.isEmpty()) {
            return;
        }

        NodeId renderedLswDevId = null;
        for (EdgeMappingInfo edge : subnetLswEdgeList) {
            LogicalSwitchMappingInfo lswInfo = this.findLswFromSubnetLswEdge(edge);
            if (lswInfo != null && lswInfo.hasServiceBeenRendered() == true) {
                renderedLswDevId = lswInfo.getRenderedDeviceId();
                break;
            }
        }

        if (renderedLswDevId == null) {
            LOG.error(
                    "FABMGR: ERROR: updateLSWsConnectedToSubnet: subnet is marked as rendered, but cannot find rendered LSW");
            return;
        }

        for (EdgeMappingInfo edge : subnetLswEdgeList) {
            LogicalSwitchMappingInfo lswInfo = this.findLswFromSubnetLswEdge(edge);
            if (lswInfo != null && lswInfo.hasServiceBeenRendered() == false) {
                lswInfo.setRenderedDeviceId(renderedLswDevId);
            }
        }
    }
}
