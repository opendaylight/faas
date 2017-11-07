/**
 * Copyright (c) 2017 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.netconfcli;

import java.io.IOException;

import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.SSHSession;

public class NetConfSessionHW extends NetconfSession {

    private final SSHSession mySSHSession;

    public NetConfSessionHW(SSHSession transport) throws JNCException,IOException {
              super();
              this.mySSHSession = transport;
              setTransport(transport);
}

    public SSHSession getMySSHSession() {
        return mySSHSession;
    }

    public String readReplyString() throws IOException, JNCException {
        final StringBuffer reply = getTransport().readOne();
        return reply.toString();
    }

}
