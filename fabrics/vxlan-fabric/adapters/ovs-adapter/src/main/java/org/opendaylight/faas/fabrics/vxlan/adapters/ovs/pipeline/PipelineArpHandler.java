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

import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Status;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.providers.Openflow13Provider;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.AdpaterAction;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.Constants;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.StatusCode;
import org.opendaylight.ovsdb.utils.mdsal.openflow.ActionUtils;
import org.opendaylight.ovsdb.utils.mdsal.openflow.MatchUtils;
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
    //private static final Logger LOG = LoggerFactory.getLogger(PipelineArpHandler.class);

    public PipelineArpHandler(DataBroker dataBroker) {
        super(Service.ARP_HANDlER, dataBroker);
    }

    /*
     * (Table:  ARP_HANDlER) Handle the ARP packet, construct ARP Response
     */
    public Status programStaticArpEntry(Long dpid, Long segmentationId, String macAddressStr, IpAddress ipAddress,
            AdpaterAction action) {

        String nodeName = Constants.OPENFLOW_NODE_PREFIX + dpid;
        MacAddress macAddress = new MacAddress(macAddressStr);

        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = Openflow13Provider.createNodeBuilder(nodeName);

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

        // Move Eth Src to Eth Dst
        ab.setAction(ActionUtils.nxMoveEthSrcToEthDstAction());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Set Eth Src
        ab.setAction(ActionUtils.setDlSrcAction(new MacAddress(macAddress)));
        ab.setOrder(1);
        ab.setKey(new ActionKey(1));
        actionList.add(ab.build());

        // Set ARP OP
        ab.setAction(ActionUtils.nxLoadArpOpAction(BigInteger.valueOf(0x02L)));
        ab.setOrder(2);
        ab.setKey(new ActionKey(2));
        actionList.add(ab.build());

        // Move ARP SHA to ARP THA
        ab.setAction(ActionUtils.nxMoveArpShaToArpThaAction());
        ab.setOrder(3);
        ab.setKey(new ActionKey(3));
        actionList.add(ab.build());

        // Move ARP SPA to ARP TPA
        ab.setAction(ActionUtils.nxMoveArpSpaToArpTpaAction());
        ab.setOrder(4);
        ab.setKey(new ActionKey(4));
        actionList.add(ab.build());

        // Load Mac to ARP SHA
        ab.setAction(ActionUtils.nxLoadArpShaAction(macAddress));
        ab.setOrder(5);
        ab.setKey(new ActionKey(5));
        actionList.add(ab.build());

        // Load IP to ARP SPA
        ab.setAction(ActionUtils.nxLoadArpSpaAction(ipAddress.getIpv4Address().getValue()));
        ab.setOrder(6);
        ab.setKey(new ActionKey(6));
        actionList.add(ab.build());

        // Output of InPort
        ab.setAction(ActionUtils.outputAction(new NodeConnectorId(nodeName + ":INPORT")));
        ab.setOrder(7);
        ab.setKey(new ActionKey(7));
        actionList.add(ab.build());

        // Create Apply Actions Instruction
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(isb.setInstruction(instructions).build());

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

        if (action.equals(AdpaterAction.ADD)) {
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }

        return new Status(StatusCode.SUCCESS);
    }

}
