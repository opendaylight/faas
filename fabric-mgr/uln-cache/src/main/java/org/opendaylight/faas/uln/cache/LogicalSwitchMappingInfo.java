/*
 * Copyright (c) 2015, 2016 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public final class LogicalSwitchMappingInfo {

    private final LogicalSwitch lsw;
    private final Map<NodeId, RenderedSwitch> renderedSwitches;
    private final Map<RenderedLinkKey<RenderedSwitch>, RenderedLayer2Link> renderedL2Links;
    private boolean isToBeDeleted;
    private final Set<Uuid> securityRuleGroupsList;
    private final Set<Uuid> portList;
    private final Set<Uuid> lrLswEdgeList;

    public LogicalSwitchMappingInfo(LogicalSwitch lsw) {
        super();
        this.lsw = lsw;
        this.isToBeDeleted = false;
        this.securityRuleGroupsList = new HashSet<>();
        this.portList = new HashSet<>();
        this.lrLswEdgeList = new HashSet<>();
        this.renderedSwitches = new HashMap<>();
        this.renderedL2Links = new HashMap<>();
    }

    public void addRenderedSwitch(RenderedSwitch renderedSW) {
        this.renderedSwitches.put(renderedSW.getFabricId(),renderedSW);
    }

    public void addRenderedLink(RenderedLinkKey key, RenderedLayer2Link renderedLink) {
        this.renderedL2Links.put(key,renderedLink);
    }


    public Map<RenderedLinkKey<RenderedSwitch>, RenderedLayer2Link> getRenderedL2Links() {
        return renderedL2Links;
    }

    public Map<NodeId, RenderedSwitch> getRenderedSwitches() {
        return this.renderedSwitches;
    }


    public LogicalSwitch getLsw() {
        return lsw;
    }

    public RenderedSwitch getRenderedSwitchOnFabric(NodeId fabricID) {
        return renderedSwitches.get(fabricID);
    }

    public boolean hasServiceBeenRendered() {
        return !renderedSwitches.isEmpty();
    }


    public boolean hasServiceBeenRenderedOnFabric(NodeId fabricID) {
        return renderedSwitches.containsKey(fabricID);
    }


    public boolean isToBeDeleted() {
        return this.isToBeDeleted;
    }

    public void markDeleted() {
        this.isToBeDeleted = true;
    }

    public void addSecurityRuleGroups(Uuid ruleGroupsId) {
        this.securityRuleGroupsList.add(ruleGroupsId);
    }

    public void removeSecurityRuleGroups(Uuid ruleGroupsId) {
        this.securityRuleGroupsList.remove(ruleGroupsId);
    }

    public void addPort(Uuid portId) {
        this.portList.add(portId);
    }

    public void removePort(Uuid portId) {
        this.portList.remove(portId);
    }

    public void addLrLswEdge(Uuid lrLswEdgeId) {
        this.lrLswEdgeList.add(lrLswEdgeId);
    }

    public void removeLrLswEdge(Uuid lrLswEdgeId) {
        this.lrLswEdgeList.remove(lrLswEdgeId);
    }

    public int getSecurityRuleGroupsListSize() {
        return this.securityRuleGroupsList.size();
    }

    public int getPortListSize() {
        return this.portList.size();
    }

    public int getLrLswEdgeListSize() {
        return this.lrLswEdgeList.size();
    }
}
