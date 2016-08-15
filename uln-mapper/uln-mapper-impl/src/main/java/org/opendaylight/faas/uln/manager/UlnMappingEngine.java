/*
 * Copyright (c) 2015, 2016 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabricmgr.FabMgrDatastoreUtil;
import org.opendaylight.faas.fabricmgr.FabMgrYangDataUtil;
import org.opendaylight.faas.fabricmgr.FabricMgrProvider;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.uln.cache.EdgeMappingInfo;
import org.opendaylight.faas.uln.cache.EndpointLocationMappingInfo;
import org.opendaylight.faas.uln.cache.LogicalEdgeType;
import org.opendaylight.faas.uln.cache.LogicalRouterMappingInfo;
import org.opendaylight.faas.uln.cache.LogicalSwitchMappingInfo;
import org.opendaylight.faas.uln.cache.PortMappingInfo;
import org.opendaylight.faas.uln.cache.RenderedRouter;
import org.opendaylight.faas.uln.cache.RenderedSwitch;
import org.opendaylight.faas.uln.cache.SecurityRuleGroupsMappingInfo;
import org.opendaylight.faas.uln.cache.SubnetMappingInfo;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.AccessLists;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.Ipv4Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.AclBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.AclKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.AccessListEntries;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.AccessListEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.Ace;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.AceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.MatchesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.actions.packet.handling.DenyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.actions.packet.handling.PermitBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.AceIpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.packet.fields.rev160218.acl.transport.header.fields.DestinationPortRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.packet.fields.rev160218.acl.transport.header.fields.SourcePortRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricPortAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.RedirectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.tunnel.tunnel.type.NshBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.edges.rev151013.edges.container.edges.Edge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.routers.rev151013.logical.routers.container.logical.routers.LogicalRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.port.PrivateIps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.parameter.values.grouping.ParameterValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.SecurityRuleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.SecurityRule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleClassifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleClassifier.Direction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UlnMappingEngine - map the logical network model into FaaS Services.
 *  - it handles the events from all kinds of listeners - lr, lsw, edge, sg, port and subnet
 *  - it is the core of the uln-mapper.
 *  - from south it is fed with users' logical network definition, it programs
 *  - the fabric oriented topology from FaaS. In Be release, single fabric is all its
 *  - target, now the single target assumption is gone. it faces north a topology consists
 *  - of many fabrics.
 *
 */
public class UlnMappingEngine {

    private static final Logger LOG = LoggerFactory.getLogger(UlnMappingEngine.class);

    private static final String PV_SFC_TYPE_NAME = "sfc-chain-name";
    private static final String PV_PERMIT_TYPE_NAME = "action-definition-id";
    private static final String PV_ACTION_VALUE_ALLOW = "Action-Allow";
    private static final String PV_ACTION_VALUE_DENY = "Action-Deny";
    private static final String PV_PROTO_TYPE_NAME = "proto";
    private static final String PV_DESTPORT_TYPE_NAME = "destport";
    private static final String PV_SOURCEPORT_TYPE_NAME = "sourceport";
    private final int EXT_ACCESS_TAG_START = 100;
    private final int EXT_ACCESS_TAG_RANGE = 100;

    private Map<Uuid, UserLogicalNetworkCache> ulnStore = null;
    private final Executor exec;
    private final Semaphore workerThreadLock;
    private boolean applyAclToBothEpgs = true; // see Bug 5191
    private FabricMgrProvider fmgr = null;

    /**
     * constructor - initialize the data members.
     * Initialize the worker thread and the cache object.
     */
    public UlnMappingEngine() {

        fmgr = FabricMgrProvider.getInstance();
        assert fmgr != null;

        this.ulnStore = fmgr.getCacheStore();
        assert ulnStore != null;

        this.exec = Executors.newSingleThreadExecutor();
        /*
         * Releases must occur before any acquires will be
         * granted. The worker thread is blocked initially.
         */
        this.workerThreadLock = new Semaphore(-1);
    }

    /**
     * LogicalSwitch creation event handler.
     * @param lsw - logical switch object.
     */
    public synchronized void handleLswCreateEvent(LogicalSwitch lsw) {
        Uuid tenantId = lsw.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLswCreateEvent: uln is null");
            return;
        }
        if (uln.isLswAlreadyCached(lsw)) {
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
        LOG.debug("Logical switch created for tenantID " + tenantId +
                " uln : " + uln.toString() + "lsw:" +  lsw.toString());
    }

    /**
     * Logical Router creation handler.
     * @param lr - the Logical Router object.
     */
    public synchronized void handleLrCreateEvent(LogicalRouter lr) {
        Uuid tenantId = lr.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleLrCreateEvent: uln is null");
            return;
        }
        if (uln.isLrAlreadyCached(lr)) {
            LOG.error("FABMGR: ERROR: handleLrCreateEvent: lr already exist");
            return;
        }

        uln.cacheLr(lr);

