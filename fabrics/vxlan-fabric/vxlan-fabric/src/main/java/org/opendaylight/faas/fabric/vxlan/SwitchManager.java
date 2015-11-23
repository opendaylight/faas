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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class SwitchManager {

    private final Table<FabricId, NodeId, LogicSwitchContext> logicSwitches = HashBasedTable.create();

    private final Table<FabricId, NodeId, DeviceContext> devices = HashBasedTable.create();

    private final Table<FabricId, NodeId, LogicRouterContext> logicRouters = HashBasedTable.create();

    private final DataBroker databroker;

    private final ExecutorService executor;

    public SwitchManager(DataBroker databroker, ExecutorService executor) {
        this.databroker = databroker;
        this.executor = executor;
    }

    public void addLogicRouter(FabricId fabricId, NodeId routerId, long vrf) {
        logicRouters.put(fabricId, routerId, new LogicRouterContext(vrf));
    }

    public void addLogicSwitch(FabricId fabricid, NodeId nodeId, long vni) {
        System.out.println(String.format("fabric:%s, \t lsw:%s",  fabricid, nodeId));
        logicSwitches.put(fabricid, nodeId, new LogicSwitchContext(databroker, fabricid, vni));
    }

    public void addDeviceSwitch(FabricId fabricid,InstanceIdentifier<Node> deviceIId, IpAddress vtep) {
        System.out.println(String.format("fabric:%s, \t device:%s",  fabricid, deviceIId.firstKeyOf(Node.class).getNodeId()));
        devices.put(fabricid, deviceIId.firstKeyOf(Node.class).getNodeId(), new DeviceContext(databroker, vtep, deviceIId));
    }

    public LogicSwitchContext getLogicSwitchCtx(FabricId fabricid, NodeId nodeId) {
        return logicSwitches.get(fabricid, nodeId);
    }

    public void associateSwitchToRouter(FabricId fabricid, NodeId lsw, NodeId lr, IpPrefix ip) {
        LogicRouterContext routerCtx = getLogicRouterCtx(fabricid, lr);
        LogicSwitchContext switchCtx = getLogicSwitchCtx(fabricid, lsw);
        switchCtx.associateToRouter(routerCtx, ip);

        for (NodeId device : switchCtx.getMembers()) {
        	devices.get(fabricid, device).createBDIF(switchCtx.getVni(), routerCtx);
        }
    }
    
    public DeviceContext getDeviceCtx(FabricId fabricid, NodeId nodeId) {
        return devices.get(fabricid, nodeId);
    }

    public LogicRouterContext getLogicRouterCtx(FabricId fabricid, NodeId routerId) {
        return logicRouters.get(fabricid, routerId);
    }
}