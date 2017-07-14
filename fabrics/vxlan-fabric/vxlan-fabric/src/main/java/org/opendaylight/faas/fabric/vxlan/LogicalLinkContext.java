/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

 package org.opendaylight.faas.fabric.vxlan;

import java.util.concurrent.ExecutorService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;

/**
 * @author xingjun
 *
 */
public class LogicalLinkContext implements AutoCloseable  {

    private final DataBroker databroker;

    private final FabricId fabricid;
    private final LinkId linkid;

    private final ExecutorService executor;

public LogicalLinkContext(DataBroker databroker, FabricId fabricid, LinkId linkid, ExecutorService executor)
{
        this.databroker = databroker;
        this.fabricid = fabricid;
        this.linkid = linkid;
        this.executor = executor;
}



public DataBroker getDatabroker() {
    return databroker;
}



public FabricId getFabricid() {
    return fabricid;
}



public LinkId getLinkid() {
    return linkid;
}



public ExecutorService getExecutor() {
    return executor;
}



/* (non-Javadoc)
 * @see java.lang.AutoCloseable#close()
 */
@Override
public void close() {
    // TODO Auto-generated method stub

}

}
