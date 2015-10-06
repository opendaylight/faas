/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.List;

@Immutable
final public class VRF {

    final private String name;
    final private String networkElementID;
    final private String routeDistinguisher;
    final private List<RouteTarget> routeTargets;
    final private List<String> ifIDs;
    final AddressFamily family;

    public String getName() {
        return name;
    }

    public String getNetworkElementID() {
        return networkElementID;
    }

    public String getRouteDistinguisher() {
        return routeDistinguisher;
    }

    public List<RouteTarget> getRouteTargets() {
        return routeTargets;
    }

    public List<String> getIfs() {
        return ifIDs;
    }

    public VRF(String networkElementID, String name, String routeDistinguisher, AddressFamily family,
            List<RouteTarget> routeTargets, List<String> ifs) {
        super();
        this.name = name;
        this.networkElementID = networkElementID;
        this.routeDistinguisher = routeDistinguisher;
        this.routeTargets = routeTargets;
        this.ifIDs = ifs;
        this.family = family;
    }

    @Override
    public String toString() {
        return "VRF [name=" + name + ", networkElementID=" + networkElementID + ", routeDistinguisher="
                + routeDistinguisher + ", routeTargets=" + routeTargets + ", ifs=" + ifIDs + "]";
    }
}
