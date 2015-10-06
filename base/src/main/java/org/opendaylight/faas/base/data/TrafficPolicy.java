/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficPolicy {

    final private String policyName;
    private List<TrafficACL> acls = new ArrayList<TrafficACL>();
    final private Map<TrafficClassifier, TrafficBehavior> tcs = new HashMap<TrafficClassifier, TrafficBehavior>();

    public String getPolicyName() {
        return policyName;
    }

    public void addACL(TrafficACL acl) {
        this.acls.add(acl);
    }

    public void addEntry(TrafficClassifier c, TrafficBehavior b) {
        this.tcs.put(c, b);
    }

    public Map<TrafficClassifier, TrafficBehavior> getTcs() {
        return tcs;
    }

    public List<TrafficACL> getACL() {
        return acls;
    }

    public TrafficPolicy(String policyName) {
        super();
        this.policyName = policyName;
    }

    @Override
    public String toString() {
        return "TrafficPolicy [policyName=" + policyName + ", acls=" + acls + ", tcs=" + tcs + "]";
    }
}
