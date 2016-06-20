/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.utils;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceManager {

    private static final Logger LOG = LoggerFactory.getLogger(InterfaceManager.class);

    public static TpId createFabricPort(NodeId nodeid, TpId tpid) {
        return new TpId(String.format("%s - %s", nodeid.getValue(), tpid.getValue()));
    }
}