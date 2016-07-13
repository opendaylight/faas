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
 * A Virtual Container is inspired by the "Container" concept from virtualization practice, such as docker.
 * Within networking/FaaS project context,  it represents the abstraction of the network resource
 * allocated to a tenant. It contains two parts, one is a Net Node which represents
 * the provisioned logical network object configuration,  * i.e. the used part of the resource and
 * the other is  a Logical Device node which represents the unused network resource.
 * Those objects are managed by VContainerConfigMgr, VContainerLDConfigMgr and VContainerConfigMgr.
 * VContainerConfigMgr is composed of the other two as in UML definition.
 */
public final class VContainerConfigMgr {

    private final Uuid tenantId;
    private final VContainerLDConfigMgr ldNodeConfigDataMgr;
    private final VContainerNetConfigMgr netNodeConfigDataMgr;

    /**
     * Constructor: initialize the objects contained.
     * @param tenantId - tenant identifier.
     */
    public VContainerConfigMgr(Uuid tenantId) {
        this.tenantId = tenantId;
        this.ldNodeConfigDataMgr = new VContainerLDConfigMgr(tenantId);
        this.netNodeConfigDataMgr = new VContainerNetConfigMgr(tenantId);
    }

    public VContainerLDConfigMgr getLdNodeConfigDataMgr() {
        return ldNodeConfigDataMgr;
    }

    public VContainerNetConfigMgr getNetNodeConfigDataMgr() {
        return netNodeConfigDataMgr;
    }

    public Uuid getTenantId() {
        return tenantId;
    }
}
