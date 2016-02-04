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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
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
    private static final String PV_SFC_TYPE_NAME = "sfc-chain-name";
    private static final String PV_PERMIT_TYPE_NAME = "action-definition-id";
    private static final String PV_ACTION_VALUE_ALLOW = "Action-Allow";
    private static final String PV_ACTION_VALUE_DENY = "Action-Deny";
    private static final String PV_PROTO_TYPE_NAME = "proto";
    private static final String PV_DESTPORT_TYPE_NAME = "destport";
    private static final String PV_SOURCEPORT_TYPE_NAME = "sourceport";

    private Map<Uuid, UserLogicalNetworkCache> ulnStore;
    private final Executor exec;
    private final Semaphore workerThreadLock;
    private boolean renderOnSingleFabric = true; // TODO: temporary workaround for Bug 5146
    private boolean applyAclToBothEpgs = true; // TODO: temporary workaround for Bug 5191
    private Map<Uuid, Uuid> lswLswPairStore;

    public UlnMappingEngine() {
        /*
         * TODO: We are experimenting Full Sync vs. concurrentMap.
         */
        boolean useSyncMap = false;
        if (useSyncMap) {
            this.ulnStore = Collections.synchronizedMap(new HashMap<Uuid, UserLogicalNetworkCache>());
        } else {
            this.ulnStore = new ConcurrentHashMap<Uuid, UserLogicalNetworkCache>();
        }
        this.lswLswPairStore = new HashMap<Uuid, Uuid>();
        this.exec = Executors.newSingleThreadExecutor();
        /*
         * Releases must occur before any acquires will be
         * granted. The worker thread is blocked initially.
         */
        this.workerThreadLock = new Semaphore(-1);
    }

    public synchronized void handleLswCreateEvent(LogicalSwitch lsw) {
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

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    private void doLogicalSwitchCreate(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        /*
         * For LSW, we can directly render it on Fabric.
         *
         * 29Jan2016: Due to Bug 5144, we cannot just directly render
         * an incoming LSW onto the fabric. Instead, LSW will be rendered
         * when EP registration takes place.
         *
         */
        // this.renderLogicalSwitch(tenantId, uln, lsw);
    }

    public synchronized void handleLrCreateEvent(LogicalRouter lr) {
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

        this.workerThreadLock.release();
    }

    private void doLogicalRouterCreate(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouter lr) {
        /*
         * For LR, we can directly render it on Fabric.
         *
         * 29Jan2016: Due to Bug 5146, we can create only one LR on Fabric per ULN.
         * So, we save the first (and only) LR in ULN cache, and skip rendering any
         * subsequent LRs.
         */
        if (this.renderOnSingleFabric == true) {
            if (uln.hasLrBeenRenderedOnFabric() == false) {
                this.renderLogicalRouter(tenantId, uln, lr);
            }
        } else {
            this.renderLogicalRouter(tenantId, uln, lr);
        }
    }

    public synchronized void handleSubnetCreateEvent(Subnet subnet) {
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

        this.workerThreadLock.release();
    }

    private void doSubnetCreate(Uuid tenantId, UserLogicalNetworkCache uln, Subnet subnet) {
        /*
         * For subnet, we do not need to render it, but due to Bug 5144,
         * we need to add device ID of the rendered LSW to all other
         * unrendered LSWs that are connected to the given subnet.
         */
        uln.updateLSWsConnectedToSubnet(subnet);
    }

    public synchronized void handlePortCreateEvent(Port port) {
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

        this.workerThreadLock.release();
    }

    /*
     * If this port belongs to a logical switch and the lsw
     * is already received (note that the lsw should also have
     * been rendered, because lsw is rendered upon reception),
     * then we call renderPortOnLsw()
     */
    @SuppressWarnings("unused")
    private void doPortCreate(Uuid tenantId, UserLogicalNetworkCache uln, Port port) {
        port.getLocationType();

        if (uln.isPortLswType(port.getUuid()) == false) {
            // Only ports on LSW need to be rendered.
            return;
        }

        LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(port);
        if (lsw == null) {
            LOG.debug("FABMGR: doPortCreate: lsw not in cache");
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

    public synchronized void handleEndpointLocationCreateEvent(EndpointLocation epLocation) {
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

        this.workerThreadLock.release();
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

        EdgeMappingInfo subnetLswEdge = uln.findSingleSubnetLswEdgeFromSubnet(subnet);
        if (subnetLswEdge == null) {
            LOG.debug("FABMGR: renderEpRegistration: cannot find subnetLswEdge in cache");
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
         * If we get here, then we have received all the
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
            uln.addPortToLsw(lsw.getLsw(), lswPort.getPort());
        }

        EndpointAttachInfo endpoint = UlnUtil.createEpAttachmentInput(epLocation, subnet.getSubnet(), epPort.getPort());

        this.renderEpRegistration(tenantId, uln, epLocation, lsw.getRenderedDeviceId(), lswPort.getRenderedDeviceId(),
                endpoint);
        uln.setLswIdOnEpLocation(epLocation, lsw.getLsw().getUuid());
        uln.setLswPortIdOnEpLocation(epLocation, lswPort.getPort().getUuid());
        uln.markEdgeAsRendered(epEdge.getEdge());
        uln.markPortAsRendered(subnetPort.getPort());
        uln.markPortAsRendered(subnetPort2.getPort());
        uln.markEdgeAsRendered(subnetLswEdge.getEdge());
        uln.markSubnetAsRendered(subnet.getSubnet()); // subnet being rendered meaning its LSW has
                                                      // been chosen and rendered.
    }

    public synchronized void handleEdgeCreateEvent(Edge edge) {
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

        this.workerThreadLock.release();
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
        if (uln.isEdgeLrToLrType(edge)) {
            /*
             * We need to apply ACL to both consumer and producer EPG (Bug 5191).
             * So, when we detect a LR-LR edge, we use it to find the LSW-to-LSW
             * pair, and cache that information. Note that we do not cache this
             * information in UlserLogicalNetworkCache. Instead we cache it in this
             * class. Also note that one EPG may contain multiple LSWs, so really we
             * should find a set of LSws to a set of LSW mapping. But since the real
             * fix for Bug 5191 is to change the ULN model, we only put a simple fix here.
             */
            if (this.renderOnSingleFabric && this.applyAclToBothEpgs) {
                this.doFindLswToLswPair(tenantId, uln, edge);
            }
            return;
        }

        boolean canRenderEdge = false;
        LogicalSwitchMappingInfo lsw = null;
        LogicalRouterMappingInfo lr = null;
        SubnetMappingInfo subnet = null;
        PortMappingInfo leftPort = null;
        PortMappingInfo rightPort = null;

        if (uln.isEdgeLrToLswType(edge) == true) {
            LOG.trace("FABMGR: doEdgeCreate: found LrToLsw edge: {}", edge.getUuid().getValue());
            leftPort = uln.findLeftPortOnEdge(edge);
            rightPort = uln.findRightPortOnEdge(edge);
            if (leftPort != null && rightPort != null) {
                if (uln.isPortLswType(leftPort.getPort().getUuid()) == true) {
                    lsw = uln.findLswFromItsPort(leftPort.getPort());
                    if (lsw != null && uln.isPortLrType(rightPort.getPort().getUuid()) == true) {
                        subnet = uln.findSubnetFromLsw(lsw);
                        lr = uln.findLrFromItsPort(rightPort.getPort());
                        if (lr != null && subnet != null && lsw.hasServiceBeenRendered() == true) {
                            /*
                             * If lsw is already rendered, then that means
                             * we can safely add the gateway. If lsw is not
                             * rendered, we do not know if that lsw should be
                             * created or not (due to bug 5144). So we need to wait
                             * for EpRegistration to render the correct lsw first.
                             */
                            canRenderEdge = true;
                        }
                    }
                } else if (uln.isPortLswType(rightPort.getPort().getUuid()) == true) {
                    lsw = uln.findLswFromItsPort(rightPort.getPort());
                    if (lsw != null && uln.isPortLrType(leftPort.getPort().getUuid()) == true) {
                        subnet = uln.findSubnetFromLsw(lsw);
                        lr = uln.findLrFromItsPort(leftPort.getPort());
                        if (lr != null && subnet != null && lsw.hasServiceBeenRendered() == true) {
                            canRenderEdge = true;
                        }
                    }
                }
            }
        }

        if (canRenderEdge == true) {
            NodeId lrDevId = null;
            NodeId lswDevId = null;
            IpAddress gatewayIp = null;
            IpPrefix ipPrefix = null;

            // if (lsw.hasServiceBeenRendered() == false) {
            // LOG.error("FABMGR: ERROR: doEdgeCreate: lsw is not rendered. edgeId={}, lswId={}",
            // edge.getUuid().getValue(), lsw.getLsw().getUuid().getValue());
            // lswDevId = this.renderLogicalSwitch(tenantId, uln, lsw.getLsw());
            // } else {
            lswDevId = lsw.getRenderedDeviceId();
            // }

            if (lswDevId == null) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: lswDevId is null. edgeId={}, lswId={}",
                        edge.getUuid().getValue(), lsw.getLsw().getUuid().getValue());
                return;
            }

            /*
             * One ULN can have only one rendered LR (Bug 5146). If LR is already rendered,
             * then use it. If not, then render this LR as the first and only LR
             * to render.
             */
            if (this.renderOnSingleFabric == true) {
                if (uln.hasLrBeenRenderedOnFabric() == false) {
                    if (lr.hasServiceBeenRendered() == false) {
                        lrDevId = this.renderLogicalRouter(tenantId, uln, lr.getLr());
                    } else {
                        LOG.error("FABMGR: ERROR: doEdgeCreate: lr is rendered but not cached. edgeId={}, lrId={}",
                                edge.getUuid().getValue(), lr.getLr().getUuid().getValue());
                    }
                } else {
                    lrDevId = uln.getRenderedLrDeviceId();
                }
            } else {
                if (lr.hasServiceBeenRendered() == false) {
                    lrDevId = this.renderLogicalRouter(tenantId, uln, lr.getLr());
                } else {
                    lrDevId = uln.getRenderedLrDeviceId();
                }
            }

            if (lrDevId == null) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: lrDevId is null. edgeId={}, lrId={}", edge.getUuid().getValue(),
                        lr.getLr().getUuid().getValue());
                return;
            }

            gatewayIp = subnet.getSubnet().getVirtualRouterIp();
            if (gatewayIp == null) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: gatewayIp is null. edgeId={}, subnetId={}",
                        edge.getUuid().getValue(), subnet.getSubnet().getUuid().getValue());
                return;
            }

            ipPrefix = subnet.getSubnet().getIpPrefix();
            if (ipPrefix == null) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: ipPrefix is null. edgeId={}, subnetId={}",
                        edge.getUuid().getValue(), subnet.getSubnet().getUuid().getValue());
                return;
            }

            LOG.debug("FABMGR: doEdgeCreate: edgeId={}, lrDevId={}, lswDevId={}, gateway={}, ipPrefix={}",
                    edge.getUuid().getValue(), lrDevId.getValue(), lswDevId.getValue(),
                    gatewayIp.getIpv4Address().getValue(), ipPrefix.getIpv4Prefix().getValue());
            this.renderLrLswEdge(tenantId, uln, lrDevId, lswDevId, gatewayIp, ipPrefix, edge);

            /*
             * One subnet can have only one gateway.
             * Mark this subnet as rendered so that we know no more
             * gateways can be rendered for this subnet. There is a bug (Bug 5144)
             * in ULN model that permits subnet to have multiple gateways.
             */
            if (subnet.hasServiceBeenRendered() == false) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: subnet is not rendered. subnetId={}",
                        subnet.getSubnet().getUuid().getValue());
                uln.markSubnetAsRendered(subnet.getSubnet());
            }

            /*
             * The following calls are for debug info collection purpose
             */
            uln.addLrLswEdgeToLsw(lsw.getLsw(), edge);
            uln.addLrLswEdgeToLr(lr.getLr(), edge);
            uln.addGatewayIpToLr(lr.getLr(), gatewayIp);
            uln.addLrLswEdgeToPort(leftPort.getPort(), edge);
            uln.addLrLswEdgeToPort(rightPort.getPort(), edge);
        }

    }

    public synchronized void handleSecurityRuleGroupsCreateEvent(SecurityRuleGroups ruleGroups) {
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

        this.workerThreadLock.release();
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
        List<Uuid> portList = ruleGroups.getPorts();
        if (portList == null) {
            /*
             * This means we cannot render this ruleGroups yet. We
             * need to wait for the update event to update the ports.
             */
            LOG.debug("FABMGR: doSecurityRuleGroupsCreate: portList is null. do nothing");
            return;
        }

        Uuid portId = portList.get(0);
        LogicalSwitchMappingInfo lsw = uln.findLswFromPortId(portId);
        if (lsw != null) {
            if (lsw.hasServiceBeenRendered() == false) {
                LOG.debug("FABMGR: doSecurityRuleGroupsCreate: lsw not rendered: {}",
                        lsw.getLsw().getUuid().getValue());
                nodeId = this.renderLogicalSwitch(tenantId, uln, lsw.getLsw());
            } else {
                nodeId = lsw.getRenderedDeviceId();
            }
            if (nodeId == null) {
                LOG.error("FABMGR:doSecurityRuleGroupsCreate: lsw nodeId is null");
            } else {
                uln.addSecurityRuleGroupsToLsw(lsw.getLsw(), ruleGroups);
                readyToRender = true;
            }
        } else {
            LogicalRouterMappingInfo lr = uln.findLrFromPortId(portId);
            if (lr != null) {
                if (this.renderOnSingleFabric == true) {
                    /*
                     * Due to Bug 5146, we cannot apply ACL on LR. We need to find
                     * the LSW which connects to this LR and apply ACL rules there.
                     *
                     * Due to Bug 5191, we have to apply ACL to both EPGs. So, we
                     * cannot render ACL until both LSWs are rendered.
                     */
                    if (this.applyAclToBothEpgs == true) {
                        this.doAclCreateOnLswPair(tenantId, uln, lr, ruleGroups);
                        return;
                    } else {
                        nodeId = uln.findLswRenderedDeviceIdFromLr(lr);
                        if (nodeId != null) {
                            readyToRender = true;
                        }
                    }
                } else {
                    if (lr.hasServiceBeenRendered() == false) {
                        LOG.debug("FABMGR: doSecurityRuleGroupsCreate: lr not rendered: {}",
                                lr.getLr().getUuid().getValue());
                        nodeId = this.renderLogicalRouter(tenantId, uln, lr.getLr());
                    } else {
                        nodeId = lr.getRenderedDeviceId();
                    }
                    if (nodeId == null) {
                        LOG.error("FABMGR:doSecurityRuleGroupsCreate: lr nodeId is null");
                    } else {
                        uln.addSecurityRuleGroupsToLr(lr.getLr(), ruleGroups);
                        readyToRender = true;
                    }
                }
            }
        }

        if (readyToRender == true) {
            this.renderSecurityRuleGroups(tenantId, uln, nodeId, ruleGroups);
        }
    }

    private void doAclCreateOnLswPair(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouterMappingInfo lr,
            SecurityRuleGroups ruleGroups) {
        NodeId nodeId = null;
        NodeId nodeId2 = null;

        EdgeMappingInfo lrLswEdge = uln.findLrLswEdge(lr);
        if (lrLswEdge == null) {
            return;
        }
        PortMappingInfo lswPort = uln.findLswPortOnEdge(lrLswEdge);
        if (lswPort == null) {
            return;
        }
        LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(lswPort.getPort());
        if (lsw == null) {
            return;
        }
        nodeId = lsw.getRenderedDeviceId();
        if (nodeId == null) {
            return;
        }

        Uuid lswId = lsw.getLsw().getUuid();
        Uuid lswId2 = this.lswLswPairStore.get(lswId);
        LogicalSwitchMappingInfo lsw2 = uln.findLswFromLswId(lswId2);
        nodeId2 = lsw2.getRenderedDeviceId();
        if (nodeId2 == null) {
            return;
        }

        this.renderSecurityRuleGroupsOnPair(tenantId, uln, nodeId, nodeId2, ruleGroups);

    }

    private NodeId renderLogicalSwitch(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        CreateLneLayer2Input input = UlnUtil.createLneLayer2Input(tenantId, lsw);
        NodeId renderedLswId = VcontainerServiceProviderAPI.createLneLayer2(UlnUtil.convertToYangUuid(tenantId), input);
        uln.markLswAsRendered(lsw, renderedLswId);
        return renderedLswId;
    }

    private NodeId renderLogicalRouter(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouter lr) {
        CreateLneLayer3Input input = UlnUtil.createLneLayer3Input(tenantId, lr);
        NodeId renderedLrId = VcontainerServiceProviderAPI.createLneLayer3(UlnUtil.convertToYangUuid(tenantId), input);
        uln.markLrAsRendered(lr, renderedLrId);
        uln.setRenderedlrOnFabric(uln.getLrMappingInfo(lr));
        return renderedLrId;
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

    private void renderLrLswEdge(Uuid tenantId, UserLogicalNetworkCache uln, NodeId lrId, NodeId lswId,
            IpAddress gatewayIpAddr, IpPrefix ipPrefix, Edge edge) {
        VcontainerServiceProviderAPI.createLrLswGateway(UlnUtil.convertToYangUuid(tenantId), lrId, lswId, gatewayIpAddr,
                ipPrefix);
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
                uln.findSecurityRuleGroupsFromRuleGroupsId(ruleGroups.getUuid());
        if (ruleGroupsMappingInfo == null) {
            LOG.error("FABMGR: ERROR: renderSecurityRuleGroups: ruleGroupsMappingInfo is null");
            return;
        }
        List<SecurityRuleGroup> ruleGroupList = ruleGroups.getSecurityRuleGroup();
        for (SecurityRuleGroup ruleGroup : ruleGroupList) {
            List<SecurityRule> ruleList = ruleGroup.getSecurityRule();
            for (SecurityRule rule : ruleList) {
                String aclName = this.createAclFromSecurityRule(rule);
                this.renderSecurityRule(tenantId, uln, nodeId, ruleGroupsMappingInfo, aclName);
            }
        }

        uln.markSecurityRuleGroupsAsRendered(ruleGroups);
    }

    private void renderSecurityRuleGroupsOnPair(Uuid tenantId, UserLogicalNetworkCache uln, NodeId nodeId,
            NodeId nodeId2, SecurityRuleGroups ruleGroups) {
        /*
         * One SecurityRuleGroups contains a list SecurityRuleGroup.
         * One SecurityRuleGroup contains a list of SecurityRule.
         * One SecurityRule can be mapped to one ietf-acl.
         */
        SecurityRuleGroupsMappingInfo ruleGroupsMappingInfo =
                uln.findSecurityRuleGroupsFromRuleGroupsId(ruleGroups.getUuid());
        if (ruleGroupsMappingInfo == null) {
            LOG.error("FABMGR: ERROR: renderSecurityRuleGroups: ruleGroupsMappingInfo is null");
            return;
        }
        List<SecurityRuleGroup> ruleGroupList = ruleGroups.getSecurityRuleGroup();
        for (SecurityRuleGroup ruleGroup : ruleGroupList) {
            List<SecurityRule> ruleList = ruleGroup.getSecurityRule();
            for (SecurityRule rule : ruleList) {
                String aclName = this.createAclFromSecurityRule(rule);
                this.renderSecurityRule(tenantId, uln, nodeId, ruleGroupsMappingInfo, aclName);
                this.renderSecurityRule(tenantId, uln, nodeId2, ruleGroupsMappingInfo, aclName);
            }
        }

        uln.markSecurityRuleGroupsAsRendered(ruleGroups);
    }

    private ServiceFunctionPath getSfcPath(SfcName chainName) {
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
                LOG.debug("ULN: ERROR: addSfcChain: Could not find RSP {} for Chain {}, created.", rspName,
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
                    LOG.debug("ULN: ERROR: addSfcChain: Could not find Reverse RSP {} for Chain {}", rspName,
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

    /*
     * This function is called every time when a ULN element is cached and
     * attempting to be rendered. This function is necessary because ULN elements
     * depend on each other. when an new element is cached, it may not be able
     * to be rendered immediately and thus may be cached. It can be rendered
     * in the future when its dependencies are rendered.
     */
    private void inspectUlnCache() {
        for (Entry<Uuid, UserLogicalNetworkCache> entry : this.ulnStore.entrySet()) {
            Uuid tenantId = entry.getKey();
            UserLogicalNetworkCache uln = entry.getValue();
            this.processPendingUlnRequests(tenantId, uln);
        }
    }

    private void processPendingUlnRequests(Uuid tenantId, UserLogicalNetworkCache uln) {
        /*
         * For add requests, LR and LSW can be created directly, as
         * they have no dependencies. For deletion, they cannot be deleted until
         * ACL rules and logical ports are deleted.
         */
        for (Entry<Uuid, LogicalRouterMappingInfo> lrEntry : uln.getLrStore().entrySet()) {
            LogicalRouterMappingInfo info = lrEntry.getValue();
            if (info.isToBeDeleted() == true) {
                if (info.hasServiceBeenRendered() == true) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doLogicalRouterRemove: {}",
                            info.getLr().getUuid().getValue());
                    this.doLogicalRouterRemove(tenantId, uln, info.getLr());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeLrFromCache: {}",
                            info.getLr().getUuid().getValue());
                    uln.removeLrFromCache(info.getLr());
                }
            } else if (info.hasServiceBeenRendered() == false) {
                LOG.debug("FABMGR: processPendingUlnRequests: doLogicalRouterCreate: {}",
                        info.getLr().getUuid().getValue());
                this.doLogicalRouterCreate(tenantId, uln, info.getLr());
            }
        }

        /*
         * LSW addition dependency: None
         * LSW deletion dependency: ACL, Logical Ports, Gateway
         */
        for (Entry<Uuid, LogicalSwitchMappingInfo> lswEntry : uln.getLswStore().entrySet()) {
            LogicalSwitchMappingInfo info = lswEntry.getValue();
            if (info.isToBeDeleted() == true) {
                if (info.hasServiceBeenRendered() == true) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doLogicalSwitchRemove: {}",
                            info.getLsw().getUuid().getValue());
                    this.doLogicalSwtichRemove(tenantId, uln, info.getLsw());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeLswFromCache: {}",
                            info.getLsw().getUuid().getValue());
                    uln.removeLswFromCache(info.getLsw());
                }
            } else if (info.hasServiceBeenRendered() == false) {
                LOG.debug("FABMGR: processPendingUlnRequests: doLogicalSwitchCreate: {}",
                        info.getLsw().getUuid().getValue());
                this.doLogicalSwitchCreate(tenantId, uln, info.getLsw());
            }
        }

        /*
         * EP addition dependency: LSW
         * EP deletion dependency: None
         */
        for (Entry<Uuid, EndpointLocationMappingInfo> epEntry : uln.getEpLocationStore().entrySet()) {
            EndpointLocationMappingInfo info = epEntry.getValue();
            if (info.isToBeDeleted() == true) {
                if (info.hasServiceBeenRendered() == true) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doEndpointLocationRemove: {}",
                            info.getEpLocation().getUuid().getValue());
                    this.doEndpointLocationRemove(tenantId, uln, info.getEpLocation());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeEpLocationFromCache: {}",
                            info.getEpLocation().getUuid().getValue());
                    uln.removeEpLocationFromCache(info.getEpLocation());
                }
            } else if (info.hasServiceBeenRendered() == false) {
                LOG.debug("FABMGR: processPendingUlnRequests: doEndpointLocationCreate: {}",
                        info.getEpLocation().getUuid().getValue());
                this.doEndpointLocationCreate(tenantId, uln, info.getEpLocation());
            }
        }

        /*
         * LSW-LR Edge addition dependency: LSW and LR
         * LSW-LR Edge deletion dependency: None
         *
         * NOTE: LSW-LR Edge is mapped to gateway. Other edge types are
         * not mapped.
         */
        for (Entry<Uuid, EdgeMappingInfo> edgeEntry : uln.getEdgeStore().entrySet()) {
            EdgeMappingInfo info = edgeEntry.getValue();
            if (info.isToBeDeleted() == true) {
                if (info.hasServiceBeenRendered() == true) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doEdgeRemove: {}",
                            info.getEdge().getUuid().getValue());
                    this.doEdgeRemove(tenantId, uln, info.getEdge());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeEdgeFromCache: {}",
                            info.getEdge().getUuid().getValue());
                    uln.removeEdgeFromCache(info.getEdge());
                }
            } else if (info.hasServiceBeenRendered() == false) {
                LOG.debug("FABMGR: processPendingUlnRequests: doEdgeCreate: {}", info.getEdge().getUuid().getValue());
                this.doEdgeCreate(tenantId, uln, info.getEdge());
            }
        }

        /*
         * ACL addition dependency: LSW or LR
         * ACL deletion dependency: None
         */
        for (Entry<Uuid, SecurityRuleGroupsMappingInfo> rulesEntry : uln.getSecurityRuleGroupsStore().entrySet()) {
            if (rulesEntry.getValue().isToBeDeleted() == true) {
                if (rulesEntry.getValue().hasServiceBeenRendered() == true) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doSecurityRuleGroupsRemove: {}",
                            rulesEntry.getValue().getSecurityRuleGroups().getUuid().getValue());
                    this.doSecurityRuleGroupsRemove(tenantId, uln, rulesEntry.getValue().getSecurityRuleGroups());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeSecurityRuleGroupsFromCache: {}",
                            rulesEntry.getValue().getSecurityRuleGroups().getUuid().getValue());
                    uln.removeSecurityRuleGroupsFromCache(rulesEntry.getValue().getSecurityRuleGroups());
                }
            } else if (rulesEntry.getValue().hasServiceBeenRendered() == false) {
                LOG.debug("FABMGR: processPendingUlnRequests: doSecurityRuleGroupsCreate: {}",
                        rulesEntry.getValue().getSecurityRuleGroups().getUuid().getValue());
                this.doSecurityRuleGroupsCreate(tenantId, uln, rulesEntry.getValue().getSecurityRuleGroups());
            }
        }

        /*
         * Subnet addition dependency: subnets are not mapped to Fabric; no need to render
         * Subnet deletion dependency: None. can be directly deleted from cache
         */
        for (Entry<Uuid, SubnetMappingInfo> subnetEntry : uln.getSubnetStore().entrySet()) {
            if (subnetEntry.getValue().isToBeDeleted() == true) {
                LOG.debug("FABMGR: processPendingUlnRequests: delete subnet: {}",
                        subnetEntry.getValue().getSubnet().getUuid().getValue());
                uln.removeSubnetFromCache(subnetEntry.getValue().getSubnet());
            } else {
                LOG.debug("FABMGR: processPendingUlnRequests: create subnet: {}",
                        subnetEntry.getValue().getSubnet().getUuid().getValue());
                this.doSubnetCreate(tenantId, uln, subnetEntry.getValue().getSubnet());
            }
        }

        /*
         * Port addition dependency: ports are not mapped to Fabric; no need to render
         * Port deletion dependency: Gateway
         */
        for (Entry<Uuid, PortMappingInfo> portEntry : uln.getPortStore().entrySet()) {
            PortMappingInfo info = portEntry.getValue();
            if (info.isToBeDeleted() == true) {
                if (info.isLrLswEdgeListEmpty() == true) {
                    LOG.debug("FABMGR: processPendingUlnRequests: removePortFromCache: {}",
                            portEntry.getValue().getPort().getUuid().getValue());
                    uln.removePortFromCache(portEntry.getValue().getPort());
                } else {
                    LOG.trace("FABMGR: processPendingUlnRequests: port LrLswEdgeList not Empty: {}",
                            portEntry.getValue().getPort().getUuid().getValue());
                }
            }
        }
    }

    private void doSecurityRuleGroupsRemove(Uuid tenantId, UserLogicalNetworkCache uln, SecurityRuleGroups ruleGroups) {
        /*
         * We check to see if the LSW or LR is rendered. If not, then we
         * generate error and abort. The model supports the rules
         * to be be applied on multiple ports, but in practice, GBP only use
         * one port. So, we can just use port 0 to fin the LSW (or LR).
         */
        NodeId nodeId = null;
        List<Uuid> portList = ruleGroups.getPorts();
        if (portList == null) {
            /*
             * This means we cannot render this ruleGroups yet. We
             * need to wait for the update event to update the ports.
             */
            LOG.error("FABMGR: ERROR: doSecurityRuleGroupsRemove: portList is null.");
            return;
        }

        Uuid portId = portList.get(0);
        LogicalSwitchMappingInfo lsw = uln.findLswFromPortId(portId);
        if (lsw != null) {
            if (lsw.hasServiceBeenRendered() == false) {
                LOG.error("FABMGR: ERROR: doSecurityRuleGroupsRemove: lsw not rendered: {}",
                        lsw.getLsw().getUuid().getValue());
                return;
            } else {
                nodeId = lsw.getRenderedDeviceId();
            }
            if (nodeId == null) {
                LOG.error("FABMGR: ERROR: doSecurityRuleGroupsRemove: lsw nodeId is null");
                return;
            } else {
                /*
                 * We are now ready to remove ACL. After ACL is removed, The
                 * LSW can also be removed
                 */
                LOG.debug("FABMGR: doSecurityRuleGroupsRemove: calling removeSecurityRuleGroups...");
                this.removeSecurityRuleGroupsFromFabric(tenantId, uln, nodeId, ruleGroups);
                uln.removeSecurityRuleGroupsFromLsw(lsw.getLsw(), ruleGroups);
                uln.removeSecurityRuleGroupsFromCache(ruleGroups);
            }
        } else {
            LogicalRouterMappingInfo lr = uln.findLrFromPortId(portId);
            if (lr != null) {
                if (lr.hasServiceBeenRendered() == false) {
                    LOG.error("FABMGR: ERROR: doSecurityRuleGroupsRemove: lr not rendered: {}",
                            lr.getLr().getUuid().getValue());
                    return;
                } else {
                    nodeId = lr.getRenderedDeviceId();
                }
                if (nodeId == null) {
                    LOG.error("FABMGR: ERROR: doSecurityRuleGroupsCreate: lr nodeId is null");
                    return;
                } else {
                    /*
                     * We are now ready to remove ACL. After ACL is removed, The
                     * LSW can also be removed
                     */
                    LOG.debug("FABMGR: doSecurityRuleGroupsRemove: calling removeSecurityRuleGroups");
                    this.removeSecurityRuleGroupsFromFabric(tenantId, uln, nodeId, ruleGroups);
                    uln.removeSecurityRuleGroupsFromLr(lr.getLr(), ruleGroups);
                    uln.removeSecurityRuleGroupsFromCache(ruleGroups);
                }
            }
        }
    }

    private void removeSecurityRuleGroupsFromFabric(Uuid tenantId, UserLogicalNetworkCache uln, NodeId nodeId,
            SecurityRuleGroups ruleGroups) {
        SecurityRuleGroupsMappingInfo ruleGroupsInfo = uln.findSecurityRuleGroupsFromRuleGroupsId(ruleGroups.getUuid());
        if (ruleGroupsInfo == null) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromFabric: ruleGroups not in cache: {}",
                    ruleGroups.getUuid().getValue());
            return;
        }

        if (ruleGroupsInfo.hasServiceBeenRendered() == false) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromFabric: securityRuleGroups has not been rendered: {}",
                    ruleGroups.getUuid().getValue());
            return;
        }

        /*
         * One SecurityRuleGroups may be mapped to multiple ACL rules.
         * So we need to delete ACL in a loop.
         */
        List<String> aclNameList = ruleGroupsInfo.getRenderedAclNameList();
        if (aclNameList == null || aclNameList.isEmpty() == true) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromFabric: alcNameList is null or empty: {}",
                    ruleGroups.getUuid().getValue());
            return;
        }
        for (String aclName : aclNameList) {
            this.removeAclFromFabric(tenantId, uln, nodeId, aclName);
        }
    }

    private void removeAclFromFabric(Uuid tenantId, UserLogicalNetworkCache uln, NodeId nodeId, String aclName) {
        VcontainerServiceProviderAPI.removeAcl(UlnUtil.convertToYangUuid(tenantId), nodeId, aclName);

        this.removeAclFromDatastore(aclName);
    }

    private void removeAclFromDatastore(String aclName) {
        InstanceIdentifier<Acl> aclPath =
                InstanceIdentifier.builder(AccessLists.class).child(Acl.class, new AclKey(aclName)).build();

        boolean transactionSuccessful =
                SfcDataStoreAPI.deleteTransactionAPI(aclPath, LogicalDatastoreType.CONFIGURATION);
        if (transactionSuccessful == false) {
            LOG.error("FABMGR: ERROR: removeAclFromDatastore: deleteTransactionAPI failed: {}", aclName);
        }
    }

    private void doEdgeRemove(Uuid tenantId, UserLogicalNetworkCache uln, Edge edge) {
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
         * multi-fabric.) so, we can remove other types of edges
         * directly from the ULN cache.
         */
        if (uln.isEdgeLrToLswType(edge) == false) {
            uln.removeEdgeFromCache(edge);
            return;
        }

        /*
         * Now this is a Lr-Lsw edge. We need to remove its
         * reference from the two Ports, LR and LSW. Then call the Fabric
         * RPC to remove the Gateway on Fabric.
         */
        LOG.debug("FABMGR: doEdgeRemove: found LrToLsw edge: {}", edge.getUuid().getValue());
        if (uln.isEdgeRendered(edge) == false) {
            LOG.error("FABMGR: ERROR: doEdgeRemove: LrToLsw edge not rendered: {}", edge.getUuid().getValue());
            return;
        }

        LogicalSwitchMappingInfo lsw = null;
        LogicalRouterMappingInfo lr = null;
        PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
        PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);
        if (leftPort != null && rightPort != null) {
            if (uln.isPortLswType(leftPort.getPort().getUuid()) == true) {
                lsw = uln.findLswFromItsPort(leftPort.getPort());
                if (lsw != null && uln.isPortLrType(rightPort.getPort().getUuid()) == true) {
                    uln.removeLrLswEdgeFromLsw(lsw.getLsw(), edge);
                    lr = uln.findLrFromItsPort(rightPort.getPort());
                    if (lr != null) {
                        uln.removeLrLswEdgeFromLr(lr.getLr(), edge);
                    } else {
                        LOG.error("FABMGR: ERROR: doEdgeRemove: lr is null: {}", edge.getUuid().getValue());
                        return;
                    }
                } else {
                    LOG.error("FABMGR: ERROR: doEdgeRemove: lsw is null: {}", edge.getUuid().getValue());
                    return;
                }
            } else if (uln.isPortLswType(rightPort.getPort().getUuid()) == true) {
                lsw = uln.findLswFromItsPort(rightPort.getPort());
                if (lsw != null && uln.isPortLrType(leftPort.getPort().getUuid()) == true) {
                    uln.removeLrLswEdgeFromLsw(lsw.getLsw(), edge);
                    lr = uln.findLrFromItsPort(leftPort.getPort());
                    if (lr != null) {
                        uln.removeLrLswEdgeFromLr(lr.getLr(), edge);
                    } else {
                        LOG.error("FABMGR: ERROR: doEdgeRemove: lr is null: {}", edge.getUuid().getValue());
                        return;
                    }
                } else {
                    LOG.error("FABMGR: ERROR: doEdgeRemove: lsw is null: {}", edge.getUuid().getValue());
                    return;
                }
            }
        } else {
            LOG.error("FABMGR: ERROR: doEdgeRemove: at lease on port is null: {}", edge.getUuid().getValue());
        }

        uln.removeLrLswEdgeFromPort(leftPort.getPort(), edge);
        uln.removeLrLswEdgeFromPort(rightPort.getPort(), edge);

        NodeId lrDevId = null;
        IpAddress gatewayIp = null;

        lrDevId = lr.getRenderedDeviceId();

        if (lrDevId == null) {
            LOG.error("FABMGR: ERROR: doEdgeRemove: lrDevId is null. edgeId={}, lrId={}", edge.getUuid().getValue(),
                    lr.getLr().getUuid().getValue());
            return;
        }

        gatewayIp = lr.getGatewayIpAddr();
        if (gatewayIp == null) {
            LOG.error("FABMGR: doEdgeCreate: gatewayIp is null. edgeId={}, lrId={}", edge.getUuid().getValue(),
                    lr.getLr().getUuid().getValue());
            return;
        }

        LOG.debug("FABMGR: doEdgeRemove: edgeId={}, lrDevId={}, gateway={}", edge.getUuid().getValue(),
                lrDevId.getValue(), gatewayIp.getIpv4Address().getValue());
        this.removeLrLswEdgeFromFabric(tenantId, uln, lrDevId, gatewayIp);
        uln.removeEdgeFromCache(edge);
    }

    private void removeLrLswEdgeFromFabric(Uuid tenantId, UserLogicalNetworkCache uln, NodeId lrDevId,
            IpAddress gatewayIp) {
        VcontainerServiceProviderAPI.removeLrLswGateway(UlnUtil.convertToYangUuid(tenantId), lrDevId, gatewayIp);
    }

    private void doEndpointLocationRemove(Uuid tenantId, UserLogicalNetworkCache uln, EndpointLocation epLocation) {
        EndpointLocationMappingInfo epInfo = uln.findEpLocationFromEpLocationId(epLocation.getUuid());
        if (epInfo == null) {
            LOG.error("FABMGR: ERROR: doEndpointLocationRemove: epLocation not in cache: {}",
                    epLocation.getUuid().getValue());
            return;
        }

        if (epInfo.hasServiceBeenRendered() == false) {
            LOG.error("FABMGR: ERROR: doEndpointLocationRemove: EP has not been rendered: {}",
                    epLocation.getUuid().getValue());
            return;
        }

        Uuid lswId = epInfo.getLswId();
        LogicalSwitchMappingInfo lsw = uln.findLswFromLswId(lswId);
        if (lsw == null) {
            LOG.error("FABMGR: ERROR: doEndpointLocationRemove: lsw not in cache: {}", lswId.getValue());
            return;
        }

        Uuid lswPortId = epInfo.getLswPortId();
        PortMappingInfo lswPort = uln.findPortFromPortId(lswPortId);
        if (lswPort == null) {
            LOG.error("FABMGR: ERROR: doEndpointLocationRemove: lswPort not in cache: {}", lswPortId.getValue());
            return;
        }

        /*
         * Remove the reference to the logical port from LSW.
         * LSW can be removed when its dependency reference counters drop
         * to zero.
         */
        uln.removePortFromLsw(lsw.getLsw(), lswPort.getPort());

        this.removeEpRegistrationFromFabric(tenantId, uln, lsw.getRenderedDeviceId(), epInfo.getRenderedDeviceId());

        uln.removeEpLocationFromCache(epLocation);
    }

    private void removeEpRegistrationFromFabric(Uuid tenantId, UserLogicalNetworkCache uln, NodeId lswDevId,
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid epUuid) {
        VcontainerServiceProviderAPI.unregisterEpFromLneLayer2(UlnUtil.convertToYangUuid(tenantId), lswDevId, epUuid);
    }

    private void doLogicalSwtichRemove(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        LogicalSwitchMappingInfo lswInfo = uln.findLswFromLswId(lsw.getUuid());
        if (lswInfo == null) {
            LOG.error("FABMGR: ERROR: doLogicalSwtichRemove: lsw not in cache: {}", lsw.getUuid().getValue());
            return;
        }

        if (lswInfo.hasServiceBeenRendered() == false) {
            LOG.error("FABMGR: ERROR: doLogicalSwtichRemove: lsw has not been rendered: {}", lsw.getUuid().getValue());
            return;
        }

        int numOfPorts = lswInfo.getPortListSize();
        if (numOfPorts > 0) {
            LOG.trace("FABMGR: doLogicalSwtichRemove: numOfPorts={}", numOfPorts);
            return;
        }

        int numOfRuleGroups = lswInfo.getSecurityRuleGroupsListSize();
        if (numOfRuleGroups > 0) {
            LOG.trace("FABMGR: doLogicalSwtichRemove: numOfRuleGroups={}", numOfRuleGroups);
            return;
        }

        int numOfLrLswEdges = lswInfo.getLrLswEdgeListSize();
        if (numOfLrLswEdges > 0) {
            LOG.trace("FABMGR: doLogicalSwtichRemove: numOfLrLswEdges={}", numOfLrLswEdges);
            return;
        }

        /*
         * All the dependencies of this LSW are deleted(There are may still be undeleted
         * logical ports on the LSW, but they can be removed together with
         * this LSW). So we can go ahead to remove this LSW.
         */
        this.removeLswFromFabric(tenantId, uln, lswInfo.getRenderedDeviceId());

        uln.removeLswFromCache(lsw);
    }

    private void removeLswFromFabric(Uuid tenantId, UserLogicalNetworkCache uln, NodeId lswDevId) {
        VcontainerServiceProviderAPI.removeLneLayer2(UlnUtil.convertToYangUuid(tenantId), lswDevId);
    }

    private void doLogicalRouterRemove(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouter lr) {
        LogicalRouterMappingInfo lrInfo = uln.findLrFromLrId(lr.getUuid());
        if (lrInfo == null) {
            LOG.error("FABMGR: ERROR: doLogicalRouterRemove: lr not in cache: {}", lr.getUuid().getValue());
            return;
        }

        if (lrInfo.hasServiceBeenRendered() == false) {
            LOG.error("FABMGR: ERROR: doLogicalRouterRemove: lr has not been rendered: {}", lr.getUuid().getValue());
            return;
        }

        int numOfRuleGroups = lrInfo.getSecurityRuleGroupsListSize();
        if (numOfRuleGroups > 0) {
            LOG.trace("FABMGR: doLogicalRouterRemove: numOfRuleGroups={}", numOfRuleGroups);
            return;
        }

        int numOfLrLswEdges = lrInfo.getLrLswEdgeListSize();
        if (numOfLrLswEdges > 0) {
            LOG.trace("FABMGR: doLogicalRouterRemove: numOfLrLswEdges={}", numOfLrLswEdges);
            return;
        }

        /*
         * All the dependencies of this LR are deleted(There are may still be undeleted
         * logical ports on the LR, but they can be removed together with
         * this LR). So we can go ahead to remove this LR.
         */
        this.removeLrFromFabric(tenantId, uln, lrInfo.getRenderedDeviceId());

        uln.removeLrFromCache(lr);

    }

    private void removeLrFromFabric(Uuid tenantId, UserLogicalNetworkCache uln, NodeId lrDevId) {
        VcontainerServiceProviderAPI.removeLneLayer3(UlnUtil.convertToYangUuid(tenantId), lrDevId);
    }

    private void renderSecurityRule(Uuid tenantId, UserLogicalNetworkCache uln, NodeId nodeId,
            SecurityRuleGroupsMappingInfo ruleGroupsMappingInfo, String aclName) {
        VcontainerServiceProviderAPI.createAcl(UlnUtil.convertToYangUuid(tenantId), nodeId, aclName);
        ruleGroupsMappingInfo.addRenderedAclName(aclName);
    }

    private String createAclFromSecurityRule(SecurityRule securityRule) {
        String aclName = securityRule.getName().getValue();

        /*
         * create Access List with entries and IID, then write transaction to data store
         */
        AccessListEntries aceList = this.createAceListFromSecurityRule(securityRule);
        AclBuilder aclBuilder = new AclBuilder();
        aclBuilder.setAclName(aclName).setKey(new AclKey(aclName)).setAccessListEntries(aceList);

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
        boolean foundSrcPort = false; // TODO: This is a kludge to work around yangtools mandatory
                                      // leaf bug
        boolean foundDestPort = false;
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
                foundDestPort = true;
            } else if (pvName.equals(PV_SOURCEPORT_TYPE_NAME)) {
                int portNum = (int) pv.getIntValue().longValue();
                SourcePortRangeBuilder sourcePortRangeBuilder = new SourcePortRangeBuilder();
                sourcePortRangeBuilder.setLowerPort(new PortNumber(portNum));
                sourcePortRangeBuilder.setUpperPort(new PortNumber(portNum));
                aceIpBuilder.setSourcePortRange(sourcePortRangeBuilder.build());
                foundSrcPort = true;
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

        if (foundDestPort == false) {
            String aceRuleName = classifier.getName().getValue();
            LOG.debug("FABMGR: ERROR: createAceFromSecurityRuleEntry: foundDestPort is false: {}", aceRuleName);
            DestinationPortRangeBuilder destinationPortRangeBuilder = new DestinationPortRangeBuilder();
            destinationPortRangeBuilder.setLowerPort(new PortNumber(0));
            destinationPortRangeBuilder.setUpperPort(new PortNumber(65535));
            aceIpBuilder.setDestinationPortRange(destinationPortRangeBuilder.build());
        }

        if (foundSrcPort == false) {
            String aceRuleName = classifier.getName().getValue();
            LOG.debug("FABMGR: ERROR: createAceFromSecurityRuleEntry: foundSrcPort is false: {}", aceRuleName);
            SourcePortRangeBuilder sourcePortRangeBuilder = new SourcePortRangeBuilder();
            sourcePortRangeBuilder.setLowerPort(new PortNumber(0));
            sourcePortRangeBuilder.setUpperPort(new PortNumber(65535));
            aceIpBuilder.setSourcePortRange(sourcePortRangeBuilder.build());
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
        if (ruleAction == null) {
            LOG.error("FABMGR: ERROR: createAceFromSecurityRuleEntry: ruleAction is null");
            return null;
        }
        List<ParameterValue> actions = ruleAction.getParameterValue();
        if (actions == null || actions.isEmpty() == true) {
            /*
             * The policy rules inherited from GBP may allow null P-V list
             * in actions. Although from real use case perspective, null P-V list
             * in actions may provide few practical values, this function decides
             * to handle the case more gracefully than disruptively -- if the function
             * aborts here and returns null, SFC provider will generate exceptions.
             */
            LOG.error("FABMGR: ERROR: createAceFromSecurityRuleEntry: actions is null or empty");
            PermitBuilder permitBuilder = new PermitBuilder();
            permitBuilder.setPermit(true);
            actionsBuilder.setPacketHandling(permitBuilder.build());
        } else {
            ParameterValue pv = actions.get(0);
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
                LOG.debug("FABMGR: createAceFromSecurityRuleEntry: ADD sfc chain: {}", sfcChainName);
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
        }

        // set matches and actions
        String aceRuleName = classifier.getName().getValue();
        AceBuilder aceBuilder = new AceBuilder();
        aceBuilder.setRuleName(aceRuleName);
        aceBuilder.setMatches(matchesBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        return aceBuilder.build();
    }

    public synchronized void handleSubnetUpdateEvent(Subnet subnet) {
        Uuid tenantId = subnet.getTenantId();
        if (this.isUlnAlreadyInCache(tenantId) == false) {
            LOG.error("FABMGR: ERROR: handleSubnetUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSubnetUpdateEvent: uln is null");
            return;
        }

        if (uln.isSubnetAlreadyCached(subnet) == false) {
            LOG.error("FABMGR: ERROR: handleSubnetUpdateEvent: subnet should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isSubnetRendered(subnet) == true) {
                LOG.error("FABMGR: ERROR: handleSubnetUpdateEvent: subnet has already been rendered: {}",
                        subnet.getUuid().getValue());
                return;
            } else {
                uln.removeSubnetFromCache(subnet);
            }
        }

        /*
         * Now we can treat update event as a new create event.
         */
        this.handleSubnetCreateEvent(subnet);
    }

    public synchronized void handleEdgeUpdateEvent(Edge edge) {
        Uuid tenantId = edge.getTenantId();
        if (this.isUlnAlreadyInCache(tenantId) == false) {
            LOG.error("FABMGR: ERROR: handleEdgeUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEdgeUpdateEvent: uln is null");
            return;
        }

        if (uln.isEdgeAlreadyCached(edge) == false) {
            LOG.error("FABMGR: ERROR: handleEdgeUpdateEvent: edge should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isEdgeRendered(edge) == true) {
                LOG.error("FABMGR: ERROR: handleEdgeUpdateEvent: edge has already been rendered: {}",
                        edge.getUuid().getValue());
                return;
            } else {
                uln.removeEdgeFromCache(edge);
            }
        }

        /*
         * Now we can treat update event as a new create event.
         */
        this.handleEdgeCreateEvent(edge);
    }

    public synchronized void handleEndpointLocationUpdateEvent(EndpointLocation epLocation) {
        Uuid tenantId = epLocation.getTenantId();
        if (this.isUlnAlreadyInCache(tenantId) == false) {
            LOG.error(
                    "FABMGR: ERROR: handleEndpointLocationUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEndpointLocationUpdateEvent: uln is null");
            return;
        }

        if (uln.isEpLocationAlreadyCached(epLocation) == false) {
            LOG.error("FABMGR: ERROR: handleEndpointLocationUpdateEvent: epLocation should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isEpLocationRendered(epLocation) == true) {
                LOG.error("FABMGR: ERROR: handleEndpointLocationUpdateEvent: epLocation has already been rendered: {}",
                        epLocation.getUuid().getValue());
                return;
            } else {
                uln.removeEpLocationFromCache(epLocation);
            }
        }

        /*
         * Now we can treat update event as a new create event.
         */
        this.handleEndpointLocationCreateEvent(epLocation);
    }

    public synchronized void handlePortUpdateEvent(Port port) {
        Uuid tenantId = port.getTenantId();
        if (this.isUlnAlreadyInCache(tenantId) == false) {
            LOG.error("FABMGR: ERROR: handlePortUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handlePortUpdateEvent: uln is null");
            return;
        }

        if (uln.isPortAlreadyCached(port) == false) {
            LOG.error("FABMGR: ERROR: handlePortUpdateEvent: port should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isPortRendered(port) == true) {
                LOG.error("FABMGR: ERROR: handlePortUpdateEvent: port has already been rendered: {}",
                        port.getUuid().getValue());
                return;
            } else {
                uln.removePortFromCache(port);
            }
        }

        /*
         * Now we can treat update event as a new create event.
         */
        this.handlePortCreateEvent(port);
    }

    public synchronized void handleLrUpdateEvent(LogicalRouter lr) {
        Uuid tenantId = lr.getTenantId();
        if (this.isUlnAlreadyInCache(tenantId) == false) {
            LOG.error("FABMGR: ERROR: handleLrUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLrUpdateEvent: uln is null");
            return;
        }

        if (uln.isLrAlreadyCached(lr) == false) {
            LOG.error("FABMGR: ERROR: handleLrUpdateEvent: lr should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isLrRendered(lr) == true) {
                LOG.error("FABMGR: ERROR: handleLrUpdateEvent: lr has already been rendered: {}",
                        lr.getUuid().getValue());
                return;
            } else {
                uln.removeLrFromCache(lr);
            }
        }

        /*
         * Now we can treat update event as a new create event.
         */
        this.handleLrCreateEvent(lr);
    }

    public synchronized void handleSecurityRuleGroupsUpdateEvent(SecurityRuleGroups ruleGroups) {
        Uuid tenantId = ruleGroups.getTenantId();
        if (this.isUlnAlreadyInCache(tenantId) == false) {
            LOG.error(
                    "FABMGR: ERROR: handleSecurityRuleGroupsUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSecurityRuleGroupsUpdateEvent: uln is null");
            return;
        }

        if (uln.isSecurityRuleGroupsAlreadyCached(ruleGroups) == false) {
            LOG.error("FABMGR: ERROR: handleSecurityRuleGroupsUpdateEvent: ruleGroups should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isSecurityRuleGroupsRendered(ruleGroups) == true) {
                LOG.error(
                        "FABMGR: ERROR: handleSecurityRuleGroupsUpdateEvent: ruleGroups has already been rendered: {}",
                        ruleGroups.getUuid().getValue());
                return;
            } else {
                uln.removeSecurityRuleGroupsFromCache(ruleGroups);
            }
        }

        /*
         * Now we can treat update event as a new create event.
         */
        this.handleSecurityRuleGroupsCreateEvent(ruleGroups);
    }

    public synchronized void handleLswUpdateEvent(LogicalSwitch lsw) {
        Uuid tenantId = lsw.getTenantId();
        if (this.isUlnAlreadyInCache(tenantId) == false) {
            LOG.error("FABMGR: ERROR: handleLswUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLswCreateEvent: uln is null");
            return;
        }

        if (uln.isLswAlreadyCached(lsw) == false) {
            LOG.error("FABMGR: ERROR: handleLswUpdateEvent: lsw should have been cached");
            // fall through. Treat this case as create event
        } else {
            /*
             * lsw is already in cache. This is expected, because
             * we expect a create event already took place before
             * this update event. If lsw is already rendered, then
             * we can no longer do any update, so we just return with error,
             * because this is a real update case which we do not handle
             * in this release. If lsw has not yet been rendered, then we
             * replace the one in cache with this updated lsw, and then
             * invoke render attempt.
             */
            if (uln.isLswRendered(lsw) == true) {
                LOG.error("FABMGR: ERROR: handleLswUpdateEvent: lsw has already been rendered: {}",
                        lsw.getUuid().getValue());
                return;
            } else {
                uln.removeLswFromCache(lsw);
            }
        }

        /*
         * Now we can treat update event as a new create event.
         */
        this.handleLswCreateEvent(lsw);
    }

    private synchronized boolean isUlnAlreadyInCache(Uuid tenantId) {
        if (this.ulnStore.get(tenantId) == null) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private synchronized void createUlnCache(Uuid tenantId) {
        if (this.isUlnAlreadyInCache(tenantId) == true) {
            LOG.warn("FABMGR: ERROR: createUlnCache: ULN already cached: {}", tenantId.getValue());
            this.ulnStore.remove(tenantId);
        }

        this.ulnStore.put(tenantId, new UserLogicalNetworkCache(tenantId));
    }

    private synchronized void createUlnCacheIfNotExist(Uuid tenantId) {
        if (this.ulnStore.get(tenantId) == null) {
            this.ulnStore.put(tenantId, new UserLogicalNetworkCache(tenantId));
        }
    }

    public void initialize() {
        this.exec.execute(new Runnable() {

            public void run() {
                try {
                    for (;;) {
                        workerThreadLock.acquire();
                        LOG.debug("FABMGR: run: acquired the lock; start to work");
                        /*
                         * We enter here implying that at lease one ULN
                         * event has taken place (event listener calls
                         * lock.release()). While we are calling checkUlnCache(),
                         * there might be more ULN events taking place. In that case,
                         * lock permits increases, so that we will be able to acquire
                         * the lock again in the next loop iteration.
                         */
                        int numOfJobs = workerThreadLock.availablePermits();
                        LOG.debug("FABMGR: calling checkUlnCache(), numOfJobs={}", numOfJobs);
                        inspectUlnCache();
                        boolean debug = true; // TODO: temp code; remove it
                        if (numOfJobs == 0 && debug) {
                            LOG.debug("FABMGR: run: dumpUlnTable: {}", dumpUlnTable());
                        }
                    }
                } catch (InterruptedException e) {
                    workerThreadLock.release();
                    LOG.error("FABMGR: ERROR: initialize:", e);
                }
            }
        });

    }

    private String dumpUlnTable() {
        StringBuilder sb = new StringBuilder();
        for (Entry<Uuid, UserLogicalNetworkCache> entry : this.ulnStore.entrySet()) {
            Uuid tenantId = entry.getKey();
            sb.append("\n***ULN: " + tenantId.getValue() + "\n");
            UserLogicalNetworkCache uln = entry.getValue();
            sb.append(uln.dumpUlnInstance());
        }

        return sb.toString();
    }

    public synchronized void handleLswRemoveEvent(LogicalSwitch lsw) {
        Uuid tenantId = lsw.getTenantId();
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLswRemoveEvent: uln is null");
            return;
        }

        if (uln.isLswAlreadyCached(lsw) == false) {
            LOG.error("FABMGR: ERROR: handleLswRemoveEvent: lsw not in cache");
            return;
        }

        uln.addRequestRemoveLsw(lsw);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    public synchronized void handleLrRemoveEvent(LogicalRouter lr) {
        Uuid tenantId = lr.getTenantId();
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLrRemoveEvent: uln is null");
            return;
        }

        if (uln.isLrAlreadyCached(lr) == false) {
            LOG.error("FABMGR: ERROR: handleLrRemoveEvent: lr not in cache");
            return;
        }

        uln.addRequestRemoveLr(lr);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    public synchronized void handleEdgeRemoveEvent(Edge edge) {
        Uuid tenantId = edge.getTenantId();
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEdgeRemoveEvent: uln is null");
            return;
        }

        if (uln.isEdgeAlreadyCached(edge) == false) {
            LOG.error("FABMGR: ERROR: handleEdgeRemoveEvent: edge not in cache");
            return;
        }

        uln.addRequestRemoveEdge(edge);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    public synchronized void handleEndpointLocationRemoveEvent(EndpointLocation epLocation) {
        Uuid tenantId = epLocation.getTenantId();
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEndpointLocationRemoveEvent: uln is null");
            return;
        }

        if (uln.isEpLocationAlreadyCached(epLocation) == false) {
            LOG.error("FABMGR: ERROR: handleEndpointLocationRemoveEvent: epLocation not in cache");
            return;
        }

        uln.addRequestRemoveEpLocation(epLocation);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    public synchronized void handlePortRemoveEvent(Port port) {
        Uuid tenantId = port.getTenantId();
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handlePortRemoveEvent: uln is null");
            return;
        }

        if (uln.isPortAlreadyCached(port) == false) {
            LOG.error("FABMGR: ERROR: handlePortRemoveEvent: port not in cache");
            return;
        }

        uln.addRequestRemovePort(port);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    public synchronized void handleSecurityRuleGroupsRemoveEvent(SecurityRuleGroups ruleGroups) {
        Uuid tenantId = ruleGroups.getTenantId();
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSecurityRuleGroupsRemoveEvent: uln is null");
            return;
        }

        if (uln.isSecurityRuleGroupsAlreadyCached(ruleGroups) == false) {
            LOG.error("FABMGR: ERROR: handleSecurityRuleGroupsRemoveEvent: ruleGroups not in cache");
            return;
        }

        uln.addRequestRemoveSecurityRuleGroups(ruleGroups);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    public synchronized void handleSubnetRemoveEvent(Subnet subnet) {
        Uuid tenantId = subnet.getTenantId();
        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSubnetRemoveEvent: uln is null");
            return;
        }

        if (uln.isSubnetAlreadyCached(subnet) == false) {
            LOG.error("FABMGR: ERROR: handleSubnetRemoveEvent: subnet not in cache");
            return;
        }

        uln.addRequestRemoveSubnet(subnet);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    private void doFindLswToLswPair(Uuid tenantId, UserLogicalNetworkCache uln, Edge lrToLrEdge) {
        if (uln.isEdgeLrToLrType(lrToLrEdge) == false) {
            return;
        }

        PortMappingInfo leftLrPort = uln.findLeftPortOnEdge(lrToLrEdge);
        if (leftLrPort == null) {
            return;
        }
        LogicalRouterMappingInfo leftLr = uln.findLrFromItsPort(leftLrPort.getPort());
        if (leftLr == null) {
            return;
        }
        EdgeMappingInfo leftLrLswEdge = uln.findLrLswEdge(leftLr);
        if (leftLrLswEdge == null) {
            return;
        }
        PortMappingInfo leftLswPort = uln.findLswPortOnEdge(leftLrLswEdge);
        if (leftLswPort == null) {
            return;
        }
        LogicalSwitchMappingInfo leftLsw = uln.findLswFromItsPort(leftLswPort.getPort());
        if (leftLsw == null) {
            return;
        }

        PortMappingInfo rightLrPort = uln.findRightPortOnEdge(lrToLrEdge);
        if (rightLrPort == null) {
            return;
        }
        LogicalRouterMappingInfo rightLr = uln.findLrFromItsPort(rightLrPort.getPort());
        if (rightLr == null) {
            return;
        }
        EdgeMappingInfo rightLrLswEdge = uln.findLrLswEdge(rightLr);
        if (rightLrLswEdge == null) {
            return;
        }
        PortMappingInfo rightLswPort = uln.findLswPortOnEdge(rightLrLswEdge);
        if (rightLswPort == null) {
            return;
        }
        LogicalSwitchMappingInfo rightLsw = uln.findLswFromItsPort(rightLswPort.getPort());
        if (rightLsw == null) {
            return;
        }

        Uuid leftLswId = leftLsw.getLsw().getUuid();
        Uuid rightLswId = rightLsw.getLsw().getUuid();
        this.lswLswPairStore.put(leftLswId, rightLswId);
        this.lswLswPairStore.put(rightLswId, leftLswId);
    }
}
