/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.Map;

public class TrafficPolicyCfg {

    @Override
    public String toString() {
        return "TrafficPolicy [policyName=" + policyName + ", classifierBehavirors=" + classifierBehavirors + "]";
    }

    final private String policyName;
    final private Map<String, String> classifierBehavirors;

    public String getPolicyName() {
        return policyName;
    }

    public Map<String, String> getClassifierBehavirors() {
        return classifierBehavirors;
    }

    public TrafficPolicyCfg(String policyName, Map<String, String> classifierBehavirors) {
        super();
        this.policyName = policyName;
        this.classifierBehavirors = classifierBehavirors;
    }
}
