/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.subnets.rev151013.subnets.container.subnets.Subnet;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class SubnetMappingInfo {

    private Subnet subnet;
    private NodeId renderedDeviceId;
    private boolean serviceHasBeenRendered;
    private boolean isToBeDeleted;

    public SubnetMappingInfo(Subnet subnet) {
        super();
        this.subnet = subnet;
        this.serviceHasBeenRendered = false;
        this.isToBeDeleted = false;
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

    public Subnet getSubnet() {
        return subnet;
    }

    public void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }

    public boolean isToBeDeleted() {
        return this.isToBeDeleted;
    }

    public void markDeleted() {
        this.isToBeDeleted = true;
    }
}
