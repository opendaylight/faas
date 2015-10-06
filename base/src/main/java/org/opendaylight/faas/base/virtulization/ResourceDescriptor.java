/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

public class ResourceDescriptor {

    public static String defaultOwnerID = "unknown";
    final private ResourceType type;
    final private int id;
    final private int size;
    private String ownerID;

    public ResourceDescriptor(ResourceType type, int id, int size, String owner) {
        super();
        this.type = type;
        this.id = id;
        this.size = size;
        this.ownerID = owner;
    }

    public int getId() {
        return id;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String id) {
        ownerID = id;
    }

    public int getSize() {
        return size;
    }

    public boolean isValidResourceID(int id, int quota) {
        if (id >= id && (id + quota) <= (id + size))
            return true;

        return false;
    }

    @Override
    public String toString() {
        return "ResourceDescriptor [type=" + type + ", id=" + id + ", size=" + size + ", ownerID=" + ownerID + "]";
    }

}
