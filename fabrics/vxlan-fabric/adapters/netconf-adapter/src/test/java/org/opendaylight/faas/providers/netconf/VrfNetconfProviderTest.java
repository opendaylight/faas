/**
 * Copyright (c) 2017 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.providers.netconf;

import static org.junit.Assert.*;

import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.faas.fabric.vxlan.adapter.ce.Utility;
import org.opendaylight.faas.netconfcli.MessageBuilder;

public class VrfNetconfProviderTest {

    private VrfNetconfProvider client;

    private final String serverUnderTest = "192.168.252.21";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testBD() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        System.out.print("Reply is " + client.createBridgeDomain("6000", "benz6000", "16000"));
        System.out.print("Reply is " + client.createBDIF(Long.valueOf(6000)));

        System.out.print("Reply is " + client.deleteBDIF(Long.valueOf(6000)));
        System.out.print("Reply is " + client.deleteBridgeDomain("6000", "16000"));
        client.close();
        assertTrue(true);
    }

    @Test
    public void testSubnet() {
        System.out.println("the subnet of 24 is " + Utility.getIPv4Mask(16));
    }

    @Test
    public void getAllPorts() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        client.getAllPorts().forEach(item -> System.out.println(item));
        client.close();
        assertTrue(true);
    }


    @Test
    public void getLLDPInfo() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        System.out.print("Reply is " + client.getLLDPInfo("40GE1/0/3"));
        client.close();
        assertTrue(true);
    }

    @Test
    public void getNNIs() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        client.getAllPorts().forEach(item -> System.out.println(item));

        client.getNNIs().keySet().forEach(key -> {
                    com.google.common.collect.ImmutableMap<String, String> map = client.getNNIs().get(key);
                    if (map.entrySet().size() != 0) { // only consider the first one assuming it is one to one link, not one to many link.
                        String source = client.getServerIP();
                        String sourcetp = key;
                        Map.Entry<String, String> entry0  = map.entrySet().iterator().next();
                        String desttp = entry0.getKey();
                        String dest = entry0.getValue();

                        String lidvalue = source + ":" + sourcetp + "-" + dest + ":" + desttp;
                        System.out.println(lidvalue);
                    }
                });
        client.close();
        assertTrue(true);
    }

    @Test
    public void getNNI2s() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        client.getNNIs().keySet().forEach(
                (eifname) -> {
                    System.out.print(eifname);
                    if (!client.getNNIs().get(eifname).isEmpty()) {
                        for (String keyvalue : client.getNNIs().get(eifname).keySet()) {
                            System.out.println("-" + keyvalue + ":" + client.getNNIs().get(eifname).get(keyvalue));
                        }
                    }
                    });
        client.close();
        assertTrue(true);
    }

    @Test
    public void getSysInfo() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        System.out.println(client.getLLDPSysInfo().toString());
        System.out.println("vtep is " + client.getVtepIPAddress());
        client.close();
        assertTrue(true);
    }


    @Test
    public void createSubIf() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        System.out.println(client.createSubIf("10GE1/0/5", "10GE", "200", "200"));
        client.close();
        assertTrue(true);
    }

    @Test
    public void removeSubIf() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        System.out.println(client.removeSubIf("10GE1/0/5.200"));
        client.close();
        assertTrue(true);
    }


    @Test
    public void testHexString() {
        System.out.println(MessageBuilder.buildHexString(10));
    }

    @Test
    public void createBDPort() {

    client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
    client.init();
    System.out.println(client.createSubIf("10GE1/0/5", "10GE", "200", "200"));
    System.out.println(client.createBdPort("1", "10GE1/0/5" + "." + "200"));
    System.out.println(client.deleteBdPort("1", "10GE1/0/5" + "." + "200"));
    client.close();
    assertTrue(true);
    }


    @Test
    public void createVRF() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        System.out.println(client.createVRF("1000"));
        System.out.println(client.setVRFVNI("1000", "1000"));
        System.out.println(client.setVRFAfandRD("1000", "1000:1000"));
        System.out.println(client.setVRFRT("1000",
                "1000:1000", "1000:1000"
                ));
        System.out.println(client.removeVRF("1000"));

        System.out.println(client.bindIFtoVRF("vbdif5001", "1000", "1.2.3.4", "255.255.255.0"));

        client.close();
        assertTrue(true);

    }

    @Test
    public void enableArpCollectHost() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        System.out.println(client.enableArpConfig("vbdif5000"));
        client.close();
        assertTrue(true);
    }

    @Test
    public void testDirectRouteAllowInBGP() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.allowDirectRoute("bmw");
        System.out.println(ret);
        client.close();
        assertTrue(true);
    }

    @Test
    public void testAddIPv4familyinBGP() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.addIPv4FamilyinBGP("bmw");
        System.out.println(ret);
        client.close();
        assertTrue(true);
    }

    @Test
    public void testDeleteIPv4familyinBGP() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.deleteIPv4FamilyinBGP("bmw");
        System.out.println(ret);
        client.close();
        assertTrue(true);
    }


    @Test
    public void testDeleteDirectRouteFromBGP() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.deleteDirectRoute("bmw");
        System.out.println(ret);
        client.close();
        assertTrue(true);
    }


    @Test
    public void testCreateIPV4Prefix() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.createIPv4Prefix("prefix");
        System.out.println( ret);
        client.close();
        assertTrue(true);
    }

    @Test
    public void testRemoveIPV4Prefix() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.removeIPv4Prefix("prefix");
        System.out.println( ret);
        client.close();
        assertTrue(true);
    }


    @Test
    public void testCreateIPV4PrefixNode() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.createIPv4PrefixNode("prefix", "10", "192.168.1.10", 24);
        System.out.println( ret);
        client.close();
        assertTrue(true);
    }

    @Test
    public void testRemoveIPV4PrefixNode() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.removeIPv4PrefixNode("prefix", "10", "192.168.1.10", 24);
        System.out.println( ret);
        client.close();
        assertTrue(true);
    }

    @Test
    public void testCreatePolicy() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.addRoutePolicy("bmw", true);
        String ret1 = client.addRoutePolicy("bmw", false);
        System.out.println( ret + ret1);
        client.close();
        assertTrue(true);
    }

    @Test
    public void testRemovePolicy() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.deleteRoutePolicy("bmw", true);
        String ret1 = client.deleteRoutePolicy("bmw", false);
        System.out.println( ret + ret1);
        client.close();
        assertTrue(true);
    }



    @Test
    public void testCreatePolicyEntry() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.addImportRoutePolicyEntry("bmw", "prefix", "50");
        System.out.println( ret);
        client.close();
        assertTrue(true);
    }

    @Test
    public void testRemovePolicyEntry() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.deleteImportRoutePolicyEntry("bmw", "prefix", "50");
        System.out.println( ret);
        client.close();
        assertTrue(true);
    }



    @Test
    public void testGetRouteImportFromBGP() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret3 = client.getRouteImportFromBGP("bmw");

        System.out.println( ret3);
        client.close();
        assertTrue(true);
    }


    @Test
    public void testRoutePolicy() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret = client.createIPv4Prefix("prefix_test");
        String ret1 = client.createIPv4PrefixNode("prefix_test", "10", "192.168.1.10", 31);
        String ret2 = client.addRoutePolicy("bmw",true);
        String ret3 = client.addRoutePolicy("bmw",false);
        String ret4 = client.addImportRoutePolicyEntry("bmw", "prefix_test", "20");
        String ret5 = client.addIPv4FamilyinBGP("bmw");
        String ret6 = client.allowDirectRoute("bmw");
        String ret7 = client.setVRFAfandRD("bmw", "1234:1");

        client.close();
        assertTrue(true);
    }


    @Test
    public void testAddPolicy() {
        client = new VrfNetconfProvider("xingjun", "Huawei@123", serverUnderTest);
        client.init();
        String ret2 = client.addRoutePolicy("bmw",true);
        String ret3 = client.addRoutePolicy("bmw",false);
        String ret = client.setVRFAfandRD("bmw", "1234:1");
        System.out.println(ret);
        client.close();
        assertTrue(true);
    }



}
