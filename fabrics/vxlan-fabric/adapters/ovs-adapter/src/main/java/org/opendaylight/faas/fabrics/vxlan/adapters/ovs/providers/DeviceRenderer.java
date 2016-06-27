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
import java.util.concurrent.ThreadFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.MdsalUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.bridge.domain.BdPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.Options;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Acls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Rib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.Members;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
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
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class DeviceRenderer implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceRenderer.class);

    private DeviceContext ctx;

    private Openflow13Provider openflow13Provider;

    private FabricId fabricId;

    private ExecutorService executor;
    private final DataBroker databroker;

    private ListenerRegistration<DataChangeListener> hostRouteListener = null;
    private ListenerRegistration<DataChangeListener> bridgeDomainListener = null;
    private ListenerRegistration<DataChangeListener> bdifListener = null;
    private ListenerRegistration<DataChangeListener> vtepMembersListener = null;
    private ListenerRegistration<DataChangeListener> bridgeDomainAclListener = null;
    private ListenerRegistration<DataChangeListener> bridgePortAclListener = null;
    private ListenerRegistration<DataChangeListener> bdPortListener = null;
    private ListenerRegistration<DataChangeListener> ribRouteListener = null;
    private ListenerRegistration<DataChangeListener> portFunctionListener = null;

    public DeviceRenderer(ExecutorService exector, DataBroker databroker, InstanceIdentifier<Node> iid, Node node,
            FabricId fabricId) {

        ThreadFactory threadFact = new ThreadFactoryBuilder().setNameFormat("ova-vxlan-adapter-%d").build();
        this.executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(threadFact));

        this.databroker = databroker;

        this.fabricId = fabricId;

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

        InstanceIdentifier<Members> vtepMembersIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(VniMembers.class).child(Members.class);
        vtepMembersListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, vtepMembersIId,
                this, DataChangeScope.BASE);

        // Acl function in the scope of Bridge Domain
        InstanceIdentifier<FabricAcl> bridgeDomainAclIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(Acls.class).child(FabricAcl.class);
        bridgeDomainAclListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                bridgeDomainAclIId, this, DataChangeScope.BASE);

        // Acl function in the scope of Bridge Port
        // InstanceIdentifier<FabricAcl> bridgePortAclIId =
        // iid.child(TerminationPoint.class)
        // .augmentation(BridgeDomainPort.class).child(FabricAcl.class);
        // bridgePortAclListener =
        // databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
        // bridgePortAclIId, this, DataChangeScope.BASE);
        InstanceIdentifier<FabricAcl> bdPortAclIId = iid.augmentation(FabricCapableDevice.class).child(Config.class)
                .child(BridgeDomain.class).child(BdPort.class).child(FabricAcl.class);
        bridgePortAclListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, bdPortAclIId,
                this, DataChangeScope.BASE);

        // If Bridge Domain Port has access tag info, do vlan vni mapping
        // InstanceIdentifier<BridgeDomainPort> bridgeDomainPortIID =
        // iid.child(TerminationPoint.class)
        // .augmentation(BridgeDomainPort.class);
        // bridgeDomainPortListener =
        // databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
        // bridgeDomainPortIID, this, DataChangeScope.BASE);

        InstanceIdentifier<BdPort> bdPortIID = iid.augmentation(FabricCapableDevice.class).child(Config.class)
                .child(BridgeDomain.class).child(BdPort.class);
        bdPortListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, bdPortIID, this,
                DataChangeScope.BASE);

        InstanceIdentifier<Route> routeIID = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(Rib.class).child(Route.class);
        ribRouteListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, routeIID, this,
                DataChangeScope.BASE);

        InstanceIdentifier<PortFunction> portFunctionIID = iid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(Bdif.class).child(PortFunction.class);
        portFunctionListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, portFunctionIID,
                this, DataChangeScope.BASE);

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
                    ctx.setTrafficBehavior(behavior == null ? TrafficBehavior.Normal : behavior);
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
        if (vtepMembersListener != null) {
            vtepMembersListener.close();
        }
        if (bridgeDomainAclListener != null) {
            bridgeDomainAclListener.close();
        }
        if (bridgePortAclListener != null) {
            bridgePortAclListener.close();
        }
        if (bdPortListener != null) {
            bdPortListener.close();
        }
        if (ribRouteListener != null) {
            ribRouteListener.close();
        }
        if (portFunctionListener != null) {
            portFunctionListener.close();
        }
        executor.shutdownNow();
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

        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject oldData = change.getOriginalData().get(iid);
            onDataRemoved(iid, oldData);
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
        } else if (entry.getValue() instanceof Members) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Members> vtepMembersIid = (InstanceIdentifier<Members>) entry.getKey();
            final Members newRec = (Members) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onVtepMembersCreate(vtepMembersIid, newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof FabricAcl) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<FabricAcl> fabricAclIid = (InstanceIdentifier<FabricAcl>) entry.getKey();
            final FabricAcl newRec = (FabricAcl) entry.getValue();

            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onFabricAclCreate(fabricAclIid, newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof BdPort) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<BdPort> bdPortIid = (InstanceIdentifier<BdPort>) entry.getKey();
            final BdPort newRec = (BdPort) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBdPortCreate(bdPortIid, newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof Route) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Route> routeIid = (InstanceIdentifier<Route>) entry.getKey();
            final Route newRec = (Route) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onRouteCreate(routeIid, newRec);
                    return null;
                }
            });
        } else if (entry.getValue() instanceof PortFunction) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<PortFunction> portFunctionIid = (InstanceIdentifier<PortFunction>) entry.getKey();
            final PortFunction newRec = (PortFunction) entry.getValue();
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onPortFunctionCreate(portFunctionIid, newRec);
                    return null;
                }
            });
        }

    }

    private void onDataUpdated(Entry<InstanceIdentifier<?>, DataObject> entry) {
        LOG.error("No need to support modify!!!");
    }

    private void onDataRemoved(InstanceIdentifier<?> iid, DataObject entry) {

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
        } else if (entry instanceof Members) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Members> vtepMembersIid = (InstanceIdentifier<Members>) iid;
            final Members newRec = (Members) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onVtepMembersDelete(vtepMembersIid, newRec);
                    return null;
                }
            });
        } else if (entry instanceof FabricAcl) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<FabricAcl> fabricAclIid = (InstanceIdentifier<FabricAcl>) iid;
            final FabricAcl newRec = (FabricAcl) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onFabricAclDelete(fabricAclIid, newRec);
                    return null;
                }
            });
        } else if (entry instanceof BdPort) {

            @SuppressWarnings("unchecked")
            final InstanceIdentifier<BdPort> bdPortIid = (InstanceIdentifier<BdPort>) iid;
            final BdPort newRec = (BdPort) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onBdPortDelete(bdPortIid, newRec);
                    return null;
                }
            });

        } else if (entry instanceof Route) {

            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Route> routeIid = (InstanceIdentifier<Route>) iid;
            final Route newRec = (Route) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onRouteDelete(routeIid, newRec);
                    return null;
                }
            });

        } else if (entry instanceof PortFunction) {

            @SuppressWarnings("unchecked")
            final InstanceIdentifier<PortFunction> portFunctionIid = (InstanceIdentifier<PortFunction>) iid;
            final PortFunction newRec = (PortFunction) entry;
            executor.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    onPortFunctionDelete(portFunctionIid, newRec);
                    return null;
                }
            });

        }

    }

    private void onHostRouteCreate(HostRoute newRec) {
        Long dpid = ctx.getDpid();

        Long gpeTunnelOfPort = ctx.getGpe_vtep_ofPort();
        if (gpeTunnelOfPort == null) {
            gpeTunnelOfPort = OvsSouthboundUtils.getVxlanGpeTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(),
                    databroker);
            if (gpeTunnelOfPort != null) {
                ctx.setGpe_vtep_ofPort(gpeTunnelOfPort);
            }
        }

        if (ctx.getVtep().equals(newRec.getDestVtep())) {
            Long ofPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getDestBridgePort(), databroker);
            Long vlanId = null;
            if (ofPort != null) {

//                BridgeDomainPort bdPort = OvsSouthboundUtils.getBridgeDomainPort(ctx.getMyIId(),
//                        newRec.getDestBridgePort(), databroker);
//                if (bdPort != null) {
//                    if (bdPort.getAccessType() == AccessType.Vlan)
//                        vlanId = bdPort.getAccessTag();
//                }
                if (newRec.getAccessType() == AccessType.Vlan) {
                    vlanId = newRec.getAccessTag();
                }
                openflow13Provider.updateLocalHostRouteInDevice(dpid, ofPort, gpeTunnelOfPort, vlanId, newRec, true);
            }
        } else {
            Long tunnelOfPort = ctx.getVtep_ofPort();
            if (tunnelOfPort == null) {
                tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);
                if (tunnelOfPort != null) {
                    ctx.setVtep_ofPort(tunnelOfPort);
                }
            }

            if (tunnelOfPort != null) {
                openflow13Provider.updateRemoteHostRouteInDevice(dpid, tunnelOfPort, gpeTunnelOfPort, newRec, true);
            }
        }
    }

    private void onHostRouteDelete(HostRoute newRec) {
        Long dpid = ctx.getDpid();

        Long gpeTunnelOfPort = ctx.getGpe_vtep_ofPort();
        if (gpeTunnelOfPort == null) {
            gpeTunnelOfPort = OvsSouthboundUtils.getVxlanGpeTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(),
                    databroker);
            if (gpeTunnelOfPort != null) {
                ctx.setGpe_vtep_ofPort(gpeTunnelOfPort);
            }
        }

        if (ctx.getVtep().equals(newRec.getDestVtep())) {
            Long ofPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getDestBridgePort(), databroker);
            Long vlanId = null;
            if (ofPort != null) {
//                BridgeDomainPort bdPort = OvsSouthboundUtils.getBridgeDomainPort(ctx.getMyIId(),
//                        newRec.getDestBridgePort(), databroker);
//                if (bdPort != null) {
//                    if (bdPort.getAccessType() == AccessType.Vlan)
//                        vlanId = bdPort.getAccessTag();
//                }
                if (newRec.getAccessType() == AccessType.Vlan) {
                    vlanId = newRec.getAccessTag();
                }
                openflow13Provider.updateLocalHostRouteInDevice(dpid, ofPort, gpeTunnelOfPort, vlanId, newRec, false);
            }
        } else {
            Long tunnelOfPort = ctx.getVtep_ofPort();
            if (tunnelOfPort == null) {
                tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);
                if (tunnelOfPort != null)
                    ctx.setVtep_ofPort(tunnelOfPort);
            }

            if (tunnelOfPort != null) {
                openflow13Provider.updateRemoteHostRouteInDevice(dpid, tunnelOfPort, gpeTunnelOfPort, newRec, false);
            }
        }
    }

    private void onBDIFCreate(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        Long dpid = ctx.getDpid();

        AdapterBdIf newAdapterBdIf = new AdapterBdIf(newRec, vni);

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        openflow13Provider.updateBdifInDevice(dpid, bdIfs, newAdapterBdIf, true);

        ctx.addBdifToCache(newAdapterBdIf);
    }

    private void onBDIFDelete(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        Long dpid = ctx.getDpid();

        AdapterBdIf newAdapterBdIf = new AdapterBdIf(newRec, vni);

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        openflow13Provider.updateBdifInDevice(dpid, bdIfs, newAdapterBdIf, false);

        ctx.deleteBdifFromCache(newAdapterBdIf);
    }

    private void onBridgeDomainCreate(BridgeDomain newRec) {
        Long dpid = ctx.getDpid();
        Long segmentationId = newRec.getAugmentation(BridgeDomain1.class).getVni();

        Long tunnelOfPort = ctx.getVtep_ofPort();
        if (tunnelOfPort == null) {
            tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);
        }
        if (tunnelOfPort != null) {
            ctx.setVtep_ofPort(tunnelOfPort);
            openflow13Provider.updateBridgeDomainInDevice(dpid, tunnelOfPort, segmentationId, true);
        }

        Long gpeTunnelOfPort = ctx.getGpe_vtep_ofPort();
        if (gpeTunnelOfPort == null) {
            gpeTunnelOfPort = OvsSouthboundUtils.getVxlanGpeTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(),
                    databroker);
        }
        if (gpeTunnelOfPort != null) {
            ctx.setGpe_vtep_ofPort(gpeTunnelOfPort);
            openflow13Provider.updateBridgeDomainInDevice(dpid, gpeTunnelOfPort, segmentationId, true);
            openflow13Provider.updateSfcTunnelInDevice(dpid, gpeTunnelOfPort, segmentationId, true);
        }

    }

    private void onBridgeDomainDelete(BridgeDomain newRec) {
        Long dpid = ctx.getDpid();
        Long segmentationId = newRec.getAugmentation(BridgeDomain1.class).getVni();

        Long tunnelOfPort = ctx.getVtep_ofPort();
        if (tunnelOfPort == null) {
            tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);
        }
        if (tunnelOfPort != null) {
            ctx.setVtep_ofPort(tunnelOfPort);
            openflow13Provider.updateBridgeDomainInDevice(dpid, tunnelOfPort, segmentationId, false);
        }

        Long gpeTunnelOfPort = ctx.getGpe_vtep_ofPort();
        if (gpeTunnelOfPort == null) {
            gpeTunnelOfPort = OvsSouthboundUtils.getVxlanGpeTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(),
                    databroker);
        }
        if (gpeTunnelOfPort != null) {
            ctx.setGpe_vtep_ofPort(gpeTunnelOfPort);
            openflow13Provider.updateBridgeDomainInDevice(dpid, gpeTunnelOfPort, segmentationId, false);
            openflow13Provider.updateSfcTunnelInDevice(dpid, gpeTunnelOfPort, segmentationId, false);
        }

    }

    private void onVtepMembersCreate(InstanceIdentifier<Members> iid, Members newRec) {
        Long dpid = ctx.getDpid();
        Long tunnelOfPort = ctx.getVtep_ofPort();
        if (tunnelOfPort == null) {
            tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);

            if (tunnelOfPort != null) {
                ctx.setVtep_ofPort(tunnelOfPort);
            } else {
                return;
            }
        }
        Long segmentationId = iid.firstKeyOf(VniMembers.class).getVni();

        IpAddress dstTunIp = newRec.getVtep();

        if (!dstTunIp.equals(ctx.getVtep())) {
            openflow13Provider.updateVniMembersInDevice(dpid, tunnelOfPort, segmentationId, dstTunIp, true);
        }
    }

    private void onVtepMembersDelete(InstanceIdentifier<Members> iid, Members newRec) {
        Long dpid = ctx.getDpid();
        Long tunnelOfPort = ctx.getVtep_ofPort();
        if (tunnelOfPort == null) {
            tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(ctx.getMyIId(), ctx.getBridgeName(), databroker);

            if (tunnelOfPort != null) {
                ctx.setVtep_ofPort(tunnelOfPort);
            } else {
                return;
            }
        }
        Long segmentationId = iid.firstKeyOf(VniMembers.class).getVni();

        IpAddress dstTunIp = newRec.getVtep();

        if (!dstTunIp.equals(ctx.getVtep())) {
            openflow13Provider.updateVniMembersInDevice(dpid, tunnelOfPort, segmentationId, dstTunIp, false);
        }
    }

    private void onFabricAclCreate(InstanceIdentifier<FabricAcl> iid, FabricAcl newRec) {
        if (iid.firstKeyOf(Acls.class) != null) {
            Long dpid = ctx.getDpid();
            Long segmentationId = iid.firstKeyOf(Acls.class).getVni();

            openflow13Provider.updateBridgeDomainAclsInDevice(dpid, segmentationId, newRec, true);
        } else if (iid.firstKeyOf(BdPort.class) != null) {
            Long dpid = ctx.getDpid();

            InstanceIdentifier<BdPort> bdPortIid = InstanceIdentifier.create(FabricCapableDevice.class)
                    .child(Config.class).child(BridgeDomain.class).child(BdPort.class, iid.firstKeyOf(BdPort.class));

            BdPort bdport = MdsalUtils.read(LogicalDatastoreType.OPERATIONAL, bdPortIid, databroker);

            Long bridgeInPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), bdport.getRefTpId(), databroker);
            if (bridgeInPort != null) {
                openflow13Provider.updateBridgePortAclsInDevice(dpid, bridgeInPort, newRec, true);
            } else {
                return;
            }

        }
    }

    private void onFabricAclDelete(InstanceIdentifier<FabricAcl> iid, FabricAcl newRec) {
        if (iid.firstKeyOf(Acls.class) != null) {
            Long dpid = ctx.getDpid();
            Long segmentationId = iid.firstKeyOf(Acls.class).getVni();

            openflow13Provider.updateBridgeDomainAclsInDevice(dpid, segmentationId, newRec, false);
        } else if (iid.firstKeyOf(TerminationPoint.class) != null) {
            Long dpid = ctx.getDpid();

            InstanceIdentifier<BdPort> bdPortIid = InstanceIdentifier.create(FabricCapableDevice.class)
                    .child(Config.class).child(BridgeDomain.class).child(BdPort.class, iid.firstKeyOf(BdPort.class));

            BdPort bdport = MdsalUtils.read(LogicalDatastoreType.OPERATIONAL, bdPortIid, databroker);

            Long bridgeInPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), bdport.getRefTpId(), databroker);

            if (bridgeInPort != null) {
                openflow13Provider.updateBridgePortAclsInDevice(dpid, bridgeInPort, newRec, false);
            } else {
                return;
            }
        }
    }

    private void onBdPortCreate(InstanceIdentifier<BdPort> iid, BdPort newRec) {
        Long dpid = ctx.getDpid();
        Long ofInPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getRefTpId(), databroker);

        if (ofInPort != null) {
            Long vni = OvsSouthboundUtils.getBdPortVni(ctx.getMyIId(), iid.firstKeyOf(BridgeDomain.class), databroker);

            if (vni != null) {
                openflow13Provider.updateBdPortInDevice(dpid, ofInPort, vni, newRec, true);
            }
        }
    }

    private void onBdPortDelete(InstanceIdentifier<BdPort> iid, BdPort newRec) {
        Long dpid = ctx.getDpid();
        Long ofInPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getRefTpId(),
                databroker);

        if (ofInPort != null) {
            Long vni = OvsSouthboundUtils.getBdPortVni(ctx.getMyIId(), iid.firstKeyOf(BridgeDomain.class), databroker);

            if (vni != null) {
                openflow13Provider.updateBdPortInDevice(dpid, ofInPort, vni, newRec, false);
            }
        }
    }

    private void onRouteCreate(InstanceIdentifier<Route> iid, Route newRec) {
        Long dpid = ctx.getDpid();

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        for (AdapterBdIf bdIf : bdIfs) {
            String bdifMac = bdIf.getMacAddress().getValue();
            if (bdifMac != null) {
                openflow13Provider.updateRouteInDevice(dpid, bdifMac, newRec, true);
            }
        }
    }

    private void onRouteDelete(InstanceIdentifier<Route> iid, Route newRec) {
        Long dpid = ctx.getDpid();

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        for (AdapterBdIf bdIf : bdIfs) {
            String bdifMac = bdIf.getMacAddress().getValue();
            if (bdifMac != null) {
                openflow13Provider.updateRouteInDevice(dpid, bdifMac, newRec, false);
            }
        }
    }

    private void onPortFunctionCreate(InstanceIdentifier<PortFunction> iid, PortFunction newRec) {
        Long dpid = ctx.getDpid();
        Long segmentationId = null;

        if (iid.firstKeyOf(Bdif.class) != null) {
            Bdif bdif = (Bdif) iid.firstKeyOf(Bdif.class);
            segmentationId = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bdif.getBdid(), databroker);
        }

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        for (AdapterBdIf bdIf : bdIfs) {
            String bdifMac = bdIf.getMacAddress().getValue();
            openflow13Provider.updatePortFunctionInDevice(dpid, segmentationId, bdifMac, newRec, true);
        }
    }

    private void onPortFunctionDelete(InstanceIdentifier<PortFunction> iid, PortFunction newRec) {
        Long dpid = ctx.getDpid();
        Long segmentationId = null;

        if (iid.firstKeyOf(Bdif.class) != null) {
            Bdif bdif = (Bdif) iid.firstKeyOf(Bdif.class);
            segmentationId = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bdif.getBdid(), databroker);
        }

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        for (AdapterBdIf bdIf : bdIfs) {
            String bdifMac = bdIf.getMacAddress().getValue();
            openflow13Provider.updatePortFunctionInDevice(dpid, segmentationId, bdifMac, newRec, false);
        }
    }

}
