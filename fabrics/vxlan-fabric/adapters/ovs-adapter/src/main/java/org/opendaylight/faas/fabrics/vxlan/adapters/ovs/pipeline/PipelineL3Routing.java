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

import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers.Openflow13Provider;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Constants;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfActionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfMatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;

import com.google.common.collect.Lists;

public class PipelineL3Routing extends AbstractServiceInstance {
    // private static final Logger LOG =
    // LoggerFactory.getLogger(PipelineL3Routing.class);
    private static final Integer L3_ROUTING_PRIORITY = 2048;
    private static final Integer STATIC_ROUTING_PRIORITY = 2049;

    public PipelineL3Routing(DataBroker dataBroker) {
        super(Service.L3_ROUTING, dataBroker);
    }

    /*
     * (Table: L3_ROUTING) Distribute Virtual Routing function Match: IP, Source
     * subnet TunnelID , dest subnet Actions: Set ethernet source address to
     * Gateway mac, Set TunnelId to dest subnet tunnel id, GOTO Next Table Flow
     * example: table=60, ip,tun_id=0x3ea,nw_dst=1.0.0.0/24, \
     * actions=set_field:fa:16:3e:69:5a:42->eth_src,dec_ttl,set_field:0x3e9->
     * tun_id,goto_table=<next-table>"
     */
    public void programRouterInterface(Long dpid, Long sourceSegId, Long destSegId, String macAddress,
            IpAddress address, int mask, boolean isWriteFlow) {

        String nodeName = Constants.OPENFLOW_NODE_PREFIX + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = Openflow13Provider.createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionList = Lists
                .newArrayList();

        OfMatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(sourceSegId.longValue()));

        SubnetUtils addressSubnetInfo = new SubnetUtils(address.getIpv4Address().getValue() + "/" + mask);
        final String prefixString = addressSubnetInfo.getInfo().getNetworkAddress() + "/" + mask;
        OfMatchUtils.createDstL3IPv4Match(matchBuilder, prefixString);

        String flowId = "Routing_" + sourceSegId + "_" + destSegId + "_" + prefixString;
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(this.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(L3_ROUTING_PRIORITY);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setMatch(matchBuilder.build());

        if (isWriteFlow) {
            // Set source Mac address
            ab.setAction(OfActionUtils.setDlSrcAction(macAddress));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // DecTTL
            ab.setAction(OfActionUtils.decNwTtlAction());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Set Destination Tunnel ID
            ab.setAction(OfActionUtils.setTunnelIdAction(BigInteger.valueOf(destSegId.longValue())));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Create Apply Actions Instruction
            aab.setAction(actionList);
            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Goto Next Table
            ib = getMutablePipelineInstructionBuilder();
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            flowBuilder.setInstructions(isb.setInstruction(instructions).build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    /*
     * (Table: L3_ROUTING) For Static route entry in RIB
     * Match: static route IP address, Destination Mac is Local Gateway MAC
     * Actions: Set ethernet source address to Gateway mac, Set TunnelId to dest subnet tunnel id
     * Examples: table=60, priority=2049,ip,nw_dst=172.16.2.2,dl_dst=80:38:bc:a1:33:c7
     * actions=set_field:80:38:bc:a1:33:c7->eth_src,dec_ttl,set_field:0x100->tun_id,goto_table:70
     */
    public void programStaticRouting(Long dpid, String gwMacAddress,
            Ipv4Prefix destIpv4Prefix, Long nexthopSegId,  boolean isWriteFlow) {

        String nodeName = Constants.OPENFLOW_NODE_PREFIX + dpid;

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = Openflow13Provider.createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionList = Lists
                .newArrayList();

        OfMatchUtils.createDestEthMatch(matchBuilder, gwMacAddress, "");

        final String prefixString = destIpv4Prefix.getValue();
        OfMatchUtils.createDstL3IPv4Match(matchBuilder, prefixString);

        String flowId = "StaticRouting_" + nexthopSegId + "_" + prefixString;
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(this.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(STATIC_ROUTING_PRIORITY);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setMatch(matchBuilder.build());

        if (isWriteFlow) {
            // Set source Mac address
            ab.setAction(OfActionUtils.setDlSrcAction(gwMacAddress));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // DecTTL
            ab.setAction(OfActionUtils.decNwTtlAction());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Set Destination Tunnel ID
            ab.setAction(OfActionUtils.setTunnelIdAction(BigInteger.valueOf(nexthopSegId.longValue())));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Create Apply Actions Instruction
            aab.setAction(actionList);
            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            // Goto Next Table
            ib = getMutablePipelineInstructionBuilder();
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            flowBuilder.setInstructions(isb.setInstruction(instructions).build());

            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }
}
