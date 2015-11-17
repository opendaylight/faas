/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers;

import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineAclHandler;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineArpHandler;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL2Forwarding;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL3Forwarding;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineL3Routing;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.PipelineTrafficClassifier;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdapterBdIf;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdpaterAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vxlan.rendered.mapping.rev150930.fabric.rendered.mapping.fabric.HostRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

public class Openflow13Provider {
    //private static final Logger LOG = LoggerFactory.getLogger(Openflow13Provider.class);

    private PipelineTrafficClassifier trafficClassifier;
    private PipelineArpHandler arpHandler;
    private PipelineAclHandler aclHandler;
    private PipelineL3Routing l3Routing;
    private PipelineL3Forwarding l3Forwarding;
    private PipelineL2Forwarding l2Forwarding;

    private DataBroker databroker = null;

    public Openflow13Provider(Node node, DataBroker databroker) {
        this.databroker = databroker;
        initialOpenflowPipeline(node);
    }

    public void initialOpenflowPipeline(Node node) {

        trafficClassifier = new PipelineTrafficClassifier(databroker);
        trafficClassifier.programDefaultPipelineRule(node);

        arpHandler = new PipelineArpHandler(databroker);
        arpHandler.programDefaultPipelineRule(node);

        aclHandler = new PipelineAclHandler(databroker);
        aclHandler.programDefaultPipelineRule(node);

        l3Routing = new PipelineL3Routing(databroker);
        l3Routing.programDefaultPipelineRule(node);

        l3Forwarding = new PipelineL3Forwarding(databroker);
        l3Forwarding.programDefaultPipelineRule(node);

        l2Forwarding = new PipelineL2Forwarding(databroker);
        l2Forwarding.programDefaultPipelineRule(node);

    }

//    private long getDpid(Node node) {
//        long dpid = OvsSouthboundUtils.getDataPathId(node);
//        if (dpid == 0) {
//            LOG.warn("getDpid: dpid not found: {}", node);
//        }
//        return dpid;
//    }

    //The host route is located in this Device
    public void addLocalHostRouteInDevice(Long dpidLong, Long ofPort, HostRoute hostRoute)
    {
        Long segmentationId = hostRoute.getVni();
        String macAddress = hostRoute.getMac().getValue();
        IpAddress ipAddress = hostRoute.getIp();

        trafficClassifier.programLocalInPort(dpidLong, segmentationId, ofPort, macAddress, true);
        trafficClassifier.programDropSrcIface(dpidLong, ofPort, true);

        arpHandler.programStaticArpEntry(dpidLong, segmentationId, macAddress, ipAddress, AdpaterAction.ADD);

        l3Forwarding.programForwardingTableEntry(dpidLong, segmentationId, ipAddress, macAddress, AdpaterAction.ADD);

        l2Forwarding.programLocalUcastOut(dpidLong, segmentationId, ofPort, macAddress, true);
        l2Forwarding.programRemoteBcastOutToLocalPort(dpidLong, segmentationId, ofPort, true);
        l2Forwarding.programLocalBcastToLocalPort(dpidLong, segmentationId, ofPort, true);
        //l2Forwarding.programLocalBcastToTunnelPort(dpidLong, segmentationId, ofPort, dstTunIpAddress, true);

    }

    //The host route is located in The remove Device, add some flows in this device for the remote host route
    public void addRemoteHostRouteInDevice(Long dpidLong, Long tunnelOfPort, HostRoute hostRoute)
    {
        Long segmentationId = hostRoute.getVni();
        String macAddress = hostRoute.getMac().getValue();
        IpAddress ipAddress = hostRoute.getIp();

        IpAddress dstTunIp = hostRoute.getDestVtep();

        arpHandler.programStaticArpEntry(dpidLong, segmentationId, macAddress, ipAddress, AdpaterAction.ADD);

        l3Forwarding.programForwardingTableEntry(dpidLong, segmentationId, ipAddress, macAddress, AdpaterAction.ADD);

        l2Forwarding.programTunnelOut(dpidLong, segmentationId, tunnelOfPort, macAddress, dstTunIp, true);

        //Because in Device context, it is difficult to find the peer's vtep ip, so add flood to tunnel port function here
        l2Forwarding.programLocalBcastToTunnelPort(dpidLong, segmentationId, tunnelOfPort, dstTunIp, true);

    }

//    public void addHostRoute(Node node, HostRoute hostRoute) {
//        Long dpidLong = getDpid(node);
//
//
//
//        if (1) {
//            TpId tpid = hostRoute.getDestBridgePort();
//            Long ofPort = OvsSouthboundUtils.getOfPort(node, tpid);
//
//            if (ofPort == 0) {
//                LOG.warn("hostRouteInLocalDevice: could not find ofPort");
//                return;
//            }
//            addLocalHostRouteInDevice(dpidLong, ofPort, hostRoute);
//        }
//        else {
//            Long tunnelOfPort = OvsSouthboundUtils.getVxlanTunnelOFPort(node);
//            String dstTunIpAddress = hostRoute.getDestVtep().getIpv4Address().getValue();
//
//            addRemoteHostRouteInDevice(dpidLong, tunnelOfPort, dstTunIpAddress, hostRoute);
//        }
//    }

