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
import org.opendaylight.ovsdb.utils.mdsal.openflow.ActionUtils;
import org.opendaylight.ovsdb.utils.mdsal.openflow.InstructionUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.AccessListEntries;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.Ace;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.Actions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.Matches;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.actions.packet.handling.Permit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.AceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.AceEth;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.AceIp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;

import com.google.common.collect.Lists;

public class PipelineAclHandler extends AbstractServiceInstance{
    public static final Integer MATCH_PRIORITY = 61000;
    public static final short PROTOCOL_ICMP = 1;
    public static final short PROTOCOL_TCP = 6;
    public static final short PROTOCOL_UDP = 17;
    public final static long REG_VALUE_SFC_REDIRECT = 0x5L;

    //User NXM REG1 to identify the SFC redirect traffic
    public static final Class<? extends NxmNxReg> REG_SFC_FIELD = NxmNxReg1.class;

    public PipelineAclHandler(DataBroker dataBroker) {
        super(Service.ACL_HANDlER, dataBroker);
    }

    public void programTrafficBehaviorRule(Long dpid, TrafficBehavior trafficBehavior, boolean writeFlow) {
        MatchBuilder matchBuilder = new MatchBuilder();
        FlowBuilder flowBuilder = new FlowBuilder();

        if (dpid == 0L) {
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
        if (trafficBehavior == TrafficBehavior.NeedAcl) {
            //If Traffic Behavior is NeedAcl, the Default action is drop, need Acl to allow traffic
            ib = InstructionUtils.createDropInstructions(ib);
        }
        else {
            //Default Traffic Behavior is allow traffic goto next table, it has done in Pipeline initialization step
            return;
        }
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        // Add InstructionBuilder to the Instruction(s)Builder List
        isb.setInstruction(instructions);

        // Add InstructionsBuilder to FlowBuilder
        flowBuilder.setInstructions(isb.build());

        String flowId = "DEFAULT_PIPELINE_FLOW_"+getTable();
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setPriority(0);
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (writeFlow) {
            writeFlow(flowBuilder, nodeBuilder);
        }
        else {
            removeFlow(flowBuilder, nodeBuilder);
        }
    }

    public void programAclEntry(Long dpidLong, Acl acl, boolean writeFlow) {
        String nodeName = OPENFLOW + dpidLong;

        //MatchBuilder matchBuilder = new MatchBuilder();
        //NodeBuilder nodeBuilder = createNodeBuilder(nodeName);
        //FlowBuilder flowBuilder = new FlowBuilder();

        String flowId = "PipelineAcl_";

        AccessListEntries accessListEntries = acl.getAccessListEntries();

        for (Ace ace: accessListEntries.getAce()) {
            flowId = flowId + ace.getRuleName();
            Matches aclMatches = ace.getMatches();

            Actions aclActions = ace.getActions();
            AceType aceType = aclMatches.getAceType();
            if (aceType instanceof AceEth ) {
                aceEthAcl(nodeName, flowId, (AceEth)aceType, writeFlow, aclActions);
            } else if (aceType instanceof AceIp) {
                aceIpAcl(nodeName, flowId, (AceIp)aceType, writeFlow, aclActions);
            }

        }
    }

    //Match Ethernet source/dest mac, macmask
    private void aceEthAcl(String nodeName, String flowId, AceEth aceEth, boolean writeFlow, Actions aclActions) {
        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);

        String srcMacAddress = aceEth.getSourceMacAddress().getValue();
        String srcMacAddressMask = aceEth.getSourceMacAddressMask().getValue();
        String dstMacAddress = aceEth.getDestinationMacAddress().getValue();
        String dstMacAddressMask = aceEth.getDestinationMacAddressMask().getValue();

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();

        if (srcMacAddress != null) {
            EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
            ethSourceBuilder.setAddress(new MacAddress(srcMacAddress));
            if (srcMacAddressMask != null) {
                ethSourceBuilder.setMask(new MacAddress(srcMacAddressMask));
            }
            ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        }

        if (dstMacAddress != null) {
            EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
            ethDestinationBuilder.setAddress(new MacAddress(dstMacAddress));
            if (dstMacAddressMask != null) {
                ethDestinationBuilder.setMask(new MacAddress(dstMacAddressMask));
            }
            ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        }

        matchBuilder.setEthernetMatch(ethernetMatch.build());

        syncFlow(flowId, nodeBuilder, matchBuilder, MATCH_PRIORITY, writeFlow, aclActions);
    }

