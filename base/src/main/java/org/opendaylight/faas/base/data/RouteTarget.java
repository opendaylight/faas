/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

@Immutable
final public class RouteTarget {

    final private String targetID;
    final private RouteTargetCommunity rtc;

    public RouteTarget(String targetID, RouteTargetCommunity rtc) {
        super();
        this.targetID = targetID;
        this.rtc = rtc;
    }

    public String getTargetID() {
        return targetID;
    }

    public RouteTargetCommunity getRtc() {
        return rtc;
    }

    @Override
    public String toString() {
        return "RouteTarget [targetID=" + targetID + ", rtc=" + rtc + "]";
    }

}
