/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

import java.util.ArrayList;
import java.util.List;

public class PBridge extends NetNode {

    public PBridge(NetNode node, String url, String model, String managementIP) {
        super(node);

        ResourceDescriptor rd = new ResourceDescriptor(ResourceType.VLAN, VLANResourceManager.MIN_RESOURCE_ID,
                VLANResourceManager.MAX_RESOURCE_ID - VLANResourceManager.MIN_RESOURCE_ID, node.getId());

        this.resource = new VLANResourceManager(rd);
        this.partitionURL = url;
        this.managementIP = managementIP;
        this.deviceModel = model;
    }

    private String managementIP;
    private String deviceRole;
    private String deviceModel;
    private String vendor;

    private List<String> tenants = new ArrayList<String>();
    private final VLANResourceManager resource;
    final private String partitionURL; // partition id

    @Override
    public String toString() {
        return "PBridge [tenants=" + tenants + ", partitionURL=" + partitionURL + "]";
    }

    public List<String> getTenants() {
        return tenants;
    }

    public void setTenants(List<String> tenants) {
        this.tenants = tenants;
    }

    public VLANResourceManager getResource() {
        return resource;
    }

    public String getPartitionURL() {
        return partitionURL;
    }

    public String getManagementIP() {
        return managementIP;
    }

    public void setManagementIP(String managementIP) {
        this.managementIP = managementIP;
    }

    public String getDeviceRole() {
        return deviceRole;
    }

    public void setDeviceRole(String deviceRole) {
        this.deviceRole = deviceRole;
    }

    public String getDevieModel() {
        return deviceModel;
    }

    public void setDevieModel(String devieModel) {
        this.deviceModel = devieModel;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
