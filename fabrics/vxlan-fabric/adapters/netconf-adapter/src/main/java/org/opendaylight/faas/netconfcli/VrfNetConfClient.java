/**
 * Copyright (c) 2017 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.netconfcli;

import java.io.IOException;

import org.opendaylight.faas.providers.netconf.VrfNetconfProvider;

import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;

public class VrfNetConfClient {
    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Element.setDebugLevel(4);
        //testCreateBD();
        //testCreatePorttoBD();
        testGetIfTags();
    }

    public static void testCreateBD() {
        VrfNetconfProvider client = new VrfNetconfProvider("xingjun", "Huawei@123", "192.168.252.20");
        client.init();
        String ret = client.createBridgeDomain("6000", "benz6000", "16000");
        System.out.print("Reply is " + ret);

        client.close();
    }

    public static void testDeleteBD() {
        VrfNetconfProvider client = new VrfNetconfProvider("xingjun", "Huawei@123", "192.168.252.20");
        client.init();
        System.out.print("Reply is " + client.deleteBridgeDomain("6000", "16000"));
        client.close();
    }


    public static void testCreateBDIF() {
        VrfNetconfProvider client = new VrfNetconfProvider("xingjun", "Huawei@123", "192.168.252.20");
        client.init();
        System.out.print("Reply is " + client.createBDIF(Long.valueOf(6000)));
        client.close();
    }

    public static void testDeleteBDIF() {
        VrfNetconfProvider client = new VrfNetconfProvider("xingjun", "Huawei@123", "192.168.252.20");
        client.init();
        System.out.print("Reply is " + client.deleteBDIF(Long.valueOf(6000)));
        client.close();
    }

    public static void testCreatePorttoBD() {
        VrfNetconfProvider client = new VrfNetconfProvider("xingjun", "Huawei@123", "192.168.252.20");
        client.init();

        System.out.print("reply is " + client.createSubIf("10GE1/0/2", "10GE", "3", "300"));

        System.out.print("Reply is " + client.createBdPort("6000", "10GE1/0/2.3"));

        client.close();
    }

    public static void testGetIfTags() {
        VrfNetconfProvider client = new VrfNetconfProvider("xingjun", "Huawei@123", "192.168.252.20");
        client.init();
        System.out.print("reply is " + client.getSubIFTags("10GE1/0/1.1"));
        client.close();
    }



}
