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

public class EndpointLocationMappingInfo {

    private EndpointLocation epLocation;
    private Uuid renderedDeviceId;
    private boolean serviceHasBeenRendered;
    private boolean isToBeDeleted;
    private org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid lswId;
    private org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid lswPortId;

    public EndpointLocationMappingInfo(EndpointLocation epLocation) {
        super();
        this.epLocation = epLocation;
        this.serviceHasBeenRendered = false;
        this.isToBeDeleted = false;
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

    public boolean isToBeDeleted() {
        return this.isToBeDeleted;
    }

    public void markDeleted() {
        this.isToBeDeleted = true;
    }

    public org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid getLswId() {
        return lswId;
    }

    public void setLswId(org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid lswId) {
        this.lswId = lswId;
    }

    public org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid getLswPortId() {
        return lswPortId;
    }

    public void setLswPortId(
            org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid lswPortId) {
        this.lswPortId = lswPortId;
    }
}
