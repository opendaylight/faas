/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;

public class AdapterBridgeDomain {
    BridgeDomain bridgeDomain;
    List<IpAddress> vtepIpList;

    public AdapterBridgeDomain(BridgeDomain bridgeDomain, List<IpAddress> vtepIpList) {
        this.bridgeDomain = bridgeDomain;
        this.vtepIpList = vtepIpList;
    }

    public String getId() {
        return bridgeDomain.getId();
    }


}
