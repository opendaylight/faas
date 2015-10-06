/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.List;

public class Port {

    @Override
    public String toString() {
        return "Port [bandWidth=" + bandWidth + ", supportedVLANNumber=" + supportedVLANNumber + ", description="
                + description + ", hasNeighbor=" + hasNeighbor + ", neighbor=" + neighbor + ", resourceType="
                + resourceType + ", resourceLimit=" + resourceLimit + ", is8021QSupported=" + is8021QSupported
                + ", isDuplex=" + isDuplex + ", isAutoNegoEnabled=" + isAutoNegoEnabled + ", type=" + type
                + ", portID=" + portID + ", speed=" + speed + ", subnet=" + subnet + ", trunkList=" + trunkList
                + ", pVID=" + pVID + "]";
    }

    public LinkType getType() {
        return type;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    public long getBandWidth() {
        return bandWidth;
    }

    private long bandWidth;

    public void setBandWidth(long val) {
        this.bandWidth = val;
    }

    String supportedVLANNumber;
    String description;
    boolean hasNeighbor;
    Port neighbor;
    String resourceType;
    String resourceLimit;
    Boolean is8021QSupported;
    Boolean isDuplex;
    Boolean isAutoNegoEnabled;

    private LinkType type;

    public Port(String portID) {
        super();
        this.portID = portID;
    }

    public String getSupportedVLANNumber() {
        return supportedVLANNumber;
    }

    public void setSupportedVLANNumber(String supportedVLANNumber) {
        this.supportedVLANNumber = supportedVLANNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHasNeighbor() {
        return hasNeighbor;
    }

    public void setHasNeighbor(boolean hasNeighbor) {
        this.hasNeighbor = hasNeighbor;
    }

    public Port getNeighbor() {
        return neighbor;
    }

    public void setNeighbor(Port neighbor) {
        this.neighbor = neighbor;
    }

    public String getPortID() {
        return portID;
    }

    /*
     * LLDP Enable Status :enabled (default is disabled)
     * Total Neighbors :1
     * Port ID subtype :interfaceName
     * Port ID :GigabitEthernet0/0/1
     * Port description :HUAWEI, Quidway Series, GigabitEthernet0/0/1 Interface
     * Port And Protocol VLAN ID(PPVID) don't supported
     * Port VLAN ID(PVID) :100
     * VLAN name of VLAN 100: VLAN100
     * Protocol identity :STP RSTP/MSTP LACP EthOAM CFM
     * Auto-negotiation supported :Yes
     * Auto-negotiation enabled :Yes
     * OperMau :speed(1000)/duplex(Full)
     * Power port class :PD
     * PSE power supported :No
     * PSE power enabled :No
     * PSE pairs control ability:No
     * Power pairs :Unknown
     * Port power classification:Unknown
     * Link aggregation supported:Yes
     * Link aggregation enabled :No
     * Aggregation port ID :0
     * Maximum frame Size :9216
     * MED port information
     * Media policy type :Voice
     * Unknown Policy :Defined
     * VLAN tagged :Yes
     * Media policy VlanID :0
     * Media policy L2 priority :6
     * Media policy Dscp :46
     * Power Type :Unknown
     * PoE PSE power source :Unknown
     * Port PSE Priority :Unknown
     * Port Available power value:0
     */
    final private String portID;
    long speed;
    Subnet subnet;

    public Subnet getSubnet() {
        return subnet;
    }

    public void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceLimit() {
        return resourceLimit;
    }

    public void setResourceLimit(String resourceLimit) {
        this.resourceLimit = resourceLimit;
    }

    public Boolean getIs8021QSupported() {
        return is8021QSupported;
    }

    public void setIs8021QSupported(Boolean is8021qSupported) {
        is8021QSupported = is8021qSupported;
    }

    public Boolean getIsDuplex() {
        return isDuplex;
    }

    public void setIsDuplex(Boolean isDuplex) {
        this.isDuplex = isDuplex;
    }

    public Boolean getIsAutoNegoEnabled() {
        return isAutoNegoEnabled;
    }

    public void setIsAutoNegoEnabled(Boolean isAutoNegoEnabled) {
        this.isAutoNegoEnabled = isAutoNegoEnabled;
    }

    public LinkType getLinkType() {
        return type;
    }

    public void setLinkType(LinkType type) {
        this.type = type;
    }

    public List<String> getTrunkList() {
        return trunkList;
    }

    public void setTrunkList(List<String> trunkList) {
        this.trunkList = trunkList;
    }

    public String getpVID() {
        return pVID;
    }

    public void setpVID(String pVID) {
        this.pVID = pVID;
    }

    private List<String> trunkList;
    private String pVID;

    public enum ResourceType {
        VLAN, Tunnel;
    }

    public enum Bandwidth {
        ethernet(10), fastethernet(100), gigabitEthernet(1000), ge(1000), xg(10000), xgigabitethernet(10000), qxgigabitethernet(
                40000), xxgigabitethernet(100000), fortyge(40000), tenge(10000);

        private long value; // unit Megabit.

        Bandwidth(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }

}
