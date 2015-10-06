/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.List;

public class TrafficClassifier {

    private String name;
    private LogicalOperator op;
    private List<IFMatch> ifMatches;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LogicalOperator getOp() {
        return op;
    }

    public void setOp(LogicalOperator op) {
        this.op = op;
    }

    public List<IFMatch> getIfMatches() {
        return ifMatches;
    }

    public void setIfMatches(List<IFMatch> ifMatches) {
        this.ifMatches = ifMatches;
    }

    public TrafficClassifier(String name, LogicalOperator op, List<IFMatch> ifMatches) {
        super();
        this.name = name;
        this.op = op;
        this.ifMatches = ifMatches;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IFMatch match : ifMatches) {
            sb.append(match);
            sb.append(" " + op.toString());
        }

        return sb.toString();
    }

}
