/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.common.rev151010.TenantId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VContainerLDConfigMgr - manage the logical device object of a virtual container.
 * A Virtual Container object contains a Net Node and a Logical Device node.
 * A Net Node object contains the fully provisioned logical network object configuration.
 * A logical device node represents the unused network resource which is represented by VContainerLDConfigMgr.
 */

public final class VContainerLDConfigMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VContainerLDConfigMgr.class);

    private Uuid tenantId;
    private Map<NodeId, VFabricConfigDataMgr> vfabricDataMgrStore; // vfabricId<-->vfabMgr Map

    /**
     * allocate a virtual container for a tenant.
     * @param tenantId - tenant identifier.
     */
    public VContainerLDConfigMgr(Uuid tenantId) {
        this.setTenantId(tenantId);
        this.vfabricDataMgrStore = new ConcurrentHashMap<>();
    }

    public Uuid getTenantId() {
        return tenantId;
    }

    public void setTenantId(Uuid tenantId) {
        this.tenantId = tenantId;
    }

    public void addVFabrics(TenantId tenantId, List<NodeId> vfabricIdList) {
        for (NodeId vfabId : vfabricIdList) {
            VFabricConfigDataMgr vfabDataMgr = new VFabricConfigDataMgr(vfabId);
            vfabDataMgr.setTenantId(tenantId);
            this.vfabricDataMgrStore.put(vfabId, vfabDataMgr);

            LOG.debug("addFabrics: vfabDataMgr created for vfabId: {}", vfabId.getValue());
        }
    }

    /**
     * Check resource availability.
     * @param target - the resource ID to be checked.
     * @return true if exists, otherwise false.
     */
    public boolean isVFabricAvailable(NodeId target) {
        if (this.vfabricDataMgrStore == null || this.vfabricDataMgrStore.isEmpty() == Boolean.TRUE) {
            LOG.error("FABMGR: ERROR: getAvailableVfabricId: vfabricDataMgrStore is null");
            return false;
        }

        /*
         * Just (randomly) grab the first entry in the vfabMgr list for now.
         */
        for (Entry<NodeId, VFabricConfigDataMgr> entry : this.vfabricDataMgrStore.entrySet()) {
            if (entry.getKey() == target) {
                return true;
            }
        }

        return false;

    }

    public int getAvailableL2Resource(NodeId vfabricId) {
        if (this.vfabricDataMgrStore == null || this.vfabricDataMgrStore.isEmpty() == true) {
            LOG.error("FABMGR: ERROR: getAvailableL2Resurce: vfabricDataMgrStore is null");
            return 0;
        }

        VFabricConfigDataMgr vfabDataMgr = this.vfabricDataMgrStore.get(vfabricId);
        if (vfabDataMgr == null) {
            LOG.error("FABMGR: ERROR: getAvailableL2Resurce: vfabDataMgr is null");
            return 0;
        }

        return vfabDataMgr.getAvailableL2Resource();
    }

    public Map<NodeId, VFabricConfigDataMgr> getVfabricDataMgrStore() {
        return vfabricDataMgrStore;
    }

    public void setVfabricDataMgrStore(Map<NodeId, VFabricConfigDataMgr> vfabricDataMgrStore) {
        this.vfabricDataMgrStore = vfabricDataMgrStore;
    }

    public void releaseL2Resource(NodeId vfabricId) {
        if (this.vfabricDataMgrStore == null || this.vfabricDataMgrStore.isEmpty() == true) {
            LOG.error("FABMGR: ERROR: releaseL2Resource: vfabricDataMgrStore is null");
            return;
        }

        VFabricConfigDataMgr vfabDataMgr = this.vfabricDataMgrStore.get(vfabricId);
        if (vfabDataMgr == null) {
            LOG.error("FABMGR: ERROR: releaseL2Resource: vfabDataMgr is null");
            return;
        }

        vfabDataMgr.releaseL2Resource();

    }

}
