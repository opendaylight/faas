/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.Options;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Acls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class DeviceRenderer implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceRenderer.class);

    private DeviceContext ctx;

    private Openflow13Provider openflow13Provider;

    private FabricId fabricId;

    private NodeId topoNodeId;

    private ExecutorService executor;
    private final DataBroker databroker;

    private ListenerRegistration<DataChangeListener> hostRouteListener = null;
    private ListenerRegistration<DataChangeListener> bridgeDomainListener = null;
    private ListenerRegistration<DataChangeListener> bdifListener = null;
    private ListenerRegistration<DataChangeListener> vniMembersListener = null;
    private ListenerRegistration<DataChangeListener> aclsListener = null;

    public DeviceRenderer(ExecutorService exector, DataBroker databroker, InstanceIdentifier<Node> iid, Node node,
            FabricId fabricId) {
        this.executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor());
        ;
        this.databroker = databroker;

        this.fabricId = fabricId;
        this.topoNodeId = node.getNodeId();

        ctx = new DeviceContext(node, iid);

        // Initialize openflow pipeline in this Device
        openflow13Provider = new Openflow13Provider(databroker);
        openflow13Provider.initialOpenflowPipeline(node);

        InstanceIdentifier<HostRoute> hostRouteIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(HostRoute.class);
        hostRouteListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, hostRouteIId, this,
                DataChangeScope.BASE);

        InstanceIdentifier<BridgeDomain> bridgeDomainIId = iid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(BridgeDomain.class);
        bridgeDomainListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, bridgeDomainIId,
                this, DataChangeScope.BASE);

        InstanceIdentifier<Bdif> bdifIId = iid.augmentation(FabricCapableDevice.class).child(Config.class)
                .child(Bdif.class);
        bdifListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, bdifIId, this,
                DataChangeScope.BASE);

        InstanceIdentifier<VniMembers> vniMembersIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(VniMembers.class);
        vniMembersListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, vniMembersIId,
                this, DataChangeScope.SUBTREE);

        InstanceIdentifier<Acls> aclsIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(Acls.class);
        aclsListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, aclsIId, this,
                DataChangeScope.SUBTREE);

        readFabricOptions(node);
    }

    private void readFabricOptions(final Node node) {
        ReadOnlyTransaction trans = databroker.newReadOnlyTransaction();
        InstanceIdentifier<Options> iid = MdSalUtils.createFabricIId(this.fabricId).child(FabricAttribute.class)
                .child(Options.class);

        ListenableFuture<Optional<Options>> future = trans.read(LogicalDatastoreType.OPERATIONAL, iid);
        Futures.addCallback(future, new FutureCallback<Optional<Options>>() {

            @Override
            public void onSuccess(Optional<Options> result) {
                if (result.isPresent()) {
                    Options opt = result.get();
                    TrafficBehavior behavior = opt.getTrafficBehavior();
                    ctx.setTrafficBehavior(behavior == null ? behavior : TrafficBehavior.Normal);
                } else {
                    ctx.setTrafficBehavior(TrafficBehavior.Normal);
                }

                // Set Traffic Behavior in Pipeline table 90, Acl table
                openflow13Provider.updateTrafficBehavior(ctx.getDpid(), ctx.getTrafficBehavior(), true);

            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("unexcepted exception", t);
            }
        }, executor);
    }

    @Override
    public void close() {
        if (hostRouteListener != null) {
            hostRouteListener.close();
        }
        if (bridgeDomainListener != null) {
            bridgeDomainListener.close();
        }
        if (bdifListener != null) {
            bdifListener.close();
        }
        if (vniMembersListener != null) {
            vniMembersListener.close();
        }
        if (aclsListener != null) {
            aclsListener.close();
        }
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();
        for (Entry<InstanceIdentifier<?>, DataObject> entry : createdData.entrySet()) {
            onDataCreated(entry);
        }

        Map<InstanceIdentifier<?>, DataObject> updatedData = change.getUpdatedData();
        for (Entry<InstanceIdentifier<?>, DataObject> entry : updatedData.entrySet()) {
            onDataUpdated(entry);
        }

        for (InstanceIdentifier<?> iid: change.getRemovedPaths()) {
            DataObject oldData = change.getOriginalData().get(iid);
            onDataRemoved(oldData);
        }

    }


    private void onDataCreated(Entry<InstanceIdentifier<?>, DataObject> entry) {
        if (entry.getValue() instanceof HostRoute) {
            final HostRoute newRec = (HostRoute) entry.getValue();

            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onHostRouteCreate(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof BridgeDomain) {
            final BridgeDomain newRec = (BridgeDomain) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBridgeDomainCreate(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof Bdif) {
            final Bdif newRec = (Bdif) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBDIFCreate(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof VniMembers) {
            final VniMembers newRec = (VniMembers) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onVniMembersCreate(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof Acls) {
            final Acls newRec = (Acls) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onAclsCreate(newRec);
                    return null;
                }
            });
        }

    }

    private void onDataUpdated(Entry<InstanceIdentifier<?>, DataObject> entry) {

        if (entry.getValue() instanceof HostRoute) {
            final HostRoute newRec = (HostRoute) entry.getValue();

            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onHostRouteModified(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof BridgeDomain) {
            final BridgeDomain newRec = (BridgeDomain) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBridgeDomainModified(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof Bdif) {
            final Bdif newRec = (Bdif) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBDIFModified(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof VniMembers) {
            final VniMembers newRec = (VniMembers) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onVniMembersModified(newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof Acls) {
            final Acls newRec = (Acls) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onAclsModified(newRec);
                    return null;
                }
            });
        }

    }

    private void onDataRemoved(DataObject entry) {

        if (entry instanceof HostRoute) {
            final HostRoute newRec = (HostRoute) entry;

            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onHostRouteDelete(newRec);
                    return null;
                }
            });
        } else if (entry instanceof BridgeDomain) {
            final BridgeDomain newRec = (BridgeDomain) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBridgeDomainDelete(newRec);
                    return null;
                }
            });
        } else if (entry instanceof Bdif) {
            final Bdif newRec = (Bdif) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBDIFDelete(newRec);
                    return null;
                }
            });
        } /*else if (entry instanceof VniMembers) {
            final VniMembers newRec = (VniMembers) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onVniMembersDelete(newRec);
                    return null;
                }
            });
        } else if (entry instanceof Acls) {
            final Acls newRec = (Acls) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onAclsDelete(newRec);
                    return null;
                }
            });
        }*/
    }

    private void onHostRouteCreate(HostRoute newRec) {
        Long dpidLong = ctx.getDpid();

        Long gpeTunnelOfPort = ctx.getGpe_vtep_ofPort();
        if (gpeTunnelOfPort == 0l) {
            gpeTunnelOfPort = OvsSouthboundUtils.getVxlanGpeTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(),
                    databroker);
            if (gpeTunnelOfPort != 0l) {
                ctx.setGpe_vtep_ofPort(gpeTunnelOfPort);
            }
        }

        if (ctx.getVtep().equals(newRec.getDestVtep())) {
            Long ofPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getDestBridgePort(), databroker);
            if (ofPort != 0l) {
                openflow13Provider.updateLocalHostRouteInDevice(dpidLong, ofPort, gpeTunnelOfPort, newRec, true);
            }
        } else {
            Long tunnelOfPort = ctx.getVtep_ofPort();
            if (tunnelOfPort == 0l) {
                tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);
                ctx.setVtep_ofPort(tunnelOfPort);
            }

            if (tunnelOfPort != 0l) {
                openflow13Provider.updateRemoteHostRouteInDevice(dpidLong, tunnelOfPort, gpeTunnelOfPort, newRec, true);
            }
        }
    }

    private void onHostRouteDelete(HostRoute newRec) {
        Long dpidLong = ctx.getDpid();

        Long gpeTunnelOfPort = ctx.getGpe_vtep_ofPort();
        if (gpeTunnelOfPort == 0l) {
            gpeTunnelOfPort = OvsSouthboundUtils.getVxlanGpeTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(),
                    databroker);
            if (gpeTunnelOfPort != 0l) {
                ctx.setGpe_vtep_ofPort(gpeTunnelOfPort);
            }
        }

        if (ctx.getVtep().equals(newRec.getDestVtep())) {
            Long ofPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getDestBridgePort(), databroker);
            if (ofPort != 0l) {
                openflow13Provider.updateLocalHostRouteInDevice(dpidLong, ofPort, gpeTunnelOfPort, newRec, false);
            }
        } else {
            Long tunnelOfPort = ctx.getVtep_ofPort();
            if (tunnelOfPort == 0l) {
                tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);
                ctx.setVtep_ofPort(tunnelOfPort);
            }

            if (tunnelOfPort != 0l) {
                openflow13Provider.updateRemoteHostRouteInDevice(dpidLong, tunnelOfPort, gpeTunnelOfPort, newRec, false);
            }
        }
    }

    private void onHostRouteModified(HostRoute newRec) {
        // Just create new flows
        onHostRouteCreate(newRec);
    }

    private void onBDIFCreate(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        Long dpidLong = ctx.getDpid();

        AdapterBdIf newAdapterBdIf = new AdapterBdIf(newRec, vni);

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        openflow13Provider.updateBdifInDevice(dpidLong, bdIfs, newAdapterBdIf, true);

        ctx.addBdifToCache(newAdapterBdIf);
    }

    private void onBDIFDelete(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        Long dpidLong = ctx.getDpid();

        AdapterBdIf newAdapterBdIf = new AdapterBdIf(newRec, vni);

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        openflow13Provider.updateBdifInDevice(dpidLong, bdIfs, newAdapterBdIf, false);

        ctx.addBdifToCache(newAdapterBdIf);
    }

    private void onBDIFModified(Bdif newRec) {
        // Just create new flows
        onBDIFCreate(newRec);
    }

    private void onBridgeDomainCreate(BridgeDomain newRec) {
        Long dpidLong = ctx.getDpid();
        Long tunnelOfPort = ctx.getVtep_ofPort();
        if (tunnelOfPort == 0l) {
            tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);

            if (tunnelOfPort != 0l) {
                ctx.setVtep_ofPort(tunnelOfPort);
            } else {
                return;
            }
        }

        Long segmentationId = newRec.getAugmentation(BridgeDomain1.class).getVni();

        openflow13Provider.updateBridgeDomainInDevice(dpidLong, tunnelOfPort, segmentationId, true);

    }

    private void onBridgeDomainDelete(BridgeDomain newRec) {
        Long dpidLong = ctx.getDpid();
        Long tunnelOfPort = ctx.getVtep_ofPort();
        if (tunnelOfPort == 0l) {
            tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);

            if (tunnelOfPort != 0l) {
                ctx.setVtep_ofPort(tunnelOfPort);
            } else {
                return;
            }
        }

        Long segmentationId = newRec.getAugmentation(BridgeDomain1.class).getVni();

        openflow13Provider.updateBridgeDomainInDevice(dpidLong, tunnelOfPort, segmentationId, false);
    }

    private void onBridgeDomainModified(BridgeDomain newRec) {
        // Just create new flows
        onBridgeDomainCreate(newRec);
    }

    private void onVniMembersCreate(VniMembers newRec) {
        Long dpidLong = ctx.getDpid();
        Long tunnelOfPort = ctx.getVtep_ofPort();
        if (tunnelOfPort == 0l) {
            tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);

            if (tunnelOfPort != 0l) {
                ctx.setVtep_ofPort(tunnelOfPort);
            } else {
                return;
            }
        }
        Long segmentationId = newRec.getVni();

        for (IpAddress dstTunIp : newRec.getVteps()) {
            if (!dstTunIp.equals(ctx.getVtep())) {
                openflow13Provider.updateVniMembersInDevice(dpidLong, tunnelOfPort, segmentationId, dstTunIp, true);
            }
        }
    }

    private void onVniMembersDelete() {

    }

    private void onVniMembersModified(VniMembers newRec) {
        // Not really implemented the real modify function
        onVniMembersCreate(newRec);
    }

    private void onAclsCreate(Acls newRec) {
        Long dpidLong = ctx.getDpid();

        for (FabricAcl acl : newRec.getFabricAcl()) {
            openflow13Provider.updateAclsInDevice(dpidLong, acl, true);
        }

    }

    private void onAclsDelete() {

    }

    private void onAclsModified(Acls newRec) {
        // Not really implemented the real modify function
        onAclsCreate(newRec);

    }
}
