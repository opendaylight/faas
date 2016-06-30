/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FabricContext implements AutoCloseable {

    private final FabricId fabricId;

    private final Map<NodeId, LogicSwitchContext> logicSwitches = Maps.newConcurrentMap();

    private final Map<DeviceKey, DeviceContext> devices = Maps.newConcurrentMap();

    private final Map<NodeId, LogicRouterContext> logicRouters = Maps.newConcurrentMap();

    private final DataBroker databroker;

    protected final ListeningExecutorService executor;

    public FabricContext(FabricId fabricId, DataBroker databroker) {
        this.fabricId = fabricId;
        this.databroker = databroker;

        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(fabricId.getValue() + " - %d")
                .build();
        this.executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(threadFactory));
    }

    public FabricId getFabricId() {
        return fabricId;
    }

    public void addLogicRouter(NodeId routerId, long vrf) {
        logicRouters.put(routerId, new LogicRouterContext(vrf, databroker));
    }

    public void removeLogicRouter(NodeId routerId) {
        logicRouters.remove(routerId);
    }

    public LogicSwitchContext addLogicSwitch(NodeId nodeId, long vni, boolean external) {
        LogicSwitchContext lswCtx = new LogicSwitchContext(databroker, fabricId, vni, nodeId, executor, external);
        logicSwitches.put(nodeId, lswCtx);
        return lswCtx;
    }

    public LogicSwitchContext removeLogicSwitch(NodeId nodeId) {
        return logicSwitches.remove(nodeId);
    }

    public DeviceContext addDeviceSwitch(InstanceIdentifier<Node> deviceIId, IpAddress vtep) {
        DeviceContext devCtx = new DeviceContext(databroker, vtep, deviceIId, executor);
        devices.put(DeviceKey.newInstance(deviceIId), devCtx);
        return devCtx;
    }

    public DeviceContext removeDeviceSwitch(DeviceKey key) {
        return devices.remove(key);
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

        for (DeviceKey device : switchCtx.getMembers()) {
            devices.get(device).createBdif(switchCtx.getVni(), routerCtx);
        }
    }

    public void unAssociateSwitchToRouter(NodeId lsw, NodeId lr) {
        LogicRouterContext routerCtx = getLogicRouterCtx(lr);
        LogicSwitchContext switchCtx = getLogicSwitchCtx(lsw);

        GatewayPort gwPort = switchCtx.unAssociateToRouter(routerCtx);
        for (DeviceKey device : switchCtx.getMembers()) {
            devices.get(device).removeBdif(switchCtx.getVni(), gwPort);
        }
    }

    public DeviceContext getDeviceCtx(DeviceKey key) {
        return devices.get(key);
    }

    public Collection<DeviceContext> getDeviceCtxs() {
        return devices.values();
    }

    public LogicRouterContext getLogicRouterCtx(NodeId routerId) {
        return logicRouters.get(routerId);
    }

    public boolean isValidLogicSwitch(NodeId nodeid) {
        return logicSwitches.containsKey(nodeid);
    }

    public boolean isValidLogicRouter(NodeId nodeid) {
        return logicRouters.containsKey(nodeid);
    }

    @Override
    public void close() {
        executor.shutdown();
        logicSwitches.clear();
        logicRouters.clear();
        devices.clear();
    }
}
