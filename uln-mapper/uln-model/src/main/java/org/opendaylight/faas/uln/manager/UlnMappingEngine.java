/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.fabricmgr.api.VcontainerServiceProviderAPI;
import org.opendaylight.faas.uln.datastore.api.UlnDatastoreApi;
import org.opendaylight.faas.uln.datastore.api.UlnIidFactory;
import org.opendaylight.faas.uln.listeners.UlnUtil;
import org.opendaylight.sfc.provider.api.SfcDataStoreAPI;
import org.opendaylight.sfc.provider.api.SfcProviderRenderedPathAPI;
import org.opendaylight.sfc.provider.api.SfcProviderServicePathAPI;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.RspName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SfcName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.CreateRenderedPathInput;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.CreateRenderedPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.path.first.hop.info.RenderedServicePathFirstHop;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.paths.RenderedServicePath;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.paths.rendered.service.path.RenderedServicePathHop;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.ServiceFunctionPaths;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.AccessLists;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.AclBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.AclKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.AccessListEntries;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.AccessListEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.Ace;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.AceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.MatchesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.actions.packet.handling.DenyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.actions.packet.handling.PermitBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.AceIpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.packet.fields.rev150611.acl.transport.header.fields.DestinationPortRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.packet.fields.rev150611.acl.transport.header.fields.SourcePortRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.RedirectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.tunnel.tunnel.type.NshBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.parameter.values.grouping.ParameterValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.SecurityRuleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.SecurityRule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleClassifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleClassifier.Direction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class UlnMappingEngine {

    private static final Logger LOG = LoggerFactory.getLogger(UlnMappingEngine.class);
    private static final String PV_SFC_TYPE_NAME = "sfc_chain_name";
    private static final String PV_PERMIT_TYPE_NAME = "action-definition-id";
    private static final String PV_ACTION_VALUE_ALLOW = "Action-Allow";
    private static final String PV_ACTION_VALUE_DENY = "Action-Deny";
    private static final String PV_PROTO_TYPE_NAME = "proto";
    private static final String PV_DESTPORT_TYPE_NAME = "destport";
    private static final String PV_SOURCEPORT_TYPE_NAME = "sourceport";

    private Map<Uuid, UserLogicalNetworkCache> ulnStore;

    public UlnMappingEngine() {
        this.setUlnStore(new HashMap<Uuid, UserLogicalNetworkCache>());
    }

    public void createUlnCacheIfNotExist(Uuid tenantId) {
        if (this.ulnStore.get(tenantId) == null) {
            this.ulnStore.put(tenantId, new UserLogicalNetworkCache(tenantId));
        }
    }

    public void handleLswCreateEvent(LogicalSwitch lsw) {
        Uuid tenantId = lsw.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLswCreateEvent: uln is null");
            return;
        }
        if (uln.isLswAlreadyCached(lsw) == true) {
            LOG.error("FABMGR: ERROR: handleLswCreateEvent: lsw already exist");
            return;
        }

        uln.cacheLsw(lsw);
        this.doLogicalSwitchCreate(tenantId, uln, lsw);

        /*
         * Check to see if we can render more elements
         */
        this.checkAndRenderPendingUlnElements(tenantId, uln);
    }

    private void doLogicalSwitchCreate(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        /*
         * For LSW, we can directly render it on Fabric.
         */
        this.renderLogicalSwitch(tenantId, uln, lsw);
    }

    public void handleLrCreateEvent(LogicalRouter lr) {
        Uuid tenantId = lr.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLrCreateEvent: uln is null");
            return;
        }
        if (uln.isLrAlreadyCached(lr) == true) {
            LOG.error("FABMGR: ERROR: handleLrCreateEvent: lr already exist");
            return;
        }

        uln.cacheLr(lr);
        this.doLogicalRouterCreate(tenantId, uln, lr);

        this.checkAndRenderPendingUlnElements(tenantId, uln);
    }

    private void doLogicalRouterCreate(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouter lr) {
        /*
         * For LR, we can directly render it on Fabric.
         */
        this.renderLogicalRouter(tenantId, uln, lr);
    }

    public void handleSubnetCreateEvent(Subnet subnet) {
        Uuid tenantId = subnet.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSubnetCreateEvent: uln is null");
            return;
        }
        if (uln.isSubnetAlreadyCached(subnet) == true) {
            LOG.error("FABMGR: ERROR: handleSubnetCreateEvent: subnet already exist");
            return;
        }

        uln.cacheSubnet(subnet);
        this.doSubnetCreate(tenantId, uln, subnet);

        this.checkAndRenderPendingUlnElements(tenantId, uln);
    }

    private void doSubnetCreate(Uuid tenantId, UserLogicalNetworkCache uln, Subnet subnet) {
        /*
         * For subnet, we do not need to render it.
         */
    }

    public void handlePortCreateEvent(Port port) {
        Uuid tenantId = port.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handlePortCreateEvent: uln is null");
            return;
        }
        if (uln.isPortAlreadyCached(port) == true) {
            LOG.error("FABMGR: ERROR: handlePortCreateEvent: port already exist");
            return;
        }

        uln.cachePort(port);
        this.doPortCreate(tenantId, uln, port);

        /*
         * Now check to see if we can render any other elements.
         */
        this.checkAndRenderPendingUlnElements(tenantId, uln);
    }

    /*
     * If this port belongs to a logical switch and the lsw
     * is already received (note that the lsw should also have
     * been rendered, because lsw is rendered upon reception),
     * then we call renderPortOnLsw()
     */
    private void doPortCreate(Uuid tenantId, UserLogicalNetworkCache uln, Port port) {
        port.getLocationType();

        if (uln.isPortLswType(port.getUuid()) == false) {
            // Only ports on LSW need to be rendered.
            return;
        }

        LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(port);
        if (lsw == null) {
            LOG.info("FABMGR: doPortCreate: lsw not in cache");
            return;
        }

        if (lsw.hasServiceBeenRendered() == false) {
            LOG.error("FABMGR: doPortCreate: lsw not rendered");
            return;
        }

        PortMappingInfo lswPortInfo = uln.getPortMappingInfo(port);
        if (lswPortInfo == null) {
            LOG.error("FABMGR: ERROR: doPortCreate: port not found in cache");
            return;
        }
        this.renderPortOnLsw(tenantId, uln, lsw, lswPortInfo);
    }

    public void handleEndpointLocationCreateEvent(EndpointLocation epLocation) {
        Uuid tenantId = epLocation.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEndpointCreateEvent: uln is null");
            return;
        }
        if (uln.isEpLocationAlreadyCached(epLocation) == true) {
            LOG.error("FABMGR: ERROR: handleEndpointLocationCreateEvent: epLocation already exist");
            return;
        }

        uln.cacheEpLocation(epLocation);
        this.doEndpointLocationCreate(tenantId, uln, epLocation);

        /*
         * Now check to see if we can render any other elements.
         */
        this.checkAndRenderPendingUlnElements(tenantId, uln);
    }

    private void doEndpointLocationCreate(Uuid tenantId, UserLogicalNetworkCache uln, EndpointLocation epLocation) {
        /*
         * When an endpoint is online, we call Fabric's registerEndpoint(). However, before
         * we do that, we need to make sure that LogicalSwitch and Logical Port are created
         * (rendered) on Fabric. Not only that, we must also have to have the Subnet information
         * ready, because Fabric's registerEndpoint() need the information in Subnet.
         */

        PortMappingInfo epPort = uln.findEpPortFromEpLocation(epLocation);
        if (epPort == null) {
            LOG.debug("FABMGR: renderEpRegistration: epPort not in cache");
            return;
        }

        EdgeMappingInfo epEdge = uln.findEpLocationSubnetEdge(epLocation);
        if (epEdge == null) {
            LOG.debug("FABMGR: renderEpRegistration: epEdge not in cache");
            return;
        }

        Uuid epPortId = epPort.getPort().getUuid();
        PortMappingInfo subnetPort = uln.findOtherPortInEdge(epEdge, epPortId);
        if (subnetPort == null) {
            LOG.debug("FABMGR: renderEpRegistration: subnetPort not in cache");
            return;
        }

        SubnetMappingInfo subnet = uln.findSubnetFromItsPort(subnetPort);
        if (subnet == null) {
            LOG.debug("FABMGR: renderEpRegistration: subnet not in cache");
            return;
        }

        EdgeMappingInfo subnetLswEdge = uln.findSubnetLswEdge(subnet);
        if (subnetLswEdge == null) {
            LOG.debug("FABMGR: renderEpRegistration: subnetLswEdge not in cache");
            return;
        }

        PortMappingInfo subnetPort2 = uln.findSubnetPortOnEdge(subnetLswEdge);
        if (subnetPort2 == null) {
            LOG.debug("FABMGR: renderEpRegistration: subnetLswPort not in cache");
            return;
        }

        PortMappingInfo lswPort = uln.findOtherPortInEdge(subnetLswEdge, subnetPort2.getPort().getUuid());
        if (lswPort == null) {
            LOG.debug("FABMGR: renderEpRegistration: lswPort not in cache");
            return;
        }

        LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(lswPort.getPort());
        if (lsw == null) {
            LOG.debug("FABMGR: renderEpRegistration: lsw not in cache");
            return;
        }

        /*
         * If we get here, then we get have received all the
         * information that we need in order to do
         * EP registration. The steps are:
         * 1. create LSW
         * 2. create logical port on LSW
         * 3. register EP
         */

        if (lsw.hasServiceBeenRendered() == false) {
            this.renderLogicalSwitch(tenantId, uln, lsw.getLsw());
        }

        if (lswPort.hasServiceBeenRendered() == false) {
            this.renderPortOnLsw(tenantId, uln, lsw, lswPort);
        }

        EndpointAttachInfo endpoint = UlnUtil.createEpAttachmentInput(epLocation, subnet.getSubnet(), epPort.getPort());

        this.renderEpRegistration(tenantId, uln, epLocation, lsw.getRenderedDeviceId(), lswPort.getRenderedDeviceId(),
                endpoint);
        uln.markEdgeAsRendered(epEdge.getEdge());
        uln.markPortAsRendered(subnetPort.getPort());
        uln.markPortAsRendered(subnetPort2.getPort());
        uln.markEdgeAsRendered(subnetLswEdge.getEdge());
    }

    public void handleEdgeCreateEvent(Edge edge) {
        Uuid tenantId = edge.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEdgeCreateEvent: uln is null");
            return;
        }
        if (uln.isEdgeAlreadyCached(edge) == true) {
            LOG.error("FABMGR: ERROR: handleEdgeCreateEvent: edge already exist");
            return;
        }

        uln.cacheEdge(edge);
        this.doEdgeCreate(tenantId, uln, edge);

        /*
         * Now check to see if we can render any other elements.
         */
        this.checkAndRenderPendingUlnElements(tenantId, uln);
    }

    private void doEdgeCreate(Uuid tenantId, UserLogicalNetworkCache uln, Edge edge) {
        /*
         * An edge has the following type:
         * LR to LR
         * LSW to LSW
         * LSW to LR
         * LSW to Subnet
         * Subnet to EndpointLocation
         *
         * On a single fabric, only LR-to-LSW type needs to be mapped to
         * Fabric's createGateway() API. (Other types are applicable only to
         * multi-fabric.)
         */
        if (uln.isEdgeLrToLrType(edge) || uln.isEdgeLswToLswType(edge)) {
            uln.markEdgeAsRendered(edge);
        }

        boolean canRenderEdge = false;
        LogicalSwitchMappingInfo lsw = null;
        LogicalRouterMappingInfo lr = null;
        SubnetMappingInfo subnet = null;

        if (uln.isEdgeLrToLswType(edge) == true) {
            PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
            PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);
            if (leftPort != null && rightPort != null) {
                if (uln.isPortLswType(leftPort.getPort().getUuid()) == true) {
                    lsw = uln.findLswFromItsPort(leftPort.getPort());
                    if (lsw != null && uln.isPortLrType(rightPort.getPort().getUuid()) == true) {
                        subnet = uln.findSubnetFromLsw(lsw);
                        lr = uln.findLrFromItsPort(rightPort.getPort());
                        if (lr != null && subnet != null) {
                            canRenderEdge = true;
                        }
                    }
                } else if (uln.isPortLswType(rightPort.getPort().getUuid()) == true) {
                    lsw = uln.findLswFromItsPort(rightPort.getPort());
                    if (lsw != null && uln.isPortLrType(leftPort.getPort().getUuid()) == true) {
                        subnet = uln.findSubnetFromLsw(lsw);
                        lr = uln.findLrFromItsPort(leftPort.getPort());
                        if (lr != null && subnet != null) {
                            canRenderEdge = true;
                        }
                    }
                }
            }
        }

        if (canRenderEdge == true) {
            if (lsw.hasServiceBeenRendered() == false) {
                this.renderLogicalSwitch(tenantId, uln, lsw.getLsw());
            }
            if (lr.hasServiceBeenRendered() == false) {
                this.renderLogicalRouter(tenantId, uln, lr.getLr());
            }

            this.renderLrLswEdge(tenantId, uln, lr, lsw, subnet, edge);
        }
    }

    public void handleSecurityRuleGroupsCreateEvent(SecurityRuleGroups ruleGroups) {
        Uuid tenantId = ruleGroups.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSecurityRuleGroupsCreateEvent: uln is null");
            return;
        }
        if (uln.isSecurityRuleGroupsAlreadyCached(ruleGroups) == true) {
            LOG.error("FABMGR: ERROR: handleSecurityRuleGroupsCreateEvent: ruleGroups already exist");
            return;
        }

        uln.cacheSecurityRuleGroups(ruleGroups);
        this.doSecurityRuleGroupsCreate(tenantId, uln, ruleGroups);

        /*
         * Now check to see if we can render any other elements.
         */
        this.checkAndRenderPendingUlnElements(tenantId, uln);
    }

    /*
     * SecurityRuleGroups applies to LR or LSW. We need to find
     * the port to which the rules apply. Then find the LSW or LR
     * to which the port belongs.
     */
    private void doSecurityRuleGroupsCreate(Uuid tenantId, UserLogicalNetworkCache uln, SecurityRuleGroups ruleGroups) {
        /*
         * We check to see if the LSW or LR is rendered. If so, then we
         * can go ahead and render the rules. The model supports the rules
         * to be be applied on multiple ports, but in practice, GBP only use
         * one port. So, we can just use port 0 to fin the LSW (or LR).
         */
        boolean readyToRender = false;
        NodeId nodeId = null;
        Uuid portId = ruleGroups.getPorts().get(0);
        LogicalSwitchMappingInfo lsw = uln.findLswFromPortId(portId);
        if (lsw != null) {
            if (lsw.hasServiceBeenRendered() == false) {
                LOG.error("FABMGR: ERROR: doSecurityRuleGroupsCreate: lsw not rendered: {}",
                        lsw.getLsw().getUuid().getValue());
                this.renderLogicalSwitch(tenantId, uln, lsw.getLsw());
            }
            nodeId = lsw.getRenderedDeviceId();
            readyToRender = true;
        } else {
            LogicalRouterMappingInfo lr = uln.findLrFromPortId(portId);
            if (lr != null) {
                if (lr.hasServiceBeenRendered() == false) {
                    LOG.error("FABMGR: ERROR: doSecurityRuleGroupsCreate: lr not rendered: {}",
                            lr.getLr().getUuid().getValue());
                    this.renderLogicalRouter(tenantId, uln, lr.getLr());
                }
                nodeId = lr.getRenderedDeviceId();
                readyToRender = true;
            }
        }

        if (readyToRender == true) {
            this.renderSecurityRuleGroups(tenantId, uln, nodeId, ruleGroups);
        }
    }

    private void renderLogicalSwitch(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        CreateLneLayer2Input input = UlnUtil.createLneLayer2Input(lsw);
        NodeId renderedLswId = VcontainerServiceProviderAPI.createLneLayer2(UlnUtil.convertToYangUuid(tenantId), input);
        uln.markLswAsRendered(lsw, renderedLswId);
    }

    private void renderLogicalRouter(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouter lr) {
        CreateLneLayer3Input input = UlnUtil.createLneLayer3Input(lr);
        NodeId renderedLrId = VcontainerServiceProviderAPI.createLneLayer3(UlnUtil.convertToYangUuid(tenantId), input);
        uln.markLrAsRendered(lr, renderedLrId);
    }

    private void renderEpRegistration(Uuid tenantId, UserLogicalNetworkCache uln, EndpointLocation epLocation,
            NodeId renderedLswId, TpId renderedLswPortId, EndpointAttachInfo endpoint) {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid renderedEpId =
                VcontainerServiceProviderAPI.attachEpToLneLayer2(UlnUtil.convertToYangUuid(tenantId), renderedLswId,
                        renderedLswPortId, endpoint);
        uln.markEpLocationAsRendered(epLocation, renderedEpId);
    }

    @SuppressWarnings("unused")
    private void renderSubnet(Uuid tenantId, UserLogicalNetworkCache uln, Subnet subnet) {
        // subnet does not map to Fabric.
    }

    private void renderPortOnLsw(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitchMappingInfo lswInfo,
            PortMappingInfo lswPortInfo) {
        if (lswInfo.hasServiceBeenRendered() == false) {
            LOG.error("FABMGR: ERROR: renderPortOnLsw: lsw not rendered yet");
            return;
        }

        NodeId renderedLswId = lswInfo.getRenderedDeviceId();
        TpId renderedPortId = VcontainerServiceProviderAPI
            .createLogicalPortOnLneLayer2(UlnUtil.convertToYangUuid(tenantId), renderedLswId);
        lswPortInfo.markAsRendered(renderedPortId);
    }

    private void renderLrLswEdge(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouterMappingInfo lr,
            LogicalSwitchMappingInfo lsw, SubnetMappingInfo subnet, Edge edge) {
        VcontainerServiceProviderAPI.createLrLswGateway(UlnUtil.convertToYangUuid(tenantId), lr.getRenderedDeviceId(),
                lsw.getRenderedDeviceId(), subnet.getSubnet().getExternalGateways().get(0).getExternalGateway(),
                subnet.getSubnet().getIpPrefix());
        uln.markEdgeAsRendered(edge);
    }

    private void renderSecurityRuleGroups(Uuid tenantId, UserLogicalNetworkCache uln, NodeId nodeId,
            SecurityRuleGroups ruleGroups) {
        /*
         * One SecurityRuleGroups contains a list SecurityRuleGroup.
         * One SecurityRuleGroup contains a list of SecurityRule.
         * One SecurityRule can be mapped to one ietf-acl.
         */
        SecurityRuleGroupsMappingInfo ruleGroupsMappingInfo =
                uln.getSecurityRuleGroupsStore().get(ruleGroups.getUuid());
        if (ruleGroupsMappingInfo == null) {
            LOG.error("FABMGR: ERROR: renderSecurityRuleGroups: ruleGroupsMappingInfo is null");
            return;
        }
        List<SecurityRuleGroup> ruleGroupList = ruleGroups.getSecurityRuleGroup();
        for (SecurityRuleGroup ruleGroup : ruleGroupList) {
            List<SecurityRule> ruleList = ruleGroup.getSecurityRule();
            for (SecurityRule rule : ruleList) {
                this.renderSecurityRule(tenantId, uln, nodeId, ruleGroupsMappingInfo, rule);
            }
        }

        uln.markSecurityRuleGroupsAsRendered(ruleGroups);
    }

    public ServiceFunctionPath getSfcPath(SfcName chainName) {
        ServiceFunctionPaths paths = SfcProviderServicePathAPI.readAllServiceFunctionPaths();
        for (ServiceFunctionPath path : paths.getServiceFunctionPath()) {
            if (path.getServiceChainName().equals(chainName)) {
                return path;
            }
        }
        return null;
    }

    private SfcChainHeader retrieveSfcChain(String sfcChainName, Direction direction) {
        /*
         * NOTE: some code in this function is copied from the groupbasedpolicy
         * project.
         */

        /*
         * If path is symmetrical then there are two RSPs.
         * if srcEp is in consumer EPG use "rspName"
         * else srcEp is in provider EPG, "rspName-Reverse".
         */
        ServiceFunctionPath sfcPath = getSfcPath(new SfcName(sfcChainName));
        if (sfcPath == null || sfcPath.getName() == null) {
            LOG.error("ULN: ERROR: addSfcChain: SFC Path was invalid. Either null or name was null.", sfcPath);
            return null;
        }
        // Find existing RSP based on following naming convention, else create it.
        RspName rspName = new RspName(sfcPath.getName() + "-gbp-rsp");
        ReadOnlyTransaction rTx = UlnMapperDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        RenderedServicePath renderedServicePath;
        RenderedServicePath rsp = getRspByName(rspName, rTx);
        if (rsp == null) {
            renderedServicePath = createRsp(sfcPath, rspName);
            if (renderedServicePath != null) {
                LOG.info("ULN: ERROR: addSfcChain: Could not find RSP {} for Chain {}, created.", rspName,
                        sfcChainName);
            } else {
                LOG.error("ULN: ERROR: addSfcChain: Could not create RSP {} for Chain {}", rspName, sfcChainName);
                return null;
            }
        } else {
            renderedServicePath = rsp;
        }

        try {
            if (sfcPath.isSymmetric() && direction.equals(Direction.Out)) {
                rspName = new RspName(rspName.getValue() + "-Reverse");
                rsp = getRspByName(rspName, rTx);
                if (rsp == null) {
                    LOG.info("ULN: ERROR: addSfcChain: Could not find Reverse RSP {} for Chain {}", rspName,
                            sfcChainName);
                    renderedServicePath = createSymmetricRsp(renderedServicePath);
                    if (renderedServicePath == null) {
                        LOG.error("ULN: ERROR: addSfcChain: Could not create RSP {} for Chain {}", rspName,
                                sfcChainName);
                        return null;
                    }
                } else {
                    renderedServicePath = rsp;
                }
            }
        } catch (Exception e) {
            LOG.error("ULN: ERROR: addSfcChain: Attemping to determine if srcEp was consumer.", e);
            return null;
        }

        RenderedServicePathFirstHop rspFirstHop = SfcProviderRenderedPathAPI.readRenderedServicePathFirstHop(rspName);
        if (!isValidRspFirstHop(rspFirstHop)) {
            // Errors logged in method.
            return null;
        }

        RenderedServicePathHop firstRspHop = renderedServicePath.getRenderedServicePathHop().get(0);
        RenderedServicePathHop lastRspHop = Iterables.getLast(renderedServicePath.getRenderedServicePathHop());
        Ipv4Address nshTunIpDst = rspFirstHop.getIp().getIpv4Address();
        PortNumber nshTunUdpPort = rspFirstHop.getPort();
        Short nshNsiToChain = firstRspHop.getServiceIndex();
        Long nshNspToChain = renderedServicePath.getPathId();
        int nshNsiFromChain = (short) lastRspHop.getServiceIndex().intValue() - 1;
        Long nshNspFromChain = renderedServicePath.getPathId();

        return new SfcChainHeader(nshTunIpDst, nshTunUdpPort, nshNsiToChain, nshNspToChain, nshNsiFromChain,
                nshNspFromChain);
    }

    private boolean isValidRspFirstHop(RenderedServicePathFirstHop rspFirstHop) {
        boolean valid = true;
        if (rspFirstHop == null) {
            LOG.error("LUN: ERROR: isValidRspFirstHop: rspFirstHop is null.");
            return false;
        }
        if (rspFirstHop.getIp() == null || rspFirstHop.getIp().getIpv4Address() == null
                || rspFirstHop.getIp().getIpv6Address() != null) {
            LOG.error("LUN: ERROR: isValidRspFirstHop: rspFirstHop has invalid IP address.");
            valid = false;
        }
        if (rspFirstHop.getPort() == null) {
            LOG.error("LUN: ERROR: isValidRspFirstHop: rspFirstHop has no IP port .");
            valid = false;
        }
        if (rspFirstHop.getPathId() == null) {
            LOG.error("LUN: ERROR: isValidRspFirstHop: rspFirstHop has no Path Id (NSP).");
            valid = false;
        }
        if (rspFirstHop.getStartingIndex() == null) {
            LOG.error("LUN: ERROR: isValidRspFirstHop: rspFirstHop has no Starting Index (NSI)");
            valid = false;
        }
        return valid;
    }

    private RenderedServicePath createSymmetricRsp(RenderedServicePath rsp) {
        if (rsp == null) {
            return null;
        }
        return SfcProviderRenderedPathAPI.createSymmetricRenderedServicePathAndState(rsp);
    }

    private RenderedServicePath createRsp(ServiceFunctionPath sfcPath, RspName rspName) {
        CreateRenderedPathInput rspInput =
                new CreateRenderedPathInputBuilder().setParentServiceFunctionPath(sfcPath.getName().getValue())
                    .setName(rspName.getValue())
                    .setSymmetric(sfcPath.isSymmetric())
                    .build();
        return SfcProviderRenderedPathAPI.createRenderedServicePathAndState(sfcPath, rspInput);
    }

    private RenderedServicePath getRspByName(RspName rspName, ReadOnlyTransaction rTx) {
        Optional<RenderedServicePath> optRsp = UlnDatastoreApi.readFromDs(UlnIidFactory.rspIid(rspName), rTx);
        if (optRsp.isPresent()) {
            return optRsp.get();
        }
        return null;
    }

    public Map<Uuid, UserLogicalNetworkCache> getUlnStore() {
        return ulnStore;
    }

    public void setUlnStore(Map<Uuid, UserLogicalNetworkCache> ulnStore) {
        this.ulnStore = ulnStore;
    }

    /*
     * This function is called every time when a ULN element is cached and
     * attempting to be rendered. This function is necessary because ULN elements
     * depend on each other. when an new element is cached, it may not be able
     * to be rendered immediately and thus may be cached. It can be rendered
     * in the future when its dependencies are rendered.
     */
    private void checkAndRenderPendingUlnElements(Uuid tenantId, UserLogicalNetworkCache uln) {
        /*
         * There are should be no pending LRs and LSWs, because they
         * are directly rendered upon reception.
         */
        for (Entry<Uuid, LogicalRouterMappingInfo> lrEntry : uln.getLrStore().entrySet()) {
            if (lrEntry.getValue().hasServiceBeenRendered() == false) {
                LOG.error("FABMGR: ERROR: checkAndRenderPendingUlnElements: LR not renderred: {}",
                        lrEntry.getValue().getLr().getUuid().getValue());
                this.doLogicalRouterCreate(tenantId, uln, lrEntry.getValue().getLr());
            }
        }

        for (Entry<Uuid, LogicalSwitchMappingInfo> lswEntry : uln.getLswStore().entrySet()) {
            if (lswEntry.getValue().hasServiceBeenRendered() == false) {
                LOG.error("FABMGR: ERROR: checkAndRenderPendingUlnElements: LSW not renderred: {}",
                        lswEntry.getValue().getLsw().getUuid().getValue());
                this.doLogicalSwitchCreate(tenantId, uln, lswEntry.getValue().getLsw());
            }
        }

        for (Entry<Uuid, EndpointLocationMappingInfo> epEntry : uln.getEpLocationStore().entrySet()) {
            if (epEntry.getValue().hasServiceBeenRendered() == false) {
                LOG.info("FABMGR: checkAndRenderPendingUlnElements: found unrendered EP: {}",
                        epEntry.getValue().getEpLocation().getUuid().getValue());
                this.doEndpointLocationCreate(tenantId, uln, epEntry.getValue().getEpLocation());
            }
        }

        for (Entry<Uuid, EdgeMappingInfo> edgeEntry : uln.getEdgeStore().entrySet()) {
            if (edgeEntry.getValue().hasServiceBeenRendered() == false) {
                LOG.info("FABMGR: checkAndRenderPendingUlnElements: found unrendered edge: {}",
                        edgeEntry.getValue().getEdge().getUuid().getValue());
                this.doEdgeCreate(tenantId, uln, edgeEntry.getValue().getEdge());
            }
        }

        for (Entry<Uuid, SecurityRuleGroupsMappingInfo> rulesEntry : uln.getSecurityRuleGroupsStore().entrySet()) {
            if (rulesEntry.getValue().hasServiceBeenRendered() == false) {
                LOG.info("FABMGR: checkAndRenderPendingUlnElements: found unrendered rules: {}",
                        rulesEntry.getValue().getSecurityRuleGroups().getUuid().getValue());
                this.doSecurityRuleGroupsCreate(tenantId, uln, rulesEntry.getValue().getSecurityRuleGroups());
            }
        }
    }

    private void renderSecurityRule(Uuid tenantId, UserLogicalNetworkCache uln, NodeId nodeId,
            SecurityRuleGroupsMappingInfo ruleGroupsMappingInfo, SecurityRule securityRule) {
        String aclName = this.createAclFromSecurityRule(securityRule);
        VcontainerServiceProviderAPI.createAcl(UlnUtil.convertToYangUuid(tenantId), nodeId, aclName);
        ruleGroupsMappingInfo.addRenderedAclName(aclName);
    }

    private String createAclFromSecurityRule(SecurityRule securityRule) {
        String aclName = securityRule.getName().getValue();

        /*
         * create Access List with entries and IID, then write transaction to data store
         */
        AccessListEntries ace = this.createAceListFromSecurityRule(securityRule);
        AclBuilder aclBuilder = new AclBuilder();
        aclBuilder.setAclName(aclName).setKey(new AclKey(aclName)).setAccessListEntries(ace);

        InstanceIdentifier<Acl> aclPath =
                InstanceIdentifier.builder(AccessLists.class).child(Acl.class, new AclKey(aclName)).build();

        boolean transactionSuccessful =
                SfcDataStoreAPI.writePutTransactionAPI(aclPath, aclBuilder.build(), LogicalDatastoreType.CONFIGURATION);
        if (transactionSuccessful == false) {
            LOG.error("FABMGR: ERROR: createAclFromSecurityRule:writePutTransactionAPI failed");
        }

        return aclName;
    }

    private AccessListEntries createAceListFromSecurityRule(SecurityRule securityRule) {
        List<Ace> aceList = new ArrayList<>();
        List<RuleClassifier> classifierList = securityRule.getRuleClassifier();
        for (RuleClassifier classifier : classifierList) {
            Ace ace = this.createAceFromSecurityRuleEntry(classifier, securityRule.getRuleAction());
            aceList.add(ace);
        }

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();
        accessListEntriesBuilder.setAce(aceList);

        return accessListEntriesBuilder.build();
    }

    private Ace createAceFromSecurityRuleEntry(RuleClassifier classifier, List<RuleAction> ruleActionList) {

        /*
         * Use classifier to build matches. We setup destination ports using IN direction
         * classifier, and source ports using OUT direction classifier.
         */
        AceIpBuilder aceIpBuilder = new AceIpBuilder();
        aceIpBuilder.setDscp(new Dscp((short) 1)); // TODO: Do we have to setup DSCP?

        List<ParameterValue> pvList = classifier.getParameterValue();
        for (ParameterValue pv : pvList) {
            String pvName = pv.getName().getValue();
            if (pvName.equals(PV_PROTO_TYPE_NAME)) {
                aceIpBuilder.setProtocol((short) pv.getIntValue().longValue());
            } else if (pvName.equals(PV_DESTPORT_TYPE_NAME)) {
                DestinationPortRangeBuilder destinationPortRangeBuilder = new DestinationPortRangeBuilder();
                int portNum = (int) pv.getIntValue().longValue();
                destinationPortRangeBuilder.setLowerPort(new PortNumber(portNum));
                destinationPortRangeBuilder.setUpperPort(new PortNumber(portNum));
                aceIpBuilder.setDestinationPortRange(destinationPortRangeBuilder.build());
            } else if (pvName.equals(PV_SOURCEPORT_TYPE_NAME)) {
                int portNum = (int) pv.getIntValue().longValue();
                SourcePortRangeBuilder sourcePortRangeBuilder = new SourcePortRangeBuilder();
                sourcePortRangeBuilder.setLowerPort(new PortNumber(portNum));
                sourcePortRangeBuilder.setUpperPort(new PortNumber(portNum));
                aceIpBuilder.setSourcePortRange(sourcePortRangeBuilder.build());
            } else if (pvName.equals("ipv4_prefix_address")) { // TODO: what is the right value ?
                String ipAddrStr = pv.getStringValue();
                AceIpv4Builder aceIpv4Builder = new AceIpv4Builder();
                aceIpv4Builder.setSourceIpv4Network(new Ipv4Prefix(ipAddrStr));
                aceIpv4Builder.setDestinationIpv4Network(new Ipv4Prefix(ipAddrStr));
                aceIpBuilder.setAceIpVersion(aceIpv4Builder.build()).setProtocol((short) 4);
            } else if (pvName.equals("ipv6_prefix_address")) { // TODO: what is the right value ?
                String ipAddrStr = pv.getStringValue();
                AceIpv6Builder aceIpv6Builder = new AceIpv6Builder();
                aceIpv6Builder.setSourceIpv6Network(new Ipv6Prefix(ipAddrStr));
                aceIpv6Builder.setDestinationIpv6Network(new Ipv6Prefix(ipAddrStr));
                aceIpBuilder.setAceIpVersion(aceIpv6Builder.build()).setProtocol((short) 41);
            }
        }

        MatchesBuilder matchesBuilder = new MatchesBuilder();
        matchesBuilder.setInputInterface("interface-" + 1); // TODO: do we need to set this?
        matchesBuilder.setAceType(aceIpBuilder.build());

        /*
         * Create acl Action based on RuleAction. Although RuleAction
         * is a list, we only take the first element for our conversion,
         * because the ietf model only has one action per ace. By contrast,
         * in the ULN model one security_rule can have multiple rule_classifier and rule_action
         * instance.
         */
        ActionsBuilder actionsBuilder = new ActionsBuilder();
        RuleAction ruleAction = ruleActionList.get(0);
        ParameterValue pv = ruleAction.getParameterValue().get(0);
        String pvName = pv.getName().getValue();
        if (pvName.equals(PV_PERMIT_TYPE_NAME)) {
            String actionValue = pv.getStringValue();
            if (actionValue.equals(PV_ACTION_VALUE_ALLOW) == true) {
                PermitBuilder permitBuilder = new PermitBuilder();
                permitBuilder.setPermit(true);
                actionsBuilder.setPacketHandling(permitBuilder.build());
            } else if (actionValue.equals(PV_ACTION_VALUE_DENY) == true) {
                DenyBuilder denyBuilder = new DenyBuilder();
                denyBuilder.setDeny(true);
                actionsBuilder.setPacketHandling(denyBuilder.build());
            } else {
                LOG.error("FABMGR: ERROR: createAceFromSecurityRuleEntry: unknown actionValue: {}", actionValue);
            }

        } else if (pvName.equals(PV_SFC_TYPE_NAME)) {
            /*
             * This is SFC case.
             */
            Direction direction = classifier.getDirection();
            String sfcChainName = pv.getStringValue();
            LOG.info("FABMGR: createAceFromSecurityRuleEntry: ADD sfc chain: {}", sfcChainName);
            SfcChainHeader sfcChainHeader = retrieveSfcChain(sfcChainName, direction);
            if (sfcChainHeader == null) {
                LOG.error("FABMGR: ERROR: createAceFromSecurityRuleEntry: retrieveSfcChain() failed");
            }

            NshBuilder nshBuilder = new NshBuilder();
            nshBuilder.setDestIp(new IpAddress(sfcChainHeader.getNshTunIpDst()));
            nshBuilder.setDestPort(sfcChainHeader.getNshTunUdpPort());
            nshBuilder.setNsi(sfcChainHeader.getNshNsiToChain());
            nshBuilder.setNsp(sfcChainHeader.getNshNspToChain());

            TunnelBuilder tunnelBuilder = new TunnelBuilder();
            tunnelBuilder.setTunnelType(nshBuilder.build());

            RedirectBuilder redirectBuilder = new RedirectBuilder();
            redirectBuilder.setRedirectType(tunnelBuilder.build());

            actionsBuilder.setPacketHandling(redirectBuilder.build());
        }

        // set matches and actions
        String aceRuleName = classifier.getName().getValue();
        AceBuilder aceBuilder = new AceBuilder();
        aceBuilder.setRuleName(aceRuleName);
        aceBuilder.setMatches(matchesBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        return aceBuilder.build();
    }
}
