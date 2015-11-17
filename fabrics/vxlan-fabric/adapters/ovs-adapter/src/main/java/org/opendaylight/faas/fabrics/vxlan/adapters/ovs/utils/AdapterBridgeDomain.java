package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomainKey;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

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
