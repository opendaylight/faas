/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.vxlan.adapter.ce;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.huawei.enterprise.ce.data.CESystemInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.Constants;
import org.opendaylight.faas.fabric.utils.InterfaceManager;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.faas.providers.netconf.VrfNetconfProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDeviceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDeviceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricPortAug;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricPortAugBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.network.topology.topology.node.AttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.AddToVxlanFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.CredentialAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.CredentialAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.FabricVxlanDeviceAdapterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.RmFromVxlanFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.VtepAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.CredentialBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.Vtep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.device.adapter.vxlan.rev150930.network.topology.topology.node.attributes.VtepBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricPortAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricPortAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.termination.point.FportAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.FabricPortRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.ServiceCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.TpRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.VxlanFabric;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricDeviceManager implements FabricVxlanDeviceAdapterService, DataTreeChangeListener<CredentialAttribute>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FabricDeviceManager.class);

    private final ListeningExecutorService executor;
    private final DataBroker databroker;

    private final Map<String, VrfNetconfProvider> deviceList = new HashMap<>();
    private final Map<InstanceIdentifier<Node>, CEDeviceRenderer> renderers = Maps.newHashMap();

    private final RoutedRpcRegistration<FabricVxlanDeviceAdapterService> rpcRegistration;

    final InstanceIdentifier<CredentialAttribute> targetPath = InstanceIdentifier.create(NetworkTopology.class)
            .child(Topology.class).child(Node.class).augmentation(CredentialAttribute.class);

    public FabricDeviceManager(final DataBroker databroker,
            final RpcProviderRegistry rpcRegistry) {
        this.databroker = databroker;
        executor = MoreExecutors.listeningDecorator(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

        rpcRegistration = rpcRegistry.addRoutedRpcImplementation(FabricVxlanDeviceAdapterService.class, this);

        databroker.registerDataTreeChangeListener(
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, targetPath), this);

        this.initDeviceList();
        this.createPhysicalTopology();

        LOG.info("Vrp Netconf adapter initialized!");
    }

    public DataBroker getDatabroker() {
        return databroker;
    }

    @Override
    public Future<RpcResult<Void>> addToVxlanFabric(final AddToVxlanFabricInput input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(input.getNodeId());
        Preconditions.checkNotNull(input.getFabricId());

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        @SuppressWarnings("unchecked")
        final InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) input.getNodeId();
        final FabricId fabricId = new FabricId(input.getFabricId());

        final Node bridgeNode = Utility.getCEBridgeNode(deviceIId, databroker);

        Preconditions.checkNotNull(bridgeNode);

        /*if (!Utility.addVxlanTunnelPort(bridgeNode, databroker)) {
            LOG.error("can not create tunnel port!");
            return Futures.immediateFailedFuture(new RuntimeException("can not create tunnel port"));
        }*/

        FabricCapableDeviceBuilder deviceBuilder = new FabricCapableDeviceBuilder();
        AttributesBuilder attributesBuilder = new AttributesBuilder();

        attributesBuilder.setFabricId(input.getFabricId());

        InstanceIdentifier<Node> fabricpath =
                Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(input.getFabricId()));
        attributesBuilder.setFabricRef(new NodeRef(fabricpath));

        deviceBuilder.setAttributes(attributesBuilder.build());

        @SuppressWarnings("unchecked")
        final InstanceIdentifier<FabricCapableDevice> path
            = ((InstanceIdentifier<Node>) input.getNodeId()).augmentation(FabricCapableDevice.class);

        WriteTransaction wt = databroker.newWriteOnlyTransaction();
        wt.merge(LogicalDatastoreType.OPERATIONAL, path, deviceBuilder.build(), true);
        addTp2Fabric(wt, bridgeNode, deviceIId, fabricId);

        CheckedFuture<Void,TransactionCommitFailedException> future = wt.submit();

        CredentialAttribute credential = Utility.getCredentialAug(bridgeNode);

        return Futures.transform(future, (AsyncFunction<Void, RpcResult<Void>>) input1 -> {
            renderers.put(deviceIId, new CEDeviceRenderer(databroker,
                    deviceList.get(credential.getCredential().getMgip()), deviceIId, bridgeNode, fabricId));

            return Futures.immediateFuture(result);
        }, executor);

    }

    private void addTp2Fabric(WriteTransaction wt, Node bridgeNode, InstanceIdentifier<Node> deviceIId, FabricId fabricId) {
        NodeBuilder devBuilder = new NodeBuilder().setKey(bridgeNode.getKey());

        NodeBuilder fabricBuilder = new NodeBuilder().setNodeId(fabricId);

        List<TerminationPoint> updTps = Lists.newArrayList();
        List<TerminationPoint> fabricTps = Lists.newArrayList();

        for (TerminationPoint tp : bridgeNode.getTerminationPoint()) {
            TpId fabricTpid = InterfaceManager.createFabricPort(bridgeNode.getNodeId(), tp.getTpId());
            // add ref to device tp
            updTps.add(new TerminationPointBuilder()
                    .setKey(tp.getKey())
                    .addAugmentation(FabricPortAug.class, new FabricPortAugBuilder()
                            .setPortRole(FabricPortRole.Access)
                            .setPortRef(new TpRef(MdSalUtils.createFabricPortIId(fabricId, fabricTpid)))
                            .build())
                    .build());

            // add tp on fabric
            fabricTps.add(new TerminationPointBuilder()
                    .setKey(new TerminationPointKey(fabricTpid))
                    .setTpRef(Lists.newArrayList(tp.getTpId()))
                    .addAugmentation(FabricPortAugment.class, new FabricPortAugmentBuilder()
                            .setFportAttribute(new FportAttributeBuilder()
                                    .setDevicePort(
                                            new TpRef(deviceIId.child(TerminationPoint.class, tp.getKey()))).build())
                            .build())
                    .build());

        }
        devBuilder.setTerminationPoint(updTps);
        fabricBuilder.setTerminationPoint(fabricTps);

        wt.merge(LogicalDatastoreType.OPERATIONAL, deviceIId, devBuilder.build(), false);
        wt.merge(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createFNodeIId(fabricId), fabricBuilder.build(), false);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<CredentialAttribute>> changes) {
        for (DataTreeModification<CredentialAttribute> change: changes) {
            DataObjectModification<CredentialAttribute> rootNode = change.getRootNode();
            if(rootNode.getModificationType() == DataObjectModification.ModificationType.WRITE) {
                InstanceIdentifier<CredentialAttribute> bridgeIId = change.getRootPath().getRootIdentifier();
                InstanceIdentifier<Node> targetIId = bridgeIId.firstIdentifierOf(Node.class);
                this.rpcRegistration.registerPath(FabricCapableDeviceContext.class, targetIId);

                VrfNetconfProvider prov = deviceList.get(rootNode.getDataAfter().getCredential().getMgip());
                setupDeviceAttribute(prov, bridgeIId, rootNode.getDataAfter());
            }
            if(rootNode.getModificationType() == DataObjectModification.ModificationType.DELETE) {
                InstanceIdentifier<CredentialAttribute> bridgeIId = change.getRootPath().getRootIdentifier();
                InstanceIdentifier<Node> targetIId = bridgeIId.firstIdentifierOf(Node.class);

                this.rpcRegistration.unregisterPath(FabricCapableDeviceContext.class, targetIId);
            }
    }
    }

    private void setupDeviceAttribute(VrfNetconfProvider prov,
            final InstanceIdentifier<CredentialAttribute> ceAttr,
            CredentialAttribute ceData) {
        /* setup vtepid */
        String vtepIp = null;

        InstanceIdentifier<Node> nodeIId = ceAttr.firstIdentifierOf(Node.class);
        WriteTransaction wt = databroker.newWriteOnlyTransaction();

        vtepIp = prov.getVtepIPAddress();
        if (vtepIp != null) {
            CredentialBuilder cb = new CredentialBuilder();
            cb.setVtepip(vtepIp);
            CredentialAttributeBuilder cab  = new CredentialAttributeBuilder(ceData);
            cab.setCredential(cb.build());

            wt.merge(LogicalDatastoreType.OPERATIONAL, ceAttr, cab.build(), false);

            InstanceIdentifier<Vtep> vtepIId = nodeIId.augmentation(FabricCapableDevice.class).child(Attributes.class)
                    .augmentation(VtepAttribute.class).child(Vtep.class);

            VtepBuilder builder = new VtepBuilder();
            builder.setIp(new IpAddress(vtepIp.toCharArray()));

            wt.put(LogicalDatastoreType.OPERATIONAL, vtepIId, builder.build(), true);
        }

        /* setup supported-fabric-type */
        {
            InstanceIdentifier<FabricCapableDevice> deviceIid = nodeIId.augmentation(FabricCapableDevice.class);
            FabricCapableDeviceBuilder builder = new FabricCapableDeviceBuilder();
            builder.setSupportedFabric(Lists.newArrayList(VxlanFabric.class));
            builder.setCapabilitySupported(
                        Lists.newArrayList(ServiceCapabilities.AclRedirect, ServiceCapabilities.IpMapping));
            wt.put(LogicalDatastoreType.OPERATIONAL, deviceIid, builder.build(), true);
        }
        wt.submit();
    }

    private void  initDeviceList() {
        deviceList.put("192.168.252.18",new VrfNetconfProvider("cxj@sdd", "Huawei@123", "192.168.252.18"));
        deviceList.put("192.168.252.19",new VrfNetconfProvider("cxj@sdd", "Huawei@123", "192.168.252.19"));
        deviceList.put("192.168.252.20",new VrfNetconfProvider("cxj@sdd", "Huawei@123", "192.168.252.20"));
        deviceList.put("192.168.252.21",new VrfNetconfProvider("cxj@sdd", "Huawei@123", "192.168.252.21"));

        deviceList.values().forEach(device -> device.init());
    }

    private void createPhysicalTopology() {

        WriteTransaction wt = databroker.newWriteOnlyTransaction();
        deviceList.values().forEach((prov) -> {
            CESystemInfo info = prov.getLLDPSysInfo();
            final InstanceIdentifier<Node> deviceIId = org.opendaylight.faas.fabric.utils.MdSalUtils.createNodeIId(Constants.PHYSICAL_TOPOLOGY_ID, info.getChassisID());
            NodeBuilder devBuilder = new NodeBuilder().setKey(new NodeKey(new NodeId(info.getChassisID())));
            List<TerminationPoint> updTps = Lists.newArrayList();
            prov.getAllPorts().forEach(port -> {
                updTps.add(new TerminationPointBuilder()
                        .setKey(new TerminationPointKey(new TpId(port))).build());
            });

            CredentialAttributeBuilder creb = new CredentialAttributeBuilder();

            creb.setCredential(new CredentialBuilder().setMgip(prov.getServerIP()).setSysname(info.getSysName()).build());

            devBuilder.setTerminationPoint(updTps);
            devBuilder.addAugmentation(CredentialAttribute.class, creb.build());

            wt.put(LogicalDatastoreType.OPERATIONAL, deviceIId, devBuilder.build(), true);

            prov.getNNIs().keySet().forEach(key -> {
                com.google.common.collect.ImmutableMap<String, String> map = prov.getNNIs().get(key);
                if (map.entrySet().size() != 0) { // only consider the first one assuming it is one to one link, not one to many link.
                    String source = info.getChassisID();
                    String sourcetp = key;
                    Map.Entry<String, String> entry0  = map.entrySet().iterator().next();
                    String desttp = entry0.getKey();
                    String dest = entry0.getValue();

                    String lidvalue = source + ":" + sourcetp + "-" + dest + ":" + desttp;

                    LinkId lid = new LinkId(lidvalue);

                    LinkBuilder lBuilder = new LinkBuilder().setKey(new LinkKey(lid));
                    lBuilder.setLinkId(lid);

                    lBuilder.setSource(new SourceBuilder().setSourceNode(new NodeId(source)).setSourceTp(new TpId (sourcetp)).build());
                    lBuilder.setDestination(new DestinationBuilder().setDestNode(new NodeId(dest)).setDestTp(new TpId(desttp)).build());

                    final InstanceIdentifier<Link> linkIId = org.opendaylight.faas.fabric.utils.MdSalUtils.createLinkIId(new FabricId(Constants.PHYSICAL_TOPOLOGY_ID), lid);
                    wt.put(LogicalDatastoreType.OPERATIONAL, linkIId, lBuilder.build(), true);
                }
            });
        });

        wt.submit();
    }


    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
            deviceList.values().forEach(device -> device.close());
        }
    }


    @Override
    public Future<RpcResult<Void>> rmFromVxlanFabric(RmFromVxlanFabricInput input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(input.getNodeId());
        Preconditions.checkNotNull(input.getFabricId());

        @SuppressWarnings("unchecked")
        final InstanceIdentifier<Node> deviceIId = (InstanceIdentifier<Node>) input.getNodeId();
        CEDeviceRenderer renderer = renderers.remove(deviceIId);
        if (renderer != null) {
            renderer.close();
        }

        final Node bridgeNode = Utility.getCEBridgeNode(deviceIId, databroker);

        Utility.deleteVxlanTunnelPort(bridgeNode, databroker);

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    private void removeTpFromFabric(WriteTransaction wt, Node bridgeNode, InstanceIdentifier<Node> deviceIId) {
        for (TerminationPoint tp : bridgeNode.getTerminationPoint()) {

            InstanceIdentifier<FabricPortAug> path = deviceIId.child(TerminationPoint.class, tp.getKey())
                    .augmentation(FabricPortAug.class);
            wt.delete(LogicalDatastoreType.OPERATIONAL, path);
        }
    }

}
