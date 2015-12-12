/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.ports.rev151013.ports.container.ports.Port;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

public class PortMappingInfo {

    private Port port;
    private TpId renderedDeviceId;
    private boolean serviceHasBeenRendered;

    public PortMappingInfo(Port port) {
        super();
        this.port = port;
        this.serviceHasBeenRendered = false;
    }

    public void markAsRendered(TpId renderedPortId) {
        this.renderedDeviceId = renderedPortId;
        this.serviceHasBeenRendered = true;

    }

    public TpId getRenderedDeviceId() {
        return renderedDeviceId;
    }

    public void setRenderedDeviceId(TpId renderedTpId) {
        this.renderedDeviceId = renderedTpId;
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
