/*
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public final class RenderedGateway {
    private final NodeId fabricId;
    private final Uuid port;
    private final NodeId lswId;
    private final NodeId lrId;
    public RenderedGateway(NodeId fabricId, Uuid port, NodeId lswId, NodeId lrId) {
        super();
        this.fabricId = fabricId;
        this.port = port;
        this.lswId = lswId;
        this.lrId = lrId;
    }
    public Uuid getPort() {
        return port;
    }
    public NodeId getLswId() {
        return lswId;
    }
    public NodeId getLrId() {
        return lrId;
    }
    public NodeId getFabricId() {
        return this.fabricId;
    }
    @Override
    public String toString() {
        return "RenderedGateway [fabricId = " + fabricId + " port=" + port + ", lswId=" + lswId + ", lrId=" + lrId + "]";
    }



}
