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

public class VLANResourceManager implements ResourceManagementService {

    public static int MIN_RESOURCE_ID = 0;
    public static int MAX_RESOURCE_ID = 4096;

    final private ResourceType type = ResourceType.VLAN;
    final private List<ResourceDescriptor> allocated = new ArrayList<ResourceDescriptor>();
    final private List<ResourceDescriptor> available = new ArrayList<ResourceDescriptor>();

    public VLANResourceManager(ResourceDescriptor rd) {
        super();
        available.add(rd);
    }

    @Override
    public List<ResourceDescriptor> getAvailable() {
        return available;
    }

    @Override
    public ResourceType getType() {
        return type;
    }

    @Override
    synchronized public ResourceDescriptor isResourceAvailable(int resourceID, int size) {
        for (ResourceDescriptor rd : available) {
            if (rd.isValidResourceID(resourceID, size))
                return rd;
        }
        return null;
    }

    @Override
    synchronized public ResourceDescriptor getResource(int resourceID, NetNode node, String ownerID) {
        if (node != null && this.isResourceBasedOnMembers(node)) {
            getResourceOnAllMembers(resourceID, node, ownerID);
        }

        ResourceDescriptor rd = null;
        ResourceDescriptor ret = null;
        if ((rd = this.isResourceAvailable(resourceID, 1)) != null) {
            ret = new ResourceDescriptor(this.type, resourceID, 1, ownerID);

            allocated.add(ret);
            available.remove(rd);

            /**
             * The original *rd" might be split in one smaller or
             * two smaller pieces after the desired resource is taken out.
             * add those leftovers into *available*.
             */
            if (resourceID != rd.getId()) {
                available.add(new ResourceDescriptor(this.type, rd.getId(), resourceID - rd.getId(), node.getId()));
            }

            if (resourceID != rd.getId() + rd.getSize()) {
                available.add(new ResourceDescriptor(this.type, resourceID + 1, rd.getSize() + rd.getId() - resourceID,
                        node.getId()));
            }
        }

        return ret;
    }

    @Override
    synchronized public ResourceDescriptor valloc(int size, NetNode node, String ownerID) {

        ResourceDescriptor newrd = null;
        for (ResourceDescriptor rd : available) {
            if (rd.getSize() >= size) {
                available.remove(rd);
                if (rd.getSize() != size) {
                    available.add(new ResourceDescriptor(this.type, rd.getId() + size, rd.getSize() - size,
                            node.getId()));
                }

                newrd = new ResourceDescriptor(this.type, rd.getId(), size, ownerID);
                this.allocated.add(newrd);

                if (node != null && this.isResourceBasedOnMembers(node)) {
                    vallocOnAllMembers(size, node, ownerID);
                }

                return newrd;
            }
        }

        return newrd;
    }

    public List<ResourceDescriptor> getAllocated() {
        return allocated;
    }

    /**
     * get the minimal available resource of the top node
     */
    @Override
    synchronized public int getAvailableSize() {
        int availablesize = 0;

        for (ResourceDescriptor rd : available) {
            availablesize += rd.getSize();
        }

        return availablesize;
    }

    @Override
    synchronized public int getCapacity() {
        int availablesize = 0;

        for (ResourceDescriptor rd : available) {
            availablesize += rd.getSize();
        }

        int usedsize = 0;

        for (ResourceDescriptor rd : allocated) {
            usedsize += rd.getSize();
        }

        return availablesize + usedsize;
    }

    @Override
    synchronized public void free(int start_id, NetNode node) {
        if (this.isResourceBasedOnMembers(node)) {
            this.freeOnAllMembers(start_id, node);
        }

        ResourceDescriptor found = null;
        for (ResourceDescriptor rd : allocated) {
            if (rd.getId() == start_id) {
                found = rd;
                break;
            }
        }

        if (found != null) {
            allocated.remove(found);
            available.add(found);
        }
    }

    private List<ResourceManagementService> getMemberResourceMgrs(NetNode node) {
        List<ResourceManagementService> memberResourcelist = new ArrayList<ResourceManagementService>();
        List<NetNode> nlist = new ArrayList<NetNode>();

        try {
            nlist = node.getAllPhysicalLeaveNetnodes();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

        for (NetNode cNode : nlist) {
            if (cNode instanceof PBridge) {
                ResourceManagementService r = ((PBridge) cNode).getResource();
                if (r != null && !memberResourcelist.contains(r)) {
                    memberResourcelist.add(r);
                }
            }
        }

        return memberResourcelist;
    }

    synchronized private void getResourceOnAllMembers(int quantity, NetNode node, String ownerID) {

        for (ResourceManagementService r : getMemberResourceMgrs(node)) {
            r.valloc(quantity, node, ownerID);
        }
    }

    synchronized private void freeOnAllMembers(int rid, NetNode node) {
        for (ResourceManagementService r : getMemberResourceMgrs(node)) {
            r.free(rid, node);
        }
    }

    synchronized private void vallocOnAllMembers(int rid, NetNode node, String ownerID) {
        for (ResourceManagementService r : getMemberResourceMgrs(node)) {
            r.valloc(rid, node, ownerID);
        }
    }

    private boolean isResourceBasedOnMembers(NetNode node) {
        return node != null && node.getType() == NetNodeType.FABRIC;
    }
}
