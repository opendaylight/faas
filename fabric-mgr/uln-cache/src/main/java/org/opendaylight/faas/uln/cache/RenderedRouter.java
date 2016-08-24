/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

/**
 * RenderedSwtich - capture the association between the rendered(distributed) logical router
 * its parent router and its fabric.
 *
 */
public final class RenderedRouter {
    private final NodeId fabricId;
    private final NodeId routerID;
    private final Map<NodeId, Uuid> gateways;
    private boolean isExternal = false;
    private NodeId extSwitch;
    private TpId accessTP;
    private int extAccessTag;
    private Uuid gwid;


    /**
     * Constructor.
     * @param ofabricId - the host fabric of the rendered switch.
     * @param routerID - the rendered router id.
     */
    public RenderedRouter(NodeId ofabricId, NodeId routerID) {
        super();
        this.fabricId = ofabricId;
        this.routerID = routerID;
        this.gateways = new HashMap<>(); //Differentiators
    }

    /**
     * Constructor.
     * @param fabricId - the host fabric of the rendered switch.
     * @param routerID - the rendered router id.
     */
    public RenderedRouter(String fabricId, NodeId routerID) {
        super();
        this.fabricId = new NodeId(fabricId);
        this.routerID = routerID;
        this.gateways = new HashMap<>(); //Differentiators
    }

    public NodeId getFabricId() {
        return fabricId;
    }

    public NodeId getRouterID() {
        return routerID;
    }



    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }

    public NodeId getExtSwitch() {
        return extSwitch;
    }

    public void setExtSwitch(NodeId extSwitch) {
        this.extSwitch = extSwitch;
    }


    public TpId getAccessTP() {
        return accessTP;
    }

    public void setAccessTP(TpId accessTP) {
        this.accessTP = accessTP;
    }

    public Uuid getGwid() {
        return gwid;
    }

    public void setGwid(Uuid gwid) {
        this.gwid = gwid;
    }

    public int getExtAccessTag() {
        return extAccessTag;
    }

    public void setExtAccessTag(int extAccessTag) {
        this.extAccessTag = extAccessTag;
    }

    public Map<NodeId, Uuid> getGateways() {
        return gateways;
    }

    public void addGateway(NodeId lsw, Uuid gatewayPort) {
        gateways.put(lsw, gatewayPort);
    }

    public void removeGateway(NodeId lsw) {
        gateways.remove(lsw);
    }

    @Override
    public String toString() {
        return "RenderedRouter [fabricId=" + fabricId + ", routerID=" + routerID + "]";
    }


}
