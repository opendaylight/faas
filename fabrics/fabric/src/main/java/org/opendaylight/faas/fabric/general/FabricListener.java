/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface FabricListener {

    void fabricCreated(InstanceIdentifier<FabricNode> fabric);

    void fabricDeleted(Node fabric);

    void deviceAdded(InstanceIdentifier<FabricNode> fabric, InstanceIdentifier<Node> device);

    void deviceRemoved(InstanceIdentifier<FabricNode> fabric, InstanceIdentifier<Node> device);

    void endpointAdded(InstanceIdentifier<Endpoint> epIId);

    void endpointUpdated(InstanceIdentifier<Endpoint> epIId);

    void aclUpdate(InstanceIdentifier<?> iid, boolean port);

}