/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan.adapter.ce;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.opendaylight.faas.providers.netconf.VrfNetconfProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BdPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Vrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.Options;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.ServiceCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.route.next.hop.options.SimpleNextHop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.route.next.hop.options.VrfNextHop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.route.group.route.NextHopOptions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.Rib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.VniMembers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.vni.members.Members;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CEDeviceRenderer implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(CEDeviceRenderer.class);

    private final CEDeviceContext ctx;

    private final VrfNetconfProvider vrfNetconfProvider;
    private final FabricId fabricId;

    private final ExecutorService executor;
    private final DataBroker databroker;
    private int prefixIndex = 1000;
    private final String ROUTE_POLICY_PERMIT_INDEX = "10";
    private final String PREFIX_NAME = "prefix";

    private ListenerRegistration<DataChangeListener> bridgeDomainListener = null;
    private ListenerRegistration<DataChangeListener> bdifListener = null;
    private ListenerRegistration<DataChangeListener> vtepMembersListener = null;
    private ListenerRegistration<DataChangeListener> bdPortListener = null;
    private ListenerRegistration<DataChangeListener> vrfListener = null;
    private ListenerRegistration<DataChangeListener> hostRouteListener = null;
    private ListenerRegistration<DataChangeListener> ribRouteListener = null;
    private ListenerRegistration<DataChangeListener> ribListener = null;


    public CEDeviceRenderer(DataBroker databroker, VrfNetconfProvider provider, InstanceIdentifier<Node> iid, Node node,
            FabricId fabricId) {

        ThreadFactory threadFact = new ThreadFactoryBuilder().setNameFormat("CENetConf-vxlan-adapter-%d").build();
        this.executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(threadFact));
        this.databroker = databroker;
        this.fabricId = fabricId;

        ctx = new CEDeviceContext(node, iid);

        this.vrfNetconfProvider = provider;

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


        InstanceIdentifier<BdPort> bdPortIID = iid.augmentation(FabricCapableDevice.class).child(Config.class)
                .child(BdPort.class);
        bdPortListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, bdPortIID, this,
                DataChangeScope.BASE);

        InstanceIdentifier<Vrf> vrfIId = iid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(Vrf.class);
        vrfListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, vrfIId, this,
                DataChangeScope.BASE);

        InstanceIdentifier<HostRoute> hostRouteIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(HostRoute.class);
        hostRouteListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, hostRouteIId, this,
                DataChangeScope.BASE);

        InstanceIdentifier<Route> routeIID = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(Rib.class).child(Route.class);
        ribRouteListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, routeIID, this,
                DataChangeScope.BASE);

        InstanceIdentifier<Rib> ribIID = InstanceIdentifier.create(FabricRenderedMapping.class)
                .child(Fabric.class, new FabricKey(fabricId)).child(Rib.class);
        ribListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, ribIID, this,
                DataChangeScope.BASE);

        System.out.println("Registering listener for rib : fabricID :[" + fabricId + "]");
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
                //openflow13Provider.updateTrafficBehavior(ctx.getDpid(), ctx.getTrafficBehavior(), true);
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
        if (bdPortListener != null) {
            bdPortListener.close();
        }

        if (vrfListener != null) {
            vrfListener.close();
        }

        if (ribRouteListener != null) {
            ribRouteListener.close();
        }

        if (ribListener != null) {
            ribListener.close();
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
        System.out.println("onDataCreated! the data is " + entry.getValue());

        if (entry.getValue() instanceof BridgeDomain) {
            final BridgeDomain newRec = (BridgeDomain) entry.getValue();
            executor.submit(() -> {
                onBridgeDomainCreate(newRec);
                return null;
            });
        } else if (entry.getValue() instanceof Bdif) {
            final Bdif newRec = (Bdif) entry.getValue();
            executor.submit(() -> {
                onBDIFCreate(newRec);
                return null;
            });
        } else if (entry.getValue() instanceof Members) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Members> vtepMembersIid = (InstanceIdentifier<Members>) entry.getKey();
            final Members newRec = (Members) entry.getValue();
            executor.submit(() -> {
                onVtepMembersCreate(vtepMembersIid, newRec);
                return null;
            });
        } else if (entry.getValue() instanceof BdPort) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<BdPort> bdPortIid = (InstanceIdentifier<BdPort>) entry.getKey();
            final BdPort newRec = (BdPort) entry.getValue();
            executor.submit(() -> {
                onBdPortCreate(bdPortIid, newRec);
                return null;
            });
        } else if (entry.getValue() instanceof Vrf) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Vrf> vrfIid = (InstanceIdentifier<Vrf>) entry.getKey();
            final Vrf newRec = (Vrf) entry.getValue();
            executor.submit(() -> {
                // For now this is delayed to when the BDIF is created.
                //TODO onVrfCreate(vrfIid, newRec);
                return null;
            });
        } else if (entry.getValue() instanceof HostRoute) {
            final HostRoute newRec = (HostRoute) entry.getValue();

            executor.submit(() -> {
                onHostRouteCreate(newRec);
                return null;
            });
        } else if (entry.getValue() instanceof Rib) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Rib> rid = (InstanceIdentifier<Rib>) entry.getKey();
            final Rib newRec = (Rib) entry.getValue();
            executor.submit(() -> {
                onRouteListCreate(newRec);
                return null;
            });
        } else if (entry.getValue() instanceof Route) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Route> routeIid = (InstanceIdentifier<Route>) entry.getKey();
            final Route newRec = (Route) entry.getValue();
            executor.submit(() -> {
                onRouteCreate(newRec);
                return null;
            });
        }
    }


    private void onDataUpdated(Entry<InstanceIdentifier<?>, DataObject> entry) {
        System.out.println("onDataUpdated! the data is " + entry.getValue());
        LOG.error("No need to support modify!!!");
    }

    private void onDataRemoved(InstanceIdentifier<?> iid, DataObject entry) {

        if (entry instanceof BridgeDomain) {
            final BridgeDomain newRec = (BridgeDomain) entry;
            executor.submit(() -> {
                onBridgeDomainDelete(newRec);
                return null;
            });
        } else if (entry instanceof Bdif) {
            final Bdif newRec = (Bdif) entry;
            executor.submit(() -> {
                onBDIFDelete(newRec);
                return null;
            });
        } else if (entry instanceof Members) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Members> vtepMembersIid = (InstanceIdentifier<Members>) iid;
            final Members newRec = (Members) entry;
            executor.submit(() -> {
                onVtepMembersDelete(vtepMembersIid, newRec);
                return null;
            });
        } else if (entry instanceof BdPort) {

            @SuppressWarnings("unchecked")
            final InstanceIdentifier<BdPort> bdPortIid = (InstanceIdentifier<BdPort>) iid;
            final BdPort newRec = (BdPort) entry;
            executor.submit(() -> {
                onBdPortDelete(bdPortIid, newRec);
                return null;
            });

        } else if (entry instanceof Vrf) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Vrf> vrfIid = (InstanceIdentifier<Vrf>) iid;
            final Vrf newRec = (Vrf) entry;
            executor.submit(() -> {
                onVrfDelete(vrfIid, newRec);
                return null;
            });
        } else if (entry instanceof HostRoute) {
            final HostRoute newRec = (HostRoute) entry;

            executor.submit(() -> {
                onHostRouteDelete(newRec);
                return null;
            });
        } else if (entry instanceof Route) {

            @SuppressWarnings("unchecked")
            final InstanceIdentifier<Route> routeIid = (InstanceIdentifier<Route>) iid;
            final Route newRec = (Route) entry;
            executor.submit(() -> {
                onRouteDelete(newRec);
                return null;
            });
        }
    }

    //TODO
    private void onHostRouteCreate(HostRoute newRec) {
        //String fromVrfID = getVrfIDBasedOnIP(newRec.getIp());
        //String toVrfID = getVrfIDBasedOnVni(newRec.getVni());
        //importRoute(fromVrfID, toVrfID, newRec.getIp().getIpv4Address().getValue(), 2);
    }

    private void onHostRouteDelete(HostRoute newRec) {
    }


    private void onRouteListCreate(Rib newRib) {
        for (Route newRec : newRib.getRoute()) {
            System.out.println("onRouteCreate: " + newRec.getDestinationPrefix().getValue());
            String fromVrfID = newRec.getRoutingDomain();
            NextHopOptions opt = newRec.getNextHopOptions();
            if (opt instanceof VrfNextHop) {
                VrfNextHop vnh = (VrfNextHop) opt;
                String toVrfID = vnh.getRoutingDomain();
                importRoute(fromVrfID, toVrfID, newRec.getDestinationPrefix().getValue(), 31);
            } else if (opt instanceof SimpleNextHop) {
                // this should not happen for now
            }
        }
    }

    private void onRouteCreate(Route newRec) {
        System.out.println("onRouteCreate: " + newRec.getDestinationPrefix().getValue());
        String fromVrfID = newRec.getRoutingDomain();
        NextHopOptions opt = newRec.getNextHopOptions();
        if (opt instanceof VrfNextHop) {
            VrfNextHop vnh =  (VrfNextHop)opt;
            String toVrfID = vnh.getRoutingDomain();
            importRoute(fromVrfID, toVrfID, newRec.getDestinationPrefix().getValue(), 31);
        } else if (opt instanceof SimpleNextHop) {
            // this should not happen for now
        }
    }


    private void onRouteDelete(HostRoute newRec) {}

    private void onRouteDelete(Route newRec) {
        String fromVrfID = newRec.getRoutingDomain();
        NextHopOptions opt = newRec.getNextHopOptions();
        if (opt instanceof VrfNextHop) {
            VrfNextHop vnh =  (VrfNextHop)opt;
            String toVrfID = vnh.getRoutingDomain();
            removeRoute(fromVrfID, toVrfID, newRec.getDestinationPrefix().getValue(), 31);
        } else if (opt instanceof SimpleNextHop) {
            // this should not happen for now
        }

    }

    private String importRoute (String fromVrfID, String toVrfID, String ipaddress, int masklen)
    {

        //this is idempotent which means harmless if it exist. just failed to create.
        String ret1 = vrfNetconfProvider.createIPv4Prefix(this.PREFIX_NAME);

        //Adding a new  permit prefix in the "prefix" prefix.
        //TODO, need to figure out how to manager the node index.
        String ret2 = vrfNetconfProvider.createIPv4PrefixNode(this.PREFIX_NAME, String.valueOf(prefixIndex ++), ipaddress, masklen);
        String ret3 = "", ret4 = "";
        if (toVrfID != null) {
            //allow BGP to import direct route for the vrf.
            ret3 = vrfNetconfProvider.allowDirectRoute(fromVrfID);

            //import the routes from the target vrf.
            ret4 = vrfNetconfProvider.addVRFImportRT(fromVrfID, toVrfID);
        }

        //White list: control the granularity to only allow the target prefix to be allowed. deny everything else.
        //TODO fix the hardcoded node index "10"
        String ret5 = vrfNetconfProvider.addImportRoutePolicyEntry(toVrfID, this.PREFIX_NAME, ROUTE_POLICY_PERMIT_INDEX);

        return String.join("\n", ret1, ret2, ret3, ret4, ret5);
    }

    private String removeRoute (String fromVrfID, String toVrfID, String ipaddress, int masklen)
    {
        String ret = vrfNetconfProvider.removeIPv4PrefixNode(this.PREFIX_NAME, "10", ipaddress, masklen);
        return ret;
    }


    private String createVRF(String vrfID, String rd, String rt)
    {
        //create both import and export policies
        String ret  = vrfNetconfProvider.createPolicyForVRF(vrfID, true);
        String ret0  = vrfNetconfProvider.createPolicyForVRF(vrfID, false);

        //configure VRF and its RD and default RTs.
        String ret1 = vrfNetconfProvider.createVRF(vrfID);
        String ret2 = vrfNetconfProvider.setVRFVNI(vrfID, vrfID);
        String ret3 = vrfNetconfProvider.setVRFAfandRD(vrfID, rd);
        String ret4 = vrfNetconfProvider.setVRFRT(vrfID, rt, rt);

        //create IPv4uni family in BGP for VRf {vrfID} and allow to import direct route.
        //prepare for inter VRF routing.
        String ret5 = vrfNetconfProvider.addIPv4FamilyinBGP(vrfID);
        String ret6 = vrfNetconfProvider.allowDirectRoute(vrfID);

        //TODO Check for individual failure. how do we recover from those failures.
        //we need some sort of transaction here.
        return new StringBuilder().
                append(ret).
                append(ret0).
                append(ret1).
                append(ret2).
                append(ret3).
                append(ret4).
                append(ret5).
                append(ret6).
                toString();
    }

    private void onBDIFCreate(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = Utility.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        if (vni == 0L) {
            vni = Long.valueOf(bridgeDomainId.replace("bd:", ""));
            String ret = vrfNetconfProvider.createBridgeDomain(vni.toString(), "forLogicalLink", vni.toString());
            System.out.println(ret);
        }

        String ret = vrfNetconfProvider.createBDIF(vni);

        //4K is the maximum value on the CE device we know of.
        //TODO this is a hack, will be addressed later
        Integer normalizedValue = newRec.getVrf() - 15000000 + 1;
        String rd = normalizedValue.toString() + ":" + normalizedValue.toString();

        if(!ctx.isVrfExists(newRec.getVrf())) {
            String resp = createVRF(newRec.getVrf().toString(), rd, rd);
            System.out.println(newRec.getVrf().toString() + " doesn not exist on " + ctx.getBridgeName());
            LOG.debug("createVRF response : " + resp);
        } else {
            System.out.println(newRec.getVrf().toString() + " exists! on " + ctx.getBridgeName());
        }

        String ifname = "vbdif" + vni;
        ret = vrfNetconfProvider.bindIFtoVRF(ifname,
                newRec.getVrf().toString(),
                newRec.getIpAddress().getIpv4Address().getValue(),
                Utility.getIPv4Mask(newRec.getMask().intValue()) );

        System.out.println(ret);

        //Enable host collect and distributed gateway.
        vrfNetconfProvider.enableArpConfig(ifname);

        //Ensure the RT of BD compliant with Vrf RT as Huawei Document describes.
        vrfNetconfProvider.setBDRT(vni.toString(), rd);

        //remove the old RT
        vrfNetconfProvider.removeBDRT(vni.toString(), vni + ":1");

        ctx.addBdifToCache(new AdapterBdIf(newRec, vni));
        ctx.addBdifToVrfCache(newRec.getVrf(), vni);

    }

    private void onBDIFDelete(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = Utility.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);

        //TODO How to ensure the integrity of the following operations.
        vrfNetconfProvider.deleteBDIF(vni);
        ctx.deleteBdifFromCache(new AdapterBdIf(newRec, vni));
        ctx.removeBdifFromVrfCache(newRec.getVrf(), vni);

        if(! ctx.isVrfExists(newRec.getVrf())) {
            vrfNetconfProvider.removeVRF(newRec.getVrf().toString());
        }
    }

    private void onBridgeDomainCreate(BridgeDomain newRec) {
        Long segmentationId = newRec.getAugmentation(BridgeDomain1.class).getVni();
        vrfNetconfProvider.createBridgeDomain(segmentationId.toString(), newRec.getId(), segmentationId.toString());
    }

    private void onVrfDelete(InstanceIdentifier<Vrf> vrfIid, Vrf newRec) {
        vrfNetconfProvider.removeVRF(newRec.getName());
    }


    private void onBridgeDomainDelete(BridgeDomain newRec) {
        Long segmentationId = newRec.getAugmentation(BridgeDomain1.class).getVni();
        vrfNetconfProvider.deleteBridgeDomain(segmentationId.toString(), segmentationId.toString());
    }

    private void onVtepMembersCreate(InstanceIdentifier<Members> iid, Members newRec) {
            //BGP will take care of this.
    }

    private void onVtepMembersDelete(InstanceIdentifier<Members> iid, Members newRec) {
            //BGP will take care of this.
    }

    private String getPhyType(TpId tpid) {
        return "10GE"; //TODO
    }

    private void onBdPortCreate(InstanceIdentifier<BdPort> iid, BdPort newRec) {
        Long vni = Utility.getBridgeDomainVni(ctx.getMyIId(), newRec.getBdid(), databroker);
        if (vni == 0L) {
            System.out.println("Attaching to a l3 port!");
            vni = Long.valueOf(newRec.getBdid().replace("bd:", ""));
        }

        System.out.println("in onBdPortCreate  vni =" + vni);
        if (vni != null) {
            //openflow13Provider.updateBdPortInDevice(dpid, ofInPort, vni, newRec, true);
            String response = vrfNetconfProvider.createSubIf(newRec.getRefTpId().getValue(), getPhyType(newRec.getRefTpId()), newRec.getAccessTag().toString(), newRec.getAccessTag().toString());
            LOG.debug(response);
            String response1 = vrfNetconfProvider.createBdPort(vni.toString(), newRec.getRefTpId().getValue() + "." + newRec.getAccessTag());
            LOG.debug(response1);
        }
    }

    private void onBdPortDelete(InstanceIdentifier<BdPort> iid, BdPort newRec) {
        Long vni = Utility.getBridgeDomainVni(ctx.getMyIId(), newRec.getBdid(), databroker);
        if (vni == 0L) {
            System.out.println("detaching to a l3 port!");
            vni = Long.valueOf(newRec.getBdid().replace("bd:", ""));
        }

        String ifname = newRec.getRefTpId().getValue() + "." + newRec.getAccessTag();
        System.out.println("in onBdPortDelete  vni =" + vni + "node:" + vrfNetconfProvider.getServerIP() + " ifname :" + ifname);
        if (vni != null) {
            vrfNetconfProvider.deleteBdPort(vni.toString(), ifname);
            //openflow13Provider.updateBdPortInDevice(dpid, ofInPort, vni, newRec, false);
        }
    }
}
