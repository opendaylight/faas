/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VfabricConfigDataMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VfabricConfigDataMgr.class);
    private NodeId vfabricId;
    private TenantId tenantId;
    private int availablel2Resource;

    public VfabricConfigDataMgr(NodeId vfabricId) {
        super();
        this.setVfabricId(vfabricId);
        this.availablel2Resource = 100; // TODO: kludge kludge kludge
    }

    public NodeId getVfabricId() {
        return vfabricId;
    }

    public void setVfabricId(NodeId vfabricId) {
        this.vfabricId = vfabricId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public int getAvailablel2Resource() {
        this.availablel2Resource++;
        LOG.info("FABMGR:getAvailablel2Resource: l2resource={}", this.availablel2Resource);
        return availablel2Resource;
    }

    public void setAvailablel2Resource(int availablel2Resource) {
        this.availablel2Resource = availablel2Resource;
    }

}
