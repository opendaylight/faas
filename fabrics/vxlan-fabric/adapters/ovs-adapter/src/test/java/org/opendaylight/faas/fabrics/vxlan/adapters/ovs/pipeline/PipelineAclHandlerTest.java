package org.opendaylight.faas.fabrics.vxlan.adapters.ovs.pipeline;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.AclBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.AccessListEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.Ace;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.AceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.MatchesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.actions.packet.handling.DenyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.actions.packet.handling.PermitBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.AceEthBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.AceIpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev150317.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.AceIpv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.packet.fields.rev150611.acl.transport.header.fields.DestinationPortRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.packet.fields.rev150611.acl.transport.header.fields.SourcePortRangeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.rev150930.FabricOptions.TrafficBehavior;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.RedirectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.fabric.type.rev150930.access.lists.acl.access.list.entries.ace.actions.packet.handling.redirect.redirect.type.tunnel.tunnel.type.NshBuilder;

import com.google.common.util.concurrent.CheckedFuture;

@RunWith(MockitoJUnitRunner.class)
public class PipelineAclHandlerTest {
    @Mock private DataBroker dataBroker;

    @InjectMocks private PipelineAclHandler pipelineAclHandler= new PipelineAclHandler(dataBroker);

    @Mock private WriteTransaction writeTransaction;
    @Mock CheckedFuture<Void, TransactionCommitFailedException> commitFuture;

    private AclBuilder aclBuilder;

    @Before
    public void initialTest() {
        when(writeTransaction.submit()).thenReturn(commitFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);

    }

    @Test
    public void testProgramTrafficBehaviorRule() throws Exception {
        pipelineAclHandler.programTrafficBehaviorRule(Long.valueOf(1), TrafficBehavior.NeedAcl, true);
        pipelineAclHandler.programTrafficBehaviorRule(Long.valueOf(1), TrafficBehavior.NeedAcl, false);

        pipelineAclHandler.programTrafficBehaviorRule(Long.valueOf(1), TrafficBehavior.Normal, true);
        pipelineAclHandler.programTrafficBehaviorRule(Long.valueOf(1), TrafficBehavior.Normal, false);
    }

    @Test
    public void testProgramGpeTunnelInEntry() throws Exception {
        pipelineAclHandler.programGpeTunnelInEntry(Long.valueOf(1), Long.valueOf(2), Long.valueOf(7), true);
        pipelineAclHandler.programGpeTunnelInEntry(Long.valueOf(1), Long.valueOf(2), Long.valueOf(7), false);
    }

    @Test
    public void testProgramTrafficInBridgeDomain() throws Exception {
        pipelineAclHandler.programTrafficInBridgeDomain(Long.valueOf(1), Long.valueOf(2), true);
        pipelineAclHandler.programTrafficInBridgeDomain(Long.valueOf(1), Long.valueOf(2), false);
    }

