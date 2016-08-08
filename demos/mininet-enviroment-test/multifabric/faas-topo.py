#!/usr/bin/python

import re
import os
from mininet.cli import CLI
from mininet.log import lg, info
from mininet.log import setLogLevel, info,error
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.link import Intf
from mininet.node import RemoteController, OVSKernelSwitch,Controller

# learning mac from arp request for spine device
SPINE_ARP_LEARNING = "ovs-ofctl add-flow -OOpenFlow13 %s 'table=10, priority=1024, reg0=0x3, arp, arp_op=1 actions=learn(table=110,priority=1024,NXM_NX_TUN_ID[],NXM_OF_ETH_DST[]=NXM_OF_ETH_SRC[],load:NXM_OF_VLAN_TCI[]->NXM_OF_VLAN_TCI[],output:NXM_OF_IN_PORT[]),goto_table:20'"
# learning mac from normal packets for spine device
SPINE_NORMAL_LEARNING = "ovs-ofctl add-flow -OOpenFlow13 %s 'table=10, priority=1023, reg0=0x3, actions=learn(table=110,priority=1024,NXM_NX_TUN_ID[],NXM_OF_ETH_DST[]=NXM_OF_ETH_SRC[],load:NXM_OF_VLAN_TCI[]->NXM_OF_VLAN_TCI[],output:NXM_OF_IN_PORT[]),load:0->NXM_OF_VLAN_TCI[],goto_table:20'"
# learning mac for leaf device
LEAF_LEARNING = "ovs-ofctl add-flow -OOpenFlow13 %s 'table=10, priority=1024, reg0=0x2, actions=learn(table=110,priority=1024,NXM_NX_TUN_ID[],NXM_OF_ETH_DST[]=NXM_OF_ETH_SRC[],load:NXM_NX_TUN_IPV4_SRC[]->NXM_NX_TUN_IPV4_DST[],output:NXM_OF_IN_PORT[]),goto_table:20'"

HOSTX_MAC = '62:02:1a:00:00:02'
HOSTX_IP = '192.168.2.2'
FLOATING_IP_1 = '192.168.1.2'
HOSTX_GW_MAC = '62:02:1a:00:00:01'
HOSTX_GW_MAC_HEX = '0x62021a000001'
HOSTX_GW = '192.168.2.1'
HOSTX_GW_HEX = '0xc0a80201'
FLOATING_IP_GW_MAC = '80:38:bc:a1:33:c8'
FIXED_IP_GW_MAC = '80:38:bc:a1:33:c7'

S3_INBOUND_FLOW = "ovs-ofctl add-flow -OOpenFlow13 s3 'table=0,in_port=1,ip, nw_src=%s, nw_dst=%s actions=set_field:%s->eth_dst, set_field:%s->eth_src, output:2'"
S3_OUTBOUND_FLOW = "ovs-ofctl add-flow -OOpenFlow13 s3 'table=0,in_port=2,ip, nw_src=%s, nw_dst=%s actions=set_field:%s->eth_dst, set_field:%s->eth_src, output:1'"
S3_ARP_RESPONDER = "ovs-ofctl add-flow -OOpenFlow13 s3 'table=0, arp,arp_tpa=%s,arp_op=1 actions=move:NXM_OF_ETH_SRC[]->NXM_OF_ETH_DST[],set_field:%s->eth_src,load:0x2->NXM_OF_ARP_OP[],move:NXM_NX_ARP_SHA[]->NXM_NX_ARP_THA[],move:NXM_OF_ARP_SPA[]->NXM_OF_ARP_TPA[],load:%s->NXM_NX_ARP_SHA[],load:%s->NXM_OF_ARP_SPA[],IN_PORT'"

