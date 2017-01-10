/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.general;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.faas.fabric.general.spi.FabricRenderer;
import org.opendaylight.faas.fabric.general.spi.FabricRendererFactory;
import org.opendaylight.faas.fabric.utils.MdSalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.capable.device.rev150930.FabricCapableDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddLinkToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.AddNodeToFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.ComposeFabricOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.DecomposeFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.GetAllFabricsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.GetAllFabricsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.GetPortsFromFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.GetPortsFromFabricOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.RmLinkFromFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.RmNodeFromFabricInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceLinks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceLinksBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceLinksKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.DeviceNodesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.fabric.attributes.OptionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttribute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.network.topology.topology.node.FabricAttributeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.DeviceRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.LinkRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.ServiceCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.VlanFabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.VxlanFabric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.fabric.impl.rev150930.FabricsSetting;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.fabric.impl.rev150930.FabricsSettingBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.node.attributes.SupportingNodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricManagementAPIProvider implements AutoCloseable, FabricService {

    private static final Logger LOG = LoggerFactory.getLogger(FabricManagementAPIProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;

    private RpcRegistration<FabricService> rpcRegistration;

    private final FabricRendererRegistry rendererMgr;

    private final ExecutorService executor;

    public FabricManagementAPIProvider(final DataBroker dataBroker,
            final RpcProviderRegistry rpcRegistry, ExecutorService executor, FabricRendererRegistry rendererMgr) {

        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;

        this.executor = executor;

        this.rendererMgr = rendererMgr;

    }

    public void start() {
        rpcRegistration = rpcRegistry.addRpcImplementation(FabricService.class, this);
    }

    @Override
    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
    }

    @Override
    public Future<RpcResult<Void>> decomposeFabric(DecomposeFabricInput input) {
        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        if ( input == null) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException("fabricId can not be empty!"));
        }
        final FabricId fabricId = input.getFabricId();

        if ( fabricId == null) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException("fabricId can not be empty!"));
        }

        ReadWriteTransaction rt = dataBroker.newReadWriteTransaction();

        final InstanceIdentifier<Node> fabricpath = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId));

        CheckedFuture<Optional<Node>,ReadFailedException> readFuture =
                rt.read(LogicalDatastoreType.OPERATIONAL, fabricpath);

        return Futures.transform(readFuture, optional -> {

		    if (optional.isPresent()) {
		        Node fabric = optional.get();
		        FabricInstanceCache.INSTANCE.retrieveFabric(fabricId).notifyFabricDeleted(fabric);

		        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
		        wt.delete(LogicalDatastoreType.OPERATIONAL, fabricpath);
		        wt.delete(LogicalDatastoreType.CONFIGURATION, fabricpath);
		        wt.delete(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createTopoIId(fabricId.getValue()));
		        MdSalUtils.wrapperSubmit(wt, executor);

		        FabricInstanceCache.INSTANCE.removeFabric(fabricId);
		    }

		    return result;
		});
    }

    @Override
    public Future<RpcResult<GetAllFabricsOutput>> getAllFabrics() {

        final SettableFuture<RpcResult<GetAllFabricsOutput>> futureResult = SettableFuture.create();
        final RpcResultBuilder<GetAllFabricsOutput> resultBuilder = RpcResultBuilder.<GetAllFabricsOutput>success();
        final GetAllFabricsOutputBuilder outputBuilder = new GetAllFabricsOutputBuilder();

        ReadOnlyTransaction trans = dataBroker.newReadOnlyTransaction();
        ListenableFuture<Optional<Topology>> readFuture =
                trans.read(LogicalDatastoreType.OPERATIONAL, Constants.DOM_FABRICS_PATH);
        Futures.addCallback(readFuture, new FutureCallback<Optional<Topology>>() {

            @Override
            public void onSuccess(Optional<Topology> result) {
                if (result.isPresent()) {
                    List<Node> nodes = result.get().getNode();
                    List<FabricId> fabricIds = new ArrayList<>();
                    if (nodes != null) {
                        for (Node node : nodes) {
                            FabricNode fnode = node.getAugmentation(FabricNode.class);
                            if (fnode != null) {
                                fabricIds.add(new FabricId(node.getNodeId()));
                            }
                        }
                        outputBuilder.setFabricId(fabricIds);
                    }
                }
                futureResult.set(resultBuilder.withResult(outputBuilder.build()).build());

            }

            @Override
            public void onFailure(Throwable th) {
                LOG.debug( "Failed to read network-topology dom", th);
                futureResult.setException(th);
            }
        }, executor);

        return futureResult;
    }

    @Override
    public Future<RpcResult<ComposeFabricOutput>> composeFabric(final ComposeFabricInput input)  {
        ComposeFabricInputBuilder inputBuilder = new ComposeFabricInputBuilder(input);
        String msg = null;
        if ((msg = checkFabricOptions(inputBuilder)) != null) {
            return Futures.immediateFailedCheckedFuture(new IllegalArgumentException(msg));
        }

        final FabricId fabricId = new FabricId(String.format("fabric:%d", this.genNextFabricNum()));


        final InstanceIdentifier<Node> fnodepath = MdSalUtils.createFNodeIId(fabricId);
        final InstanceIdentifier<FabricNode> fabricpath = fnodepath.augmentation(FabricNode.class);

        NodeBuilder fnodeBuilder = new NodeBuilder();
        buildNodeAttribute(fnodeBuilder, input, fabricId);

        FabricNodeBuilder fabricBuilder = new FabricNodeBuilder();
        FabricAttributeBuilder attrBuilder = new FabricAttributeBuilder();
        buildFabricAttribute(attrBuilder, inputBuilder);

        FabricRendererFactory rendererFactory = rendererMgr.getFabricRendererFactory(input.getType());
        FabricRenderer renderer = rendererFactory.composeFabric(fabricpath, attrBuilder, input);
        if (renderer == null) {
            return Futures.immediateFailedCheckedFuture(
                    new RuntimeException("Can not compose fabric due the renderer return false."));
        }

        fabricBuilder.setFabricAttribute(attrBuilder.build());
        FabricInstance fabric = FabricInstanceCache.INSTANCE.addFabric(fabricId, input.getType(), renderer);
        fabric.addListener(rendererFactory.createListener(fabricpath, fabricBuilder.getFabricAttribute()));

        final FabricNode fabricNode = fabricBuilder.build();
        fnodeBuilder.addAugmentation(FabricNode.class, fabricNode);

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        trans.put(LogicalDatastoreType.OPERATIONAL, fnodepath, fnodeBuilder.build(), true);
        trans.put(LogicalDatastoreType.CONFIGURATION, fnodepath, fnodeBuilder.build(), true);
        trans.put(LogicalDatastoreType.OPERATIONAL, MdSalUtils.createTopoIId(fabricId.getValue()),
                MdSalUtils.newTopo(fabricId.getValue()));

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, submitResult -> {
		    RpcResultBuilder<ComposeFabricOutput> resultBuilder = RpcResultBuilder.<ComposeFabricOutput>success();
		    ComposeFabricOutputBuilder outputBuilder = new ComposeFabricOutputBuilder();
		    outputBuilder.setFabricId(fabricId);

		    FabricInstanceCache.INSTANCE.retrieveFabric(fabricId).notifyFabricCreated(fabricNode);
		    return resultBuilder.withResult(outputBuilder.build()).build();
		});
    }

    private String checkFabricOptions(final ComposeFabricInputBuilder input) {
        List<ServiceCapabilities> capabilities = null;
        if (input.getOptions() != null) {
            capabilities = input.getOptions().getCapabilitySupported();
        }

        if (capabilities == null) {
            capabilities = Lists.newArrayList();
        }
        Set<ServiceCapabilities> allDevCapabilities = Sets.newHashSet();

        List<DeviceNodes> devices = input.getDeviceNodes();
        if (devices == null || devices.isEmpty()) {
            return "No device can support the capability of fabric.";
        }

        ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        List<CheckedFuture<Optional<FabricCapableDevice>,ReadFailedException>> futures = Lists.newArrayList();
        for (DeviceNodes device : devices) {
            @SuppressWarnings("unchecked")
            InstanceIdentifier<Node> devIid = (InstanceIdentifier<Node>) device.getDeviceRef().getValue();
            CheckedFuture<Optional<FabricCapableDevice>,ReadFailedException>  future =
                    rt.read(LogicalDatastoreType.OPERATIONAL, devIid.augmentation(FabricCapableDevice.class));
            futures.add(future);
        }

        try {
            List<Optional<FabricCapableDevice>> capDevices = Futures.allAsList(futures).get();

            for (Optional<FabricCapableDevice> opt : capDevices) {
                if (opt.isPresent()) {
                    FabricCapableDevice capDevice = opt.get();
                    if (capDevice.getCapabilitySupported() != null) {
                        allDevCapabilities.addAll(capDevice.getCapabilitySupported());
                    }
                    if (capDevice.getSupportedFabric() != null) {
                        boolean supported = false;
                        switch (input.getType()) {
                            case VXLAN:
                                supported = capDevice.getSupportedFabric().contains(VxlanFabric.class);
                                break;
                            case VLAN:
                                supported = capDevice.getSupportedFabric().contains(VlanFabric.class);
                                break;
                            default:
                                break;
                        }
                        if (!supported) {
                            return String.format("Device does not support this fabric type.");
                        }
                    } else {
                        return String.format("Device does not support this fabric type.");
                    }
                } else {
                    return String.format("Device is not a fabric capable device.");
                }
            }

            for (ServiceCapabilities cap : capabilities) {
                if (!allDevCapabilities.contains(cap)) {
                    return String.format("No device can support this capability [%s].", cap.name());
                }
            }

            if (capabilities.isEmpty()) {
                OptionsBuilder builder = input.getOptions() == null
                        ? new OptionsBuilder() : new OptionsBuilder(input.getOptions());

                input.setOptions(builder.setCapabilitySupported(Lists.newArrayList(allDevCapabilities)).build());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("", e);
            return "Exception ocurred when reading DomStore.";
        }
        return null;
    }

    private void buildNodeAttribute(NodeBuilder builder, ComposeFabricInput input, FabricId fabricId) {

        builder.setKey(new NodeKey(fabricId));

        List<DeviceNodes> devices = input.getDeviceNodes();
        if (devices != null) {
            List<SupportingNode> snodes = Lists.newArrayList();
            for (DeviceNodes device : devices) {
                InstanceIdentifier<?> nodeRef = device.getDeviceRef().getValue();
                NodeId nodeid = nodeRef.firstKeyOf(Node.class).getNodeId();
                TopologyId topoId = nodeRef.firstKeyOf(Topology.class).getTopologyId();

                SupportingNodeBuilder snodeBuilder = new SupportingNodeBuilder();
                snodeBuilder.setNodeRef(nodeid);
                snodeBuilder.setTopologyRef(topoId);
                snodeBuilder.setKey(new SupportingNodeKey(nodeid, topoId));
                snodes.add(snodeBuilder.build());
            }
            builder.setSupportingNode(snodes);
        }
    }

    private void buildFabricAttribute(FabricAttributeBuilder builder, ComposeFabricInputBuilder input) {
        builder.setName(input.getName());

        builder.setDescription(input.getDescription());
        builder.setType(input.getType());
        builder.setDeviceLinks(input.getDeviceLinks());
        // remove augment
        builder.setDeviceNodes(getDeviceNodesInput(input.getDeviceNodes()));
        builder.setOptions(input.getOptions());
    }

    private List<DeviceNodes> getDeviceNodesInput(List<DeviceNodes> input) {
        if (input == null) {
            return null;
        }
        List<DeviceNodes> ret = Lists.newArrayList();
        for (DeviceNodes node : input) {
            DeviceNodesBuilder builder = new DeviceNodesBuilder();
            builder.setDeviceRef(node.getDeviceRef());
            builder.setKey(node.getKey());
            builder.setRole(node.getRole());
            ret.add(builder.build());
        }
        return ret;
    }

    private long genNextFabricNum() {
        long ret = 1;

        FabricsSettingBuilder settingBuilder = new FabricsSettingBuilder();
        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        InstanceIdentifier<FabricsSetting> fabricImplPath = InstanceIdentifier.create(FabricsSetting.class);
        ListenableFuture<Optional<FabricsSetting>> readFuture =
                trans.read(LogicalDatastoreType.CONFIGURATION, fabricImplPath);
        Optional<FabricsSetting> optional;
        try {
            optional = readFuture.get();
            if (optional.isPresent()) {
                ret = optional.get().getNextFabricNum();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("can not read fabric setting", e);
        }

        settingBuilder.setNextFabricNum(ret + 1);
        trans.put(LogicalDatastoreType.CONFIGURATION, fabricImplPath, settingBuilder.build());
        MdSalUtils.wrapperSubmit(trans, executor);

        return ret;
    }

    @Override
    public Future<RpcResult<Void>> rmNodeFromFabric(RmNodeFromFabricInput input) {
        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        FabricId fabricId = input.getFabricId();
        final NodeRef device = input.getNodeRef();

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        ReadWriteTransaction trans = dataBroker.newReadWriteTransaction();

        // del fabric attribute
        InstanceIdentifier<DeviceNodes> devicepath = Constants.DOM_FABRICS_PATH
                    .child(Node.class, new NodeKey(fabricId)).augmentation(FabricNode.class)
                    .child(FabricAttribute.class)
                    .child(DeviceNodes.class, new DeviceNodesKey(device));

        trans.delete(LogicalDatastoreType.OPERATIONAL, devicepath);

        // del node attribute
        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> noderef = (InstanceIdentifier<Node>) device.getValue();
        NodeId deviceid = noderef.firstKeyOf(Node.class).getNodeId();
        TopologyId topoid = noderef.firstKeyOf(Topology.class).getTopologyId();
        InstanceIdentifier<SupportingNode> suplNodeIid = MdSalUtils.createFNodeIId(input.getFabricId())
                .child(SupportingNode.class, new SupportingNodeKey(deviceid, topoid));

        trans.delete(LogicalDatastoreType.OPERATIONAL, suplNodeIid);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transformAsync(future, new AsyncFunction<Void, RpcResult<Void>>() {

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyDeviceRemoved((InstanceIdentifier<Node>) device.getValue());
                return Futures.immediateFuture(result);
            }
        });
    }

    @Override
    public Future<RpcResult<Void>> addNodeToFabric(AddNodeToFabricInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        final FabricId fabricId = input.getFabricId();
        final NodeRef device = input.getNodeRef();
        final DeviceRole role = input.getRole();

        final InstanceIdentifier<DeviceNodes> path = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId))
                .augmentation(FabricNode.class).child(FabricAttribute.class)
                .child(DeviceNodes.class, new DeviceNodesKey(device));

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();
        // set fabric attribute
        DeviceNodesBuilder dnodeBuilder = new DeviceNodesBuilder();
        dnodeBuilder.setKey(new DeviceNodesKey(device)).setDeviceRef(device).build();
        fabricObj.addNodeToFabric(dnodeBuilder, input);
        trans.put(LogicalDatastoreType.OPERATIONAL, path, dnodeBuilder.build(), true);

        // set node attribute
        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> noderef = (InstanceIdentifier<Node>) device.getValue();
        NodeId deviceid = noderef.firstKeyOf(Node.class).getNodeId();
        TopologyId topoid = noderef.firstKeyOf(Topology.class).getTopologyId();
        final InstanceIdentifier<SupportingNode> suplNodeIid = MdSalUtils.createFNodeIId(input.getFabricId())
                .child(SupportingNode.class, new SupportingNodeKey(deviceid, topoid));
        SupportingNodeBuilder suplNodeBuilder = new SupportingNodeBuilder();
        suplNodeBuilder.setNodeRef(deviceid);
        suplNodeBuilder.setTopologyRef(topoid);
        suplNodeBuilder.setKey(new SupportingNodeKey(deviceid, topoid));
        trans.put(LogicalDatastoreType.OPERATIONAL, suplNodeIid, suplNodeBuilder.build(), true);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transformAsync(future, new AsyncFunction<Void, RpcResult<Void>>() {

            @SuppressWarnings("unchecked")
            @Override
            public ListenableFuture<RpcResult<Void>> apply(Void submitResult) throws Exception {
                fabricObj.notifyDeviceAdded((InstanceIdentifier<Node>) device.getValue(), role);
                return Futures.immediateFuture(result);
            }
        });
    }

    @Override
    public Future<RpcResult<Void>> rmLinkFromFabric(RmLinkFromFabricInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        FabricId fabricId = input.getFabricId();
        LinkRef link = input.getLinkRef();

        InstanceIdentifier<DeviceLinks> devicepath = Constants.DOM_FABRICS_PATH
                    .child(Node.class, new NodeKey(fabricId)).augmentation(FabricNode.class)
                    .child(FabricAttribute.class)
                    .child(DeviceLinks.class, new DeviceLinksKey(link));

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        trans.delete(LogicalDatastoreType.OPERATIONAL, devicepath);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, submitResult -> result);
    }

    @Override
    public Future<RpcResult<Void>> addLinkToFabric(AddLinkToFabricInput input) {

        final RpcResult<Void> result = RpcResultBuilder.<Void>success().build();

        FabricId fabricId = input.getFabricId();
        LinkRef link = input.getLinkRef();

        final InstanceIdentifier<DeviceLinks> path = Constants.DOM_FABRICS_PATH.child(Node.class, new NodeKey(fabricId))
                .augmentation(FabricNode.class).child(FabricAttribute.class)
                .child(DeviceLinks.class, new DeviceLinksKey(link));

        final FabricInstance fabricObj = FabricInstanceCache.INSTANCE.retrieveFabric(fabricId);
        if (fabricObj == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("fabric is not exist!"));
        }

        WriteTransaction trans = dataBroker.newWriteOnlyTransaction();

        DeviceLinksBuilder linkBuilder = new DeviceLinksBuilder();
        linkBuilder.setKey(new DeviceLinksKey(link)).setLinkRef(link).build();

        trans.put(LogicalDatastoreType.OPERATIONAL, path, linkBuilder.build(), false);

        CheckedFuture<Void,TransactionCommitFailedException> future = trans.submit();

        return Futures.transform(future, submitResult -> result;
    }

    @Override
    public Future<RpcResult<GetPortsFromFabricOutput>> getPortsFromFabric(GetPortsFromFabricInput input) {
        // TODO Auto-generated method stub
        return null;
    }
}
