/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAclKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Acls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.AclsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembersKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.Members;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.MembersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.MembersKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LogicSwitchContext implements AutoCloseable {
    private final long vni;

    private Map<DeviceKey, IpAddress> members = Maps.newConcurrentMap();

    private LogicRouterContext vrfCtx = null;

    private List<String> acls = Lists.newArrayList();
    private List<String> inhertAcls = Lists.newArrayList();

    private final DataBroker databroker;

    private final FabricId fabricid;
    private final NodeId nodeid;
    private final boolean external;

    private final ExecutorService executor;

    LogicSwitchContext(DataBroker databroker, FabricId fabricid, long vni, NodeId nodeid, ExecutorService executor,
            boolean external) {
        this.databroker = databroker;
        this.vni = vni;
        this.fabricid = fabricid;
        this.nodeid = nodeid;
        this.executor = executor;
        this.external = external;
    }

    public long getVni() {
        return vni;
    }

    public boolean checkAndSetNewMember(DeviceKey key, IpAddress vtepIp) {
        if (!members.containsKey(key)) {
            members.put(key, vtepIp);
            writeToDom(key, vtepIp);
            return true;
        } else {
            return false;
        }
    }

    public void removeMember(DeviceKey key) {
        IpAddress vtep = null;
        if ((vtep = members.remove(key)) != null) {
            deleteFromDom(key, vtep);
        }
    }

    public void associateToRouter(LogicRouterContext vrfCtx, IpPrefix ip) {
        this.vrfCtx = vrfCtx;
        vrfCtx.addGatewayPort(ip, vni, this.nodeid);

        inhertAcls.addAll(vrfCtx.getAcls());
        writeToDom(false, vrfCtx.getAcls());
    }

    public GatewayPort unAssociateToRouter(LogicRouterContext vrfCtx) {
        LogicRouterContext oldVrfCtx = this.vrfCtx;
        this.vrfCtx = null;

        GatewayPort gwPort = oldVrfCtx.removeGatewayPort(vni);
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
        InstanceIdentifier<VniMembers> vniMembersIId = createVniMemberIId(fabricid, vni);

        WriteTransaction trans = databroker.newWriteOnlyTransaction();
        trans.delete(LogicalDatastoreType.OPERATIONAL, vniMembersIId);
        MdSalUtils.wrapperSubmit(trans, executor);

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
        if (delete && (acls.contains(aclName) || inhertAcls.contains(aclName))) {
            return false;
        }
        InstanceIdentifier<FabricAcl> aclIid = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(this.fabricid))
                .child(Acls.class, new AclsKey(this.vni))
                .child(FabricAcl.class, new FabricAclKey(aclName));

        WriteTransaction trans = wt == null ? databroker.newWriteOnlyTransaction() : wt;

        if (delete) {
            trans.delete(LogicalDatastoreType.OPERATIONAL, aclIid);
        } else {
            FabricAclBuilder builder = new FabricAclBuilder();
            builder.setFabricAclName(aclName);
            trans.merge(LogicalDatastoreType.OPERATIONAL, aclIid, builder.build(), true);
        }

        if (wt == null) {
            MdSalUtils.wrapperSubmit(trans);
        }
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

    private void writeToDom(DeviceKey key, IpAddress vtepIp) {
        InstanceIdentifier<Members> vniMembersIId = createVniVtepIId(fabricid, vni, vtepIp);

        MembersBuilder vbuilder = new MembersBuilder();
        vbuilder.setVtep(vtepIp);
        vbuilder.setKey(new MembersKey(vtepIp));

        InstanceIdentifier<SupportingNode> suplNodeIId = createSuplNodeIId(key);
        SupportingNodeBuilder sbuilder = new SupportingNodeBuilder();
        sbuilder.setNodeRef(key.getNodeId());
        sbuilder.setTopologyRef(key.getTopoId());

        WriteTransaction trans = databroker.newWriteOnlyTransaction();
        trans.merge(LogicalDatastoreType.OPERATIONAL, vniMembersIId, vbuilder.build(), true);
        trans.merge(LogicalDatastoreType.OPERATIONAL, suplNodeIId, sbuilder.build(), false);
        MdSalUtils.wrapperSubmit(trans, executor);
    }

    private void deleteFromDom(DeviceKey key, IpAddress vtepIp) {
        InstanceIdentifier<Members> vniMembersIId = createVniVtepIId(fabricid, vni, vtepIp);
        InstanceIdentifier<SupportingNode> suplNodeIId = createSuplNodeIId(key);

        WriteTransaction trans = databroker.newWriteOnlyTransaction();
        trans.delete(LogicalDatastoreType.OPERATIONAL, vniMembersIId);
        trans.delete(LogicalDatastoreType.OPERATIONAL, suplNodeIId);
        MdSalUtils.wrapperSubmit(trans, executor);
    }

    private InstanceIdentifier<Members> createVniVtepIId(FabricId fabricId, long vni, IpAddress vtep) {
        return InstanceIdentifier.create(FabricRenderedMapping.class).child(Fabric.class, new FabricKey(fabricId))
                .child(VniMembers.class, new VniMembersKey(vni))
                .child(Members.class, new MembersKey(vtep));
    }

    private InstanceIdentifier<SupportingNode> createSuplNodeIId(DeviceKey key) {
        return InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(this.fabricid.getValue())))
                .child(Node.class, new NodeKey(this.nodeid))
                .child(SupportingNode.class, new SupportingNodeKey(key.getNodeId(), key.getTopoId()));
    }

    private InstanceIdentifier<VniMembers> createVniMemberIId(FabricId fabricId, long vni) {
        return InstanceIdentifier.create(FabricRenderedMapping.class).child(Fabric.class, new FabricKey(fabricId))
                .child(VniMembers.class, new VniMembersKey(vni));
    }
}
