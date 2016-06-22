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
import org.opendaylight.netvirt.utils.mdsal.openflow.ActionUtils;
import org.opendaylight.netvirt.utils.mdsal.openflow.MatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;

import com.google.common.collect.Lists;

public class PipelineArpHandler extends AbstractServiceInstance {
    // private static final Logger LOG =
    // LoggerFactory.getLogger(PipelineArpHandler.class);

    public PipelineArpHandler(DataBroker dataBroker) {
        super(Service.ARP_HANDlER, dataBroker);
    }

    /*
     * (Table: ARP_HANDlER) Handle the ARP packet, construct ARP Response Match:
     * arp, TunnelID , source mac, source ip Actions: Make ARP response packet
     * Flow example: table=20, priority=1024,arp,tun_id=0x3ea,arp_tpa=2.0.0.2
     * actions=move:NXM_OF_ETH_SRC[]->NXM_OF_ETH_DST[],set_field:fa:16:3e:41:56:
     * ec->eth_src,
     * load:0x2->NXM_OF_ARP_OP[],move:NXM_NX_ARP_SHA[]->NXM_NX_ARP_THA[],move:
     * NXM_OF_ARP_SPA[]->NXM_OF_ARP_TPA[],
     * load:0xfa163e4156ec->NXM_NX_ARP_SHA[],load:0x2000002->NXM_OF_ARP_SPA[],
     * IN_PORT
     */
    public void programStaticArpEntry(Long dpid, Long segmentationId, String macAddressStr, IpAddress ipAddress,
            boolean isWriteFlow) {

        String nodeName = Constants.OPENFLOW_NODE_PREFIX + dpid;
        MacAddress macAddress = new MacAddress(macAddressStr);

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = Openflow13Provider.createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        String flowId = "ArpResponder_" + segmentationId + "_" + ipAddress.getIpv4Address().getValue();

        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(this.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setPriority(1024);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionList = Lists
                .newArrayList();

        if (segmentationId != null) {
            final Long inPort = MatchUtils.parseExplicitOFPort(String.valueOf(segmentationId));
            if (inPort != null) {
                MatchUtils.createInPortMatch(matchBuilder, dpid, inPort);
            } else {
                MatchUtils.createTunnelIDMatch(matchBuilder, BigInteger.valueOf(segmentationId.longValue()));
            }
        }

        MatchUtils.createEtherTypeMatch(matchBuilder, new EtherType(Constants.ARP_ETHERTYPE));
        MatchUtils.createArpDstIpv4Match(matchBuilder,
                MatchUtils.iPv4PrefixFromIPv4Address(ipAddress.getIpv4Address().getValue()));

        flowBuilder.setMatch(matchBuilder.build());

        if (isWriteFlow) {
            // Move Eth Src to Eth Dst
            ab.setAction(ActionUtils.nxMoveEthSrcToEthDstAction());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Set Eth Src
            ab.setAction(ActionUtils.setDlSrcAction(new MacAddress(macAddress)));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Set ARP OP
            ab.setAction(ActionUtils.nxLoadArpOpAction(BigInteger.valueOf(0x02L)));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Move ARP SHA to ARP THA
            ab.setAction(ActionUtils.nxMoveArpShaToArpThaAction());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Move ARP SPA to ARP TPA
            ab.setAction(ActionUtils.nxMoveArpSpaToArpTpaAction());
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Load Mac to ARP SHA
            ab.setAction(ActionUtils.nxLoadArpShaAction(macAddress));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Load IP to ARP SPA
            ab.setAction(ActionUtils.nxLoadArpSpaAction(ipAddress.getIpv4Address().getValue()));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Output of InPort
            ab.setAction(ActionUtils.outputAction(new NodeConnectorId(nodeName + ":INPORT")));
            ab.setOrder(actionList.size());
            ab.setKey(new ActionKey(actionList.size()));
            actionList.add(ab.build());

            // Create Apply Actions Instruction
            aab.setAction(actionList);
            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(instructions.size());
            ib.setKey(new InstructionKey(instructions.size()));
            instructions.add(ib.build());

            flowBuilder.setInstructions(isb.setInstruction(instructions).build());

            writeFlow(flowBuilder, nodeBuilder);
        }

        else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

}