        this.workerThreadLock.release();
    }

    /**
     * Subnet object creation handler.
     * @param subnet - the subnet object.
     */
    public synchronized void handleSubnetCreateEvent(Subnet subnet) {
        Uuid tenantId = subnet.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSubnetCreateEvent: uln is null");
            return;
        }
        if (uln.isSubnetAlreadyCached(subnet)) {
            LOG.error("FABMGR: ERROR: handleSubnetCreateEvent: subnet already exist");
            return;
        }

        uln.cacheSubnet(subnet);

        this.workerThreadLock.release();
    }

    private void doSubnetCreate(UserLogicalNetworkCache uln, Subnet subnet) {
        /*
         * For subnet, we do not need to render it, but due to Bug 5144,
         * we need to add device ID of the rendered LSW to all other
         * unrendered LSWs that are connected to the given subnet.
         */
        uln.updateLSWsConnectedToSubnet(subnet);
    }

    /**
     * Logical Port Creation handler.
     * @param port - logical Port object to be created.
     */
    public synchronized void handlePortCreateEvent(Port port) {
        Uuid tenantId = port.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handlePortCreateEvent: uln is null");
            return;
        }
        if (uln.isPortAlreadyCached(port)) {
            LOG.error("FABMGR: ERROR: handlePortCreateEvent: port already exist");
            return;
        }

        uln.cachePort(port);

        this.workerThreadLock.release();
    }

    /**
     * End point binding creation handler.
     * @param epLocation - the binding between logical port and physical location.
     */
    public synchronized void handleEndpointLocationCreateEvent(EndpointLocation epLocation) {
        Uuid tenantId = epLocation.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEndpointCreateEvent: uln is null");
            return;
        }
        if (uln.isEpLocationAlreadyCached(epLocation)) {
            LOG.error("FABMGR: ERROR: handleEndpointLocationCreateEvent: epLocation already exist");
            return;
        }

        uln.cacheEpLocation(epLocation);

        this.workerThreadLock.release();
    }

    public NodeId getFabricIdFromLocation(
            org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId nid,
            org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId cid) {
        Optional<TerminationPoint> opt = FabMgrDatastoreUtil.readData(LogicalDatastoreType.OPERATIONAL,
                FabMgrYangDataUtil.createTpPath(nid.getValue(), cid.getValue()));
        if (opt.isPresent()) {
            TerminationPoint tp = opt.get();
            FabricPortAug aug = tp.getAugmentation(FabricPortAug.class);
            if (aug != null && aug.getPortRef() != null && aug.getPortRef().getValue().firstKeyOf(Node.class) != null) {
                LOG.error("Invalid FabricPortAugumentation!");
                return aug.getPortRef().getValue().firstKeyOf(Node.class).getNodeId();
            }
        }

        return null;
    }


    private void doEndpointLocationCreate(
            Uuid tenantId, UserLogicalNetworkCache uln, EndpointLocation epLocation) {
        /*
         * When an end point is online, we call Fabric's registerEndpoint(). However, before
         * we do that, we need to make sure that LogicalSwitch, Logical Port and Logical router are created
         * and rendered on Fabric. Not only that, we must also have to have the Subnet information
         * ready.
         */

        PortMappingInfo epPort = uln.findEpPortFromEpLocation(epLocation);
        if (epPort == null) {
            LOG.debug("FABMGR: renderEpRegistration: epPort not in cache, the logical port in the location is not valid!");
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

        //Find the fabric location of the end point belongs.
        NodeId fabricId = getFabricIdFromLocation(
                epLocation.getNodeId(), epLocation.getNodeConnectorId());
        if (fabricId == null) {
            LOG.error("Failed to locate a vaid fabric for eplocation {} {}" ,
                    epLocation.getNodeId(), epLocation.getNodeConnectorId());
            return ;
        } else {
            LOG.info("Finding EndPoint on logical Port UUID " +  epLocation.getPort() +
                    " attaching nodeID " +     epLocation.getNodeId().getValue() +
                    " connectionID " + epLocation.getNodeConnectorId().getValue() +
                    " is registered against Fabric " +         fabricId);
        }

        /*
         * If we get here, then we have received all the
         * information that we need in order to do
         * EP registration. The steps are:
         * 1. create LSW
         * 2. create logical port on LSW
         * 3. register EP
         * 4. render the logical router if required
         *
         */

        //L2 rendering
        if (!lsw.hasServiceBeenRenderedOnFabric(fabricId)) {
            NodeId renderedSwitchId = this.renderLogicalSwitch(tenantId, fabricId, uln, lsw.getLsw());
            if (renderedSwitchId == null) {
                LOG.debug("renderLogicalSwitch failed! check fabric {} is allocated to tenant {}", fabricId, tenantId);
                return;
            }
        }

        if (!lswPort.hasServiceBeenRendered()) {
            this.renderPortOnLsw(tenantId, fabricId, uln, lsw, lswPort);
            uln.addPortToLsw(lsw.getLsw(), lswPort.getPort());
        }

        //Register end point
        EndpointAttachInfo endpoint = UlnUtil.createEpAttachmentInput(epLocation, subnet.getSubnet(), epPort.getPort());

        this.renderEpRegistration(tenantId, fabricId, uln, epLocation, lsw.getRenderedSwitchOnFabric(fabricId).getSwitchID(), lswPort.getRenderedLogicalPortId(),
                endpoint);
        uln.setLswIdOnEpLocation(epLocation, lsw.getLsw().getUuid());
        uln.setLswPortIdOnEpLocation(epLocation, lswPort.getPort().getUuid());
        uln.markEdgeAsRendered(epEdge.getEdge());
        uln.markPortAsRendered(subnetPort.getPort());
        uln.markPortAsRendered(subnetPort2.getPort());
        uln.markEdgeAsRendered(subnetLswEdge.getEdge());
        uln.markSubnetAsRendered(subnet.getSubnet()); // subnet being rendered meaning its LSW has
                                                      // been chosen and rendered.

        // L3 Rendering - optional for an EP, that is why we put after the
        // EP rendering state is changed. but we still have the opportunity to render it
        // since the Lr has its own rendering state.
        EdgeMappingInfo lrLswEdge = uln.findLswLrEdge(lsw);
        if (lrLswEdge != null) {
            PortMappingInfo lrPort = uln.findLrPortOnEdge(lrLswEdge);
            if (lrPort == null) {
                LOG.debug("FABMGR: renderEpRegistration: lr Port for the Edge not in cache");
                return;
            }

            LogicalRouterMappingInfo lr = uln.findLrFromItsPort(lrPort.getPort());
            if (lr == null) {
                LOG.debug("FABMGR: renderEpRegistration: lr not in cache");
                return;
            }

            //Should we render its external gateway.
            this.renderExternalGW(tenantId, uln, lr);

            if (lr.hasServiceBeenRenderedOnFabric(fabricId)) {
                this.renderLogicalRouter(tenantId, fabricId, uln, lr);
                fmgr.connectAllDVRs(UlnUtil.convertToYangUuid(tenantId), uln, lr.getRenderedRouters());
            }

            if (lr.getRenderedRouterOnFabric(fabricId).getGateways().get(lsw) == null){
               lr.getRenderedRouterOnFabric(fabricId).addGateway(
                       lsw.getRenderedSwitchOnFabric(fabricId).getSwitchID(),
                       this.fmgr.createLrLswGateway(
                               UlnUtil.convertToYangUuid(tenantId),
                               fabricId,
                               lr.getRenderedDeviceIdOnFabric(fabricId),
                               lsw.getRenderedSwitchOnFabric(fabricId).getSwitchID(),
                               subnet.getSubnet().getVirtualRouterIp(),
                               subnet.getSubnet().getIpPrefix())
                       );
            }

            fmgr.updateRoutes(
                    UlnUtil.convertToYangUuid(tenantId),
                    uln,
                    lr.getRenderedRouters());

            //Set up ACLs between all subnets and logical switches within
            //the logical routing domain.
            this.setACLforLogicalRouter(tenantId, uln, lr);

            //Set IP Mapping //TODO
            List<IpAddress> pips = epPort.getPort().getPublicIps();
            List<PrivateIps> priips = epPort.getPort().getPrivateIps();
            if(! pips.isEmpty() && !priips.isEmpty()) {
                for (Map.Entry<NodeId, RenderedRouter> entry : uln.getExtGateways().entrySet()) {
                    this.fmgr.setIPMapping(UlnUtil.convertToYangUuid(tenantId), entry.getKey(),
                            entry.getValue().getExtSwitch(), entry.getValue().getAccessTP(), pips.get(0),
                            priips.get(0).getIpAddress());
                    return; //only support the first pair.
                }
            }
        }

    }

    public synchronized void handleEdgeCreateEvent(Edge edge) {
        Uuid tenantId = edge.getTenantId();

        this.createUlnCacheIfNotExist(tenantId);

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleEdgeCreateEvent: uln is null");
            return;
        }
        if (uln.isEdgeAlreadyCached(edge)) {
            LOG.error("FABMGR: ERROR: handleEdgeCreateEvent: edge already exist");
            return;
        }

        uln.cacheEdge(edge);

        this.workerThreadLock.release();
    }

    private void doLR2LREdge(Uuid tenantId, UserLogicalNetworkCache uln, Edge edge) {
        //Find two routers
        List<Subnet> subnets = new ArrayList<>();
        List<LogicalSwitchMappingInfo> llsws = new ArrayList<>();
        List<LogicalSwitchMappingInfo> rlsws = new ArrayList<>();

        PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
        PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);

        LogicalRouterMappingInfo leftLr = uln.findLrFromItsPort(leftPort.getPort());
        LogicalRouterMappingInfo rightLr = uln.findLrFromItsPort(rightPort.getPort());

        //set up connection
        Map<NodeId,RenderedRouter> combinedMap = new HashMap<>();
        combinedMap.putAll(leftLr.getRenderedRouters());
        combinedMap.putAll(rightLr.getRenderedRouters());
        fmgr.connectAllDVRs(UlnUtil.convertToYangUuid(tenantId), uln, combinedMap);

        //ACL
        List<EdgeMappingInfo> edges = uln.findLrLswEdge(leftLr);
        for (EdgeMappingInfo edge2 : edges) {
            PortMappingInfo port = uln.findLswPortOnEdge(edge2);
            LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(port.getPort());
            llsws.add(lsw);
            SubnetMappingInfo subnet = uln.findSubnetFromLsw(lsw);
            subnets.add(subnet.getSubnet());
        }

        List<EdgeMappingInfo> edges2 = uln.findLrLswEdge(rightLr);
        for (EdgeMappingInfo edge2 : edges2) {
            PortMappingInfo port = uln.findLswPortOnEdge(edge2);
            LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(port.getPort());
            rlsws.add(lsw);
            SubnetMappingInfo subnet = uln.findSubnetFromLsw(lsw);
            subnets.add(subnet.getSubnet());
        }

        String aclName = this.createAclForGroupComm(subnets, leftLr.getLr().getName() + "-" + leftLr.getLr().getName());
        uln.getEdgeStore().get(edge.getUuid()).setGroupACLName(aclName);


        List<NodeId> fabrics = uln.findAllFabricsOfRenderedLswsFromLr(leftLr);
        for (NodeId fabricId : fabrics) {
            for (LogicalSwitchMappingInfo lsw : llsws) {
                RenderedSwitch rsw = lsw.getRenderedSwitchOnFabric(fabricId);
                fmgr.createAcl(UlnUtil.convertToYangUuid(tenantId), fabricId, rsw.getSwitchID(), aclName);
            }
        }

        List<NodeId> fabrics2 = uln.findAllFabricsOfRenderedLswsFromLr(rightLr);
        for (NodeId fabricId : fabrics2) {
            for (LogicalSwitchMappingInfo lsw : rlsws) {
                RenderedSwitch rsw = lsw.getRenderedSwitchOnFabric(fabricId);
                fmgr.createAcl(UlnUtil.convertToYangUuid(tenantId), fabricId, rsw.getSwitchID(), aclName);
            }
        }

        //routing table
        fmgr.updateRoutes(UlnUtil.convertToYangUuid(tenantId), uln, combinedMap);
    }

    final class RenderableInfo {
        private final boolean canRenderEdge;
        private final LogicalSwitchMappingInfo lsw ;
        private final LogicalRouterMappingInfo lr;
        private final SubnetMappingInfo subnet ;

        public RenderableInfo(boolean canRenderEdge, LogicalSwitchMappingInfo lsw, LogicalRouterMappingInfo lr,
                SubnetMappingInfo subnet) {
            super();
            this.canRenderEdge = canRenderEdge;
            this.lsw = lsw;
            this.lr = lr;
            this.subnet = subnet;
        }

        public boolean isCanRenderEdge() {
            return canRenderEdge;
        }
        public LogicalSwitchMappingInfo getLsw() {
            return lsw;
        }
        public LogicalRouterMappingInfo getLr() {
            return lr;
        }
        public SubnetMappingInfo getSubnet() {
            return subnet;
        }
    }

    private void cacheEdgeForItsPort(Edge edge, UserLogicalNetworkCache uln)
    {
        PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
        PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);
        uln.addLrLswEdgeToPort(leftPort.getPort(), edge);
        uln.addLrLswEdgeToPort(rightPort.getPort(), edge);
    }

    private PortMappingInfo getLSWEnd(Edge edge, UserLogicalNetworkCache uln)
    {
        PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
        PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);
        if (uln.isPortLswType(leftPort.getPort().getUuid())) {
            return leftPort;
        }

        if (uln.isPortLswType(rightPort.getPort().getUuid())) {
            return rightPort;
        }

        return null;
    }

    private PortMappingInfo getLREnd(Edge edge, UserLogicalNetworkCache uln)
    {
        PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
        PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);
        if (uln.isPortLrType(leftPort.getPort().getUuid())) {
            return leftPort;
        }

        if (uln.isPortLrType(rightPort.getPort().getUuid())) {
            return rightPort;
        }

        return null;
    }


    private RenderableInfo getRenderableInfo (Edge edge, UserLogicalNetworkCache uln)
    {
        PortMappingInfo lswPort = getLSWEnd(edge, uln);
        PortMappingInfo lrPort = getLREnd(edge, uln);

        if (lswPort == null || lrPort == null)
        {
            LOG.error("One of the Edge's port is not valid for LSW-LR Edge rendering");
            return new RenderableInfo(false, null, null, null);
        }

        LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(lswPort.getPort());
        if (lsw != null) {
            SubnetMappingInfo subnet = uln.findSubnetFromLsw(lsw);
            LogicalRouterMappingInfo lr = uln.findLrFromItsPort(lrPort.getPort());
            if (lr != null && subnet != null && lsw.hasServiceBeenRendered()) {
            /*
             * If lsw is already rendered, then that means
             * we can safely add the gateway. If lsw is not
             * rendered, we do not know if that lsw should be
             * created or not (due to bug 5144). So we need to wait
             * for EpRegistration to render the correct lsw first.
             */
                return new RenderableInfo(true, lsw, lr, subnet);
            }
        }

        return new RenderableInfo(false, null, null, null);
    }

    private void doEdgeCreate(Uuid tenantId, UserLogicalNetworkCache uln, Edge edge) {

        //first cache the edge data into its ports.
        cacheEdgeForItsPort(edge, uln);

        /*
         * On a single fabric, only LR-to-LSW type needs to be mapped to
         * Fabric's createGateway() API. (Other types are applicable only to
         * multi-fabric.)
         */

        if (uln.isEdgeLrToLrType(edge)) {
            doLR2LREdge(tenantId, uln, edge);
            return;
        }

        if (uln.isEdgeLrToLswType(edge)) {
            LOG.trace("FABMGR: doEdgeCreate: found LrToLsw edge: {}", edge.getUuid().getValue());
            RenderableInfo info = getRenderableInfo(edge, uln);

            if (!info.isCanRenderEdge()) {
                return ;
            }

            Map<NodeId, RenderedSwitch> renderedLSWs = info.getLsw().getRenderedSwitches();

            /*
             * One ULN can have only one rendered LR (Bug 5146). If LR is
             * already rendered, then use it. If not, then render this LR as the
             * first and only LR to render.
             */
            for (Map.Entry<NodeId, RenderedSwitch> entry : renderedLSWs.entrySet()) {
                if (!info.getLr().hasServiceBeenRenderedOnFabric(entry.getKey())) {
                    this.renderLogicalRouter(tenantId, entry.getKey(), uln, info.getLr());
                }
            }
            fmgr.connectAllDVRs(UlnUtil.convertToYangUuid(tenantId), uln, info.getLr().getRenderedRouters());


            Map<NodeId, RenderedRouter> renderedRouters = info.getLr().getRenderedRouters();

            IpAddress gatewayIp = info.getSubnet().getSubnet().getVirtualRouterIp();
            if (gatewayIp == null) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: gatewayIp is null. edgeId={}, subnetId={}",
                        edge.getUuid().getValue(), info.getSubnet().getSubnet().getUuid().getValue());
                return;
            }

            IpPrefix ipPrefix = info.getSubnet().getSubnet().getIpPrefix();
            if (ipPrefix == null) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: ipPrefix is null. edgeId={}, subnetId={}",
                        edge.getUuid().getValue(), info.getSubnet().getSubnet().getUuid().getValue());
                return;
            }

            for (Map.Entry<NodeId, RenderedRouter> entry : renderedRouters.entrySet()) {
                LOG.debug("FABMGR: doEdgeCreate: edgeId={}, lrDevId={}, lswDevId={}, gateway={}, ipPrefix={}",
                        edge.getUuid().getValue(),
                        entry.getValue().getRouterID(),
                        info.getLsw().getRenderedSwitchOnFabric(entry.getKey()).getSwitchID().getValue(),
                        gatewayIp.getIpv4Address().getValue(), ipPrefix.getIpv4Prefix().getValue()
                        );

                info.getLr().getRenderedRouterOnFabric(entry.getKey()).addGateway(
                        info.getLsw().getRenderedSwitchOnFabric(entry.getKey()).getSwitchID(),
                        this.renderLrLswEdge(
                                tenantId,
                                entry.getKey(),
                                uln,
                                entry.getValue().getRouterID(),
                                info.getLsw().getRenderedSwitchOnFabric(entry.getKey()).getSwitchID(),
                                gatewayIp,
                                ipPrefix,
                                edge)
                        );
            }

            fmgr.updateRoutes(UlnUtil.convertToYangUuid(tenantId), uln, info.getLr().getRenderedRouters());

            /*
             * One subnet can have only one gateway. Mark this subnet as
             * rendered so that we know no more gateways can be rendered for
             * this subnet. There is a bug (Bug 5144) in ULN model that permits
             * subnet to have multiple gateways.
             */
            if (!info.getSubnet().hasServiceBeenRendered()) {
                LOG.error("FABMGR: ERROR: doEdgeCreate: subnet is not rendered. subnetId={}",
                        info.getSubnet().getSubnet().getUuid().getValue());
                uln.markSubnetAsRendered(info.getSubnet().getSubnet());
            }

            /*
             * The following calls are for debug info collection purpose
             */
            uln.addLrLswEdgeToLsw(info.getLsw().getLsw(), edge);
            uln.addLrLswEdgeToLr(info.getLr().getLr(), edge);
            uln.addGatewayIpToLr(info.getLr().getLr(), gatewayIp);
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
        Map <NodeId, List<NodeId>> flsws = new HashMap<>();
        if (lsw != null) { // port on a logical switch.
            if (!lsw.hasServiceBeenRendered()) {
                LOG.debug("FABMGR: doSecurityRuleGroupsCreate: lsw not rendered: {}",
                        lsw.getLsw().getUuid().getValue());
            } else {
                Map<NodeId, RenderedSwitch> renderedSwitches = lsw.getRenderedSwitches();
                for (Map.Entry<NodeId, RenderedSwitch> entry : renderedSwitches.entrySet()) {
                    this.renderSecurityRuleGroups(tenantId, entry.getKey(), uln, entry.getValue().getSwitchID(), ruleGroups);
                }
            }
            uln.addSecurityRuleGroupsToLsw(lsw.getLsw(), ruleGroups);
        } else { // port on a logical router
            //
            //Currently we don't support apply acl rules on logical router
            // we will see if we should remove this limitation in future releases.
            //
            LogicalRouterMappingInfo lr = uln.findLrFromPortId(portId);
            if (lr != null) {

                EdgeMappingInfo theEdge = uln.findTheEdge(uln.getPortStore().get(portId));
                if (uln.findEdgeType(theEdge) == LogicalEdgeType.LR_LSW) {
                    LOG.error("The policy should be applied to the switch side, not the router side.");
                    return;
                }

                if (uln.findEdgeType(theEdge) == LogicalEdgeType.LR_LR) {
                    LOG.info("The policy is between two routers.");
                    /*
                     * Due to Bug 5146, we cannot apply ACL on LR. We need to
                     * find the LSW which connects to this LR and apply ACL
                     * rules there.
                     *
                     * Due to Bug 5191, we have to apply ACL to both EPGs. So,
                     * we cannot render ACL until both LSWs are rendered.
                     */
                    for (Map.Entry<NodeId, RenderedRouter> entry : lr.getRenderedRouters().entrySet()) {
                        flsws.put(entry.getKey(), uln.findLswRenderedDeviceIdFromLr(lr, entry.getKey()));
                    }
                    uln.addSecurityRuleGroupsToLr(lr.getLr(), ruleGroups);
                    for (Map.Entry<NodeId, List<NodeId>> entry : flsws.entrySet()) {
                        for (NodeId swid : entry.getValue()) {
                            this.renderSecurityRuleGroups(tenantId, entry.getKey(), uln, swid, ruleGroups);
                        }
                    }

                    if (this.applyAclToBothEpgs) {
                        flsws.clear();
                        PortMappingInfo left = uln.findLeftPortOnEdge(theEdge.getEdge());
                        uln.findRightPortOnEdge(theEdge.getEdge());
                        LogicalRouterMappingInfo theOtherLr = uln.findLrFromPortId(left.getPort().getUuid());
                        for (Map.Entry<NodeId, RenderedRouter> entry : theOtherLr.getRenderedRouters().entrySet()) {
                            flsws.put(entry.getKey(), uln.findLswRenderedDeviceIdFromLr(theOtherLr, entry.getKey()));
                        }
                        uln.addSecurityRuleGroupsToLr(theOtherLr.getLr(), ruleGroups);
                        for (Map.Entry<NodeId, List<NodeId>> entry : flsws.entrySet()) {
                            for (NodeId swid : entry.getValue()) {
                                this.renderSecurityRuleGroups(tenantId, entry.getKey(), uln, swid, ruleGroups);
                            }
                        }
                    }
                }
            }
        }

    }


    private NodeId renderLogicalSwitch(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        return fmgr.createLneLayer2(UlnUtil.convertToYangUuid(tenantId),
                fabricId, UlnUtil.convertToYangUuid(lsw.getUuid()), uln);
    }

    private IpAddress getPubIP(LogicalRouter lr, UserLogicalNetworkCache uln)
    {
        List<IpAddress> pubips = new ArrayList<>();
        new ArrayList<>();
        for (Uuid p : lr.getPort())
        {
            PortMappingInfo pi = uln.getPortStore().get(p);
            pubips.addAll(pi.getPort().getPublicIps());
        }

        //TODO - for now Only provide the first available Public IP address
        if (pubips.isEmpty()) {
            return null;
        }

        return pubips.get(0);
    }

   private void renderExternalGW (
       Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouterMappingInfo lr ) {
       if (lr.getLr().isPublic() &&
               !uln.isHasExternalGateway()) {

           // TODO
           // the prefix and tag should all come from admin provisioning,
           // so does the tag. we hard code it for now.
           Random rand = new Random();
           int accessTag = rand.nextInt(EXT_ACCESS_TAG_RANGE) + EXT_ACCESS_TAG_START;
           IpAddress pubip = this.getPubIP(lr.getLr(), uln);

           this.fmgr.setupExternalGW(
                   UlnUtil.convertToYangUuid(tenantId),
                   uln,
                   lr,
                   pubip,
                   new IpPrefix(new Ipv4Prefix(pubip.getIpv4Address().getValue() + "/24")),
                   accessTag);

           uln.setHasExternalGateway(true);
       }
   }

    private void setACLforLogicalRouter(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouterMappingInfo lr) {
        List<Subnet> subnets = new ArrayList<>();
        List<LogicalSwitchMappingInfo> lsws = new ArrayList<>();

        List<EdgeMappingInfo> edges = uln.findLrLswEdge(lr);
        for (EdgeMappingInfo edge : edges) {
            PortMappingInfo port = uln.findLswPortOnEdge(edge);
            LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(port.getPort());
            lsws.add(lsw);
            SubnetMappingInfo subnet = uln.findSubnetFromLsw(lsw);
            subnets.add(subnet.getSubnet());
        }
        String aclName = this.createAclForGroupComm(subnets,lr.getLr().getName().getValue());
        lr.setGroupAclName(aclName);

        List<NodeId> fabrics = uln.findAllFabricsOfRenderedLswsFromLr(lr);
        for (NodeId fId : fabrics) {
            for (LogicalSwitchMappingInfo lsw : lsws) {
                RenderedSwitch rsw = lsw.getRenderedSwitchOnFabric(fId);
                fmgr.createAcl(UlnUtil.convertToYangUuid(tenantId), fId, rsw.getSwitchID(), aclName);
            }
        }
    }

    private NodeId renderLogicalRouter(
            Uuid tenantId, NodeId fabricId,
            UserLogicalNetworkCache uln, LogicalRouterMappingInfo lr) {

        NodeId rr ;
        if ((rr = uln.getRenderedRouterOnFabirc(fabricId)) == null) {
            rr =  fmgr.createLneLayer3(UlnUtil.convertToYangUuid(tenantId), fabricId,uln);
            uln.addRenderedRouterOnFabric(fabricId, rr);
        }

        RenderedRouter renderedLr = new RenderedRouter(fabricId, rr);
        uln.getLrStore().get(lr).addRenderedRouter(renderedLr);
        return rr;
    }

    private void renderEpRegistration(Uuid tenantId, NodeId fId, UserLogicalNetworkCache uln, EndpointLocation epLocation,
            NodeId renderedLswId, TpId renderedLswPortId, EndpointAttachInfo endpoint) {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid renderedEpId =
                fmgr.attachEpToLneLayer2(UlnUtil.convertToYangUuid(tenantId), fId, renderedLswId,
                        renderedLswPortId, endpoint);
        uln.markEpLocationAsRendered(epLocation, renderedEpId);
    }

    private void renderPortOnLsw(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, LogicalSwitchMappingInfo lswInfo,
            PortMappingInfo lswPortInfo) {
        if (!lswInfo.hasServiceBeenRendered()) {
            LOG.error("FABMGR: ERROR: renderPortOnLsw: lsw not rendered yet");
            return;
        }

        NodeId renderedLswId = lswInfo.getRenderedSwitchOnFabric(fabricId).getSwitchID();
        TpId renderedPortId = fmgr
            .createLogicalPortOnLneLayer2(UlnUtil.convertToYangUuid(tenantId), fabricId, renderedLswId);
        lswPortInfo.markAsRendered(renderedPortId);
    }

    private org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid renderLrLswEdge(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, NodeId lrId, NodeId lswId,
            IpAddress gatewayIpAddr, IpPrefix ipPrefix, Edge edge) {
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid id = fmgr.createLrLswGateway(UlnUtil.convertToYangUuid(tenantId), fabricId, lrId, lswId, gatewayIpAddr,
                ipPrefix);

        uln.markEdgeAsRendered(edge);

        return id;
    }

    private void renderSecurityRuleGroups(Uuid tenantId, NodeId fabricId,UserLogicalNetworkCache uln, NodeId nodeId,
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
                this.renderSecurityRule(tenantId, fabricId, uln, nodeId, ruleGroupsMappingInfo, aclName);
            }
        }

        uln.markSecurityRuleGroupsAsRendered(ruleGroups);
    }

    private void renderSecurityRuleGroupsOnPair(Uuid tenantId, UserLogicalNetworkCache uln, NodeId fabricId, NodeId nodeId,
            NodeId fabricId2, NodeId nodeId2, SecurityRuleGroups ruleGroups) {
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
                this.renderSecurityRule(tenantId, fabricId, uln, nodeId, ruleGroupsMappingInfo, aclName);
                this.renderSecurityRule(tenantId, fabricId2, uln, nodeId2, ruleGroupsMappingInfo, aclName);
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
            LogicalRouterMappingInfo lrInfo = lrEntry.getValue();
            if (lrInfo.isToBeDeleted()) {
                if (lrInfo.hasServiceBeenRendered()) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doLogicalRouterRemove: {}",
                            lrInfo.getLr().getUuid().getValue());
                    this.doLogicalRouterRemove(tenantId, uln, lrInfo.getLr());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeLrFromCache: {}",
                            lrInfo.getLr().getUuid().getValue());
                    uln.removeLrFromCache(lrInfo.getLr());
                }
            } else if (!lrInfo.hasServiceBeenRendered()) {
                LOG.debug("FABMGR: processPendingUlnRequests: doLogicalRouterCreate: {}",
                        lrInfo.getLr().getUuid().getValue());
                List<NodeId> rlrs = uln.findAllFabricsOfRenderedLswsFromLr(lrInfo);
                for (NodeId fabricId : rlrs) {
                    this.renderLogicalRouter(tenantId, fabricId, uln, lrInfo);
                }
                fmgr.connectAllDVRs(UlnUtil.convertToYangUuid(tenantId), uln, lrInfo.getRenderedRouters());
                fmgr.updateRoutes(UlnUtil.convertToYangUuid(tenantId), uln, lrInfo.getRenderedRouters());
            }
        }

        /*
         * LSW addition dependency: None
         * LSW deletion dependency: ACL, Logical Ports, Gateway
         */
        for (Entry<Uuid, LogicalSwitchMappingInfo> lswEntry : uln.getLswStore().entrySet()) {
            LogicalSwitchMappingInfo info = lswEntry.getValue();
            if (info.isToBeDeleted()) {
                if (info.hasServiceBeenRendered()) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doLogicalSwitchRemove: {}",
                            info.getLsw().getUuid().getValue());
                    this.doLogicalSwitchRemove(tenantId, uln, info.getLsw());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeLswFromCache: {}",
                            info.getLsw().getUuid().getValue());
                    uln.removeLswFromCache(info.getLsw());
                }
            } else if (!info.hasServiceBeenRendered()) {
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
            if (info.isToBeDeleted()) {
                if (info.hasServiceBeenRendered()) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doEndpointLocationRemove: {}",
                            info.getEpLocation().getUuid().getValue());
                    this.doEndpointLocationRemove(tenantId, uln, info.getEpLocation());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeEpLocationFromCache: {}",
                            info.getEpLocation().getUuid().getValue());
                    uln.removeEpLocationFromCache(info.getEpLocation());
                }
            } else if (!info.hasServiceBeenRendered()) {
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
            if (info.isToBeDeleted()) {
                if (info.hasServiceBeenRendered()) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doEdgeRemove: {}",
                            info.getEdge().getUuid().getValue());
                    this.doEdgeRemove(tenantId, uln, info.getEdge());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeEdgeFromCache: {}",
                            info.getEdge().getUuid().getValue());
                    uln.removeEdgeFromCache(info.getEdge());
                }
            } else if (!info.hasServiceBeenRendered()) {
                LOG.debug("FABMGR: processPendingUlnRequests: doEdgeCreate: {}", info.getEdge().getUuid().getValue());
                this.doEdgeCreate(tenantId, uln, info.getEdge());
            }
        }

        /*
         * ACL addition dependency: LSW or LR
         * ACL deletion dependency: None
         */
        for (Entry<Uuid, SecurityRuleGroupsMappingInfo> rulesEntry : uln.getSecurityRuleGroupsStore().entrySet()) {
            if (rulesEntry.getValue().isToBeDeleted()) {
                if (rulesEntry.getValue().hasServiceBeenRendered()) {
                    LOG.debug("FABMGR: processPendingUlnRequests: doSecurityRuleGroupsRemove: {}",
                            rulesEntry.getValue().getSecurityRuleGroups().getUuid().getValue());
                    this.doSecurityRuleGroupsRemove(tenantId, uln, rulesEntry.getValue().getSecurityRuleGroups());
                } else {
                    LOG.debug("FABMGR: processPendingUlnRequests: removeSecurityRuleGroupsFromCache: {}",
                            rulesEntry.getValue().getSecurityRuleGroups().getUuid().getValue());
                    uln.removeSecurityRuleGroupsFromCache(rulesEntry.getValue().getSecurityRuleGroups());
                }
            } else if (!rulesEntry.getValue().hasServiceBeenRendered()) {
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
            if (subnetEntry.getValue().isToBeDeleted()) {
                LOG.debug("FABMGR: processPendingUlnRequests: delete subnet: {}",
                        subnetEntry.getValue().getSubnet().getUuid().getValue());
                uln.removeSubnetFromCache(subnetEntry.getValue().getSubnet());
            } else {
                LOG.debug("FABMGR: processPendingUlnRequests: create subnet: {}",
                        subnetEntry.getValue().getSubnet().getUuid().getValue());
                this.doSubnetCreate(uln, subnetEntry.getValue().getSubnet());
            }
        }

        /*
         * Port addition dependency: ports are not mapped to Fabric; no need to render
         * Port deletion dependency: Gateway
         */
        for (Entry<Uuid, PortMappingInfo> portEntry : uln.getPortStore().entrySet()) {
            PortMappingInfo info = portEntry.getValue();
            if (info.isToBeDeleted()) {
                if (info.isLrLswEdgeListEmpty()) {
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
            if (!lsw.hasServiceBeenRendered()) {
                LOG.error("FABMGR: ERROR: doSecurityRuleGroupsRemove: lsw not rendered: {}",
                        lsw.getLsw().getUuid().getValue());
                return;
            }

            for (Map.Entry<NodeId, RenderedSwitch> entry : lsw.getRenderedSwitches().entrySet()) {
                /*
                 * We are now ready to remove ACL. After ACL is removed, The
                 * LSW can also be removed
                 */
                LOG.debug("FABMGR: doSecurityRuleGroupsRemove: calling removeSecurityRuleGroups...");
                this.removeSecurityRuleGroupsFromFabric(tenantId, entry.getKey(), uln, entry.getValue().getSwitchID(), ruleGroups);
                uln.removeSecurityRuleGroupsFromLsw(lsw.getLsw(), ruleGroups);
                uln.removeSecurityRuleGroupsFromCache(ruleGroups);
            }
        } else {
            LogicalRouterMappingInfo lr = uln.findLrFromPortId(portId);
            if (lr != null) {
                if (!lr.hasServiceBeenRendered()) {
                    LOG.error("FABMGR: ERROR: doSecurityRuleGroupsRemove: lr not rendered: {}",
                            lr.getLr().getUuid().getValue());
                    return;
                }

                Map<NodeId, RenderedRouter> rlrs  = lr.getRenderedRouters();
                for (Map.Entry<NodeId, RenderedRouter> entry : rlrs.entrySet()) {
                    /*
                     * We are now ready to remove ACL. After ACL is removed, The
                     * LSW can also be removed
                     */
                    for (NodeId nodeId : uln.findLswRenderedDeviceIdFromLr(lr, entry.getKey())) {
                        LOG.debug("FABMGR: doSecurityRuleGroupsRemove: calling removeSecurityRuleGroups");
                        this.removeSecurityRuleGroupsFromFabric(tenantId, entry.getKey(), uln, nodeId, ruleGroups);
                        uln.removeSecurityRuleGroupsFromLr(lr.getLr(), ruleGroups);
                        uln.removeSecurityRuleGroupsFromCache(ruleGroups);
                    }
                }
            }
        }
    }

    private void removeSecurityRuleGroupsFromFabric(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, NodeId nodeId,
            SecurityRuleGroups ruleGroups) {
        SecurityRuleGroupsMappingInfo ruleGroupsInfo = uln.findSecurityRuleGroupsFromRuleGroupsId(ruleGroups.getUuid());
        if (ruleGroupsInfo == null) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromFabric: ruleGroups not in cache: {}",
                    ruleGroups.getUuid().getValue());
            return;
        }

        if (!ruleGroupsInfo.hasServiceBeenRendered()) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromFabric: securityRuleGroups has not been rendered: {}",
                    ruleGroups.getUuid().getValue());
            return;
        }

        /*
         * One SecurityRuleGroups may be mapped to multiple ACL rules.
         * So we need to delete ACL in a loop.
         */
        List<String> aclNameList = ruleGroupsInfo.getRenderedAclNameList();
        if (aclNameList == null || aclNameList.isEmpty()) {
            LOG.error("FABMGR: ERROR: removeSecurityRuleGroupsFromFabric: alcNameList is null or empty: {}",
                    ruleGroups.getUuid().getValue());
            return;
        }
        for (String aclName : aclNameList) {
            this.removeAclFromFabric(tenantId, fabricId, uln, nodeId, aclName);
        }
    }

    private void removeAclFromFabric(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, NodeId nodeId, String aclName) {
        fmgr.removeAcl(UlnUtil.convertToYangUuid(tenantId), fabricId, nodeId, aclName);

        this.removeAclFromDatastore(aclName);
    }

    private void removeAclFromDatastore(String aclName) {
        InstanceIdentifier<Acl> aclPath =
                InstanceIdentifier.builder(AccessLists.class).child(Acl.class, new AclKey(aclName, Ipv4Acl.class)).build();

        boolean transactionSuccessful =
                SfcDataStoreAPI.deleteTransactionAPI(aclPath, LogicalDatastoreType.CONFIGURATION);
        if (!transactionSuccessful) {
            LOG.error("FABMGR: ERROR: removeAclFromDatastore: deleteTransactionAPI failed: {}", aclName);
        }
    }

    void removeACLforInterLrComm(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouterMappingInfo lr, String aclname) {
        for (Map.Entry<NodeId, RenderedRouter> entry : lr.getRenderedRouters().entrySet()) {
            for (NodeId rsw :uln.findLswRenderedDeviceIdFromLr(lr, entry.getKey())) {
                fmgr.removeAcl(UlnUtil.convertToYangUuid(tenantId), entry.getKey(), rsw, aclname);
            }
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
        if (!uln.isEdgeLrToLswType(edge)) {
            if (uln.isEdgeLrToLrType(edge)) {
                PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
                PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);
                LogicalRouterMappingInfo rlr = uln.findLrFromItsPort(rightPort.getPort());
                LogicalRouterMappingInfo llr = uln.findLrFromItsPort(rightPort.getPort());

                this.removeACLforInterLrComm(tenantId, uln, rlr, uln.getEdgeStore().get(edge.getUuid()).getGroupACLName());
                this.removeACLforInterLrComm(tenantId, uln, llr, uln.getEdgeStore().get(edge.getUuid()).getGroupACLName());

                uln.removeEdgeFromCache(edge);

            }
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

        LogicalSwitchMappingInfo lsw ;
        LogicalRouterMappingInfo lr;
        PortMappingInfo leftPort = uln.findLeftPortOnEdge(edge);
        PortMappingInfo rightPort = uln.findRightPortOnEdge(edge);
        if (leftPort != null && rightPort != null) {
            if (uln.isPortLswType(leftPort.getPort().getUuid())) {
                lsw = uln.findLswFromItsPort(leftPort.getPort());
                if (lsw != null && uln.isPortLrType(rightPort.getPort().getUuid()) == true) {
                    uln.removeLrLswEdgeFromLsw(lsw.getLsw(), edge);
                    lr = uln.findLrFromItsPort(rightPort.getPort());
                    if (lr != null) {
                        uln.removeLrLswEdgeFromLr(lr.getLr(), edge);
                        for (Map.Entry<NodeId, RenderedSwitch> entry : lsw.getRenderedSwitches().entrySet()) {
                            fmgr.removeAcl(
                                    UlnUtil.convertToYangUuid(tenantId),
                                    entry.getKey(),
                                    entry.getValue().getSwitchID(),
                                    lr.getGroupAclName());
                        }

                    } else {
                        LOG.error("FABMGR: ERROR: doEdgeRemove: lr is null: {}", edge.getUuid().getValue());
                        return;
                    }
                } else {
                    LOG.error("FABMGR: ERROR: doEdgeRemove: lsw is null: {}", edge.getUuid().getValue());
                    return;
                }
            } else if (uln.isPortLswType(rightPort.getPort().getUuid())) {
                lsw = uln.findLswFromItsPort(rightPort.getPort());
                if (lsw != null && uln.isPortLrType(leftPort.getPort().getUuid()) == true) {
                    uln.removeLrLswEdgeFromLsw(lsw.getLsw(), edge);
                    lr = uln.findLrFromItsPort(leftPort.getPort());
                    if (lr != null) {
                        Map<NodeId, RenderedRouter> lrDevIds = lr.getRenderedRouters();
                        for (Map.Entry<NodeId, RenderedRouter> entry : lrDevIds.entrySet()) {
                            IpAddress gatewayIp = lr.getGatewayIpAddr();
                            if (gatewayIp == null) {
                                LOG.error("FABMGR: doEdgeCreate: gatewayIp is null. edgeId={}, lrId={}",
                                        edge.getUuid().getValue(), lr.getLr().getUuid().getValue());
                                return;
                            }

                            LOG.debug("FABMGR: doEdgeRemove: edgeId={}, lrDevId={}, gateway={}",
                                    edge.getUuid().getValue(), entry.getValue().getRouterID().getValue(),
                                    gatewayIp.getIpv4Address().getValue());
                            this.removeLrLswEdgeFromFabric(tenantId, entry.getKey(),uln, entry.getValue().getRouterID(), gatewayIp);
                            uln.removeEdgeFromCache(edge);
                        }

                        uln.removeLrLswEdgeFromLr(lr.getLr(), edge);
                        for (Map.Entry<NodeId, RenderedSwitch> entry : lsw.getRenderedSwitches().entrySet()) {
                            fmgr.removeAcl(
                                    UlnUtil.convertToYangUuid(tenantId),
                                    entry.getKey(),
                                    entry.getValue().getSwitchID(),
                                    lr.getGroupAclName());
                        }

                    } else {
                        LOG.error("FABMGR: ERROR: doEdgeRemove: lr is null: {}", edge.getUuid().getValue());
                        return;
                    }
                } else {
                    LOG.error("FABMGR: ERROR: doEdgeRemove: lsw is null: {}", edge.getUuid().getValue());
                    return;
                }
            }
            uln.removeLrLswEdgeFromPort(leftPort.getPort(), edge);
            uln.removeLrLswEdgeFromPort(rightPort.getPort(), edge);
        } else {
            LOG.error("FABMGR: ERROR: doEdgeRemove: at lease on port is null: {}", edge.getUuid().getValue());
        }
    }

    private void removeLrLswEdgeFromFabric(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, NodeId lrDevId,
            IpAddress gatewayIp) {
        fmgr.removeLrLswGateway(UlnUtil.convertToYangUuid(tenantId), fabricId, lrDevId, gatewayIp);
    }

    private void doEndpointLocationRemove(Uuid tenantId, UserLogicalNetworkCache uln, EndpointLocation epLocation) {
        EndpointLocationMappingInfo epInfo = uln.findEpLocationFromEpLocationId(epLocation.getUuid());
        if (epInfo == null) {
            LOG.error("FABMGR: ERROR: doEndpointLocationRemove: epLocation not in cache: {}",
                    epLocation.getUuid().getValue());
            return;
        }

        if (!epInfo.hasServiceBeenRendered()) {
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

        this.removeEpRegistrationFromFabric(tenantId, new NodeId(epInfo.getEpLocation().getNodeId().getValue()), uln, lsw.getRenderedSwitchOnFabric(new NodeId(epInfo.getEpLocation().getNodeId().getValue())).getSwitchID(), epInfo.getRenderedDeviceId());

        uln.removeEpLocationFromCache(epLocation);
    }

    private void removeEpRegistrationFromFabric(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, NodeId lswDevId,
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid epUuid) {
        fmgr.unregisterEpFromLneLayer2(UlnUtil.convertToYangUuid(tenantId), fabricId, lswDevId, epUuid);
    }

    private void doLogicalSwitchRemove(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        LogicalSwitchMappingInfo lswInfo = uln.findLswFromLswId(lsw.getUuid());
        if (lswInfo == null) {
            LOG.error("FABMGR: ERROR: doLogicalSwtichRemove: lsw not in cache: {}", lsw.getUuid().getValue());
            return;
        }

        if (!lswInfo.hasServiceBeenRendered()) {
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
        for (Map.Entry<NodeId, RenderedSwitch> entry: lswInfo.getRenderedSwitches().entrySet()) {
            this.removeLswFromFabric(tenantId, entry.getKey(), uln, entry.getValue().getSwitchID());
        }

        uln.removeLswFromCache(lsw);
    }

    private void removeLswFromFabric(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, NodeId lswDevId) {
        fmgr.removeLneLayer2(UlnUtil.convertToYangUuid(tenantId), fabricId, lswDevId);
    }

    private void doLogicalRouterRemove(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouter lr) {
        LogicalRouterMappingInfo lrInfo = uln.findLrFromLrId(lr.getUuid());
        if (lrInfo == null) {
            LOG.error("FABMGR: ERROR: doLogicalRouterRemove: lr not in cache: {}", lr.getUuid().getValue());
            return;
        }

        if (!lrInfo.hasServiceBeenRendered()) {
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
        for (Map.Entry<NodeId, RenderedRouter> entry : lrInfo.getRenderedRouters().entrySet()) {
            this.removeLrFromFabric(tenantId, entry.getKey(),uln, entry.getValue().getRouterID());
        }

        uln.removeLrFromCache(lr);

    }

    private void removeLrFromFabric(Uuid tenantId, NodeId fabricId, UserLogicalNetworkCache uln, NodeId lrDevId) {
        fmgr.removeLneLayer3(UlnUtil.convertToYangUuid(tenantId), fabricId, lrDevId);
    }

    private void renderSecurityRule(Uuid tenantId, NodeId fabricId,UserLogicalNetworkCache uln, NodeId nodeId,
            SecurityRuleGroupsMappingInfo ruleGroupsMappingInfo, String aclName) {
        fmgr.createAcl(UlnUtil.convertToYangUuid(tenantId), fabricId,  nodeId, aclName);
        ruleGroupsMappingInfo.addRenderedAclName(aclName);
    }

    private String createAclFromSecurityRule(SecurityRule securityRule) {
        String aclName = securityRule.getName().getValue();

        /*
         * create Access List with entries and IID, then write transaction to data store
         */
        AccessListEntries aceList = this.createAceListFromSecurityRule(securityRule);
        AclBuilder aclBuilder = new AclBuilder();
        aclBuilder.setAclName(aclName).setKey(new AclKey(aclName, Ipv4Acl.class)).setAccessListEntries(aceList);

        InstanceIdentifier<Acl> aclPath =
                InstanceIdentifier.builder(AccessLists.class).child(Acl.class, new AclKey(aclName, Ipv4Acl.class)).build();

        boolean transactionSuccessful =
                SfcDataStoreAPI.writePutTransactionAPI(aclPath, aclBuilder.build(), LogicalDatastoreType.CONFIGURATION);
        if (!transactionSuccessful) {
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

    private String createAclForGroupComm(List<Subnet> subnets, String groupName) {
        String aclName = "lracl" + groupName; //TODO, this is not right :(
        /*
         * create Access List with entries and IID, then write transaction to data store
         */
        AccessListEntries aceList = this.createAceListFromIPv4PrefixPairs(subnets);
        AclBuilder aclBuilder = new AclBuilder();
        aclBuilder.setAclName(aclName).setKey(new AclKey(aclName, Ipv4Acl.class)).setAccessListEntries(aceList);

        InstanceIdentifier<Acl> aclPath =
                InstanceIdentifier.builder(AccessLists.class).child(Acl.class, new AclKey(aclName, Ipv4Acl.class)).build();

        boolean transactionSuccessful =
                SfcDataStoreAPI.writePutTransactionAPI(aclPath, aclBuilder.build(), LogicalDatastoreType.CONFIGURATION);
        if (!transactionSuccessful) {
            LOG.error("FABMGR: ERROR: createAclFromSecurityRule:writePutTransactionAPI failed");
        }

        return aclName;
    }


    private AccessListEntries createAceListFromIPv4PrefixPairs(List<Subnet> subnets) {
        List<Ace> aceList = new ArrayList<>();
        for (Subnet entrys: subnets) {
            for (Subnet entryd: subnets) {
            Ace ace = this.createAceForGroupComm(entrys.getIpPrefix().getIpv4Prefix(), entryd.getIpPrefix().getIpv4Prefix());
            aceList.add(ace);
        }
        }

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();
        accessListEntriesBuilder.setAce(aceList);

        return accessListEntriesBuilder.build();
    }


    //Only IPv4 supported
    private Ace createAceForGroupComm(Ipv4Prefix source, Ipv4Prefix dest)
    {
        AceIpBuilder aceIpBuilder = new AceIpBuilder();
        aceIpBuilder.setDscp(new Dscp((short) 1)); // TODO: Do we have to setup DSCP?

        AceIpv4Builder aceIpv4Builder = new AceIpv4Builder();
        aceIpv4Builder.setSourceIpv4Network(source);
        aceIpv4Builder.setDestinationIpv4Network(dest);
        aceIpBuilder.setAceIpVersion(aceIpv4Builder.build()).setProtocol((short) 4);

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
        PermitBuilder permitBuilder = new PermitBuilder();
        permitBuilder.setPermit(true);
        actionsBuilder.setPacketHandling(permitBuilder.build());

        // set matches and actions
        String aceRuleName = "GroupAllow";
        AceBuilder aceBuilder = new AceBuilder();
        aceBuilder.setRuleName(aceRuleName);
        aceBuilder.setMatches(matchesBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        return aceBuilder.build();
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

        if (!foundDestPort) {
            String aceRuleName = classifier.getName().getValue();
            LOG.debug("FABMGR: ERROR: createAceFromSecurityRuleEntry: foundDestPort is false: {}", aceRuleName);
            DestinationPortRangeBuilder destinationPortRangeBuilder = new DestinationPortRangeBuilder();
            destinationPortRangeBuilder.setLowerPort(new PortNumber(0));
            destinationPortRangeBuilder.setUpperPort(new PortNumber(65535));
            aceIpBuilder.setDestinationPortRange(destinationPortRangeBuilder.build());
        }

        if (!foundSrcPort) {
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
        if (actions == null || actions.isEmpty()) {
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
                if (actionValue.equals(PV_ACTION_VALUE_ALLOW)) {
                    PermitBuilder permitBuilder = new PermitBuilder();
                    permitBuilder.setPermit(true);
                    actionsBuilder.setPacketHandling(permitBuilder.build());
                } else if (actionValue.equals(PV_ACTION_VALUE_DENY)) {
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
        if (!this.isUlnAlreadyInCache(tenantId)) {
            LOG.error("FABMGR: ERROR: handleSubnetUpdateEvent: this is update; ULN is supposed to be in cache: {}",
                    tenantId.getValue());
            return;
        }

        UserLogicalNetworkCache uln = this.ulnStore.get(tenantId);
        if (uln == null) {
            LOG.error("FABMGR: ERROR: handleSubnetUpdateEvent: uln is null");
            return;
        }

        if (!uln.isSubnetAlreadyCached(subnet)) {
            LOG.error("FABMGR: ERROR: handleSubnetUpdateEvent: subnet should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isSubnetRendered(subnet)) {
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

        if (!uln.isEdgeAlreadyCached(edge)) {
            LOG.error("FABMGR: ERROR: handleEdgeUpdateEvent: edge should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isEdgeRendered(edge)) {
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
        if (!this.isUlnAlreadyInCache(tenantId)) {
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

        if (!uln.isEpLocationAlreadyCached(epLocation)) {
            LOG.error("FABMGR: ERROR: handleEndpointLocationUpdateEvent: epLocation should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isEpLocationRendered(epLocation)) {
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

        if (!uln.isPortAlreadyCached(port)) {
            LOG.error("FABMGR: ERROR: handlePortUpdateEvent: port should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isPortRendered(port)) {
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

        if (!uln.isLrAlreadyCached(lr)) {
            LOG.error("FABMGR: ERROR: handleLrUpdateEvent: lr should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.getLrStore().get(lr.getUuid()).hasServiceBeenRendered()) {
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

        if (!uln.isSecurityRuleGroupsAlreadyCached(ruleGroups)) {
            LOG.error("FABMGR: ERROR: handleSecurityRuleGroupsUpdateEvent: ruleGroups should have been cached");
            // fall through. Treat this case as create event
        } else {
            if (uln.isSecurityRuleGroupsRendered(ruleGroups)) {
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
            if (uln.getLswStore().get(lsw.getUuid()).hasServiceBeenRendered()) {
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

    private synchronized void createUlnCacheIfNotExist(Uuid tenantId) {
        if (this.ulnStore.get(tenantId) == null) {
            this.ulnStore.put(tenantId, new UserLogicalNetworkCache(tenantId));
        }
    }

    public void initialize() {
        this.exec.execute(() -> {
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

        if (!uln.isSecurityRuleGroupsAlreadyCached(ruleGroups)) {
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

        if (!uln.isSubnetAlreadyCached(subnet)) {
            LOG.error("FABMGR: ERROR: handleSubnetRemoveEvent: subnet not in cache");
            return;
        }

        uln.addRequestRemoveSubnet(subnet);

        /*
         * Notify worker thread to start work
         */
        this.workerThreadLock.release();
    }

    private Map<Uuid, Uuid> doFindLswToLswPair(Uuid tenantId, UserLogicalNetworkCache uln, Edge lrToLrEdge) {
        if (!uln.isEdgeLrToLrType(lrToLrEdge)) {
            return null;
        }

        // Find the left side lsw associated with left lr.
        PortMappingInfo leftLrPort = uln.findLeftPortOnEdge(lrToLrEdge);
        if (leftLrPort == null) {
            return null;
        }
        LogicalRouterMappingInfo leftLr = uln.findLrFromItsPort(leftLrPort.getPort());
        if (leftLr == null) {
            return null;
        }
        List<EdgeMappingInfo> leftLrLswEdges = uln.findLrLswEdge(leftLr);
        if (leftLrLswEdges.isEmpty()) {
            return null;
        }

        List<LogicalSwitchMappingInfo> leftLsws = new ArrayList<>();
        for (EdgeMappingInfo leftLrLswEdge : leftLrLswEdges) {
            PortMappingInfo leftLswPort = uln.findLswPortOnEdge(leftLrLswEdge);
            if (leftLswPort == null) {
                continue;
            }
            LogicalSwitchMappingInfo leftLsw = uln.findLswFromItsPort(leftLswPort.getPort());
            if (leftLsw == null) {
                continue;
            }
            leftLsws.add(leftLsw);
        }

        // Find the right side lsw associated with right lr.
        PortMappingInfo rightLrPort = uln.findRightPortOnEdge(lrToLrEdge);
        if (rightLrPort == null) {
            return null;
        }
        LogicalRouterMappingInfo rightLr = uln.findLrFromItsPort(rightLrPort.getPort());
        if (rightLr == null) {
            return null;
        }
        List<EdgeMappingInfo> rightLrLswEdges = uln.findLrLswEdge(rightLr);
        if (rightLrLswEdges.isEmpty()) {
            return null;
        }

        List<LogicalSwitchMappingInfo> rightLsws = new ArrayList<>();
        for (EdgeMappingInfo rightLrLswEdge : rightLrLswEdges) {
            PortMappingInfo rightLswPort = uln.findLswPortOnEdge(rightLrLswEdge);
            if (rightLswPort == null) {
                continue;
            }
            LogicalSwitchMappingInfo rightLsw = uln.findLswFromItsPort(rightLswPort.getPort());
            if (rightLsw == null) {
                continue;
            }

            rightLsws.add(rightLsw);
        }

        Map<Uuid, Uuid> pairs = new HashMap<>();
        for (LogicalSwitchMappingInfo leftLsw : leftLsws) {
            for (LogicalSwitchMappingInfo rightLsw : rightLsws) {
                Uuid leftLswId = leftLsw.getLsw().getUuid();
                Uuid rightLswId = rightLsw.getLsw().getUuid();
                pairs.put(leftLswId, rightLswId);
                pairs.put(rightLswId, leftLswId);
            }
        }

        return pairs;
    }
}

