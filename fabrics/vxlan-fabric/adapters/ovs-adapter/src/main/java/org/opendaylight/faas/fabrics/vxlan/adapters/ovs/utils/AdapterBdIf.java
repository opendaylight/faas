package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdifKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.bdif.Acl;

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

    public List<Acl> getAcl() {
        return bdif.getAcl();
    }

    public BdifKey getKey() {
        return bdif.getKey();
    }

    public Long getVni() {
        return vni;
    }

}
