/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembersKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LogicSwitchContext {
    private final long vni;

    private Set<NodeId> members = Sets.newConcurrentHashSet();

    private LogicRouterContext vrfCtx = null;

    private final DataBroker databroker;

    private final FabricId fabricid;

    LogicSwitchContext(DataBroker databroker, FabricId fabricid, long vni) {
    	this.databroker = databroker;
        this.vni = vni;
        this.fabricid = fabricid;
    }

    public long getVni() {
        return vni;
    }

    public boolean checkAndSetNewMember(NodeId nodeid, IpAddress vtepIp) {
    	if (members.add(nodeid)) {
    		writeToDom(vtepIp);
    		return true;
    	} else {
    		return false;
    	}
    }

    public void associateToRouter(LogicRouterContext vrfCtx, IpPrefix ip) {
        this.vrfCtx = vrfCtx;
        vrfCtx.addGatewayPort(ip, vni);
    }

    public LogicRouterContext getVrfCtx() {
        return vrfCtx;
    }

    public Set<NodeId> getMembers() {
    	return members;
    }

    private void writeToDom(IpAddress vtepIp) {
    	InstanceIdentifier<VniMembers> vniMembersIId = createVniMembersIId(fabricid, vni);

    	VniMembersBuilder builder = new VniMembersBuilder();
		builder.setVni(vni);
		builder.setKey(new VniMembersKey(vni));
		builder.setVteps(Lists.newArrayList(vtepIp));

		WriteTransaction trans = databroker.newWriteOnlyTransaction();
		trans.merge(LogicalDatastoreType.OPERATIONAL, vniMembersIId, builder.build(), true);
		trans.submit();
    }

    private InstanceIdentifier<VniMembers> createVniMembersIId(FabricId fabricId, long vni) {
        return InstanceIdentifier.create(FabricRenderedMapping.class).child(Fabric.class, new FabricKey(fabricId))
                .child(VniMembers.class, new VniMembersKey(vni));
    }
}
