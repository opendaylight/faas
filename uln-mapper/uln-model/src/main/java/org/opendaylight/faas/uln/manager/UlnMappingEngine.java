/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.faas.fabricmgr.api.EndpointAttachInfo;
import org.opendaylight.faas.fabricmgr.api.VcontainerServiceProviderAPI;
import org.opendaylight.faas.uln.datastore.api.UlnDatastoreApi;
import org.opendaylight.faas.uln.datastore.api.UlnIidFactory;
import org.opendaylight.faas.uln.listeners.UlnUtil;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Name;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleClassifier.Direction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class UlnMappingEngine {

    private static final Logger LOG = LoggerFactory.getLogger(UlnMappingEngine.class);
    private static final String PV_SFC_TYPE_NAME = "sfc_chain_name";

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
        this.renderSubnet(tenantId, uln, subnet);
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
        this.renderPort(tenantId, uln, port);
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
        this.renderEpRegistration(tenantId, uln, epLocation);
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
        this.renderEdge(tenantId, uln, edge);
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
        this.renderSecurityRuleGroups(tenantId, uln, ruleGroups);
    }

    private void renderLogicalSwitch(Uuid tenantId, UserLogicalNetworkCache uln, LogicalSwitch lsw) {
        /*
         * For LSW, we can directly render it on Fabric.
         */
        CreateLneLayer2Input input = UlnUtil.createLneLayer2Input(lsw);
        NodeId renderedLswId = VcontainerServiceProviderAPI.createLneLayer2(UlnUtil.convertToYangUuid(tenantId), input);
        uln.markLswAsRendered(lsw, renderedLswId);

        /*
         * After we render LSW, we can render all the logical ports that
         * belong to this LSW.
         */
    }

    private void renderLogicalRouter(Uuid tenantId, UserLogicalNetworkCache uln, LogicalRouter lr) {
        /*
         * For LR, we can directly render it on Fabric.
         */
        CreateLneLayer3Input input = UlnUtil.createLneLayer3Input(lr);
        NodeId renderedLrId = VcontainerServiceProviderAPI.createLneLayer3(UlnUtil.convertToYangUuid(tenantId), input);
        uln.markLrAsRendered(lr, renderedLrId);

    }

    private void renderEpRegistration(Uuid tenantId, UserLogicalNetworkCache uln, EndpointLocation epLocation) {
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

        LogicalSwitchMappingInfo lsw = uln.findLswFromItsPort(lswPort);
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

        /*
         * Render LSW port is done by attachEpToLneLayer2()
         * if (lswPort.hasServiceBeenRendered() == false) {
         * this.renderPort(tenantId, uln, lswPort.getPort());
         * }
         */

        EndpointAttachInfo endpoint = UlnUtil.createEpAttachmentInput(epLocation, subnet.getSubnet(), epPort.getPort());
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid renderedEpId =
                VcontainerServiceProviderAPI.attachEpToLneLayer2(UlnUtil.convertToYangUuid(tenantId),
                        lsw.getRenderedDeviceId(), endpoint);
        uln.markEpLocationAsRendered(epLocation, renderedEpId);
        uln.markPortAsRendered(epPort.getPort());
        uln.markEdgeAsRendered(epEdge.getEdge());
        uln.markPortAsRendered(lswPort.getPort());
        uln.markPortAsRendered(subnetPort.getPort());
        uln.markPortAsRendered(subnetPort2.getPort());
        uln.markEdgeAsRendered(subnetLswEdge.getEdge());
        uln.markPortAsRendered(lswPort.getPort());
    }

    private void renderSubnet(Uuid tenantId, UserLogicalNetworkCache uln, Subnet subnet) {
        // TODO Auto-generated method stub

    }

    private void renderPort(Uuid tenantId, UserLogicalNetworkCache uln, Port port) {
        // TODO Auto-generated method stub

    }

    private void renderEdge(Uuid tenantId, UserLogicalNetworkCache uln, Edge edge) {
        // TODO Auto-generated method stub

    }

    private void renderSecurityRuleGroups(Uuid tenantId, UserLogicalNetworkCache uln, SecurityRuleGroups ruleGroups) {
        this.addNewRules(ruleGroups);
    }

    private void addNewRules(SecurityRuleGroups ruleGroups) {
        List<SecurityRuleGroup> securityRuleGroupList = ruleGroups.getSecurityRuleGroup();
        for (SecurityRuleGroup securityRuleGroup : securityRuleGroupList) {
            List<SecurityRule> securityRuleList = securityRuleGroup.getSecurityRule();
            for (SecurityRule securityRule : securityRuleList) {
                List<RuleAction> ruleActionList = securityRule.getRuleAction();
                for (RuleAction ruleAction : ruleActionList) {
                    List<ParameterValue> pvList = ruleAction.getParameterValue();
                    for (ParameterValue pv : pvList) {
                        Name pvName = pv.getName();
                        if (pvName.getValue().equals(PV_SFC_TYPE_NAME) == true) {
                            String sfcChainName = pv.getStringValue();
                            LOG.info("FABMGR: ADD sfc chain: {}", sfcChainName);
                            addSfcChain(sfcChainName, Direction.Bidirectional);
                        }
                    }
                }
            }
        }
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

    private void addSfcChain(String sfcChainName, Direction direction) {
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
            return;
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
                return;
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
                        return;
                    }
                } else {
                    renderedServicePath = rsp;
                }
            }
        } catch (Exception e) {
            LOG.error("ULN: ERROR: addSfcChain: Attemping to determine if srcEp was consumer.", e);
            return;
        }

        RenderedServicePathFirstHop rspFirstHop = SfcProviderRenderedPathAPI.readRenderedServicePathFirstHop(rspName);
        if (!isValidRspFirstHop(rspFirstHop)) {
            // Errors logged in method.
            return;
        }

        RenderedServicePathHop firstRspHop = renderedServicePath.getRenderedServicePathHop().get(0);
        RenderedServicePathHop lastRspHop = Iterables.getLast(renderedServicePath.getRenderedServicePathHop());
        Ipv4Address nshTunIpDst = rspFirstHop.getIp().getIpv4Address();
        PortNumber nshTunUdpPort = rspFirstHop.getPort();
        Short nshNsiToChain = firstRspHop.getServiceIndex();
        Long nshNspToChain = renderedServicePath.getPathId();
        int nshNsiFromChain = (short) lastRspHop.getServiceIndex().intValue() - 1;
        Long nshNspFromChain = renderedServicePath.getPathId();
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
}
