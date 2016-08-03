/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfActionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfInstructionUtils;
import org.opendaylight.faas.fabrics.vxlan.adapters.ovs.utils.OfMatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.AccessListEntries;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.Ace;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.Actions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.Matches;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.actions.packet.handling.Permit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.AceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.AceEth;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.AceIp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.Tunnel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.tunnel.tunnel.type.Nsh;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;

import com.google.common.collect.Lists;

public class PipelineAclHandler extends AbstractServiceInstance {

    private static final Integer ACL_MATCH_PRIORITY = 60000;
    private static final Integer GPE_TUNNEL_IN_PRIORITY = 61001;
    private static final Integer TRAFFIC_BEHAVIOR_RULE_PRIORITY = 1;

    public static final short PROTOCOL_ICMP = 1;
    public static final short PROTOCOL_TCP = 6;
    public static final short PROTOCOL_UDP = 17;
    public static final long REG_VALUE_SFC_REDIRECT = 0x5L;

    // User NXM REG1 to identify the SFC redirect traffic
    public static final Class<? extends NxmNxReg> REG_SFC_FIELD = NxmNxReg1.class;

    public PipelineAclHandler(DataBroker dataBroker) {
        super(Service.ACL_HANDlER, dataBroker);
    }

    public void programTrafficBehaviorRule(Long dpid, TrafficBehavior trafficBehavior, boolean isWriteFlow) {
        MatchBuilder matchBuilder = new MatchBuilder();
        FlowBuilder flowBuilder = new FlowBuilder();

        if (dpid == null) {
            return;
        }
        String nodeName = OPENFLOW + dpid;
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);

        // Create the OF Actions and Instructions
        InstructionsBuilder isb = new InstructionsBuilder();

        // Instructions List Stores Individual Instructions
        List<Instruction> instructions = Lists.newArrayList();

        // Call the InstructionBuilder Methods Containing Actions
        InstructionBuilder ib = new InstructionBuilder();
        if (trafficBehavior == TrafficBehavior.PolicyDriven) {
            // If Traffic Behavior is NeedAcl, the Default action is drop, need
            // Acl to allow traffic
            ib = OfInstructionUtils.createDropInstructions(ib);
        } else {
            // Default Traffic Behavior is allow traffic goto next table, it has
            // done in Pipeline initialization step
            return;
        }

        ib.setOrder(instructions.size());
        ib.setKey(new InstructionKey(instructions.size()));
        instructions.add(ib.build());

        // Add InstructionBuilder to the Instruction(s)Builder List
        isb.setInstruction(instructions);

        // Add InstructionsBuilder to FlowBuilder
        flowBuilder.setInstructions(isb.build());

        String flowId = "Acl_TrafficBehavior_" + getTable();
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setPriority(TRAFFIC_BEHAVIOR_RULE_PRIORITY);
        flowBuilder.setBarrier(true);
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