    //Match IP protocol(TCP/UDP/ICMP), IPv4 source/des Address
    private void aceIpAcl(String nodeName, String flowId, AceIp aceIp, boolean writeFlow, Actions aclActions) {
        MatchBuilder matchBuilder = new MatchBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(nodeName);

        if (aceIp.getAceIpVersion() instanceof AceIpv6) {
            /*TODO IPv6 Support*/
            return;
        }

        if (aceIp.getAceIpVersion() instanceof AceIpv4) {
            //Match IPv4 protocol and IP Address

            //Match IP protocol, Now, support TCP, UDP, ICMP
            matchBuilder = createIpProtocolMatch(matchBuilder, aceIp.getProtocol());

            AceIpv4 aceIpv4 = (AceIpv4)aceIp.getAceIpVersion();
            Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();

            Ipv4Prefix ipv4SourcePrefix = aceIpv4.getSourceIpv4Network();
            if (ipv4SourcePrefix != null) {
                ipv4match.setIpv4Source(ipv4SourcePrefix);
            }

            Ipv4Prefix ipv4DestinationPrefix = aceIpv4.getSourceIpv4Network();
            if (ipv4DestinationPrefix != null) {
                ipv4match.setIpv4Destination(ipv4DestinationPrefix);
            }

            matchBuilder.setLayer3Match(ipv4match.build());
        }

        if (PROTOCOL_TCP == aceIp.getProtocol()) {
            //Set TCP Port
            matchBuilder = createTcpMatch(matchBuilder, aceIp);
        }

        if (PROTOCOL_UDP == aceIp.getProtocol()) {
            //Set UDP Port
            matchBuilder = createUdpMatch(matchBuilder, aceIp);
        }

        syncFlow(flowId, nodeBuilder, matchBuilder, MATCH_PRIORITY, writeFlow, aclActions);
    }

    private MatchBuilder createIpProtocolMatch(MatchBuilder matchBuilder, short ipProtocol) {

        IpMatchBuilder ipMmatch = new IpMatchBuilder();
        if (ipProtocol == PROTOCOL_TCP) {
            ipMmatch.setIpProtocol(PROTOCOL_TCP);
        }
        else if (ipProtocol == PROTOCOL_UDP) {
            ipMmatch.setIpProtocol(PROTOCOL_UDP);
        }
        else if (ipProtocol == PROTOCOL_ICMP) {
            ipMmatch.setIpProtocol(PROTOCOL_ICMP);
        }
        matchBuilder.setIpMatch(ipMmatch.build());
        return matchBuilder;
    }

    private MatchBuilder createTcpMatch(MatchBuilder matchBuilder, AceIp aceIp) {
        TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        PortNumber tcpSourcePortLower = aceIp.getSourcePortRange().getLowerPort();
        PortNumber tcpDestinationPortLower = aceIp.getDestinationPortRange().getLowerPort();

        if ( (null != tcpSourcePortLower) && tcpSourcePortLower.equals(aceIp.getSourcePortRange().getUpperPort())) {
            tcpmatch.setTcpSourcePort(tcpSourcePortLower);
        } else {
            /*TODO TCP PortRange Match*/
        }

        if ( (null != tcpDestinationPortLower) && tcpDestinationPortLower.equals(aceIp.getDestinationPortRange().getUpperPort())) {
            tcpmatch.setTcpDestinationPort(tcpDestinationPortLower);
        } else {
            /*TODO TCP PortRange Match*/
        }

        matchBuilder.setLayer4Match(tcpmatch.build());

        return matchBuilder;
    }

    private MatchBuilder createUdpMatch(MatchBuilder matchBuilder, AceIp aceIp) {
        UdpMatchBuilder udpmatch = new UdpMatchBuilder();
        PortNumber udpSourcePortLower = aceIp.getSourcePortRange().getLowerPort();
        PortNumber udpDestinationPortLower = aceIp.getDestinationPortRange().getLowerPort();

        if ( (null != udpSourcePortLower) && udpSourcePortLower.equals(aceIp.getSourcePortRange().getUpperPort())) {
            udpmatch.setUdpSourcePort(udpSourcePortLower);
        } else {
            /*TODO UDP PortRange Match*/
        }

        if ( (null != udpDestinationPortLower) && udpDestinationPortLower.equals(aceIp.getDestinationPortRange().getUpperPort())) {
            udpmatch.setUdpDestinationPort(udpDestinationPortLower);
        } else {
            /*TODO UDP PortRange Match*/
        }

        return matchBuilder;
    }

