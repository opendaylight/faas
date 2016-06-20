/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan.res;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;

public class ResourceManager implements SegmentManager, VrfCtxManager {

    private SegmentManager sgMgr = null;

    private VrfCtxManager vrfMgr = null;

    private static final Map<FabricId, ResourceManager> cache = new HashMap<FabricId, ResourceManager>();

    public static synchronized void initResourceManager(FabricId fabricId) {
        cache.put(fabricId, new ResourceManager());
    }

    public static ResourceManager getInstance(FabricId fabricId) {
        return cache.get(fabricId);
    }

    public static void freeResourceManager(FabricId fabricId) {

    }

    private ResourceManager() {
        SimpleManageAlgorithm mgr = new SimpleManageAlgorithm();
        sgMgr = mgr;
        vrfMgr = mgr;
    }

    @Override
    public long allocSeg() {
        return sgMgr.allocSeg();
    }

    @Override
    public void freeSeg(long segment) {
        sgMgr.allocSeg();
    }

    @Override
    public long allocVrfCtx() {
        return vrfMgr.allocVrfCtx();
    }

    @Override
    public void freeVrfCtx(long vrf) {
        vrfMgr.freeVrfCtx(vrf);
    }

}
