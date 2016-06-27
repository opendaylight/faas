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

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers.Openflow13Provider;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Constants;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfInstructionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfMatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
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

public class PipelineL3Forwarding extends AbstractServiceInstance {
    //private static final Logger LOG = LoggerFactory.getLogger(PipelineL3Forwarding.class);

    public PipelineL3Forwarding(DataBroker dataBroker) {
        super(Service.L3_FORWARDING, dataBroker);
    }

    /*
     * (Table:  L3_FORWARDING) According to the dest ip address, set dest mac address
     * Match:   IP, TunnelID , Dest ip address
     * Actions: Set dest Mac address, GOTO Next Table
     * Flow example:
     *      table=70, ip,tun_id=0x3ea,nw_dst=2.0.0.2, \
     *      actions=set_field:fa:16:3e:41:56:ec->eth_dst,goto_table=<next-table>"
     */
    public void programForwardingTableEntry(Long dpid, Long segmentationId, IpAddress ipAddress,
            String macAddress, boolean isWriteFlow) {
        String nodeName = Constants.OPENFLOW_NODE_PREFIX + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = Openflow13Provider.createNodeBuilder(nodeName);

        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();

        OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue()));
        OfMatchUtils.createDstL3IPv4Match(matchBuilder, OfMatchUtils.iPv4PrefixFromIPv4Address(ipAddress.getIpv4Address().getValue()));

        // Set Dest Mac address
        OfInstructionUtils.createDlDstInstructions(ib, macAddress);
        ib.setOrder(instructions.size());
        ib.setKey(new InstructionKey(instructions.size()));
        instructions.add(ib.build());

        // Goto Next Table
        ib = getMutablePipelineInstructionBuilder();
        ib.setOrder(instructions.size());
        ib.setKey(new InstructionKey(instructions.size()));
        instructions.add(ib.build());

        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(isb.setInstruction(instructions).build());

        String flowId = "L3Forwarding_" + segmentationId + "_" + ipAddress.getIpv4Address().getValue();
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(this.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(1024);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (isWriteFlow) {
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }
}
