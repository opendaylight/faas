/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.ArrayList;
import java.util.List;

public class Vlan {

    public Vlan(boolean isTagged, String vlanID, List<String> portList) {
        super();
        this.isTagged = isTagged;
        this.vlanID = vlanID;
        this.portList = portList;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public String getVlanID() {
        return vlanID;
    }

    public List<String> getPortList() {
        return portList;
    }

    private boolean isTagged;
    private String vlanID;
    private List<String> portList = new ArrayList<String>();

    @Override
    public String toString() {
        return portList.toString();
    }
}
