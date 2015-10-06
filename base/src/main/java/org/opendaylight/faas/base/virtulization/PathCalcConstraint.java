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

public class PathCalcConstraint {

    private List<String> inclusiveNodeIds = new ArrayList<String>();
    private List<String> exclusiveNodeIds = new ArrayList<String>();
    private List<String> inclusiveLinkIds = new ArrayList<String>();
    private List<String> exclusiveLinkIds = new ArrayList<String>();

    public List<String> getInclusiveNodeIds() {
        return inclusiveNodeIds;
    }

    public void setInclusiveNodeIds(List<String> inclusiveNodeIds) {
        this.inclusiveNodeIds = inclusiveNodeIds;
    }

    public List<String> getExclusiveNodeIds() {
        return exclusiveNodeIds;
    }

    public PathCalcConstraint() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void setExclusiveNodeIds(List<String> exclusiveNodeIds) {
        this.exclusiveNodeIds = exclusiveNodeIds;
    }

    public List<String> getInclusiveLinkIds() {
        return inclusiveLinkIds;
    }

    public void setInclusiveLinkIds(List<String> inclusiveLinkIds) {
        this.inclusiveLinkIds = inclusiveLinkIds;
    }

    public List<String> getExclusiveLinkIds() {
        return exclusiveLinkIds;
    }

    public void setExclusiveLinkIds(List<String> exclusiveLinkIds) {
        this.exclusiveLinkIds = exclusiveLinkIds;
    }
}
