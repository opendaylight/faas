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
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline.AbstractServiceInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg2;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfActionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfInstructionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfMatchUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import com.google.common.collect.Lists;

public class PipelineTrafficClassifier extends AbstractServiceInstance {

    public final static long REG_VALUE_FROM_LOCAL = 0x1L;
    public final static long REG_VALUE_FROM_REMOTE = 0x2L;
    public final static long REG_VALUE_FROM_VLAN = 0x3L;

    public static final Class<? extends NxmNxReg> REG_FIELD = NxmNxReg0.class;
    public static final Class<? extends NxmNxReg> REG_SRC_TUN_ID = NxmNxReg2.class;

    public PipelineTrafficClassifier(DataBroker dataBroker) {
        super(Service.TRAFFIC_CLASSIFIER, dataBroker);
        //PipelineOrchestrator.setServiceRegistry(Service.TRAFFIC_CLASSIFIER, this);
    }

    /*
     * (Table:  PipelineTrafficClassifier) Egress VM Traffic
     * Match:   VM Source Ethernet Addr , OpenFlow InPort
     * Actions: Set TunnelID ,Load NXM_NX_REG0 to identify the local VM traffic, GOTO Next Table
     * Flow example:
     *      table=TRAFFIC_CLASSIFIER,in_port=2,dl_src=00:00:00:00:00:01 \
     *      actions=set_field:5->tun_id,load:0x1->NXM_NX_REG0,goto_table=<next-table>"
     */
    public void programLocalInPort(Long dpid, Long segmentationId, Long vlanId, Long inPort, String attachedMac, boolean isWriteFlow) {
        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(OfMatchUtils.createEthSrcMatch(matchBuilder, attachedMac).build());
        flowBuilder.setMatch(OfMatchUtils.createInPortMatch(matchBuilder, dpid, inPort).build());
        if (vlanId != null) {
            flowBuilder.setMatch(OfMatchUtils.createVlanIdMatch(matchBuilder, new VlanId(vlanId.intValue()), true).build());
        }

        String flowId = "LocalMac_"+segmentationId+"_"+inPort+"_"+attachedMac;
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

        if (isWriteFlow) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

            OfInstructionUtils.createSetTunnelIdInstructions(ib, BigInteger.valueOf(segmentationId.longValue()));

            ApplyActionsCase aac = (ApplyActionsCase) ib.getInstruction();
            List<Action> actionList = aac.getApplyActions().getAction();

            ActionBuilder ab = new ActionBuilder();

            if (vlanId != null) {
                ab.setAction(OfActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_FIELD).build(),
                        BigInteger.valueOf(REG_VALUE_FROM_VLAN)));
                ab.setOrder(actionList.size());
                ab.setKey(new ActionKey(actionList.size()));
                actionList.add(ab.build());
            }
            else {
                ab.setAction(OfActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_FIELD).build(),
                        BigInteger.valueOf(REG_VALUE_FROM_LOCAL)));
                ab.setOrder(actionList.size());
                ab.setKey(new ActionKey(actionList.size()));
                actionList.add(ab.build());
            }
            ab.setAction(OfActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_SRC_TUN_ID).build(),
                    BigInteger.valueOf(segmentationId.longValue())));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Next service GOTO Instructions Need to be appended to the List
            ib = this.getMutablePipelineInstructionBuilder();
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
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
     * (Table:  PipelineTrafficClassifier) Drop Traffic
     * Match:   OpenFlow InPort
     * Actions: Drop
     * Flow example:
     *      table=TRAFFIC_CLASSIFIER,in_port=2 \
     *      actions=drop"
     */
    public void programDropSrcIface(Long dpid, Long inPort, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(OfMatchUtils.createInPortMatch(matchBuilder, dpid, inPort).build());

        if (isWriteFlow) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

            // Call the InstructionBuilder Methods Containing Actions
            OfInstructionUtils.createDropInstructions(ib);
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());
        }

        String flowId = "DropFilter_"+inPort;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setPriority(8192);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        if (isWriteFlow) {
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    /*
     * (Table:  PipelineTrafficClassifier) Input Traffic from TunnelPort
     * Match:   TunnelID , OpenFlow InPort
     * Actions: Load NXM_NX_REG0 to identify the Remote traffic from TunnelPort, GOTO Next Table
     * Flow example:
     *      table=TRAFFIC_CLASSIFIER, tun_id=0x5, in_port=2, \
     *      actions=load:0x2->NXM_NX_REG0,goto_table=<next-table>"
     */
    public void programTunnelIn(Long dpid, Long segmentationId,
            Long ofPort, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        BigInteger tunnelId = BigInteger.valueOf(segmentationId.longValue());
        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create Match(es) and Set them in the FlowBuilder Object
        flowBuilder.setMatch(OfMatchUtils.createTunnelIDMatch(matchBuilder, tunnelId).build());
        flowBuilder.setMatch(OfMatchUtils.createInPortMatch(matchBuilder, dpid, ofPort).build());

        if (isWriteFlow) {
            // Create the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

            List<Action> actionList = Lists.newArrayList();
            ActionBuilder ab = new ActionBuilder();
            ab.setAction(OfActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_FIELD).build(),
                    BigInteger.valueOf(REG_VALUE_FROM_REMOTE)));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            ab.setAction(OfActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_SRC_TUN_ID).build(),
                    tunnelId));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);
            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

            // Call the InstructionBuilder Methods Containing Actions
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Append the default pipeline after the first classification
            ib = this.getMutablePipelineInstructionBuilder();
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());
        }

        String flowId = "TunnelIn_"+segmentationId+"_"+ofPort;
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

        if (isWriteFlow) {
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    public void programVlanInPort(Long dpid, Long vlanId, Long segmentationId, Long inPort, boolean isWriteFlow) {
        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(OfMatchUtils.createInPortMatch(matchBuilder, dpid, inPort).build());
        flowBuilder.setMatch(OfMatchUtils.createVlanIdMatch(matchBuilder, new VlanId(vlanId.intValue()), true).build());

        String flowId = "VlanIn_"+vlanId+"_"+inPort+"_"+segmentationId;
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

        if (isWriteFlow) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

            OfInstructionUtils.createSetTunnelIdInstructions(ib, BigInteger.valueOf(segmentationId.longValue()));

            ApplyActionsCase aac = (ApplyActionsCase) ib.getInstruction();
            List<Action> actionList = aac.getApplyActions().getAction();

            ActionBuilder ab = new ActionBuilder();

            ab.setAction(OfActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_FIELD).build(),
                    BigInteger.valueOf(REG_VALUE_FROM_VLAN)));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Next service GOTO Instructions Need to be appended to the List
            ib = this.getMutablePipelineInstructionBuilder();
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
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

}
