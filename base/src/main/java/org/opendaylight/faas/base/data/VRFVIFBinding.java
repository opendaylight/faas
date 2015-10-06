/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.List;

public class VRFVIFBinding {

    private String vrfName;
    private List<String> vifs;

    public String getVrfName() {
        return vrfName;
    }

    public void setVrfName(String vrfName) {
        this.vrfName = vrfName;
    }

    public List<String> getVifs() {
        return vifs;
    }

    public void setVifs(List<String> vifs) {
        this.vifs = vifs;
    }

    public VRFVIFBinding(String vrfName, List<String> vifs) {
        super();
        this.vrfName = vrfName;
        this.vifs = vifs;
    }

    @Override
    public String toString() {
        return "VRFVIFBinding [vrfName=" + vrfName + ", vifs=" + vifs + "]";
    }

}
