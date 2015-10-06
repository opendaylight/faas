/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

public class L2Resource {

    private L2TechnologyType tech_m;
    private String resourceId_m;

    public L2Resource(L2TechnologyType tech, String l2id) {
        this.tech_m = tech;
        this.resourceId_m = l2id;
    }

    public String getResourceId() {
        return resourceId_m;
    }

    public void setResourceId(String id) {
        this.resourceId_m = id;
    }

    public L2TechnologyType getTechType() {
        return tech_m;
    }

    public void setTechType(L2TechnologyType tech) {
        this.tech_m = tech;
    }
}
