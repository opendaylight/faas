/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class PortMappingInfo {

    private Port port;
    private NodeId renderedDeviceId;
    private boolean serviceHasBeenRendered;

    public PortMappingInfo(Port port) {
        super();
        this.port = port;
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

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

}
