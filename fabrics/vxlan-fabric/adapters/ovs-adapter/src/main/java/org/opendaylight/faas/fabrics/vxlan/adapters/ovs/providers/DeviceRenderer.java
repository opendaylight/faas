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
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBridgeDomain;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OvsSouthboundUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.Bdif;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.fabric.capable.device.config.BridgeDomainKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.BridgeDomain1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.FabricRenderedMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.Fabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.FabricKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DeviceRenderer implements DataChangeListener, AutoCloseable {

    private DeviceContext ctx;

    private Openflow13Provider openflow13Provider;

    private FabricId fabricId;

    private NodeId topoNodeId;

    private final ExecutorService executor;
    private final DataBroker databroker;

    private ListenerRegistration<DataChangeListener> hostRouteListener = null;
    private ListenerRegistration<DataChangeListener> bridgeDomainListener = null;
    private ListenerRegistration<DataChangeListener> bdifListener = null;

    public DeviceRenderer(ExecutorService exector, DataBroker databroker, InstanceIdentifier<Node> iid, Node node, FabricId fabricId) {
        this.executor = exector;
        this.databroker = databroker;

        this.fabricId = fabricId;
        this.topoNodeId = node.getNodeId();

        ctx = new DeviceContext(node, iid);

        //Initialize openflow pipeline in this Device
        openflow13Provider = new Openflow13Provider(node, databroker);

        InstanceIdentifier<HostRoute> hostRouteIId = InstanceIdentifier.create(FabricRenderedMapping.class)
                    .child(Fabric.class, new FabricKey(fabricId)).child(HostRoute.class);
        hostRouteListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, hostRouteIId, this, DataChangeScope.BASE);

        InstanceIdentifier<BridgeDomain> bridgeDomainIId = iid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(BridgeDomain.class);
        bridgeDomainListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, bridgeDomainIId, this, DataChangeScope.BASE);

        InstanceIdentifier<Bdif> bdifIId = iid.augmentation(FabricCapableDevice.class)
                .child(Config.class).child(Bdif.class);
        bdifListener = databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, bdifIId, this, DataChangeScope.BASE);
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
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        Map<InstanceIdentifier<?>, DataObject> newData = change.getCreatedData();
        for (Entry<InstanceIdentifier<?>, DataObject> entry : newData.entrySet()) {
            if (entry.getValue() instanceof HostRoute) {
                HostRoute newRec = (HostRoute) entry.getValue();
                onHostRouteCreate(newRec);
            } else if (entry.getValue() instanceof BridgeDomain) {
                BridgeDomain newRec = (BridgeDomain) entry.getValue();
                onBridgeDomainCreate(newRec);
            } else if (entry.getValue() instanceof Bdif) {
                Bdif newRec = (Bdif) entry.getValue();
                onBDIFCreate(newRec);
            }
        }

    }

    private void onHostRouteCreate(HostRoute newRec) {
        Long dpidLong = ctx.getDpid();
        Long ofPort = OvsSouthboundUtils.getOfPort(ctx.getMyIId(), newRec.getDestBridgePort(), databroker);

        if (ctx.getVtep().equals(newRec.getDestVtep())) {
            openflow13Provider.addLocalHostRouteInDevice(dpidLong, ofPort, newRec);
        }
        else {
            openflow13Provider.addRemoteHostRouteInDevice(dpidLong, ctx.getVtep_ofPort(), newRec);
        }
    }

    private void onHostRouteDelete() {

    }

    private void onHostRouteModified() {

    }

    private void onBDIFCreate(Bdif newRec) {
        String bridgeDomainId = newRec.getBdid();
        Long vni = OvsSouthboundUtils.getBridgeDomainVni(ctx.getMyIId(), bridgeDomainId, databroker);
        Long dpidLong = ctx.getDpid();

        AdapterBdIf newAdapterBdIf = new AdapterBdIf(newRec, vni);

        List<AdapterBdIf> bdIfs = new ArrayList<AdapterBdIf>(ctx.getBdifCache().values());

        openflow13Provider.addBdifInDevice(dpidLong, bdIfs, newAdapterBdIf);

        ctx.addBdifToCache(newAdapterBdIf);
    }

    private void onBDIFDelete() {

    }

    private void onBDIFModified() {
        // do nothing
    }


    private void onBridgeDomainCreate(BridgeDomain newRec) {

        Long dpidLong = ctx.getDpid();
        Long tunnelOfPort = ctx.getVtep_ofPort();
        Long segmentationId = newRec.getAugmentation(BridgeDomain1.class).getVni();

        openflow13Provider.addBridgeDomainInDevice(dpidLong, tunnelOfPort, segmentationId);


    }

    private void onBridgeDomainDelete() {

    }

    private void onBridgeDomainModified() {
        // do nothing
    }
}
