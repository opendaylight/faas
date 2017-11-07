/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.CheckedFuture;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Constants;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfActionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfInstructionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfMatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.VlanCfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.strip.vlan.action._case.StripVlanActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxTunIdCaseBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineL2Forwarding extends AbstractServiceInstance {
    private static final Logger LOG = LoggerFactory.getLogger(PipelineL2Forwarding.class);

    public PipelineL2Forwarding(DataBroker dataBroker) {
        super(Service.L2_FORWARDING, dataBroker);
    }

    /*
     * To Local port unicast traffic Match: TunnelID , Dest Mac Actions: Output
     * to local port, Flow example: table=110, n_packets=2, n_bytes=196,
     * tun_id=0x3ea,dl_dst=fa:16:3e:41:56:ec , \ actions=output:1"
     */
    public void programLocalUcastOut(Long dpid, Long segmentationId, Long vlanId, Long localPort, String attachedMac,
            boolean isWriteFlow) {
        programLocalUcastOutForUntagged(dpid,  segmentationId,  vlanId,  localPort,  attachedMac,
                 isWriteFlow) ;
        programLocalUcastOutForTagged( dpid,  segmentationId,  vlanId,  localPort,  attachedMac,
                     isWriteFlow);
        }

    private void programLocalUcastOutForTagged(Long dpid, Long segmentationId, Long vlanId, Long localPort, String attachedMac,
                boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();


        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        flowBuilder.setMatch(OfMatchUtils.createDestEthMatch(matchBuilder, attachedMac, "").build());

        //Matching all Tagged VLANs
        OfMatchUtils.createVlanIdMatch(matchBuilder, new VlanId(0), true);
        flowBuilder.setMatch(matchBuilder.build());


        String flowId;
        if (vlanId != null) {
            flowId = "UcastOutForTagged_" + segmentationId + "_" + localPort + "_" + attachedMac + "_" + vlanId;
        } else {
            flowId = "UcastOutForTagged_" + segmentationId + "_" + localPort + "_" + attachedMac;
        }
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
        flowBuilder.setPriority(1024);

        if (isWriteFlow) {
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();
            List<Action> actionList = new ArrayList<>();
            List<Instruction> instructions = Lists.newArrayList();
            ActionBuilder popVlanActionBuilder = new ActionBuilder();
            ActionBuilder setVlanActionBuilder = new ActionBuilder();

            if (vlanId != null) {
                SetVlanIdActionBuilder vl = new SetVlanIdActionBuilder();
                vl.setVlanId(new VlanId(vlanId.intValue()));
                setVlanActionBuilder.setAction(new SetVlanIdActionCaseBuilder().setSetVlanIdAction(vl.build()).build());
                setVlanActionBuilder.setOrder(actionList.size());
                setVlanActionBuilder.setKey(new ActionKey(actionList.size()));
                actionList.add(setVlanActionBuilder.build());
            } else {
                PopVlanActionBuilder popVlan = new PopVlanActionBuilder();
                popVlanActionBuilder.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(popVlan.build()).build());
                popVlanActionBuilder.setOrder(actionList.size());
                popVlanActionBuilder.setKey(new ActionKey(actionList.size()));
                actionList.add(popVlanActionBuilder.build());
            }


            /* Set output port */
            ActionBuilder outPortActionBuilder = new ActionBuilder();

            OutputActionBuilder oab = new OutputActionBuilder();
            NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + localPort);
            oab.setOutputNodeConnector(ncid);
            outPortActionBuilder.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());

            outPortActionBuilder.setOrder(actionList.size());
            outPortActionBuilder.setKey(new ActionKey(actionList.size()));
            actionList.add(outPortActionBuilder.build());

            ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);

            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));

            instructions.add(ib.build());
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    private void programLocalUcastOutForUntagged(Long dpid, Long segmentationId, Long vlanId, Long localPort, String attachedMac,
            boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        flowBuilder.setMatch(OfMatchUtils.createDestEthMatch(matchBuilder, attachedMac, "").build());

        // Match no VLANs
        OfMatchUtils.createVlanIdMatch(matchBuilder, new VlanId(0), false);
        flowBuilder.setMatch(matchBuilder.build());

        String flowId;
        if (vlanId != null) {
            flowId = "UcastOutForUntagged_" + segmentationId + "_" + localPort + "_" + attachedMac + "_" + vlanId;
        } else {
            flowId = "UcastOutForUntagged_" + segmentationId + "_" + localPort + "_" + attachedMac;
        }
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
        flowBuilder.setPriority(1023); //lower than L3

        if (isWriteFlow) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();
            List<Action> actionList = new ArrayList<>();
            List<Instruction> instructions = Lists.newArrayList();
            ActionBuilder pushVlanActionBuilder = new ActionBuilder();
            ActionBuilder setVlanActionBuilder = new ActionBuilder();


           if (vlanId != null) {
                PushVlanActionBuilder pvl = new PushVlanActionBuilder();
                pvl.setEthernetType(0x8100);

                //Why this one doesn't work? we have to use a set VLAN action explicitly?
                pvl.setVlanId(new VlanId(vlanId.intValue()));
                pushVlanActionBuilder
                        .setAction(new PushVlanActionCaseBuilder().setPushVlanAction(pvl.build()).build());

                pushVlanActionBuilder.setOrder(actionList.size());
                pushVlanActionBuilder.setKey(new ActionKey(actionList.size()));
                actionList.add(pushVlanActionBuilder.build());

                //Set vlan id value as vlanId
                SetVlanIdActionBuilder svl = new SetVlanIdActionBuilder();
                svl.setVlanId(new VlanId(vlanId.intValue()));
                setVlanActionBuilder.setAction(new SetVlanIdActionCaseBuilder().setSetVlanIdAction(svl.build()).build());

                setVlanActionBuilder.setOrder(actionList.size());
                setVlanActionBuilder.setKey(new ActionKey(actionList.size()));
                actionList.add(setVlanActionBuilder.build());
            }
            /* Set output port */
            ActionBuilder outPortActionBuilder = new ActionBuilder();

            OutputActionBuilder oab = new OutputActionBuilder();
            NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + localPort);
            oab.setOutputNodeConnector(ncid);
            outPortActionBuilder.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());

            outPortActionBuilder.setOrder(actionList.size());
            outPortActionBuilder.setKey(new ActionKey(actionList.size()));
            actionList.add(outPortActionBuilder.build());

            ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);

            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));

            instructions.add(ib.build());
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }



    /*
     * Local Table Miss Match: Any Remaining Flows w/a TunID Action: Drop w/ a
     * low priority Example: table=110,priority=8192,tun_id=0x5 actions=drop
     */
    public void programLocalTableMiss(Long dpid, Long segmentationId, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create Match(es) and Set them in the FlowBuilder Object
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());

        if (isWriteFlow) {
            // Create the OF Actions and Instructions
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

        String flowId = "LocalTableMiss_" + segmentationId;
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
        if (isWriteFlow) {
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    /*
     * (Table: L2_FORWARDING) To Tunnel port unicast traffic Match: TunnelID ,
     * Dest Mac Actions: Load dest Tunnel ip address, Output to Tunnel port Flow
     * example: table=110, n_packets=2, n_bytes=196,
     * tun_id=0x3ea,dl_dst=fa:16:3e:41:56:ec , \
     * actions=load:0xc0a876a1->NXM_NX_TUN_IPV4_DST[],output:2"
     */
    public void programTunnelOut(Long dpid, Long segmentationId, Long OFPortOut, String attachedMac,
            IpAddress dstTunIpAddress, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        flowBuilder.setMatch(OfMatchUtils.createDestEthMatch(matchBuilder, attachedMac, "").build());

        String flowId = "TunnelOut_" + segmentationId + "_" + OFPortOut + "_" + attachedMac + "_"
                + dstTunIpAddress.getIpv4Address().getValue();
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(32768);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (isWriteFlow) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();
            List<Action> actionList = new ArrayList<>();
            List<Instruction> instructions = Lists.newArrayList();

            ActionBuilder ab = new ActionBuilder();

            // add Load Tunnel Ip Action
            ab.setAction(OfActionUtils.nxLoadTunIPv4Action(dstTunIpAddress.getIpv4Address().getValue(), false));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // add Output Action
            NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + OFPortOut);
            ab.setAction(OfActionUtils.outputAction(ncid));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);

            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));

            instructions.add(ib.build());
            isb.setInstruction(instructions);

            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    public void programNexthopTunnelOut(Long dpid, Long OFPortOut, IpAddress dstTunIpAddress, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        OfMatchUtils.addNxRegMatch(matchBuilder, new OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
                PipelineL3Routing.REG_VALUE_IS_STATIC_ROUTING));
        flowBuilder.setMatch(matchBuilder.build());

        String flowId = "NexthopTunnelOut_" + OFPortOut + "_" + dstTunIpAddress.getIpv4Address().getValue();
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(32769);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (isWriteFlow) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();
            List<Action> actionList = new ArrayList<>();
            List<Instruction> instructions = Lists.newArrayList();

            ActionBuilder ab = new ActionBuilder();

            // add Load Tunnel Ip Action
            ab.setAction(OfActionUtils.nxLoadTunIPv4Action(dstTunIpAddress.getIpv4Address().getValue(), false));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // add Output Action
            NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + OFPortOut);
            ab.setAction(OfActionUtils.outputAction(ncid));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);

            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));

            instructions.add(ib.build());
            isb.setInstruction(instructions);

            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    public void programSfcTunnelOut(Long dpid, Long segmentationId, Long OFSfcTunPort, String attachedMac,
            IpAddress dstVmVtepIp, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        OfMatchUtils.addNxRegMatch(matchBuilder,
                new OfMatchUtils.RegMatch(PipelineAclHandler.REG_SFC_FIELD, PipelineAclHandler.REG_VALUE_SFC_REDIRECT));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        flowBuilder.setMatch(OfMatchUtils.createDestEthMatch(matchBuilder, attachedMac, "").build());

        flowBuilder.setMatch(matchBuilder.build());

        String flowId = "SfcTunnelOut_" + segmentationId + "_" + OFSfcTunPort + "_" + attachedMac + "_"
                + dstVmVtepIp.getIpv4Address().getValue();

        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        // Priority is bigger than TunnelOut_** flow
        flowBuilder.setPriority(32769);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (isWriteFlow) {
            // Instantiate the Builders for the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();
            List<Action> actionList = new ArrayList<>();
            List<Instruction> instructions = Lists.newArrayList();

            ActionBuilder ab = new ActionBuilder();

            //load Tunnel gpe np
            ab.setAction(OfActionUtils.nxLoadTunGpeNpAction(Short.valueOf((short)0x4)));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Load Dest Vm Vtep IP to Nshc1 Register
            int ip = InetAddresses.coerceToInteger(InetAddresses.forString(dstVmVtepIp.getIpv4Address().getValue()));
            long ipl = ip & 0xffffffffL;
            ab.setAction(OfActionUtils.nxLoadNshc1RegAction(ipl));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Load Dest Vm VNI to Nshc1 Register
            ab.setAction(OfActionUtils.nxLoadNshc2RegAction(segmentationId));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Load Dest Vm VNI to TUN_ID
            ab.setAction(nxLoadTunIdAction(segmentationId));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // add Output Action
            NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpid + ":" + OFSfcTunPort);
            ab.setAction(OfActionUtils.outputAction(ncid));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);

            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));

            instructions.add(ib.build());
            isb.setInstruction(instructions);

            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxLoadTunIdAction(
            Long value) {
        return OfActionUtils.nxLoadRegAction(new DstNxTunIdCaseBuilder().setNxTunId(Boolean.TRUE).build(),
                BigInteger.valueOf(value));
    }

    /*
     * Broadcast traffic from Remote device Match: NXM_NX_REG0=0x2 , TunnelId
     * Actions: Output to local port belongs to this Tunnel bridge domain Flow
     * example: table=110,
     * reg0=0x2,tun_id=0x3ea,dl_dst=01:00:00:00:00:00/01:00:00:00:00:00 , \
     * actions=output:3,output:5"
     */
    public void programRemoteBcastOutToLocalPort(Long dpid, Long segmentationId, Long localPort, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        OfMatchUtils.addNxRegMatch(matchBuilder, new OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
                PipelineTrafficClassifier.REG_VALUE_FROM_REMOTE));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "BcastOut_" + segmentationId;
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

        if (isWriteFlow) {
            // Create output port list
            createOutputPortInstructions(ib, dpid, localPort, existingInstructions);
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            boolean flowRemove = OfInstructionUtils.removeOutputPortFromInstructions(ib, dpid, localPort,
                    existingInstructions);
            if (flowRemove) {
                /* if all ports are removed, remove flow */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
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
     * (Table: L2_FORWARDING) Broadcast packet from local port, to other local
     * porte Match: NXM_NX_REG0=0x1 , TunnelId Actions: group:TunnelId Flow
     * example: table=110,
     * reg0=0x1,tun_id=0x3ea,dl_dst=01:00:00:00:00:00/01:00:00:00:00:00 , \
     * actions=group:0x3ea"
     *
     * groupid=0x3ea bucketid=1, actions=output:1,output2; bucketid: 1 is for
     * all local ports
     */
    public void programLocalBcastToLocalPort(Long dpid, Long segmentationId, Long OFLocalPortOut, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        OfMatchUtils.addNxRegMatch(matchBuilder, new OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
                PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "TunnelFloodOut_" + segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(16383); // FIXME: change it back to 16384 once
                                        // bug 3005 is fixed.
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

        if (isWriteFlow) {
            // Set the Output Port/Iface
            createOutputGroupInstructionsToLocalPort(nodeBuilder, ib, dpid, OFLocalPortOut, segmentationId,
                    existingInstructions);
            // createOutputPortInstructions(ib, dpid, OFLocalPortOut,
            // existingInstructions);
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            /* remove port from the bucket used for local port, bucketid=1 */
            boolean flowRemove = removeActionFromGroup(nodeBuilder, ib, dpid, OFLocalPortOut, null,
                    existingInstructions);
            if (flowRemove) {
                /* if all port are removed, remove the flow too. */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
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
     * (Table: L2_FORWARDING) Broadcast packet from local port, to tunnel port,
     * each tunnel port, create a single flow Match: NXM_NX_REG0=0x1 , TunnelId
     * Actions: group:TunnelId Flow example: table=110,
     * reg0=0x1,tun_id=0x3ea,dl_dst=01:00:00:00:00:00/01:00:00:00:00:00 , \
     * actions=group:0x3ea"
     *
     * groupid=0x3ea bucketid=destTunnelIp,
     * actions=load:0xc0a876a1->NXM_NX_TUN_IPV4_DST[],output:3; bucket id is
     * dest tunnel ip
     */
    public void programLocalBcastToTunnelPort(Long dpid, Long segmentationId, Long OFTunnelPortOut,
            IpAddress dstTunIpAddress, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        OfMatchUtils.addNxRegMatch(matchBuilder, new OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
                PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "TunnelFloodOut_" + segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(16383); // FIXME: change it back to 16384 once
                                        // bug 3005 is fixed.
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

        if (isWriteFlow) {
            // create a bucket to group bucket list,
            // bucketid=(long)dstTunIpAddress
            createOutputGroupInstructionsToTunnelPort(nodeBuilder, ib, dpid, OFTunnelPortOut, segmentationId,
                    dstTunIpAddress, existingInstructions);

            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            /* remove port from the bucket used for local port, bucketid=1 */
            boolean flowRemove = removeActionFromGroup(nodeBuilder, ib, dpid, OFTunnelPortOut, dstTunIpAddress,
                    existingInstructions);
            if (flowRemove) {
                /* if all port are removed, remove the flow too. */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
                instructions.add(ib.build());

                // Add InstructionBuilder to the Instruction(s)Builder List
                isb.setInstruction(instructions);

                // Add InstructionsBuilder to FlowBuilder
                flowBuilder.setInstructions(isb.build());
                writeFlow(flowBuilder, nodeBuilder);
            }
        }
    }

    public void programLocalBcastToVlanPort(Long dpid, Long segmentationId, Long OfVlanPortOut, Long vlanId,
            boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        OfMatchUtils.addNxRegMatch(matchBuilder, new OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
                PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "LocalBcastToVlanPort_" + segmentationId + vlanId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(16385);
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

        if (isWriteFlow) {
            // create a bucket to group bucket list,
            // bucketid=(long)dstTunIpAddress
            createOutputGroupInstructionsToVlanPort(nodeBuilder, ib, dpid, OfVlanPortOut, segmentationId, vlanId,
                    existingInstructions);

            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            /* remove port from the bucket used for local port, bucketid=1 */
            boolean flowRemove = removeVlanActionFromGroup(nodeBuilder, ib, dpid, OfVlanPortOut, vlanId,
                    existingInstructions);
            if (flowRemove) {
                /* if all port are removed, remove the flow too. */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
                instructions.add(ib.build());

                // Add InstructionBuilder to the Instruction(s)Builder List
                isb.setInstruction(instructions);

                // Add InstructionsBuilder to FlowBuilder
                flowBuilder.setInstructions(isb.build());
                writeFlow(flowBuilder, nodeBuilder);
            }
        }
    }

    public void programRemoteBcastToVlanPort(Long dpid, Long segmentationId, Long OfVlanPortOut, Long vlanId,
            boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        OfMatchUtils.addNxRegMatch(matchBuilder, new OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
                PipelineTrafficClassifier.REG_VALUE_FROM_REMOTE));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "RemoteBcastToVlanPort_" + segmentationId + vlanId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(16385);
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

        if (isWriteFlow) {
            // create a bucket to group bucket list,
            // bucketid=(long)dstTunIpAddress
            createOutputGroupInstructionsToVlanPort(nodeBuilder, ib, dpid, OfVlanPortOut, segmentationId, vlanId,
                    existingInstructions);

            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            /* remove port from the bucket used for local port, bucketid=1 */
            boolean flowRemove = removeVlanActionFromGroup(nodeBuilder, ib, dpid, OfVlanPortOut, vlanId,
                    existingInstructions);
            if (flowRemove) {
                /* if all port are removed, remove the flow too. */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
                instructions.add(ib.build());

                // Add InstructionBuilder to the Instruction(s)Builder List
                isb.setInstruction(instructions);

                // Add InstructionsBuilder to FlowBuilder
                flowBuilder.setInstructions(isb.build());
                writeFlow(flowBuilder, nodeBuilder);
            }
        }
    }

    protected InstructionBuilder createOutputPortInstructions(InstructionBuilder ib, Long dpid, Long port,
            List<Instruction> instructions) {
        NodeConnectorId ncid = new NodeConnectorId(OPENFLOW + dpid + ":" + port);
        LOG.debug(
                "createOutputPortInstructions() Node Connector ID is - Type=openflow: DPID={} port={} existingInstructions={}",
                dpid, port, instructions);

        List<Action> actionList = Lists.newArrayList();
        ActionBuilder ab = new ActionBuilder();

        List<Action> existingActions;
        if (instructions != null && instructions.size() > 0) {
            /**
             * First instruction is the one containing the output ports. So,
             * only extract the actions from that.
             */
            Instruction in = instructions.get(0);
            if (in.getInstruction() instanceof ApplyActionsCase) {
                existingActions = ((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction();
                // Only include output actions
                for (Action action : existingActions) {
                    if (action.getAction() instanceof OutputActionCase) {
                        actionList.add(action);
                    }
                }
            }
        }
        /* Create output action for this port */
        OutputActionBuilder oab = new OutputActionBuilder();
        oab.setOutputNodeConnector(ncid);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        boolean addNew = true;

        /* Find the group action and get the group */
        for (Action action : actionList) {
            OutputActionCase opAction = (OutputActionCase) action.getAction();
            /*
             * If output port action already in the action list of one of the
             * buckets, skip
             */

            if (opAction.getOutputAction().getOutputNodeConnector().equals(ncid)) {
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
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(groupBuilder.getGroupId()))
                .build();
        DataBroker databroker = getDataBroker();
        ReadOnlyTransaction readTx = databroker.newReadOnlyTransaction();
        try {
            Optional<Group> data = readTx.read(LogicalDatastoreType.CONFIGURATION, path1).get();
            if (data.isPresent()) {
                return data.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.debug("Cannot find data for Group " + groupBuilder.getGroupName());
        return null;
    }

    private void writeGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
        DataBroker databroker = getDataBroker();
        ReadWriteTransaction modification = databroker.newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(groupBuilder.getGroupId()))
                .build();
        // modification.put(LogicalDatastoreType.CONFIGURATION, path1,
        // groupBuilder.build(), true /*createMissingParents*/);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, groupBuilder.build(),
                true /* createMissingParents */);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        try {
            commitFuture.get();
            LOG.debug("Transaction success for write of Group " + groupBuilder.getGroupName());
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void removeGroup(GroupBuilder groupBuilder, NodeBuilder nodeBuilder) {
        DataBroker databroker = getDataBroker();
        WriteTransaction modification = databroker.newWriteOnlyTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(groupBuilder.getGroupId()))
                .build();
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();

        try {
            commitFuture.get();
            LOG.debug("Transaction success for deletion of Group " + groupBuilder.getGroupName());
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /*
     * Used for flood to local port groupId is segment id bucketId is always 1,
     * all local port in this flood domain use one bucket ID
     */
    protected InstructionBuilder createOutputGroupInstructionsToLocalPort(NodeBuilder nodeBuilder,
            InstructionBuilder ib, Long dpid, Long port, long groupId, List<Instruction> instructions) {
        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpid + ":" + port);
        LOG.debug(
                "createOutputGroupInstructionsToLocalPort() Node Connector ID is - Type=openflow: DPID={} port={} existingInstructions={}",
                dpid, port, instructions);

        List<Action> actionList = Lists.newArrayList();
        ActionBuilder ab = new ActionBuilder();

        List<Action> existingActions;
        if (instructions != null) {
            for (Instruction in : instructions) {
                if (in.getInstruction() instanceof ApplyActionsCase) {
                    existingActions = ((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction();
                    actionList.addAll(existingActions);
                }
            }
        }

        GroupBuilder groupBuilder = new GroupBuilder();
        Group group = null;

        /* Create output action for this port */
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
            // find the bucket used for local ports
            int bucketIndex = -1;
            int tempIndex = -1;
            for (Bucket bucket : buckets.getBucket()) {
                tempIndex++;
                if (bucket.getBucketId().getValue() == 1l) {
                    // for local port, the bucket id is always 1
                    List<Action> bucketActions = bucket.getAction();
                    for (Action action : bucketActions) {
                        if (action.getAction() instanceof OutputActionCase) {
                            OutputActionCase opAction = (OutputActionCase) action.getAction();
                            /*
                             * If output port action already in the action list
                             * of one of the buckets, skip
                             */
                            if (opAction.getOutputAction().getOutputNodeConnector().equals(ncid)) {
                                addNew = false;
                                break;
                            }
                        }
                    }
                    bucketIndex = tempIndex;
                    break;
                }
            }

            if (addNew) {
                BucketsBuilder bucketsBuilder = new BucketsBuilder();
                BucketBuilder bucketBuilder = new BucketBuilder();
                List<Action> bucketActionList = Lists.newArrayList();
                List<Bucket> bucketList = Lists.newArrayList();

                for (Bucket bucket : buckets.getBucket()) {
                    if (bucket.getBucketId().getValue() != 1l) {
                        bucketList.add(bucket);
                    }
                }

                if (bucketIndex != -1) {
                    /*
                     * Bucket exsit, but the new output action is not in the
                     * exsited bucket, add to bucket
                     */
                    Bucket bucket = buckets.getBucket().get(bucketIndex);

                    bucketActionList.addAll(bucket.getAction());
                    /* set order for new action and add to action list */
                    ab.setOrder(bucketActionList.size());
                    ab.setKey(new ActionKey(bucketActionList.size()));
                    bucketActionList.add(ab.build());
                } else {
                    /* Create new bucket, and add output action to the bucket */
                    ab.setOrder(bucketActionList.size());
                    ab.setKey(new ActionKey(bucketActionList.size()));
                    bucketActionList.add(ab.build());
                }

                /*
                 * set bucket and buckets list. Reset groupBuilder with new
                 * buckets.
                 */
                bucketBuilder.setBucketId(bucketId);
                bucketBuilder.setKey(new BucketKey(bucketId));
                bucketBuilder.setAction(bucketActionList);

                bucketList.add(bucketBuilder.build());
                bucketsBuilder.setBucket(bucketList);
                groupBuilder.setBuckets(bucketsBuilder.build());
            }

        } else {
            /* create group */
            groupBuilder = new GroupBuilder();
            groupBuilder.setGroupType(GroupTypes.GroupAll);
            groupBuilder.setGroupId(new GroupId(groupId));
            groupBuilder.setKey(new GroupKey(new GroupId(groupId)));
            groupBuilder.setGroupName("Output port group " + groupId);
            groupBuilder.setBarrier(false);

            BucketsBuilder bucketsBuilder = new BucketsBuilder();
            List<Bucket> bucketList = Lists.newArrayList();
            BucketBuilder bucketBuilder = new BucketBuilder();
            bucketBuilder.setBucketId(bucketId);
            bucketBuilder.setKey(new BucketKey(bucketId));

            /* put output action to the bucket */
            List<Action> bucketActionList = Lists.newArrayList();
            /* set order for new action and add to action list */
            ab.setOrder(bucketActionList.size());
            ab.setKey(new ActionKey(bucketActionList.size()));
            bucketActionList.add(ab.build());

            bucketBuilder.setAction(bucketActionList);
            bucketList.add(bucketBuilder.build());
            bucketsBuilder.setBucket(bucketList);
            groupBuilder.setBuckets(bucketsBuilder.build());

            /* Add new group action */
            GroupActionBuilder groupActionB = new GroupActionBuilder();
            groupActionB.setGroupId(groupId);
            groupActionB.setGroup("Output port group " + groupId);
            ab = new ActionBuilder();
            ab.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // groupId++;
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
     * Used for flood to tunnel port, for different dest Node, have different
     * dest tunnel ip address groupId is segment id
     */
    protected InstructionBuilder createOutputGroupInstructionsToTunnelPort(NodeBuilder nodeBuilder,
            InstructionBuilder ib, Long dpid, Long port, Long groupId, IpAddress destTunnelIp,
            List<Instruction> instructions) {
        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpid + ":" + port);
        // LOG.debug("createOutputGroupInstructionsToTunnelPort() Node Connector
        // ID is - Type=openflow: DPID={} port={} existingInstructions={}",
        // dpid, port, instructions);

        List<Action> actionList = Lists.newArrayList();

        List<Action> existingActions;
        if (instructions != null) {
            for (Instruction in : instructions) {
                if (in.getInstruction() instanceof ApplyActionsCase) {
                    existingActions = ((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction();
                    actionList.addAll(existingActions);
                }
            }
        }

        GroupBuilder groupBuilder = new GroupBuilder();
        Group group = null;

        /* Create output action for this port */
        ActionBuilder outPortActionBuilder = new ActionBuilder();
        ActionBuilder loadTunIPv4ActionBuilder = new ActionBuilder();

        OutputActionBuilder oab = new OutputActionBuilder();
        oab.setOutputNodeConnector(ncid);
        outPortActionBuilder.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        /* Create load tunnel ip action */
        loadTunIPv4ActionBuilder
                .setAction(OfActionUtils.nxLoadTunIPv4Action(destTunnelIp.getIpv4Address().getValue(), false));

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

        // add tunnel port out, bucket_id=destTunnelIp.int
        int ip = InetAddresses.coerceToInteger(InetAddresses.forString(destTunnelIp.getIpv4Address().getValue()));
        long ipl = ip & 0xffffffffL;
        bucketId = new BucketId(ipl);

        if (groupActionAdded) {
            /* modify the action bucket in group */
            groupBuilder = new GroupBuilder(group);
            Buckets buckets = groupBuilder.getBuckets();

            for (Bucket bucket : buckets.getBucket()) {
                if (bucket.getBucketId().getValue() == bucketId.getValue() && bucket.getBucketId().getValue() != 1l) {
                    LOG.warn(
                            "Warning: createOutputGroupInstructionsToTunnelPort: the bucket is exsit for a tunnel port");
                    addNew = false;
                    break;
                }
            }
            if (addNew) {
                /* the new output action is not in the bucket, add to bucket */
                // Bucket bucket = buckets.getBucket().get(0);
                // BucketBuilder bucket = new BucketBuilder();
                List<Action> bucketActionList = Lists.newArrayList();
                // bucketActionList.addAll(bucket.getAction());
                /* set order for new action and add to action list */
                loadTunIPv4ActionBuilder.setOrder(bucketActionList.size());
                loadTunIPv4ActionBuilder.setKey(new ActionKey(bucketActionList.size()));
                bucketActionList.add(loadTunIPv4ActionBuilder.build());

                outPortActionBuilder.setOrder(bucketActionList.size());
                outPortActionBuilder.setKey(new ActionKey(bucketActionList.size()));
                bucketActionList.add(outPortActionBuilder.build());

                /*
                 * set bucket and buckets list. Reset groupBuilder with new
                 * buckets.
                 */
                BucketsBuilder bucketsBuilder = new BucketsBuilder();
                List<Bucket> bucketList = Lists.newArrayList();
                bucketList.addAll(buckets.getBucket());

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
            loadTunIPv4ActionBuilder.setOrder(bucketActionList.size());
            loadTunIPv4ActionBuilder.setKey(new ActionKey(bucketActionList.size()));
            bucketActionList.add(loadTunIPv4ActionBuilder.build());

            outPortActionBuilder.setOrder(bucketActionList.size());
            outPortActionBuilder.setKey(new ActionKey(bucketActionList.size()));
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

            // groupId++;
        }
        // LOG.debug("createOutputGroupInstructions: group {}",
        // groupBuilder.build());
        // LOG.debug("createOutputGroupInstructions: actionList {}",
        // actionList);

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

    protected InstructionBuilder createOutputGroupInstructionsToVlanPort(NodeBuilder nodeBuilder, InstructionBuilder ib,
            Long dpid, Long port, Long groupId, Long vlanId, List<Instruction> instructions) {
        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpid + ":" + port);
        // LOG.debug("createOutputGroupInstructionsToTunnelPort() Node Connector
        // ID is - Type=openflow: DPID={} port={} existingInstructions={}",
        // dpid, port, instructions);

        List<Action> actionList = Lists.newArrayList();

        List<Action> existingActions;
        if (instructions != null) {
            for (Instruction in : instructions) {
                if (in.getInstruction() instanceof ApplyActionsCase) {
                    existingActions = ((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction();
                    actionList.addAll(existingActions);
                }
            }
        }

        GroupBuilder groupBuilder = new GroupBuilder();
        Group group = null;

        /* Create output action for this port */
        ActionBuilder outPortActionBuilder = new ActionBuilder();
        ActionBuilder pushVlanActionBuilder = new ActionBuilder();
        ActionBuilder setVlanActionBuilder = new ActionBuilder();

        OutputActionBuilder oab = new OutputActionBuilder();
        oab.setOutputNodeConnector(ncid);
        outPortActionBuilder.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        /* Create mod vlan action */
        /* First we push vlan header */
        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(0x8100);
        pushVlanActionBuilder.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());

        /* Then we set vlan id value as vlanId */
        SetVlanIdActionBuilder vl = new SetVlanIdActionBuilder();
        vl.setVlanId(new VlanId(vlanId.intValue()));
        setVlanActionBuilder.setAction(new SetVlanIdActionCaseBuilder().setSetVlanIdAction(vl.build()).build());

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

        // add vlan port out, bucket_id=vlanId
        bucketId = new BucketId(vlanId);

        if (groupActionAdded) {
            /* modify the action bucket in group */
            groupBuilder = new GroupBuilder(group);
            Buckets buckets = groupBuilder.getBuckets();

            for (Bucket bucket : buckets.getBucket()) {
                if (bucket.getBucketId().getValue() == bucketId.getValue() && bucket.getBucketId().getValue() != 1l) {
                    LOG.warn(
                            "Warning: createOutputGroupInstructionsToTunnelPort: the bucket is exsit for a tunnel port");
                    addNew = false;
                    break;
                }
            }
            if (addNew) {
                /* the new output action is not in the bucket, add to bucket */
                // Bucket bucket = buckets.getBucket().get(0);
                // BucketBuilder bucket = new BucketBuilder();
                List<Action> bucketActionList = Lists.newArrayList();
                // bucketActionList.addAll(bucket.getAction());
                /* set order for new action and add to action list */
                pushVlanActionBuilder.setOrder(bucketActionList.size());
                pushVlanActionBuilder.setKey(new ActionKey(bucketActionList.size()));
                bucketActionList.add(pushVlanActionBuilder.build());

                setVlanActionBuilder.setOrder(bucketActionList.size());
                setVlanActionBuilder.setKey(new ActionKey(bucketActionList.size()));
                bucketActionList.add(setVlanActionBuilder.build());

                outPortActionBuilder.setOrder(bucketActionList.size());
                outPortActionBuilder.setKey(new ActionKey(bucketActionList.size()));
                bucketActionList.add(outPortActionBuilder.build());

                /*
                 * set bucket and buckets list. Reset groupBuilder with new
                 * buckets.
                 */
                BucketsBuilder bucketsBuilder = new BucketsBuilder();
                List<Bucket> bucketList = Lists.newArrayList();
                bucketList.addAll(buckets.getBucket());

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
            pushVlanActionBuilder.setOrder(bucketActionList.size());
            pushVlanActionBuilder.setKey(new ActionKey(bucketActionList.size()));
            bucketActionList.add(pushVlanActionBuilder.build());

            setVlanActionBuilder.setOrder(bucketActionList.size());
            setVlanActionBuilder.setKey(new ActionKey(bucketActionList.size()));
            bucketActionList.add(setVlanActionBuilder.build());

            outPortActionBuilder.setOrder(bucketActionList.size());
            outPortActionBuilder.setKey(new ActionKey(bucketActionList.size()));
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

            // groupId++;
        }
        // LOG.debug("createOutputGroupInstructions: group {}",
        // groupBuilder.build());
        // LOG.debug("createOutputGroupInstructions: actionList {}",
        // actionList);

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

    protected boolean removeActionFromGroup(NodeBuilder nodeBuilder, InstructionBuilder ib, Long dpid, Long port,
            IpAddress destTunnelIp, List<Instruction> instructions) {

        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpid + ":" + port);

        List<Action> actionList = Lists.newArrayList();
        ActionBuilder ab;

        List<Action> existingActions;
        if (instructions != null) {
            for (Instruction in : instructions) {
                if (in.getInstruction() instanceof ApplyActionsCase) {
                    existingActions = ((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction();
                    actionList.addAll(existingActions);
                    break;
                }
            }
        }

        GroupBuilder groupBuilder = new GroupBuilder();
        Group group = null;
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
                break;
            }
        }

        if (groupActionAdded) {
            /* modify the action bucket in group */
            groupBuilder = new GroupBuilder(group);
            Buckets buckets = groupBuilder.getBuckets();
            List<Action> bucketActions = Lists.newArrayList();
            for (Bucket bucket : buckets.getBucket()) {
                // if ((destTunnelIp != null) &&
                // (bucket.getBucketId().getValue() == 1l)) {
                if (bucket.getBucketId().getValue() == 1l) {
                    // remove port from the bucket id = 1
                    int index = 0;
                    boolean isPortDeleted = false;
                    bucketActions = bucket.getAction();
                    for (Action action : bucketActions) {
                        if (action.getAction() instanceof OutputActionCase) {
                            OutputActionCase opAction = (OutputActionCase) action.getAction();
                            if (opAction.getOutputAction().getOutputNodeConnector().equals(ncid)) {
                                /*
                                 * Find the output port in action list and
                                 * remove
                                 */
                                index = bucketActions.indexOf(action);
                                bucketActions.remove(action);
                                isPortDeleted = true;
                                break;
                            }
                        }
                    }
                    if (isPortDeleted && !bucketActions.isEmpty()) {
                        for (int i = index; i < bucketActions.size(); i++) {
                            Action action = bucketActions.get(i);
                            if (action.getOrder() != i) {
                                /* Shift the action order */
                                ab = new ActionBuilder();
                                ab.setAction(action.getAction());
                                ab.setOrder(i);
                                ab.setKey(new ActionKey(i));
                                Action actionNewOrder = ab.build();
                                bucketActions.remove(action);
                                bucketActions.add(i, actionNewOrder);
                            }
                        }

                    } else if (bucketActions.isEmpty()) {
                        /* remove bucket with empty action list */
                        buckets.getBucket().remove(bucket);
                        break;
                    }
                } // if bucketid=1
                else if (destTunnelIp != null) {
                    // Remove the bucket to the dest Vtep
                    int ip = InetAddresses
                            .coerceToInteger(InetAddresses.forString(destTunnelIp.getIpv4Address().getValue()));
                    long ipBucketId = ip & 0xffffffffL;
                    // BucketId bucketId = new BucketId(ipl);
                    if (bucket.getBucketId().getValue() == ipBucketId) {
                        buckets.getBucket().remove(bucket);
                    }
                }
            }
            if (!buckets.getBucket().isEmpty()) {
                /* rewrite the group to group table */
                /*
                 * set bucket and buckets list. Reset groupBuilder with new
                 * buckets.
                 */
                List<Bucket> bucketList = Lists.newArrayList();

                bucketList.addAll(buckets.getBucket());
                BucketsBuilder bucketsBuilder = new BucketsBuilder();

                bucketsBuilder.setBucket(bucketList);
                groupBuilder.setBuckets(bucketsBuilder.build());
                LOG.debug("removeOutputPortFromGroup: bucketList {}", bucketList);

                writeGroup(groupBuilder, nodeBuilder);
                ApplyActionsBuilder aab = new ApplyActionsBuilder();
                aab.setAction(actionList);
                ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
                return false;
            } else {
                /* remove group with empty bucket. return true to delete flow */
                removeGroup(groupBuilder, nodeBuilder);
                return true;
            }
        } else {
            /* no group for port list. flow can be removed */
            return true;
        }
    }

    protected boolean removeVlanActionFromGroup(NodeBuilder nodeBuilder, InstructionBuilder ib, Long dpid, Long port,
            Long vlanId, List<Instruction> instructions) {

        NodeConnectorId ncid = new NodeConnectorId(Constants.OPENFLOW_NODE_PREFIX + dpid + ":" + port);

        List<Action> actionList = Lists.newArrayList();
        ActionBuilder ab;

        List<Action> existingActions;
        if (instructions != null) {
            for (Instruction in : instructions) {
                if (in.getInstruction() instanceof ApplyActionsCase) {
                    existingActions = ((ApplyActionsCase) in.getInstruction()).getApplyActions().getAction();
                    actionList.addAll(existingActions);
                    break;
                }
            }
        }

        GroupBuilder groupBuilder = new GroupBuilder();
        Group group = null;
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
                break;
            }
        }

        if (groupActionAdded) {
            /* modify the action bucket in group */
            groupBuilder = new GroupBuilder(group);
            Buckets buckets = groupBuilder.getBuckets();
            List<Action> bucketActions = Lists.newArrayList();
            for (Bucket bucket : buckets.getBucket()) {
                // if ((destTunnelIp != null) &&
                // (bucket.getBucketId().getValue() == 1l)) {
                if (bucket.getBucketId().getValue() == 1l) {
                    // remove port from the bucket id = 1
                    int index = 0;
                    boolean isPortDeleted = false;
                    bucketActions = bucket.getAction();
                    for (Action action : bucketActions) {
                        if (action.getAction() instanceof OutputActionCase) {
                            OutputActionCase opAction = (OutputActionCase) action.getAction();
                            if (opAction.getOutputAction().getOutputNodeConnector().equals(ncid)) {
                                /*
                                 * Find the output port in action list and
                                 * remove
                                 */
                                index = bucketActions.indexOf(action);
                                bucketActions.remove(action);
                                isPortDeleted = true;
                                break;
                            }
                        }
                    }
                    if (isPortDeleted && !bucketActions.isEmpty()) {
                        for (int i = index; i < bucketActions.size(); i++) {
                            Action action = bucketActions.get(i);
                            if (action.getOrder() != i) {
                                /* Shift the action order */
                                ab = new ActionBuilder();
                                ab.setAction(action.getAction());
                                ab.setOrder(i);
                                ab.setKey(new ActionKey(i));
                                Action actionNewOrder = ab.build();
                                bucketActions.remove(action);
                                bucketActions.add(i, actionNewOrder);
                            }
                        }

                    } else if (bucketActions.isEmpty()) {
                        /* remove bucket with empty action list */
                        buckets.getBucket().remove(bucket);
                        break;
                    }
                } // if bucketid=1
                else if (vlanId != null) {
                    if (bucket.getBucketId().getValue() == vlanId.longValue()) {
                        buckets.getBucket().remove(bucket);
                    }
                }
            }
            if (!buckets.getBucket().isEmpty()) {
                /* rewrite the group to group table */
                /*
                 * set bucket and buckets list. Reset groupBuilder with new
                 * buckets.
                 */
                List<Bucket> bucketList = Lists.newArrayList();

                bucketList.addAll(buckets.getBucket());
                BucketsBuilder bucketsBuilder = new BucketsBuilder();

                bucketsBuilder.setBucket(bucketList);
                groupBuilder.setBuckets(bucketsBuilder.build());
                LOG.debug("removeOutputPortFromGroup: bucketList {}", bucketList);

                writeGroup(groupBuilder, nodeBuilder);
                ApplyActionsBuilder aab = new ApplyActionsBuilder();
                aab.setAction(actionList);
                ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
                return false;
            } else {
                /* remove group with empty bucket. return true to delete flow */
                removeGroup(groupBuilder, nodeBuilder);
                return true;
            }
        } else {
            /* no group for port list. flow can be removed */
            return true;
        }
    }

    public void programBcastToLocalPort(Long dpid, Long segmentationId, Long OFLocalPortOut, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        // OfMatchUtils.addNxRegMatch(matchBuilder, new
        // OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
        // PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "FloodOut_" + segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(17000);
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

        if (isWriteFlow) {
            // Set the Output Port/Iface
            createOutputGroupInstructionsToLocalPort(nodeBuilder, ib, dpid, OFLocalPortOut, segmentationId,
                    existingInstructions);
            // createOutputPortInstructions(ib, dpid, OFLocalPortOut,
            // existingInstructions);
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            /* remove port from the bucket used for local port, bucketid=1 */
            boolean flowRemove = removeActionFromGroup(nodeBuilder, ib, dpid, OFLocalPortOut, null,
                    existingInstructions);
            if (flowRemove) {
                /* if all port are removed, remove the flow too. */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
                instructions.add(ib.build());

                // Add InstructionBuilder to the Instruction(s)Builder List
                isb.setInstruction(instructions);

                // Add InstructionsBuilder to FlowBuilder
                flowBuilder.setInstructions(isb.build());
                writeFlow(flowBuilder, nodeBuilder);
            }
        }
    }

    public void programBcastToTunnelPort(Long dpid, Long segmentationId, Long OFTunnelPortOut,
            IpAddress dstTunIpAddress, boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        // OfMatchUtils.addNxRegMatch(matchBuilder, new
        // OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
        // PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "FloodOut_" + segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(17000);
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

        if (isWriteFlow) {
            // create a bucket to group bucket list,
            // bucketid=(long)dstTunIpAddress
            createOutputGroupInstructionsToTunnelPort(nodeBuilder, ib, dpid, OFTunnelPortOut, segmentationId,
                    dstTunIpAddress, existingInstructions);

            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            /* remove port from the bucket used for local port, bucketid=1 */
            boolean flowRemove = removeActionFromGroup(nodeBuilder, ib, dpid, OFTunnelPortOut, dstTunIpAddress,
                    existingInstructions);
            if (flowRemove) {
                /* if all port are removed, remove the flow too. */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
                instructions.add(ib.build());

                // Add InstructionBuilder to the Instruction(s)Builder List
                isb.setInstruction(instructions);

                // Add InstructionsBuilder to FlowBuilder
                flowBuilder.setInstructions(isb.build());
                writeFlow(flowBuilder, nodeBuilder);
            }
        }
    }

    public void programBcastToVlanPort(Long dpid, Long segmentationId, Long OfVlanPortOut, Long vlanId,
            boolean isWriteFlow) {

        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        // Match TunnelID
        // OfMatchUtils.addNxRegMatch(matchBuilder, new
        // OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_FIELD,
        // PipelineTrafficClassifier.REG_VALUE_FROM_LOCAL));
        flowBuilder.setMatch(
                OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue())).build());
        // Match DMAC
        flowBuilder.setMatch(
                OfMatchUtils.createDestEthMatch(matchBuilder, "01:00:00:00:00:00", "01:00:00:00:00:00").build());

        String flowId = "FloodOut_" + segmentationId;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(17000);
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

        if (isWriteFlow) {
            // create a bucket to group bucket list,
            // bucketid=(long)dstTunIpAddress
            createOutputGroupInstructionsToVlanPort(nodeBuilder, ib, dpid, OfVlanPortOut, segmentationId, vlanId,
                    existingInstructions);

            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Add InstructionBuilder to the Instruction(s)Builder List
            isb.setInstruction(instructions);

            // Add InstructionsBuilder to FlowBuilder
            flowBuilder.setInstructions(isb.build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            /* remove port from the bucket used for local port, bucketid=1 */
            boolean flowRemove = removeVlanActionFromGroup(nodeBuilder, ib, dpid, OfVlanPortOut, vlanId,
                    existingInstructions);
            if (flowRemove) {
                /* if all port are removed, remove the flow too. */
                removeFlow(flowBuilder, nodeBuilder);
            } else {
                /* Install instruction with new output port list */
                ib.setOrder(instructions.size());
                ib.setKey(new InstructionKey(instructions.size()));
                instructions.add(ib.build());

                // Add InstructionBuilder to the Instruction(s)Builder List
                isb.setInstruction(instructions);

                // Add InstructionsBuilder to FlowBuilder
                flowBuilder.setInstructions(isb.build());
                writeFlow(flowBuilder, nodeBuilder);
            }
        }
    }

}
