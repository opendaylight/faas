/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

public enum PathCalcAlg {
    manual("manual"), stp("stp"), sp("spf"), mst("mst"), none("default"); // minimal spaning tree ;

    String algName;

    PathCalcAlg(String name) {
        algName = name;
    }

    // TODO
    public Topology getTopology(String topInfo) {
        if (algName.equalsIgnoreCase("manual")) {
            return null;
        }

        if (algName.equalsIgnoreCase("mst")) {
            return null;
        }

        return null;
    }
}
