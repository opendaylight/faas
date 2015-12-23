/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general.spi;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface FabricListener {

    void fabricCreated(FabricNode fabric);

    void fabricDeleted(Node fabric);

    void deviceAdded(InstanceIdentifier<Node> device);

    void deviceRemoved(InstanceIdentifier<Node> device);

    void logicSwitchCreated(NodeId nodeId, Node lsw);

    void logicSwitchRemoved(Node lsw);

    void logicRouterCreated(NodeId nodeId, Node lr);

    void logicRouterRemoved(Node lr);

    void gatewayRemoved(NodeId lswId, NodeId lrId);

    void aclUpdate(InstanceIdentifier<FabricAcl> iid, boolean delete);

}