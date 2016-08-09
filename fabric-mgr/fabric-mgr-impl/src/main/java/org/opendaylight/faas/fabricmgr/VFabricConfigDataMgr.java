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

/**
 * VFabricConfigDataMgr - manage allocated network resource to a tenant from a fabric.
 *
 */
public class VFabricConfigDataMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VFabricConfigDataMgr.class);
    private NodeId vfabricId;
    private TenantId tenantId;
    private int availableL2Resource;
    private int start = 0;

    //TODO - hardcode for now move those constants to a class.
    public static final int L2_DEF_NUM = 100;
    public static final int L2_START_INDEX = 200;

    /**
     * Constructor : initialize with default network resource.
     * @param vfabricId - vfabric Identifier.
     */
    public VFabricConfigDataMgr(NodeId vfabricId) {
        super();
        this.setVfabricId(vfabricId);
        this.start = L2_START_INDEX;
        this.availableL2Resource = L2_DEF_NUM;
    }

    /**
     * Constructor : initialize with default network resource.
     * @param start - start index of the allocated chunk resource.
     * @param vfabricId
     */
    public VFabricConfigDataMgr(NodeId vfabricId, int start) {
        super();
        this.setVfabricId(vfabricId);
        this.start = start;
        this.availableL2Resource = L2_DEF_NUM;
    }

    /**
     * Constructor : initialize with a given quantity of network resource.
     * @param vfabricId - Fabric identifier.
     * @param start - start index of the allocated chunk resource.
     * @param l2num  - layer 2 resource number.
     */
    public VFabricConfigDataMgr(NodeId vfabricId, int start, int l2num) {
        super();
        this.setVfabricId(vfabricId);
        this.availableL2Resource = l2num;
        this.start = start;
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

    public int getAvailableL2Resource() {
        this.availableL2Resource--;
        LOG.debug("FABMGR: getAvailablel2Resource: l2resource={}", this.availableL2Resource);
        return start + availableL2Resource + 1;
    }

    public void setAvailableL2Resource(int availablel2Resource) {
        this.availableL2Resource = availablel2Resource;
    }

    /**
     * Release a network resource.
     */

    public void releaseL2Resource() {
        this.availableL2Resource++;
        LOG.debug("FABMGR: releaseL2Resource: l2resource={}", this.availableL2Resource);
    }

    public int getUsedResourceNum() {
        return availableL2Resource;
    }

    public int getResourceStartIndex() {
        return start;
    }
}
