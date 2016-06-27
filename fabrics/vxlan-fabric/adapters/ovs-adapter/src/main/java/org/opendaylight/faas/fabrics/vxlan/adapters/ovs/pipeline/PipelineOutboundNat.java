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

public class PipelineOutboundNat extends AbstractServiceInstance{

    public PipelineOutboundNat(DataBroker dataBroker){
        super(Service.OUTBOUND_NAT, dataBroker);
    }

    /*
     * (Table:  PipelineOutboundNat) Outbound NAT, fixed ip to floating ip mapping
     * Match:   Floating ip network VNI , Fixed ip
     * Actions: Set eth_src to fixed ip gw mac, Set fixed ip to floating ip, GOTO Next Table
     * Flow example:
     *      table=100,tun_id=0x111,nw_src=172.16.1.2 \
     *      actions=set_field:fa:16:3e:41:56:ec->eth_src,set_field:192.168.1.252->nw_src,goto_table=<next-table>"
     */
    public void programFixedIpToFloatingIp(Long dpid, Long floatingSegmentId, String fixedIp,
            String fixedGwMac, String floatingIp, boolean isWriteFlow) {
        String nodeName = OPENFLOW + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create the OF Match using MatchBuilder
        OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(floatingSegmentId.longValue()));
        OfMatchUtils.createSrcL3IPv4Match(matchBuilder, OfMatchUtils.iPv4PrefixFromIPv4Address(fixedIp));
        flowBuilder.setMatch(matchBuilder.build());

        String flowId = "OutboundNat_"+floatingSegmentId+"_"+fixedIp+"_"+floatingIp;
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

            // Set Src Mac address
            OfInstructionUtils.createDlSrcInstructions(ib, fixedGwMac);
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Set Src Ip Address to floating ip
            OfInstructionUtils.createNwSrcInstructions(ib,
                    OfMatchUtils.iPv4PrefixFromIPv4Address(floatingIp));
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
