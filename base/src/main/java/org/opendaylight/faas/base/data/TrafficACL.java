/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.List;

public class TrafficACL {

    private String name;
    private Long aclNo;
    private List<String> rules;

    public Long getAclNo() {
        return aclNo;
    }

    public void setAclNo(Long aclNo) {
        this.aclNo = aclNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public TrafficACL(String name, List<String> rules) {
        super();
        this.name = name;
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "TrafficACL [name=" + name + ", rules=" + rules + "]";
    }

}
