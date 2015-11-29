/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.listeners;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.uln.datastore.api.UlnDatastoreApi;
import org.opendaylight.faas.uln.datastore.api.UlnIidFactory;
import org.opendaylight.faas.uln.manager.UlnMapperDatastoreDependency;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.parameter.values.grouping.ParameterValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.SecurityRuleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.SecurityRule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.security.rule.groups.security.rule.group.security.rule.RuleClassifier.Direction;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class SecurityRuleGroupsListener implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityRuleGroupsListener.class);
    private static final String PV_SFC_TYPE_NAME = "sfc_chain_name";

    private final ListenerRegistration<DataChangeListener> registerListener;

    private final ScheduledExecutorService executor;

    public SecurityRuleGroupsListener(ScheduledExecutorService executor) {
        this.executor = executor;
        this.registerListener = UlnMapperDatastoreDependency.getDataProvider().registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, UlnIidFactory.securityGroupsIid(), this,
                AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                executeEvent(change);
            }
        });
    }

    public void executeEvent(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // Create
        for (DataObject dao : change.getCreatedData().values()) {
            if (dao instanceof SecurityRuleGroups) {
                LOG.debug("Created SecurityRuleGroups {}", dao);
                addNewRules((SecurityRuleGroups) dao);
            }
        }
        // Update
        Map<InstanceIdentifier<?>, DataObject> dao = change.getUpdatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dao.entrySet()) {
            if (entry.getValue() instanceof SecurityRuleGroups) {
                LOG.debug("Updated SecurityRuleGroups {}", dao);
                InstanceIdentifier<?> iid = entry.getKey();
                DataObject old = change.getOriginalData().get(iid);
                updateRules((SecurityRuleGroups) entry.getValue(), (SecurityRuleGroups) old);
            }
        }
        // Remove
        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject old = change.getOriginalData().get(iid);
            if (old == null) {
                continue;
            }
            if (old instanceof SecurityRuleGroups) {
                LOG.debug("Removed SecurityRuleGroups {}", old);
            }
        }
    }

    private void addNewRules(SecurityRuleGroups dao) {
        List<SecurityRuleGroup> securityRuleGroupList = dao.getSecurityRuleGroup();
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
                            LOG.debug("ULN: ADD sfc chain: {}", sfcChainName);
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
                LOG.info("ULN: ERROR: addSfcChain: Could not find RSP {} for Chain {}, created.", rspName, sfcChainName);
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
                    LOG.info("ULN: ERROR: addSfcChain: Could not find Reverse RSP {} for Chain {}", rspName, sfcChainName);
                    renderedServicePath = createSymmetricRsp(renderedServicePath);
                    if (renderedServicePath == null) {
                        LOG.error("ULN: ERROR: addSfcChain: Could not create RSP {} for Chain {}", rspName, sfcChainName);
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

    private void updateRules(SecurityRuleGroups newData, SecurityRuleGroups oldData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws Exception {
        if (registerListener != null)
            registerListener.close();
    }
}
