/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class LogicalRouterMappingInfo {

    private LogicalRouter lr;
    private NodeId renderedDeviceId;
    private boolean serviceHasBeenRendered;
    private boolean isToBeDeleted;
    private Set<Uuid> securityRuleGroupsList;
    private Set<Uuid> portList;
    private Set<Uuid> lrLswEdgeList;
    private IpAddress gatewayIpAddr;

    public LogicalRouterMappingInfo(LogicalRouter lr) {
        super();
        this.lr = lr;
        this.serviceHasBeenRendered = false;
        this.isToBeDeleted = false;
        this.securityRuleGroupsList = new HashSet<Uuid>();
        this.portList = new HashSet<Uuid>();
        this.lrLswEdgeList = new HashSet<Uuid>();
    }

    public void markAsRendered(NodeId renderedLswId) {
        this.renderedDeviceId = renderedLswId;
        this.serviceHasBeenRendered = true;

    }

    public LogicalRouter getLr() {
        return lr;
    }

    public void setLr(LogicalRouter lr) {
        this.lr = lr;
    }

    public NodeId getRenderedDeviceId() {
        return renderedDeviceId;
    }

    public void setRenderedDeviceId(NodeId renderedLswId) {
        this.renderedDeviceId = renderedLswId;
    }

    public boolean hasServiceBeenRendered() {
        return serviceHasBeenRendered;
    }

    public void setServiceHasBeenRendered(boolean serviceHasBeenRendered) {
        this.serviceHasBeenRendered = serviceHasBeenRendered;
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

    public void addGatewayIp(IpAddress gatewayIp) {
        this.gatewayIpAddr = gatewayIp;
    }

    public IpAddress getGatewayIpAddr() {
        return gatewayIpAddr;
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
