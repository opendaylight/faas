/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.faas.base.data.Subnet;
import org.opendaylight.faas.base.data.VIFStatistics;
import org.opendaylight.faas.base.data.VIFStatus;

/**
 * VIF - Virtual Interface
 * - may belong to multiple net nodes due to abstraction and
 * - may be used by multiple service networks.
 */
final public class VIF {

    final private VIFType type;

    /**
     * User friendly name for display.
     */

    private String name;

    /**
     * internal ID used by SDN controller
     */
    final private String id;

    /**
     * Vendor/system specific ID
     */
    final private String systemId;

    /*
     * reverse pointer to the link who has the VIF as an end point.
     */
    private String linkID;

    /**
     * Where the VIF resides
     */
    final private String physicalNodeID;

    /**
     * list of net nodes that contain the VIF and
     * the list collection maintains the abstraction order
     */

    private List<String> netNodeIdList_m = new ArrayList<String>();

    /**
     * The list of service NetNode who USEs the VIF.
     */
    private List<String> serviceNetNodeID = new ArrayList<String>();

    /**
     * Ownership: owningServiceNetNodeID : the NetNode who owns the VIF as a resource
     */
    private String owningServiceNetNodeID;

    private VIFStatus status;
    private int bandwidth;

    private VIFStatistics statistics;

    private List<VIF> underlay_ports = new ArrayList<VIF>();
    private String linkAddress;
    private String ipAddress;
    private Subnet subNet;

    public VIF(String systemId, VIFType type, String hostNetNodeID) {
        super();
        this.systemId = systemId;
        this.type = type;
        this.physicalNodeID = hostNetNodeID;

        this.netNodeIdList_m.add(this.physicalNodeID);
        this.id = VIF.generateID(hostNetNodeID, systemId);
        this.name = id;
    }

    public VIF(String systemId, String netNodeID) {
        super();
        this.systemId = systemId;
        this.type = VIFType.port;
        this.physicalNodeID = netNodeID;

        this.netNodeIdList_m.add(this.physicalNodeID);
        this.id = VIF.generateID(netNodeID, systemId);
        this.name = id;
    }

    public Subnet getSubnet() {
        return subNet;
    }

    public void setSubnet(Subnet subnet) {
        this.subNet = subnet;
    }

    public String getOwningServiceNetNodeID() {
        return owningServiceNetNodeID;
    }

    public void setOwningServiceNetNodeID(String owningServiceNetNodeID) {
        this.owningServiceNetNodeID = owningServiceNetNodeID;
    }

    public String getPhysicalNodeID() {
        return this.physicalNodeID;
    }

    public String getLinkID() {
        return linkID;
    }

    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VIF> getPorts() {
        return Collections.unmodifiableList(underlay_ports);
    }

    public void setPorts(List<VIF> ports) {
        this.underlay_ports = ports;
    }

    synchronized public void addServiceNetNodeID(String serviceNetNodeID) {
        this.serviceNetNodeID.add(serviceNetNodeID);
    }

    synchronized public void removeServiceNetNodeID(String serviceNetNodeID) {
        this.serviceNetNodeID.remove(serviceNetNodeID);
    }

    synchronized public List<String> getAllServiceNetNodeID() {
        return Collections.unmodifiableList(serviceNetNodeID);
    }

    static public String generateID(String deviceId, String portSystemId) {
        return deviceId + ":" + portSystemId;
    }

    public String getLinkaddress() {
        return linkAddress;
    }

    public void setLinkaddress(String linkaddress) {
        this.linkAddress = linkaddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public VIFStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(VIFStatistics statistics) {
        this.statistics = statistics;
    }

    public VIFStatus getStatus() {
        return status;
    }

    public void setStatus(VIFStatus status) {
        this.status = status;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getId() {
        return id;
    }

    public VIFType getType() {
        return type;
    }

    public List<String> getAllNetNodeIDs() {
        return Collections.unmodifiableList(netNodeIdList_m);
    }

    synchronized public void addNetNodeID(String nodeID) {
        netNodeIdList_m.add(nodeID);
    }

    synchronized public String getVFabricNodeID() {
        if (this.netNodeIdList_m == null || this.netNodeIdList_m.isEmpty()) {
            return null;
        }

        return this.netNodeIdList_m.get(this.netNodeIdList_m.size() - 1);
    }

    synchronized public void addAllNetNodeIDs(List<String> ids) {
        netNodeIdList_m.removeAll(ids);
        netNodeIdList_m.addAll(ids);
    }

    synchronized public void clearAllNetNodeIDs(List<String> ids) {
        netNodeIdList_m.removeAll(ids);
    }

    synchronized public void removeNetNodeID(String id) {
        netNodeIdList_m.remove(id);
    }

    @Override
    public String toString() {
        return "VIF [type=" + type + ", name=" + name + ", id=" + id + ", systemId=" + systemId + ", linkID=" + linkID
                + ", physicalNodeID=" + physicalNodeID + ", netNodeIDs=" + netNodeIdList_m + ", serviceNetNodeID="
                + serviceNetNodeID + ", owningServiceNetNodeID=" + owningServiceNetNodeID + ", status=" + status
                + ", bandwidth=" + bandwidth + ", statistics=" + statistics + ", underlay_ports=" + underlay_ports
                + ", linkAddress=" + linkAddress + ", ipAddress=" + ipAddress + ", subNet=" + subNet + "]";
    }

}
