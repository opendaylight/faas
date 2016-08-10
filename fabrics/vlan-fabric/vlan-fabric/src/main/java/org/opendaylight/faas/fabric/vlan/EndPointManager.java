/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vlan;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.Constants;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPortKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoint.attributes.Location;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.endpoint.rev150930.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPointManager implements AutoCloseable, DataTreeChangeListener<Endpoint> {

    private static final Logger LOG = LoggerFactory.getLogger(EndPointManager.class);

    private final DataBroker databroker;

    private final RpcProviderRegistry rpcRegistry;

    private final FabricContext fabricCtx;

    private final ListenerRegistration<EndPointManager> epListener;

    private static FutureCallback<Void> simpleFutureMonitor = new FutureCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
            // do nothing
        }

        @Override
        public void onFailure(Throwable th) {
            LOG.error("Exception in onDataChanged", th);
        }

    };

    public EndPointManager(DataBroker databroker, RpcProviderRegistry rpcRegistry, FabricContext fabricCtx) {
        this.databroker = databroker;
        this.rpcRegistry = rpcRegistry;
        this.fabricCtx = fabricCtx;

        DataTreeIdentifier<Endpoint> dtid = new DataTreeIdentifier<Endpoint>(LogicalDatastoreType.OPERATIONAL,
                Constants.DOM_ENDPOINTS_PATH.child(Endpoint.class));
        epListener = databroker.registerDataTreeChangeListener(dtid, this);
    }

    @Override
    public void close() {
        epListener.close();
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Endpoint>> changes) {

        for (DataTreeModification<Endpoint> change : changes) {
            switch (change.getRootNode().getModificationType()) {
                case DELETE: {
                    final Endpoint ep = change.getRootNode().getDataBefore();
                    final InstanceIdentifier<Endpoint> iid = change.getRootPath().getRootIdentifier();
                    if (ep.getLocation() != null && fabricCtx.getFabricId().equals(ep.getOwnFabric())) {
                        Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                removeEndPointIId(iid, ep);
                                return null;
                            }
                        }), simpleFutureMonitor, fabricCtx.executor);
                    }
                    break;
                }
                case WRITE: {
                    final Endpoint ep = change.getRootNode().getDataAfter();
                    if (ep != null) {
                        if (ep.getLocation() != null && fabricCtx.getFabricId().equals(ep.getOwnFabric())) {
                            Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>() {

                                @Override
                                public Void call() throws Exception {
                                    rendererEndpoint(ep);
                                    return null;
                                }
                            }), simpleFutureMonitor, fabricCtx.executor);
                        }
                    }
                    break;
                }
                case SUBTREE_MODIFIED: {
                    final Endpoint newEp = change.getRootNode().getDataAfter();
                    DataObjectModification<Location> loc = change.getRootNode().getModifiedChildContainer(Location.class);
                    if (loc != null && loc.getModificationType().equals(ModificationType.WRITE)) {
                        if (fabricCtx.getFabricId().equals(newEp.getOwnFabric())) {
                            Futures.addCallback(fabricCtx.executor.submit(new Callable<Void>() {

                                @Override
                                public Void call() throws Exception {
                                    rendererEndpoint(newEp);
                                    return null;
                                }
                            }), simpleFutureMonitor, fabricCtx.executor);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private void removeEndPointIId(final InstanceIdentifier<Endpoint> epIId, Endpoint ep) {

        if (ep.getLogicalLocation() != null) {
            @SuppressWarnings("unchecked")
            InstanceIdentifier<TerminationPoint> lportIid = (InstanceIdentifier<TerminationPoint>) ep.getLocation().getTpRef().getValue();
            EpAccessPortRenderer portRender = EpAccessPortRenderer.newRenderer(databroker, lportIid);
            try {
                portRender.removeEpAccessPort();
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    private void rendererEndpoint(Endpoint ep) throws Exception {

        // 1, create bridge domain port
        NodeId logicNode = ep.getLogicalLocation().getNodeId();

        LogicSwitchContext switchCtx = fabricCtx.getLogicSwitchCtx(logicNode);
        if (switchCtx == null) {
            LOG.warn("There are no such switch's context.({})", logicNode.getValue());
            return;
        }
        final int vlan = switchCtx.getVlan();

        TpId destTpPort = ep.getLocation().getTpRef().getValue().firstKeyOf(TerminationPoint.class).getTpId();
        InstanceIdentifier<Node> devIid = (InstanceIdentifier<Node>) ep.getLocation().getNodeRef().getValue();
        DeviceContext devCtx = fabricCtx.getDeviceCtx(DeviceKey.newInstance(devIid));
        String bdPortId = devCtx.createBdPort(vlan,
                destTpPort, ep.getLocation().getAccessType(), ep.getLocation().getAccessSegment());

        InstanceIdentifier<BdPort> bdPortIid = devIid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(BdPort.class, new BdPortKey(bdPortId));

        // 2, use bridge domain port to renderer logic port
        WriteTransaction trans = databroker.newWriteOnlyTransaction();

        EpAccessPortRenderer portRender = EpAccessPortRenderer.newRenderer(databroker);
        portRender.createEpAccessPort(trans, ep, bdPortIid);
        MdSalUtils.wrapperSubmit(trans);
    }
}