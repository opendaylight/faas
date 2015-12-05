/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class VcLdNodeConfigDataMgr {

    private Uuid tenantId;
    private List<NodeId> availVfabricList;

    public VcLdNodeConfigDataMgr(Uuid tenantId) {
        this.setTenantId(tenantId);
        this.availVfabricList = new ArrayList<NodeId>();
    }

    public Uuid getTenantId() {
        return tenantId;
    }

    public void setTenantId(Uuid tenantId) {
        this.tenantId = tenantId;
    }

    public void listenerActionOnVcLdNodeCreate(TenantId tenantId, List<NodeId> vfabricIdList) {
        if (this.availVfabricList == null) {
            this.availVfabricList = vfabricIdList;
        } else {
            this.availVfabricList.addAll(vfabricIdList);
        }
    }

    public NodeId getAvailableVfabricResurce() {
        if (this.availVfabricList == null || this.availVfabricList.isEmpty() == true) {
            return null;
        } else {
            return this.availVfabricList.get(0);
        }
    }

}
