/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class VcConfigDataMgr {

    private Uuid tenantId;
    private VcLdNodeConfigDataMgr ldNodeConfigDataMgr;
    private VcNetNodeConfigDataMgr netNodeConfigDataMgr;

    public VcConfigDataMgr(Uuid tenantId) {
        this.tenantId = tenantId;
        this.ldNodeConfigDataMgr = new VcLdNodeConfigDataMgr(tenantId);
        this.netNodeConfigDataMgr = new VcNetNodeConfigDataMgr(tenantId);
    }

    public VcLdNodeConfigDataMgr getLdNodeConfigDataMgr() {
        return ldNodeConfigDataMgr;
    }

    public void setLdNodeConfigDataMgr(VcLdNodeConfigDataMgr ldNodeConfigDataMgr) {
        this.ldNodeConfigDataMgr = ldNodeConfigDataMgr;
    }

    public VcNetNodeConfigDataMgr getNetNodeConfigDataMgr() {
        return netNodeConfigDataMgr;
    }

    public void setNetNodeConfigDataMgr(VcNetNodeConfigDataMgr netNodeConfigDataMgr) {
        this.netNodeConfigDataMgr = netNodeConfigDataMgr;
    }

    public Uuid getTenantId() {
        return tenantId;
    }

    public void setTenantId(Uuid tenantId) {
        this.tenantId = tenantId;
    }

    public NodeId getAvailabeVfabricResource() {
        return this.ldNodeConfigDataMgr.getAvailableVfabricResurce();
    }

}
