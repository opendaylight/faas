/*
 * Copyright (c) 2015, 2016 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

/**
 * Read only object to cache the rendered link information.
 * - between two layer 2 segments for a logical device
 *
 *
 */
public  final class RenderedLayer2Link {
    private final  RenderedSwitch alsw; // segment A to be bridged.
    private final  RenderedSwitch blsw; // segment B to be bridged.
    private final int tag;  // global Tag allocated for this link
    private final  TpId aftp; // fabric port for segment A.
    private final TpId altp; // logical port for segment A.
    private final TpId btp; //  fabric port for segment B.
    private final TpId bltp; // logical port for segment B.

    public RenderedSwitch getAlsw() {
        return alsw;
    }

    public RenderedSwitch getBlsw() {
        return blsw;
    }

    public int getTag() {
        return tag;
    }

    public TpId getAftp() {
        return aftp;
    }

    public TpId getAltp() {
        return altp;
    }

    public TpId getBtp() {
        return btp;
    }

    public TpId getBltp() {
        return bltp;
    }

    public RenderedLayer2Link(RenderedSwitch alsw, RenderedSwitch blsw,
            int tag, TpId aftp, TpId altp, TpId btp,
            TpId bltp) {
        super();
        this.alsw = alsw;
        this.blsw = blsw;
        this.tag = tag;
        this.aftp = aftp;
        this.altp = altp;
        this.btp = btp;
        this.bltp = bltp;
    }

    @Override
    public String toString() {
        return "RenderedLayer2Link [aLsw=" + alsw + ", bLsw=" + blsw + ", tag=" + tag + ", aftp=" + aftp + ", altp="
                + altp + ", btp=" + btp + ", bltp=" + bltp + "]";
    }



}
