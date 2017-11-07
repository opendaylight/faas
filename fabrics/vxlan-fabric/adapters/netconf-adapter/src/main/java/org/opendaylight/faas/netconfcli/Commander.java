/**
 * Copyright (c) 2017 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.faas.netconfcli;

import java.io.IOException;

import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.JNCException;


public class Commander {

        private DeviceCE dev;
        private DeviceUser duser;
        private final String username;
        private final String password;
        private final String ipaddress;

        public Commander(String username, String password, String ip) {
            this.username = username;
            this.password = password;
            this.ipaddress = ip;

            this.init();
        }

        private void init() {

            System.out.println("Connecting to " + ipaddress );

            duser = new DeviceUser(username, "netconf", password);
            dev = new DeviceCE("mydev", duser, ipaddress, 22);

            try {
                dev.connect(username);
                dev.newSession("cfg");
                this.sendHello();
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } catch (JNCException e) {
                System.err.println("Can't authenticate " + e);
                System.exit(1);
            }
        }

        private String sendHello() {
            NetConfSessionHW s = dev.getMySession();
            String hello = MessageBuilder.msgHello();
            try {
                 s.sendRequest(hello);
                 return s.readReplyString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JNCException e) {
                e.printStackTrace();
                return null;
            }

        }

        public String sendRequest(String request){
            NetConfSessionHW s = dev.getMySession();

            try {
                if (s.getMySSHSession().serverSideClosed()) {
                    System.out.println("Session closed, reconnect ...");
                    init();
                }
            } catch (IOException e1) {
                System.out.println(e1.getMessage()); // TODO
                init();
            }

            s = dev.getMySession();


            try {
                 s.sendRequest(request);
                 return s.readReplyString();
            } catch (IOException e) {
                e.printStackTrace();
                return "nothing";
            } catch (JNCException e) {
                e.printStackTrace();
                return "readNothing";
            }
        }


        public void close() {
            dev.close();
        }
}
