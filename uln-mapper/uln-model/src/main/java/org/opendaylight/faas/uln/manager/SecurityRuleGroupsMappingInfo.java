/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.security.rules.rev151013.security.rule.groups.attributes.security.rule.groups.container.SecurityRuleGroups;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class SecurityRuleGroupsMappingInfo {

    private SecurityRuleGroups securityRuleGroups;
    private NodeId renderedDeviceId;
    private boolean serviceHasBeenRendered;

    public SecurityRuleGroupsMappingInfo(SecurityRuleGroups ruleGroups) {
        super();
        this.securityRuleGroups = ruleGroups;
        this.serviceHasBeenRendered = false;
    }

    public void markAsRendered(NodeId renderedLswId) {
        this.renderedDeviceId = renderedLswId;
        this.serviceHasBeenRendered = true;

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

    public SecurityRuleGroups getSecurityRuleGroups() {
        return securityRuleGroups;
    }

    public void setSecurityRuleGroups(SecurityRuleGroups securityRuleGroups) {
        this.securityRuleGroups = securityRuleGroups;
    }

}
