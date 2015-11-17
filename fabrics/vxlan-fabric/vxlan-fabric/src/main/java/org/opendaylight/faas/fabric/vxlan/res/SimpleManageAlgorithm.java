/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan.res;

public class SimpleManageAlgorithm implements SegmentManager, VrfCtxManager {

    private long seg = 1L;

    private long vrfctx = 1L;

    @Override
    public long allocSeg() {
        return seg++;
    }

    @Override
    public void freeSeg(long segment) {
        // do nothing
    }

    @Override
    public long allocVrfCtx() {
        return vrfctx++;
    }

    @Override
    public void freeVrfCtx(long vrf) {
        // do nothing
    }
}