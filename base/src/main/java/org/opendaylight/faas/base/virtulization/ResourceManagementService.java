/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

import java.util.List;

/**
 * @author xjchu
 */
public interface ResourceManagementService {

    ResourceType getType();

    ResourceDescriptor valloc(int size, NetNode node, String ownerID);

    public ResourceDescriptor getResource(int resourceID, NetNode node, String ownerID);

    void free(int id, NetNode node);

    ResourceDescriptor isResourceAvailable(int startID, int quota);

    int getCapacity();

    int getAvailableSize();

    List<ResourceDescriptor> getAvailable();
}
