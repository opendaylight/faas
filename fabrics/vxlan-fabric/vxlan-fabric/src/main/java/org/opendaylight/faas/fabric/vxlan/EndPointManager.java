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
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class EndPointManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EndPointManager.class);

    private final DataBroker databroker;

    private final ExecutorService executor;

    private final FabricContext fabricCtx;

    public EndPointManager (DataBroker databroker, ExecutorService executor, FabricContext fabricCtx) {
        this.databroker = databroker;
        this.executor = executor;
        this.fabricCtx = fabricCtx;
    }

    public void addEndPointIId(final InstanceIdentifier<Endpoint> epIId) {
    	ReadOnlyTransaction readTrans = databroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Endpoint>,ReadFailedException> readFuture = readTrans.read(LogicalDatastoreType.OPERATIONAL, epIId);

        Futures.addCallback(readFuture, new FutureCallback<Optional<Endpoint>>(){

            @Override
            public void onSuccess(Optional<Endpoint> result) {
                if (result.isPresent()) {
                    Endpoint ep = result.get();
                    FabricId fabricid = null;
                    if (ep.getLogicLocation() != null) {
                        @SuppressWarnings("unchecked")
                        InstanceIdentifier<Node> nodeIId = (InstanceIdentifier<Node>) ep.getLogicLocation().getNodeRef().getValue();
                        fabricid = new FabricId(nodeIId.firstKeyOf(Topology.class).getTopologyId().getValue());
                    }
                    if (ep.getLocation() != null) {
                        if (fabricid != null) {
                            rendererEndpoint(fabricid, ep);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("", t);
            }}, executor);
    }

    private void rendererEndpoint(FabricId fabricid, Endpoint ep) {

        // MAC
        MacAddress mac = ep.getMacAddress();

        // VNI
        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> logicNodeIId = (InstanceIdentifier<Node>) ep.getLogicLocation().getNodeRef().getValue();
        NodeId logicNode = logicNodeIId.firstKeyOf(Node.class).getNodeId();
        LogicSwitchContext switchCtx = fabricCtx.getLogicSwitchCtx(logicNode);
        if (switchCtx == null) {
            LOG.warn("There are no such switch's context.({})", logicNode.getValue());
            return;
        }
        long vni = switchCtx.getVni();

        // ip
        IpAddress ip = ep.getIpAddress();

        // dest-vtep
        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> destNodeIId = (InstanceIdentifier<Node>) ep.getLocation().getNodeRef().getValue();
        NodeId destNodeId = destNodeIId.firstKeyOf(Node.class).getNodeId();
        DeviceContext devCtx = fabricCtx.getDeviceCtx(destNodeId);
        IpAddress vtepIp = devCtx.getVtep();
        if (switchCtx.checkAndSetNewMember(destNodeId, vtepIp)) {
            devCtx.createBridgeDomain(switchCtx);
        }

        // dest-bridge-port
        @SuppressWarnings("unchecked")
        InstanceIdentifier<TerminationPoint> tpIId = (InstanceIdentifier<TerminationPoint>) ep.getLocation().getTpRef().getValue();
        TpId tpid = tpIId.firstKeyOf(TerminationPoint.class).getTpId();

        WriteTransaction trans = databroker.newWriteOnlyTransaction();

        InstanceIdentifier<HostRoute> hostRouteIId = createHostRouteIId(fabricid, mac);

        HostRouteBuilder builder = new HostRouteBuilder();
        builder.setMac(mac);
        builder.setVni(vni);
        builder.setIp(ip);
        builder.setDestVtep(vtepIp);
        builder.setDestBridgePort(tpid);
        builder.setAccessType(ep.getLocation().getAccessType());
        builder.setAccessTag(ep.getLocation().getAccessSegment());

        trans.merge(LogicalDatastoreType.OPERATIONAL, hostRouteIId, builder.build(), true);

        trans.submit();
    }

    private InstanceIdentifier<HostRoute> createHostRouteIId(FabricId fabricId, MacAddress mac) {
        return InstanceIdentifier.create(FabricRenderedMapping.class).child(Fabric.class, new FabricKey(fabricId))
                .child(HostRoute.class, new HostRouteKey(mac));
    }


    @Override
    public void close() throws Exception {

    }
}
