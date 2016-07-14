/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LogicSwitchContext implements AutoCloseable {
    private final int vlan;

    private Map<DeviceKey, String> members = Maps.newConcurrentMap();

    private LogicRouterContext vrfCtx = null;

    private List<String> acls = Lists.newArrayList();
    private List<String> inhertAcls = Lists.newArrayList();

    private final DataBroker databroker;

    private final FabricId fabricid;
    private final NodeId nodeid;
    private final boolean external;

    private final ExecutorService executor;

    LogicSwitchContext(DataBroker databroker, FabricId fabricid, int vlan, NodeId nodeid, ExecutorService executor,
            boolean external) {
        this.databroker = databroker;
        this.vlan = vlan;
        this.fabricid = fabricid;
        this.nodeid = nodeid;
        this.executor = executor;
        this.external = external;
    }

    public int getVlan() {
        return vlan;
    }

    public boolean checkAndSetNewMember(DeviceKey key, String mgntIp) {
        if (!members.containsKey(key)) {
            members.put(key, mgntIp);
            writeToDom(key, mgntIp);
            return true;
        } else {
            return false;
        }
    }

    public void removeMember(DeviceKey key) {
        String mgntIp = null;
        if ((mgntIp = members.remove(key)) != null) {
            deleteFromDom(key, mgntIp);
        }
    }

    public void associateToRouter(LogicRouterContext vrfCtx, IpPrefix ip) {
        this.vrfCtx = vrfCtx;
        vrfCtx.addGatewayPort(ip, vlan, this.nodeid);

        inhertAcls.addAll(vrfCtx.getAcls());
        writeToDom(false, vrfCtx.getAcls());
    }

    public GatewayPort unAssociateToRouter(LogicRouterContext vrfCtx) {
        LogicRouterContext oldVrfCtx = this.vrfCtx;
        this.vrfCtx = null;

        GatewayPort gwPort = oldVrfCtx.removeGatewayPort(vlan);
        List<String> oldAcls = Collections.unmodifiableList(inhertAcls);
        inhertAcls.clear();
        if (!oldAcls.isEmpty()) {
            writeToDom(true, oldAcls);
        }
        return gwPort;
    }

    public LogicRouterContext getVrfCtx() {
        return vrfCtx;
    }

    public Set<DeviceKey> getMembers() {
        return members.keySet();
    }

    @Override
    public void close() {
    }

    public void addAcl(String aclName) {
        acls.add(aclName);
        writeToDom(false, aclName, null);
    }

    public void removeAcl(String aclName) {
        acls.remove(aclName);
        writeToDom(true, aclName, null);
    }

    public void removeVrfAcl(String aclName) {
        inhertAcls.remove(aclName);
        writeToDom(true, aclName, null);
    }

    public void addVrfAcl(String aclName) {
        inhertAcls.add(aclName);
        writeToDom(false, aclName, null);
    }

    public boolean isExternal() {
        return external;
    }

    private boolean writeToDom(boolean delete, String aclName, WriteTransaction wt) {
        return true;
    }

    private void writeToDom(boolean delete, List<String> acls) {

        boolean upt = false;
        WriteTransaction trans = databroker.newWriteOnlyTransaction();

        for (String acl : acls) {
            upt |= writeToDom(delete, acl, trans);
        }

        if (upt) {
            MdSalUtils.wrapperSubmit(trans);
        }
    }

    private void writeToDom(DeviceKey key, String mgntIp) {

    }

    private void deleteFromDom(DeviceKey key, String mgntIp) {

    }

    private InstanceIdentifier<SupportingNode> createSuplNodeIId(DeviceKey key) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(this.fabricid.getValue())))
                .child(Node.class, new NodeKey(this.nodeid))
                .child(SupportingNode.class, new SupportingNodeKey(key.getNodeId(), key.getTopoId()));
    }
}
