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

public class VcLdNodeConfigDataMgr {

    private static final Logger LOG = LoggerFactory.getLogger(VcLdNodeConfigDataMgr.class);

    private Uuid tenantId;
    private Map<NodeId, VfabricConfigDataMgr> vfabricDataMgrStore; // vfabricId<-->vfabMgr Map

    public VcLdNodeConfigDataMgr(Uuid tenantId) {
        this.setTenantId(tenantId);
        this.vfabricDataMgrStore = new ConcurrentHashMap<NodeId, VfabricConfigDataMgr>();
    }

    public Uuid getTenantId() {
        return tenantId;
    }

    public void setTenantId(Uuid tenantId) {
        this.tenantId = tenantId;
    }

    // TODO: this function should a listener action.
    public void listenerActionOnVcLdNodeCreate(TenantId tenantId, List<NodeId> vfabricIdList) {
        for (NodeId vfabId : vfabricIdList) {
            VfabricConfigDataMgr vfabDataMgr = new VfabricConfigDataMgr(vfabId);
            vfabDataMgr.setTenantId(tenantId);
            this.vfabricDataMgrStore.put(vfabId, vfabDataMgr);
            LOG.debug("FABMGR:listenerActionOnVcLdNodeCreate: vfabDataMgr created for vfabId: {}", vfabId.getValue());
        }
    }

    public NodeId getAvailableVfabricId() {
        if (this.vfabricDataMgrStore == null || this.vfabricDataMgrStore.isEmpty() == true) {
            LOG.error("FABMGR: ERROR: getAvailableVfabricId: vfabricDataMgrStore is null");
            return null;
        }

        NodeId vfabricId = null;

        /*
         * Just (randomly) grab the first entry in the vfabMgr list for now.
         */
        for (Entry<NodeId, VfabricConfigDataMgr> entry : this.vfabricDataMgrStore.entrySet()) {
            vfabricId = entry.getKey();
            break;
        }

        return vfabricId;

    }

    public int getAvailableL2Resource(NodeId vfabricId) {
        if (this.vfabricDataMgrStore == null || this.vfabricDataMgrStore.isEmpty() == true) {
            LOG.error("FABMGR: ERROR: getAvailableL2Resurce: vfabricDataMgrStore is null");
            return 0;
        }

        VfabricConfigDataMgr vfabDataMgr = this.vfabricDataMgrStore.get(vfabricId);
        if (vfabDataMgr == null) {
            LOG.error("FABMGR: ERROR: getAvailableL2Resurce: vfabDataMgr is null");
            return 0;
        }

        return vfabDataMgr.getAvailablel2Resource();
    }

    public Map<NodeId, VfabricConfigDataMgr> getVfabricDataMgrStore() {
        return vfabricDataMgrStore;
    }

    public void setVfabricDataMgrStore(Map<NodeId, VfabricConfigDataMgr> vfabricDataMgrStore) {
        this.vfabricDataMgrStore = vfabricDataMgrStore;
    }

}
