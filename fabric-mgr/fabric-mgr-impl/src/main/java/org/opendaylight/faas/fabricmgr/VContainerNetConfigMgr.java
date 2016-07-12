/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;

/**
 * VContainerNetConfMgr - manage the Net object of a virtual container.
 * A Virtual Container object contains a Net Node and a Logical Device node.
 * A Net Node object contains the fully provisioned logical network object configuration.
 * A logical device node represents the unused network resource.
 */
//TODO : to implement later

public final class VContainerNetConfigMgr {
    private Uuid tenantId;

    /**
     * Constructor.
     * @param tenantId - tenant identifier
     */
    public VContainerNetConfigMgr(Uuid tenantId) {
        this.setTenantId(tenantId);
    }

    public Uuid getTenantId() {
        return tenantId;
    }

    public void setTenantId(Uuid tenantId) {
        this.tenantId = tenantId;
    }

}