class FabricTopo( Topo ):
    "Simple Fabric Topology example."

    def __init__( self, **opts ):
        "Create fabric topo."

        super(FabricTopo, self).__init__(**opts)

        host1 = self.addHost('h1', mac='62:02:1a:00:b7:11', ip='172.16.1.11/24', defaultRoute='via 172.16.1.1')
        host2 = self.addHost('h2', mac='62:02:1a:00:b7:12', ip='172.16.1.12/24', defaultRoute='via 172.16.1.1')

        switch11 = self.addSwitch('s11')

        self.addLink(host1, switch11)
        self.addLink(host2, switch11)

        host3 = self.addHost('h3', mac='62:02:1a:00:b7:13', ip='172.16.1.13/24', defaultRoute='via 172.16.1.1')
        host4 = self.addHost('h4', mac='62:02:1a:00:b7:22', ip='172.16.2.22/24', defaultRoute='via 172.16.2.1')

        switch12 = self.addSwitch('s12')

        self.addLink(host3, switch12)
        self.addLink(host4, switch12)

        switch1 = self.addSwitch('s1')

        switch2 = self.addSwitch('s2')
        self.addLink(switch1, switch2)

        switch21 = self.addSwitch('s21')
        host5 = self.addHost('h5',  mac='62:02:1a:00:b7:14', ip='172.16.1.14/24', defaultRoute='via 172.16.1.1')
        host6 = self.addHost('h6',  mac='62:02:1a:00:b7:23', ip='172.16.2.23/24', defaultRoute='via 172.16.2.1')
        self.addLink(host5, switch21)
        self.addLink(host6, switch21)

        switch3 = self.addSwitch('s3')
        self.addLink(switch2, switch3)

        hostx = self.addHost('hostx', mac=HOSTX_MAC, ip='192.168.2.2/24', defaultRoute='via 192.168.2.1') 
        self.addLink(hostx, switch3)

topos = { 'fabric' : FabricTopo}

if __name__ == '__main__':
    lg.setLogLevel( 'info' )

    os.system("ovs-vsctl set-manager tcp:127.0.0.1:6640")

    info( "*** Initializing Mininet and kernel modules\n" )
    OVSKernelSwitch.setup()

    info( "*** Creating network\n" )
    network = Mininet( FabricTopo( ), switch=OVSKernelSwitch ,controller=None)

    network.addController(name='c0', controller=RemoteController, ip='127.0.0.1')

    info( "*** Starting network\n" )
    network.start()

    # setup vtep
    os.system("ifconfig s1 192.168.20.101")
    os.system("ovs-vsctl br-set-external-id s1 vtep-ip 192.168.20.101")
    os.system("ifconfig s11 192.168.20.111")
    os.system("ovs-vsctl br-set-external-id s11 vtep-ip 192.168.20.111")
    os.system("ifconfig s12 192.168.20.112")
    os.system("ovs-vsctl br-set-external-id s12 vtep-ip 192.168.20.112")
    os.system("ifconfig s2 192.168.20.102")
    os.system("ovs-vsctl br-set-external-id s2 vtep-ip 192.168.20.102")
    os.system("ifconfig s21 192.168.20.121")
    os.system("ovs-vsctl br-set-external-id s21 vtep-ip 192.168.20.121")
    os.system("ifconfig s3 192.168.20.103")
    os.system("ovs-vsctl br-set-external-id s3 vtep-ip 192.168.20.103")

    #----------------------------------- Learning flow, because ODL openflowplugin did not support "learn Action -------------------------------------
    os.system(SPINE_ARP_LEARNING % 's1')
    os.system(SPINE_NORMAL_LEARNING % 's1')
    os.system(SPINE_ARP_LEARNING % 's2')
    os.system(SPINE_NORMAL_LEARNING % 's2')

    os.system(LEAF_LEARNING % 's11')
    os.system(LEAF_LEARNING % 's12')
    os.system(LEAF_LEARNING % 's21')

    #----------------------------------- OVS s3 flow, used for test NAT function -------------------------------------
    os.system(S3_INBOUND_FLOW % (FLOATING_IP_1, HOSTX_IP, HOSTX_MAC, HOSTX_GW_MAC))
    os.system(S3_OUTBOUND_FLOW % (HOSTX_IP, FLOATING_IP_1, FIXED_IP_GW_MAC, FLOATING_IP_GW_MAC))
    os.system(S3_ARP_RESPONDER % (HOSTX_GW, HOSTX_GW_MAC, HOSTX_GW_MAC_HEX, HOSTX_GW_HEX))

    info( "*** Starting CLI (type 'exit' to exit)\n" )
    CLI( network )

    info( "*** Stopping network\n" )
    network.stop()
