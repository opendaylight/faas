/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdifKey;

public class AdapterBdIf {

    Bdif bdif;
    Long vni;

    public AdapterBdIf(Bdif bdif, Long vni) {
        this.bdif = bdif;
        this.vni = vni;
    }

    public String getId() {
        return bdif.getId();
    }

    public String getBdid() {
        return bdif.getBdid();
    }

    public Integer getVrf() {
        return bdif.getVrf();
    }

    public IpAddress getIpAddress() {
        return bdif.getIpAddress();
    }

    public Integer getMask() {
        return bdif.getMask();
    }

    public MacAddress getMacAddress() {
        return bdif.getMacAddress();
    }

    public BdifKey getKey() {
        return bdif.getKey();
    }

    public Long getVni() {
        return vni;
    }

}
