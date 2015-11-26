/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

public enum Service {

    TRAFFIC_CLASSIFIER ((short) 0, "Traffic Classifier"),
    ARP_HANDlER ((short) 20, "Distributed ARP hander"),
    L3_ROUTING ((short) 60, "Distributed Virtual Routing (DVR)"),
    L3_FORWARDING ((short) 70, "Layer 3 forwarding/lookup service"),
    ACL_HANDlER ((short) 90, "ACL handler"),
    L2_FORWARDING ((short) 110, "Layer2 mac,vlan based forwarding");

    short table;
    String description;



    private Service (short table, String description)  {
        this.table = table;
        this.description = description;
    }

    public short getTable() {
        return table;
    }

    public String getDescription() {
        return description;
    }
}