    @Test
    public void testProgramAclEntryEthPermit() throws Exception {
        aclBuilder = new AclBuilder();

        aclBuilder.setAclName("acl-eth");

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();

        AceBuilder aceBuilder = new AceBuilder();

        aceBuilder.setRuleName("rule-eth");

        MatchesBuilder matchesBuilder = new MatchesBuilder();

        AceEthBuilder aceEthBuilder = new AceEthBuilder();
        aceEthBuilder.setDestinationMacAddress(new MacAddress("00:00:00:00:00:01"));
        aceEthBuilder.setDestinationMacAddressMask(null);
        aceEthBuilder.setSourceMacAddress(new MacAddress("00:00:00:00:00:02"));
        aceEthBuilder.setSourceMacAddressMask(null);

        matchesBuilder.setAceType(aceEthBuilder.build());

        aceBuilder.setMatches(matchesBuilder.build());

        ActionsBuilder actionsBuilder = new ActionsBuilder();

        PermitBuilder permitBuilder = new PermitBuilder();
        permitBuilder.setPermit(true);

        actionsBuilder.setPacketHandling(permitBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        List<Ace> aceList = new ArrayList<Ace>();
        aceList.add(aceBuilder.build());
        accessListEntriesBuilder.setAce(aceList);

        aclBuilder.setAccessListEntries(accessListEntriesBuilder.build());

        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), true);
        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), false);

        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), true);
        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), false);
    }

    @Test
    public void testProgramAclEntryEthDeny() throws Exception {
        aclBuilder = new AclBuilder();

        aclBuilder.setAclName("acl-eth");

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();

        AceBuilder aceBuilder = new AceBuilder();

        aceBuilder.setRuleName("rule-eth");

        MatchesBuilder matchesBuilder = new MatchesBuilder();

        AceEthBuilder aceEthBuilder = new AceEthBuilder();
        aceEthBuilder.setDestinationMacAddress(new MacAddress("00:00:00:00:00:01"));
        aceEthBuilder.setDestinationMacAddressMask(null);
        aceEthBuilder.setSourceMacAddress(new MacAddress("00:00:00:00:00:02"));
        aceEthBuilder.setSourceMacAddressMask(null);

        matchesBuilder.setAceType(aceEthBuilder.build());

        aceBuilder.setMatches(matchesBuilder.build());

        ActionsBuilder actionsBuilder = new ActionsBuilder();

        DenyBuilder denyBuilder = new DenyBuilder();
        denyBuilder.setDeny(true);

        actionsBuilder.setPacketHandling(denyBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        List<Ace> aceList = new ArrayList<Ace>();
        aceList.add(aceBuilder.build());
        accessListEntriesBuilder.setAce(aceList);

        aclBuilder.setAccessListEntries(accessListEntriesBuilder.build());

        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), true);
        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), false);

        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), true);
        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), false);
    }

    @Test
    public void testProgramAclEntryEthRedirect() throws Exception {
        aclBuilder = new AclBuilder();

        aclBuilder.setAclName("acl-eth");

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();

        AceBuilder aceBuilder = new AceBuilder();

        aceBuilder.setRuleName("rule-eth");

        MatchesBuilder matchesBuilder = new MatchesBuilder();

        AceEthBuilder aceEthBuilder = new AceEthBuilder();
        aceEthBuilder.setDestinationMacAddress(new MacAddress("00:00:00:00:00:01"));
        aceEthBuilder.setDestinationMacAddressMask(null);
        aceEthBuilder.setSourceMacAddress(new MacAddress("00:00:00:00:00:02"));
        aceEthBuilder.setSourceMacAddressMask(null);

        matchesBuilder.setAceType(aceEthBuilder.build());

        aceBuilder.setMatches(matchesBuilder.build());
        ActionsBuilder actionsBuilder = new ActionsBuilder();

        RedirectBuilder redirectBuilder = new RedirectBuilder();

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        NshBuilder nshBuilder = new NshBuilder();

        nshBuilder.setDestIp(new IpAddress(new Ipv4Address("192.168.2.3")));
        nshBuilder.setDestPort(new PortNumber(Integer.valueOf(80)));
        nshBuilder.setNsi((short) 255);
        nshBuilder.setNsp(Long.valueOf(12));
        tunnelBuilder.setTunnelType(nshBuilder.build());
        redirectBuilder.setRedirectType(tunnelBuilder.build());

        actionsBuilder.setPacketHandling(redirectBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        List<Ace> aceList = new ArrayList<Ace>();
        aceList.add(aceBuilder.build());
        accessListEntriesBuilder.setAce(aceList);

        aclBuilder.setAccessListEntries(accessListEntriesBuilder.build());

        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), true);
        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), false);

        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), true);
        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), false);
    }

    @Test
    public void testProgramAclEntryIcmpRedirect() throws Exception {
        aclBuilder = new AclBuilder();

        aclBuilder.setAclName("acl-icmp");

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();

        AceBuilder aceBuilder = new AceBuilder();

        aceBuilder.setRuleName("rule-icmp");

        MatchesBuilder matchesBuilder = new MatchesBuilder();

        AceIpBuilder aceIpBuilder = new AceIpBuilder();
        AceIpv4Builder aceIpv4Builder = new AceIpv4Builder();

        aceIpv4Builder.setDestinationIpv4Network(new Ipv4Prefix("192.168.2.3/24"));
        aceIpv4Builder.setSourceIpv4Network(new Ipv4Prefix("192.168.2.3/24"));
        aceIpBuilder.setAceIpVersion(aceIpv4Builder.build());
        aceIpBuilder.setProtocol((short)1);

        matchesBuilder.setAceType(aceIpBuilder.build());

        aceBuilder.setMatches(matchesBuilder.build());
        ActionsBuilder actionsBuilder = new ActionsBuilder();

        RedirectBuilder redirectBuilder = new RedirectBuilder();

        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        NshBuilder nshBuilder = new NshBuilder();

        nshBuilder.setDestIp(new IpAddress(new Ipv4Address("10.10.10.10")));
        nshBuilder.setDestPort(new PortNumber(Integer.valueOf(6633)));
        nshBuilder.setNsi((short) 255);
        nshBuilder.setNsp(Long.valueOf(12));
        tunnelBuilder.setTunnelType(nshBuilder.build());
        redirectBuilder.setRedirectType(tunnelBuilder.build());

        actionsBuilder.setPacketHandling(redirectBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        List<Ace> aceList = new ArrayList<Ace>();
        aceList.add(aceBuilder.build());
        accessListEntriesBuilder.setAce(aceList);

        aclBuilder.setAccessListEntries(accessListEntriesBuilder.build());

        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), true);
        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), false);

        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), true);
        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), false);
    }

    @Test
    public void testProgramAclEntryTcpPermit() throws Exception {
        aclBuilder = new AclBuilder();

        aclBuilder.setAclName("acl-tcp");

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();

        AceBuilder aceBuilder = new AceBuilder();

        aceBuilder.setRuleName("rule-tcp");

        MatchesBuilder matchesBuilder = new MatchesBuilder();

        AceIpBuilder aceIpBuilder = new AceIpBuilder();
        AceIpv4Builder aceIpv4Builder = new AceIpv4Builder();

        aceIpv4Builder.setDestinationIpv4Network(new Ipv4Prefix("192.168.2.3/24"));
        aceIpv4Builder.setSourceIpv4Network(new Ipv4Prefix("192.168.2.3/24"));
        aceIpBuilder.setAceIpVersion(aceIpv4Builder.build());
        aceIpBuilder.setProtocol((short)6);

        DestinationPortRangeBuilder destinationPortRangeBuilder = new DestinationPortRangeBuilder();
        destinationPortRangeBuilder.setLowerPort(new PortNumber(80));
        aceIpBuilder.setDestinationPortRange(destinationPortRangeBuilder.build());

        SourcePortRangeBuilder sourcePortRangeBuilder = new SourcePortRangeBuilder();
        sourcePortRangeBuilder.setLowerPort(new PortNumber(0));
        aceIpBuilder.setSourcePortRange(sourcePortRangeBuilder.build());

        matchesBuilder.setAceType(aceIpBuilder.build());

        aceBuilder.setMatches(matchesBuilder.build());
        ActionsBuilder actionsBuilder = new ActionsBuilder();

        PermitBuilder permitBuilder = new PermitBuilder();
        permitBuilder.setPermit(true);

        actionsBuilder.setPacketHandling(permitBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        List<Ace> aceList = new ArrayList<Ace>();
        aceList.add(aceBuilder.build());
        accessListEntriesBuilder.setAce(aceList);

        aclBuilder.setAccessListEntries(accessListEntriesBuilder.build());

        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), true);
        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), false);

        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), true);
        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), false);
    }

    @Test
    public void testProgramAclEntryUdpPermit() throws Exception {
        aclBuilder = new AclBuilder();

        aclBuilder.setAclName("acl-udp");

        AccessListEntriesBuilder accessListEntriesBuilder = new AccessListEntriesBuilder();

        AceBuilder aceBuilder = new AceBuilder();

        aceBuilder.setRuleName("rule-udp");

        MatchesBuilder matchesBuilder = new MatchesBuilder();

        AceIpBuilder aceIpBuilder = new AceIpBuilder();
        AceIpv4Builder aceIpv4Builder = new AceIpv4Builder();

        aceIpv4Builder.setDestinationIpv4Network(new Ipv4Prefix("192.168.2.3/24"));
        aceIpv4Builder.setSourceIpv4Network(new Ipv4Prefix("192.168.2.3/24"));
        aceIpBuilder.setAceIpVersion(aceIpv4Builder.build());
        aceIpBuilder.setProtocol((short)17);

        DestinationPortRangeBuilder destinationPortRangeBuilder = new DestinationPortRangeBuilder();
        destinationPortRangeBuilder.setLowerPort(new PortNumber(80));
        aceIpBuilder.setDestinationPortRange(destinationPortRangeBuilder.build());

        SourcePortRangeBuilder sourcePortRangeBuilder = new SourcePortRangeBuilder();
        sourcePortRangeBuilder.setLowerPort(new PortNumber(0));
        aceIpBuilder.setSourcePortRange(sourcePortRangeBuilder.build());

        matchesBuilder.setAceType(aceIpBuilder.build());

        aceBuilder.setMatches(matchesBuilder.build());
        ActionsBuilder actionsBuilder = new ActionsBuilder();

        PermitBuilder permitBuilder = new PermitBuilder();
        permitBuilder.setPermit(true);

        actionsBuilder.setPacketHandling(permitBuilder.build());
        aceBuilder.setActions(actionsBuilder.build());

        List<Ace> aceList = new ArrayList<Ace>();
        aceList.add(aceBuilder.build());
        accessListEntriesBuilder.setAce(aceList);

        aclBuilder.setAccessListEntries(accessListEntriesBuilder.build());

        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), true);
        pipelineAclHandler.programBridgeDomainAclEntry(Long.valueOf(11), Long.valueOf(2), aclBuilder.build(), false);

        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), true);
        pipelineAclHandler.programBridgePortAclEntry(Long.valueOf(11), Long.valueOf(22), aclBuilder.build(), false);
    }

}
