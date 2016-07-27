/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan.res;

public class SimpleManageAlgorithm implements SegmentManager, VrfCtxManager {

    private long seg = 2L;

    private int vrfctx = 1;

    @Override
    public long allocSeg() {
        return seg++;
    }

    @Override
    public void freeSeg(long segment) {
        // do nothing
    }

    @Override
    public int allocVrfCtx() {
        return vrfctx++;
    }

    @Override
    public void freeVrfCtx(int vrf) {
        // do nothing
    }
}