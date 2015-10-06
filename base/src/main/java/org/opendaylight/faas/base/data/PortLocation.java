/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

public class PortLocation {

    private final int slotID;
    private final int subSlotID;
    private final int portNumber;

    public int getSlotID() {
        return slotID;
    }

    public int getSubSlotID() {
        return subSlotID;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public PortLocation(String portID /* x/y/z */) {
        super();

        String[] tokens = portID.split("/");

        if (tokens.length != 3) {// eth-trunk (not physical port.)
            slotID = -1;
            subSlotID = -1;
            portNumber = -1;
        }

        else {
            slotID = Integer.parseInt(tokens[0].replaceAll("[^0-9]*", ""));
            subSlotID = Integer.parseInt(tokens[1].replaceAll("[^0-9]*", ""));
            portNumber = Integer.parseInt(tokens[2].replaceAll("[^0-9]*", ""));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PortLocation))
            return false;

        PortLocation loc = (PortLocation) obj;

        return slotID == loc.getSlotID() && subSlotID == loc.getSubSlotID() && portNumber == loc.getPortNumber();
    }

}
