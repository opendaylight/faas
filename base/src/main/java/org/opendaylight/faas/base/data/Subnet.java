/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.List;

final public class Subnet {

    public enum ADDRESS_FAMILY {
        ipv4, ipv6
    };

    final private ADDRESS_FAMILY family = ADDRESS_FAMILY.ipv4; // support IPV4 only for now.
    final private String ipAddress; // CIDR
    final private String subnetMask; // CIDR
    private boolean isDHCPEnabled = true;
    private List<String> dnsServers;
    private String defaultGW;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultGW == null) ? 0 : defaultGW.hashCode());
        result = prime * result + ((dnsServers == null) ? 0 : dnsServers.hashCode());
        result = prime * result + ((family == null) ? 0 : family.hashCode());
        result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
        result = prime * result + (isDHCPEnabled ? 1231 : 1237);
        result = prime * result + ((subnetMask == null) ? 0 : subnetMask.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Subnet other = (Subnet) obj;
        if (defaultGW == null) {
            if (other.defaultGW != null)
                return false;
        } else if (!defaultGW.equals(other.defaultGW))
            return false;
        if (dnsServers == null) {
            if (other.dnsServers != null)
                return false;
        } else if (!dnsServers.equals(other.dnsServers))
            return false;
        if (family != other.family)
            return false;
        if (ipAddress == null) {
            if (other.ipAddress != null)
                return false;
        } else if (!ipAddress.equals(other.ipAddress))
            return false;
        if (isDHCPEnabled != other.isDHCPEnabled)
            return false;
        if (subnetMask == null) {
            if (other.subnetMask != null)
                return false;
        } else if (!subnetMask.equals(other.subnetMask))
            return false;
        return true;
    }

    public String toSimpleString() {
        return ipAddress + ":" + subnetMask;
    }

    public static Subnet ANY = new Subnet("*", "*");

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isDHCPEnabled() {
        return isDHCPEnabled;
    }

    public List<String> getDnsServers() {
        return dnsServers;
    }

    public String getDefaultGW() {
        return defaultGW;
    }

    public ADDRESS_FAMILY getFamily() {
        return family;
    }

    public Subnet(String networkID, String subnetMask) {
        super();
        this.ipAddress = networkID;
        this.subnetMask = subnetMask;
    }

    public Subnet(String networkID, String subnetMask, String defaultGW, boolean isDHCPEnabled, List<String> dnsServers) {
        super();
        this.ipAddress = networkID;
        this.subnetMask = subnetMask;
        this.defaultGW = defaultGW;
        this.isDHCPEnabled = isDHCPEnabled;
        this.dnsServers = dnsServers;
    }

    public String getNetworkID() {
        return ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    @Override
    public String toString() {
        return "Subnet [family=" + family + ", ipAddress=" + ipAddress + ", subnetMask=" + subnetMask
                + ", isDHCPEnabled=" + isDHCPEnabled + ", dnsServers=" + dnsServers + ", defaultGW=" + defaultGW + "]";
    }

}
