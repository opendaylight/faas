/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import org.opendaylight.faas.fabric.general.spi.FabricListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MockFabricListener implements FabricListener {

    @Override
    public void fabricCreated(FabricNode fabric) {
    }

    @Override
    public void fabricDeleted(Node fabric) {
    }

    @Override
    public void deviceAdded(InstanceIdentifier<Node> device) {
    }

    @Override
    public void deviceRemoved(InstanceIdentifier<Node> device) {
    }

    @Override
    public void logicSwitchCreated(NodeId nodeId, Node lsw) {
    }

    @Override
    public void logicSwitchRemoved(Node lsw) {
    }

    @Override
    public void logicRouterCreated(NodeId nodeId, Node lr) {
    }

    @Override
    public void logicRouterRemoved(Node lr) {
    }

    @Override
    public void gatewayRemoved(NodeId lswId, NodeId lrId) {
    }

    @Override
    public void aclUpdate(InstanceIdentifier<FabricAcl> iid, boolean delete) {
    }

}