    // For Traffic from GPE Tunnel, allow it go to next table by default
    public void programGpeTunnelInEntry(Long dpid, Long segmentationId, Long gpeTunnelOfPort, boolean isWriteFlow) {
        String nodeName = OPENFLOW + dpid;

        BigInteger tunnelId = BigInteger.valueOf(segmentationId.longValue());
        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create Match(es) and Set them in the FlowBuilder Object
        flowBuilder.setMatch(OfMatchUtils.createTunnelIDMatch(matchBuilder, tunnelId).build());
        flowBuilder.setMatch(OfMatchUtils.createInPortMatch(matchBuilder, dpid, gpeTunnelOfPort).build());

        if (isWriteFlow) {
            // Create the OF Actions and Instructions
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();

            // Instructions List Stores Individual Instructions
            List<Instruction> instructions = Lists.newArrayList();

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

        String flowId = "GpeTunnelIn_" + segmentationId + "_" + gpeTunnelOfPort;
        // Add Flow Attributes
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setPriority(GPE_TUNNEL_IN_PRIORITY);
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

    public void programBridgeDomainAclEntry(Long dpid, Long segmentationId, Acl acl, boolean isWriteFlow) {
        String nodeName = OPENFLOW + dpid;

        String flowId = "PipelineAcl_BridgeDomain_" + segmentationId.toString();

        AccessListEntries accessListEntries = acl.getAccessListEntries();

        for (Ace ace : accessListEntries.getAce()) {
            flowId = flowId + ace.getRuleName();
            Matches aclMatches = ace.getMatches();

            MatchBuilder matchBuilder = new MatchBuilder();

            OfMatchUtils.addNxRegMatch(matchBuilder,
                    new OfMatchUtils.RegMatch(PipelineTrafficClassifier.REG_SRC_TUN_ID, segmentationId));

            Actions aclActions = ace.getActions();
            AceType aceType = aclMatches.getAceType();
            if (aceType instanceof AceEth) {
                aceEthAcl(nodeName, flowId, matchBuilder, (AceEth) aceType, isWriteFlow, aclActions);
            } else if (aceType instanceof AceIp) {
                aceIpAcl(nodeName, flowId, matchBuilder, (AceIp) aceType, isWriteFlow, aclActions);
            }

        }
    }

    public void programBridgePortAclEntry(Long dpid, Long bridgePort, Acl acl, boolean isWriteFlow) {
        String nodeName = OPENFLOW + dpid;

        String flowId = "PipelineAcl_BridgePort_" + bridgePort.toString();

        AccessListEntries accessListEntries = acl.getAccessListEntries();

        for (Ace ace : accessListEntries.getAce()) {
            flowId = flowId + ace.getRuleName();
            Matches aclMatches = ace.getMatches();

            MatchBuilder matchBuilder = new MatchBuilder();
            matchBuilder = OfMatchUtils.createInPortMatch(matchBuilder, dpid, bridgePort);

            Actions aclActions = ace.getActions();
            AceType aceType = aclMatches.getAceType();
            if (aceType instanceof AceEth) {
                aceEthAcl(nodeName, flowId, matchBuilder, (AceEth) aceType, isWriteFlow, aclActions);
            } else if (aceType instanceof AceIp) {
                aceIpAcl(nodeName, flowId, matchBuilder, (AceIp) aceType, isWriteFlow, aclActions);
            }

        }
    }

    // Match Ethernet source/dest mac, macmask
    private void aceEthAcl(String nodeName, String flowId, MatchBuilder matchBuilder, AceEth aceEth,
            boolean isWriteFlow, Actions aclActions) {
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);

        createEthAddressMatch(matchBuilder, aceEth);

        syncFlow(flowId, nodeBuilder, matchBuilder, ACL_MATCH_PRIORITY, isWriteFlow, aclActions);
    }

    private void aceIpAcl(String nodeName, String flowId, MatchBuilder matchBuilder, AceIp aceIp, boolean isWriteFlow,
            Actions aclActions) {
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);

        short aceIpProtocol = aceIp.getProtocol();
        matchBuilder = createIpProtocolMatch(matchBuilder, aceIpProtocol);

        if (aceIp.getAceIpVersion() instanceof AceIpv6) {
            /* TODO IPv6 Support */
            return;
        } else {
            // Match IPv4 protocol and IP Address
            EthernetMatchBuilder eth = new EthernetMatchBuilder();
            EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
            ethTypeBuilder.setType(new EtherType(0x0800L));
            eth.setEthernetType(ethTypeBuilder.build());
            matchBuilder.setEthernetMatch(eth.build());

            createIpAddressMatch(matchBuilder, aceIp);

            // Match IP protocol, Now, support TCP, UDP, ICMP
            if (aceIpProtocol != 0) {
                if (PROTOCOL_TCP == aceIpProtocol) {
                    // Set TCP Port
                    matchBuilder = createTcpMatch(matchBuilder, aceIp);
                }

                if (PROTOCOL_UDP == aceIpProtocol) {
                    // Set UDP Port
                    matchBuilder = createUdpMatch(matchBuilder, aceIp);
                }
            }
        }

        syncFlow(flowId, nodeBuilder, matchBuilder, ACL_MATCH_PRIORITY, isWriteFlow, aclActions);
    }

    private MatchBuilder createEthAddressMatch(MatchBuilder matchBuilder, AceEth aceEth) {

        return OfMatchUtils.createEthAddressMatch(matchBuilder,
                aceEth.getSourceMacAddress() != null ? aceEth.getSourceMacAddress().getValue() : null,
                aceEth.getSourceMacAddressMask() != null ? aceEth.getSourceMacAddressMask().getValue() : null,
                aceEth.getDestinationMacAddress() != null ? aceEth.getDestinationMacAddress().getValue() : null,
                aceEth.getDestinationMacAddressMask() != null ? aceEth.getDestinationMacAddressMask().getValue()
                        : null);
    }

    private MatchBuilder createIpAddressMatch(MatchBuilder matchBuilder, AceIp aceIp) {
        if (aceIp.getAceIpVersion() == null) {
            return matchBuilder;
        }
        AceIpv4 aceIpv4 = (AceIpv4) aceIp.getAceIpVersion();

        return OfMatchUtils.createIpAddressMatch(matchBuilder,
                aceIpv4.getSourceIpv4Network() != null ? aceIpv4.getSourceIpv4Network().getValue() : null,
                aceIpv4.getDestinationIpv4Network() != null ? aceIpv4.getDestinationIpv4Network().getValue() : null);
    }

    private MatchBuilder createIpProtocolMatch(MatchBuilder matchBuilder, short ipProtocol) {

        IpMatchBuilder ipMmatch = new IpMatchBuilder();
        if (ipProtocol == PROTOCOL_TCP) {
            ipMmatch.setIpProtocol(PROTOCOL_TCP);
        } else if (ipProtocol == PROTOCOL_UDP) {
            ipMmatch.setIpProtocol(PROTOCOL_UDP);
        } else if (ipProtocol == PROTOCOL_ICMP) {
            ipMmatch.setIpProtocol(PROTOCOL_ICMP);
        }
        matchBuilder.setIpMatch(ipMmatch.build());
        return matchBuilder;
    }