    //For the condition: Add a new vni in a fabric capable device
    public void addBridgeDomainInDevice(Long dpidLong, Long tunnelOfPort, Long segmentationId) {
        trafficClassifier.programTunnelIn(dpidLong, segmentationId, tunnelOfPort, true);

//        if (dstTunIpAddress != null)
//            l2Forwarding.programLocalBcastToTunnelPort(dpidLong, segmentationId, tunnelOfPort, dstTunIpAddress, true);


    }


    public void addBdifInDevice(Long dpidLong, List<AdapterBdIf> bdIfs, AdapterBdIf newBdIf) {

        Long newBdIfSegId = newBdIf.getVni();
        String newBdIfMac = newBdIf.getMacAddress().getValue();
        IpAddress newBdIfIp = newBdIf.getIpAddress();
        int newBdIfMask = newBdIf.getMask();

        for (AdapterBdIf bdIf : bdIfs) {
            if ((bdIf.getVrf() == newBdIf.getVrf()) ) {
                Long bdIfSegId = bdIf.getVni();
                String bdifMac = bdIf.getMacAddress().getValue();
                IpAddress bdIfIp = bdIf.getIpAddress();
                int bdIfMask = bdIf.getMask();

                if (bdIfSegId != newBdIfSegId) {
                    l3Routing.programRouterInterface(dpidLong, bdIfSegId, newBdIfSegId, newBdIfMac, newBdIfIp, newBdIfMask, AdpaterAction.ADD);
                    l3Routing.programRouterInterface(dpidLong, newBdIfSegId, bdIfSegId, bdifMac, bdIfIp, bdIfMask, AdpaterAction.ADD);
                }
            }
        }

    }

//    private Group getGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
//        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory
//                .rev130819.nodes.Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class).child(Group.class,
//                        new GroupKey(groupBuilder.getGroupId())).build();
//        ReadOnlyTransaction readTx = databroker.newReadOnlyTransaction();
//        try {
//            Optional<Group> data = readTx.read(LogicalDatastoreType.CONFIGURATION, path1).get();
//            if (data.isPresent()) {
//                return data.get();
//            }
//        } catch (InterruptedException|ExecutionException e) {
//            LOG.error(e.getMessage(), e);
//        }
//
//        LOG.debug("Cannot find data for Group " + groupBuilder.getGroupName());
//        return null;
//    }
//
//    private void writeGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
//        ReadWriteTransaction modification = databroker.newReadWriteTransaction();
//        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory
//                .rev130819.nodes.Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class).child(Group.class,
//                        new GroupKey(groupBuilder.getGroupId())).build();
//        modification.put(LogicalDatastoreType.CONFIGURATION, path1, groupBuilder.build(), true /*createMissingParents*/);
//
//        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
//        try {
//            commitFuture.get();  // TODO: Make it async (See bug 1362)
//            LOG.debug("Transaction success for write of Group " + groupBuilder.getGroupName());
//        } catch (InterruptedException|ExecutionException e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }

//    /*
//     * Used for flood to local port
//     * groupId is segment id
//     */
//    protected InstructionBuilder createOutputGroupInstructionsToLocalPort(NodeBuilder nodeBuilder,
//            InstructionBuilder ib,
//            Long dpidLong, Long port , long groupId,
//            List<Instruction> instructions) {
//        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpidLong + ":" + port);
//        LOG.debug("createOutputGroupInstructionsToLocalPort() Node Connector ID is - Type=openflow: DPID={} port={} existingInstructions={}", dpidLong, port, instructions);
//
//        List<Action> actionList = Lists.newArrayList();
//        ActionBuilder ab = new ActionBuilder();
//
//        List<Action> existingActions;
//        if (instructions != null) {
//            for (Instruction in : instructions) {
//                if (in.getInstruction() instanceof ApplyActionsCase) {
//                    existingActions = (((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction());
//                    actionList.addAll(existingActions);
//                }
//            }
//        }
//
//        GroupBuilder groupBuilder = new GroupBuilder();
//        Group group = null;
//
//        /* Create output action for this port*/
//        OutputActionBuilder oab = new OutputActionBuilder();
//        oab.setOutputNodeConnector(ncid);
//        ab.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
//        LOG.debug("createOutputGroupInstructionsToLocalPort(): output action {}", ab.build());
//        boolean addNew = true;
//        boolean groupActionAdded = false;
//
//        /* Find the group action and get the group */
//        for (Action action : actionList) {
//            if (action.getAction() instanceof GroupActionCase) {
//                groupActionAdded = true;
//                GroupActionCase groupAction = (GroupActionCase) action.getAction();
//                Long id = groupAction.getGroupAction().getGroupId();
//                String groupName = groupAction.getGroupAction().getGroup();
//                GroupKey key = new GroupKey(new GroupId(id));
//
//                groupBuilder.setGroupId(new GroupId(id));
//                groupBuilder.setGroupName(groupName);
//                groupBuilder.setGroupType(GroupTypes.GroupAll);
//                groupBuilder.setKey(key);
//                group = getGroup(groupBuilder, nodeBuilder);
//                LOG.debug("createOutputGroupInstructionsToLocalPort: group {}", group);
//                break;
//            }
//        }
//
//        BucketId bucketId = new BucketId((long) 1);
//
//        LOG.debug("createOutputGroupInstructionsToLocalPort: groupActionAdded {}", groupActionAdded);
//        if (groupActionAdded) {
//            /* modify the action bucket in group */
//            groupBuilder = new GroupBuilder(group);
//            Buckets buckets = groupBuilder.getBuckets();
//            for (Bucket bucket : buckets.getBucket()) {
//                List<Action> bucketActions = bucket.getAction();
//                LOG.debug("createOutputGroupInstructionsToLocalPort: bucketActions {}", bucketActions);
//                if (bucket.getBucketId().getValue() == 1l) {
//                    //for local port, the bucket id is always 1
//                    for (Action action : bucketActions) {
//                        if (action.getAction() instanceof OutputActionCase) {
//                            OutputActionCase opAction = (OutputActionCase)action.getAction();
//                            /* If output port action already in the action list of one of the buckets, skip */
//                            if (opAction.getOutputAction().getOutputNodeConnector().equals(new Uri(ncid))) {
//                                addNew = false;
//                                break;
//                            }
//                        }
//                    }
//
//                    LOG.debug("createOutputGroupInstructionsToLocalPort: addNew {}", addNew);
//                    if (addNew && !buckets.getBucket().isEmpty()) {
//                        /* the new output action is not in the bucket, add to bucket */
//                        //bucket = buckets.getBucket().get(0);
//                        List<Action> bucketActionList = Lists.newArrayList();
//                        bucketActionList.addAll(bucket.getAction());
//                        /* set order for new action and add to action list */
//                        ab.setOrder(bucketActionList.size());
//                        ab.setKey(new ActionKey(bucketActionList.size()));
//                        bucketActionList.add(ab.build());
//
//                        /* set bucket and buckets list. Reset groupBuilder with new buckets.*/
//                        BucketsBuilder bucketsBuilder = new BucketsBuilder();
//                        List<Bucket> bucketList = Lists.newArrayList();
//                        BucketBuilder bucketBuilder = new BucketBuilder();
//                        bucketBuilder.setBucketId(new BucketId(bucketId));
//                        bucketBuilder.setKey(new BucketKey(bucketId));
//                        bucketBuilder.setAction(bucketActionList);
//                        bucketList.add(bucketBuilder.build());
//                        bucketsBuilder.setBucket(bucketList);
//                        groupBuilder.setBuckets(bucketsBuilder.build());
//                        LOG.debug("createOutputGroupInstructionsToLocalPort: bucketList {}", bucketList);
//                    }
//                    break;
//                }
//            }
//
//        } else {
//            /* create group */
//            groupBuilder = new GroupBuilder();
//            groupBuilder.setGroupType(GroupTypes.GroupAll);
//            groupBuilder.setGroupId(new GroupId(groupId));
//            groupBuilder.setKey(new GroupKey(new GroupId(groupId)));
//            groupBuilder.setGroupName("Output port group " + groupId);
//            groupBuilder.setBarrier(false);
//
//            BucketsBuilder bucketBuilder = new BucketsBuilder();
//            List<Bucket> bucketList = Lists.newArrayList();
//            BucketBuilder bucket = new BucketBuilder();
//            bucket.setBucketId(new BucketId(bucketId));
//            bucket.setKey(new BucketKey(bucketId));
//
//            /* put output action to the bucket */
//            List<Action> bucketActionList = Lists.newArrayList();
//            /* set order for new action and add to action list */
//            ab.setOrder(bucketActionList.size());
//            ab.setKey(new ActionKey(bucketActionList.size()));
//            bucketActionList.add(ab.build());
//
//            bucket.setAction(bucketActionList);
//            bucketList.add(bucket.build());
//            bucketBuilder.setBucket(bucketList);
//            groupBuilder.setBuckets(bucketBuilder.build());
//
//            /* Add new group action */
//            GroupActionBuilder groupActionB = new GroupActionBuilder();
//            groupActionB.setGroupId(groupId);
//            groupActionB.setGroup("Output port group " + groupId);
//            ab = new ActionBuilder();
//            ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
//            ab.setOrder(actionList.size());
//            ab.setKey(new ActionKey(actionList.size()));
//            actionList.add(ab.build());
//
//            //groupId++;
//        }
//        LOG.debug("createOutputGroupInstructionsToLocalPort: group {}", groupBuilder.build());
//        LOG.debug("createOutputGroupInstructionsToLocalPort: actionList {}", actionList);
//
//        if (addNew) {
//            /* rewrite the group to group table */
//            writeGroup(groupBuilder, nodeBuilder);
//        }
//
//        // Create an Apply Action
//        ApplyActionsBuilder aab = new ApplyActionsBuilder();
//        aab.setAction(actionList);
//        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
//
//        return ib;
//    }
//
//    /*
//     * Used for flood to tunnel port, for different dest Node, have different dest tunnel ip address
//     * groupId is segment id
//     */
//    protected InstructionBuilder createOutputGroupInstructionsToTunnelPort(NodeBuilder nodeBuilder,
//            InstructionBuilder ib,Long groupId,
//            Long dpidLong, Long port , IpAddress destTunnelIp,
//            List<Instruction> instructions) {
//        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpidLong + ":" + port);
//        LOG.debug("createOutputGroupInstructionsToTunnelPort() Node Connector ID is - Type=openflow: DPID={} port={} existingInstructions={}", dpidLong, port, instructions);
//
//        List<Action> actionList = Lists.newArrayList();
//
//        List<Action> existingActions;
//        if (instructions != null) {
//            for (Instruction in : instructions) {
//                if (in.getInstruction() instanceof ApplyActionsCase) {
//                    existingActions = (((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction());
//                    actionList.addAll(existingActions);
//                }
//            }
//        }
//
//        GroupBuilder groupBuilder = new GroupBuilder();
//        Group group = null;
//
//        /* Create output action for this port*/
//        ActionBuilder outPortActionBuilder = new ActionBuilder();
//        ActionBuilder loadTunIPv4ActionBuilder = new ActionBuilder();
//
//        OutputActionBuilder oab = new OutputActionBuilder();
//        oab.setOutputNodeConnector(ncid);
//        outPortActionBuilder.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
//        /* Create load tunnel ip action */
//        loadTunIPv4ActionBuilder.setAction(ActionUtils.nxLoadTunIPv4Action(destTunnelIp.getIpv4Address().getValue(), false));
//
//        boolean addNew = true;
//        boolean groupActionAdded = false;
//
//        /* Find the group action and get the group */
//        for (Action action : actionList) {
//            if (action.getAction() instanceof GroupActionCase) {
//                groupActionAdded = true;
//                GroupActionCase groupAction = (GroupActionCase) action.getAction();
//                Long id = groupAction.getGroupAction().getGroupId();
//                String groupName = groupAction.getGroupAction().getGroup();
//                GroupKey key = new GroupKey(new GroupId(id));
//
//                groupBuilder.setGroupId(new GroupId(id));
//                groupBuilder.setGroupName(groupName);
//                groupBuilder.setGroupType(GroupTypes.GroupAll);
//                groupBuilder.setKey(key);
//                group = getGroup(groupBuilder, nodeBuilder);
//                LOG.debug("createOutputGroupInstructionsToTunnelPort: group {}", group);
//                break;
//            }
//        }
//
//        BucketId bucketId = null;
//
//        //add tunnel port out, bucket_id=destTunnelIp.int
//        int ip = InetAddresses.coerceToInteger(InetAddresses.forString(destTunnelIp.getIpv4Address().getValue()));
//        long ipl = ip & 0xffffffffL;
//        bucketId = new BucketId(ipl);
//
//        if (groupActionAdded) {
//            /* modify the action bucket in group */
//            groupBuilder = new GroupBuilder(group);
//            Buckets buckets = groupBuilder.getBuckets();
//
//            for (Bucket bucket : buckets.getBucket()) {
//                if ( (bucket.getBucketId().getValue() == bucketId.getValue())
//                        && (bucket.getBucketId().getValue() != 1l) ) {
//                    LOG.warn("Warning: createOutputGroupInstructionsToTunnelPort: the bucket is exsit for a tunnel port");
//                    addNew = false;
//                    //return null;
//                }
//            }
//            if (addNew) {
//                /* the new output action is not in the bucket, add to bucket */
//                //Bucket bucket = buckets.getBucket().get(0);
//                //BucketBuilder bucket = new BucketBuilder();
//                List<Action> bucketActionList = Lists.newArrayList();
//                //bucketActionList.addAll(bucket.getAction());
//                /* set order for new action and add to action list */
//                loadTunIPv4ActionBuilder.setOrder(0);
//                loadTunIPv4ActionBuilder.setKey(new ActionKey(0));
//                bucketActionList.add(loadTunIPv4ActionBuilder.build());
//
//                outPortActionBuilder.setOrder(1);
//                outPortActionBuilder.setKey(new ActionKey(1));
//                bucketActionList.add(outPortActionBuilder.build());
//
//                /* set bucket and buckets list. Reset groupBuilder with new buckets.*/
//                BucketsBuilder bucketsBuilder = new BucketsBuilder();
//                List<Bucket> bucketList = Lists.newArrayList();
//                BucketBuilder bucketBuilder = new BucketBuilder();
//                bucketBuilder.setBucketId(bucketId);
//                bucketBuilder.setKey(new BucketKey(bucketId));
//                bucketBuilder.setAction(bucketActionList);
//                bucketList.add(bucketBuilder.build());
//                bucketsBuilder.setBucket(bucketList);
//                groupBuilder.setBuckets(bucketsBuilder.build());
//                LOG.debug("createOutputGroupInstructionsToTunnelPort: bucketList {}", bucketList);
//            }
//
//        } else {
//            /* create group */
//            groupBuilder = new GroupBuilder();
//            groupBuilder.setGroupType(GroupTypes.GroupAll);
//            groupBuilder.setGroupId(new GroupId(groupId));
//            groupBuilder.setKey(new GroupKey(new GroupId(groupId)));
//            groupBuilder.setGroupName("Output port group " + groupId);
//            groupBuilder.setBarrier(false);
//
//            BucketsBuilder bucketBuilder = new BucketsBuilder();
//            List<Bucket> bucketList = Lists.newArrayList();
//            BucketBuilder bucket = new BucketBuilder();
//
//            bucket.setBucketId(bucketId);
//            bucket.setKey(new BucketKey(bucketId));
//
//            /* put output action to the bucket */
//            List<Action> bucketActionList = Lists.newArrayList();
//            /* set order for new action and add to action list */
//            loadTunIPv4ActionBuilder.setOrder(0);
//            loadTunIPv4ActionBuilder.setKey(new ActionKey(0));
//            bucketActionList.add(loadTunIPv4ActionBuilder.build());
//
//            outPortActionBuilder.setOrder(1);
//            outPortActionBuilder.setKey(new ActionKey(1));
//            bucketActionList.add(outPortActionBuilder.build());
//
//            bucket.setAction(bucketActionList);
//            bucketList.add(bucket.build());
//            bucketBuilder.setBucket(bucketList);
//            groupBuilder.setBuckets(bucketBuilder.build());
//
//            /* Add new group action */
//            GroupActionBuilder groupActionB = new GroupActionBuilder();
//            groupActionB.setGroupId(groupId);
//            groupActionB.setGroup("Output port group " + groupId);
//            ActionBuilder ab = new ActionBuilder();
//            ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
//            ab.setOrder(actionList.size());
//            ab.setKey(new ActionKey(actionList.size()));
//            actionList.add(ab.build());
//
//            //groupId++;
//        }
//        //LOG.debug("createOutputGroupInstructions: group {}", groupBuilder.build());
//        //LOG.debug("createOutputGroupInstructions: actionList {}", actionList);
//
//        if (addNew) {
//            /* rewrite the group to group table */
//            writeGroup(groupBuilder, nodeBuilder);
//        }
//
//        // Create an Apply Action
//        ApplyActionsBuilder aab = new ApplyActionsBuilder();
//        aab.setAction(actionList);
//        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
//
//        return ib;
//    }

    public static NodeBuilder createNodeBuilder(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }
}
