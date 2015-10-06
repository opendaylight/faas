/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

final public class TrafficBehavior {

    final private String bname;
    final private TrafficAction action;

    @Override
    public String toString() {
        return "TrafficBehavior [bname=" + bname + ", action=" + action + "]";
    }

    public String getBname() {
        return bname;
    }

    public TrafficAction getAction() {
        return action;
    }

    public TrafficBehavior(String bname, TrafficAction action) {
        super();
        this.bname = bname;
        this.action = action;
    }

}
