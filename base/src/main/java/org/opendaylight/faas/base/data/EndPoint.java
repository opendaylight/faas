/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

final public class EndPoint {

    final private String id;
    private String name;

    /**
     * Physical Port ID
     */
    private String interfaceID;
    private String type;
    private boolean isTagged;

    private int accessVLAN;
    private String ipAddress;
    private String macAddress;
    private String lswID;

    @Override
    public String toString() {
        return "EndPoint [id =" + id + " interfaceID=" + interfaceID + ", type=" + type + ", isTagged=" + isTagged
                + ", accessVLAN=" + accessVLAN + ", ipAddress=" + ipAddress + ", macAddress=" + macAddress + "]";
    }

    
    public EndPoint(String id, String type, String systemID, boolean isTagged, String macAddr, String ipAddr) {
        super();
        this.id = id;
        this.type = type;
        this.isTagged = isTagged;
        this.interfaceID = systemID;
        this.macAddress = macAddr;
        this.ipAddress = ipAddr;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean isTagged) {
        this.isTagged = isTagged;
    }

    public String getInterfaceID() {
        return interfaceID;
    }

    public void setInterfaceID(String interfaceID) {
        this.interfaceID = interfaceID;
    }

    public int getAccessVLAN() {
        return accessVLAN;
    }

    public void setAccessVLAN(int accessVLAN) {
        this.accessVLAN = accessVLAN;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getLswID() {
        return lswID;
    }

    public void setLswID(String lswID) {
        this.lswID = lswID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
