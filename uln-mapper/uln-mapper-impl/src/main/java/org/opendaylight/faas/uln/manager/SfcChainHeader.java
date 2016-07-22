/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.manager;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;

public class SfcChainHeader {

    private Ipv4Address nshTunIpDst;
    private PortNumber nshTunUdpPort;
    private Short nshNsiToChain;
    private Long nshNspToChain;
    private int nshNsiFromChain;
    private Long nshNspFromChain;

    public SfcChainHeader(Ipv4Address nshTunIpDst, PortNumber nshTunUdpPort, Short nshNsiToChain, Long nshNspToChain,
            int nshNsiFromChain, Long nshNspFromChain) {
        super();
        this.nshTunIpDst = nshTunIpDst;
        this.nshTunUdpPort = nshTunUdpPort;
        this.nshNsiToChain = nshNsiToChain;
        this.nshNspToChain = nshNspToChain;
        this.nshNsiFromChain = nshNsiFromChain;
        this.nshNspFromChain = nshNspFromChain;
    }

    public Ipv4Address getNshTunIpDst() {
        return nshTunIpDst;
    }

    public void setNshTunIpDst(Ipv4Address nshTunIpDst) {
        this.nshTunIpDst = nshTunIpDst;
    }

    public PortNumber getNshTunUdpPort() {
        return nshTunUdpPort;
    }

    public void setNshTunUdpPort(PortNumber nshTunUdpPort) {
        this.nshTunUdpPort = nshTunUdpPort;
    }

    public Short getNshNsiToChain() {
        return nshNsiToChain;
    }

    public void setNshNsiToChain(Short nshNsiToChain) {
        this.nshNsiToChain = nshNsiToChain;
    }

    public Long getNshNspToChain() {
        return nshNspToChain;
    }

    public void setNshNspToChain(Long nshNspToChain) {
        this.nshNspToChain = nshNspToChain;
    }

    public int getNshNsiFromChain() {
        return nshNsiFromChain;
    }

    public void setNshNsiFromChain(int nshNsiFromChain) {
        this.nshNsiFromChain = nshNsiFromChain;
    }

    public Long getNshNspFromChain() {
        return nshNspFromChain;
    }

    public void setNshNspFromChain(Long nshNspFromChain) {
        this.nshNspFromChain = nshNspFromChain;
    }

}
