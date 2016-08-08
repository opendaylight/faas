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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

/**
 * RenderedSwtich - capture the association between the rendered logical switch and
 * its parent and its fabric.
 *
 */
public final class RenderedSwitch {
    private final NodeId parentId;
    private final NodeId fabricId;
    private final NodeId switchId;
    private Map<TpId, TpId> portMappings;

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
        portMappings = new HashMap();
    }

    public Map<TpId, TpId> getPortMappings() {
        return portMappings;
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

    /**
     * Add a logical port to fabric port map.
     * @param lport - logical port tpid.
     * @param fport - fabric port tpid.
     */
    public void addPortMap(TpId lport, TpId fport) {
        portMappings.put(lport, fport);
    }

    /**
     * remove a logical port to fabric port map.
     * @param lport - logical port tpid.
     * @param fport - fabric port tpid.
     */

    public void rmPortMap(TpId lport, TpId fport) {
        portMappings.remove(lport);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fabricId == null) ? 0 : fabricId.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((switchId == null) ? 0 : switchId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RenderedSwitch other = (RenderedSwitch) obj;
        if (fabricId == null) {
            if (other.fabricId != null)
                return false;
        } else if (!fabricId.equals(other.fabricId))
            return false;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (switchId == null) {
            if (other.switchId != null)
                return false;
        } else if (!switchId.equals(other.switchId))
            return false;
        return true;
    }



}
