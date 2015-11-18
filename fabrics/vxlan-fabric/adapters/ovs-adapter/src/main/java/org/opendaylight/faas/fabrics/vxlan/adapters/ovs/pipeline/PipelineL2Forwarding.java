/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Constants;
import org.opendaylight.ovsdb.utils.mdsal.openflow.ActionUtils;
import org.opendaylight.ovsdb.utils.mdsal.openflow.InstructionUtils;
import org.opendaylight.ovsdb.utils.mdsal.openflow.MatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.CheckedFuture;

public class PipelineL2Forwarding  extends AbstractServiceInstance {
    private static final Logger LOG = LoggerFactory.getLogger(PipelineL2Forwarding.class);

    public PipelineL2Forwarding(DataBroker dataBroker) {
        super(Service.L2_FORWARDING, dataBroker);
    }

    public void programLocalUcastOut(Long dpidLong, Long segmentationId,
            Long localPort, String attachedMac, boolean write) {

        String nodeName = OPENFLOW + dpidLong;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        flowBuilder.setMatch(MatchUtils.createDestEthMatch(matchBuilder, new MacAddress(attachedMac), null).build());

        String flowId = "UcastOut_"+segmentationId+"_"+localPort+"_"+attachedMac;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (write) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

            // Set the Output Port/Iface
            InstructionUtils.createOutputPortInstructions(ib, dpidLong, localPort);
            ib.setOrder(0);
            ib.setKey(new InstructionKey(0));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    /*
     * Local Broadcast Flood
     */
    public void programRemoteBcastOutToLocalPort(Long dpidLong, Long segmentationId, Long localPort, boolean write) {

        String nodeName = OPENFLOW + dpidLong;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        MatchUtils.addNxRegMatch(matchBuilder, new MatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD, PipelineTrafficClassifier.REG_VALUE_FROM_REMOTE));
        flowBuilder.setMatch(MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        BigInteger.valueOf(segmentationId.longValue());
        flowBuilder.setMatch(MatchUtils.createDestEthMatch(matchBuilder, new MacAddress("01:00:00:00:00:00"),
                new MacAddress("01:00:00:00:00:00")).build());

        String flowId = "BcastOut_"+segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(16384);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        Flow flow = this.getFlow(flowBuilder, nodeBuilder);
        // Instantiate the Builders for the OF Actions and Instructions
        InstructionBuilder ib = new InstructionBuilder();
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        List<Instruction> existingInstructions = null;
        if (flow != null) {
            Instructions ins = flow.getInstructions();
            if (ins != null) {
                existingInstructions = ins.getInstruction();
            }
        }

        if (write) {
            // Create output port list
            createOutputPortInstructions(ib, dpidLong, localPort, existingInstructions);
            ib.setOrder(0);
            ib.setKey(new InstructionKey(0));

            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            boolean flowRemove = InstructionUtils.removeOutputPortFromInstructions(ib, dpidLong, localPort,
                    existingInstructions);
            if (flowRemove) {
                /* if all ports are removed, remove flow */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list*/
                ib.setOrder(0);
                ib.setKey(new InstructionKey(0));
                instructions.add(ib.build());

                // Add InstructionBuilder to the Instruction(s)Builder List
                isb.setInstruction(instructions);

                // Add InstructionsBuilder to FlowBuilder
                flowBuilder.setInstructions(isb.build());
                writeFlow(flowBuilder, nodeBuilder);
            }
        }
    }

    /*
     * Local Table Miss
     */
    public void programLocalTableMiss(Long dpidLong, Long segmentationId, boolean write) {

        String nodeName = OPENFLOW + dpidLong;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create Match(es) and Set them in the FlowBuilder Object
        flowBuilder.setMatch(MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());

        if (write) {
            // Create the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

            // Call the InstructionBuilder Methods Containing Actions
            InstructionUtils.createDropInstructions(ib);
            ib.setOrder(0);
            ib.setKey(new InstructionKey(0));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());
        }

        String flowId = "LocalTableMiss_"+segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(8192);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        if (write) {
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    /*
     * Egress Tunnel Traffic
     */
    public void programTunnelOut(Long dpidLong, Long segmentationId, Long OFPortOut, String attachedMac, IpAddress dstTunIpAddress, boolean write) {

        String nodeName = OPENFLOW + dpidLong;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        flowBuilder.setMatch(MatchUtils.createDestEthMatch(matchBuilder, new MacAddress(attachedMac), null).build());

        String flowId = "TunnelOut_"+segmentationId+"_"+OFPortOut+"_"+attachedMac;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (write) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

            // Set the Output Port/Iface
            InstructionUtils.createOutputPortInstructions(ib, dpidLong, OFPortOut);

            //add by yzy:
            ApplyActionsCase aac = (ApplyActionsCase) ib.getInstruction();
            List<Action> actionList = aac.getApplyActions().getAction();
            ActionBuilder ab = new ActionBuilder();
            ab.setAction(ActionUtils.nxLoadTunIPv4Action(dstTunIpAddress.getIpv4Address().getValue(), false));
            ab.setOrder(1);
            ab.setKey(new ActionKey(1));
            actionList.add(ab.build());
            //end by yzy

            ib.setOrder(0);
            ib.setKey(new InstructionKey(1));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    //Broadcast packet from local port, to other local port
    public void programLocalBcastToLocalPort(Long dpidLong, Long segmentationId, Long OFLocalPortOut, boolean write) {

        String nodeName = OPENFLOW + dpidLong;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        MatchUtils.addNxRegMatch(matchBuilder, new MatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD, PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(MatchUtils.createDestEthMatch(matchBuilder, new MacAddress("01:00:00:00:00:00"),
                new MacAddress("01:00:00:00:00:00")).build());

        String flowId = "TunnelFloodOut_"+segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(16383);  // FIXME: change it back to 16384 once bug 3005 is fixed.
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        Flow flow = this.getFlow(flowBuilder, nodeBuilder);
        // Instantiate the Builders for the OF Actions and Instructions
        InstructionBuilder ib = new InstructionBuilder();
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        List<Instruction> existingInstructions = null;
        if (flow != null) {
            Instructions ins = flow.getInstructions();
            if (ins != null) {
                existingInstructions = ins.getInstruction();
            }
        }

        if (write) {
            // Set the Output Port/Iface
            createOutputGroupInstructionsToLocalPort(nodeBuilder, ib, dpidLong, OFLocalPortOut, segmentationId, existingInstructions);
            //createOutputPortInstructions(ib, dpidLong, OFLocalPortOut, existingInstructions);
            ib.setOrder(0);
            ib.setKey(new InstructionKey(0));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            //yzy: not implement now!!!
//            /* remove port from action list */
//            boolean flowRemove = InstructionUtils.removeOutputPortFromInstructions(ib, dpidLong,
//                    OFLocalPortOut, existingInstructions);
//            if (flowRemove) {
//                /* if all port are removed, remove the flow too. */
//                removeFlow(flowBuilder, nodeBuilder);
//            } else {
//                /* Install instruction with new output port list*/
//                ib.setOrder(0);
//                ib.setKey(new InstructionKey(0));
//                instructions.add(ib.build());
//
//                // Add InstructionBuilder to the Instruction(s)Builder List
//                isb.setInstruction(instructions);
//
//                // Add InstructionsBuilder to FlowBuilder
//                flowBuilder.setInstructions(isb.build());
//                writeFlow(flowBuilder, nodeBuilder);
//            }
        }
    }

    //Broadcast packet from local port, to tunnel port, each tunnel port, create a single flow
    public void programLocalBcastToTunnelPort(Long dpidLong, Long segmentationId, Long OFTunnelPortOut, IpAddress dstTunIpAddress,  boolean write) {

        String nodeName = OPENFLOW + dpidLong;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        MatchUtils.addNxRegMatch(matchBuilder, new MatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD, PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(MatchUtils.createDestEthMatch(matchBuilder, new MacAddress("01:00:00:00:00:00"),
                new MacAddress("01:00:00:00:00:00")).build());

        String flowId = "TunnelFloodOut_"+segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(16383);  // FIXME: change it back to 16384 once bug 3005 is fixed.
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        Flow flow = this.getFlow(flowBuilder, nodeBuilder);

        // Instantiate the Builders for the OF Actions and Instructions
        InstructionBuilder ib = new InstructionBuilder();
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        List<Instruction> existingInstructions = null;
        if (flow != null) {
            Instructions ins = flow.getInstructions();
            if (ins != null) {
                existingInstructions = ins.getInstruction();
            }
        }

        if (write) {
            // Set the Output Port/Iface
            //InstructionUtils.createOutputPortInstructions(ib, dpidLong, OFTunnelPortOut);
            createOutputGroupInstructionsToTunnelPort(nodeBuilder, ib,
                    dpidLong, OFTunnelPortOut , segmentationId, dstTunIpAddress,
                    existingInstructions);
//            //add by yzy:
//            ApplyActionsCase aac = (ApplyActionsCase) ib.getInstruction();
//            List<Action> actionList = aac.getApplyActions().getAction();
//            ActionBuilder ab = new ActionBuilder();
//            ab.setAction(ActionUtils.nxLoadTunIPv4Action(dstTunIpAddress.getIpv4Address().getValue(), false));
//            ab.setOrder(1);
//            ab.setKey(new ActionKey(1));
//            actionList.add(ab.build());
//            //end by yzy

            ib.setOrder(0);
            ib.setKey(new InstructionKey(0));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    //called when a bridgeDomain create
//    public void programLocalBcastGroupFlow(Long dpidLong, Long segmentationId, boolean write) {
//        String nodeName = OPENFLOW + dpidLong;
//
//        MatchBuilder matchBuilder = new MatchBuilder();
//        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
//        FlowBuilder flowBuilder = new FlowBuilder();
//
//        // Create the OF Match using MatchBuilder
//        // Match TunnelID
//        MatchUtils.addNxRegMatch(matchBuilder, new MatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD, PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
//        flowBuilder.setMatch(MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
//        // Match DMAC
//        flowBuilder.setMatch(MatchUtils.createDestEthMatch(matchBuilder, new MacAddress("01:00:00:00:00:00"),
//                new MacAddress("01:00:00:00:00:00")).build());
//
//        String flowId = "BcastGroup_"+segmentationId;
//        // Add Flow Attributes
//        flowBuilder.setId(new FlowId(flowId));
//        FlowKey key = new FlowKey(new FlowId(flowId));
//        flowBuilder.setBarrier(true);
//        flowBuilder.setTableId(getTable());
//        flowBuilder.setKey(key);
//        flowBuilder.setPriority(16383);  // FIXME: change it back to 16384 once bug 3005 is fixed.
//        flowBuilder.setFlowName(flowId);
//        flowBuilder.setHardTimeout(0);
//        flowBuilder.setIdleTimeout(0);
//
//        // Instantiate the Builders for the OF Actions and Instructions
//        InstructionBuilder ib = new InstructionBuilder();
//        InstructionsBuilder isb = new InstructionsBuilder();
//        List<Instruction> instructions = Lists.newArrayList();
//
//        if (write) {
//            ApplyActionsCase aac = (ApplyActionsCase) ib.getInstruction();
//            List<Action> actionList = aac.getApplyActions().getAction();
//            ActionBuilder ab = new ActionBuilder();
//            ab.setAction(OvsFlowUtils.groupAction(segmentationId));
//            ab.setOrder(0);
//            ab.setKey(new ActionKey(0));
//            actionList.add(ab.build());
//
//            ib.setOrder(0);
//            ib.setKey(new InstructionKey(0));
//            instructions.add(ib.build());
//
//            // Add InstructionBuilder to the Instruction(s)Builder List
//            isb.setInstruction(instructions);
//
//            // Add InstructionsBuilder to FlowBuilder
//            flowBuilder.setInstructions(isb.build());
//
//            writeFlow(flowBuilder, nodeBuilder);
//
//        } else {
//            //yzy: have not implement delete a port from group function
//            //removeFlow(flowBuilder, nodeBuilder);
//        }
//    }

    protected InstructionBuilder createOutputPortInstructions(InstructionBuilder ib,
            Long dpidLong, Long port ,
            List<Instruction> instructions) {
        NodeConnectorId ncid = new NodeConnectorId(OPENFLOW + dpidLong + ":" + port);
        LOG.debug("createOutputPortInstructions() Node Connector ID is - Type=openflow: DPID={} port={} existingInstructions={}", dpidLong, port, instructions);

        List<Action> actionList = Lists.newArrayList();
        ActionBuilder ab = new ActionBuilder();

        List<Action> existingActions;
        if (instructions != null && instructions.size() > 0) {
            /**
             * First instruction is the one containing the output ports.
             * So, only extract the actions from that.
             */
            Instruction in = instructions.get(0);
            if (in.getInstruction() instanceof ApplyActionsCase) {
                existingActions = (((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction());
                // Only include output actions
                for (Action action : existingActions) {
                    if (action.getAction() instanceof OutputActionCase) {
                        actionList.add(action);
                    }
                }
            }
        }
        /* Create output action for this port*/
        OutputActionBuilder oab = new OutputActionBuilder();
        oab.setOutputNodeConnector(ncid);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        boolean addNew = true;

        /* Find the group action and get the group */
        for (Action action : actionList) {
            OutputActionCase opAction = (OutputActionCase)action.getAction();
            /* If output port action already in the action list of one of the buckets, skip */
            if (opAction.getOutputAction().getOutputNodeConnector().equals(new Uri(ncid))) {
                addNew = false;
                break;
            }
        }
        if (addNew) {
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());
        }
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        LOG.debug("createOutputPortInstructions() : applyAction {}", aab.build());
        return ib;
    }


    private Group getGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory
                .rev130819.nodes.Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class).child(Group.class,
                        new GroupKey(groupBuilder.getGroupId())).build();
        DataBroker databroker = getDataBroker();
        ReadOnlyTransaction readTx = databroker.newReadOnlyTransaction();
        try {
            Optional<Group> data = readTx.read(LogicalDatastoreType.CONFIGURATION, path1).get();
            if (data.isPresent()) {
                return data.get();
            }
        } catch (InterruptedException|ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.debug("Cannot find data for Group " + groupBuilder.getGroupName());
        return null;
    }

    private void writeGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
        DataBroker databroker = getDataBroker();
        ReadWriteTransaction modification = databroker.newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory
                .rev130819.nodes.Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class).child(Group.class,
                        new GroupKey(groupBuilder.getGroupId())).build();
        modification.put(LogicalDatastoreType.CONFIGURATION, path1, groupBuilder.build(), true /*createMissingParents*/);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        try {
            commitFuture.get();  // TODO: Make it async (See bug 1362)
            LOG.debug("Transaction success for write of Group " + groupBuilder.getGroupName());
        } catch (InterruptedException|ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /*
     * Used for flood to local port
     * groupId is segment id
     */
    protected InstructionBuilder createOutputGroupInstructionsToLocalPort(NodeBuilder nodeBuilder,
            InstructionBuilder ib,
            Long dpidLong, Long port , long groupId,
            List<Instruction> instructions) {
        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpidLong + ":" + port);
        LOG.debug("createOutputGroupInstructionsToLocalPort() Node Connector ID is - Type=openflow: DPID={} port={} existingInstructions={}", dpidLong, port, instructions);

        List<Action> actionList = Lists.newArrayList();
        ActionBuilder ab = new ActionBuilder();

        List<Action> existingActions;
        if (instructions != null) {
            for (Instruction in : instructions) {
                if (in.getInstruction() instanceof ApplyActionsCase) {
                    existingActions = (((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction());
                    actionList.addAll(existingActions);
                }
            }
        }

        GroupBuilder groupBuilder = new GroupBuilder();
        Group group = null;

        /* Create output action for this port*/
        OutputActionBuilder oab = new OutputActionBuilder();
        oab.setOutputNodeConnector(ncid);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        LOG.debug("createOutputGroupInstructionsToLocalPort(): output action {}", ab.build());
        boolean addNew = true;
        boolean groupActionAdded = false;

        /* Find the group action and get the group */
        for (Action action : actionList) {
            if (action.getAction() instanceof GroupActionCase) {
                groupActionAdded = true;
                GroupActionCase groupAction = (GroupActionCase) action.getAction();
                Long id = groupAction.getGroupAction().getGroupId();
                String groupName = groupAction.getGroupAction().getGroup();
                GroupKey key = new GroupKey(new GroupId(id));

                groupBuilder.setGroupId(new GroupId(id));
                groupBuilder.setGroupName(groupName);
                groupBuilder.setGroupType(GroupTypes.GroupAll);
                groupBuilder.setKey(key);
                group = getGroup(groupBuilder, nodeBuilder);
                LOG.debug("createOutputGroupInstructionsToLocalPort: group {}", group);
                break;
            }
        }

        BucketId bucketId = new BucketId((long) 1);

        LOG.debug("createOutputGroupInstructionsToLocalPort: groupActionAdded {}", groupActionAdded);
        if (groupActionAdded) {
            /* modify the action bucket in group */
            groupBuilder = new GroupBuilder(group);
            Buckets buckets = groupBuilder.getBuckets();
            for (Bucket bucket : buckets.getBucket()) {
                List<Action> bucketActions = bucket.getAction();
                LOG.debug("createOutputGroupInstructionsToLocalPort: bucketActions {}", bucketActions);
                if (bucket.getBucketId().getValue() == 1l) {
                    //for local port, the bucket id is always 1
                    for (Action action : bucketActions) {
                        if (action.getAction() instanceof OutputActionCase) {
                            OutputActionCase opAction = (OutputActionCase)action.getAction();
                            /* If output port action already in the action list of one of the buckets, skip */
                            if (opAction.getOutputAction().getOutputNodeConnector().equals(new Uri(ncid))) {
                                addNew = false;
                                break;
                            }
                        }
                    }

                    LOG.debug("createOutputGroupInstructionsToLocalPort: addNew {}", addNew);
                    if (addNew && !buckets.getBucket().isEmpty()) {
                        /* the new output action is not in the bucket, add to bucket */
                        //bucket = buckets.getBucket().get(0);
                        List<Action> bucketActionList = Lists.newArrayList();
                        bucketActionList.addAll(bucket.getAction());
                        /* set order for new action and add to action list */
                        ab.setOrder(bucketActionList.size());
                        ab.setKey(new ActionKey(bucketActionList.size()));
                        bucketActionList.add(ab.build());

                        /* set bucket and buckets list. Reset groupBuilder with new buckets.*/
                        BucketsBuilder bucketsBuilder = new BucketsBuilder();
                        List<Bucket> bucketList = Lists.newArrayList();
                        BucketBuilder bucketBuilder = new BucketBuilder();
                        bucketBuilder.setBucketId(new BucketId(bucketId));
                        bucketBuilder.setKey(new BucketKey(bucketId));
                        bucketBuilder.setAction(bucketActionList);
                        bucketList.add(bucketBuilder.build());
                        bucketsBuilder.setBucket(bucketList);
                        groupBuilder.setBuckets(bucketsBuilder.build());
                        LOG.debug("createOutputGroupInstructionsToLocalPort: bucketList {}", bucketList);
                    }
                    break;
                }
            }

        } else {
            /* create group */
            groupBuilder = new GroupBuilder();
            groupBuilder.setGroupType(GroupTypes.GroupAll);
            groupBuilder.setGroupId(new GroupId(groupId));
            groupBuilder.setKey(new GroupKey(new GroupId(groupId)));
            groupBuilder.setGroupName("Output port group " + groupId);
            groupBuilder.setBarrier(false);

            BucketsBuilder bucketBuilder = new BucketsBuilder();
            List<Bucket> bucketList = Lists.newArrayList();
            BucketBuilder bucket = new BucketBuilder();
            bucket.setBucketId(new BucketId(bucketId));
            bucket.setKey(new BucketKey(bucketId));

            /* put output action to the bucket */
            List<Action> bucketActionList = Lists.newArrayList();
            /* set order for new action and add to action list */
            ab.setOrder(bucketActionList.size());
            ab.setKey(new ActionKey(bucketActionList.size()));
            bucketActionList.add(ab.build());

            bucket.setAction(bucketActionList);
            bucketList.add(bucket.build());
            bucketBuilder.setBucket(bucketList);
            groupBuilder.setBuckets(bucketBuilder.build());

            /* Add new group action */
            GroupActionBuilder groupActionB = new GroupActionBuilder();
            groupActionB.setGroupId(groupId);
            groupActionB.setGroup("Output port group " + groupId);
            ab = new ActionBuilder();
            ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            //groupId++;
        }
        LOG.debug("createOutputGroupInstructionsToLocalPort: group {}", groupBuilder.build());
        LOG.debug("createOutputGroupInstructionsToLocalPort: actionList {}", actionList);

        if (addNew) {
            /* rewrite the group to group table */
            writeGroup(groupBuilder, nodeBuilder);
        }

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        return ib;
    }

    /*
     * Used for flood to tunnel port, for different dest Node, have different dest tunnel ip address
     * groupId is segment id
     */
    protected InstructionBuilder createOutputGroupInstructionsToTunnelPort(NodeBuilder nodeBuilder,
            InstructionBuilder ib,
            Long dpidLong, Long port , Long groupId, IpAddress destTunnelIp,
            List<Instruction> instructions) {
        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpidLong + ":" + port);
        LOG.debug("createOutputGroupInstructionsToTunnelPort() Node Connector ID is - Type=openflow: DPID={} port={} existingInstructions={}", dpidLong, port, instructions);

        List<Action> actionList = Lists.newArrayList();

        List<Action> existingActions;
        if (instructions != null) {
            for (Instruction in : instructions) {
                if (in.getInstruction() instanceof ApplyActionsCase) {
                    existingActions = (((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction());
                    actionList.addAll(existingActions);
                }
            }
        }

        GroupBuilder groupBuilder = new GroupBuilder();
        Group group = null;

        /* Create output action for this port*/
        ActionBuilder outPortActionBuilder = new ActionBuilder();
        ActionBuilder loadTunIPv4ActionBuilder = new ActionBuilder();

        OutputActionBuilder oab = new OutputActionBuilder();
        oab.setOutputNodeConnector(ncid);
        outPortActionBuilder.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        /* Create load tunnel ip action */
        loadTunIPv4ActionBuilder.setAction(ActionUtils.nxLoadTunIPv4Action(destTunnelIp.getIpv4Address().getValue(), false));

        boolean addNew = true;
        boolean groupActionAdded = false;

        /* Find the group action and get the group */
        for (Action action : actionList) {
            if (action.getAction() instanceof GroupActionCase) {
                groupActionAdded = true;
                GroupActionCase groupAction = (GroupActionCase) action.getAction();
                Long id = groupAction.getGroupAction().getGroupId();
                String groupName = groupAction.getGroupAction().getGroup();
                GroupKey key = new GroupKey(new GroupId(id));

                groupBuilder.setGroupId(new GroupId(id));
                groupBuilder.setGroupName(groupName);
                groupBuilder.setGroupType(GroupTypes.GroupAll);
                groupBuilder.setKey(key);
                group = getGroup(groupBuilder, nodeBuilder);
                LOG.debug("createOutputGroupInstructionsToTunnelPort: group {}", group);
                break;
            }
        }

        BucketId bucketId = null;

        //add tunnel port out, bucket_id=destTunnelIp.int
        int ip = InetAddresses.coerceToInteger(InetAddresses.forString(destTunnelIp.getIpv4Address().getValue()));
        long ipl = ip & 0xffffffffL;
        bucketId = new BucketId(ipl);

        if (groupActionAdded) {
            /* modify the action bucket in group */
            groupBuilder = new GroupBuilder(group);
            Buckets buckets = groupBuilder.getBuckets();

            for (Bucket bucket : buckets.getBucket()) {
                if ( (bucket.getBucketId().getValue() == bucketId.getValue())
                        && (bucket.getBucketId().getValue() != 1l) ) {
                    LOG.warn("Warning: createOutputGroupInstructionsToTunnelPort: the bucket is exsit for a tunnel port");
                    addNew = false;
                    //return null;
                }
            }
            if (addNew) {
                /* the new output action is not in the bucket, add to bucket */
                //Bucket bucket = buckets.getBucket().get(0);
                //BucketBuilder bucket = new BucketBuilder();
                List<Action> bucketActionList = Lists.newArrayList();
                //bucketActionList.addAll(bucket.getAction());
                /* set order for new action and add to action list */
                loadTunIPv4ActionBuilder.setOrder(0);
                loadTunIPv4ActionBuilder.setKey(new ActionKey(0));
                bucketActionList.add(loadTunIPv4ActionBuilder.build());

                outPortActionBuilder.setOrder(1);
                outPortActionBuilder.setKey(new ActionKey(1));
                bucketActionList.add(outPortActionBuilder.build());

                /* set bucket and buckets list. Reset groupBuilder with new buckets.*/
                BucketsBuilder bucketsBuilder = new BucketsBuilder();
                List<Bucket> bucketList = Lists.newArrayList();
                BucketBuilder bucketBuilder = new BucketBuilder();
                bucketBuilder.setBucketId(bucketId);
                bucketBuilder.setKey(new BucketKey(bucketId));
                bucketBuilder.setAction(bucketActionList);
                bucketList.add(bucketBuilder.build());
                bucketsBuilder.setBucket(bucketList);
                groupBuilder.setBuckets(bucketsBuilder.build());
                LOG.debug("createOutputGroupInstructionsToTunnelPort: bucketList {}", bucketList);
            }

        } else {
            /* create group */
            groupBuilder = new GroupBuilder();
            groupBuilder.setGroupType(GroupTypes.GroupAll);
            groupBuilder.setGroupId(new GroupId(groupId));
            groupBuilder.setKey(new GroupKey(new GroupId(groupId)));
            groupBuilder.setGroupName("Output port group " + groupId);
            groupBuilder.setBarrier(false);

            BucketsBuilder bucketBuilder = new BucketsBuilder();
            List<Bucket> bucketList = Lists.newArrayList();
            BucketBuilder bucket = new BucketBuilder();

            bucket.setBucketId(bucketId);
            bucket.setKey(new BucketKey(bucketId));

            /* put output action to the bucket */
            List<Action> bucketActionList = Lists.newArrayList();
            /* set order for new action and add to action list */
            loadTunIPv4ActionBuilder.setOrder(0);
            loadTunIPv4ActionBuilder.setKey(new ActionKey(0));
            bucketActionList.add(loadTunIPv4ActionBuilder.build());

            outPortActionBuilder.setOrder(1);
            outPortActionBuilder.setKey(new ActionKey(1));
            bucketActionList.add(outPortActionBuilder.build());

            bucket.setAction(bucketActionList);
            bucketList.add(bucket.build());
            bucketBuilder.setBucket(bucketList);
            groupBuilder.setBuckets(bucketBuilder.build());

            /* Add new group action */
            GroupActionBuilder groupActionB = new GroupActionBuilder();
            groupActionB.setGroupId(groupId);
            groupActionB.setGroup("Output port group " + groupId);
            ActionBuilder ab = new ActionBuilder();
            ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            //groupId++;
        }
        //LOG.debug("createOutputGroupInstructions: group {}", groupBuilder.build());
        //LOG.debug("createOutputGroupInstructions: actionList {}", actionList);

        if (addNew) {
            /* rewrite the group to group table */
            writeGroup(groupBuilder, nodeBuilder);
        }

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        return ib;
    }
}
