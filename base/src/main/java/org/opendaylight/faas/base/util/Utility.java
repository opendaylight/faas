/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utility {

    private static final int MAX_VLAN_PORT = 1024;

    public static String validateIP(String ipAddress) {
        try {
            return InetAddress.getByName(ipAddress).getHostAddress();

        } catch (UnknownHostException ex) {
            return null;
        }
    }

    public static String validatePort(String port) {
        try {

            int portnum = Integer.parseInt(port);
            if (portnum < MAX_VLAN_PORT) // Don't mess up with reserved port range
            {
                return null;
            }

            return port;

        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static TransportProtocol validateTransportProto(String proto) {
        if (proto.trim().equalsIgnoreCase("tcp"))
            return TransportProtocol.TCP;

        if (proto.trim().equalsIgnoreCase("udp"))
            return TransportProtocol.UDP;

        return TransportProtocol.UNKNOWN;

    }
}
