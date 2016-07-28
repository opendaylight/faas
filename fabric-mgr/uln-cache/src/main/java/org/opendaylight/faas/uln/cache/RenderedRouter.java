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
 * RenderedSwtich - capture the association between the rendered(distributed) logical router
 * its parent router and its fabric.
 *
 */
public final class RenderedRouter {
    private final NodeId parentId;
    private final NodeId fabricId;
    private final NodeId routerID;


    /**
     * Constructor.
     * @param fabricId - the host fabric of the rendered switch.
     * @param parentId - the logical switch of which the rendered switch is part.
     * @param routerID - the rendered router id.
     */
    public RenderedRouter(NodeId fabricId, NodeId parentId, NodeId routerID) {
        super();
        this.parentId = parentId;
        this.fabricId = fabricId;
        this.routerID = routerID;
    }

    public NodeId getFabricId() {
        return fabricId;
    }

    public NodeId getParentId() {
        return parentId;
    }

    public NodeId getRouterID() {
        return routerID;
    }



}