    private MatchBuilder createTcpMatch(MatchBuilder matchBuilder, AceIp aceIp) {
        PortNumber tcpSourcePortLower = aceIp.getSourcePortRange().getLowerPort();
        PortNumber tcpDestinationPortLower = aceIp.getDestinationPortRange().getLowerPort();

        return OfMatchUtils.createTcpMatch(matchBuilder,
                tcpSourcePortLower.getValue() != 0 ? tcpSourcePortLower.getValue() : 0,
                tcpDestinationPortLower.getValue() != 0 ? tcpDestinationPortLower.getValue() : 0);

    }

    private MatchBuilder createUdpMatch(MatchBuilder matchBuilder, AceIp aceIp) {
        PortNumber udpSourcePortLower = aceIp.getSourcePortRange().getLowerPort();
        PortNumber udpDestinationPortLower = aceIp.getDestinationPortRange().getLowerPort();

        return OfMatchUtils.createTcpMatch(matchBuilder,
                udpSourcePortLower.getValue() != 0 ? udpSourcePortLower.getValue() : 0,
                udpDestinationPortLower.getValue() != 0 ? udpDestinationPortLower.getValue() : 0);

    }

    private void syncFlow(String flowId, NodeBuilder nodeBuilder, MatchBuilder matchBuilder, Integer priority,
            boolean isWriteFlow, Actions aclActions) {

        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(true);
        flowBuilder.setPriority(priority);
        flowBuilder.setBarrier(false);
        flowBuilder.setTableId(this.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (isWriteFlow) {
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();
            List<Instruction> instructionsList = Lists.newArrayList();

            if (aclActions.getPacketHandling() instanceof Permit) {
                ib = this.getMutablePipelineInstructionBuilder();
                ib.setOrder(instructionsList.size());
                ib.setKey(new InstructionKey(instructionsList.size()));
                instructionsList.add(ib.build());
            } else if (aclActions.getPacketHandling() instanceof Redirect) {
                Nsh nsh = getNsh(aclActions);

                if (nsh != null) {
                    InstructionBuilder sfcIb = new InstructionBuilder();
                    List<Action> redirectActionList = new ArrayList<Action>();

                    ActionBuilder redirectActionBuilder = new ActionBuilder();

                    IpAddress destSffVtepIp = nsh.getDestIp();

                    redirectActionBuilder.setAction(
                            OfActionUtils.nxLoadTunIPv4Action(destSffVtepIp.getIpv4Address().getValue(), false));
                    redirectActionBuilder.setOrder(redirectActionList.size());
                    redirectActionBuilder.setKey(new ActionKey(redirectActionList.size()));
                    redirectActionList.add(redirectActionBuilder.build());

                    Short nsi = nsh.getNsi();
                    redirectActionBuilder.setAction(OfActionUtils.nxSetNsiAction(nsi));
                    redirectActionBuilder.setOrder(redirectActionList.size());
                    redirectActionBuilder.setKey(new ActionKey(redirectActionList.size()));
                    redirectActionList.add(redirectActionBuilder.build());

                    Long nsp = nsh.getNsp();
                    redirectActionBuilder.setAction(OfActionUtils.nxSetNspAction(nsp));
                    redirectActionBuilder.setOrder(redirectActionList.size());
                    redirectActionBuilder.setKey(new ActionKey(redirectActionList.size()));
                    redirectActionList.add(redirectActionBuilder.build());

                    redirectActionBuilder.setAction(
                            OfActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_SFC_FIELD).build(),
                                    BigInteger.valueOf(REG_VALUE_SFC_REDIRECT)));
                    redirectActionBuilder.setOrder(redirectActionList.size());
                    redirectActionBuilder.setKey(new ActionKey(redirectActionList.size()));
                    redirectActionList.add(redirectActionBuilder.build());

                    ApplyActionsBuilder aab = new ApplyActionsBuilder();
                    aab.setAction(redirectActionList);

                    sfcIb.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
                    sfcIb.setOrder(instructionsList.size());
                    sfcIb.setKey(new InstructionKey(instructionsList.size()));
                    instructionsList.add(sfcIb.build());
                }

                ib = this.getMutablePipelineInstructionBuilder();
                ib.setOrder(instructionsList.size());
                ib.setKey(new InstructionKey(instructionsList.size()));
                instructionsList.add(ib.build());
            } else {
                // Default Action for ACL is DENY
                ib = OfInstructionUtils.createDropInstructions(ib);
                ib.setOrder(instructionsList.size());
                ib.setKey(new InstructionKey(instructionsList.size()));
                instructionsList.add(ib.build());
            }

            isb.setInstruction(instructionsList);
            flowBuilder.setInstructions(isb.build());
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }

    }

    private Nsh getNsh(Actions aclActions) {
        if (((Redirect) aclActions.getPacketHandling()).getRedirectType() instanceof Tunnel) {
            Tunnel redirectTunnel = (Tunnel) (((Redirect) aclActions.getPacketHandling()).getRedirectType());
            if (redirectTunnel.getTunnelType() instanceof Nsh) {
                return (Nsh) (redirectTunnel.getTunnelType());
            }
        }

        return null;
    }

}
