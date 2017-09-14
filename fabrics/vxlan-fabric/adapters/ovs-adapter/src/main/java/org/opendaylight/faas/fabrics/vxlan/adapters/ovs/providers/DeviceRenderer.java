/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiConsumer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.MdsalUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.Options;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.AccessType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.ServiceCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.acl.list.FabricAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.PortFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.port.functions.port.function.function.type.ip.mapping.IpMappingEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.route.next.hop.options.SimpleNextHop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Acls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Rib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.rib.VxlanRouteAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.Members;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceRenderer implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceRenderer.class);

    private final DeviceContext ctx;

    private final Openflow13Provider openflow13Provider;

    private final FabricId fabricId;

    private final ExecutorService executor;
    private final DataBroker databroker;

    private final Collection<ListenerRegistration<?>> listenerRegs = new ArrayList<>();

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
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, hostRouteIId), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onHostRouteCreate(created)),
                    (id, removed) -> executor.execute(() -> onHostRouteDelete(removed)))));

        InstanceIdentifier<BridgeDomain> bridgeDomainIId = iid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(BridgeDomain.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, bridgeDomainIId), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onBridgeDomainCreate(created)),
                    (id, removed) -> executor.execute(() -> onBridgeDomainDelete(removed)))));

        InstanceIdentifier<Bdif> bdifIId = iid.augmentation(FabricCapableDevice.class).child(Config.class)
                .child(Bdif.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, bdifIId), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onBDIFCreate(created)),
                    (id, removed) -> executor.execute(() -> onBDIFDelete(removed)))));

        InstanceIdentifier<Members> vtepMembersIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(VniMembers.class).child(Members.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, vtepMembersIId), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onVtepMembersCreate(id, created)),
                    (id, removed) -> executor.execute(() -> onVtepMembersDelete(id, removed)))));

        // Acl function in the scope of Bridge Domain
        InstanceIdentifier<FabricAcl> bridgeDomainAclIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(Acls.class).child(FabricAcl.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, bridgeDomainAclIId), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onFabricAclCreate(id, created)),
                    (id, removed) -> executor.execute(() -> onFabricAclDelete(id, removed)))));

        // Acl function in the scope of Bridge Port
        InstanceIdentifier<FabricAcl> bdPortAclIId = iid.augmentation(FabricCapableDevice.class).child(Config.class)
                .child(BdPort.class).child(FabricAcl.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, bdPortAclIId), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onFabricAclCreate(id, created)),
                    (id, removed) -> executor.execute(() -> onFabricAclDelete(id, removed)))));

        InstanceIdentifier<BdPort> bdPortIID = iid.augmentation(FabricCapableDevice.class).child(Config.class)
                .child(BdPort.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, bdPortIID), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onBdPortCreate(id, created)),
                    (id, removed) -> executor.execute(() -> onBdPortDelete(id, removed)))));

        InstanceIdentifier<Route> routeIID = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(Rib.class).child(Route.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, routeIID), changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onRouteCreate(id, created)),
                    (id, removed) -> executor.execute(() -> onRouteDelete(id, removed)))));

        InstanceIdentifier<IpMappingEntry> ipMappingIID = iid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(Bdif.class).child(PortFunction.class).child(IpMappingEntry.class);
        listenerRegs.add(databroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, ipMappingIID),changes -> onDataTreeChanged(changes,
                    (id, created) -> executor.execute(() -> onIpMappingCreate(id, created)),
                    (id, removed) -> executor.execute(() -> onIpMappingDelete(id, removed)))));

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

                    List<ServiceCapabilities> supportedCapabilities = opt.getCapabilitySupported();
                    for (ServiceCapabilities capability : supportedCapabilities) {
                        if (capability.equals(ServiceCapabilities.AclRedirect)) {
                            ctx.setAclRedirectCapability(true);
                            break;
                        } else {
                            ctx.setAclRedirectCapability(false);
                        }
                    }
                } else {
                    ctx.setTrafficBehavior(TrafficBehavior.Normal);
                    ctx.setAclRedirectCapability(false);
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
        for (ListenerRegistration<?> reg: listenerRegs) {
            reg.close();
        }

        executor.shutdownNow();
    }

    private <T extends DataObject> void onDataTreeChanged(Collection<DataTreeModification<T>> changes,
            BiConsumer<InstanceIdentifier<T>, T> onCreated, BiConsumer<InstanceIdentifier<T>, T> onRemoved) {
        for (DataTreeModification<T> change: changes) {
            DataObjectModification<T> rootNode = change.getRootNode();
            final InstanceIdentifier<T> identifier = change.getRootPath().getRootIdentifier();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    if (rootNode.getDataBefore() == null) {
                        onCreated.accept(identifier, rootNode.getDataAfter());
                    } else {
                        LOG.error("No need to support modify!!!");
                    }
                    break;
                case DELETE:
                    onRemoved.accept(identifier, rootNode.getDataBefore());
                    break;
                default:
                    break;
            }
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
            Long ofPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getDestTpPort(), databroker);
            Long vlanId = null;
            if (ofPort != null) {
                if (newRec.getAccessType() == AccessType.Vlan) {
                    vlanId = newRec.getAccessTag();
                }
                openflow13Provider.updateLocalHostRouteInDevice(dpid, ctx.isAclRedirectCapability(), ofPort,
                        gpeTunnelOfPort, vlanId, newRec, true);
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
                openflow13Provider.updateRemoteHostRouteInDevice(dpid, ctx.isAclRedirectCapability(), tunnelOfPort,
                        gpeTunnelOfPort, newRec, true);
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
            Long ofPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getDestTpPort(), databroker);
            Long vlanId = null;
            if (ofPort != null) {
                if (newRec.getAccessType() == AccessType.Vlan) {
                    vlanId = newRec.getAccessTag();
                }
                openflow13Provider.updateLocalHostRouteInDevice(dpid, ctx.isAclRedirectCapability(), ofPort,
                        gpeTunnelOfPort, vlanId, newRec, false);
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
                openflow13Provider.updateRemoteHostRouteInDevice(dpid, ctx.isAclRedirectCapability(), tunnelOfPort,
                        gpeTunnelOfPort, newRec, false);
            }
        }
    }

    private void onBDIFCreate(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        Long dpid = ctx.getDpid();

        AdapterBdIf newAdapterBdIf = new AdapterBdIf(newRec, vni);

        List<AdapterBdIf> bdIfs = new ArrayList<>(ctx.getBdifCache().values());

        openflow13Provider.updateBdifInDevice(dpid, bdIfs, newAdapterBdIf, true);

        ctx.addBdifToCache(newAdapterBdIf);
    }

    private void onBDIFDelete(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        Long dpid = ctx.getDpid();

        AdapterBdIf newAdapterBdIf = new AdapterBdIf(newRec, vni);

        List<AdapterBdIf> bdIfs = new ArrayList<>(ctx.getBdifCache().values());

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
            openflow13Provider.updateSfcTunnelInDevice(dpid, ctx.isAclRedirectCapability(), gpeTunnelOfPort,
                    segmentationId, true);
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
            openflow13Provider.updateSfcTunnelInDevice(dpid, ctx.isAclRedirectCapability(), gpeTunnelOfPort,
                    segmentationId, false);
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

            InstanceIdentifier<BdPort> bdPortIid = iid.firstIdentifierOf(BdPort.class);

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

            InstanceIdentifier<BdPort> bdPortIid = iid.firstIdentifierOf(BdPort.class);

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
            Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), newRec.getBdid(), databroker);

            if (vni != null) {
                openflow13Provider.updateBdPortInDevice(dpid, ofInPort, vni, newRec, true);
            }
        }
    }

    private void onBdPortDelete(InstanceIdentifier<BdPort> iid, BdPort newRec) {
        Long dpid = ctx.getDpid();
        Long ofInPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getRefTpId(), databroker);

        if (ofInPort != null) {
            Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), newRec.getBdid(), databroker);
            if (vni != null) {
                openflow13Provider.updateBdPortInDevice(dpid, ofInPort, vni, newRec, false);
            }
        }
    }

    private void onRouteCreate(InstanceIdentifier<Route> iid, Route newRec) {
        Long dpid = ctx.getDpid();

        List<AdapterBdIf> bdIfs = new ArrayList<>(ctx.getBdifCache().values());

        if (newRec.getNextHopOptions() instanceof SimpleNextHop) {
            SimpleNextHop simpleNextHop = (SimpleNextHop) newRec.getNextHopOptions();
            Ipv4Address nexthopIp = simpleNextHop.getNextHop();
            boolean isLocalNexthop = OvsSouthboundUtils.isLocalNexthop(fabricId,
                    newRec.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), nexthopIp, ctx.getVtep(), databroker);

            if (isLocalNexthop) {
                for (AdapterBdIf bdIf : bdIfs) {
                    String bdifMac = bdIf.getMacAddress().getValue();
                    if (bdifMac != null) {
                        openflow13Provider.updateRouteInLocalDevice(dpid, bdifMac, newRec, true);
                    }
                }
            } else {
                for (AdapterBdIf bdIf : bdIfs) {
                    String bdifMac = bdIf.getMacAddress().getValue();
                    if (bdifMac != null) {
                        openflow13Provider.updateRouteInRemoteDevice(dpid, bdifMac, newRec, true);
                    }
                }
            }

            String nexthopMacAddress = OvsSouthboundUtils.getNexthopMac(this.fabricId,
                    newRec.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), nexthopIp, databroker);
            if (nexthopMacAddress != null) {
                // Nexthop In this Device
                if (isLocalNexthop) {
                    openflow13Provider.updateNexthopInLocalDevice(dpid, nexthopMacAddress, newRec, true);
                }
                // Nexthop in remote Device
                else {
                    IpAddress dstTunIpAddress = OvsSouthboundUtils.getNexthopTunnelIp(this.fabricId,
                            newRec.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), nexthopIp, databroker);

                    Long tunnelOfPort = ctx.getVtep_ofPort();
                    openflow13Provider.updateNexthopInRemoteDevice(dpid, tunnelOfPort, dstTunIpAddress, true);
                }
            }
        }
    }

    private void onRouteDelete(InstanceIdentifier<Route> iid, Route newRec) {
        Long dpid = ctx.getDpid();

        List<AdapterBdIf> bdIfs = new ArrayList<>(ctx.getBdifCache().values());

        if (newRec.getNextHopOptions() instanceof SimpleNextHop) {
            SimpleNextHop simpleNextHop = (SimpleNextHop) newRec.getNextHopOptions();
            Ipv4Address nexthopIp = simpleNextHop.getNextHop();
            boolean isLocalNexthop = OvsSouthboundUtils.isLocalNexthop(fabricId,
                    newRec.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), nexthopIp, ctx.getVtep(), databroker);

            if (isLocalNexthop) {
                for (AdapterBdIf bdIf : bdIfs) {
                    String bdifMac = bdIf.getMacAddress().getValue();
                    if (bdifMac != null) {
                        openflow13Provider.updateRouteInLocalDevice(dpid, bdifMac, newRec, false);
                    }
                }
            } else {
                for (AdapterBdIf bdIf : bdIfs) {
                    String bdifMac = bdIf.getMacAddress().getValue();
                    if (bdifMac != null) {
                        openflow13Provider.updateRouteInRemoteDevice(dpid, bdifMac, newRec, false);
                    }
                }
            }

            String nexthopMacAddress = OvsSouthboundUtils.getNexthopMac(this.fabricId,
                    newRec.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), nexthopIp, databroker);
            if (nexthopMacAddress != null) {
                // Nexthop In this Device
                if (isLocalNexthop) {
                    openflow13Provider.updateNexthopInLocalDevice(dpid, nexthopMacAddress, newRec, false);
                }
                // Nexthop in remote Device
                else {
                    IpAddress dstTunIpAddress = OvsSouthboundUtils.getNexthopTunnelIp(this.fabricId,
                            newRec.getAugmentation(VxlanRouteAug.class).getOutgoingVni(), nexthopIp, databroker);

                    Long tunnelOfPort = ctx.getVtep_ofPort();
                    openflow13Provider.updateNexthopInRemoteDevice(dpid, tunnelOfPort, dstTunIpAddress, false);
                }
            }
        }
    }

    private void onIpMappingCreate(InstanceIdentifier<IpMappingEntry> iid, IpMappingEntry newRec) {
        Long dpid = ctx.getDpid();
        Long segmentationId = null;

        if (iid.firstKeyOf(Bdif.class) != null) {
            InstanceIdentifier<Bdif> bdifIid = iid.firstIdentifierOf(Bdif.class);
            segmentationId = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bdifIid, databroker);
        }

        List<AdapterBdIf> bdIfs = new ArrayList<>(ctx.getBdifCache().values());

        for (AdapterBdIf bdIf : bdIfs) {
            String bdifMac = bdIf.getMacAddress().getValue();

            openflow13Provider.updateIpMappingInDevice(dpid, segmentationId, bdifMac, newRec, true);
        }
    }

    private void onIpMappingDelete(InstanceIdentifier<IpMappingEntry> iid, IpMappingEntry newRec) {
        Long dpid = ctx.getDpid();
        Long segmentationId = null;

        if (iid.firstKeyOf(Bdif.class) != null) {
            InstanceIdentifier<Bdif> bdifIid = iid.firstIdentifierOf(Bdif.class);
            segmentationId = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bdifIid, databroker);
        }

        List<AdapterBdIf> bdIfs = new ArrayList<>(ctx.getBdifCache().values());

        for (AdapterBdIf bdIf : bdIfs) {
            String bdifMac = bdIf.getMacAddress().getValue();
            openflow13Provider.updateIpMappingInDevice(dpid, segmentationId, bdifMac, newRec, false);
        }
    }

}
