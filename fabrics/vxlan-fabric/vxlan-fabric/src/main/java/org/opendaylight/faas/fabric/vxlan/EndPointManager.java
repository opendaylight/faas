/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.Constants;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.CreateBridgeDomainPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.CreateBridgeDomainPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.FabricVxlanDeviceAdapterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRouteKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class EndPointManager implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(EndPointManager.class);

    private final DataBroker databroker;

    private final RpcProviderRegistry rpcRegistry;

    private final FabricContext fabricCtx;

    private final ListenerRegistration<DataChangeListener> epListener;

    private static FutureCallback<Void> simpleFutureMonitor = new FutureCallback<Void> () {

        @Override
        public void onSuccess(Void result) {
            // do nothing
        }

        @Override
        public void onFailure(Throwable t) {
            LOG.error("Exception in onDataChanged", t);
        }

    };

    public EndPointManager (DataBroker databroker, RpcProviderRegistry rpcRegistry, FabricContext fabricCtx) {
        this.databroker = databroker;
        this.rpcRegistry = rpcRegistry;
        this.fabricCtx = fabricCtx;

        epListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, Constants.DOM_ENDPOINTS_PATH.child(Endpoint.class), this, DataChangeScope.ONE);
    }

    public void removeEndPointIId(final InstanceIdentifier<Endpoint> epIId, Endpoint ep) {
        InstanceIdentifier<HostRoute> hostRouteIId = createHostRouteIId(fabricCtx.getFabricId(), ep.getEndpointUuid());
        WriteTransaction wt = databroker.newWriteOnlyTransaction();
        wt.delete(LogicalDatastoreType.OPERATIONAL, hostRouteIId);
        MdSalUtils.wrapperSubmit(wt);
    }

    private void rendererEndpoint(Endpoint ep) throws Exception {

        // 1, create bridge domain port
        CreateBridgeDomainPortInputBuilder builder = new CreateBridgeDomainPortInputBuilder();
        builder.setNodeId(ep.getLocation().getNodeRef().getValue());
        builder.setTpId(ep.getLocation().getTpRef().getValue().firstKeyOf(TerminationPoint.class).getTpId());
        builder.setAccessType(ep.getLocation().getAccessType());
        builder.setAccessTag(ep.getLocation().getAccessSegment());
        RpcResult<CreateBridgeDomainPortOutput> rpcResult = getVlanDeviceAdapter().createBridgeDomainPort(builder.build()).get();

        TpId bridgeDomainPort = rpcResult.getResult().getBridgeDomainPort();

        // 2, use bridge domain port to renderer logic port
        WriteTransaction trans = databroker.newWriteOnlyTransaction();

        EpAccessPortRenderer portRender = EpAccessPortRenderer.newCreateTask(databroker);
        portRender.createEpAccessPort(trans, ep, bridgeDomainPort);

        // 3, write host route
        InstanceIdentifier<HostRoute> hostRouteIId = createHostRouteIId(fabricCtx.getFabricId(), ep.getEndpointUuid());

        HostRouteBuilder hrBuilder = new HostRouteBuilder();
        if (!buildHostRoute(hrBuilder, ep, bridgeDomainPort)) {
            return;
        }
        trans.merge(LogicalDatastoreType.OPERATIONAL, hostRouteIId, hrBuilder.build(), true);

        MdSalUtils.wrapperSubmit(trans);
    }

    private boolean buildHostRoute(HostRouteBuilder builder, Endpoint ep, TpId bridgeDomainPort) {
        // VNI
        NodeId logicNode = ep.getLogicalLocation().getNodeId();

        LogicSwitchContext switchCtx = fabricCtx.getLogicSwitchCtx(logicNode);
        if (switchCtx == null) {
            LOG.warn("There are no such switch's context.({})", logicNode.getValue());
            return false;
        }
        long vni = switchCtx.getVni();

        // ip
        IpAddress ip = ep.getIpAddress();

        // dest-vtep
        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> destNodeIId = (InstanceIdentifier<Node>) ep.getLocation().getNodeRef().getValue();
        DeviceKey dkey = DeviceKey.newInstance(destNodeIId);
        DeviceContext devCtx = fabricCtx.getDeviceCtx(dkey);
        IpAddress vtepIp = devCtx.getVtep();
        if (switchCtx.checkAndSetNewMember(dkey, vtepIp)) {
            devCtx.createBridgeDomain(switchCtx);
        }

        // dest-bridge-port
        builder.setHostid(ep.getEndpointUuid());
        builder.setMac(ep.getMacAddress());
        builder.setVni(vni);
        builder.setIp(ip);
        builder.setDestVtep(vtepIp);
        builder.setDestBridgePort(bridgeDomainPort);
        builder.setAccessType(ep.getLocation().getAccessType());
        builder.setAccessTag(ep.getLocation().getAccessSegment());

        return true;
    }

    private InstanceIdentifier<HostRoute> createHostRouteIId(FabricId fabricId, Uuid hostid) {
        return InstanceIdentifier.create(FabricRenderedMapping.class).child(Fabric.class, new FabricKey(fabricId))
                .child(HostRoute.class, new HostRouteKey(hostid));
    }

    @Override
    public void close() {
        epListener.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();
        for (Entry<InstanceIdentifier<?>, DataObject> entry : createdData.entrySet()) {
            if (entry.getKey().getTargetType().equals(Endpoint.class)) {

                final Endpoint ep = (Endpoint) entry.getValue();
                if (ep.getLocation() != null && fabricCtx.getFabricId().equals(ep.getOwnFabric())) {
                    Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>(){

                        @Override
                        public Void call() throws Exception {
                            rendererEndpoint(ep);
                            return null;
                        }
                    }), simpleFutureMonitor, fabricCtx.executor);
                }
            }
        }

        Map<InstanceIdentifier<?>, DataObject> updatedData = change.getUpdatedData();
        for (Entry<InstanceIdentifier<?>, DataObject> entry : updatedData.entrySet()) {
            if (entry.getKey().getTargetType().equals(Endpoint.class)) {

                final Endpoint ep = (Endpoint) entry.getValue();
                if (ep.getLocation() != null && fabricCtx.getFabricId().equals(ep.getOwnFabric())) {
                    Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>(){

                        @Override
                        public Void call() throws Exception {
                            rendererEndpoint(ep);
                            return null;
                        }
                    }), simpleFutureMonitor, fabricCtx.executor);
                }
            }
        }

        for (final InstanceIdentifier<?> iid: change.getRemovedPaths()) {
            if (iid.getTargetType().equals(Endpoint.class)) {
                DataObject oldData = change.getOriginalData().get(iid);
                final Endpoint ep = (Endpoint) oldData;
                if (ep.getLocation() != null && fabricCtx.getFabricId().equals(ep.getOwnFabric())) {
                    Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            removeEndPointIId((InstanceIdentifier<Endpoint>) iid, ep);
                            return null;
                        }
                    }), simpleFutureMonitor, fabricCtx.executor);
                }
            }
        }
    }

    private FabricVxlanDeviceAdapterService getVlanDeviceAdapter() {
        return rpcRegistry.getRpcService(FabricVxlanDeviceAdapterService.class);
    }
}
