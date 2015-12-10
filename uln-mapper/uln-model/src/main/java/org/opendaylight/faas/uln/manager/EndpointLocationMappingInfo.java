/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.endpoints.locations.container.endpoints.locations.EndpointLocation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class EndpointLocationMappingInfo {

    private EndpointLocation epLocation;
    private Uuid renderedDeviceId;
    private boolean serviceHasBeenRendered;

    public EndpointLocationMappingInfo(EndpointLocation epLocation) {
        super();
        this.epLocation = epLocation;
        this.serviceHasBeenRendered = false;
    }

    public void markAsRendered(Uuid renderedEpId) {
        this.renderedDeviceId = renderedEpId;
        this.serviceHasBeenRendered = true;

    }

    public Uuid getRenderedDeviceId() {
        return renderedDeviceId;
    }

    public void setRenderedDeviceId(Uuid renderedEpId) {
        this.renderedDeviceId = renderedEpId;
    }

    public boolean hasServiceBeenRendered() {
        return serviceHasBeenRendered;
    }

    public void setServiceHasBeenRendered(boolean serviceHasBeenRendered) {
        this.serviceHasBeenRendered = serviceHasBeenRendered;
    }

    public EndpointLocation getEpLocation() {
        return epLocation;
    }

    public void setEpLocation(EndpointLocation epLocation) {
        this.epLocation = epLocation;
    }

}
