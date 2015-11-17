package org.opendaylight.faas.fabric.vxlan;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class GatewayPort {

    private static final MacAddress GATEWAY_MAC = new MacAddress("80:38:bC:a1:33:c7");

    private IpAddress ip;
    private long vni;
    private MacAddress mac;

    public GatewayPort (IpAddress ip, long vni) {
        this.ip = ip;
        this.vni = vni;
    }

    public void setMac(MacAddress mac) {
        this.mac = mac;
    }

    IpAddress getIp() {
        return ip;
    }

    MacAddress getMac() {
        return mac == null ? GATEWAY_MAC : mac;
    }

    Long getVni() {
        return vni;
    }
}
