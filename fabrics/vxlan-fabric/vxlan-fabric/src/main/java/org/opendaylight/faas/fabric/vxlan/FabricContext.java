/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.Maps;

public class FabricContext {

	private final FabricId fabricId;

    private final Map<NodeId, LogicSwitchContext> logicSwitches = Maps.newConcurrentMap();

    private final Map<NodeId, DeviceContext> devices = Maps.newConcurrentMap();

    private final Map<NodeId, LogicRouterContext> logicRouters = Maps.newConcurrentMap();

    private final DataBroker databroker;

    private final ExecutorService executor;

    public FabricContext(FabricId fabricId, DataBroker databroker, ExecutorService executor) {
    	this.fabricId = fabricId;
        this.databroker = databroker;
        this.executor = executor;
    }

    public void addLogicRouter(NodeId routerId, long vrf) {
        logicRouters.put(routerId, new LogicRouterContext(vrf));
    }

    public void removeLogicRouter(NodeId routerId) {
    	logicRouters.remove(routerId);
    }
    
    public LogicSwitchContext addLogicSwitch(NodeId nodeId, long vni) {
    	LogicSwitchContext lswCtx = new LogicSwitchContext(databroker, fabricId, vni, executor);
        logicSwitches.put(nodeId, lswCtx);
        return lswCtx;
    }

    public LogicSwitchContext removeLogicSwitch(NodeId nodeId) {
    	return logicSwitches.remove(nodeId);
    }
    
    public DeviceContext addDeviceSwitch(InstanceIdentifier<Node> deviceIId, IpAddress vtep) {
    	DeviceContext devCtx = new DeviceContext(databroker, vtep, deviceIId, executor);
        devices.put(deviceIId.firstKeyOf(Node.class).getNodeId(), devCtx);
        return devCtx;
    }

    public DeviceContext removeDeviceSwitch(NodeId nodeid) {
    	return devices.remove(nodeid);
    }
    
    public LogicSwitchContext getLogicSwitchCtx(NodeId nodeId) {
        return logicSwitches.get(nodeId);
    }
    
    public Collection<LogicSwitchContext> getLogicSwitchCtxs() {
    	return logicSwitches.values();
    }
    
    public void associateSwitchToRouter(FabricId fabricid, NodeId lsw, NodeId lr, IpPrefix ip) {
        LogicRouterContext routerCtx = getLogicRouterCtx(lr);
        LogicSwitchContext switchCtx = getLogicSwitchCtx(lsw);
        switchCtx.associateToRouter(routerCtx, ip);

        for (NodeId device : switchCtx.getMembers()) {
        	devices.get(device).createBDIF(switchCtx.getVni(), routerCtx);
        }
    }
    
    public DeviceContext getDeviceCtx(NodeId nodeId) {
        return devices.get(nodeId);
    }

    public Collection<DeviceContext> getDeviceCtxs() {
    	return devices.values();
    }
    
    public LogicRouterContext getLogicRouterCtx(NodeId routerId) {
        return logicRouters.get(routerId);
    }
}