/**
 * Copyright (c) 2017 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.netconfcli;

import java.io.IOException;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.IOSubscriber;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.SSHSession;
import com.tailf.jnc.YangException;
import com.tailf.jnc.YangXMLParser;

public class DeviceCE extends Device {
    private NetConfSessionHW session;

    @Override
	public void newSession(String sessionName) throws JNCException, IOException, YangException {
        newSession(null, sessionName);
    }


    public NetConfSessionHW getMySession(){
        return session;
    }

    public DeviceCE(String name, DeviceUser user, String mgmt_ip, int mgmt_port) {
        super(name, user, mgmt_ip, mgmt_port);
    }

    @Override
	public void newSession(IOSubscriber sub, String sessionName) throws JNCException, IOException {
        // always create the configTree
        newSessionConfigTree(sessionName);

        final YangXMLParser parser = new YangXMLParser();
        final SSHSession sshSession = new SSHSession(con, defaultReadTimeout);
        if (sub != null) {
            sshSession.addSubscriber(sub);
        }
        session = new NetConfSessionHW(sshSession);

        if (backlog.size() > 0) {
            runBacklog(sessionName);
        }
    }

}
