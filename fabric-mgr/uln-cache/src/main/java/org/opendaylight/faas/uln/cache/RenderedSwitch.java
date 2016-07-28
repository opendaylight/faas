/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

/**
 * RenderedSwtich - capture the association between the rendered logical switch and
 * its parent and its fabric.
 *
 */
public final class RenderedSwitch {
    private final NodeId parentId;
    private final NodeId fabricId;
    private final NodeId switchId;


    /**
     * Constructor.
     * @param fabricId - the host fabric of the rendered switch.
     * @param parentId - the logical switch of which the rendered switch is part.
     * @param switchId - the rendered switch id.
     */
    public RenderedSwitch(NodeId fabricId, NodeId parentId, NodeId switchId) {
        super();
        this.parentId = parentId;
        this.fabricId = fabricId;
        this.switchId = switchId;
    }

    public NodeId getFabricId() {
        return fabricId;
    }

    public NodeId getParentId() {
        return parentId;
    }

    public NodeId getSwitchID() {
        return switchId;
    }



}
