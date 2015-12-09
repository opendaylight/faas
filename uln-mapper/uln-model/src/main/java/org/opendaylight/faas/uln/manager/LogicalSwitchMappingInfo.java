/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.logical.switches.rev151013.logical.switches.container.logical.switches.LogicalSwitch;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class LogicalSwitchMappingInfo {

    private LogicalSwitch lsw;
    private NodeId renderedDeviceId;
    private boolean serviceHasBeenRendered;

    public LogicalSwitchMappingInfo(LogicalSwitch lsw) {
        super();
        this.lsw = lsw;
        this.serviceHasBeenRendered = false;
    }

    public void markAsRendered(NodeId renderedLswId) {
        this.renderedDeviceId = renderedLswId;
        this.serviceHasBeenRendered = true;

    }

    public LogicalSwitch getLsw() {
        return lsw;
    }

    public void setLsw(LogicalSwitch lsw) {
        this.lsw = lsw;
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

}