    private void syncFlow(String flowId, NodeBuilder nodeBuilder, MatchBuilder matchBuilder,
            Integer priority, boolean writeFlow, Actions aclActions) {

        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(false);
        flowBuilder.setPriority(priority);
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(this.getTable());
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);

        if (writeFlow) {
            InstructionBuilder ib = new InstructionBuilder();
            InstructionsBuilder isb = new InstructionsBuilder();
            List<Instruction> instructionsList = Lists.newArrayList();

            if (aclActions.getPacketHandling() instanceof Permit) {
                ib = this.getMutablePipelineInstructionBuilder();
            } else if (aclActions.getPacketHandling() instanceof Redirect) {
                //yzy: here for faas Redirect augment, WARNING: not implement now!!

                Nsh nsh = getNsh(aclActions);

                if (nsh != null) {
                    InstructionBuilder sfcIb = new InstructionBuilder();
                    List<Action> redirectActionList = new ArrayList<Action>();

                    ActionBuilder redirectActionBuilder = new ActionBuilder();

                    IpAddress destSffVtepIp = nsh.getDestIp();

                    redirectActionBuilder.setAction(ActionUtils.nxLoadTunIPv4Action(destSffVtepIp.getIpv4Address().getValue(), false));
                    redirectActionBuilder.setOrder(0);
                    redirectActionBuilder.setKey(new ActionKey(0));
                    redirectActionList.add(redirectActionBuilder.build());

                    Short nsi = nsh.getNsi();
                    redirectActionBuilder.setAction(ActionUtils.nxSetNsiAction(nsi));
                    redirectActionBuilder.setOrder(1);
                    redirectActionBuilder.setKey(new ActionKey(1));
                    redirectActionList.add(redirectActionBuilder.build());

                    Long nsp = nsh.getNsp();
                    redirectActionBuilder.setAction(ActionUtils.nxSetNspAction(nsp));
                    redirectActionBuilder.setOrder(2);
                    redirectActionBuilder.setKey(new ActionKey(2));
                    redirectActionList.add(redirectActionBuilder.build());

                    redirectActionBuilder.setAction(ActionUtils.nxLoadRegAction(new DstNxRegCaseBuilder().setNxReg(REG_SFC_FIELD).build(),
                            BigInteger.valueOf(REG_VALUE_SFC_REDIRECT)));
                    redirectActionBuilder.setOrder(3);
                    redirectActionBuilder.setKey(new ActionKey(3));
                    redirectActionList.add(redirectActionBuilder.build());

                    ApplyActionsBuilder aab = new ApplyActionsBuilder();
                    aab.setAction(redirectActionList);

                    sfcIb.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
                    sfcIb.setOrder(0);
                    sfcIb.setKey(new InstructionKey(0));
                    instructionsList.add(sfcIb.build());
                }

                ib = this.getMutablePipelineInstructionBuilder();
            } else {
                //Default Action for ACL is DENY
                InstructionUtils.createDropInstructions(ib);
            }

            ib.setOrder(1);
            ib.setKey(new InstructionKey(1));
            instructionsList.add(ib.build());
            isb.setInstruction(instructionsList);
            flowBuilder.setInstructions(isb.build());
            writeFlow(flowBuilder, nodeBuilder);
        } else {
            removeFlow(flowBuilder, nodeBuilder);
        }

    }

    private Nsh getNsh(Actions aclActions) {
        if (((Redirect)aclActions.getPacketHandling()).getRedirectType() instanceof Tunnel) {
            Tunnel redirectTunnel = (Tunnel)(((Redirect)aclActions.getPacketHandling()).getRedirectType());
            if (redirectTunnel.getTunnelType() instanceof Nsh) {
                return (Nsh)(redirectTunnel.getTunnelType());
            }
        }

        return null;
    }

}
