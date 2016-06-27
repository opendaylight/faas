/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import java.math.BigInteger;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfInstructionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfMatchUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;

import com.google.common.collect.Lists;

public class PipelineInboundNat extends AbstractServiceInstance {

    public PipelineInboundNat(DataBroker dataBroker) {
        super(Service.INBOUND_NAT, dataBroker);
    }

    /*
     * (Table:  PipelineInboundNat) Inbound NAT, floating ip to fixed ip mapping
     * Match:   Floating ip
     * Actions: Set ip to fixed ip , Set tunnel id to floating ip network VNI, GOTO Next Table
     * Flow example:
     *      table=30,nw_dst=192.168.1.252 \
     *      actions=set_field:tun_id=0x111,set_field:172.16.1.2->nw_src,goto_table=<next-table>"
     */
    public void programFloatingIpToFixedIp(Long dpid, String floatingIp,
            Long floatingSegmentId, String fixedIp, boolean isWriteFlow) {
        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        OfMatchUtils.createDstL3IPv4Match(matchBuilder, OfMatchUtils.iPv4PrefixFromIPv4Address(floatingIp));
        flowBuilder.setMatch(matchBuilder.build());

        String flowId = "InboundNat_"+floatingIp+"_"+fixedIp+"_"+floatingSegmentId;
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

            // Set Dst Ip Address to fixed ip
            OfInstructionUtils.createNwSrcInstructions(ib,
                    OfMatchUtils.iPv4PrefixFromIPv4Address(fixedIp));
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            OfInstructionUtils.createSetTunnelIdInstructions(ib, BigInteger.valueOf(floatingSegmentId.longValue()));

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
